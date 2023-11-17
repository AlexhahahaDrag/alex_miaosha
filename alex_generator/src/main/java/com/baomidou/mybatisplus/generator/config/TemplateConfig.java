//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.baomidou.mybatisplus.generator.config;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateConfig.class);
    private String entity;
    private String entityKt;
    private String controller;
    private String mapper;
    private String xml;
    private String service;
    private String serviceImpl;
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
    
    private boolean disableEntity;

    private TemplateConfig() {
        this.entity = "/templates/entity.java";
        this.entityKt = "/templates/entity.kt";
        this.controller = "/templates/controller.java";
        this.mapper = "/templates/mapper.java";
        this.xml = "/templates/mapper.xml";
        this.service = "/templates/service.java";
        this.serviceImpl = "/templates/serviceImpl.java";
        this.vo = "/templates/vo.java";
        this.client = "/templates/feignClient.java";
        this.detailTs = "/templates/detail.ts";
        this.detailVue = "/templates/detail.vue";
        this.listTs = "/templates/list.ts";
        this.listVue = "/templates/list.vue";
        this.tsTs = "/templates/ts.ts";
        this.mobileTs = "/templates/mobile/mobile.ts";
        this.mobileVue = "/templates/mobile/mobile.vue";
        this.mobileDetail = "/templates/mobile/mobile.detail";
        this.mobileDetailTs = "/templates/mobile/mobile.detail.ts";
    }

    private void logger(String value, TemplateType templateType) {
        if (StringUtils.isBlank(value)) {
            LOGGER.warn("推荐使用disable(TemplateType.{})方法进行默认模板禁用.", templateType.name());
        }

    }

    public String getEntity(boolean kotlin) {
        if (!this.disableEntity) {
            if (kotlin) {
                return StringUtils.isBlank(this.entityKt) ? "/templates/entity.kt" : this.entityKt;
            } else {
                return StringUtils.isBlank(this.entity) ? "/templates/entity.java" : this.entity;
            }
        } else {
            return null;
        }
    }

    public TemplateConfig disable(@NotNull TemplateType... templateTypes) {
        if (templateTypes != null && templateTypes.length > 0) {
            TemplateType[] var2 = templateTypes;
            int var3 = templateTypes.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                TemplateType templateType = var2[var4];
                switch(templateType) {
                    case ENTITY:
                        this.entity = null;
                        this.entityKt = null;
                        this.disableEntity = true;
                        break;
                    case CONTROLLER:
                        this.controller = null;
                        break;
                    case MAPPER:
                        this.mapper = null;
                        break;
                    case XML:
                        this.xml = null;
                        break;
                    case SERVICE:
                        this.service = null;
                        break;
                    case SERVICEIMPL:
                        this.serviceImpl = null;
                    case VO:
                        this.vo = null;
                    case CLIENT:
                        this.client = null;
                    case DETAILVUE:
                        this.detailVue = null;
                    case DETAILTS:
                        this.detailTs = null;
                    case LISTVUE:
                        this.listVue = null;
                    case LISTTS:
                        this.listTs = null;
                    case TSTS:
                        this.tsTs = null;
                    case MOBILETS:
                        this.mobileTs = null;
                    case MOBILEVUE:
                        this.mobileVue = null;
                    case MOBILEDETAILTS:
                        this.mobileDetailTs = null;
                    case MOBILEDETAIL:
                        this.mobileDetail = null;
                }
            }
        }
        return this;
    }

    public TemplateConfig disable() {
        return this.disable(TemplateType.values());
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

    public String getVo() {
        return vo;
    }

    public String getClient() {
        return client;
    }

    public String getDetailVue() {
        return detailVue;
    }

    public String getDetailTs() {
        return detailTs;
    }

    public String getListTs() {
        return listTs;
    }

    public String getListVue() {
        return listVue;
    }

    public String getTsTs() {
        return tsTs;
    }

    public String getMobileTs() {
        return mobileTs;
    }

    public String getMobileVue() {
        return mobileVue;
    }

    public String getMobileDetailTs() {
        return mobileDetailTs;
    }

    public String getMobileDetail() {
        return mobileDetail;
    }

    public static class Builder implements IConfigBuilder<TemplateConfig> {
        private final TemplateConfig templateConfig = new TemplateConfig();

        public Builder() {
        }

        public Builder disable() {
            this.templateConfig.disable();
            return this;
        }

        public Builder disable(@NotNull TemplateType... templateTypes) {
            this.templateConfig.disable(templateTypes);
            return this;
        }

        public Builder entity(@NotNull String entityTemplate) {
            this.templateConfig.disableEntity = false;
            this.templateConfig.entity = entityTemplate;
            return this;
        }

        public Builder entityKt(@NotNull String entityKtTemplate) {
            this.templateConfig.disableEntity = false;
            this.templateConfig.entityKt = entityKtTemplate;
            return this;
        }

        public Builder service(@NotNull String serviceTemplate) {
            this.templateConfig.service = serviceTemplate;
            return this;
        }

        public Builder serviceImpl(@NotNull String serviceImplTemplate) {
            this.templateConfig.serviceImpl = serviceImplTemplate;
            return this;
        }

        public Builder mapper(@NotNull String mapperTemplate) {
            this.templateConfig.mapper = mapperTemplate;
            return this;
        }

        public Builder mapperXml(@NotNull String mapperXmlTemplate) {
            this.templateConfig.xml = mapperXmlTemplate;
            return this;
        }

        public Builder controller(@NotNull String controllerTemplate) {
            this.templateConfig.controller = controllerTemplate;
            return this;
        }

        public Builder vo(@NotNull String voTemplate) {
            this.templateConfig.vo = voTemplate;
            return this;
        }

        public Builder client(@NotNull String clientTemplate) {
            this.templateConfig.client = clientTemplate;
            return this;
        }

        public Builder detailVue(@NotNull String detailVueTemplate) {
            this.templateConfig.detailVue = detailVueTemplate;
            return this;
        }

        public Builder detailTs(@NotNull String detailTsTemplate) {
            this.templateConfig.detailTs = detailTsTemplate;
            return this;
        }

        public Builder listVue(@NotNull String listVueTemplate) {
            this.templateConfig.listVue = listVueTemplate;
            return this;
        }

        public Builder listTs(@NotNull String listTsTemplate) {
            this.templateConfig.listTs = listTsTemplate;
            return this;
        }

        public Builder tsTs(@NotNull String tsTsTemplate) {
            this.templateConfig.tsTs = tsTsTemplate;
            return this;
        }

        public Builder mobileTs(@NotNull String mobileTsTemplate) {
            this.templateConfig.mobileTs = mobileTsTemplate;
            return this;
        }

        public Builder mobileVue(@NotNull String mobileVueTemplate) {
            this.templateConfig.mobileVue = mobileVueTemplate;
            return this;
        }

        public Builder mobileDetailTs(@NotNull String mobileDetailTsTemplate) {
            this.templateConfig.mobileDetailTs = mobileDetailTsTemplate;
            return this;
        }

        public Builder mobileDetail(@NotNull String mobileDetailTemplate) {
            this.templateConfig.mobileDetail = mobileDetailTemplate;
            return this;
        }

        public TemplateConfig build() {
            return this.templateConfig;
        }
    }
}
