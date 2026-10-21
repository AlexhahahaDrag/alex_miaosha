package com.alex.common.annotations.rate;

/**
 * 分布式限流与防刷维度枚举
 */
public enum LimitType {

    /**
     * 全局接口维度（微服务集群共享该接口总配额）
     */
    GLOBAL,

    /**
     * 客户端真实 IP 维度（防恶意爬虫、撞库刷接口）
     */
    IP,

    /**
     * 当前登录用户维度（防狂点、重复并发，避免局域网 NAT 误伤同 IP 用户）
     */
    USER,

    /**
     * 业务参数维度（基于 Spring EL 表达式提取特定入参，如手机号、订单号等）
     */
    PARAM
}
