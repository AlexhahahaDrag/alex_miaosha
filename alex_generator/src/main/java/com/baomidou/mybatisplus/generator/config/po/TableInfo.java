//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.baomidou.mybatisplus.generator.config.po;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.builder.*;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class TableInfo {
    private final StrategyConfig strategyConfig;
    private final GlobalConfig globalConfig;
    private final Set<String> importPackages = new TreeSet();
    private final Set<String> importVoPackages = new TreeSet();
    private boolean convert;
    private String name;
    private String comment;
    private String entityName;
    private String mapperName;
    private String xmlName;
    private String serviceName;
    private String serviceImplName;
    private String controllerName;
    private final List<TableField> fields = new ArrayList();
    private boolean havePrimaryKey;
    private final List<TableField> commonFields = new ArrayList();
    private String fieldNames;
    private String voName;
    private String clientName;
    private String detailVueName;
    private String detailTsName;
    private String listVueName;
    private String listTsName;
    private String tsTsName;
    private final Entity entity;
    private final Vo vo;
    private final DetailTs detailTs;
    private final DetailVue detailVue;
    private final ListTs listTs;
    private final ListVue listVue;
    private final TsTs tsTs;
    private Boolean vueGenerator;
    private Boolean feignGenerator;

    public TableInfo(@NotNull ConfigBuilder configBuilder, @NotNull String name) {
        this.strategyConfig = configBuilder.getStrategyConfig();
        this.globalConfig = configBuilder.getGlobalConfig();
        this.entity = configBuilder.getStrategyConfig().entity();
        this.vo = configBuilder.getStrategyConfig().vo();
        this.detailVue = configBuilder.getStrategyConfig().detailVue();
        this.detailTs = configBuilder.getStrategyConfig().detailTs();
        this.listVue = configBuilder.getStrategyConfig().listVue();
        this.listTs = configBuilder.getStrategyConfig().listTs();
        this.tsTs = configBuilder.getStrategyConfig().tsTs();
        this.vueGenerator = configBuilder.getGlobalConfig().isVueGenerator();
        this.feignGenerator = configBuilder.getGlobalConfig().isFeignGenerator();
        this.name = name;
    }

    protected TableInfo setConvert() {
        if (!this.strategyConfig.startsWithTablePrefix(this.name) && !this.entity.isTableFieldAnnotationEnable()) {
            this.convert = !this.entityName.equalsIgnoreCase(this.name);
        } else {
            this.convert = true;
        }

        return this;
    }

    public String getEntityPath() {
        return this.entityName.substring(0, 1).toLowerCase() + this.entityName.substring(1);
    }

    public TableInfo setEntityName(@NotNull String entityName) {
        this.entityName = entityName;
        this.setConvert();
        return this;
    }

    public void addField(@NotNull TableField field) {
        if (!this.entity.matchIgnoreColumns(field.getColumnName())) {
            if (this.entity.matchSuperEntityColumns(field.getColumnName())) {
                this.commonFields.add(field);
            } else {
                this.fields.add(field);
            }

        }
    }

    public TableInfo addImportPackages(@NotNull String... pkgs) {
        List<String> arr = Arrays.asList(pkgs);
        this.importVoPackages.addAll(arr);
        this.importPackages.addAll(arr);
        return this;
    }

    public String getFieldNames() {
        if (StringUtils.isBlank(this.fieldNames)) {
            this.fieldNames = (String)this.fields.stream().map(item -> name + "." + item.getColumnName()).collect(Collectors.joining(", "));
        }
        return this.fieldNames;
    }

    public void importPackage() {
        String superEntity = this.entity.getSuperClass();
        if (StringUtils.isNotBlank(superEntity)) {
            this.importPackages.add(superEntity);
        } else if (this.entity.isActiveRecord()) {
            this.importPackages.add(Model.class.getCanonicalName());
        }

        if (this.entity.isSerialVersionUID()) {
            this.importPackages.add(Serializable.class.getCanonicalName());
        }

        if (this.isConvert()) {
            this.importPackages.add(TableName.class.getCanonicalName());
        }

        IdType idType = this.entity.getIdType();
        if (null != idType && this.isHavePrimaryKey()) {
            this.importPackages.add(IdType.class.getCanonicalName());
            this.importPackages.add(TableId.class.getCanonicalName());
        }

        this.fields.forEach((field) -> {
            IColumnType columnType = field.getColumnType();
            if (null != columnType && null != columnType.getPkg()) {
                this.importPackages.add(columnType.getPkg());
            }

            if (field.isKeyFlag()) {
                if (field.isConvert() || field.isKeyIdentityFlag()) {
                    this.importPackages.add(TableId.class.getCanonicalName());
                }

                if (field.isKeyIdentityFlag()) {
                    this.importPackages.add(IdType.class.getCanonicalName());
                }
            } else if (field.isConvert()) {
                this.importPackages.add(com.baomidou.mybatisplus.annotation.TableField.class.getCanonicalName());
            }

            if (null != field.getFill()) {
                this.importPackages.add(com.baomidou.mybatisplus.annotation.TableField.class.getCanonicalName());
                this.importPackages.add(FieldFill.class.getCanonicalName());
            }

            if (field.isVersionField()) {
                this.importPackages.add(Version.class.getCanonicalName());
            }

            if (field.isLogicDeleteField()) {
                this.importPackages.add(TableLogic.class.getCanonicalName());
            }

        });
    }

    public void importVoPackage() {
        String superVoClass = this.vo.getSuperVoClass();
        if (StringUtils.isNotBlank(superVoClass)) {
            this.importVoPackages.add(superVoClass);
        } else if (this.entity.isActiveRecord()) {
            this.importVoPackages.add(Model.class.getCanonicalName());
        }

        if (this.entity.isSerialVersionUID()) {
            this.importVoPackages.add(Serializable.class.getCanonicalName());
        }

        IdType idType = this.entity.getIdType();
        if (null != idType && this.isHavePrimaryKey()) {
            this.importVoPackages.add(IdType.class.getCanonicalName());
            this.importVoPackages.add(TableId.class.getCanonicalName());
        }

        this.fields.forEach((field) -> {
            IColumnType columnType = field.getColumnType();
            if (null != columnType && null != columnType.getPkg()) {
                this.importVoPackages.add(columnType.getPkg());
            }

            if (field.isKeyFlag()) {
                if (field.isConvert() || field.isKeyIdentityFlag()) {
                    this.importVoPackages.add(TableId.class.getCanonicalName());
                }

                if (field.isKeyIdentityFlag()) {
                    this.importVoPackages.add(IdType.class.getCanonicalName());
                }
            } else if (field.isConvert()) {
                this.importVoPackages.add(com.baomidou.mybatisplus.annotation.TableField.class.getCanonicalName());
            }

            if (null != field.getFill()) {
                this.importVoPackages.add(com.baomidou.mybatisplus.annotation.TableField.class.getCanonicalName());
                this.importVoPackages.add(FieldFill.class.getCanonicalName());
            }

            if (field.isVersionField()) {
                this.importVoPackages.add(Version.class.getCanonicalName());
            }
        });
    }

    public void processTable() {
        String entityName = this.entity.getNameConvert().entityNameConvert(this);
        this.setEntityName(this.entity.getConverterFileName().convert(entityName));
        this.mapperName = this.strategyConfig.mapper().getConverterMapperFileName().convert(entityName);
        this.xmlName = this.strategyConfig.mapper().getConverterXmlFileName().convert(entityName);
        this.serviceName = this.strategyConfig.service().getConverterServiceFileName().convert(entityName);
        this.serviceImplName = this.strategyConfig.service().getConverterServiceImplFileName().convert(entityName);
        this.controllerName = this.strategyConfig.controller().getConverterFileName().convert(entityName);
        this.voName = this.strategyConfig.vo().getConverterFileName().convert(entityName);
        this.clientName = this.strategyConfig.client().getConverterFileName().convert(entityName);
        this.detailVueName = this.strategyConfig.detailVue().getConverterFileName().convert(entityName);
        this.detailTsName = this.strategyConfig.detailTs().getConverterFileName().convert(entityName);
        this.listVueName = this.strategyConfig.listVue().getConverterFileName().convert(entityName);
        this.listTsName = this.strategyConfig.listTs().getConverterFileName().convert(entityName);
        this.tsTsName = this.strategyConfig.tsTs().getConverterFileName().convert(entityName);
        this.importPackage();
        this.importVoPackage();
    }

    public TableInfo setComment(String comment) {
        this.comment = this.globalConfig.isSwagger() && StringUtils.isNotBlank(comment) ? comment.replace("\"", "\\\"") : comment;
        return this;
    }

    public TableInfo setHavePrimaryKey(boolean havePrimaryKey) {
        this.havePrimaryKey = havePrimaryKey;
        return this;
    }

    @NotNull
    public Set<String> getImportPackages() {
        return this.importPackages;
    }

    @NotNull
    public Set<String> getImportVoPackages() {
        return this.importVoPackages;
    }

    public boolean isConvert() {
        return this.convert;
    }

    public TableInfo setConvert(boolean convert) {
        this.convert = convert;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public String getComment() {
        return this.comment;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public String getMapperName() {
        return this.mapperName;
    }

    public String getXmlName() {
        return this.xmlName;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getServiceImplName() {
        return this.serviceImplName;
    }

    public String getControllerName() {
        return this.controllerName;
    }

    @NotNull
    public List<TableField> getFields() {
        return this.fields;
    }

    public boolean isHavePrimaryKey() {
        return this.havePrimaryKey;
    }

    @NotNull
    public List<TableField> getCommonFields() {
        return this.commonFields;
    }

    public String getVoName() {
        return voName;
    }

    public String getClientName() {
        return clientName;
    }

    public String getDetailVueName() {
        return detailVueName;
    }

    public void setDetailVueName(String detailVueName) {
        this.detailVueName = detailVueName;
    }

    public String getDetailTsName() {
        return detailTsName;
    }

    public void setDetailTsName(String detailTsName) {
        this.detailTsName = detailTsName;
    }

    public String getListVueName() {
        return listVueName;
    }

    public String getListTsName() {
        return listTsName;
    }

    public void setListTsName(String listTsName) {
        this.listTsName = listTsName;
    }

    public void setListVueName(String listVueName) {
        this.listVueName = listVueName;
    }

    public String getTsTsName() {
        return tsTsName;
    }

    public void setTsTsName(String tsTsName) {
        this.tsTsName = tsTsName;
    }

    public Boolean getVueGenerator() {
        return vueGenerator;
    }

    public void setVueGenerator(Boolean vueGenerator) {
        this.vueGenerator = vueGenerator;
    }

    public Boolean getFeignGenerator() {
        return feignGenerator;
    }

    public void setFeignGenerator(Boolean feignGenerator) {
        this.feignGenerator = feignGenerator;
    }
}
