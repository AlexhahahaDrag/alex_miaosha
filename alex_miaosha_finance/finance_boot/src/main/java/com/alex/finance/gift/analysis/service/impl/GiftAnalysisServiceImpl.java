package com.alex.finance.gift.analysis.service.impl;

import com.alex.api.finance.gift.event.vo.GiftEventBusinessVo;
import com.alex.api.finance.gift.event.vo.GiftEventSummaryVo;
import com.alex.api.finance.gift.person.query.GiftPersonQuery;
import com.alex.api.finance.gift.person.vo.GiftPersonBusinessVo;
import com.alex.api.finance.gift.person.vo.GiftPersonProfileVo;
import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.api.finance.gift.summary.vo.GiftAmountTrendVo;
import com.alex.api.finance.gift.summary.vo.GiftPersonBalanceVo;
import com.alex.api.finance.gift.summary.vo.GiftRankingItemVo;
import com.alex.api.finance.gift.summary.vo.GiftRelationDistributionVo;
import com.alex.api.finance.gift.summary.vo.GiftReturnRecommendationReq;
import com.alex.api.finance.gift.summary.vo.GiftReturnRecommendationVo;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import com.alex.common.utils.date.DateUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.finance.gift.analysis.service.GiftAnalysisService;
import com.alex.finance.gift.event.service.GiftEventInfoService;
import com.alex.finance.gift.person.service.GiftPersonInfoService;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import com.alex.finance.gift.support.GiftRecordConstants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GiftAnalysisServiceImpl implements GiftAnalysisService {

    private final GiftRecordInfoService giftRecordInfoService;
    private final GiftEventInfoService giftEventInfoService;
    private final GiftPersonInfoService giftPersonInfoService;

    private static final List<BigDecimal> AUSPICIOUS_TIERS = List.of(
            new BigDecimal("200"),
            new BigDecimal("500"),
            new BigDecimal("600"),
            new BigDecimal("666"),
            new BigDecimal("800"),
            new BigDecimal("888"),
            new BigDecimal("1000"),
            new BigDecimal("1200"),
            new BigDecimal("1288"),
            new BigDecimal("1688"),
            new BigDecimal("1888"),
            new BigDecimal("2000"),
            new BigDecimal("2888"),
            new BigDecimal("5000")
    );

    /**
     * 统计总览：direction 白名单过滤后走 getSummary 的 SQL 聚合。
     */
    @Override
    public GiftRecordSummaryVo overview(String direction) {
        GiftRecordQuery query = new GiftRecordQuery();
        query.setDirection(normalizeDirection(direction));
        return giftRecordInfoService.getSummary(query);
    }

    /**
     * 收支趋势：下沉为 SQL DATE_FORMAT 分组聚合。
     */
    @Override
    public List<GiftAmountTrendVo> trend(String period, String direction) {
        return giftRecordInfoService.getTrend(period, normalizeDirection(direction));
    }

    /**
     * 关系分布：下沉为 SQL GROUP BY 聚合。
     */
    @Override
    public List<GiftRelationDistributionVo> relationDistribution() {
        return giftPersonInfoService.getRelationDistribution();
    }

    @Override
    public List<GiftRankingItemVo> eventRanking() {
        GiftEventSummaryVo summary = giftEventInfoService.getSummary();
        Page<GiftEventBusinessVo> page = giftEventInfoService.getBusinessPage(1L, 10L, null);
        List<GiftEventBusinessVo> records = (page != null && page.getRecords() != null)
                ? page.getRecords()
                : Collections.emptyList();
        List<GiftRankingItemVo> rows = records.stream()
                .map(event -> new GiftRankingItemVo()
                        .setName(event.getEventName())
                        .setAmount(defaultAmount(event.getTotalAmount()))
                        .setCount(defaultLong(event.getParticipantCount())))
                .sorted(Comparator.comparing(GiftRankingItemVo::getAmount).reversed())
                .toList();
        if (rows.isEmpty()) {
            rows = List.of(new GiftRankingItemVo()
                    .setName("全部事由")
                    .setAmount(defaultAmount(summary != null ? summary.getTotalAmount() : null))
                    .setCount(defaultLong(summary != null ? summary.getMonthPendingCount() : null)));
        }
        return rows;
    }

    @Override
    public List<GiftRankingItemVo> personRanking() {
        Page<GiftPersonBusinessVo> page = giftPersonInfoService.getBusinessPage(1L, 10L, null);
        List<GiftPersonBusinessVo> records = (page != null && page.getRecords() != null)
                ? page.getRecords()
                : Collections.emptyList();
        return records.stream()
                .map(person -> new GiftRankingItemVo()
                        .setName(person.getPersonName())
                        .setAmount(defaultAmount(person.getTotalGiveAmount())
                                .add(defaultAmount(person.getTotalReceiveAmount())))
                        .setCount(person.getLatestRecordTime() == null ? 0L : 1L))
                .sorted(Comparator.comparing(GiftRankingItemVo::getAmount).reversed())
                .toList();
    }

    @Override
    public List<GiftPersonBalanceVo> balanceMatrix(String relationType, String searchKey) {
        GiftPersonQuery query = new GiftPersonQuery();
        if (StringUtils.isNotEmpty(relationType) && !"all".equalsIgnoreCase(relationType)) {
            query.setRelationType(relationType);
        }
        if (StringUtils.isNotEmpty(searchKey)) {
            query.setKeyword(searchKey);
        }

        Page<GiftPersonBusinessVo> page = giftPersonInfoService.getBusinessPage(1L, 1000L, query);
        List<GiftPersonBusinessVo> records = (page != null && page.getRecords() != null)
                ? page.getRecords()
                : Collections.emptyList();

        LocalDateTime halfYearAgo = DateUtils.now().minusDays(180);
        BigDecimal balancedThreshold = new BigDecimal("200");
        BigDecimal alertThreshold = new BigDecimal("800");

        List<GiftPersonBalanceVo> list = records.stream().map(p -> {
            BigDecimal receive = defaultAmount(p.getTotalReceiveAmount());
            BigDecimal give = defaultAmount(p.getTotalGiveAmount());
            BigDecimal net = receive.subtract(give);

            String status;
            if (net.compareTo(balancedThreshold) > 0) {
                status = "SURPLUS";
            } else if (net.compareTo(balancedThreshold.negate()) < 0) {
                status = "DEFICIT";
            } else {
                status = "BALANCED";
            }

            boolean isAlert = false;
            String alertReason = null;
            if (net.compareTo(alertThreshold) >= 0) {
                if (p.getLatestRecordTime() == null || p.getLatestRecordTime().isBefore(halfYearAgo)) {
                    isAlert = true;
                    alertReason = String.format("该亲友累计礼金盈余达 ¥%s，且已超半年无后续往来，建议维护关系或适时体面回礼", net.toPlainString());
                }
            }

            return new GiftPersonBalanceVo()
                    .setPersonId(p.getId())
                    .setPersonName(p.getPersonName())
                    .setRelationType(p.getRelationType())
                    .setRelationTypeName(resolveRelationLabel(p.getRelationType()))
                    .setTotalReceiveAmount(receive)
                    .setTotalGiveAmount(give)
                    .setNetBalance(net)
                    .setBalanceStatus(status)
                    .setRecordCount(p.getLatestRecordTime() == null ? 0 : 1)
                    .setLatestRecordTime(p.getLatestRecordTime())
                    .setIsImbalancedAlert(isAlert)
                    .setAlertReason(alertReason);
        }).sorted(Comparator.comparing(GiftPersonBalanceVo::getNetBalance, Comparator.nullsLast(Comparator.reverseOrder()))).toList();

        return list;
    }

    @Override
    public GiftReturnRecommendationVo recommendReturnGift(GiftReturnRecommendationReq req) {
        if (req == null || req.getPersonId() == null) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "亲友ID不能为空");
        }

        GiftPersonProfileVo profile = giftPersonInfoService.getProfile(req.getPersonId());
        GiftPersonBusinessVo person = profile != null ? profile.getPerson() : null;
        if (person == null) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "亲友不存在");
        }

        BigDecimal historicalBase = BigDecimal.ZERO;
        if (profile.getRecords() != null && !profile.getRecords().isEmpty()) {
            historicalBase = profile.getRecords().stream()
                    .filter(r -> GiftRecordConstants.DIRECTION_RECEIVE.equalsIgnoreCase(r.getDirection()))
                    .map(r -> defaultAmount(r.getAmount()))
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
        }
        if (historicalBase.compareTo(BigDecimal.ZERO) <= 0) {
            historicalBase = defaultAmount(person.getTotalReceiveAmount());
        }
        if (historicalBase.compareTo(BigDecimal.ZERO) <= 0) {
            historicalBase = defaultAmount(person.getTotalGiveAmount());
        }
        if (historicalBase.compareTo(BigDecimal.ZERO) <= 0) {
            historicalBase = new BigDecimal("500");
        }

        String eventType = req.getEventType() != null ? req.getEventType().toUpperCase() : "GENERAL";
        BigDecimal eventMultiplier = switch (eventType) {
            case "WEDDING" -> new BigDecimal("1.2");
            case "FULL_MONTH", "BIRTHDAY" -> new BigDecimal("1.0");
            case "HOUSEWARMING" -> new BigDecimal("0.95");
            case "FUNERAL" -> new BigDecimal("0.85");
            default -> new BigDecimal("1.0");
        };

        String relationType = person.getRelationType() != null ? person.getRelationType().toLowerCase() : "";
        BigDecimal relationMultiplier = switch (relationType) {
            case "parents", "brother_sister", "close_relative" -> new BigDecimal("1.3");
            case "relatives", "family" -> new BigDecimal("1.1");
            case "friend", "best_friend" -> new BigDecimal("1.1");
            case "colleague", "classmate" -> new BigDecimal("1.0");
            default -> new BigDecimal("0.95");
        };

        BigDecimal targetAmount = historicalBase.multiply(eventMultiplier).multiply(relationMultiplier);

        BigDecimal recommended = matchAuspiciousTier(targetAmount);
        BigDecimal minDecent = findLowerTier(recommended);
        BigDecimal generous = findHigherTier(recommended);

        String relationLabel = resolveRelationLabel(person.getRelationType());

        String ruleSummary = String.format("根据与亲友【%s】的历史互赠记录（历史基准 ¥%s），结合【%s】事由（系数 %s）与【%s】关系（系数 %s），推导目标金额 ¥%s 并按民间吉利数字推荐 ¥%s 档位。",
                person.getPersonName(),
                historicalBase.toPlainString(),
                getEventTypeName(eventType),
                eventMultiplier.toPlainString(),
                relationLabel,
                relationMultiplier.toPlainString(),
                targetAmount.setScale(0, RoundingMode.HALF_UP).toPlainString(),
                recommended.toPlainString());

        String blessingWords = getBlessingWords(eventType);

        return new GiftReturnRecommendationVo()
                .setPersonName(person.getPersonName())
                .setRelationTypeName(relationLabel)
                .setHistoricalBaseAmount(historicalBase)
                .setMinDecentAmount(minDecent)
                .setRecommendedAmount(recommended)
                .setGenerousAmount(generous)
                .setAuspiciousTierList(AUSPICIOUS_TIERS)
                .setRuleSummary(ruleSummary)
                .setBlessingWords(blessingWords);
    }

    private String resolveRelationLabel(String relationType) {
        if (relationType == null) return "亲友";
        return switch (relationType.toLowerCase()) {
            case "parents" -> "父母长辈";
            case "brother_sister" -> "兄弟姐妹";
            case "close_relative" -> "近亲属";
            case "relatives", "family" -> "亲戚";
            case "friend", "best_friend" -> "朋友";
            case "colleague" -> "同事";
            case "classmate" -> "同学";
            default -> relationType;
        };
    }

    private BigDecimal matchAuspiciousTier(BigDecimal target) {
        for (BigDecimal tier : AUSPICIOUS_TIERS) {
            if (tier.compareTo(target) >= 0) {
                return tier;
            }
        }
        return AUSPICIOUS_TIERS.get(AUSPICIOUS_TIERS.size() - 1);
    }

    private BigDecimal findLowerTier(BigDecimal current) {
        int idx = AUSPICIOUS_TIERS.indexOf(current);
        if (idx > 0) {
            return AUSPICIOUS_TIERS.get(idx - 1);
        }
        return AUSPICIOUS_TIERS.get(0);
    }

    private BigDecimal findHigherTier(BigDecimal current) {
        int idx = AUSPICIOUS_TIERS.indexOf(current);
        if (idx >= 0 && idx < AUSPICIOUS_TIERS.size() - 1) {
            return AUSPICIOUS_TIERS.get(idx + 1);
        }
        return current.add(new BigDecimal("1000"));
    }

    private String getEventTypeName(String eventType) {
        return switch (eventType) {
            case "WEDDING" -> "结婚宴请";
            case "FULL_MONTH" -> "满月百天";
            case "BIRTHDAY" -> "长辈寿宴";
            case "HOUSEWARMING" -> "乔迁之喜";
            case "FUNERAL" -> "白事慰问";
            default -> "人情往来";
        };
    }

    private String getBlessingWords(String eventType) {
        return switch (eventType) {
            case "WEDDING" -> "百年好合，永结同心！新婚快乐，白头偕老！";
            case "FULL_MONTH" -> "祝小宝贝健康成长，聪明伶俐，平安喜乐！";
            case "BIRTHDAY" -> "福如东海，寿比南山！岁岁平安，福寿康宁！";
            case "HOUSEWARMING" -> "乔迁大吉，喜迁新居！紫气东来，吉星高照！";
            case "FUNERAL" -> "节哀顺变，逝者安息，生者坚强。";
            default -> "吉祥如意，顺心遂愿，福气满满！";
        };
    }

    /** 方向参数白名单：非法值按"全部"处理 */
    private String normalizeDirection(String direction) {
        return GiftRecordConstants.normalizeDirection(direction);
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private Long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}
