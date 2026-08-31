package com.alex.ai.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.WebFluxRequestHandlerProvider;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI Swagger / OpenAPI 配置（Knife4j + Apifox 导入用）。
 * <p>
 * Spring Boot 2.6+ 默认 PathPatternParser 与 Springfox 3 不兼容，需过滤带 patternParser 的 HandlerMapping，
 * 否则 documentationPluginsBootstrapper 启动 NPE。
 */
@Configuration
@EnableOpenApi
@Profile({"test", "dev", "prod"})
public class SwaggerConfig {

    @Bean(value = "aiApi")
    public Docket buildDocket() {
        return new Docket(DocumentationType.OAS_30)
                .pathMapping("/am-ai")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.alex.ai"))
                .paths(PathSelectors.any())
                .build()
                .groupName("alex-ai");
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("alex miaosha ai document")
                .contact(new Contact("alex", "localhost", "734663446@qq.com"))
                .description("AI chat / stream APIs for Apifox and Knife4j")
                .termsOfServiceUrl("www.baidu.com")
                .version("1.0-version")
                .build();
    }

    /**
     * 兼容 Springfox 3 + Spring Boot 2.6+：去掉使用 PathPatternParser 的 mapping。
     */
    @Bean
    public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider || bean instanceof WebFluxRequestHandlerProvider) {
                    customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
                }
                return bean;
            }

            private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
                List<T> copy = mappings.stream()
                        .filter(mapping -> mapping.getPatternParser() == null)
                        .collect(Collectors.toList());
                mappings.clear();
                mappings.addAll(copy);
            }

            @SuppressWarnings("unchecked")
            private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
                try {
                    Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                    if (field == null) {
                        throw new IllegalStateException("handlerMappings field not found on " + bean.getClass());
                    }
                    field.setAccessible(true);
                    return (List<RequestMappingInfoHandlerMapping>) field.get(bean);
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }
}
