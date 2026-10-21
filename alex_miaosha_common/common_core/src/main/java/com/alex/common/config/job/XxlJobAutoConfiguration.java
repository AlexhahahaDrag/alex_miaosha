package com.alex.common.config.job;

import com.alex.common.job.alarm.JobAlarmNotifier;
import com.alex.common.job.alarm.LoggingJobAlarmNotifier;
import com.alex.common.job.trace.XxlJobTraceAspect;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * XXL-Job 执行器自动装配配置
 */
@Configuration
@EnableConfigurationProperties(XxlJobExecutorProperties.class)
@ConditionalOnClass(XxlJobSpringExecutor.class)
@ConditionalOnProperty(prefix = "xxl.job", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class XxlJobAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(JobAlarmNotifier.class)
    public JobAlarmNotifier jobAlarmNotifier() {
        return new LoggingJobAlarmNotifier();
    }

    @Bean
    @ConditionalOnMissingBean(XxlJobTraceAspect.class)
    public XxlJobTraceAspect xxlJobTraceAspect(JobAlarmNotifier jobAlarmNotifier) {
        return new XxlJobTraceAspect(jobAlarmNotifier);
    }

    @Bean(initMethod = "start", destroyMethod = "destroy")
    @ConditionalOnMissingBean(XxlJobSpringExecutor.class)
    public XxlJobSpringExecutor xxlJobSpringExecutor(XxlJobExecutorProperties properties) {
        log.info(">>>>>>>>>>> XXL-JOB 任务调度执行器正在初始化 >>>>>>>>>>>");
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();

        // 1. 调度中心地址
        String adminAddresses = properties.resolveAdminAddresses();
        if (StringUtils.isBlank(adminAddresses)) {
            log.warn("XXL-Job adminAddresses 未配置或为空，执行器可能无法向调度中心注册心跳！");
        } else {
            executor.setAdminAddresses(adminAddresses);
        }

        XxlJobExecutorProperties.Executor execProps = properties.getExecutor();
        if (execProps == null) {
            execProps = new XxlJobExecutorProperties.Executor();
        }

        // 2. AppName
        executor.setAppname(execProps.getAppName());

        // 3. 智能探测执行器注册 IP
        String ip = execProps.getIp();
        if (StringUtils.isBlank(ip)) {
            ip = resolveLocalIp();
            log.info("XXL-Job 执行器未显式配置 IP，智能探测绑定本地内网 IP: {}", ip);
        }
        executor.setIp(ip);

        int port = execProps.getPort() > 0 ? execProps.getPort() : 9999;
        executor.setPort(port);
        executor.setAccessToken(execProps.getAccessToken());

        // 5. 日志路径与保留天数
        executor.setLogPath(execProps.getLogPath());
        int retentionDays = execProps.getLogRetentionDays() > 0 ? execProps.getLogRetentionDays() : 30;
        executor.setLogRetentionDays(retentionDays);

        log.info(">>>>>>>>>>> XXL-JOB 执行器装配完成: appName=[{}], ip=[{}], port=[{}], logRetentionDays=[{}]",
                execProps.getAppName(), ip, port, retentionDays);
        return executor;
    }

    /**
     * 智能解析本机内网非回环有效 IPv4 地址
     */
    public static String resolveLocalIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || ni.isVirtual() || !ni.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress() && addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
            // 回退到 getLocalHost
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Throwable t) {
            log.warn("探测本地内网 IP 发生异常，回退 127.0.0.1: {}", t.getMessage());
            return "127.0.0.1";
        }
    }
}
