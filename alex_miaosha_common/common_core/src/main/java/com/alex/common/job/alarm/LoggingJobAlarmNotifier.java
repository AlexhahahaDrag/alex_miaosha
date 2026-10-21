package com.alex.common.job.alarm;

import lombok.extern.slf4j.Slf4j;

/**
 * 默认的日志告警通知器
 */
@Slf4j
public class LoggingJobAlarmNotifier implements JobAlarmNotifier {

    @Override
    public void notifyAlarm(JobAlarmMessage message) {
        if (message == null) {
            return;
        }
        log.error("==================== [XXL-JOB-ALARM 任务告警] ====================\n" +
                        "Handler  : {}\n" +
                        "TraceId  : {}\n" +
                        "Param    : {}\n" +
                        "Cost     : {} ms\n" +
                        "Time     : {}\n" +
                        "Error    : {}\n" +
                        "Stack    :\n{}" +
                        "==================================================================",
                message.getJobHandler(),
                message.getTraceId(),
                message.getJobParam(),
                message.getCostMillis(),
                message.getOccurTime(),
                message.getErrorMessage(),
                message.getErrorStack() != null ? message.getErrorStack() : "N/A\n"
        );
    }
}
