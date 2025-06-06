package com.alex.generator.service.impl;

import com.alex.api.user.api.UserApi;
import com.alex.api.user.vo.menuInfo.MenuInfoVo;
import com.alex.api.user.vo.permissionInfo.PermissionInfoVo;
import com.alex.base.common.Result;
import com.alex.common.common.BaseEntity;
import com.alex.common.common.BaseVo;
import com.alex.common.utils.string.StringUtils;
import com.alex.generator.config.DatabaseConfig;
import com.alex.generator.config.GeneratorConfig;
import com.alex.generator.service.GeneratorService;
import com.alex.generator.vo.MenuSearchInfo;
import com.alex.generator.vo.PermissionSearchInfo;
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
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description:
 * author: alex
 * createDate: 2022/10/11 21:22
 * version: 1.0.0
 */
@Service
@RequiredArgsConstructor
public class GeneratorServiceImpl implements GeneratorService {

    private final DatabaseConfig databaseConfig;

    private final GeneratorConfig generatorConfig;

    private final UserApi userApi;

    private final static String DETAIL = "Detail";

    private final static String NO_INFO = "0";

    private final static String YES_INFO = "1";

    @Override
    public Boolean generator(String moduleName, String javaPathName, String javaPath, String[] tableNames, String[] tableNameInfo, String author) throws Exception {
        for (int i = 0; i < tableNames.length; i++) {
            executeGenerate(tableNames[i], moduleName, javaPathName, author, javaPath, tableNameInfo[i]);
        }
        return true;
    }

    private void executeGenerate(String tableName, String moduleName, String javaPathName,
                                 String author, String javaPath, String fileNameInfo) throws Exception {
        String separator = FileSystems.getDefault().getSeparator();
        String base = "/src/main/";
        String basePath = StringUtils.isNotBlank(generatorConfig.getJavaPath()) ? generatorConfig.getJavaPath() : System.getProperty("user.dir");
        String innerModule = moduleName.substring(moduleName.lastIndexOf('_') + 1);
        String projectPath = basePath + separator + moduleName + separator + innerModule + "_boot" + getPath(base, separator);
        String fileName = StringUtils.camel(tableName.startsWith("t_") ? tableName.substring(2) : tableName);
        String clientPathProject = basePath + separator + moduleName + separator + innerModule + "_api" + getPath(base, separator);
        String boot = javaPath + ".";
        String api = "api." + javaPath + ".";
        List<IFill> list = Lists.newArrayList();
        DataSourceConfig.Builder dataSourceConfig = dataSourceConfig(databaseConfig);
        Map<OutputFile, String> pathMap = pathMap(fileName, separator, javaPath, projectPath, clientPathProject);
        FastAutoGenerator fastAutoGenerator = fastAutoGenerator(dataSourceConfig, projectPath, author, boot, fileName, api, pathMap, tableName, javaPath, list);
        fastAutoGenerator.execute();// 使用Freemarker引擎模板，默认的是Velocity引擎模板
        // 插入菜单数据到菜单表中
        addMenu(javaPath, javaPathName, fileName, fileNameInfo);
        // 插入数据权限到权限表中
        addPermission(javaPath, javaPathName, fileName, fileNameInfo);
    }

    private void addMenu(String javaPath, String javaPathName, String fileName, String fileNameInfo) {
        // 查询主菜单是否存在
        MenuInfoVo query = new MenuInfoVo();
        query.setStatus(YES_INFO);
        Result<List<MenuInfoVo>> result = userApi.getMenuInfoList(query);
        List<MenuInfoVo> menuInfoList = result.getData();
        MenuSearchInfo moduleMenuInfo = findMenuInfo(menuInfoList, javaPath);
        MenuSearchInfo menuInfo = findMenuInfo(moduleMenuInfo.getMenuInfoVo() == null ? null : moduleMenuInfo.getMenuInfoVo().getChildren(), fileName);
        MenuSearchInfo detailMenuInfo = findMenuInfo(menuInfo.getMenuInfoVo() == null ? null : moduleMenuInfo.getMenuInfoVo().getChildren(), fileName + DETAIL);
        if (!moduleMenuInfo.getMenuExists()) {
            MenuInfoVo addModualMenuInfoVo = addMenuInfo(getMenuInfo(javaPath, null, null, null, "/" + javaPath + (StringUtils.isEmpty(fileName) ? "" : "/" + fileName),
                    moduleMenuInfo.getOrderBy(), javaPathName, null, NO_INFO, NO_INFO, javaPath));
            moduleMenuInfo.setMenuInfoVo(addModualMenuInfoVo);
        }
        MenuInfoVo addMenuInfo = getMenuInfo(javaPath, fileName, fileName, moduleMenuInfo.getMenuInfoVo().getId(), null,
                menuInfo.getOrderBy(), fileNameInfo, fileName, NO_INFO, YES_INFO, javaPath + ":" + fileName);
        if (menuInfo.getMenuExists()) {
            addMenuInfo.setId(menuInfo.getMenuInfoVo().getId());
            menuInfo.setMenuInfoVo(updateMenuInfo(addMenuInfo));
        } else {
            menuInfo.setMenuInfoVo(addMenuInfo(addMenuInfo));
        }
        MenuInfoVo addChildMenuInfo = getMenuInfo(javaPath, fileName + DETAIL, fileName + "/" + fileName + "Detail", moduleMenuInfo.getMenuInfoVo().getId(),
                null, detailMenuInfo.getOrderBy(), fileNameInfo + "详情", fileName + "/" + fileName + "Detail", YES_INFO, NO_INFO, javaPath + ":" + fileName + ":" + "detail");
        if (detailMenuInfo.getMenuExists()) {
            addChildMenuInfo.setId(detailMenuInfo.getMenuInfoVo().getId());
            menuInfo.setMenuInfoVo(updateMenuInfo(addChildMenuInfo));
        } else {
            menuInfo.setMenuInfoVo(addMenuInfo(addChildMenuInfo));
        }
    }

    /**
     * @param menuInfoList
     * @param menuName     description: 根据菜单名称查询菜单信息
     *                     author:      majf
     *                     return:      com.alex.generator.vo.MenuSearchInfo
     */
    private MenuSearchInfo findMenuInfo(List<MenuInfoVo> menuInfoList, String menuName) {
        MenuSearchInfo menuSearchInfo = new MenuSearchInfo();
        if (menuInfoList == null || menuInfoList.isEmpty() || StringUtils.isEmpty(menuName)) {
            menuSearchInfo.setOrderBy(10);
            return menuSearchInfo;
        }
        int orderBy = 0;
        for (MenuInfoVo menuInfoVo : menuInfoList) {
            if (menuName.equals(menuInfoVo.getName())) {
                menuSearchInfo.setMenuInfoVo(menuInfoVo);
                menuSearchInfo.setMenuExists(true);
            }
            orderBy = Math.max(orderBy, menuInfoVo.getOrderBy() == null ? 0 : menuInfoVo.getOrderBy());
        }
        menuSearchInfo.setOrderBy(orderBy + 10);
        return menuSearchInfo;
    }

    private MenuInfoVo getMenuInfo(String moduleName, String fileName, String path, Long parentId, String redirect, Integer orderBy,
                                   String title, String component, String hideInMenu, String showInHome, String permissionCode) {
        MenuInfoVo menuInfoVo = new MenuInfoVo();
        menuInfoVo.setName(StringUtils.isEmpty(fileName) ? moduleName : fileName);
        menuInfoVo.setPath("/" + moduleName + (StringUtils.isEmpty(path) ? "" : "/" + path));
        menuInfoVo.setTitle(title);
        if (StringUtils.isEmpty(fileName)) {
            menuInfoVo.setComponent("Layout");
            menuInfoVo.setRedirect(redirect);
        } else {
            menuInfoVo.setComponent("/src/views/" + moduleName + "/" + component + "/index.vue");
        }
        menuInfoVo.setIcon(StringUtils.isEmpty(fileName) ? moduleName : fileName);
        menuInfoVo.setParentId(parentId);
        menuInfoVo.setStatus(YES_INFO);
        menuInfoVo.setOrderBy(orderBy);
        menuInfoVo.setShowInHome(showInHome);
        menuInfoVo.setHideInMenu(hideInMenu);
        menuInfoVo.setPermissionCode(permissionCode);
        return menuInfoVo;
    }

    private MenuInfoVo addMenuInfo(MenuInfoVo menuInfoVo) {
        Result<MenuInfoVo> menuInfoVoResult = userApi.addMenuInfo(menuInfoVo);
        return menuInfoVoResult.getData();
    }

    private MenuInfoVo updateMenuInfo(MenuInfoVo menuInfoVo) {
        Result<MenuInfoVo> menuInfoVoResult = userApi.updateMenuInfo(menuInfoVo);
        return menuInfoVoResult.getData();
    }

    private void addPermission(String javaPath, String javaPathName, String fileName, String fileNameInfo) {
        // 查询主菜单是否存在
        PermissionInfoVo query = new PermissionInfoVo();
        query.setStatus(YES_INFO);
        Result<List<PermissionInfoVo>> result = userApi.getPermissionInfoList(query);
        List<PermissionInfoVo> menuInfoList = result.getData();
        PermissionSearchInfo moduleMenuInfo = findPermissionInfo(menuInfoList, javaPath);
        PermissionSearchInfo menuInfo = findPermissionInfo(moduleMenuInfo.getPermissionInfoVo() == null ? null : moduleMenuInfo.getPermissionInfoVo().getChildren(), javaPath + ":" + fileName);
        PermissionSearchInfo detailMenuInfo = findPermissionInfo(menuInfo.getPermissionInfoVo() == null ? null : moduleMenuInfo.getPermissionInfoVo().getChildren(), javaPath + ":" + fileName + ":" + "detail");
        if (!moduleMenuInfo.getPermissionExists()) {
            PermissionInfoVo addPermissionInfoVo = addPermissionInfo(getPermissionInfo(javaPath, null, null, null, "/" + javaPath + (StringUtils.isEmpty(fileName) ? "" : "/" + fileName)));
            moduleMenuInfo.setPermissionInfoVo(addPermissionInfoVo);
        }
        PermissionInfoVo addPermissionInfo = getPermissionInfo(javaPath, javaPath + ":" + fileName, fileName, moduleMenuInfo.getPermissionInfoVo().getId(), fileNameInfo);
        if (menuInfo.getPermissionExists()) {
            addPermissionInfo.setId(menuInfo.getPermissionInfoVo().getId());
            menuInfo.setPermissionInfoVo(updatePermissionInfo(addPermissionInfo));
        } else {
            menuInfo.setPermissionInfoVo(addPermissionInfo(addPermissionInfo));
        }
//        PermissionInfoVo addChildPermissionInfo = getPermissionInfo(javaPath, javaPath + ":" + fileName + ":detail", fileName + "/" + fileName + "Detail",
//                moduleMenuInfo.getPermissionInfoVo().getId(), fileNameInfo + "详情");
//        if (detailMenuInfo.getPermissionExists()) {
//            addChildPermissionInfo.setId(detailMenuInfo.getPermissionInfoVo().getId());
//            menuInfo.setPermissionInfoVo(updatePermissionInfo(addChildPermissionInfo));
//        } else {
//            menuInfo.setPermissionInfoVo(addPermissionInfo(addChildPermissionInfo));
//        }
    }

    /**
     * @param permissionInfoList
     * @param permissionCode     description: 根据菜单名称查询菜单信息
     *                           author:      majf
     *                           return:      com.alex.generator.vo.MenuSearchInfo
     */
    private PermissionSearchInfo findPermissionInfo(List<PermissionInfoVo> permissionInfoList, String permissionCode) {
        PermissionSearchInfo permissionSearchInfo = new PermissionSearchInfo();
        if (permissionInfoList == null || permissionInfoList.isEmpty() || StringUtils.isEmpty(permissionCode)) {
            return permissionSearchInfo;
        }
        for (PermissionInfoVo permissionInfoVo : permissionInfoList) {
            if (permissionCode.equals(permissionInfoVo.getPermissionCode())) {
                permissionSearchInfo.setPermissionInfoVo(permissionInfoVo);
                permissionSearchInfo.setPermissionExists(true);
            }
        }
        return permissionSearchInfo;
    }

    private PermissionInfoVo getPermissionInfo(String moduleName, String fileName, String path, Long parentId, String title) {
        PermissionInfoVo permissionInfoVo = new PermissionInfoVo();
        permissionInfoVo.setPermissionCode(StringUtils.isEmpty(fileName) ? moduleName : fileName);
        permissionInfoVo.setOptions("/" + moduleName + (StringUtils.isEmpty(path) ? "" : "/" + path));
        permissionInfoVo.setPermissionName(title);
        permissionInfoVo.setStatus(YES_INFO);
        permissionInfoVo.setParentId(parentId);
        return permissionInfoVo;
    }

    private PermissionInfoVo addPermissionInfo(PermissionInfoVo permissionInfoVo) {
        Result<PermissionInfoVo> menuInfoVoResult = userApi.addPermissionInfo(permissionInfoVo);
        return menuInfoVoResult.getData();
    }

    private PermissionInfoVo updatePermissionInfo(PermissionInfoVo permissionInfoVo) {
        Result<PermissionInfoVo> menuInfoVoResult = userApi.updatePermissionInfo(permissionInfoVo);
        return menuInfoVoResult.getData();
    }

    private Map<OutputFile, String> pathMap(String fileName, String separator, String javaPath, String projectPath, String clientPathProject) {
        String bootDir = "/java/com/alex" + separator + javaPath;
        String apiDir = "/java/com/alex" + separator + "api" + separator + javaPath;
        String controllerPath = projectPath + bootDir + separator + fileName + separator + "controller";
        String entityPath = projectPath + bootDir + separator + fileName + separator + "entity";
        String mapperPath = projectPath + bootDir + separator + fileName + separator + "mapper";
        String servicePath = projectPath + bootDir + separator + fileName + separator + "service";
        String voPath = clientPathProject + apiDir + separator + fileName + separator + "vo";
        String clientPath = clientPathProject + apiDir + separator + fileName + separator + "api";
        String vuePath = StringUtils.isNotEmpty(
                generatorConfig.getVuePath()) ? generatorConfig.getVuePath() + separator + javaPath : projectPath + bootDir + separator + "vue";
        String tsPath = StringUtils.isNotEmpty(generatorConfig.getTsPath()) ? generatorConfig.getTsPath() + separator + javaPath : projectPath + bootDir + separator + "vue";
        String mobileTsTsPath = StringUtils.isNotEmpty(generatorConfig.getMobileTsPath()) ?
                generatorConfig.getMobileTsPath() + separator + javaPath : projectPath + bootDir + separator + "vue";
        String mobileVuePath = StringUtils.isNotEmpty(
                generatorConfig.getMobileVuePath()) ? generatorConfig.getMobileVuePath() + separator + javaPath : projectPath + bootDir + separator + "vue";
        Map<OutputFile, String> pathMap = new HashMap<>();
        pathMap.put(OutputFile.mapperXml, mapperPath + separator);
        pathMap.put(OutputFile.service, servicePath + separator);
        pathMap.put(OutputFile.serviceImpl, servicePath + separator + separator + "impl");
        pathMap.put(OutputFile.mapper, mapperPath + separator);
        pathMap.put(OutputFile.entity, entityPath + separator);
        pathMap.put(OutputFile.vo, voPath + separator);
        pathMap.put(OutputFile.client, clientPath + separator);
        pathMap.put(OutputFile.controller, controllerPath + separator);
        pathMap.put(OutputFile.detail, vuePath + separator + fileName + separator + fileName + "Detail");
        pathMap.put(OutputFile.list, vuePath + separator + fileName);
        // TODO (majf) 2025/6/6 11:18 测试ts的路径
        pathMap.put(OutputFile.ts, tsPath + separator + fileName + separator + "api");
        pathMap.put(OutputFile.mobileTsTs, mobileTsTsPath + separator + fileName);
        pathMap.put(OutputFile.mobileDetail, mobileVuePath + separator + fileName + separator + fileName + "Detail");
        pathMap.put(OutputFile.mobileVue, mobileVuePath + separator + fileName);
        return pathMap;
    }

    private DataSourceConfig.Builder dataSourceConfig(DatabaseConfig databaseConfig) {
        DataSourceConfig.Builder dataSourceConfig = new DataSourceConfig.Builder(databaseConfig.getUrl(), databaseConfig.getUsername(), databaseConfig.getPassword())
                .dbQuery(new MySqlQuery())
                .typeConvert(new MySqlTypeConvert())
                .keyWordsHandler(new MySqlKeyWordsHandler());
        return dataSourceConfig;
    }

    private String getFileName(String fileName, String defaultFileName) {
        return StringUtils.isBlank(fileName) ? defaultFileName : fileName + ".";
    }

    private FastAutoGenerator fastAutoGenerator(DataSourceConfig.Builder dataSourceConfig, String projectPath,
                                                String author, String boot, String fileName, String api,
                                                Map<OutputFile, String> pathMap, String tableName,
                                                String javaPath, List<IFill> list) {
        FastAutoGenerator fastAutoGenerator = FastAutoGenerator.create(dataSourceConfig);
        fastAutoGenerator.globalConfig(builder -> {
            builder.outputDir(projectPath + "\\java")
                    .author(author)
                    .fileOverride()
                    .enableSwagger()
                    .disableOpenDir() // 打开目录
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
        String file = getFileName(fileName, "");
        fastAutoGenerator.packageConfig(builder -> {
            builder.parent(generatorConfig.getParentPackage()) // 设置父包名
                    .entity(boot + file + "entity")
                    .service(boot + file + "service")
                    .serviceImpl(boot + file + "service" + ".impl")
                    .mapper(boot + file + "mapper")
                    .controller(boot + file + "controller")
                    .vo(api + file + "vo")
                    .client(api + file + "api")
                    .detailTs(boot + "vue" + (getFileName(fileName, "detail") + "Detail") + file)
                    .detailVue(boot + "vue" + (getFileName(fileName, "detail") + "Detail") + file)
                    .listTs(boot + "vue" + file)
                    .listVue(boot + "vue" + file)
                    .tsTs(boot + "vue" + file)
                    .mobileDetailTs(boot + "vue." + (getFileName(fileName, "detail") + "Detail") + file)
                    .mobileDetail(boot + "vue." + (getFileName(fileName, "detail") + "Detail") + file)
                    .mobileTs(boot + "vue" + file)
                    .mobileVue(boot + "vue" + file)
                    .mobileTsTs(boot + "vue" + file)
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
                    .listVueBuilder()
                    .formatListVueFileName("index")
                    .detailVueBuilder()
                    .formatDetailVueFileName("index")
                    // mobile
                    .mobileTsBuilder()
                    .formatMobileTsFileName("%sTs")
                    .mobileVueBuilder()
                    .formatMobileVueFileName("index")
                    .mobileDetailTsBuilder()
                    .formatMobileDetailTsFileName("%sDetailTs")
                    .mobileDetailBuilder()
                    .formatMobileDetailFileName("index")
                    .mobileTsTsBuilder()
                    .formatMobileTsTsFileName("%sTs")
                    .build()
            ;
        });
        fastAutoGenerator.injectionConfig(builder -> {
            builder.beforeOutputFile((tableInfo, objectMap) -> {
//                                ConfigBuilder config = (ConfigBuilder) objectMap.get("config");
//                                //配置other模板及类名
            }).customMap(Collections.singletonMap("javaPath", javaPath)).build();
        });
        fastAutoGenerator.templateEngine(new BeetlTemplateEngine());
        return fastAutoGenerator;
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
