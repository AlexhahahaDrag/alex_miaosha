package com.alex.finance.job;

import com.alex.finance.message.service.SysMessageInfoService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 系统消息与业务到期提醒 XXL-Job 定时调度任务
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SysMessageRemindJob {

    private final SysMessageInfoService sysMessageInfoService;

    /**
     * 每日定时巡检：礼尚往来事件（提前1天/当天）、消费券到期、猫超卡与预付卡到期提醒
     */
    @XxlJob("sysMessageRemindHandler")
    public void sysMessageRemind() {
        log.info("=============== 开始执行系统消息与到期提醒 XXL-Job 巡检任务 ===============");
        long startTime = System.nanoTime();
        int insertedCount = sysMessageInfoService.executeRemindJob();
        Duration cost = Duration.ofNanos(System.nanoTime() - startTime);
        String resultMsg = String.format("到期提醒巡检完成，本次新生成提醒消息 %d 条，耗时：%s", insertedCount, cost);
        log.info("=============== {} ===============", resultMsg);
        XxlJobHelper.log(resultMsg);
    }
}
