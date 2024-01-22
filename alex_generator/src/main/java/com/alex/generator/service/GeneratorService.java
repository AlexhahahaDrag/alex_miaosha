package com.alex.generator.service;

/**
 * description: 自动生成service
 * author: alex
 * createDate: 2022/10/11 21:19
 * version: 1.0.0
 */
public interface GeneratorService {

    Boolean generator(String moduleName, String javaPathName, String javaPath,
                      String[] tableNames, String[] tableNameInfo, String author) throws Exception;

    String test();
}