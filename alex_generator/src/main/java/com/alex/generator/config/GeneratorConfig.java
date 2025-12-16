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

    // 父包名
    private String parentPackage;

    // 是否生成 feign
    private boolean feign;

    // 是否生成 vue
    private boolean vue;

    // 是否生成 mobile
    private boolean mobile;

    // 实体类字段
    private String superEntityColumns;

    // 添加超级VO字段
    private String addSuperVoColumns;

    // 表前缀
    private String tablePrefix;

    // 表后缀
    private String tableSuffix;

    // 逻辑删除字段名
    private String logicDeleteColumnName;

    // 逻辑删除属性名
    private String logicDeletePropertyName;

    // Java 路径
    private String javaPath;

    // VO 路径
    private String voPath;

    // Feign 路径
    private String feignPath;

    // Vue 路径
    private String vuePath;

    // TS 路径
    private String tsPath;

    // Mobile Vue 路径
    private String mobileVuePath;

    // Mobile TS 路径
    private String mobileTsPath;
}
