//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.baomidou.mybatisplus.generator.config.builder;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.ITemplate;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.function.ConverterFileName;
import com.baomidou.mybatisplus.generator.util.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ListVue implements ITemplate {

    private boolean restStyle;

    private boolean hyphenStyle;

    private String superClass;

    private ConverterFileName converterFileName;

    private ListVue() {
        this.converterFileName = (entityName) -> entityName + "List";
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
        private final ListVue listVue = new ListVue();

        public Builder(@NotNull StrategyConfig strategyConfig) {
            super(strategyConfig);
        }

        public Builder convertFileName(@NotNull ConverterFileName converter) {
            this.listVue.converterFileName = converter;
            return this;
        }

        public Builder formatListVueFileName(@NotNull String format) {
            return this.convertFileName((entityName) -> String.format(format, entityName));
        }

        @NotNull
        public ListVue get() {
            return this.listVue;
        }
    }
}
