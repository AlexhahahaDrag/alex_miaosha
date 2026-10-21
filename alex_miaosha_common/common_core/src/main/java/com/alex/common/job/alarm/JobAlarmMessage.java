package com.alex.common.job.alarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * XXL-Job 任务执行异常告警消息模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobAlarmMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务处理器名称（@XxlJob 标注的值）
     */
    private String jobHandler;

    /**
     * 调度参数
     */
    private String jobParam;

    /**
     * 当前任务执行 TraceId
     */
    private String traceId;

    /**
     * 任务执行耗时（毫秒）
     */
    private long costMillis;

    /**
     * 异常摘要信息
     */
    private String errorMessage;

    /**
     * 异常堆栈前 N 行摘要
     */
    private String errorStack;

    /**
     * 发生时间
     */
    private LocalDateTime occurTime;
}
