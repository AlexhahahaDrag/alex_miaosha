package com.alex.finance.config;

import com.alex.common.config.job.XxlJobAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 财务微服务 XXL-JOB 执行器配置。
 * <p>已平滑迁移并接入 {@code common_core} 通用任务调度底座 {@link XxlJobAutoConfiguration}，
 * 自动获得内网非回环 IP 智能探测、MDC 链路染色切面、分片辅助工具与统一告警通知机制。
 */
@Configuration
@Slf4j
@Import(XxlJobAutoConfiguration.class)
public class XxlJobConfig {
}
