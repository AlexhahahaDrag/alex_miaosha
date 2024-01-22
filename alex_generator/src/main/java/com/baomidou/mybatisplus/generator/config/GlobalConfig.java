package com.baomidou.mybatisplus.generator.config;

/**
 * description:
 * author:       majf
 * createDate:   2023/2/28 10:59
 * version:      1.0.0
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public class GlobalConfig {
    private String outputDir;
    /** @deprecated */
    @Deprecated
    private boolean fileOverride;
    private boolean open;
    private String author;
    private boolean kotlin;
    private boolean swagger;
    private DateType dateType;
    private Supplier<String> commentDate;
    private boolean vueGenerator;

    private boolean mobileGenerator;

    private boolean feignGenerator;

    private GlobalConfig() {
        this.outputDir = System.getProperty("os.name").toLowerCase().contains("windows") ? "D://" : "/tmp";
        this.open = true;
        this.author = "作者";
        this.dateType = DateType.TIME_PACK;
        this.vueGenerator = false;
        this.mobileGenerator = false;
        this.feignGenerator = false;
        this.commentDate = () -> (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
    }

    public String getOutputDir() {
        return this.outputDir;
    }

    /** @deprecated */
    @Deprecated
    public boolean isFileOverride() {
        return this.fileOverride;
    }

    public boolean isOpen() {
        return this.open;
    }

    public String getAuthor() {
        return this.author;
    }

    public boolean isKotlin() {
        return this.kotlin;
    }

    public boolean isSwagger() {
        return this.swagger;
    }

    public boolean isVueGenerator() {
        return vueGenerator;
    }

    public boolean isMobileGenerator() {
        return mobileGenerator;
    }

    @NotNull
    public DateType getDateType() {
        return this.dateType;
    }

    public boolean isFeignGenerator() {
        return feignGenerator;
    }

    public void setFeignGenerator(boolean feignGenerator) {
        this.feignGenerator = feignGenerator;
    }

    @NotNull
    public String getCommentDate() {
        return (String)this.commentDate.get();
    }

    public static class Builder implements IConfigBuilder<GlobalConfig> {
        private final GlobalConfig globalConfig = new GlobalConfig();

        public Builder() {
        }

        /** @deprecated */
        @Deprecated
        public GlobalConfig.Builder fileOverride() {
            this.globalConfig.fileOverride = true;
            return this;
        }

        public GlobalConfig.Builder disableOpenDir() {
            this.globalConfig.open = false;
            return this;
        }

        public GlobalConfig.Builder outputDir(@NotNull String outputDir) {
            this.globalConfig.outputDir = outputDir;
            return this;
        }

        public GlobalConfig.Builder author(@NotNull String author) {
            this.globalConfig.author = author;
            return this;
        }

        public GlobalConfig.Builder enableKotlin() {
            this.globalConfig.kotlin = true;
            return this;
        }

        public GlobalConfig.Builder enableSwagger() {
            this.globalConfig.swagger = true;
            return this;
        }

        public GlobalConfig.Builder dateType(@NotNull DateType dateType) {
            this.globalConfig.dateType = dateType;
            return this;
        }

        public GlobalConfig.Builder commentDate(@NotNull Supplier<String> commentDate) {
            this.globalConfig.commentDate = commentDate;
            return this;
        }

        public GlobalConfig.Builder commentDate(@NotNull String pattern) {
            return this.commentDate(() -> (new SimpleDateFormat(pattern)).format(new Date()));
        }

        public GlobalConfig.Builder enableVueGenerator() {
            this.globalConfig.vueGenerator = true;
            return this;
        }

        public GlobalConfig.Builder enableMobileGenerator() {
            this.globalConfig.mobileGenerator = true;
            return this;
        }

        public GlobalConfig.Builder enableFeignGenerator() {
            this.globalConfig.feignGenerator = true;
            return this;
        }

        public GlobalConfig build() {
            return this.globalConfig;
        }
    }
}
