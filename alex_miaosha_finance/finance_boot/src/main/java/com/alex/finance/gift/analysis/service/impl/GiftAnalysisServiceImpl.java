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
import com.alex.common.exception.FinanceException;
import com.alex.common.utils.date.DateUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.finance.gift.analysis.service.GiftAnalysisService;
import com.alex.finance.gift.event.service.GiftEventInfoService;
import com.alex.finance.gift.person.service.GiftPersonInfoService;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import com.alex.finance.gift.support.GiftRecordConstants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
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

    @Override
    public void exportBalanceMatrix(String relationType, String searchKey, HttpServletResponse response) {
        List<GiftPersonBalanceVo> matrixList = balanceMatrix(relationType, searchKey);
        GiftRecordSummaryVo summary = overview(null);

        try (Workbook workbook = new XSSFWorkbook()) {
            // 样式与字体
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorder(headerStyle);

            CellStyle textStyle = workbook.createCellStyle();
            textStyle.setAlignment(HorizontalAlignment.LEFT);
            textStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorder(textStyle);

            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.setAlignment(HorizontalAlignment.CENTER);
            centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorder(centerStyle);

            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.setDataFormat(workbook.createDataFormat().getFormat("¥#,##0.00"));
            moneyStyle.setAlignment(HorizontalAlignment.RIGHT);
            moneyStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorder(moneyStyle);

            CellStyle alertStyle = workbook.createCellStyle();
            Font alertFont = workbook.createFont();
            alertFont.setColor(IndexedColors.RED.getIndex());
            alertStyle.setFont(alertFont);
            alertStyle.setAlignment(HorizontalAlignment.LEFT);
            alertStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorder(alertStyle);

            // ================= Sheet 1: 亲友互赠平衡矩阵 =================
            Sheet matrixSheet = workbook.createSheet("亲友互赠平衡矩阵");
            Row mHeaderRow = matrixSheet.createRow(0);
            mHeaderRow.setHeightInPoints(24);
            String[] mHeaders = { "亲友姓名", "关系圈层", "累计收TA礼金", "累计随TA礼金", "人情净差额", "平衡状态", "往来笔数", "最近往来时间", "健康度预警" };
            for (int i = 0; i < mHeaders.length; i++) {
                Cell cell = mHeaderRow.createCell(i);
                cell.setCellValue(mHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            int mRowIdx = 1;
            for (GiftPersonBalanceVo item : matrixList) {
                Row row = matrixSheet.createRow(mRowIdx++);
                row.setHeightInPoints(20);

                // 0: 亲友姓名
                Cell c0 = row.createCell(0);
                c0.setCellValue(item.getPersonName() != null ? item.getPersonName() : "-");
                c0.setCellStyle(textStyle);

                // 1: 关系圈层
                Cell c1 = row.createCell(1);
                c1.setCellValue(item.getRelationTypeName() != null ? item.getRelationTypeName() : (item.getRelationType() != null ? item.getRelationType() : "未分类"));
                c1.setCellStyle(centerStyle);

                // 2: 累计收礼金额
                Cell c2 = row.createCell(2);
                c2.setCellValue(item.getTotalReceiveAmount() != null ? item.getTotalReceiveAmount().doubleValue() : 0.0);
                c2.setCellStyle(moneyStyle);

                // 3: 累计随礼金额
                Cell c3 = row.createCell(3);
                c3.setCellValue(item.getTotalGiveAmount() != null ? item.getTotalGiveAmount().doubleValue() : 0.0);
                c3.setCellStyle(moneyStyle);

                // 4: 净差额
                Cell c4 = row.createCell(4);
                c4.setCellValue(item.getNetBalance() != null ? item.getNetBalance().doubleValue() : 0.0);
                c4.setCellStyle(moneyStyle);

                // 5: 平衡状态
                Cell c5 = row.createCell(5);
                String stText = switch (item.getBalanceStatus() != null ? item.getBalanceStatus() : "") {
                    case "SURPLUS" -> "礼金盈余(待回)";
                    case "DEFICIT" -> "礼金借出(待收)";
                    case "BALANCED" -> "基本持平";
                    default -> "-";
                };
                c5.setCellValue(stText);
                c5.setCellStyle(centerStyle);

                // 6: 往来笔数
                Cell c6 = row.createCell(6);
                c6.setCellValue(item.getRecordCount() != null ? item.getRecordCount() : 0);
                c6.setCellStyle(centerStyle);

                // 7: 最近往来时间
                Cell c7 = row.createCell(7);
                String timeStr = item.getLatestRecordTime() != null ? item.getLatestRecordTime().format(DateUtils.FORMATTER_YYYY_MM_DD_HH_MM_SS) : "-";
                c7.setCellValue(timeStr);
                c7.setCellStyle(centerStyle);

                // 8: 健康度预警
                Cell c8 = row.createCell(8);
                if (Boolean.TRUE.equals(item.getIsImbalancedAlert())) {
                    c8.setCellValue("⚠️ " + (item.getAlertReason() != null ? item.getAlertReason() : "大额未回礼失衡预警"));
                    c8.setCellStyle(alertStyle);
                } else {
                    c8.setCellValue("正常");
                    c8.setCellStyle(centerStyle);
                }
            }

            for (int i = 0; i < mHeaders.length; i++) {
                matrixSheet.setColumnWidth(i, 20 * 256);
            }

            // ================= Sheet 2: 人情收支汇总总览 =================
            Sheet summarySheet = workbook.createSheet("人情收支汇总总览");
            Row sHeaderRow = summarySheet.createRow(0);
            sHeaderRow.setHeightInPoints(24);
            String[] sHeaders = { "统计维度", "数值 / 金额", "备注口径" };
            for (int i = 0; i < sHeaders.length; i++) {
                Cell cell = sHeaderRow.createCell(i);
                cell.setCellValue(sHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            int sRowIdx = 1;
            Object[][] summaryRows = new Object[][] {
                    { "收礼总额", summary.getReceiveAmount() != null ? summary.getReceiveAmount().doubleValue() : 0.0, "收到的所有礼金总额" },
                    { "收礼总笔数", summary.getReceiveCount() != null ? summary.getReceiveCount() : 0, "收礼人情记录总数" },
                    { "随礼总额", summary.getGiveAmount() != null ? summary.getGiveAmount().doubleValue() : 0.0, "随礼支出总额" },
                    { "随礼总笔数", summary.getGiveCount() != null ? summary.getGiveCount() : 0, "随礼记录总数" },
                    { "回礼支出总额", summary.getReturnAmount() != null ? summary.getReturnAmount().doubleValue() : 0.0, "主动还礼/回礼支出总额" },
                    { "回礼总笔数", summary.getReturnCount() != null ? summary.getReturnCount() : 0, "还礼记录总数" },
                    { "全盘人情净值", summary.getNetAmount() != null ? summary.getNetAmount().doubleValue() : 0.0, "收礼 - (随礼 + 回礼)" }
            };

            for (Object[] rData : summaryRows) {
                Row row = summarySheet.createRow(sRowIdx++);
                row.setHeightInPoints(20);

                Cell c0 = row.createCell(0);
                c0.setCellValue(String.valueOf(rData[0]));
                c0.setCellStyle(textStyle);

                Cell c1 = row.createCell(1);
                if (rData[1] instanceof Double dVal) {
                    c1.setCellValue(dVal);
                    c1.setCellStyle(moneyStyle);
                } else if (rData[1] instanceof Number nVal) {
                    c1.setCellValue(nVal.doubleValue());
                    c1.setCellStyle(centerStyle);
                } else {
                    c1.setCellValue(String.valueOf(rData[1]));
                    c1.setCellStyle(centerStyle);
                }

                Cell c2 = row.createCell(2);
                c2.setCellValue(String.valueOf(rData[2]));
                c2.setCellStyle(textStyle);
            }

            summarySheet.setColumnWidth(0, 22 * 256);
            summarySheet.setColumnWidth(1, 20 * 256);
            summarySheet.setColumnWidth(2, 35 * 256);

            // 写出响应
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = "人情往来互赠平衡对账单_" + System.currentTimeMillis() + ".xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName + "; filename*=UTF-8''" + encodedFileName);

            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (IOException e) {
            log.error("导出亲友互赠平衡矩阵失败: {}", e.getMessage(), e);
            throw new FinanceException("500", "导出平衡矩阵失败: " + e.getMessage());
        }
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}

