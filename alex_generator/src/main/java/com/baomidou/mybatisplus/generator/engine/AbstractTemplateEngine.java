//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.baomidou.mybatisplus.generator.engine;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.util.FileUtils;
import com.baomidou.mybatisplus.generator.util.RuntimeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractTemplateEngine {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ConfigBuilder configBuilder;

    private static final String TS = ".ts";

    private static final String VUE = ".vue";

    public AbstractTemplateEngine() {
    }

    @NotNull
    public abstract AbstractTemplateEngine init(@NotNull ConfigBuilder configBuilder);

    protected void outputCustomFile(@NotNull Map<String, String> customFile, @NotNull TableInfo tableInfo, @NotNull Map<String, Object> objectMap) {
        String otherPath = this.getPathInfo(OutputFile.other);
        String feignPath = this.getPathInfo(OutputFile.feign);
        customFile.forEach((key, value) -> {
            String fileName = String.format(feignPath + File.separator + File.separator + "%s", key);
            this.outputFile(new File(fileName), objectMap, value);
        });
    }

    protected void outputEntity(@NotNull TableInfo tableInfo, @NotNull Map<String, Object> objectMap) {
        String entityName = tableInfo.getEntityName();
        String entityPath = this.getPathInfo(OutputFile.entity);
        if (StringUtils.isNotBlank(entityName) && StringUtils.isNotBlank(entityPath)) {
            this.getTemplateFilePath((template) -> template.getEntity(this.getConfigBuilder().getGlobalConfig().isKotlin())).ifPresent((entity) -> {
                String entityFile = String.format(entityPath + File.separator + "%s" + this.suffixJavaOrKt(), entityName);
                this.outputFile(new File(entityFile), objectMap, entity);
            });
        }

    }

    protected void outputMapper(@NotNull TableInfo tableInfo, @NotNull Map<String, Object> objectMap) {
        String entityName = tableInfo.getEntityName();
        String mapperPath = this.getPathInfo(OutputFile.mapper);
        if (StringUtils.isNotBlank(tableInfo.getMapperName()) && StringUtils.isNotBlank(mapperPath)) {
            this.getTemplateFilePath(TemplateConfig::getMapper).ifPresent((mapper) -> {
                String mapperFile = String.format(mapperPath + File.separator + tableInfo.getMapperName() + this.suffixJavaOrKt(), entityName);
                this.outputFile(new File(mapperFile), objectMap, mapper);
            });
        }

        String xmlPath = this.getPathInfo(OutputFile.mapperXml);
        if (StringUtils.isNotBlank(tableInfo.getXmlName()) && StringUtils.isNotBlank(xmlPath)) {
            this.getTemplateFilePath(TemplateConfig::getXml).ifPresent((xml) -> {
                String xmlFile = String.format(xmlPath + File.separator + tableInfo.getXmlName() + ".xml", entityName);
                this.outputFile(new File(xmlFile), objectMap, xml);
            });
        }

    }

    protected void outputService(@NotNull TableInfo tableInfo, @NotNull Map<String, Object> objectMap) {
        String entityName = tableInfo.getEntityName();
        String servicePath = this.getPathInfo(OutputFile.service);
        if (StringUtils.isNotBlank(tableInfo.getServiceName()) && StringUtils.isNotBlank(servicePath)) {
            this.getTemplateFilePath(TemplateConfig::getService).ifPresent((service) -> {
                String serviceFile = String.format(servicePath + File.separator + tableInfo.getServiceName() + this.suffixJavaOrKt(), entityName);
                this.outputFile(new File(serviceFile), objectMap, service);
            });
        }

        String serviceImplPath = this.getPathInfo(OutputFile.serviceImpl);
        if (StringUtils.isNotBlank(tableInfo.getServiceImplName()) && StringUtils.isNotBlank(serviceImplPath)) {
            this.getTemplateFilePath(TemplateConfig::getServiceImpl).ifPresent((serviceImpl) -> {
                String implFile = String.format(serviceImplPath + File.separator + tableInfo.getServiceImplName() + this.suffixJavaOrKt(), entityName);
                this.outputFile(new File(implFile), objectMap, serviceImpl);
            });
        }

    }

    protected void outputController(@NotNull TableInfo tableInfo, @NotNull Map<String, Object> objectMap) {
        String controllerPath = this.getPathInfo(OutputFile.controller);
        if (StringUtils.isNotBlank(tableInfo.getControllerName()) && StringUtils.isNotBlank(controllerPath)) {
            this.getTemplateFilePath(TemplateConfig::getController).ifPresent((controller) -> {
                String entityName = tableInfo.getEntityName();
                String controllerFile = String.format(controllerPath + File.separator + tableInfo.getControllerName() + this.suffixJavaOrKt(), entityName);
                this.outputFile(new File(controllerFile), objectMap, controller);
            });
        }

    }

    protected void outputVo(@NotNull TableInfo tableInfo, @NotNull Map<String, Object> objectMap) {
        String voName = tableInfo.getVoName();
        String voPath = this.getPathInfo(OutputFile.vo);
        if (StringUtils.isNotBlank(voName) && StringUtils.isNotBlank(voPath)) {
            this.getTemplateFilePath((template) -> {
                return template.getVo();
            }).ifPresent((entity) -> {
                String voFile = String.format(voPath + File.separator + "%s" + this.suffixJavaOrKt(), voName);
                this.outputFile(new File(voFile), objectMap, entity);
            });
        }

    }

    protected void outputClient(@NotNull TableInfo tableInfo, @NotNull Map<String, Object> objectMap) {
        String clientPath = this.getPathInfo(OutputFile.client);
        if (StringUtils.isNotBlank(tableInfo.getClientName()) && StringUtils.isNotBlank(clientPath)) {
            this.getTemplateFilePath(TemplateConfig::getClient).ifPresent((client) -> {
                String clientName = tableInfo.getClientName();
                String clientFile = String.format(clientPath + File.separator + tableInfo.getClientName() + this.suffixJavaOrKt(), clientName);
                this.outputFile(new File(clientFile), objectMap, client);
            });
        }

    }

    protected void outputFile(@NotNull File file, @NotNull Map<String, Object> objectMap, @NotNull String templatePath) {
        if (this.isCreate(file)) {
            try {
                boolean exist = file.exists();
                if (!exist) {
                    File parentFile = file.getParentFile();
                    FileUtils.forceMkdir(parentFile);
                }

                this.writer(objectMap, templatePath, file);
            } catch (Exception var6) {
                throw new RuntimeException(var6);
            }
        }
    }

    protected void outputDetailTs(@NotNull TableInfo tableInfo, @NotNull Map<String, Object> objectMap) {
        String detailTsPath = this.getPathInfo(OutputFile.detail);
        if (StringUtils.isNotBlank(tableInfo.getDetailTsName()) && StringUtils.isNotBlank(detailTsPath)) {
            this.getTemplateFilePath(TemplateConfig::getDetailTs).ifPresent((detailTs) -> {
                String detailTsName = tableInfo.getDetailTsName();
                String detailTsFile = String.format(detailTsPath + File.separator + tableInfo.getDetailTsName() + TS, detailTsName);
                this.outputFile(new File(detailTsFile), objectMap, detailTs);
            });
        }
    }

    protected void outputDetailVue(@NotNull TableInfo tableInfo, @NotNull Map<String, Object> objectMap) {
        String detailVuePath = this.getPathInfo(OutputFile.detail);
        if (StringUtils.isNotBlank(tableInfo.getDetailVueName()) && StringUtils.isNotBlank(detailVuePath)) {
            this.getTemplateFilePath(TemplateConfig::getDetailVue).ifPresent((detailVue) -> {
                String detailVueName = tableInfo.getDetailVueName();
                String detailVueFile = String.format(detailVuePath + File.separator + tableInfo.getDetailVueName() + VUE, detailVueName);
                this.outputFile(new File(detailVueFile), objectMap, detailVue);
            });
        }
    }

    protected void outputListTs(@NotNull TableInfo tableInfo, @NotNull Map<String, Object> objectMap) {
        String tsPath = this.getPathInfo(OutputFile.list);
        if (StringUtils.isNotBlank(tableInfo.getListTsName()) && StringUtils.isNotBlank(tsPath)) {
            this.getTemplateFilePath(TemplateConfig::getListTs).ifPresent((listTs) -> {
                String clientName = tableInfo.getClientName();
                String tsTsFile = String.format(tsPath + File.separator + tableInfo.getListTsName() + TS, clientName);
                this.outputFile(new File(tsTsFile), objectMap, listTs);
            });
        }
    }

    protected void outputListVue(@NotNull TableInfo tableInfo, @NotNull Map<String, Object> objectMap) {
        String listVuePath = this.getPathInfo(OutputFile.list);
        if (StringUtils.isNotBlank(tableInfo.getListVueName()) && StringUtils.isNotBlank(listVuePath)) {
            this.getTemplateFilePath(TemplateConfig::getListVue).ifPresent((listVue) -> {
                String listVueName = tableInfo.getListVueName();
                String listVueFile = String.format(listVuePath + File.separator + tableInfo.getListVueName() + VUE, listVueName);
                this.outputFile(new File(listVueFile), objectMap, listVue);
            });
        }
    }

    protected void outputTsTs(@NotNull TableInfo tableInfo, @NotNull Map<String, Object> objectMap) {
        String tsTsPath = this.getPathInfo(OutputFile.ts);
        if (StringUtils.isNotBlank(tableInfo.getTsTsName()) && StringUtils.isNotBlank(tsTsPath)) {
            this.getTemplateFilePath(TemplateConfig::getTsTs).ifPresent((tsTs) -> {
                String tsTsName = tableInfo.getTsTsName();
                String tsTsFile = String.format(tsTsPath + File.separator + tableInfo.getTsTsName() + TS, tsTsName);
                this.outputFile(new File(tsTsFile), objectMap, tsTs);
            });
        }
    }

    protected void outputMobileTsTs(@NotNull TableInfo tableInfo, @NotNull Map<String, Object> objectMap) {
        String mobileTsTsPath = this.getPathInfo(OutputFile.mobileTsTs);
        if (StringUtils.isNotBlank(tableInfo.getMobileTsTsName()) && StringUtils.isNotBlank(mobileTsTsPath)) {
            this.getTemplateFilePath(TemplateConfig::getMobileTsTs).ifPresent((detailTs) -> {
                String mobileTsTsName = tableInfo.getMobileTsTsName();
                String mobileTsTsFile = String.format(mobileTsTsPath + File.separator + tableInfo.getDetailTsName() + TS, mobileTsTsName);
                this.outputFile(new File(mobileTsTsFile), objectMap, detailTs);
            });
        }
    }

    protected void outputMobileDetail(@NotNull TableInfo tableInfo, @NotNull Map<String, Object> objectMap) {
        String mobileDetailPath = this.getPathInfo(OutputFile.mobileDetail);
        if (StringUtils.isNotBlank(tableInfo.getMobileDetailName()) && StringUtils.isNotBlank(mobileDetailPath)) {
            this.getTemplateFilePath(TemplateConfig::getMobileDetail).ifPresent((detailVue) -> {
                String mobileDetailName = tableInfo.getMobileDetailName();
                String mobileDetailFile = String.format(mobileDetailPath + File.separator + tableInfo.getDetailVueName() + VUE, mobileDetailName);
                this.outputFile(new File(mobileDetailFile), objectMap, detailVue);
            });
        }
    }

    protected void outputMobileDetailTs(@NotNull TableInfo tableInfo, @NotNull Map<String, Object> objectMap) {
        String mobileDetailTsPath = this.getPathInfo(OutputFile.mobileDetailTs);
        if (StringUtils.isNotBlank(tableInfo.getMobileDetailTsName()) && StringUtils.isNotBlank(mobileDetailTsPath)) {
            this.getTemplateFilePath(TemplateConfig::getMobileDetailTs).ifPresent((listTs) -> {
                String clientName = tableInfo.getClientName();
                String mobileDetailTsFile = String.format(mobileDetailTsPath + File.separator + tableInfo.getListTsName() + TS, clientName);
                this.outputFile(new File(mobileDetailTsFile), objectMap, listTs);
            });
        }
    }

    protected void outputMobileVue(@NotNull TableInfo tableInfo, @NotNull Map<String, Object> objectMap) {
        String mobileVuePath = this.getPathInfo(OutputFile.mobileVue);
        if (StringUtils.isNotBlank(tableInfo.getMobileVueName()) && StringUtils.isNotBlank(mobileVuePath)) {
            this.getTemplateFilePath(TemplateConfig::getMobileVue).ifPresent((listVue) -> {
                String mobileVueName = tableInfo.getMobileVueName();
                String mobileVueFile = String.format(mobileVuePath + File.separator + tableInfo.getListVueName() + VUE, mobileVueName);
                this.outputFile(new File(mobileVueFile), objectMap, listVue);
            });
        }
    }

    protected void outputMobileTs(@NotNull TableInfo tableInfo, @NotNull Map<String, Object> objectMap) {
        String mobileTsPath = this.getPathInfo(OutputFile.mobileTs);
        if (StringUtils.isNotBlank(tableInfo.getMobileTsName()) && StringUtils.isNotBlank(mobileTsPath)) {
            this.getTemplateFilePath(TemplateConfig::getMobileTs).ifPresent((tsTs) -> {
                String mobileTsName = tableInfo.getMobileTsName();
                String mobileTsFile = String.format(mobileTsPath + File.separator + tableInfo.getTsTsName() + TS, mobileTsName);
                this.outputFile(new File(mobileTsFile), objectMap, tsTs);
            });
        }
    }

    @NotNull
    protected Optional<String> getTemplateFilePath(@NotNull Function<TemplateConfig, String> function) {
        TemplateConfig templateConfig = this.getConfigBuilder().getTemplateConfig();
        String filePath = function.apply(templateConfig);
        return StringUtils.isNotBlank(filePath) ? Optional.of(this.templateFilePath(filePath)) : Optional.empty();
    }

    @Nullable
    protected String getPathInfo(@NotNull OutputFile outputFile) {
        return this.getConfigBuilder().getPathInfo().get(outputFile);
    }

    @NotNull
    public AbstractTemplateEngine batchOutput() {
        try {
            ConfigBuilder config = this.getConfigBuilder();
            List<TableInfo> tableInfoList = config.getTableInfoList();
            tableInfoList.forEach((tableInfo) -> {
                Map<String, Object> objectMap = this.getObjectMap(config, tableInfo);
                Optional.ofNullable(config.getInjectionConfig()).ifPresent((t) -> {
                    t.beforeOutputFile(tableInfo, objectMap);
                    this.outputCustomFile(t.getCustomFile(), tableInfo, objectMap);
                });
                this.outputEntity(tableInfo, objectMap);
                this.outputMapper(tableInfo, objectMap);
                this.outputService(tableInfo, objectMap);
                this.outputController(tableInfo, objectMap);
                this.outputVo(tableInfo, objectMap);
                if (tableInfo.getFeignGenerator()) {
                    this.outputClient(tableInfo, objectMap);
                }
                //设置是否生成前端代码
                if (tableInfo.getVueGenerator()) {
                    this.outputDetailVue(tableInfo, objectMap);
                    this.outputDetailTs(tableInfo, objectMap);
                    this.outputListVue(tableInfo, objectMap);
                    this.outputListTs(tableInfo, objectMap);
                    this.outputTsTs(tableInfo, objectMap);
                }
                if (tableInfo.getMobileGenerator()) {
                    this.outputMobileDetail(tableInfo, objectMap);
                    this.outputMobileDetailTs(tableInfo, objectMap);
                    this.outputMobileTs(tableInfo, objectMap);
                    this.outputMobileVue(tableInfo, objectMap);
                    this.outputMobileTsTs(tableInfo, objectMap);
                }
            });
            return this;
        } catch (Exception var3) {
            throw new RuntimeException("无法创建文件，请检查配置信息！", var3);
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    protected void writerFile(Map<String, Object> objectMap, String templatePath, String outputFile) throws Exception {
        if (StringUtils.isNotBlank(templatePath)) {
            this.writer(objectMap, templatePath, outputFile);
        }

    }

    /**
     * @deprecated
     */
    @Deprecated
    public void writer(@NotNull Map<String, Object> objectMap, @NotNull String templatePath, @NotNull String outputFile) throws Exception {
    }

    public void writer(@NotNull Map<String, Object> objectMap, @NotNull String templatePath, @NotNull File outputFile) throws Exception {
        this.writer(objectMap, templatePath, outputFile.getPath());
        this.logger.debug("模板:" + templatePath + ";  文件:" + outputFile);
    }

    public void open() {
        String outDir = this.getConfigBuilder().getGlobalConfig().getOutputDir();
        if (!StringUtils.isBlank(outDir) && (new File(outDir)).exists()) {
            if (this.getConfigBuilder().getGlobalConfig().isOpen()) {
                try {
                    RuntimeUtils.openDir(outDir);
                } catch (IOException var3) {
                    this.logger.error(var3.getMessage(), var3);
                }
            }
        } else {
            System.err.println("未找到输出目录：" + outDir);
        }

    }

    @NotNull
    public Map<String, Object> getObjectMap(@NotNull ConfigBuilder config, @NotNull TableInfo tableInfo) {
        StrategyConfig strategyConfig = config.getStrategyConfig();
        Map<String, Object> controllerData = strategyConfig.client().renderData(tableInfo);
        Map<String, Object> objectMap = new HashMap(controllerData);
        Map<String, Object> mapperData = strategyConfig.mapper().renderData(tableInfo);
        objectMap.putAll(mapperData);
        Map<String, Object> serviceData = strategyConfig.service().renderData(tableInfo);
        objectMap.putAll(serviceData);
        Map<String, Object> entityData = strategyConfig.entity().renderData(tableInfo);
        objectMap.putAll(entityData);
        Map<String, Object> voData = strategyConfig.vo().renderData(tableInfo);
        objectMap.putAll(voData);
        Map<String, Object> clientData = strategyConfig.controller().renderData(tableInfo);
        objectMap.putAll(clientData);
        objectMap.put("config", config);
        objectMap.put("package", config.getPackageConfig().getPackageInfo());
        GlobalConfig globalConfig = config.getGlobalConfig();
        objectMap.put("author", globalConfig.getAuthor());
        objectMap.put("kotlin", globalConfig.isKotlin());
        objectMap.put("swagger", globalConfig.isSwagger());
        objectMap.put("date", globalConfig.getCommentDate());
        String schemaName = "";
        if (strategyConfig.isEnableSchema()) {
            schemaName = config.getDataSourceConfig().getSchemaName();
            if (StringUtils.isNotBlank(schemaName)) {
                schemaName = schemaName + ".";
                tableInfo.setConvert(true);
            }
        }

        objectMap.put("schemaName", schemaName);
        objectMap.put("table", tableInfo);
        objectMap.put("entity", tableInfo.getEntityName());
        return objectMap;
    }

    @NotNull
    public abstract String templateFilePath(@NotNull String filePath);

    /**
     * @deprecated
     */
    @Deprecated
    protected boolean isCreate(String filePath) {
        return this.isCreate(new File(filePath));
    }

    protected boolean isCreate(@NotNull File file) {
        return !file.exists() || this.getConfigBuilder().getGlobalConfig().isFileOverride();
    }

    protected String suffixJavaOrKt() {
        return this.getConfigBuilder().getGlobalConfig().isKotlin() ? ".kt" : ".java";
    }

    @NotNull
    public ConfigBuilder getConfigBuilder() {
        return this.configBuilder;
    }

    @NotNull
    public AbstractTemplateEngine setConfigBuilder(@NotNull ConfigBuilder configBuilder) {
        this.configBuilder = configBuilder;
        return this;
    }
}
