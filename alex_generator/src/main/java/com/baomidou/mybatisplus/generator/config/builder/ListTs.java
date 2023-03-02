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

public class ListTs implements ITemplate {

    private ConverterFileName converterFileName;

    private ListTs() {
        this.converterFileName = (entityName) -> Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1) + "ListTs";
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
        private final ListTs listTs = new ListTs();

        public Builder(@NotNull StrategyConfig strategyConfig) {
            super(strategyConfig);
        }


        public Builder convertFileName(@NotNull ConverterFileName converter) {
            this.listTs.converterFileName = converter;
            return this;
        }

        public Builder formatListTsFileName(@NotNull String format) {
            return this.convertFileName((entityName) -> String.format(format, entityName));
        }

        @NotNull
        public ListTs get() {
            return this.listTs;
        }
    }
}
