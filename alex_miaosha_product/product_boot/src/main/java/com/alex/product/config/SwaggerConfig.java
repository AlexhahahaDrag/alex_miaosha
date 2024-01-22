package com.alex.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.List;

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

    @Bean(value = "productApi")
    public Docket buildDocket() {
        //添加head参数配置start
        List<RequestParameter> globalRequestParameters = new ArrayList<>();
        RequestParameter requestParameter = new RequestParameterBuilder()
                .name("Authorization")
                .description("令牌")
                .in(ParameterType.HEADER)
                .required(true)
                .query(q -> q.model(m -> m.scalarModel(ScalarType.STRING)))
                .build();
        globalRequestParameters.add(requestParameter);
        return new Docket(DocumentationType.OAS_30)
                .pathMapping("/am-product")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.alex.product.controller"))
                .paths(PathSelectors.any())
                .build()
                .groupName("alex-product")
                .globalRequestParameters(globalRequestParameters)
                ;
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("alex miaosha product document")
                .contact(new Contact("alex", "localhost", "734663446@qq.com"))
                .description("ha ha ha ! be happy")
                .termsOfServiceUrl("www.baidu.com")
                .version("1.0-version")
                .build();
    }
}
