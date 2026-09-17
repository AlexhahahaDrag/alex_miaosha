package com.alex.oss.storage.vo;

import lombok.Data;

/**
 * description:  存储对象信息 VO
 * author:       alex
 * createDate:   2023/1/25 20:36
 * version:      2.1.0
 */
@Data
public class ObjectItem {

    private String objectName;

    private Long size;
}
