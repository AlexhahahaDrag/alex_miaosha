//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.baomidou.mybatisplus.generator.config.builder;

import com.baomidou.mybatisplus.generator.IDatabaseQuery;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

public class ConfigBuilder {
    private final TemplateConfig templateConfig;
    private final List<TableInfo> tableInfoList = new ArrayList();
    private final Map<OutputFile, String> pathInfo = new HashMap();
    private StrategyConfig strategyConfig;
    private GlobalConfig globalConfig;
    private InjectionConfig injectionConfig;
    private static final Pattern REGX = Pattern.compile("[~!/@#$%^&*()+\\\\\\[\\]|{};:'\",<.>?]+");
    private final PackageConfig packageConfig;
    private final DataSourceConfig dataSourceConfig;

    public ConfigBuilder(@Nullable PackageConfig packageConfig, @NotNull DataSourceConfig dataSourceConfig, @Nullable StrategyConfig strategyConfig, @Nullable TemplateConfig templateConfig, @Nullable GlobalConfig globalConfig, @Nullable InjectionConfig injectionConfig) {
        this.dataSourceConfig = dataSourceConfig;
        this.strategyConfig = (StrategyConfig)Optional.ofNullable(strategyConfig).orElseGet(() -> {
            return GeneratorBuilder.strategyConfig();
        });
        this.globalConfig = (GlobalConfig)Optional.ofNullable(globalConfig).orElseGet(() -> {
            return GeneratorBuilder.globalConfig();
        });
        this.templateConfig = (TemplateConfig)Optional.ofNullable(templateConfig).orElseGet(() -> GeneratorBuilder.templateConfig());
        this.packageConfig = (PackageConfig)Optional.ofNullable(packageConfig).orElseGet(() -> GeneratorBuilder.packageConfig());
        this.injectionConfig = (InjectionConfig)Optional.ofNullable(injectionConfig).orElseGet(() -> GeneratorBuilder.injectionConfig());
        this.pathInfo.putAll((new PathInfoHandler(this.globalConfig, this.templateConfig, this.packageConfig)).getPathInfo());
    }

    public static boolean matcherRegTable(@NotNull String tableName) {
        return REGX.matcher(tableName).find();
    }

    @NotNull
    public ConfigBuilder setStrategyConfig(@NotNull StrategyConfig strategyConfig) {
        this.strategyConfig = strategyConfig;
        return this;
    }

    @NotNull
    public ConfigBuilder setGlobalConfig(@NotNull GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
        return this;
    }

    @NotNull
    public ConfigBuilder setInjectionConfig(@NotNull InjectionConfig injectionConfig) {
        this.injectionConfig = injectionConfig;
        return this;
    }

    @NotNull
    public TemplateConfig getTemplateConfig() {
        return this.templateConfig;
    }

    @NotNull
    public List<TableInfo> getTableInfoList() {
        if (this.tableInfoList.isEmpty()) {
            List<TableInfo> tableInfos = (new IDatabaseQuery.DefaultDatabaseQuery(this)).queryTables();
            if (!tableInfos.isEmpty()) {
                this.tableInfoList.addAll(tableInfos);
            }
        }

        return this.tableInfoList;
    }

    @NotNull
    public Map<OutputFile, String> getPathInfo() {
        return this.pathInfo;
    }

    @NotNull
    public StrategyConfig getStrategyConfig() {
        return this.strategyConfig;
    }

    @NotNull
    public GlobalConfig getGlobalConfig() {
        return this.globalConfig;
    }

    @Nullable
    public InjectionConfig getInjectionConfig() {
        return this.injectionConfig;
    }

    @NotNull
    public PackageConfig getPackageConfig() {
        return this.packageConfig;
    }

    @NotNull
    public DataSourceConfig getDataSourceConfig() {
        return this.dataSourceConfig;
    }
}
