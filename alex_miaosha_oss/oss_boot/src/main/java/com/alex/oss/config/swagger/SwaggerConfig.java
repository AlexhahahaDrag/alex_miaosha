package com.alex.oss.config.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 *description:  swagger配置类
 *author:       alex
 *createDate:   2021/6/6 15:17
 *version:      1.0.0
 */
@Configuration
@EnableOpenApi
@Profile({"test", "dev"})
public class SwaggerConfig {

    @Bean(value = "ossApi")
    public Docket buildDocket() {
        return new Docket(DocumentationType.OAS_30)
                .pathMapping("/am-oss")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.alex.oss"))
                .paths(PathSelectors.any())
                .build()
                .groupName("alex-oss")
                ;
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("alex miaosha oss document")
                .contact(new Contact("alex", "localhost", "734663446@qq.com"))
                .description("ha ha ha ! be happy")
                .termsOfServiceUrl("www.baidu.com")
                .version("1.0-version")
                .build();
    }

    /**
     * 兼容 Springfox 3 + Spring Boot 2.6+：去掉使用 PathPatternParser 的 mapping，避免 documentationPluginsBootstrapper NPE 启动报错
     */
    @Bean
    public static org.springframework.beans.factory.config.BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new org.springframework.beans.factory.config.BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws org.springframework.beans.BeansException {
                if (bean instanceof springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider
                        || bean instanceof springfox.documentation.spring.web.plugins.WebFluxRequestHandlerProvider) {
                    customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
                }
                return bean;
            }

            private <T extends org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(java.util.List<T> mappings) {
                java.util.List<T> copy = mappings.stream()
                        .filter(mapping -> mapping.getPatternParser() == null)
                        .collect(java.util.stream.Collectors.toList());
                mappings.clear();
                mappings.addAll(copy);
            }

            @SuppressWarnings("unchecked")
            private java.util.List<org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
                try {
                    java.lang.reflect.Field field = org.springframework.util.ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                    if (field == null) {
                        throw new IllegalStateException("handlerMappings field not found on " + bean.getClass());
                    }
                    field.setAccessible(true);
                    return (java.util.List<org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping>) field.get(bean);
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }
}
