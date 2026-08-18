package com.alex.user.menuInfo.service.impl;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.api.user.userInfo.vo.UserPermissionContextVo;
import com.alex.base.enums.ResultEnum;
import com.alex.common.utils.string.StringUtils;
import com.alex.common.exception.SystemException;
import com.alex.user.menuInfo.mapper.MenuInfoMapper;
import com.alex.user.menuInfo.entity.MenuInfo;
import com.alex.user.menuInfo.service.MenuInfoService;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.common.redis.key.LoginKey;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * description: 菜单管理表服务实现类
 * author: alex
 * createDate: 2023-12-19 17:34:23
 * version: 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MenuInfoServiceImp extends ServiceImpl<MenuInfoMapper, MenuInfo> implements MenuInfoService {

    private static final String MENU_CACHE_KEY = "menu_all_tree";

    private final MenuInfoMapper menuInfoMapper;

    private final RedisUtils redisUtils;

    private final UserUtils userUtils;

    @Override
    public Page<MenuInfoVo> getPage(Long pageNum, Long pageSize, MenuInfoVo menuInfoVo) {
        Page<MenuInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return menuInfoMapper.getPage(page, menuInfoVo);
    }

    /**
     * @param menuInfoVo
     * description: 查询菜单列表并拼接成父子组结构
     * author:      majf
     * return:      java.util.List<com.alex.api.user.menuInfo.vo.MenuInfoVo>
    */
    @Override
    public List<MenuInfoVo> getList(MenuInfoVo menuInfoVo) {
        boolean isFullQuery = menuInfoVo != null && "1".equals(menuInfoVo.getStatus())
                && menuInfoVo.getName() == null && menuInfoVo.getPath() == null
                && menuInfoVo.getTitle() == null;

        String cacheRealKey = LoginKey.loginKey.getPrefix() + ":" + MENU_CACHE_KEY;
        if (isFullQuery) {
            try {
                List<MenuInfoVo> cachedList = redisUtils.getList(cacheRealKey, MenuInfoVo.class);
                if (cachedList != null && !cachedList.isEmpty()) {
                    log.info("从 Redis 缓存中获取完整菜单树成功");
                    return cachedList;
                }
            } catch (Exception e) {
                log.error("获取菜单缓存异常：{}", e.getMessage());
            }
        }

        // C1 修复（批次2终审）：isFullQuery 命中的是全局共享缓存 menu_all_tree，
        // 必须走不带 @DataPermission 的 getListAll，绝不能用按调用者数据范围过滤的
        // 有注解的 getList——否则非超管的一次直连调用会把被截断的子集写进全局缓存，
        // 污染其后 1 小时内所有用户（含超管、登录构建上下文）读到的菜单树。
        // 非全量查询（管理端按条件筛选列表）维持走 getList，保留数据范围隔离。
        List<MenuInfoVo> list = isFullQuery ? menuInfoMapper.getListAll(menuInfoVo) : menuInfoMapper.getList(menuInfoVo);
        if (list == null || list.isEmpty()) {
            return null;
        }
        Map<Long, List<MenuInfoVo>> menuMap = list.stream()
                .filter(item -> item.getParentId() != null)
                .collect(Collectors.groupingBy(MenuInfoVo::getParentId));
        List<MenuInfoVo> result = list.stream().filter(item -> item.getParentId() == null)
                .peek(item -> item.setChildren(getChildren(item.getId(), menuMap)))
                .toList();

        if (isFullQuery && !result.isEmpty()) {
            try {
                redisUtils.setEx(cacheRealKey, JSONObject.toJSONString(result), 1, TimeUnit.HOURS);
                log.info("完整菜单树成功写入 Redis 缓存");
            } catch (Exception e) {
                log.error("写入菜单缓存异常：{}", e.getMessage());
            }
        }
        return result;
    }

    /**
     * RBAC-BE-MENU-004: load scoped flat list via annotated {@code getList}, then assemble children.
     * Isolated from login cache {@code menu_all_tree}: never calls {@code getListAll} / Redis write.
     * Roots: parentId null/0, or parent not present in the scoped result (orphan promotion).
     */
    @Override
    public List<MenuInfoVo> getTree(MenuInfoVo menuInfoVo) {
        List<MenuInfoVo> list = menuInfoMapper.getList(menuInfoVo);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        return buildMenuTree(list);
    }

    private static List<MenuInfoVo> buildMenuTree(List<MenuInfoVo> list) {
        Map<Long, MenuInfoVo> byId = new HashMap<>(list.size() * 2);
        Set<Long> ids = new HashSet<>(list.size() * 2);
        for (MenuInfoVo node : list) {
            if (node == null || node.getId() == null) {
                continue;
            }
            node.setChildren(new ArrayList<>());
            byId.put(node.getId(), node);
            ids.add(node.getId());
        }
        List<MenuInfoVo> roots = new ArrayList<>();
        for (MenuInfoVo node : list) {
            if (node == null || node.getId() == null) {
                continue;
            }
            Long parentId = node.getParentId();
            if (isRootParent(parentId) || !ids.contains(parentId)) {
                roots.add(node);
                continue;
            }
            MenuInfoVo parent = byId.get(parentId);
            if (parent != null) {
                parent.getChildren().add(node);
            } else {
                roots.add(node);
            }
        }
        return roots;
    }

    private static boolean isRootParent(Long parentId) {
        return parentId == null || parentId == 0L;
    }

    /**
     * param pId
     * param menuMap
     * return
     */
    public List<MenuInfoVo> getChildren(Long pId, Map<Long, List<MenuInfoVo>> menuMap) {
        if (pId == null || menuMap == null || menuMap.get(pId) == null || menuMap.get(pId).isEmpty()) {
            return null;
        }
        List<MenuInfoVo> children = menuMap.get(pId);
        children.forEach(item -> item.setChildren(getChildren(item.getId(), menuMap)));
        return children;
    }

    @Override
    public MenuInfoVo queryMenuInfo(String id) {
        MenuInfoVo menuInfoVo = menuInfoMapper.queryMenuInfo(id);
        // 挂了 @DataPermission 后，越权或不存在的菜单 id 查询结果为 null。
        // 与 role/org/permission 详情查询保持一致的空安全语义：直接返回 null，不 NPE。
        if (menuInfoVo == null) {
            return null;
        }
        return menuInfoVo;
    }

    private void clearMenuCache() {
        try {
            String cacheRealKey = LoginKey.loginKey.getPrefix() + ":" + MENU_CACHE_KEY;
            redisUtils.delete(LoginKey.loginKey, MENU_CACHE_KEY);
            log.info("清理菜单缓存成功：{}", cacheRealKey);
        } catch (Exception e) {
            log.error("清理菜单缓存失败：{}", e.getMessage());
        }
    }

    @Override
    public MenuInfoVo addMenuInfo(MenuInfoVo menuInfoVo) {
        MenuInfo menuInfo = new MenuInfo();
        BeanUtils.copyProperties(menuInfoVo, menuInfo);
        menuInfoMapper.insert(menuInfo);
        menuInfoVo.setId(menuInfo.getId());
        clearMenuCache();
        return menuInfoVo;
    }

    @Override
    public MenuInfoVo updateMenuInfo(MenuInfoVo menuInfoVo) {
        assertMenuAccessible(menuInfoVo == null ? null : menuInfoVo.getId());
        MenuInfo menuInfo = new MenuInfo();
        BeanUtils.copyProperties(menuInfoVo, menuInfo);
        menuInfoMapper.updateById(menuInfo);
        clearMenuCache();
        return menuInfoVo;
    }

    @Override
    public Boolean deleteMenuInfo(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(id -> !StringUtils.isEmpty(id))
                .collect(Collectors.toList());
        if (idArr.isEmpty()) {
            return true;
        }
        // Ownership first: deny out-of-scope menus before delete.
        for (String menuId : idArr) {
            assertMenuAccessible(Long.valueOf(menuId));
        }
        List<Long> parentIds = idArr.stream().map(Long::valueOf).collect(Collectors.toList());
        long childMenuCount = this.count(Wrappers.<MenuInfo>lambdaQuery()
                .in(MenuInfo::getParentId, parentIds)
                .eq(MenuInfo::getIsDelete, 0));
        if (childMenuCount > 0) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "菜单存在下级菜单，不能删除:");
        }
        menuInfoMapper.deleteBatchIds(idArr);
        clearMenuCache();
        return true;
    }

    /**
     * Write-path ownership guard: non-super users must pass a scoped queryMenuInfo
     * before update/delete. Null means outside data scope — never silent success.
     */
    private void assertMenuAccessible(Long id) {
        if (id == null) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "菜单ID不能为空");
        }
        TUserVo loginUser = userUtils.getLoginUser();
        if (loginUser == null) {
            // 登录上下文不可用时必须 fail-closed，不能默认放行。
            throw new SystemException(ResultEnum.PARAM_ERROR, "无权访问：登录上下文不可用");
        }
        if (isSuperAdminLogin(loginUser)) {
            return;
        }
        MenuInfoVo visible = menuInfoMapper.queryMenuInfo(String.valueOf(id));
        if (visible == null) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "无权访问其他机构的菜单");
        }
    }

    private static boolean isSuperAdminLogin(TUserVo loginUser) {
        if (loginUser == null) {
            return false;
        }
        UserPermissionContextVo context = loginUser.getPermissionContext();
        if (context != null && Boolean.TRUE.equals(context.getSuperAdmin())) {
            return true;
        }
        List<RoleInfoVo> roles = loginUser.getRoleInfoVoList();
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        for (RoleInfoVo role : roles) {
            if (role != null && RbacRoleCodes.SUPER.equals(role.getRoleCode())) {
                return true;
            }
        }
        return false;
    }
}
