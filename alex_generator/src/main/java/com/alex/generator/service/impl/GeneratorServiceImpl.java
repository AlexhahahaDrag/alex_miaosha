package com.alex.generator.service.impl;

import com.alex.common.common.BaseEntity;
import com.alex.common.common.BaseVo;
import com.alex.generator.config.DatabaseConfig;
import com.alex.generator.service.GeneratorService;
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
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public boolean generator(String moduleName, String javaPath, String fileName, String parentPackage, String[] tableNames, String author) {
        String java = "/java/com/alex" + System.getProperty("file.separator") + javaPath;
        for (String tableName : tableNames) {
            executeGenerate(parentPackage, java, tableName, moduleName, fileName, author);
        }
        return true;
    }

    private void executeGenerate(String parentPackage, String java, String tableName, String moduleName, String fileName, String author) {
        String dbConfig = databaseConfig.getUrl();
        String dbUser = databaseConfig.getUsername();
        String dbPassword = databaseConfig.getPassword();
        String base = "/src/main/";
        String separator = System.getProperty("file.separator");
        String basePath = System.getProperty("user.dir");
        String projectPath = basePath + separator + moduleName + getPath(base, separator);
        String voPath =  projectPath + java + "/vo";
        String controllerPath = projectPath + java + "/controller";
        String entityPath = projectPath + java + "/entity";
        String mapperPath = projectPath + java + "/mapper";
        String servicePath = projectPath + java + "/service";
        String clientPath = projectPath + java + "/client";
        List<IFill> list = new ArrayList<>();
        DataSourceConfig.Builder dataSourceConfig = new DataSourceConfig.Builder(dbConfig, dbUser, dbPassword)
                .dbQuery(new MySqlQuery())
                .typeConvert(new MySqlTypeConvert())
                .keyWordsHandler(new MySqlKeyWordsHandler());
        Map<OutputFile, String> pathMap = new HashMap<>();
        pathMap.put(OutputFile.mapperXml, mapperPath + separator + fileName);
        pathMap.put(OutputFile.service, servicePath + separator + fileName);
        pathMap.put(OutputFile.serviceImpl, servicePath + separator + fileName + separator + "impl");
        pathMap.put(OutputFile.mapper, mapperPath + separator + fileName);
        pathMap.put(OutputFile.entity, entityPath + separator + fileName);
        pathMap.put(OutputFile.vo, voPath + separator + fileName);
        pathMap.put(OutputFile.client, clientPath + separator + fileName);
        pathMap.put(OutputFile.controller, controllerPath + separator + fileName);
        FastAutoGenerator.create(dataSourceConfig)
                .globalConfig(builder -> {
                    builder.disableOpenDir()
                            .fileOverride()
                            .outputDir(projectPath + "\\java")
                            .author(author)
                            .enableSwagger()
                            .dateType(DateType.TIME_PACK)
                            .commentDate("yyyy-MM-dd HH:mm:ss")
                    ;
                })
                .packageConfig(builder -> {
                    builder.parent(parentPackage) // 设置父包名
                            .entity("entity" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                            .service("service" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                            .serviceImpl("service" + (StringUtils.isBlank(fileName) ? "" : "." + fileName) + ".impl")
                            .mapper("mapper" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
//                            .other("common.vo." + fileName)
                            .controller("controller" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                            .vo("vo" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                            .client("client" + (StringUtils.isBlank(fileName) ? "" : "." + fileName))
                            .pathInfo(pathMap); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude(tableName)
//                            .addTablePrefix("t_")
                            //配置entity
                            .entityBuilder()
                            .superClass(BaseEntity.class)
                            .disableSerialVersionUID()
                            .enableChainModel()
                            .enableLombok()
//                            .enableRemoveIsPrefix()
                            .enableTableFieldAnnotation()
                            .enableActiveRecord()
//                            .versionColumnName("version")
//                            .versionPropertyName("version")
                            .logicDeleteColumnName("is_delete")
                            .logicDeletePropertyName("isDelete")
//                            .naming(NamingStrategy.no_change)
                            .columnNaming(NamingStrategy.underline_to_camel)
                            .addSuperEntityColumns("id", "creator", "createTime", "updater", "updateTime",
                                    "deleter", "deleteTime", "isDelete", "operator", "operateTime")
////                            .addIgnoreColumns("age")
                            .addTableFills(list)
                            //配置controller
                            .controllerBuilder()
                            .formatFileName("%sController")
                            .enableRestStyle()
//                            //配置service
                            .serviceBuilder()
//                            .superServiceClass(SuperService.class)
//                            .superServiceImplClass(SuperServiceImpl.class)
                            .formatServiceFileName("%sService")
                            .formatServiceImplFileName("%sServiceImp")
                            //配置mapper
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
                            .addSuperVoColumns("id", "creator", "create_at", "updater", "update_at",
                                    "deleter", "delete_at", "is_valid", "is_delete", "operator", "operate_at")
////                            .addIgnoreColumns("age")
                            .addTableFills(list)
                            .enableActiveRecord()
                            //配置client
//                            .clientBuilder()
//                            .formatClientFileName("%sFeignClient")
//                            .enableRestStyle()
                            .build()
                    ; // 设置过滤表前缀
                })
                .injectionConfig(builder -> {
                    builder.beforeOutputFile((tableInfo, objectMap) -> {
                        System.out.println("tableInfo: " + tableInfo.getEntityName() + " objectMap: " + objectMap.size());
//                                ConfigBuilder config = (ConfigBuilder) objectMap.get("config");
//                                //配置other模板及类名
//                                Map<String, String> customFile = Objects.requireNonNull(config.getInjectionConfig()).getCustomFile();
//                                customFile.put(tableInfo.getEntityName() + "Vo.java", "/templates/vo.java.btl");
//                                customFile.put(tableInfo.getEntityName() + "FeignClient.java", "/templates/feignClient.java.btl");
                    })
                            //配置全局变量
//                            .customMap(Collections.singletonMap("vo11", "aaaVo"))
                            .build();
                })
                .templateEngine(new BeetlTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
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
}
