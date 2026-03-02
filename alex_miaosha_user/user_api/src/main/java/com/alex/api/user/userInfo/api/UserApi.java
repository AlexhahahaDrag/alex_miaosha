package com.alex.api.user.userInfo.api;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.api.user.permissionInfo.vo.PermissionInfoVo;
import com.alex.api.user.userInfo.fallback.UserFallbackFactory;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.base.common.Result;
import com.alex.common.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * description:
 * author:       majf
 * createDate:   2023/1/13 10:45
 * version:      1.0.0
 */
@Component
@FeignClient(contextId = "userApi", name = "alex-user-${spring.profiles.active:dev}", path = "${api.version:/api/v1}/user", fallback = UserFallbackFactory.class, configuration = FeignConfig.class)
public interface UserApi {

    @PostMapping(value = "list")
    Result<List<TUserVo>> getList(@RequestBody TUserVo tUserVo);

    // TODO: 2023/2/21 测试token携带未生效问题 
    @GetMapping(value = "getUserInfo")
    TUserVo getUserByUsername(@RequestParam("username") String username);

    @GetMapping(value = "authToken")
    Result<Boolean> authToken(@RequestParam("token") String token);

    @PostMapping(value = "menu-info/list")
    Result<List<MenuInfoVo>> getMenuInfoList(@RequestBody(required = false) MenuInfoVo menuInfoVo);

    @PostMapping("menu-info")
    Result<MenuInfoVo> addMenuInfo(@RequestBody MenuInfoVo menuInfoVo);

    @PutMapping("menu-info")
    Result<MenuInfoVo> updateMenuInfo(@RequestBody MenuInfoVo menuInfoVo);

    @PostMapping("permission-info")
    Result<PermissionInfoVo> addPermissionInfo(@RequestBody PermissionInfoVo permissionInfoVo);

    @PutMapping("permission-info")
    Result<PermissionInfoVo> updatePermissionInfo(@RequestBody PermissionInfoVo permissionInfoVo);

    @PostMapping(value = "permission-info/list")
    Result<List<PermissionInfoVo>> getPermissionInfoList(@RequestBody(required = false) PermissionInfoVo permissionInfoVo);
}
