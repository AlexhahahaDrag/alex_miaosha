//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.baomidou.mybatisplus.generator.config.builder;

import com.baomidou.mybatisplus.generator.config.IConfigBuilder;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.builder.Entity.Builder;
import org.jetbrains.annotations.NotNull;

public class BaseBuilder implements IConfigBuilder<StrategyConfig> {

    private final StrategyConfig strategyConfig;

    public BaseBuilder(@NotNull StrategyConfig strategyConfig) {
        this.strategyConfig = strategyConfig;
    }

    @NotNull
    public Builder entityBuilder() {
        return this.strategyConfig.entityBuilder();
    }

    @NotNull
    public com.baomidou.mybatisplus.generator.config.builder.Controller.Builder controllerBuilder() {
        return this.strategyConfig.controllerBuilder();
    }

    @NotNull
    public com.baomidou.mybatisplus.generator.config.builder.Mapper.Builder mapperBuilder() {
        return this.strategyConfig.mapperBuilder();
    }

    @NotNull
    public com.baomidou.mybatisplus.generator.config.builder.Service.Builder serviceBuilder() {
        return this.strategyConfig.serviceBuilder();
    }

    @NotNull
    public com.baomidou.mybatisplus.generator.config.builder.Vo.Builder voBuilder() {
        return this.strategyConfig.voBuilder();
    }

    @NotNull
    public com.baomidou.mybatisplus.generator.config.builder.Client.Builder clientBuilder() {
        return this.strategyConfig.clientBuilder();
    }

    @NotNull
    public StrategyConfig build() {
        this.strategyConfig.validate();
        return this.strategyConfig;
    }
}
