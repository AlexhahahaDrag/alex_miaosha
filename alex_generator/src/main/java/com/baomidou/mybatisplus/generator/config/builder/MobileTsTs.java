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

public class MobileTsTs implements ITemplate {

    private ConverterFileName converterFileName;

    private MobileTsTs() {
        this.converterFileName = (entityName) -> Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1) + "Ts";
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
        private final MobileTsTs mobileTsTs = new MobileTsTs();

        public Builder(@NotNull StrategyConfig strategyConfig) {
            super(strategyConfig);
        }

        public Builder convertFileName(@NotNull ConverterFileName converter) {
            this.mobileTsTs.converterFileName = converter;
            return this;
        }

        public Builder formatMobileTsTsFileName(@NotNull String format) {
            return this.convertFileName((entityName) -> String.format(format, Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1)));
        }

        @NotNull
        public MobileTsTs get() {
            return this.mobileTsTs;
        }
    }
}
