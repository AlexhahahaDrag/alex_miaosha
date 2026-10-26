package com.alex.finance.job;

import com.alex.finance.accountRecordInfo.service.AccountRecordInfoService;
import com.alex.finance.message.service.SysMessageInfoService;
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
    private final SysMessageInfoService sysMessageInfoService;

    @XxlJob("accountRecordNoticeHandler")
    public void accountRecordNotice() {
        log.info("===============开始调用快过期账号查询与到期站内信巡检===================");
        long startTime = System.nanoTime();
        sysMessageInfoService.executeRemindJob();
        accountRecordInfoService.queryRemindRecordInfo(null);
        log.info("===============结束调用快过期账号查询与到期站内信巡检===================耗时：{}", Duration.ofNanos(System.nanoTime() - startTime));
    }
}
