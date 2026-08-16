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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.alex.api.finance.gift.event.vo.GiftRecordRecommendAmountVo;
import com.alex.finance.gift.event.entity.GiftEventInfo;
import com.alex.finance.gift.record.entity.GiftRecordInfo;
import com.alex.finance.gift.record.mapper.GiftRecordInfoMapper;
import com.alex.finance.gift.eventoption.entity.GiftEventTypeUserConfig;
import com.alex.finance.gift.eventoption.mapper.GiftEventTypeUserConfigMapper;
import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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
    private final GiftEventTypeUserConfigMapper giftEventTypeUserConfigMapper;

    @PostConstruct
    public void initTable() {
        try {
            giftEventTypeUserConfigMapper.createTableIfNotExists();
        } catch (Exception e) {
            log.warn("初始化 gift_event_type_user_config_t 提示: {}", e.getMessage());
        }
        try {
            giftEventTypeUserConfigMapper.addMissingAuditColumns();
        } catch (Exception ignored) {
        }
    }

    @Override
    public GiftEventTypeOptionsVo listEventTypeOptions() {
        Long orgId = resolveOrgId();
        backfillFromEventHistory(orgId);
        return toEventTypeOptionsVo(getBaseMapper().listEventTypeOptionRows(orgId), orgId);
    }

    private GiftEventTypeOptionsVo toEventTypeOptionsVo(List<GiftEventTypeOptionRowVo> rows, Long orgId) {
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
        List<GiftEventTypeItemVo> finalPresets = giftEventTypePresetSupport.ensurePresets(presets);

        // 1. 统一从与事件列表同源的 listEntities 查询中动态统计事件分类频次（自动继承数据权限）
        List<GiftEventInfo> events = giftEventInfoMapper.listEntities(null);
        Map<String, Long> countMap = (events == null ? List.<GiftEventInfo>of() : events).stream()
                .filter(e -> StringUtils.hasText(e.getEventType()))
                .collect(Collectors.groupingBy(GiftEventInfo::getEventType, Collectors.counting()));

        // 2. 查询当前机构的个性化状态与金额覆写配置
        List<GiftEventTypeUserConfig> configs = List.of();
        try {
            configs = giftEventTypeUserConfigMapper.selectList(new LambdaQueryWrapper<GiftEventTypeUserConfig>()
                    .eq(orgId != null, GiftEventTypeUserConfig::getOrgId, orgId)
                    .isNull(orgId == null, GiftEventTypeUserConfig::getOrgId)
                    .eq(GiftEventTypeUserConfig::getIsDelete, 0));
        } catch (Exception ignored) {
        }
        Map<Long, GiftEventTypeUserConfig> configMap = configs == null ? Collections.emptyMap()
                : configs.stream().collect(Collectors.toMap(GiftEventTypeUserConfig::getOptionId, c -> c, (c1, c2) -> c1));

        List<GiftEventTypeItemVo> enrichedPresets = new ArrayList<>();
        for (GiftEventTypeItemVo p : finalPresets) {
            long c1 = countMap.getOrDefault(p.getEventCode(), 0L);
            long c2 = countMap.getOrDefault(p.getName(), 0L);
            int total = (int) (c1 + c2);

            GiftEventTypeUserConfig cfg = configMap.get(p.getId());
            Integer status = (cfg != null && cfg.getStatus() != null) ? cfg.getStatus() : p.getStatus();
            BigDecimal amount = (cfg != null && cfg.getCustomAmount() != null) ? cfg.getCustomAmount() : p.getDefaultAmount();

            enrichedPresets.add(new GiftEventTypeItemVo()
                    .setId(p.getId())
                    .setName(p.getName())
                    .setEventCode(p.getEventCode())
                    .setCategory(p.getCategory())
                    .setIcon(p.getIcon())
                    .setStatus(status)
                    .setUseCount(total)
                    .setDefaultAmount(amount)
                    .setSortOrder(p.getSortOrder()));
        }

        List<GiftEventTypeItemVo> enrichedCustoms = new ArrayList<>();
        for (GiftEventTypeItemVo c : customs) {
            int total = countMap.getOrDefault(c.getName(), 0L).intValue();
            enrichedCustoms.add(new GiftEventTypeItemVo()
                    .setId(c.getId())
                    .setName(c.getName())
                    .setEventCode(c.getEventCode())
                    .setCategory(c.getCategory())
                    .setIcon(c.getIcon())
                    .setStatus(c.getStatus())
                    .setUseCount(total)
                    .setDefaultAmount(c.getDefaultAmount())
                    .setSortOrder(c.getSortOrder()));
        }

        return new GiftEventTypeOptionsVo()
                .setPresets(enrichedPresets)
                .setCustoms(enrichedCustoms);
    }

    @Override
    public boolean updateOption(GiftEventTypeOption option) {
        if (option == null || option.getId() == null) {
            throw GiftExceptions.param("选项ID不能为空");
        }
        GiftEventTypeOption existing = getById(option.getId());
        TUserVo loginUser = giftDataScopeSupport.requireLoginUser();
        Long orgId = giftDataScopeSupport.loginOrgId(loginUser);
        Long userId = loginUser.getId();

        // 1. 如果是系统预设分类 (SYSTEM)，保存/更新到租户个性化配置表 gift_event_type_user_config_t
        boolean isSystem = existing == null
                || GiftEventTypeOptionConstants.OPTION_TYPE_SYSTEM.equals(existing.getOptionType())
                || Long.valueOf(0L).equals(existing.getUserId());

        if (isSystem) {
            GiftEventTypeUserConfig config = giftEventTypeUserConfigMapper.selectOne(new LambdaQueryWrapper<GiftEventTypeUserConfig>()
                    .eq(GiftEventTypeUserConfig::getOptionId, option.getId())
                    .eq(orgId != null, GiftEventTypeUserConfig::getOrgId, orgId)
                    .isNull(orgId == null, GiftEventTypeUserConfig::getOrgId)
                    .eq(GiftEventTypeUserConfig::getIsDelete, 0)
                    .last("LIMIT 1"));
            if (config != null) {
                if (option.getStatus() != null) {
                    config.setStatus(option.getStatus());
                }
                if (option.getDefaultAmount() != null) {
                    config.setCustomAmount(option.getDefaultAmount());
                }
                giftEventTypeUserConfigMapper.updateById(config);
            } else {
                config = new GiftEventTypeUserConfig()
                        .setOptionId(option.getId())
                        .setOrgId(orgId)
                        .setUserId(userId)
                        .setStatus(option.getStatus() == null ? (existing != null ? existing.getStatus() : 1) : option.getStatus())
                        .setCustomAmount(option.getDefaultAmount() == null ? (existing != null ? existing.getDefaultAmount() : null) : option.getDefaultAmount());
                giftEventTypeUserConfigMapper.insert(config);
            }
            return true;
        }

        // 2. 如果是自定义分类 (CUSTOM)，更新机构自身的选项
        if (orgId != null && !orgId.equals(existing.getOrgId())) {
            throw GiftExceptions.forbidden("无权修改其他机构的分类");
        }
        return updateById(option);
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
            try {
                GiftEventTypeUserConfig config = giftEventTypeUserConfigMapper.selectOne(new LambdaQueryWrapper<GiftEventTypeUserConfig>()
                        .eq(GiftEventTypeUserConfig::getOptionId, optionId)
                        .eq(orgId != null, GiftEventTypeUserConfig::getOrgId, orgId)
                        .isNull(orgId == null, GiftEventTypeUserConfig::getOrgId)
                        .eq(GiftEventTypeUserConfig::getIsDelete, 0)
                        .last("LIMIT 1"));
                if (config != null && config.getCustomAmount() != null) {
                    defaultAmount = config.getCustomAmount();
                }
            } catch (Exception ignored) {
            }
            if (defaultAmount.compareTo(BigDecimal.ZERO) <= 0) {
                GiftEventTypeOption option = getById(optionId);
                if (option != null && option.getDefaultAmount() != null) {
                    defaultAmount = option.getDefaultAmount();
                }
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
