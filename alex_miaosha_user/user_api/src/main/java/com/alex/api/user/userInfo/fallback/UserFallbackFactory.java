package com.alex.api.user.userInfo.fallback;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.api.user.permissionInfo.vo.PermissionInfoVo;
import com.alex.api.user.userInfo.api.UserApi;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class UserFallbackFactory implements FallbackFactory<UserApi> {

    @Override
    public UserApi create(Throwable cause) {
        log.error("UserApi feign fallback triggered. cause: {}", cause != null ? cause.getMessage() : "unknown", cause);
        return new UserApi() {
            private <T> Result<T> fallback(String method) {
                return Result.error(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), "用户服务不可用 [" + method + "]");
            }

            @Override
            public Result<List<TUserVo>> getList(TUserVo tUserVo) {
                return fallback("getList");
            }

            @Override
            public Result<TUserVo> queryUser(String id) {
                return fallback("queryUser");
            }

            @Override
            public TUserVo getUserByUsername(String username) {
                return null;
            }

            @Override
            public Result<Boolean> authToken(String token) {
                return fallback("authToken");
            }

            @Override
            public Result<List<MenuInfoVo>> getMenuInfoList(MenuInfoVo menuInfoVo) {
                return fallback("getMenuInfoList");
            }

            @Override
            public Result<MenuInfoVo> addMenuInfo(MenuInfoVo menuInfoVo) {
                return fallback("addMenuInfo");
            }

            @Override
            public Result<MenuInfoVo> updateMenuInfo(MenuInfoVo menuInfoVo) {
                return fallback("updateMenuInfo");
            }

            @Override
            public Result<PermissionInfoVo> addPermissionInfo(PermissionInfoVo permissionInfoVo) {
                return fallback("addPermissionInfo");
            }

            @Override
            public Result<PermissionInfoVo> updatePermissionInfo(PermissionInfoVo permissionInfoVo) {
                return fallback("updatePermissionInfo");
            }

            @Override
            public Result<List<PermissionInfoVo>> getPermissionInfoList(PermissionInfoVo permissionInfoVo) {
                return fallback("getPermissionInfoList");
            }
        };
    }
}
