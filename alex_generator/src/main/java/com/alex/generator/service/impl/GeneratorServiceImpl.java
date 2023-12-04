package com.alex.generator.service.impl;

import com.alex.common.common.BaseEntity;
import com.alex.common.common.BaseVo;
import com.alex.generator.config.DatabaseConfig;
import com.alex.generator.config.GeneratorConfig;
import com.alex.generator.service.GeneratorService;
import com.alex.common.utils.string.StringUtils;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.IFill;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.querys.MySqlQuery;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.BeetlTemplateEngine;
import com.baomidou.mybatisplus.generator.keywords.MySqlKeyWordsHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @description:
 * @author: alex
 * @createDate: 2022/10/11 21:22
 * @version: 1.0.0
 */
@Service
@RequiredArgsConstructor
public class GeneratorServiceImpl implements GeneratorService {

    private final DatabaseConfig databaseConfig;

    private final GeneratorConfig generatorConfig;

    @Override
    public Boolean generator(String moduleName, String javaPath, String[] tableNames, String author) {
        for (String tableName : tableNames) {
            executeGenerate(tableName, moduleName, author, javaPath);
        }
        return true;
    }

    private void executeGenerate(String tableName, String moduleName, String author, String javaPath) {
        String separator = System.getProperty("file.separator");
        String bootDir = "/java/com/alex" + separator + javaPath;
        String apiDir = "/java/com/alex" + separator + "api" + separator + javaPath;

        String dbConfig = databaseConfig.getUrl();
        String dbUser = databaseConfig.getUsername();
        String dbPassword = databaseConfig.getPassword();
        String base = "/src/main/";

        String basePath = StringUtils.isNotBlank(generatorConfig.getJavaPath()) ? generatorConfig.getJavaPath() : System.getProperty("user.dir");
        String innerModule = moduleName.substring(moduleName.lastIndexOf('_') + 1);
        String projectPath = basePath + separator + moduleName + separator + innerModule + "_boot" + getPath(base, separator);
        String clientPathProject = basePath + separator + moduleName + separator + innerModule + "_api" + getPath(base, separator);
        String controllerPath = projectPath + bootDir + separator + "controller";
        String entityPath = projectPath + bootDir + separator + "entity";
        String mapperPath = projectPath + bootDir + separator + "mapper";
        String servicePath = projectPath + bootDir + separator + "service";
        String voPath = clientPathProject + apiDir + separator + "vo";
        String clientPath = clientPathProject + apiDir + separator + "api";
        String vuePath = StringUtils.isNotEmpty(
                generatorConfig.getVuePath()) ? generatorConfig.getVuePath() + separator + javaPath : projectPath + bootDir + separator + "vue";
        String tsPath = StringUtils.isNotEmpty(generatorConfig.getTsPath()) ? generatorConfig.getTsPath() + separator + javaPath : projectPath + bootDir + separator + "vue";

        String mobileTsTsPath = StringUtils.isNotEmpty(generatorConfig.getMobileTsPath()) ?
                generatorConfig.getMobileTsPath() + separator + javaPath : projectPath + bootDir + separator + "vue";

        String mobileVuePath = StringUtils.isNotEmpty(
                generatorConfig.getMobileVuePath()) ? generatorConfig.getMobileVuePath() + separator + javaPath : projectPath + bootDir + separator + "vue";

        List<IFill> list = new ArrayList<>();
        DataSourceConfig.Builder dataSourceConfig = new DataSourceConfig.Builder(dbConfig, dbUser, dbPassword)
                .dbQuery(new MySqlQuery())
                .typeConvert(new MySqlTypeConvert())
                .keyWordsHandler(new MySqlKeyWordsHandler());
        Map<OutputFile, String> pathMap = new HashMap<>();
        String fileName = StringUtils.camel(tableName);
        pathMap.put(OutputFile.mapperXml, mapperPath + separator + fileName);
        pathMap.put(OutputFile.service, servicePath + separator + fileName);
        pathMap.put(OutputFile.serviceImpl, servicePath + separator + fileName + separator + "impl");
        pathMap.put(OutputFile.mapper, mapperPath + separator + fileName);
        pathMap.put(OutputFile.entity, entityPath + separator + fileName);
        pathMap.put(OutputFile.vo, voPath + separator + fileName);
        pathMap.put(OutputFile.client, clientPath + separator + fileName);
        pathMap.put(OutputFile.controller, controllerPath + separator + fileName);
        pathMap.put(OutputFile.detail, vuePath + separator + fileName + separator + "detail");
        pathMap.put(OutputFile.list, vuePath + separator + fileName);
        pathMap.put(OutputFile.ts, tsPath + separator + fileName);
        pathMap.put(OutputFile.mobileTsTs, mobileTsTsPath + separator + fileName);
        pathMap.put(OutputFile.mobileDetail, mobileVuePath + separator + fileName + separator + "detail");
        pathMap.put(OutputFile.mobileVue, mobileVuePath + separator + fileName);
        String boot = javaPath + ".";
        String api = "api." + javaPath + ".";
        FastAutoGenerator fastAutoGenerator = FastAutoGenerator.create(dataSourceConfig);
        fastAutoGenerator.globalConfig(builder -> {
            builder.outputDir(projectPath + "\\java")
                    .author(author)
                    .enableSwagger()
                    .disableOpenDir()
                    .fileOverride()
                    .dateType(DateType.TIME_PACK)
                    .commentDate("yyyy-MM-dd HH:mm:ss");
            if (generatorConfig.isFeign()) {
                builder.enableFeignGenerator();
            }
            if (generatorConfig.isVue()) {
                builder.enableVueGenerator();
            }
            if (generatorConfig.isMobile()) {
                builder.enableMobileGenerator();
            }
        });
        fastAutoGenerator.packageConfig(builder -> {
            builder.parent(generatorConfig.getParentPackage()) // 设置父包名
                    .entity(boot + "entity" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                    .service(boot + "service" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                    .serviceImpl(boot + "service" + (StringUtils.isBlank(fileName) ? "" : "." + fileName) + ".impl")
                    .mapper(boot + "mapper" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                    .controller(boot + "controller" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                    .vo(api + "vo" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                    .client(api + "api" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                    .detailTs(boot + "vue.detail" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                    .detailVue(boot + "vue.detail" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                    .listTs(boot + "vue" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                    .listVue(boot + "vue" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                    .tsTs(boot + "vue" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                    .mobileDetailTs(boot + "vue.detail" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                    .mobileDetail(boot + "vue.detail" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                    .mobileTs(boot + "vue" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                    .mobileVue(boot + "vue" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                    .mobileTsTs(boot + "vue" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                    .pathInfo(pathMap); // 设置mapperXml生成路径
        });
        fastAutoGenerator.strategyConfig(builder -> {
            builder.addInclude(tableName)
                    .addTablePrefix(generatorConfig.getTablePrefix())
                    .entityBuilder()
                    .superClass(BaseEntity.class)
                    .disableSerialVersionUID()
                    .enableChainModel()
                    .enableLombok()
                    .enableRemoveIsPrefix()
                    .enableTableFieldAnnotation()
                    .enableActiveRecord()
//                            .versionColumnName("version")
//                            .versionPropertyName("version")
                    .logicDeleteColumnName(generatorConfig.getLogicDeleteColumnName())
                    .logicDeletePropertyName(generatorConfig.getLogicDeletePropertyName())
//                            .naming(NamingStrategy.no_change)
                    .columnNaming(NamingStrategy.underline_to_camel)
                    .addSuperEntityColumns(generatorConfig.getSuperEntityColumns())
////                            .addIgnoreColumns("age")
                    .addTableFills(list)
                    .controllerBuilder()
                    .formatFileName("%sController")
                    .enableRestStyle()
                    .serviceBuilder()
//                            .superServiceClass(SuperService.class)
//                            .superServiceImplClass(SuperServiceImpl.class)
                    .formatServiceFileName("%sService")
                    .formatServiceImplFileName("%sServiceImp")
                    .mapperBuilder()
//                            .superClass(SuperMapper.class)
                    .enableMapperAnnotation()
                    .enableBaseResultMap()
                    .enableBaseColumnList()
                    .formatMapperFileName("%sMapper")
                    .formatXmlFileName("%sMapper")
                    .voBuilder()
                    .formatVoFileName("%sVo")
                    .superVoClass(BaseVo.class)
                    .enableChainModel()
                    .enableLombok()
                    .disableSerialVersionUID()
                    .enableTableFieldAnnotation()
                    .columnNaming(NamingStrategy.underline_to_camel)
                    .addSuperVoColumns(generatorConfig.getAddSuperVoColumns())//设置super类字段
                    .addIgnoreColumns("") //设置忽略字段
                    .addTableFills(list)
                    .enableActiveRecord()
                    //配置client
                    .clientBuilder()
                    .formatClientFileName("%sApi")
                    .enableRestStyle()
                    //配置ts
                    .tsTsBuilder()
                    .formatTsTsFileName("%sTs")
                    // mobile
                    .mobileTsBuilder()
                    .formatMobileTsFileName("%sTs")
                    .mobileVueBuilder()
                    .formatMobileVueFileName("%s")
                    .mobileDetailTsBuilder()
                    .formatMobileDetailTsFileName("%sDetailTs")
                    .mobileTsTsBuilder()
                    .formatMobileTsTsFileName("%sTs")
                    .build()
            ; // 设置过滤表前缀
        });
        fastAutoGenerator.injectionConfig(builder -> {
            builder.beforeOutputFile((tableInfo, objectMap) -> {
//                                ConfigBuilder config = (ConfigBuilder) objectMap.get("config");
//                                //配置other模板及类名
            }).customMap(Collections.singletonMap("javaPath", javaPath)).build();
        });
        fastAutoGenerator.templateEngine(new BeetlTemplateEngine());
        fastAutoGenerator.execute();// 使用Freemarker引擎模板，默认的是Velocity引擎模板
    }

    private static String getPath(String add, String separator) {
        if (StringUtils.isEmpty(add)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String[] split = add.split("/");
        for (String s : split) {
            sb.append(separator).append(s);
        }
        return sb.toString();
    }

    @Override
    public String test() {
        return "test";
    }
}
