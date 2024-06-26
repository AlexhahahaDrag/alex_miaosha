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

public class Controller implements ITemplate {
    private boolean restStyle;
    private boolean hyphenStyle;
    private String superClass;
    private ConverterFileName converterFileName;

    private Controller() {
        this.converterFileName = (entityName) -> entityName + "Controller";
    }

    public boolean isRestStyle() {
        return this.restStyle;
    }

    public boolean isHyphenStyle() {
        return this.hyphenStyle;
    }

    @Nullable
    public String getSuperClass() {
        return this.superClass;
    }

    @NotNull
    public ConverterFileName getConverterFileName() {
        return this.converterFileName;
    }

    @NotNull
    public Map<String, Object> renderData(@NotNull TableInfo tableInfo) {
        Map<String, Object> data = new HashMap();
        data.put("controllerMappingHyphen", StringUtils.camelToHyphen(tableInfo.getEntityPath()));
        data.put("controllerMappingHyphenStyle", this.hyphenStyle);
        data.put("restControllerStyle", this.restStyle);
        data.put("superControllerClassPackage", StringUtils.isBlank(this.superClass) ? null : this.superClass);
        data.put("superControllerClass", ClassUtils.getSimpleName(this.superClass));
        return data;
    }

    public static class Builder extends BaseBuilder {
        private final Controller controller = new Controller();

        public Builder(@NotNull StrategyConfig strategyConfig) {
            super(strategyConfig);
        }

        public Builder superClass(@NotNull Class<?> clazz) {
            return this.superClass(clazz.getName());
        }

        public Builder superClass(@NotNull String superClass) {
            this.controller.superClass = superClass;
            return this;
        }

        public Builder enableHyphenStyle() {
            this.controller.hyphenStyle = true;
            return this;
        }

        public Builder enableRestStyle() {
            this.controller.restStyle = true;
            return this;
        }

        public Builder convertFileName(@NotNull ConverterFileName converter) {
            this.controller.converterFileName = converter;
            return this;
        }

        public Builder formatFileName(@NotNull String format) {
            return this.convertFileName((entityName) -> {
                return String.format(format, entityName);
            });
        }

        @NotNull
        public Controller get() {
            return this.controller;
        }
    }
}
