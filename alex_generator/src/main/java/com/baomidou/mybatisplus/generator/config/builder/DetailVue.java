//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.baomidou.mybatisplus.generator.config.builder;

import com.baomidou.mybatisplus.generator.ITemplate;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.function.ConverterFileName;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class DetailVue implements ITemplate {

    private ConverterFileName converterFileName;

    private DetailVue() {
        this.converterFileName = (entityName) -> entityName + "Detail";
    }

    @NotNull
    public ConverterFileName getConverterFileName() {
        return this.converterFileName;
    }

    @NotNull
    public Map<String, Object> renderData(@NotNull TableInfo tableInfo) {
        Map<String, Object> data = new HashMap();
        return data;
    }

    public static class Builder extends BaseBuilder {
        private final DetailVue detailVue = new DetailVue();

        public Builder(@NotNull StrategyConfig strategyConfig) {
            super(strategyConfig);
        }

        public Builder convertFileName(@NotNull ConverterFileName converter) {
            this.detailVue.converterFileName = converter;
            return this;
        }

        public Builder formatVueFileName(@NotNull String format) {
            return this.convertFileName((entityName) -> String.format(format, entityName));
        }

        @NotNull
        public DetailVue get() {
            return this.detailVue;
        }
    }
}
