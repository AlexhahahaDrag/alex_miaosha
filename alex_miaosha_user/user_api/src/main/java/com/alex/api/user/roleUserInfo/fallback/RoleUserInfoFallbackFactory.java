package com.alex.api.user.roleUserInfo.fallback;

import com.alex.api.user.roleUserInfo.api.RoleUserInfoApi;
import com.alex.api.user.roleUserInfo.vo.RoleUserInfoVo;
import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RoleUserInfoFallbackFactory implements FallbackFactory<RoleUserInfoApi> {

    @Override
    public RoleUserInfoApi create(Throwable cause) {
        log.error("RoleUserInfoApi feign fallback triggered. cause: {}", cause != null ? cause.getMessage() : "unknown", cause);
        return new RoleUserInfoApi() {
            private <T> Result<T> fallback(String method) {
                return Result.error(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), "用户角色服务不可用 [" + method + "]");
            }

            @Override
            public Result<Page<RoleUserInfoVo>> getRoleUserInfoPage(Long pageNum, Long pageSize, RoleUserInfoVo roleUserInfoVo) {
                return fallback("getRoleUserInfoPage");
            }

            @Override
            public Result<RoleUserInfoVo> queryRoleUserInfo(String id) {
                return fallback("queryRoleUserInfo");
            }

            @Override
            public Result<Boolean> addRoleUserInfo(RoleUserInfoVo roleUserInfoVo) {
                return fallback("addRoleUserInfo");
            }

            @Override
            public Result<Boolean> updateRoleUserInfo(RoleUserInfoVo roleUserInfoVo) {
                return fallback("updateRoleUserInfo");
            }

            @Override
            public Result<Boolean> deleteRoleUserInfo(String ids) {
                return fallback("deleteRoleUserInfo");
            }
        };
    }
}
