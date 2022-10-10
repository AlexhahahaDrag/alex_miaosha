//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.baomidou.mybatisplus.generator.config.builder;

import com.baomidou.mybatisplus.generator.ITemplate;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.function.ConverterFileName;
import com.baomidou.mybatisplus.generator.util.ClassUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Service implements ITemplate {
    private String superServiceClass;
    private String superServiceImplClass;
    private ConverterFileName converterServiceFileName;
    private ConverterFileName converterServiceImplFileName;

    private Service() {
        this.superServiceClass = "com.baomidou.mybatisplus.extension.service.IService";
        this.superServiceImplClass = "com.baomidou.mybatisplus.extension.service.impl.ServiceImpl";
        this.converterServiceFileName = (entityName) -> {
            return "I" + entityName + "Service";
        };
        this.converterServiceImplFileName = (entityName) -> {
            return entityName + "ServiceImpl";
        };
    }

    @NotNull
    public String getSuperServiceClass() {
        return this.superServiceClass;
    }

    @NotNull
    public String getSuperServiceImplClass() {
        return this.superServiceImplClass;
    }

    @NotNull
    public ConverterFileName getConverterServiceFileName() {
        return this.converterServiceFileName;
    }

    @NotNull
    public ConverterFileName getConverterServiceImplFileName() {
        return this.converterServiceImplFileName;
    }

    @NotNull
    public Map<String, Object> renderData(@NotNull TableInfo tableInfo) {
        Map<String, Object> data = new HashMap();
        data.put("superServiceClassPackage", this.superServiceClass);
        data.put("superServiceClass", ClassUtils.getSimpleName(this.superServiceClass));
        data.put("superServiceImplClassPackage", this.superServiceImplClass);
        data.put("superServiceImplClass", ClassUtils.getSimpleName(this.superServiceImplClass));
        return data;
    }

    public static class Builder extends BaseBuilder {
        private final Service service = new Service();

        public Builder(@NotNull StrategyConfig strategyConfig) {
            super(strategyConfig);
        }

        public Builder superServiceClass(@NotNull Class<?> clazz) {
            return this.superServiceClass(clazz.getName());
        }

        public Builder superServiceClass(@NotNull String superServiceClass) {
            this.service.superServiceClass = superServiceClass;
            return this;
        }

        public Builder superServiceImplClass(@NotNull Class<?> clazz) {
            return this.superServiceImplClass(clazz.getName());
        }

        public Builder superServiceImplClass(@NotNull String superServiceImplClass) {
            this.service.superServiceImplClass = superServiceImplClass;
            return this;
        }

        public Builder convertServiceFileName(@NotNull ConverterFileName converter) {
            this.service.converterServiceFileName = converter;
            return this;
        }

        public Builder convertServiceImplFileName(@NotNull ConverterFileName converter) {
            this.service.converterServiceImplFileName = converter;
            return this;
        }

        public Builder formatServiceFileName(@NotNull String format) {
            return this.convertServiceFileName((entityName) -> {
                return String.format(format, entityName);
            });
        }

        public Builder formatServiceImplFileName(@NotNull String format) {
            return this.convertServiceImplFileName((entityName) -> {
                return String.format(format, entityName);
            });
        }

        @NotNull
        public Service get() {
            return this.service;
        }
    }
}
