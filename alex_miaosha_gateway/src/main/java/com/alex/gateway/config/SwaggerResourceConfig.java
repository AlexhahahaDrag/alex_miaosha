package com.alex.gateway.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * description: Swagger / OpenAPI 聚合文档资源提供器
 * author:       majf, alex
 * createDate:   2022/10/8 17:16
 * version:      2.0.0
 */
@Slf4j
@Component
@Primary
@AllArgsConstructor
public class SwaggerResourceConfig implements SwaggerResourcesProvider {

    private final RouteLocator routeLocator;
    private final GatewayProperties gatewayProperties;

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();
        List<String> routes;
        try {
            // 同步等待路由加载完成，彻底消除 subscribe 异步竞态导致空列表问题
            routes = routeLocator.getRoutes()
                    .map(Route::getId)
                    .collectList()
                    .blockOptional()
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            log.warn("获取动态路由列表异常，降级使用 GatewayProperties 预设路由: {}", e.getMessage());
            routes = gatewayProperties.getRoutes().stream()
                    .map(org.springframework.cloud.gateway.route.RouteDefinition::getId)
                    .toList();
        }

        final List<String> activeRouteIds = routes;
        // 过滤出配置文件中定义的路由 -> 过滤出 Path Route Predicate -> 根据路径拼接成 api-docs 路径 -> 生成 SwaggerResource
        gatewayProperties.getRoutes().stream()
                .filter(routeDefinition -> activeRouteIds.contains(routeDefinition.getId()))
                .forEach(route -> route.getPredicates().stream()
                        .filter(predicateDefinition -> "Path".equalsIgnoreCase(predicateDefinition.getName()))
                        .forEach(predicateDefinition -> resources.add(swaggerResource(
                                route.getId(),
                                route.getId(),
                                predicateDefinition.getArgs().get(NameUtils.GENERATED_NAME_PREFIX + "0")
                                        .replace("**", "v3/api-docs?group=" + route.getId())))));
        return resources;
    }

    private SwaggerResource swaggerResource(String url, String name, String location) {
        log.debug("Swagger resource registered: name={}, location={}", name, location);
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion("3.0");
        return swaggerResource;
    }
}
