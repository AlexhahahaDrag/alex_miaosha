package com.alex.generator.vo;

import com.alex.api.user.vo.permissionInfo.PermissionInfoVo;
import lombok.Data;

/**
 * description:  查询权限信息
 * author:       majf
 * createDate:   2023/12/27 14:02
 * version:      1.0.0
 */
@Data
public class PermissionSearchInfo {

    private PermissionInfoVo permissionInfoVo;

    private Boolean permissionExists = false;
}
