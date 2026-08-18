package com.alex.user.rbac.service.impl;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.permissionInfo.vo.PermissionInfoVo;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.userInfo.vo.UserPermissionContextVo;
import com.alex.base.constants.SysConf;
import com.alex.common.redis.key.LoginKey;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.user.menuInfo.service.MenuInfoService;
import com.alex.user.orgUserInfo.service.OrgUserInfoService;
import com.alex.user.rbac.service.UserPermissionContextService;
import com.alex.user.roleUserInfo.service.RoleUserInfoService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserPermissionContextServiceImpl implements UserPermissionContextService {

    private static final String PERMISSION_CONTEXT_CACHE_KEY = "permission_context";

    private final OrgUserInfoService orgUserInfoService;
    private final RoleUserInfoService roleUserInfoService;
    private final MenuInfoService menuInfoService;
    private final Executor asyncTaskExecutor;
    private final RedisUtils redisUtils;

    public UserPermissionContextServiceImpl(OrgUserInfoService orgUserInfoService,
                                            RoleUserInfoService roleUserInfoService,
                                            MenuInfoService menuInfoService,
                                            @Qualifier("asyncTaskExecutor") Executor asyncTaskExecutor,
                                            @Autowired(required = false) RedisUtils redisUtils) {
        this.orgUserInfoService = orgUserInfoService;
        this.roleUserInfoService = roleUserInfoService;
        this.menuInfoService = menuInfoService;
        this.asyncTaskExecutor = asyncTaskExecutor;
        this.redisUtils = redisUtils;
    }

    @Override
    public UserPermissionContextVo buildContext(Long userId) {
        if (userId == null) {
            return null;
        }

        String cacheKey = PERMISSION_CONTEXT_CACHE_KEY + ":" + userId;
        if (redisUtils != null) {
            try {
                UserPermissionContextVo cachedContext = redisUtils.get(LoginKey.loginKey, cacheKey, UserPermissionContextVo.class);
                if (cachedContext != null) {
                    log.info("从 Redis 缓存中获取用户 {} 的权限上下文成功", userId);
                    return cachedContext;
                }
            } catch (Exception e) {
                log.error("读取用户权限上下文缓存异常，userId: {}", userId, e);
            }
        }

        CompletableFuture<List<OrgInfoVo>> orgListFuture = CompletableFuture.supplyAsync(
                () -> emptyIfNull(orgUserInfoService.getOrgInfoList(userId)), asyncTaskExecutor);

        CompletableFuture<List<RoleInfoVo>> roleListFuture = CompletableFuture.supplyAsync(
                () -> emptyIfNull(roleUserInfoService.getRoleInfoList(userId, true)), asyncTaskExecutor);

        CompletableFuture<List<MenuInfoVo>> menuListFuture = CompletableFuture.supplyAsync(
                () -> {
                    MenuInfoVo menuQuery = new MenuInfoVo();
                    menuQuery.setStatus(SysConf.VALID_STATUS);
                    return emptyIfNull(menuInfoService.getList(menuQuery));
                }, asyncTaskExecutor);

        // 并发等待所有异步任务完成
        CompletableFuture.allOf(orgListFuture, roleListFuture, menuListFuture).join();

        try {
            List<OrgInfoVo> orgList = orgListFuture.get();
            List<RoleInfoVo> roleList = roleListFuture.get();
            List<MenuInfoVo> menuList = menuListFuture.get();

            List<String> permissionCodes = collectPermissionCodes(roleList);
            boolean superAdmin = hasSuperAdminRole(roleList);
            List<MenuInfoVo> visibleMenus = superAdmin ? menuList : filterMenusByPermissionCodes(menuList, permissionCodes);

            UserPermissionContextVo context = new UserPermissionContextVo();
            context.setOrgInfo(orgList.isEmpty() ? null : orgList.get(0));
            context.setRoleList(roleList);
            context.setPermissionCodes(permissionCodes);
            context.setButtonPermissionCodes(permissionCodes);
            context.setMenuList(visibleMenus);
            context.setSuperAdmin(superAdmin);

            if (redisUtils != null) {
                try {
                    redisUtils.setEx(LoginKey.loginKey, cacheKey, JSONObject.toJSONString(context), 1, TimeUnit.HOURS);
                    log.info("用户 {} 的权限上下文成功写入 Redis 缓存", userId);
                } catch (Exception e) {
                    log.error("写入用户权限上下文缓存异常，userId: {}", userId, e);
                }
            }

            return context;
        } catch (Exception e) {
            throw new RuntimeException("并行构建权限上下文发生错误", e);
        }
    }

    private List<String> collectPermissionCodes(List<RoleInfoVo> roleList) {
        Set<String> codes = new LinkedHashSet<>();
        for (RoleInfoVo roleInfoVo : roleList) {
            for (PermissionInfoVo permissionInfoVo : emptyIfNull(roleInfoVo.getPermissionList())) {
                String permissionCode = permissionInfoVo.getPermissionCode();
                if (hasText(permissionCode)) {
                    codes.add(permissionCode);
                }
            }
        }
        return new ArrayList<>(codes);
    }

    private boolean hasSuperAdminRole(List<RoleInfoVo> roleList) {
        for (RoleInfoVo roleInfoVo : roleList) {
            if (RbacRoleCodes.SUPER.equals(roleInfoVo.getRoleCode())) {
                return true;
            }
        }
        return false;
    }

    public static List<MenuInfoVo> filterMenusByPermissionCodes(List<MenuInfoVo> menuList, List<String> permissionCodes) {
        Set<String> permissionCodeSet = toPermissionCodeSet(permissionCodes);
        if (emptyIfNull(menuList).isEmpty() || permissionCodeSet.isEmpty()) {
            return Collections.emptyList();
        }

        List<MenuInfoVo> filteredMenus = new ArrayList<>();
        for (MenuInfoVo menuInfoVo : menuList) {
            MenuInfoVo filteredMenu = filterMenuByPermissionCodes(menuInfoVo, permissionCodeSet);
            if (filteredMenu != null) {
                filteredMenus.add(filteredMenu);
            }
        }
        return filteredMenus;
    }

    private static MenuInfoVo filterMenuByPermissionCodes(MenuInfoVo menuInfoVo, Set<String> permissionCodes) {
        List<MenuInfoVo> filteredChildren = filterChildrenByPermissionCodes(menuInfoVo, permissionCodes);
        boolean ownPermissionMatched = hasMenuPermission(menuInfoVo, permissionCodes);
        if (!ownPermissionMatched && filteredChildren.isEmpty()) {
            return null;
        }

        List<MenuInfoVo> originalChildren = menuInfoVo.getChildren();
        if (ownPermissionMatched && originalChildren == null) {
            return menuInfoVo;
        }

        MenuInfoVo filteredMenu = new MenuInfoVo();
        BeanUtils.copyProperties(menuInfoVo, filteredMenu);
        filteredMenu.setChildren(filteredChildren);
        return filteredMenu;
    }

    private static List<MenuInfoVo> filterChildrenByPermissionCodes(MenuInfoVo menuInfoVo, Set<String> permissionCodes) {
        List<MenuInfoVo> children = menuInfoVo.getChildren();
        if (emptyIfNull(children).isEmpty()) {
            return Collections.emptyList();
        }

        List<MenuInfoVo> filteredChildren = new ArrayList<>();
        for (MenuInfoVo child : children) {
            MenuInfoVo filteredChild = filterMenuByPermissionCodes(child, permissionCodes);
            if (filteredChild != null) {
                filteredChildren.add(filteredChild);
            }
        }
        return filteredChildren;
    }

    private static Set<String> toPermissionCodeSet(List<String> permissionCodes) {
        Set<String> permissionCodeSet = new LinkedHashSet<>();
        for (String permissionCode : emptyIfNull(permissionCodes)) {
            if (hasText(permissionCode)) {
                permissionCodeSet.add(permissionCode);
            }
        }
        return permissionCodeSet;
    }

    private static boolean hasMenuPermission(MenuInfoVo menuInfoVo, Set<String> permissionCodes) {
        String permissionCode = menuInfoVo.getPermissionCode();
        return hasText(permissionCode) && permissionCodes.contains(permissionCode);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static <T> List<T> emptyIfNull(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }
}
