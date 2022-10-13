package com.alex.generator.config;

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
 * description:  swagger配置类
 * author:       alex
 * createDate:   2021/6/6 15:17
 * version:      1.0.0
 */
@Configuration
@EnableOpenApi
@Profile({"test", "dev"})
public class SwaggerConfig {

    @Bean(value = "generateApi")
    public Docket buildDocket() {
        return new Docket(DocumentationType.OAS_30)
//                .pathMapping("/am-finance")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.alex.generator.controller"))
                .paths(PathSelectors.any())
                .build()
                .groupName("alex-generate")
                ;//注意这里
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("alex miaosha user document")
                .contact(new Contact("alex", "localhost", "734663446@qq.com"))
                .description("ha ha ha ! be happy")
                .termsOfServiceUrl("www.baidu.com")
                .version("1.0-version")
                .build();
    }
}
