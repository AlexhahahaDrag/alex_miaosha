package com.alex.finance.job;

import com.alex.common.utils.date.DateUtils;
import com.alex.finance.service.shopFinanceAnalysis.ShopFinanceAnalysisService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

@Component
@Slf4j
@RequiredArgsConstructor
public class ShopFinanceNoticeJob {

    private static final String YYYYMMDD = "yyyy-MM-dd";

    private final ShopFinanceAnalysisService shopFinanceAnalysisService;

    @XxlJob("dayShopFinanceNoticeHandler")
    public void dayShopFinanceNotice() throws Exception {
        log.info("===============开始调用店铺财务当日统计信息查询===================");
        long startTime = System.nanoTime();
        shopFinanceAnalysisService.getCurShopFinanceInfo(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(YYYYMMDD)),
                DateUtils.addDay(LocalDateTime.now(), 1).format(DateTimeFormatter.ofPattern(YYYYMMDD)),
                "day");
        log.info("===============结束调用店铺财务当日统计信息查询===================耗时：{}", Duration.ofNanos(System.nanoTime() - startTime));
    }

    @XxlJob("monthShopFinanceNoticeHandler")
    public void monthShopFinanceNotice() throws Exception {
        log.info("===============开始调用店铺财务当月统计信息查询===================");
        long startTime = System.nanoTime();
        shopFinanceAnalysisService.getCurShopFinanceInfo(
                LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).format(DateTimeFormatter.ofPattern(YYYYMMDD)),
                LocalDateTime.now().with(TemporalAdjusters.firstDayOfNextMonth()).format(DateTimeFormatter.ofPattern(YYYYMMDD)),
                "month");
        log.info("===============结束调用店铺财务当月统计信息查询===================耗时：{}", Duration.ofNanos(System.nanoTime() - startTime));
    }
}
