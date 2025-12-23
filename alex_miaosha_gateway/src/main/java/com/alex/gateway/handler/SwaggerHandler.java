package com.alex.gateway.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import springfox.documentation.swagger.web.*;

import java.util.Optional;

/**
 * description:
 * author:       majf
 * createDate:   2022/10/8 17:05
 * version:      1.0.0
 */
@RestController
@RequestMapping(value = "/swagger-resources")
public class SwaggerHandler {

    private final SwaggerResourcesProvider swaggerResources;

    @Nullable
    private final SecurityConfiguration securityConfiguration;

    @Nullable
    private final UiConfiguration uiConfiguration;

    // AI Agent：使用构造函数注入替代字段注入，提高代码可测试性和安全性
    // 说明：构造函数注入可以确保依赖明确，并且便于单元测试
    // 注意：SecurityConfiguration 和 UiConfiguration 是可选的，使用 @Nullable 标记，Spring 会允许它们为 null
    public SwaggerHandler(SwaggerResourcesProvider swaggerResources,
                         @Nullable SecurityConfiguration securityConfiguration,
                         @Nullable UiConfiguration uiConfiguration) {
        this.swaggerResources = swaggerResources;
        this.securityConfiguration = securityConfiguration;
        this.uiConfiguration = uiConfiguration;
    }

    /**
     * Swagger安全配置，支持oauth和apiKey设置
     */
    @GetMapping("/configuration/security")
    public Mono<ResponseEntity<SecurityConfiguration>> securityConfiguration() {
        return Mono.just(new ResponseEntity<>(
                Optional.ofNullable(securityConfiguration).orElse(SecurityConfigurationBuilder.builder().build()), HttpStatus.OK));
    }

    /**
     * Swagger UI配置
     */
    @GetMapping("/configuration/ui")
    public Mono<ResponseEntity<UiConfiguration>> uiConfiguration() {
        return Mono.just(new ResponseEntity<>(
                Optional.ofNullable(uiConfiguration).orElse(UiConfigurationBuilder.builder().build()), HttpStatus.OK));
    }

    /**
     * Swagger资源配置，微服务中这各个服务的api-docs信息
     */
    @GetMapping()
    public Mono<ResponseEntity> swaggerResources() {
        return Mono.just((new ResponseEntity<>(swaggerResources.get(), HttpStatus.OK)));
    }
}
