package com.alex.finance.gift.eventoption.service.impl;

import com.alex.api.finance.gift.event.vo.GiftEventTypeItemVo;
import com.alex.api.finance.gift.event.vo.GiftEventTypeOptionRowVo;
import com.alex.api.finance.gift.event.vo.GiftEventTypeOptionsVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.event.mapper.GiftEventInfoMapper;
import com.alex.finance.gift.eventoption.entity.GiftEventTypeOption;
import com.alex.finance.gift.eventoption.mapper.GiftEventTypeOptionMapper;
import com.alex.finance.gift.eventoption.service.GiftEventTypeOptionService;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import com.alex.finance.gift.support.GiftEventTypeOptionConstants;
import com.alex.finance.gift.support.GiftEventTypePresetSupport;
import com.alex.finance.gift.support.GiftExceptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.alex.api.finance.gift.event.vo.GiftRecordRecommendAmountVo;
import com.alex.finance.gift.event.entity.GiftEventInfo;
import com.alex.finance.gift.record.entity.GiftRecordInfo;
import com.alex.finance.gift.record.mapper.GiftRecordInfoMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GiftEventTypeOptionServiceImp
        extends ServiceImpl<GiftEventTypeOptionMapper, GiftEventTypeOption>
        implements GiftEventTypeOptionService {

    private static final int MAX_LABEL_LENGTH = 20;

    private final GiftDataScopeSupport giftDataScopeSupport;
    private final GiftEventInfoMapper giftEventInfoMapper;
    private final GiftEventTypePresetSupport giftEventTypePresetSupport;
    private final GiftRecordInfoMapper giftRecordInfoMapper;

    @Override
    public GiftEventTypeOptionsVo listEventTypeOptions() {
        Long orgId = resolveOrgId();
        backfillFromEventHistory(orgId);
        return toEventTypeOptionsVo(getBaseMapper().listEventTypeOptionRows(orgId));
    }

    private GiftEventTypeOptionsVo toEventTypeOptionsVo(List<GiftEventTypeOptionRowVo> rows) {
        List<GiftEventTypeItemVo> presets = new ArrayList<>();
        List<GiftEventTypeItemVo> customs = new ArrayList<>();
        if (rows != null) {
            for (GiftEventTypeOptionRowVo row : rows) {
                GiftEventTypeItemVo item = new GiftEventTypeItemVo()
                        .setId(row.getId())
                        .setName(row.getEventLabel())
                        .setEventCode(row.getEventCode())
                        .setCategory(row.getCategory())
                        .setIcon(row.getIcon())
                        .setStatus(row.getStatus())
                        .setUseCount(row.getUseCount())
                        .setDefaultAmount(row.getDefaultAmount())
                        .setSortOrder(row.getSortOrder());
                if (GiftEventTypeOptionConstants.OPTION_TYPE_SYSTEM.equals(row.getOptionType())) {
                    presets.add(item);
                    continue;
                }
                if (GiftEventTypeOptionConstants.OPTION_TYPE_CUSTOM.equals(row.getOptionType())) {
                    customs.add(item);
                }
            }
        }
        return new GiftEventTypeOptionsVo()
                .setPresets(giftEventTypePresetSupport.ensurePresets(presets))
                .setCustoms(customs);
    }

    @Override
    public String resolveEventType(Long eventTypeOptionId, Long orgId) {
        if (eventTypeOptionId == null) {
            return null;
        }
        GiftEventTypeOption option = getById(eventTypeOptionId);
        if (option == null || option.getIsDelete() != null && option.getIsDelete() == 1) {
            throw GiftExceptions.param("事由类型选项不存在");
        }
        if (GiftEventTypeOptionConstants.OPTION_TYPE_SYSTEM.equals(option.getOptionType())) {
            return option.getEventCode();
        }
        if (GiftEventTypeOptionConstants.OPTION_TYPE_CUSTOM.equals(option.getOptionType())) {
            if (orgId == null || !orgId.equals(option.getOrgId())) {
                throw GiftExceptions.forbidden("无权使用该自定义事由类型");
            }
            return option.getEventLabel();
        }
        throw GiftExceptions.param("事由类型选项不合法");
    }

    @Override
    public Long findEventTypeOptionId(Long orgId, String eventType) {
        if (!StringUtils.hasText(eventType) || orgId == null) {
            return null;
        }
        return getBaseMapper().findOptionIdByEventType(orgId, eventType.trim());
    }

    @Override
    public void rememberCustomEventType(Long orgId, Long userId, String eventType) {
        if (!StringUtils.hasText(eventType) || giftEventTypePresetSupport.isPresetCode(eventType)) {
            return;
        }
        String label = eventType.trim();
        if (label.length() > MAX_LABEL_LENGTH) {
            throw GiftExceptions.param("自定义事由类型最多20个字符");
        }
        if (giftEventTypePresetSupport.isPresetLabel(label)) {
            throw GiftExceptions.param("请从常用类型中选择「" + label + "」");
        }
        upsertLabel(orgId, userId, label);
    }

    private Long resolveOrgId() {
        TUserVo loginUser = giftDataScopeSupport.requireLoginUser();
        return giftDataScopeSupport.loginOrgId(loginUser);
    }

    private void backfillFromEventHistory(Long orgId) {
        if (orgId == null) {
            return;
        }
        List<String> labels = giftEventInfoMapper.listDistinctCustomEventTypes(orgId);
        if (labels == null || labels.isEmpty()) {
            return;
        }
        TUserVo loginUser = giftDataScopeSupport.requireLoginUser();
        for (String label : labels) {
            if (StringUtils.hasText(label)) {
                upsertLabelIfAbsent(orgId, loginUser.getId(), label.trim());
            }
        }
    }

    private void upsertLabel(Long orgId, Long userId, String label) {
        GiftEventTypeOption existing = findActiveCustomOption(orgId, label);
        LocalDateTime now = LocalDateTime.now();
        if (existing != null) {
            existing.setLastUsedTime(now);
            updateById(existing);
            return;
        }
        GiftEventTypeOption option = new GiftEventTypeOption();
        option.setOrgId(orgId);
        option.setUserId(userId);
        option.setOptionType(GiftEventTypeOptionConstants.OPTION_TYPE_CUSTOM);
        option.setEventLabel(label);
        option.setSortOrder(0);
        option.setLastUsedTime(now);
        save(option);
    }

    private void upsertLabelIfAbsent(Long orgId, Long userId, String label) {
        if (findActiveCustomOption(orgId, label) != null) {
            return;
        }
        GiftEventTypeOption option = new GiftEventTypeOption();
        option.setOrgId(orgId);
        option.setUserId(userId);
        option.setOptionType(GiftEventTypeOptionConstants.OPTION_TYPE_CUSTOM);
        option.setEventLabel(label);
        option.setSortOrder(0);
        option.setLastUsedTime(LocalDateTime.now());
        save(option);
    }

    private GiftEventTypeOption findActiveCustomOption(Long orgId, String label) {
        return getOne(new LambdaQueryWrapper<GiftEventTypeOption>()
                .eq(GiftEventTypeOption::getOrgId, orgId)
                .eq(GiftEventTypeOption::getOptionType, GiftEventTypeOptionConstants.OPTION_TYPE_CUSTOM)
                .eq(GiftEventTypeOption::getEventLabel, label)
                .last("LIMIT 1"));
    }

    @Override
    public GiftRecordRecommendAmountVo getRecommendAmount(Long personId, String eventType, String direction) {
        Long orgId = resolveOrgId();
        
        BigDecimal defaultAmount = BigDecimal.ZERO;
        Long optionId = findEventTypeOptionId(orgId, eventType);
        if (optionId != null) {
            GiftEventTypeOption option = getById(optionId);
            if (option != null && option.getDefaultAmount() != null) {
                defaultAmount = option.getDefaultAmount();
            }
        }
        if (defaultAmount.compareTo(BigDecimal.ZERO) <= 0) {
            defaultAmount = new BigDecimal("500.00");
        }

        List<Long> eventIds = giftEventInfoMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GiftEventInfo>()
                .eq("event_type", eventType)
                .eq("is_delete", 0)
        ).stream().map(GiftEventInfo::getId).toList();

        BigDecimal averageAmount = BigDecimal.ZERO;
        BigDecimal latestAmount = BigDecimal.ZERO;

        if (personId != null && !eventIds.isEmpty()) {
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GiftRecordInfo> query = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            query.eq("is_delete", 0)
                 .in("event_id", eventIds)
                 .and(wrapper -> wrapper.eq("giver_person_id", personId).or().eq("receiver_person_id", personId));
            if (StringUtils.hasText(direction)) {
                query.eq("direction", direction);
            }
            query.orderByDesc("pay_time");

            List<GiftRecordInfo> records = giftRecordInfoMapper.selectList(query);
            if (records != null && !records.isEmpty()) {
                latestAmount = records.get(0).getAmount();
                BigDecimal total = BigDecimal.ZERO;
                int count = 0;
                for (GiftRecordInfo r : records) {
                    if (r.getAmount() != null) {
                        total = total.add(r.getAmount());
                        count++;
                    }
                }
                if (count > 0) {
                    averageAmount = total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
                }
            }
        }

        BigDecimal baseAmount = averageAmount.compareTo(BigDecimal.ZERO) > 0 ? averageAmount : defaultAmount;
        
        List<BigDecimal> recommendations = List.of(
            roundAmount(baseAmount.multiply(new BigDecimal("0.80"))),
            roundAmount(baseAmount.multiply(new BigDecimal("1.00"))),
            roundAmount(baseAmount.multiply(new BigDecimal("1.50"))),
            roundAmount(baseAmount.multiply(new BigDecimal("2.00")))
        );

        return new GiftRecordRecommendAmountVo()
                .setAverageAmount(averageAmount)
                .setLatestAmount(latestAmount)
                .setDefaultAmount(defaultAmount)
                .setRecommendations(recommendations);
    }

    private BigDecimal roundAmount(BigDecimal val) {
        if (val == null) {
            return BigDecimal.ZERO;
        }
        double valDouble = val.doubleValue();
        if (valDouble > 100) {
            return BigDecimal.valueOf(Math.round(valDouble / 50.0) * 50);
        } else {
            return BigDecimal.valueOf(Math.round(valDouble / 10.0) * 10);
        }
    }
}
