package com.alex.api.user.rolePermissionInfo.fallback;

import com.alex.api.user.rolePermissionInfo.api.RolePermissionInfoApi;
import com.alex.api.user.rolePermissionInfo.vo.RolePermissionInfoVo;
import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RolePermissionInfoFallbackFactory implements FallbackFactory<RolePermissionInfoApi> {

    @Override
    public RolePermissionInfoApi create(Throwable cause) {
        log.error("RolePermissionInfoApi feign fallback triggered. cause: {}", cause != null ? cause.getMessage() : "unknown", cause);
        return new RolePermissionInfoApi() {
            private <T> Result<T> fallback(String method) {
                return Result.error(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), "角色权限服务不可用 [" + method + "]");
            }

            @Override
            public Result<Page<RolePermissionInfoVo>> getRolePermissionInfoPage(Long pageNum, Long pageSize, RolePermissionInfoVo rolePermissionInfoVo) {
                return fallback("getRolePermissionInfoPage");
            }

            @Override
            public Result<RolePermissionInfoVo> queryRolePermissionInfo(String id) {
                return fallback("queryRolePermissionInfo");
            }

            @Override
            public Result<Boolean> addRolePermissionInfo(RolePermissionInfoVo rolePermissionInfoVo) {
                return fallback("addRolePermissionInfo");
            }

            @Override
            public Result<Boolean> updateRolePermissionInfo(RolePermissionInfoVo rolePermissionInfoVo) {
                return fallback("updateRolePermissionInfo");
            }

            @Override
            public Result<Boolean> deleteRolePermissionInfo(String ids) {
                return fallback("deleteRolePermissionInfo");
            }
        };
    }
}
