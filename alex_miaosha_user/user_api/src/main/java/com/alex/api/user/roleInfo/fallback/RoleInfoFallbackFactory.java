package com.alex.api.user.roleInfo.fallback;

import com.alex.api.user.roleInfo.api.RoleInfoApi;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.roleInfo.vo.RoleOrgAssignRequest;
import com.alex.api.user.roleInfo.vo.RolePermissionAssignRequest;
import com.alex.api.user.roleInfo.vo.RoleUserAssignRequest;
import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RoleInfoFallbackFactory implements FallbackFactory<RoleInfoApi> {

    @Override
    public RoleInfoApi create(Throwable cause) {
        log.error("RoleInfoApi feign fallback triggered. cause: {}", cause != null ? cause.getMessage() : "unknown", cause);
        return new RoleInfoApi() {
            private <T> Result<T> fallback(String method) {
                return Result.error(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), "角色服务不可用 [" + method + "]");
            }

            @Override
            public Result<Page<RoleInfoVo>> getRoleInfoPage(Long pageNum, Long pageSize, RoleInfoVo roleInfoVo) {
                return fallback("getRoleInfoPage");
            }

            @Override
            public Result<RoleInfoVo> queryRoleInfo(String id) {
                return fallback("queryRoleInfo");
            }

            @Override
            public Result<String> addRoleInfo(RoleInfoVo roleInfoVo) {
                return fallback("addRoleInfo");
            }

            @Override
            public Result<Boolean> updateRoleInfo(RoleInfoVo roleInfoVo) {
                return fallback("updateRoleInfo");
            }

            @Override
            public Result<Boolean> deleteRoleInfo(String ids) {
                return fallback("deleteRoleInfo");
            }

            @Override
            public Result<Boolean> assignUsers(RoleUserAssignRequest request) {
                return fallback("assignUsers");
            }

            @Override
            public Result<Boolean> assignPermissions(RolePermissionAssignRequest request) {
                return fallback("assignPermissions");
            }

            @Override
            public Result<Boolean> assignOrgs(RoleOrgAssignRequest request) {
                return fallback("assignOrgs");
            }
        };
    }
}
