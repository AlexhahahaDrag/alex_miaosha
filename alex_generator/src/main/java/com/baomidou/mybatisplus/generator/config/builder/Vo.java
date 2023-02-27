//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.baomidou.mybatisplus.generator.config.builder;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.IFill;
import com.baomidou.mybatisplus.generator.ITemplate;
import com.baomidou.mybatisplus.generator.config.INameConvert;
import com.baomidou.mybatisplus.generator.config.INameConvert.DefaultNameConvert;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.function.ConverterFileName;
import com.baomidou.mybatisplus.generator.util.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class Vo implements ITemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(Vo.class);
    private INameConvert nameConvert;
    private String superVoClass;
    private final Set<String> superVoColumns;
    private final Set<String> ignoreColumns;
    private boolean serialVersionUID;
    private boolean columnConstant;
    private boolean chain;
    private boolean lombok;
    private boolean booleanColumnRemoveIsPrefix;
    private boolean tableFieldAnnotationEnable;
    private String versionColumnName;
    private String versionPropertyName;
    private String logicDeleteColumnName;
    private String logicDeletePropertyName;
    private final List<IFill> tableFillList;
    private NamingStrategy naming;
    private NamingStrategy columnNaming;
    private boolean activeRecord;
    private IdType idType;
    private ConverterFileName converterFileName;

    private Vo() {
        this.superVoColumns = new HashSet();
        this.ignoreColumns = new HashSet();
        this.serialVersionUID = true;
        this.tableFillList = new ArrayList();
        this.naming = NamingStrategy.underline_to_camel;
        this.columnNaming = null;
        this.converterFileName = (voName) -> {
            return voName;
        };
    }

    public void convertSuperVoColumns(Object clazz) {
        Class clazz1 = (Class) clazz;
        List<Field> fields = TableInfoHelper.getAllFields(clazz1);
        this.superVoColumns.addAll(fields.stream().map((field) -> {
            TableId tableId = field.getAnnotation(TableId.class);
            if (tableId != null && StringUtils.isNotBlank(tableId.value())) {
                return tableId.value();
            } else {
                TableField tableField = field.getAnnotation(TableField.class);
                if (tableField != null && StringUtils.isNotBlank(tableField.value())) {
                    return tableField.value();
                } else {
                    return null != this.columnNaming && this.columnNaming != NamingStrategy.no_change ? StringUtils.camelToUnderline(field.getName()) : field.getName();
                }
            }
        }).collect(Collectors.toSet()));
    }

    @NotNull
    public NamingStrategy getColumnNaming() {
        return (NamingStrategy)Optional.ofNullable(this.columnNaming).orElse(this.naming);
    }

    public boolean matchSuperVoColumns(String fieldName) {
        return this.superVoColumns.stream().anyMatch((e) -> {
            return e.equalsIgnoreCase(fieldName);
        });
    }

    public boolean matchIgnoreColumns(String fieldName) {
        return this.ignoreColumns.stream().anyMatch((e) -> {
            return e.equalsIgnoreCase(fieldName);
        });
    }

    @NotNull
    public INameConvert getNameConvert() {
        return this.nameConvert;
    }

    @Nullable
    public String getSuperVoClass() {
        return this.superVoClass;
    }

    public Set<String> getSuperVoColumns() {
        return this.superVoColumns;
    }

    public boolean isSerialVersionUID() {
        return this.serialVersionUID;
    }

    public boolean isColumnConstant() {
        return this.columnConstant;
    }

    public boolean isChain() {
        return this.chain;
    }

    public boolean isLombok() {
        return this.lombok;
    }

    public boolean isBooleanColumnRemoveIsPrefix() {
        return this.booleanColumnRemoveIsPrefix;
    }

    public boolean isTableFieldAnnotationEnable() {
        return this.tableFieldAnnotationEnable;
    }

    @Nullable
    public String getVersionColumnName() {
        return this.versionColumnName;
    }

    @Nullable
    public String getVersionPropertyName() {
        return this.versionPropertyName;
    }

    @Nullable
    public String getLogicDeleteColumnName() {
        return this.logicDeleteColumnName;
    }

    @Nullable
    public String getLogicDeletePropertyName() {
        return this.logicDeletePropertyName;
    }

    @NotNull
    public List<IFill> getTableFillList() {
        return this.tableFillList;
    }

    @NotNull
    public NamingStrategy getNaming() {
        return this.naming;
    }

    public boolean isActiveRecord() {
        return this.activeRecord;
    }

    @Nullable
    public IdType getIdType() {
        return this.idType;
    }

    @NotNull
    public ConverterFileName getConverterFileName() {
        return this.converterFileName;
    }

    @NotNull
    public Map<String, Object> renderData(@NotNull TableInfo tableInfo) {
        Map<String, Object> data = new HashMap();
        data.put("idType", this.idType == null ? null : this.idType.toString());
        data.put("logicDeleteFieldName", this.logicDeleteColumnName);
        data.put("versionFieldName", this.versionColumnName);
        data.put("activeRecord", this.activeRecord);
        data.put("voSerialVersionUID", this.serialVersionUID);
        data.put("voColumnConstant", this.columnConstant);
        data.put("voBuilderModel", this.chain);
        data.put("chainModel", this.chain);
        data.put("voLombokModel", this.lombok);
        data.put("voBooleanColumnRemoveIsPrefix", this.booleanColumnRemoveIsPrefix);
        data.put("superVoClass", ClassUtils.getSimpleName(this.superVoClass));
        return data;
    }

    public static class Builder extends BaseBuilder {
        private final Vo vo = new Vo();

        public Builder(StrategyConfig strategyConfig) {
            super(strategyConfig);
            this.vo.nameConvert = new DefaultNameConvert(strategyConfig);
        }

        public Builder nameConvert(INameConvert nameConvert) {
            this.vo.nameConvert = nameConvert;
            return this;
        }

        public Builder superVoClass(@NotNull Class<?> clazz) {
            return this.superVoClass(clazz.getName());
        }

        public Builder superVoClass(String superVoClass) {
            this.vo.superVoClass = superVoClass;
            return this;
        }

        public Builder disableSerialVersionUID() {
            this.vo.serialVersionUID = false;
            return this;
        }

        public Builder enableColumnConstant() {
            this.vo.columnConstant = true;
            return this;
        }

        public Builder enableChainModel() {
            this.vo.chain = true;
            return this;
        }

        public Builder enableLombok() {
            this.vo.lombok = true;
            return this;
        }

        public Builder enableRemoveIsPrefix() {
            this.vo.booleanColumnRemoveIsPrefix = true;
            return this;
        }

        public Builder enableTableFieldAnnotation() {
            this.vo.tableFieldAnnotationEnable = true;
            return this;
        }

        public Builder enableActiveRecord() {
            this.vo.activeRecord = true;
            return this;
        }

        public Builder versionColumnName(String versionColumnName) {
            this.vo.versionColumnName = versionColumnName;
            return this;
        }

        public Builder versionPropertyName(String versionPropertyName) {
            this.vo.versionPropertyName = versionPropertyName;
            return this;
        }

        public Builder logicDeleteColumnName(String logicDeleteColumnName) {
            this.vo.logicDeleteColumnName = logicDeleteColumnName;
            return this;
        }

        public Builder logicDeletePropertyName(String logicDeletePropertyName) {
            this.vo.logicDeletePropertyName = logicDeletePropertyName;
            return this;
        }

        public Builder naming(NamingStrategy namingStrategy) {
            this.vo.naming = namingStrategy;
            return this;
        }

        public Builder columnNaming(NamingStrategy namingStrategy) {
            this.vo.columnNaming = namingStrategy;
            return this;
        }

        public Builder addSuperVoColumns(@NotNull String... superVoColumns) {
            this.vo.superVoColumns.addAll(Arrays.asList(superVoColumns));
            return this;
        }

        public Builder addIgnoreColumns(@NotNull String... ignoreColumns) {
            this.vo.ignoreColumns.addAll(Arrays.asList(ignoreColumns));
            return this;
        }

        public Builder addTableFills(@NotNull IFill... tableFill) {
            this.vo.tableFillList.addAll(Arrays.asList(tableFill));
            return this;
        }

        public Builder addTableFills(@NotNull List<IFill> tableFillList) {
            this.vo.tableFillList.addAll(tableFillList);
            return this;
        }

        public Builder idType(IdType idType) {
            this.vo.idType = idType;
            return this;
        }

        public Builder convertFileName(@NotNull ConverterFileName converter) {
            this.vo.converterFileName = converter;
            return this;
        }

        public Builder formatVoFileName(String format) {
            return this.convertFileName((voName) -> {
                return String.format(format, voName);
            });
        }

        public Vo get() {
            String superVoClass = this.vo.superVoClass;
            if (StringUtils.isNotBlank(superVoClass)) {
                Optional var10000 = this.tryLoadClass(superVoClass);
                Vo var10001 = this.vo;
                var10000.ifPresent(var10001::convertSuperVoColumns);
            } else if (!this.vo.superVoColumns.isEmpty()) {
                Vo.LOGGER.warn("Forgot to set Vo supper class ?");
            }
            return this.vo;
        }

        private Optional<Class<?>> tryLoadClass(String className) {
            try {
                return Optional.of(ClassUtils.toClassConfident(className));
            } catch (Exception var3) {
                return Optional.empty();
            }
        }
    }
}
