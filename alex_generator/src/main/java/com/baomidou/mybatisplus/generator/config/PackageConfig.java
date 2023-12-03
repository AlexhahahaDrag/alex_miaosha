//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.baomidou.mybatisplus.generator.config;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PackageConfig {
    private String parent;
    private String moduleName;
    private String entity;
    private String service;
    private String serviceImpl;
    private String mapper;
    private String xml;
    private String controller;
    private String other;
    private String vo;
    private String client;
    private String detailVue;
    private String detailTs;
    private String listTs;
    private String listVue;
    private String tsTs;

    private String mobileTs;

    private String mobileVue;

    private String mobileDetailTs;

    private String mobileDetail;

    private String mobileTsTs;

    private Map<OutputFile, String> pathInfo;
    private final Map<String, String> packageInfo;

    private PackageConfig() {
        this.parent = "com.baomidou";
        this.moduleName = "";
        this.entity = "entity";
        this.service = "service";
        this.serviceImpl = "service.impl";
        this.mapper = "mapper";
        this.xml = "mapper.xml";
        this.controller = "controller";
        this.vo = "vo";
        this.client = "client";
        this.detailVue = "vue.detail";
        this.detailTs = "vue.detail";
        this.listTs = "vue";
        this.listVue = "vue";
        this.tsTs = "ts";
        this.other = "other";
        this.mobileTs = "mobileTs";
        this.mobileVue = "mobileVue";
        this.mobileDetailTs = "mobileDetailTs";
        this.mobileDetail = "mobileDetail";
        this.mobileTsTs = "mobileTsTs";
        this.packageInfo = new HashMap();
    }

    @NotNull
    public String getParent() {
        return StringUtils.isNotBlank(this.moduleName) ? this.parent + "." + this.moduleName : this.parent;
    }

    @NotNull
    public String joinPackage(String subPackage) {
        String parent = this.getParent();
        return StringUtils.isBlank(parent) ? subPackage : parent + "." + subPackage;
    }

    @NotNull
    public Map<String, String> getPackageInfo() {
        if (this.packageInfo.isEmpty()) {
            this.packageInfo.put("ModuleName", this.getModuleName());
            this.packageInfo.put("Entity", this.joinPackage(this.getEntity()));
            this.packageInfo.put("Mapper", this.joinPackage(this.getMapper()));
            this.packageInfo.put("Xml", this.joinPackage(this.getXml()));
            this.packageInfo.put("Service", this.joinPackage(this.getService()));
            this.packageInfo.put("ServiceImpl", this.joinPackage(this.getServiceImpl()));
            this.packageInfo.put("Controller", this.joinPackage(this.getController()));
            this.packageInfo.put("Vo", this.joinPackage(this.getVo()));
            this.packageInfo.put("Client", this.joinPackage(this.getClient()));
            this.packageInfo.put("DetailVue", this.joinPackage(this.getDetailVue()));
            this.packageInfo.put("DetailTs", this.joinPackage(this.getDetailTs()));
            this.packageInfo.put("ListTs", this.joinPackage(this.getListTs()));
            this.packageInfo.put("ListVue", this.joinPackage(this.getListVue()));
            this.packageInfo.put("TsTs", this.joinPackage(this.getTsTs()));
            this.packageInfo.put("Other", this.joinPackage(this.getOther()));
            this.packageInfo.put("MobileTs", this.joinPackage(this.getMobileTs()));
            this.packageInfo.put("MobileVue", this.joinPackage(this.getMobileVue()));
            this.packageInfo.put("MobileDetailTs", this.joinPackage(this.getMobileDetailTs()));
            this.packageInfo.put("MobileDetail", this.joinPackage(this.getMobileDetail()));
            this.packageInfo.put("MobileTsTs", this.joinPackage(this.getMobileTsTs()));
            this.packageInfo.put("Parent", this.getParent());
        }
        return Collections.unmodifiableMap(this.packageInfo);
    }

    public String getPackageInfo(String module) {
        return (String)this.getPackageInfo().get(module);
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public String getEntity() {
        return this.entity;
    }

    public String getService() {
        return this.service;
    }

    public String getServiceImpl() {
        return this.serviceImpl;
    }

    public String getMapper() {
        return this.mapper;
    }

    public String getXml() {
        return this.xml;
    }

    public String getController() {
        return this.controller;
    }

    public String getOther() {
        return this.other;
    }

    public String getVo() {
        return this.vo;
    }

    public String getClient() {
        return this.client;
    }

    public String getDetailVue() {
        return this.detailVue;
    }

    public String getDetailTs() {
        return this.detailTs;
    }

    public String getListTs() {
        return this.listTs;
    }

    public String getListVue() {
        return this.listVue;
    }

    public String getTsTs() {
        return this.tsTs;
    }

    private String getMobileTs() {
        return this.mobileTs;
    }

    private String getMobileVue() {
        return this.mobileVue;
    }

    private String getMobileDetailTs() {
        return this.mobileDetailTs;
    }

    private String getMobileDetail() {
        return this.mobileDetail;
    }

    public String getMobileTsTs() {
        return mobileTsTs;
    }

    public void setMobileTsTs(String mobileTsTs) {
        this.mobileTsTs = mobileTsTs;
    }

    public Map<OutputFile, String> getPathInfo() {
        return this.pathInfo;
    }

    public static class Builder implements IConfigBuilder<PackageConfig> {
        private final PackageConfig packageConfig;

        public Builder() {
            this.packageConfig = new PackageConfig();
        }

        public Builder(@NotNull String parent, @NotNull String moduleName) {
            this();
            this.packageConfig.parent = parent;
            this.packageConfig.moduleName = moduleName;
        }

        public Builder parent(@NotNull String parent) {
            this.packageConfig.parent = parent;
            return this;
        }

        public Builder moduleName(@NotNull String moduleName) {
            this.packageConfig.moduleName = moduleName;
            return this;
        }

        public Builder entity(@NotNull String entity) {
            this.packageConfig.entity = entity;
            return this;
        }

        public Builder service(@NotNull String service) {
            this.packageConfig.service = service;
            return this;
        }

        public Builder serviceImpl(@NotNull String serviceImpl) {
            this.packageConfig.serviceImpl = serviceImpl;
            return this;
        }

        public Builder mapper(@NotNull String mapper) {
            this.packageConfig.mapper = mapper;
            return this;
        }

        public Builder xml(@NotNull String xml) {
            this.packageConfig.xml = xml;
            return this;
        }

        public Builder controller(@NotNull String controller) {
            this.packageConfig.controller = controller;
            return this;
        }

        public Builder vo(@NotNull String vo) {
            this.packageConfig.vo = vo;
            return this;
        }

        public Builder client(@NotNull String client) {
            this.packageConfig.client = client;
            return this;
        }

        public Builder detailVue(@NotNull String detailVue) {
            this.packageConfig.detailVue = detailVue;
            return this;
        }

        public Builder detailTs(@NotNull String detailTs) {
            this.packageConfig.detailTs = detailTs;
            return this;
        }

        public Builder listTs(@NotNull String listTs) {
            this.packageConfig.listTs = listTs;
            return this;
        }

        public Builder listVue(@NotNull String listVue) {
            this.packageConfig.listVue = listVue;
            return this;
        }

        public Builder tsTs(@NotNull String tsTs) {
            this.packageConfig.tsTs = tsTs;
            return this;
        }

        public Builder mobileTs(@NotNull String mobileTs) {
            this.packageConfig.mobileTs = mobileTs;
            return this;
        }

        public Builder mobileVue(@NotNull String mobileVue) {
            this.packageConfig.mobileVue = mobileVue;
            return this;
        }

        public Builder mobileDetailTs(@NotNull String mobileDetailTs) {
            this.packageConfig.mobileDetailTs = mobileDetailTs;
            return this;
        }

        public Builder mobileDetail(@NotNull String mobileDetail) {
            this.packageConfig.mobileDetail = mobileDetail;
            return this;
        }

        public Builder mobileTsTs(@NotNull String mobileTsTs) {
            this.packageConfig.mobileTsTs = mobileTsTs;
            return this;
        }

        public Builder other(@NotNull String other) {
            this.packageConfig.other = other;
            return this;
        }

        public Builder pathInfo(@NotNull Map<OutputFile, String> pathInfo) {
            this.packageConfig.pathInfo = pathInfo;
            return this;
        }

        @NotNull
        public String joinPackage(@NotNull String subPackage) {
            return this.packageConfig.joinPackage(subPackage);
        }

        public PackageConfig build() {
            return this.packageConfig;
        }
    }
}
