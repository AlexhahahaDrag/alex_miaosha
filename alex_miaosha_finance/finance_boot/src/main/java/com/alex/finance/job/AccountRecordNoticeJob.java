package com.alex.finance.job;

import com.alex.finance.service.accountRecordInfo.AccountRecordInfoService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccountRecordNoticeJob {

    private final AccountRecordInfoService accountRecordInfoService;

    @XxlJob("accountRecordNoticeHandler")
    public void accountRecordNotice() throws Exception {
        log.info("===============开始调用快过期账号查询===================");
        long startTime = System.nanoTime();
        accountRecordInfoService.queryRemindRecordInfo(null);
        log.info("===============结束调用快过期账号查询===================耗时：{}", Duration.ofNanos(System.nanoTime() - startTime));
    }
}
