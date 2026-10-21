package com.alex.common.job.alarm;

/**
 * XXL-Job 任务异常告警通知扩展接口
 * <p>业务微服务可按需实现该接口接入钉钉机器人、企业微信 Webhook、飞书或邮件告警系统。
 */
@FunctionalInterface
public interface JobAlarmNotifier {

    /**
     * 发送任务失败告警通知
     *
     * @param message 告警信息载体
     */
    void notifyAlarm(JobAlarmMessage message);
}
