//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.baomidou.mybatisplus.generator.config;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.config.builder.*;
import com.baomidou.mybatisplus.generator.config.po.LikeTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class StrategyConfig {
    private boolean isCapitalMode;
    private boolean skipView;
    private final Set<String> tablePrefix;
    private final Set<String> tableSuffix;
    private final Set<String> fieldPrefix;
    private final Set<String> fieldSuffix;
    private final Set<String> include;
    private final Set<String> exclude;
    private boolean enableSqlFilter;
    private boolean enableSchema;
    private LikeTable likeTable;
    private LikeTable notLikeTable;
    private final com.baomidou.mybatisplus.generator.config.builder.Entity.Builder entityBuilder;
    private final com.baomidou.mybatisplus.generator.config.builder.Controller.Builder controllerBuilder;
    private final com.baomidou.mybatisplus.generator.config.builder.Mapper.Builder mapperBuilder;
    private final com.baomidou.mybatisplus.generator.config.builder.Service.Builder serviceBuilder;
    private final com.baomidou.mybatisplus.generator.config.builder.Vo.Builder voBuilder;
    private final com.baomidou.mybatisplus.generator.config.builder.Client.Builder clientBuilder;
    private final com.baomidou.mybatisplus.generator.config.builder.DetailTs.Builder detailTsBuilder;
    private final com.baomidou.mybatisplus.generator.config.builder.DetailVue.Builder detailVueBuilder;
    private final com.baomidou.mybatisplus.generator.config.builder.ListTs.Builder listTsBuilder;
    private final com.baomidou.mybatisplus.generator.config.builder.ListVue.Builder listVueBuilder;
    private final com.baomidou.mybatisplus.generator.config.builder.TsTs.Builder tsTsBuilder;

    private final com.baomidou.mybatisplus.generator.config.builder.MobileTs.Builder mobileTsBuilder;

    private final com.baomidou.mybatisplus.generator.config.builder.MobileVue.Builder mobileVueBuilder;

    private final com.baomidou.mybatisplus.generator.config.builder.MobileDetailTs.Builder mobileDetailTsBuilder;

    private final com.baomidou.mybatisplus.generator.config.builder.MobileDetail.Builder mobileDetailBuilder;

    private final com.baomidou.mybatisplus.generator.config.builder.MobileTsTs.Builder mobileTsTsBuilder;

    private Entity entity;
    private Controller controller;
    private Mapper mapper;
    private Service service;
    private Vo vo;
    private Client client;
    private DetailVue detailVue;
    private DetailTs detailTs;
    private ListVue listVue;
    private ListTs listTs;
    private TsTs tsTs;

    private MobileTs mobileTs;

    private MobileVue mobileVue;

    private MobileDetailTs mobileDetailTs;

    private MobileTsTs mobileTsTs;

    private MobileDetail mobileDetail;

    private StrategyConfig() {
        this.tablePrefix = new HashSet();
        this.tableSuffix = new HashSet();
        this.fieldPrefix = new HashSet();
        this.fieldSuffix = new HashSet();
        this.include = new HashSet();
        this.exclude = new HashSet();
        this.enableSqlFilter = true;
        this.entityBuilder = new com.baomidou.mybatisplus.generator.config.builder.Entity.Builder(this);
        this.controllerBuilder = new com.baomidou.mybatisplus.generator.config.builder.Controller.Builder(this);
        this.mapperBuilder = new com.baomidou.mybatisplus.generator.config.builder.Mapper.Builder(this);
        this.serviceBuilder = new com.baomidou.mybatisplus.generator.config.builder.Service.Builder(this);
        this.voBuilder = new com.baomidou.mybatisplus.generator.config.builder.Vo.Builder(this);
        this.clientBuilder = new com.baomidou.mybatisplus.generator.config.builder.Client.Builder(this);
        this.detailTsBuilder = new com.baomidou.mybatisplus.generator.config.builder.DetailTs.Builder(this);
        this.detailVueBuilder = new com.baomidou.mybatisplus.generator.config.builder.DetailVue.Builder(this);
        this.listTsBuilder = new com.baomidou.mybatisplus.generator.config.builder.ListTs.Builder(this);
        this.listVueBuilder = new com.baomidou.mybatisplus.generator.config.builder.ListVue.Builder(this);
        this.tsTsBuilder = new com.baomidou.mybatisplus.generator.config.builder.TsTs.Builder(this);
        this.mobileTsBuilder = new com.baomidou.mybatisplus.generator.config.builder.MobileTs.Builder(this);
        this.mobileVueBuilder = new com.baomidou.mybatisplus.generator.config.builder.MobileVue.Builder(this);
        this.mobileDetailTsBuilder = new com.baomidou.mybatisplus.generator.config.builder.MobileDetailTs.Builder(this);
        this.mobileDetailBuilder = new com.baomidou.mybatisplus.generator.config.builder.MobileDetail.Builder(this);
        this.mobileTsTsBuilder = new com.baomidou.mybatisplus.generator.config.builder.MobileTsTs.Builder(this);
    }

    @NotNull
    public com.baomidou.mybatisplus.generator.config.builder.Entity.Builder entityBuilder() {
        return this.entityBuilder;
    }

    @NotNull
    public Entity entity() {
        if (this.entity == null) {
            this.entity = this.entityBuilder.get();
        }

        return this.entity;
    }

    @NotNull
    public com.baomidou.mybatisplus.generator.config.builder.Controller.Builder controllerBuilder() {
        return this.controllerBuilder;
    }

    @NotNull
    public Controller controller() {
        if (this.controller == null) {
            this.controller = this.controllerBuilder.get();
        }

        return this.controller;
    }

    @NotNull
    public com.baomidou.mybatisplus.generator.config.builder.Mapper.Builder mapperBuilder() {
        return this.mapperBuilder;
    }

    @NotNull
    public Mapper mapper() {
        if (this.mapper == null) {
            this.mapper = this.mapperBuilder.get();
        }

        return this.mapper;
    }

    @NotNull
    public com.baomidou.mybatisplus.generator.config.builder.Service.Builder serviceBuilder() {
        return this.serviceBuilder;
    }

    @NotNull
    public Service service() {
        if (this.service == null) {
            this.service = this.serviceBuilder.get();
        }

        return this.service;
    }

    @NotNull
    public com.baomidou.mybatisplus.generator.config.builder.Vo.Builder voBuilder() {
        return this.voBuilder;
    }

    @NotNull
    public Vo vo() {
        if (this.vo == null) {
            this.vo = this.voBuilder.get();
        }
        return this.vo;
    }

    @NotNull
    public com.baomidou.mybatisplus.generator.config.builder.Client.Builder clientBuilder() {
        return this.clientBuilder;
    }

    @NotNull
    public Client client() {
        if (this.client == null) {
            this.client = this.clientBuilder.get();
        }
        return this.client;
    }

    @NotNull
    public com.baomidou.mybatisplus.generator.config.builder.DetailTs.Builder detailTsBuilder() {
        return this.detailTsBuilder;
    }

    @NotNull
    public DetailTs detailTs() {
        if (this.detailTs == null) {
            this.detailTs = this.detailTsBuilder.get();
        }
        return this.detailTs;
    }

    @NotNull
    public com.baomidou.mybatisplus.generator.config.builder.DetailVue.Builder detailVueBuilder() {
        return this.detailVueBuilder;
    }

    @NotNull
    public DetailVue detailVue() {
        if (this.detailVue == null) {
            this.detailVue = this.detailVueBuilder.get();
        }
        return this.detailVue;
    }

    @NotNull
    public com.baomidou.mybatisplus.generator.config.builder.ListVue.Builder listVueBuilder() {
        return this.listVueBuilder;
    }

    @NotNull
    public ListVue listVue() {
        if (this.listVue == null) {
            this.listVue = this.listVueBuilder.get();
        }
        return this.listVue;
    }

    @NotNull
    public com.baomidou.mybatisplus.generator.config.builder.ListTs.Builder listTsBuilder() {
        return this.listTsBuilder;
    }

    @NotNull
    public ListTs listTs() {
        if (this.listTs == null) {
            this.listTs = this.listTsBuilder.get();
        }
        return this.listTs;
    }

    @NotNull
    public com.baomidou.mybatisplus.generator.config.builder.TsTs.Builder tsTsBuilder() {
        return this.tsTsBuilder;
    }

    @NotNull
    public TsTs tsTs() {
        if (this.tsTs == null) {
            this.tsTs = this.tsTsBuilder.get();
        }
        return this.tsTs;
    }

    @NotNull
    public com.baomidou.mybatisplus.generator.config.builder.MobileTs.Builder mobileTsBuilder() {
        return this.mobileTsBuilder;
    }

    @NotNull
    public MobileTs mobileTs() {
        if (this.mobileTs == null) {
            this.mobileTs = this.mobileTsBuilder.get();
        }
        return this.mobileTs;
    }

    @NotNull
    public com.baomidou.mybatisplus.generator.config.builder.MobileVue.Builder mobileVueBuilder() {
        return this.mobileVueBuilder;
    }

    @NotNull
    public MobileVue mobileVue() {
        if (this.mobileVue == null) {
            this.mobileVue = this.mobileVueBuilder.get();
        }
        return this.mobileVue;
    }

    @NotNull
    public com.baomidou.mybatisplus.generator.config.builder.MobileDetailTs.Builder mobileDetailTsBuilder() {
        return this.mobileDetailTsBuilder;
    }

    @NotNull
    public MobileDetailTs mobileDetailTs() {
        if (this.mobileDetailTs == null) {
            this.mobileDetailTs = this.mobileDetailTsBuilder.get();
        }
        return this.mobileDetailTs;
    }

    @NotNull
    public MobileTsTs mobileTsTs() {
        if (this.mobileTsTs == null) {
            this.mobileTsTs = this.mobileTsTsBuilder.get();
        }
        return this.mobileTsTs;
    }

    @NotNull
    public com.baomidou.mybatisplus.generator.config.builder.MobileDetail.Builder mobileDetailBuilder() {
        return this.mobileDetailBuilder;
    }

    @NotNull
    public MobileDetail mobileDetail() {
        if (this.mobileDetail == null) {
            this.mobileDetail = this.mobileDetailBuilder.get();
        }
        return this.mobileDetail;
    }

    public boolean isCapitalModeNaming(@NotNull String word) {
        return this.isCapitalMode && StringUtils.isCapitalMode(word);
    }

    public boolean startsWithTablePrefix(@NotNull String tableName) {
        Stream<String> var10000 = this.tablePrefix.stream();
        tableName.getClass();
        return var10000.anyMatch(t -> tableName.startsWith(t));
    }

    public void validate() {
        boolean isInclude = this.getInclude().size() > 0;
        boolean isExclude = this.getExclude().size() > 0;
        if (isInclude && isExclude) {
            throw new IllegalArgumentException("<strategy> 标签中 <include> 与 <exclude> 只能配置一项！");
        } else if (this.getNotLikeTable() != null && this.getLikeTable() != null) {
            throw new IllegalArgumentException("<strategy> 标签中 <likeTable> 与 <notLikeTable> 只能配置一项！");
        }
    }

    public boolean matchIncludeTable(@NotNull String tableName) {
        return this.matchTable(tableName, this.getInclude());
    }

    public boolean matchExcludeTable(@NotNull String tableName) {
        return this.matchTable(tableName, this.getExclude());
    }

    private boolean matchTable(@NotNull String tableName, @NotNull Set<String> matchTables) {
        return matchTables.stream().anyMatch((t) -> {
            return this.tableNameMatches(t, tableName);
        });
    }

    private boolean tableNameMatches(@NotNull String matchTableName, @NotNull String dbTableName) {
        return matchTableName.equalsIgnoreCase(dbTableName) || StringUtils.matches(matchTableName, dbTableName);
    }

    public boolean isCapitalMode() {
        return this.isCapitalMode;
    }

    public boolean isSkipView() {
        return this.skipView;
    }

    @NotNull
    public Set<String> getTablePrefix() {
        return this.tablePrefix;
    }

    @NotNull
    public Set<String> getTableSuffix() {
        return this.tableSuffix;
    }

    @NotNull
    public Set<String> getFieldPrefix() {
        return this.fieldPrefix;
    }

    @NotNull
    public Set<String> getFieldSuffix() {
        return this.fieldSuffix;
    }

    @NotNull
    public Set<String> getInclude() {
        return this.include;
    }

    @NotNull
    public Set<String> getExclude() {
        return this.exclude;
    }

    public boolean isEnableSqlFilter() {
        return this.enableSqlFilter;
    }

    public boolean isEnableSchema() {
        return this.enableSchema;
    }

    @Nullable
    public LikeTable getLikeTable() {
        return this.likeTable;
    }

    @Nullable
    public LikeTable getNotLikeTable() {
        return this.notLikeTable;
    }

    public static class Builder extends BaseBuilder {
        private final StrategyConfig strategyConfig = super.build();

        public Builder() {
            super(new StrategyConfig());
        }

        public Builder enableCapitalMode() {
            this.strategyConfig.isCapitalMode = true;
            return this;
        }

        public Builder enableSkipView() {
            this.strategyConfig.skipView = true;
            return this;
        }

        public Builder disableSqlFilter() {
            this.strategyConfig.enableSqlFilter = false;
            return this;
        }

        public Builder enableSchema() {
            this.strategyConfig.enableSchema = true;
            return this;
        }

        public Builder addTablePrefix(@NotNull String... tablePrefix) {
            this.strategyConfig.tablePrefix.addAll(Arrays.asList(tablePrefix));
            return this;
        }

        public Builder addTableSuffix(String... tableSuffix) {
            this.strategyConfig.tableSuffix.addAll(Arrays.asList(tableSuffix));
            return this;
        }

        public Builder addFieldPrefix(@NotNull String... fieldPrefix) {
            this.strategyConfig.fieldPrefix.addAll(Arrays.asList(fieldPrefix));
            return this;
        }

        public Builder addFieldSuffix(@NotNull String... fieldSuffix) {
            this.strategyConfig.fieldSuffix.addAll(Arrays.asList(fieldSuffix));
            return this;
        }

        public Builder addInclude(@NotNull String... include) {
            this.strategyConfig.include.addAll(Arrays.asList(include));
            return this;
        }

        public Builder addInclude(@NotNull List<String> includes) {
            this.strategyConfig.include.addAll(includes);
            return this;
        }

        public Builder addExclude(@NotNull String... exclude) {
            this.strategyConfig.exclude.addAll(Arrays.asList(exclude));
            return this;
        }

        public Builder likeTable(@NotNull LikeTable likeTable) {
            this.strategyConfig.likeTable = likeTable;
            return this;
        }

        public Builder notLikeTable(@NotNull LikeTable notLikeTable) {
            this.strategyConfig.notLikeTable = notLikeTable;
            return this;
        }

        @NotNull
        public StrategyConfig build() {
            this.strategyConfig.validate();
            return this.strategyConfig;
        }
    }
}
