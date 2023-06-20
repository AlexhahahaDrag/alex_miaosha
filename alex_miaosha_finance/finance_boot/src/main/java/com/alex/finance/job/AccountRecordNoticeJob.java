package com.alex.finance.job;

import com.alex.finance.service.accountRecordInfo.AccountRecordInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccountRecordNoticeJob {

    private final AccountRecordInfoService accountRecordInfoService;

    // TODO: 2023/6/20 修改成xxl-job调用 
    @Scheduled(cron = "0 30 8/18 * * ?")
    public void accountRecordNotice() throws WxErrorException {
        log.info("=================开始调用快过期账号查询=================");
        accountRecordInfoService.queryRemindRecordInfo(null);
        log.info("=================结束调用快过期账号查询=================");
    }
}
