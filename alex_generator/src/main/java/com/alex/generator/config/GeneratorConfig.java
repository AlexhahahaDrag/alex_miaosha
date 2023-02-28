package com.alex.generator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * description:
 * author:       majf
 * createDate:   2023/2/28 11:14
 * version:      1.0.0
 */
@ConfigurationProperties(prefix = "generator")
@Configuration
@Data
public class GeneratorConfig {

    private String parentPackage;

    private boolean feign;

    private boolean vue;

    private String superEntityColumns;

    private String addSuperVoColumns;

    private String tablePrefix;

    private String logicDeleteColumnName;

    private String logicDeletePropertyName;
}
