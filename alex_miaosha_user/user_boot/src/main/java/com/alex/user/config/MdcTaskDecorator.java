package com.alex.user.config;

import org.springframework.core.task.TaskDecorator;

/**
 * 线程上下文装饰器：透传 TraceId 与 MDC 诊断上下文至异步子线程，并在退出时执行彻底清理杜绝复用污染
 * 内部委托 common_core 标准统一实现 MdcTaskDecorator
 *
 * @author alex
 * @version 2.0.0
 */
public class MdcTaskDecorator implements TaskDecorator {

    private final com.alex.common.utils.trace.MdcTaskDecorator delegate = new com.alex.common.utils.trace.MdcTaskDecorator();

    @Override
    public Runnable decorate(Runnable runnable) {
        return delegate.decorate(runnable);
    }
}
