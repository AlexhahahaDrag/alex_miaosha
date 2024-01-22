//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.baomidou.mybatisplus.generator.config.builder;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

class PathInfoHandler {
    private final Map<OutputFile, String> pathInfo = new HashMap();
    private final String outputDir;
    private final PackageConfig packageConfig;

    PathInfoHandler(GlobalConfig globalConfig, TemplateConfig templateConfig, PackageConfig packageConfig) {
        this.outputDir = globalConfig.getOutputDir();
        this.packageConfig = packageConfig;
        this.setDefaultPathInfo(globalConfig, templateConfig);
        Map<OutputFile, String> pathInfo = packageConfig.getPathInfo();
        if (CollectionUtils.isNotEmpty(pathInfo)) {
            this.pathInfo.putAll(pathInfo);
        }

    }

    private void setDefaultPathInfo(GlobalConfig globalConfig, TemplateConfig templateConfig) {
        this.putPathInfo(templateConfig.getEntity(globalConfig.isKotlin()), OutputFile.entity, "Entity");
        this.putPathInfo(templateConfig.getMapper(), OutputFile.mapper, "Mapper");
        this.putPathInfo(templateConfig.getXml(), OutputFile.mapperXml, "Xml");
        this.putPathInfo(templateConfig.getService(), OutputFile.service, "Service");
        this.putPathInfo(templateConfig.getServiceImpl(), OutputFile.serviceImpl, "ServiceImpl");
        this.putPathInfo(templateConfig.getController(), OutputFile.controller, "Controller");
        this.putPathInfo(templateConfig.getController(), OutputFile.vo, "Vo");
        this.putPathInfo(templateConfig.getController(), OutputFile.client, "Client");
        this.putPathInfo(OutputFile.other, "Other");
    }

    public Map<OutputFile, String> getPathInfo() {
        return this.pathInfo;
    }

    private void putPathInfo(String template, OutputFile outputFile, String module) {
        if (StringUtils.isNotBlank(template)) {
            this.putPathInfo(outputFile, module);
        }

    }

    private void putPathInfo(OutputFile outputFile, String module) {
        this.pathInfo.putIfAbsent(outputFile, this.joinPath(this.outputDir, this.packageConfig.getPackageInfo(module)));
    }

    private String joinPath(String parentDir, String packageName) {
        if (StringUtils.isBlank(parentDir)) {
            parentDir = System.getProperty("java.io.tmpdir");
        }

        if (!StringUtils.endsWith(parentDir, File.separator)) {
            parentDir = parentDir + File.separator;
        }

        packageName = packageName.replaceAll("\\.", "\\" + File.separator);
        return parentDir + packageName;
    }
}
