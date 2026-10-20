package com.alex.finance.message.service.impl;

import com.alex.api.finance.cpnCouponInfo.vo.CpnCouponInfoVo;
import com.alex.api.finance.message.vo.SysMessageInfoVo;
import com.alex.api.finance.message.vo.SysMessageSummaryVo;
import com.alex.api.finance.prepaidCardInfoT.vo.PrepaidCardInfoTVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.exception.FinanceException;
import com.alex.common.utils.date.DateUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.finance.accountRecordInfo.entity.AccountRecordInfo;
import com.alex.finance.accountRecordInfo.mapper.AccountRecordInfoMapper;
import com.alex.finance.cpnCouponInfo.mapper.CpnCouponInfoMapper;
import com.alex.finance.gift.event.entity.GiftEventInfo;
import com.alex.finance.gift.event.mapper.GiftEventInfoMapper;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import com.alex.finance.message.entity.SysMessageInfo;
import com.alex.finance.message.mapper.SysMessageInfoMapper;
import com.alex.finance.message.service.SysMessageInfoService;
import com.alex.finance.prepaidCardInfoT.mapper.PrepaidCardInfoTMapper;
import com.alex.finance.wechat.service.WeChatService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 系统消息与多业务到期提醒服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMessageInfoServiceImpl extends ServiceImpl<SysMessageInfoMapper, SysMessageInfo>
        implements SysMessageInfoService {

    public static final String MSG_TYPE_SYSTEM = "SYSTEM";
    public static final String MSG_TYPE_GIFT_EVENT = "GIFT_EVENT";
    public static final String MSG_TYPE_COUPON_EXPIRE = "COUPON_EXPIRE";
    public static final String MSG_TYPE_CARD_EXPIRE = "CARD_EXPIRE";
    public static final String MSG_TYPE_GIFT_RETURN = "GIFT_RETURN";

    public static final String SUB_TYPE_WELCOME = "WELCOME";
    public static final String SUB_TYPE_ANNOUNCE = "ANNOUNCE";
    public static final String SUB_TYPE_D_DAY = "D_DAY";
    public static final String SUB_TYPE_D_MINUS_1 = "D_MINUS_1";
    public static final String SUB_TYPE_D_MINUS_3 = "D_MINUS_3";
    public static final String SUB_TYPE_IMBALANCE = "IMBALANCE";
    public static final String SUB_TYPE_EVENT_RETURN = "EVENT_RETURN";

    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_MEDIUM = "MEDIUM";
    public static final String PRIORITY_NORMAL = "NORMAL";

    public static final String BIZ_TYPE_SYSTEM = "SYSTEM";
    public static final String BIZ_TYPE_GIFT_EVENT = "GIFT_EVENT";
    public static final String BIZ_TYPE_COUPON = "COUPON";
    public static final String BIZ_TYPE_ACCOUNT_RECORD = "ACCOUNT_RECORD";
    public static final String BIZ_TYPE_PREPAID_CARD = "PREPAID_CARD";
    public static final String BIZ_TYPE_GIFT_RETURN = "GIFT_RETURN";

    public static final String JUMP_GIFT_EVENT = "/finance/gift/event";
    public static final String JUMP_COUPON = "/cpn-coupon/cpn-coupon-info";
    public static final String JUMP_ACCOUNT_RECORD = "/finance/accountRecordInfo";
    public static final String JUMP_PREPAID_CARD = "/selfFinance/prepaidCardInfoT";
    public static final String JUMP_GIFT_ANALYSIS = "/finance/gift/analysis";

    private final GiftDataScopeSupport scopeSupport;
    private final GiftEventInfoMapper giftEventInfoMapper;
    private final CpnCouponInfoMapper cpnCouponInfoMapper;
    private final AccountRecordInfoMapper accountRecordInfoMapper;
    private final PrepaidCardInfoTMapper prepaidCardInfoTMapper;
    private final com.alex.finance.gift.record.mapper.GiftRecordInfoMapper giftRecordInfoMapper;
    private final com.alex.finance.gift.person.mapper.GiftPersonInfoMapper giftPersonInfoMapper;
    private final WeChatService weChatService;

    private final AtomicBoolean tableChecked = new AtomicBoolean(false);

    private void ensureTableInitialized() {
        if (tableChecked.compareAndSet(false, true)) {
            try {
                baseMapper.ensureTableExists();
            } catch (Exception e) {
                log.warn("自动检查或创建 sys_message_info_t 表失败，将继续使用已有表结构: {}", e.getMessage());
            }
        }
    }

    /**
     * XXL-Job 后台无登录态全量扫描任务：
     * 按每条业务记录自身的归属人 (user_id / org_id / creator) 生成到期提醒消息并联动微信模板消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int executeRemindJob() {
        ensureTableInitialized();
        LocalDateTime now = DateUtils.now();
        LocalDate today = now.toLocalDate();

        Map<String, SysMessageInfo> candidateMap = new LinkedHashMap<>();

        // 1. 扫描全量未删除的礼尚往来事件（提前 1 天、当天），走 BaseMapper.selectList 绕过登录态拦截器
        collectGiftEventRemindersForJob(today, candidateMap);

        // 2. 扫描全量未核销完消费券（剩余 0~3 天）
        collectCouponExpireReminders(today, candidateMap);

        // 3. 扫描猫超卡（账户记录）与预付卡到期提醒（剩余 0~3 天且余额 > 0）
        collectAccountRecordExpireReminders(today, candidateMap);
        collectPrepaidCardExpireReminders(today, candidateMap);

        // 4. 扫描礼尚往来失衡与回礼备忘提醒（净盈余 >= 800且超180天无互动，按月幂等提醒）
        collectGiftReturnRemindersForJob(today, candidateMap);

        return persistCandidates(candidateMap);
    }

    private int persistCandidates(Map<String, SysMessageInfo> candidateMap) {
        if (candidateMap.isEmpty()) {
            return 0;
        }
        List<String> existingKeys = baseMapper.selectExistingDedupKeys(candidateMap.keySet());
        Set<String> existingSet = existingKeys == null ? Collections.emptySet() : new HashSet<>(existingKeys);

        List<SysMessageInfo> toInsert = candidateMap.values().stream()
                .filter(item -> !existingSet.contains(item.getDedupKey()))
                .collect(Collectors.toList());

        int inserted = 0;
        for (SysMessageInfo entity : toInsert) {
            try {
                baseMapper.insert(entity);
                inserted++;
                notifyWeChatSafely(entity);
            } catch (Exception e) {
                log.debug("跳过重复提醒 dedupKey={}: {}", entity.getDedupKey(), e.getMessage());
            }
        }
        return inserted;
    }

    private void notifyWeChatSafely(SysMessageInfo entity) {
        if (weChatService == null || entity == null || MSG_TYPE_SYSTEM.equals(entity.getMsgType())) {
            return;
        }
        // 猫超卡在 AccountRecordNoticeJob 中已有汇总微信提醒，此处聚焦礼尚往来事件、消费券到期与预付卡到期
        if (BIZ_TYPE_ACCOUNT_RECORD.equals(entity.getBizType())) {
            return;
        }
        String remindType;
        if (BIZ_TYPE_GIFT_EVENT.equals(entity.getBizType())) {
            remindType = "礼尚往来事件";
        } else if (BIZ_TYPE_GIFT_RETURN.equals(entity.getBizType())) {
            remindType = "人情回礼备忘";
        } else if (BIZ_TYPE_COUPON.equals(entity.getBizType())) {
            remindType = "消费券到期";
        } else if (BIZ_TYPE_PREPAID_CARD.equals(entity.getBizType())) {
            remindType = "消费卡到期";
        } else {
            remindType = "到期提醒";
        }

        String stageText;
        if (BIZ_TYPE_GIFT_RETURN.equals(entity.getBizType())) {
            stageText = "礼金盈余待回";
        } else if (SUB_TYPE_D_DAY.equals(entity.getSubType())) {
            stageText = BIZ_TYPE_GIFT_EVENT.equals(entity.getBizType()) ? "今日举行" : "今日到期";
        } else if (SUB_TYPE_D_MINUS_1.equals(entity.getSubType())) {
            stageText = BIZ_TYPE_GIFT_EVENT.equals(entity.getBizType()) ? "明日举行" : "明日到期(剩1天)";
        } else {
            stageText = "临期提醒(3天内)";
        }

        try {
            weChatService.sendRemindTemplateMessage(entity.getTitle(), stageText, remindType);
        } catch (Exception e) {
            log.warn("发送微信模板消息提醒失败(不影响站内信入库) title={}, error={}", entity.getTitle(), e.getMessage());
        }
    }

    private void collectGiftEventRemindersForJob(LocalDate today, Map<String, SysMessageInfo> candidateMap) {
        try {
            LocalDateTime startOfToday = today.atStartOfDay();
            LocalDateTime endOfTomorrow = today.plusDays(1).atTime(23, 59, 59);
            LambdaQueryWrapper<GiftEventInfo> wrapper = new LambdaQueryWrapper<GiftEventInfo>()
                    .ge(GiftEventInfo::getEventTime, startOfToday)
                    .le(GiftEventInfo::getEventTime, endOfTomorrow);
            List<GiftEventInfo> events = giftEventInfoMapper.selectList(wrapper);
            if (events == null || events.isEmpty()) {
                return;
            }
            for (GiftEventInfo event : events) {
                if (event == null || event.getId() == null || event.getEventTime() == null) {
                    continue;
                }
                Long targetUserId = event.getUserId() != null
                        ? event.getUserId()
                        : (event.getCreator() != null ? event.getCreator() : 0L);
                Long targetOrgId = event.getOrgId();
                appendGiftEventCandidate(event, targetUserId, targetOrgId, today, candidateMap);
            }
        } catch (Exception e) {
            log.warn("XXL-Job 巡检礼尚往来事件提醒异常: {}", e.getMessage());
        }
    }

    private void appendGiftEventCandidate(
            GiftEventInfo event,
            Long targetUserId,
            Long targetOrgId,
            LocalDate today,
            Map<String, SysMessageInfo> candidateMap
    ) {
        LocalDate eventDate = event.getEventTime().toLocalDate();
        long daysDiff = ChronoUnit.DAYS.between(today, eventDate);
        if (daysDiff != 0L && daysDiff != 1L) {
            return;
        }
        String subType = daysDiff == 0L ? SUB_TYPE_D_DAY : SUB_TYPE_D_MINUS_1;
        String priority = daysDiff == 0L ? PRIORITY_HIGH : PRIORITY_MEDIUM;
        String dateStr = DateUtils.FORMATTER_YYYY_MM_DD.format(eventDate);
        String eventTypeSuffix = StringUtils.isNotEmpty(event.getEventType())
                ? "（" + event.getEventType() + "）"
                : "";
        String title = daysDiff == 0L
                ? String.format("【今日事件】%s%s今天举行", event.getEventName(), eventTypeSuffix)
                : String.format("【明日提醒】%s%s将于明天举行", event.getEventName(), eventTypeSuffix);
        String remarkPart = StringUtils.isNotEmpty(event.getRemark())
                ? "，备注：" + event.getRemark()
                : "";
        String content = String.format("礼尚往来事件「%s」定于 %s 举行%s，请提前做好随礼或接待准备。",
                event.getEventName(), dateStr, remarkPart);

        Long safeUserId = targetUserId == null ? 0L : targetUserId;
        String dedupKey = buildDedupKey(BIZ_TYPE_GIFT_EVENT, String.valueOf(event.getId()), subType, today, safeUserId);
        SysMessageInfo msg = new SysMessageInfo()
                .setUserId(safeUserId)
                .setOrgId(targetOrgId)
                .setMsgType(MSG_TYPE_GIFT_EVENT)
                .setSubType(subType)
                .setPriority(priority)
                .setTitle(title)
                .setContent(content)
                .setBizId(String.valueOf(event.getId()))
                .setBizType(BIZ_TYPE_GIFT_EVENT)
                .setJumpUrl(JUMP_GIFT_EVENT)
                .setReadStatus(0)
                .setRemindDate(today)
                .setDedupKey(dedupKey);
        candidateMap.put(dedupKey, msg);
    }

    private void collectCouponExpireReminders(
            LocalDate today,
            Map<String, SysMessageInfo> candidateMap
    ) {
        try {
            CpnCouponInfoVo query = new CpnCouponInfoVo().setOnlyValidAndNotFullyRedeemed(true);
            List<CpnCouponInfoVo> coupons = cpnCouponInfoMapper.getList(query);
            if (coupons == null || coupons.isEmpty()) {
                return;
            }
            for (CpnCouponInfoVo coupon : coupons) {
                if (coupon == null || coupon.getId() == null || coupon.getEndDate() == null) {
                    continue;
                }
                int remaining = coupon.getRemainingQuantity() == null ? 0 : coupon.getRemainingQuantity();
                if (remaining <= 0) {
                    continue;
                }
                LocalDate endDate = coupon.getEndDate().toLocalDate();
                long daysDiff = ChronoUnit.DAYS.between(today, endDate);
                if (daysDiff < 0L || daysDiff > 3L) {
                    continue;
                }
                String subType = resolveExpireSubType(daysDiff);
                String priority = resolveExpirePriority(daysDiff);
                String endDateStr = DateUtils.FORMATTER_YYYY_MM_DD.format(endDate);
                String unitValueStr = coupon.getUnitValue() != null ? "¥" + coupon.getUnitValue().stripTrailingZeros().toPlainString() : "";
                String title;
                if (daysDiff == 0L) {
                    title = String.format("【今日到期】消费券「%s」今天即将过期", coupon.getCouponName());
                } else if (daysDiff == 1L) {
                    title = String.format("【明日到期】消费券「%s」仅剩 1 天有效期", coupon.getCouponName());
                } else {
                    title = String.format("【临期提醒】消费券「%s」将于 %d 天后到期", coupon.getCouponName(), daysDiff);
                }
                String content = String.format("您持有的消费券「%s」%s还有 %d 张未核销，有效期至 %s，请及时使用以免过期作废。",
                        coupon.getCouponName(),
                        StringUtils.isNotEmpty(unitValueStr) ? "（面值 " + unitValueStr + "）" : "",
                        remaining,
                        endDateStr);

                Long targetUserId = coupon.getCreator() != null ? coupon.getCreator() : 0L;
                String dedupKey = buildDedupKey(BIZ_TYPE_COUPON, String.valueOf(coupon.getId()), subType, today, targetUserId);
                SysMessageInfo msg = new SysMessageInfo()
                        .setUserId(targetUserId)
                        .setMsgType(MSG_TYPE_COUPON_EXPIRE)
                        .setSubType(subType)
                        .setPriority(priority)
                        .setTitle(title)
                        .setContent(content)
                        .setBizId(String.valueOf(coupon.getId()))
                        .setBizType(BIZ_TYPE_COUPON)
                        .setJumpUrl(JUMP_COUPON)
                        .setReadStatus(0)
                        .setRemindDate(today)
                        .setDedupKey(dedupKey);
                candidateMap.put(dedupKey, msg);
            }
        } catch (Exception e) {
            log.warn("巡检消费券到期提醒异常: {}", e.getMessage());
        }
    }

    private void collectAccountRecordExpireReminders(
            LocalDate today,
            Map<String, SysMessageInfo> candidateMap
    ) {
        try {
            LocalDateTime startOfToday = today.atStartOfDay();
            LocalDateTime endOfWindow = today.plusDays(3).atTime(23, 59, 59);
            LambdaQueryWrapper<AccountRecordInfo> wrapper = new LambdaQueryWrapper<AccountRecordInfo>()
                    .ge(AccountRecordInfo::getAvliDate, startOfToday)
                    .le(AccountRecordInfo::getAvliDate, endOfWindow);
            List<AccountRecordInfo> records = accountRecordInfoMapper.selectList(wrapper);
            if (records == null || records.isEmpty()) {
                return;
            }
            for (AccountRecordInfo record : records) {
                if (record == null || record.getId() == null || record.getAvliDate() == null) {
                    continue;
                }
                if (record.getAmount() != null && record.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                LocalDate expireDate = record.getAvliDate().toLocalDate();
                long daysDiff = ChronoUnit.DAYS.between(today, expireDate);
                if (daysDiff < 0L || daysDiff > 3L) {
                    continue;
                }
                String subType = resolveExpireSubType(daysDiff);
                String priority = resolveExpirePriority(daysDiff);
                String cardName = StringUtils.isNotEmpty(record.getName()) ? record.getName() : "猫超卡/权益金";
                String amountStr = record.getAmount() != null ? "¥" + record.getAmount().stripTrailingZeros().toPlainString() : "待用余额";
                String accountPart = StringUtils.isNotEmpty(record.getAccount()) ? "（账号：" + record.getAccount() + "）" : "";
                String expireStr = DateUtils.FORMATTER_YYYY_MM_DD.format(expireDate);

                String title;
                if (daysDiff == 0L) {
                    title = String.format("【今日到期】%s（%s）今天到期", cardName, amountStr);
                } else if (daysDiff == 1L) {
                    title = String.format("【明日到期】%s（%s）明天到期", cardName, amountStr);
                } else {
                    title = String.format("【猫超卡临期】%s（%s）将于 %d 天后到期", cardName, amountStr, daysDiff);
                }
                String content = String.format("账户记录「%s」%s剩余金额 %s 将于 %s 到期，请尽快前往消费使用。",
                        cardName, accountPart, amountStr, expireStr);

                Long targetUserId = 0L;
                String dedupKey = buildDedupKey(BIZ_TYPE_ACCOUNT_RECORD, String.valueOf(record.getId()), subType, today, targetUserId);
                SysMessageInfo msg = new SysMessageInfo()
                        .setUserId(targetUserId)
                        .setMsgType(MSG_TYPE_CARD_EXPIRE)
                        .setSubType(subType)
                        .setPriority(priority)
                        .setTitle(title)
                        .setContent(content)
                        .setBizId(String.valueOf(record.getId()))
                        .setBizType(BIZ_TYPE_ACCOUNT_RECORD)
                        .setJumpUrl(JUMP_ACCOUNT_RECORD)
                        .setReadStatus(0)
                        .setRemindDate(today)
                        .setDedupKey(dedupKey);
                candidateMap.put(dedupKey, msg);
            }
        } catch (Exception e) {
            log.warn("巡检猫超卡/账户记录到期提醒异常: {}", e.getMessage());
        }
    }

    private void collectPrepaidCardExpireReminders(
            LocalDate today,
            Map<String, SysMessageInfo> candidateMap
    ) {
        try {
            List<PrepaidCardInfoTVo> cards = prepaidCardInfoTMapper.getList(new PrepaidCardInfoTVo());
            if (cards == null || cards.isEmpty()) {
                return;
            }
            for (PrepaidCardInfoTVo card : cards) {
                if (card == null || card.getId() == null || card.getExpireDate() == null) {
                    continue;
                }
                if (card.getCurrentBalance() == null || card.getCurrentBalance().compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                LocalDate expireDate = card.getExpireDate().toLocalDate();
                long daysDiff = ChronoUnit.DAYS.between(today, expireDate);
                if (daysDiff < 0L || daysDiff > 3L) {
                    continue;
                }
                String subType = resolveExpireSubType(daysDiff);
                String priority = resolveExpirePriority(daysDiff);
                String cardName = StringUtils.isNotEmpty(card.getCardName()) ? card.getCardName() : "预付消费卡";
                String balanceStr = "¥" + card.getCurrentBalance().stripTrailingZeros().toPlainString();
                String expireStr = DateUtils.FORMATTER_YYYY_MM_DD.format(expireDate);

                String title;
                if (daysDiff == 0L) {
                    title = String.format("【今日到期】消费卡「%s」今天即将过期", cardName);
                } else if (daysDiff == 1L) {
                    title = String.format("【明日到期】消费卡「%s」明天到期", cardName);
                } else {
                    title = String.format("【消费卡临期】「%s」将于 %d 天后到期", cardName, daysDiff);
                }
                String content = String.format("您的预付卡「%s」当前剩余余额 %s，有效期至 %s，请及时核销使用。",
                        cardName, balanceStr, expireStr);

                Long targetUserId = card.getUserId() != null
                        ? card.getUserId()
                        : (card.getCreator() != null ? card.getCreator() : 0L);
                String dedupKey = buildDedupKey(BIZ_TYPE_PREPAID_CARD, String.valueOf(card.getId()), subType, today, targetUserId);
                SysMessageInfo msg = new SysMessageInfo()
                        .setUserId(targetUserId)
                        .setMsgType(MSG_TYPE_CARD_EXPIRE)
                        .setSubType(subType)
                        .setPriority(priority)
                        .setTitle(title)
                        .setContent(content)
                        .setBizId(String.valueOf(card.getId()))
                        .setBizType(BIZ_TYPE_PREPAID_CARD)
                        .setJumpUrl(JUMP_PREPAID_CARD)
                        .setReadStatus(0)
                        .setRemindDate(today)
                        .setDedupKey(dedupKey);
                candidateMap.put(dedupKey, msg);
            }
        } catch (Exception e) {
            log.warn("巡检预付消费卡到期提醒异常: {}", e.getMessage());
        }
    }

    private String resolveExpireSubType(long daysDiff) {
        if (daysDiff <= 0L) {
            return SUB_TYPE_D_DAY;
        }
        if (daysDiff == 1L) {
            return SUB_TYPE_D_MINUS_1;
        }
        return SUB_TYPE_D_MINUS_3;
    }

    private String resolveExpirePriority(long daysDiff) {
        if (daysDiff <= 0L) {
            return PRIORITY_HIGH;
        }
        if (daysDiff == 1L) {
            return PRIORITY_MEDIUM;
        }
        return PRIORITY_NORMAL;
    }

    /**
     * 巡检礼尚往来失衡与回礼备忘：
     * 针对净盈余 netBalance >= 800 且最近互动超 180 天的亲友，按月生成回礼备忘提醒；
     * 同一月份同一亲友仅提醒一次，避免频繁打扰用户。
     */
    private void collectGiftReturnRemindersForJob(LocalDate today, Map<String, SysMessageInfo> candidateMap) {
        try {
            if (giftRecordInfoMapper == null || giftPersonInfoMapper == null) {
                return;
            }
            List<com.alex.finance.gift.record.entity.GiftRecordInfo> records = giftRecordInfoMapper.selectList(null);
            if (records == null || records.isEmpty()) {
                return;
            }

            // 按 (userId, personId) 分组统计收礼、随礼与最新往来时间
            Map<String, UserPersonGiftAgg> aggMap = new LinkedHashMap<>();
            for (com.alex.finance.gift.record.entity.GiftRecordInfo r : records) {
                if (r == null || r.getUserId() == null || r.getAmount() == null) {
                    continue;
                }
                Long personId = "RECEIVE".equals(r.getDirection()) ? r.getGiverPersonId() : r.getReceiverPersonId();
                if (personId == null) {
                    continue;
                }
                String key = r.getUserId() + "_" + personId;
                UserPersonGiftAgg agg = aggMap.computeIfAbsent(key, k -> new UserPersonGiftAgg(r.getUserId(), r.getOrgId(), personId));
                if ("RECEIVE".equals(r.getDirection())) {
                    agg.totalReceive = agg.totalReceive.add(r.getAmount());
                } else if ("GIVE".equals(r.getDirection()) || "RETURN".equals(r.getDirection())) {
                    agg.totalGive = agg.totalGive.add(r.getAmount());
                }
                if (r.getPayTime() != null) {
                    if (agg.latestPayTime == null || r.getPayTime().isAfter(agg.latestPayTime)) {
                        agg.latestPayTime = r.getPayTime();
                    }
                }
            }

            String currentMonth = DateUtils.FORMATTER_YYYY_MM.format(today);

            for (UserPersonGiftAgg agg : aggMap.values()) {
                BigDecimal net = agg.totalReceive.subtract(agg.totalGive);
                // 净盈余 >= 800
                if (net.compareTo(new BigDecimal("800")) < 0) {
                    continue;
                }
                // 超 180 天无后续往来互动
                if (agg.latestPayTime == null) {
                    continue;
                }
                long daysSince = ChronoUnit.DAYS.between(agg.latestPayTime.toLocalDate(), today);
                if (daysSince <= 180L) {
                    continue;
                }

                // 查询亲友姓名与关系
                com.alex.finance.gift.person.entity.GiftPersonInfo person = giftPersonInfoMapper.selectById(agg.personId);
                String personName = (person != null && StringUtils.isNotEmpty(person.getPersonName())) ? person.getPersonName() : "亲友";
                String relation = (person != null && StringUtils.isNotEmpty(person.getRelationType())) ? person.getRelationType() : "人情往来";

                String title = String.format("【人情回礼备忘】%s 礼金盈余待回礼提醒", personName);
                String content = String.format("您与 %s（%s）累计礼金盈余 ¥%s，且已超 180 天无后续往来互动。建议在适逢节庆或重要节点时适度体面回礼，维系亲友深厚情谊。",
                        personName, relation, net.toPlainString());

                // 按月防重: GIFT_RETURN:{userId}:{personId}:IMBALANCE:{currentMonth}
                String dedupKey = String.format("%s:%d:%s:%s", BIZ_TYPE_GIFT_RETURN, agg.personId, SUB_TYPE_IMBALANCE, currentMonth);

                SysMessageInfo msg = new SysMessageInfo()
                        .setUserId(agg.userId)
                        .setOrgId(agg.orgId)
                        .setMsgType(MSG_TYPE_GIFT_RETURN)
                        .setSubType(SUB_TYPE_IMBALANCE)
                        .setPriority(PRIORITY_MEDIUM)
                        .setTitle(title)
                        .setContent(content)
                        .setBizId(String.valueOf(agg.personId))
                        .setBizType(BIZ_TYPE_GIFT_RETURN)
                        .setJumpUrl(JUMP_GIFT_ANALYSIS)
                        .setReadStatus(0)
                        .setRemindDate(today)
                        .setDedupKey(dedupKey);

                candidateMap.put(dedupKey, msg);
            }
        } catch (Exception e) {
            log.warn("巡检礼尚往来失衡回礼备忘异常: {}", e.getMessage());
        }
    }

    private static class UserPersonGiftAgg {
        final Long userId;
        final Long orgId;
        final Long personId;
        BigDecimal totalReceive = BigDecimal.ZERO;
        BigDecimal totalGive = BigDecimal.ZERO;
        LocalDateTime latestPayTime;

        UserPersonGiftAgg(Long userId, Long orgId, Long personId) {
            this.userId = userId;
            this.orgId = orgId;
            this.personId = personId;
        }
    }

    private String buildDedupKey(String bizType, String bizId, String subType, LocalDate remindDate, Long userId) {
        return String.format("%s:%s:%s:%s:%d",
                bizType,
                bizId,
                subType,
                DateUtils.FORMATTER_YYYY_MM_DD.format(remindDate),
                userId == null ? 0L : userId);
    }

    @Override
    public SysMessageSummaryVo getSummary() {
        ensureTableInitialized();
        TUserVo user = scopeSupport.requireLoginUser();
        Long userId = user.getId();
        Long orgId = scopeSupport.loginOrgId(user);

        List<SysMessageInfoVo> unreadList = baseMapper.selectUnreadList(userId, orgId);
        long systemUnread = 0L;
        long giftEventUnread = 0L;
        long couponExpireUnread = 0L;
        long cardExpireUnread = 0L;

        if (unreadList != null) {
            for (SysMessageInfoVo item : unreadList) {
                if (item == null || item.getMsgType() == null) {
                    continue;
                }
                if (MSG_TYPE_GIFT_EVENT.equals(item.getMsgType())) {
                    giftEventUnread++;
                } else if (MSG_TYPE_COUPON_EXPIRE.equals(item.getMsgType())) {
                    couponExpireUnread++;
                } else if (MSG_TYPE_CARD_EXPIRE.equals(item.getMsgType())) {
                    cardExpireUnread++;
                } else {
                    systemUnread++;
                }
            }
        }

        long total = systemUnread + giftEventUnread + couponExpireUnread + cardExpireUnread;
        return new SysMessageSummaryVo()
                .setUnreadTotal(total)
                .setSystemUnread(systemUnread)
                .setGiftEventUnread(giftEventUnread)
                .setCouponExpireUnread(couponExpireUnread)
                .setCardExpireUnread(cardExpireUnread);
    }

    @Override
    public Page<SysMessageInfoVo> getPage(Long pageNum, Long pageSize, SysMessageInfoVo query) {
        ensureTableInitialized();
        long current = pageNum == null || pageNum < 1 ? 1L : pageNum;
        long size = pageSize == null || pageSize < 1 ? 10L : pageSize;
        TUserVo user = scopeSupport.requireLoginUser();
        Page<SysMessageInfoVo> page = new Page<>(current, size);
        return baseMapper.getPage(page, query == null ? new SysMessageInfoVo() : query, user.getId(), scopeSupport.loginOrgId(user));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean markRead(Long id) {
        ensureTableInitialized();
        if (id == null) {
            throw FinanceException.param("消息ID不能为空");
        }
        SysMessageInfo entity = baseMapper.selectById(id);
        assertMessageAccessible(entity);
        if (Integer.valueOf(1).equals(entity.getReadStatus())) {
            return true;
        }
        entity.setReadStatus(1)
                .setReadTime(DateUtils.now());
        return baseMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer markAllRead(String msgType) {
        ensureTableInitialized();
        TUserVo user = scopeSupport.requireLoginUser();
        return baseMapper.markAllRead(user.getId(), scopeSupport.loginOrgId(user), msgType, DateUtils.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysMessageInfoVo publishSystemMessage(SysMessageInfoVo vo) {
        ensureTableInitialized();
        if (vo == null || vo.getTitle() == null || vo.getContent() == null) {
            throw FinanceException.param("消息标题和正文不能为空");
        }
        String title = vo.getTitle().trim();
        String content = vo.getContent().trim();
        if (StringUtils.isEmpty(title) || StringUtils.isEmpty(content)) {
            throw FinanceException.param("消息标题和正文不能为空");
        }
        if (title.length() > 200) {
            throw FinanceException.param("消息标题长度不能超过200个字符");
        }
        if (content.length() > 1000) {
            throw FinanceException.param("消息正文长度不能超过1000个字符");
        }

        TUserVo user = scopeSupport.requireLoginUser();
        Long targetUserId = vo.getUserId() != null ? vo.getUserId() : user.getId();
        Long targetOrgId = vo.getOrgId() != null ? vo.getOrgId() : scopeSupport.loginOrgId(user);
        String msgType = StringUtils.isNotEmpty(vo.getMsgType()) ? vo.getMsgType() : MSG_TYPE_SYSTEM;
        String subType = StringUtils.isNotEmpty(vo.getSubType()) ? vo.getSubType() : SUB_TYPE_ANNOUNCE;
        String priority = StringUtils.isNotEmpty(vo.getPriority()) ? vo.getPriority() : PRIORITY_NORMAL;

        SysMessageInfo entity = new SysMessageInfo()
                .setUserId(targetUserId)
                .setOrgId(targetOrgId)
                .setMsgType(msgType)
                .setSubType(subType)
                .setPriority(priority)
                .setTitle(title)
                .setContent(content)
                .setBizId(vo.getBizId())
                .setBizType(StringUtils.isNotEmpty(vo.getBizType()) ? vo.getBizType() : BIZ_TYPE_SYSTEM)
                .setJumpUrl(vo.getJumpUrl())
                .setReadStatus(0)
                .setRemindDate(DateUtils.now().toLocalDate());

        baseMapper.insert(entity);
        SysMessageInfoVo result = new SysMessageInfoVo();
        BeanUtils.copyProperties(entity, result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteMessages(String ids) {
        ensureTableInitialized();
        if (StringUtils.isEmpty(ids)) {
            throw FinanceException.param("删除的消息ID不能为空");
        }
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .map(Long::valueOf)
                .collect(Collectors.toList());
        if (idList.isEmpty()) {
            return true;
        }
        List<Long> deletableIds = new ArrayList<>(idList.size());
        for (Long id : idList) {
            SysMessageInfo entity = baseMapper.selectById(id);
            if (entity != null) {
                assertMessageAccessible(entity);
                deletableIds.add(id);
            }
        }
        if (deletableIds.isEmpty()) {
            return true;
        }
        return removeBatchByIds(deletableIds);
    }

    private void assertMessageAccessible(SysMessageInfo entity) {
        if (entity == null || Integer.valueOf(1).equals(entity.getIsDelete())) {
            throw FinanceException.param("消息不存在或已被删除");
        }
        TUserVo user = scopeSupport.requireLoginUser();
        if (scopeSupport.isSuper(user)) {
            return;
        }
        Long currentUserId = user.getId();
        Long currentOrgId = scopeSupport.loginOrgId(user);
        if (entity.getUserId() != null && entity.getUserId() > 0L) {
            if (entity.getUserId().equals(currentUserId)) {
                return;
            }
            throw FinanceException.forbidden("无权操作他人的消息通知");
        }
        if (entity.getOrgId() == null || entity.getOrgId() == 0L || entity.getOrgId().equals(currentOrgId)) {
            return;
        }
        throw FinanceException.forbidden("无权操作其他机构的消息通知");
    }
}
