//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.baomidou.mybatisplus.generator.config.builder;

import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.GlobalConfig.Builder;

public class GeneratorBuilder {
    public GeneratorBuilder() {
    }

    public static GlobalConfig globalConfig() {
        return (new Builder()).build();
    }

    public static Builder globalConfigBuilder() {
        return new Builder();
    }

    public static PackageConfig packageConfig() {
        return (new com.baomidou.mybatisplus.generator.config.PackageConfig.Builder()).build();
    }

    public static com.baomidou.mybatisplus.generator.config.PackageConfig.Builder packageConfigBuilder() {
        return new com.baomidou.mybatisplus.generator.config.PackageConfig.Builder();
    }

    public static StrategyConfig strategyConfig() {
        return (new com.baomidou.mybatisplus.generator.config.StrategyConfig.Builder()).build();
    }

    public static com.baomidou.mybatisplus.generator.config.StrategyConfig.Builder strategyConfigBuilder() {
        return new com.baomidou.mybatisplus.generator.config.StrategyConfig.Builder();
    }

    public static TemplateConfig templateConfig() {
        return (new com.baomidou.mybatisplus.generator.config.TemplateConfig.Builder()).build();
    }

    public static com.baomidou.mybatisplus.generator.config.TemplateConfig.Builder templateConfigBuilder() {
        return new com.baomidou.mybatisplus.generator.config.TemplateConfig.Builder();
    }

    public static InjectionConfig injectionConfig() {
        return (new com.baomidou.mybatisplus.generator.config.InjectionConfig.Builder()).build();
    }

    public static com.baomidou.mybatisplus.generator.config.InjectionConfig.Builder injectionConfigBuilder() {
        return new com.baomidou.mybatisplus.generator.config.InjectionConfig.Builder();
    }
}
