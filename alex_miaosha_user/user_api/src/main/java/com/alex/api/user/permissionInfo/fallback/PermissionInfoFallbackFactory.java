package com.alex.api.user.permissionInfo.fallback;

import com.alex.api.user.permissionInfo.api.PermissionInfoApi;
import com.alex.api.user.permissionInfo.vo.PermissionInfoVo;
import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PermissionInfoFallbackFactory implements FallbackFactory<PermissionInfoApi> {

    @Override
    public PermissionInfoApi create(Throwable cause) {
        log.error("PermissionInfoApi feign fallback triggered. cause: {}", cause != null ? cause.getMessage() : "unknown", cause);
        return new PermissionInfoApi() {
            private <T> Result<T> fallback(String method) {
                return Result.error(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), "权限服务不可用 [" + method + "]");
            }

            @Override
            public Result<Page<PermissionInfoVo>> getPermissionInfoPage(Long pageNum, Long pageSize, PermissionInfoVo permissionInfoVo) {
                return fallback("getPermissionInfoPage");
            }

            @Override
            public Result<PermissionInfoVo> queryPermissionInfo(String id) {
                return fallback("queryPermissionInfo");
            }

            @Override
            public Result<Boolean> addPermissionInfo(PermissionInfoVo permissionInfoVo) {
                return fallback("addPermissionInfo");
            }

            @Override
            public Result<Boolean> updatePermissionInfo(PermissionInfoVo permissionInfoVo) {
                return fallback("updatePermissionInfo");
            }

            @Override
            public Result<Boolean> deletePermissionInfo(String ids) {
                return fallback("deletePermissionInfo");
            }
        };
    }
}
