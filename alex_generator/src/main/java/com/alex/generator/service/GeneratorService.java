package com.alex.generator.service;

/**
 * @description: 自动生成service
 * @author: alex
 * @createDate: 2022/10/11 21:19
 * @version: 1.0.0
 */
public interface GeneratorService {

    boolean generator(String moduleName, String javaPath, String fileName, String parentPackage, String[] tableNames, String author,
                      String myControllerPath, String myServicePath, String myMapperPath, String myEntityPath, String myVoPath, String myClientPath);
}
