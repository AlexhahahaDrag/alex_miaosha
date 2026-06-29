package com.alex.user.menuInfo.service.impl;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.menuInfo.mapper.MenuInfoMapper;
import com.alex.user.menuInfo.entity.MenuInfo;
import com.alex.user.menuInfo.service.MenuInfoService;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.common.redis.key.LoginKey;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

        List<MenuInfoVo> list = menuInfoMapper.getList(menuInfoVo);
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

    /**ma
     *
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
        return menuInfoMapper.queryMenuInfo(id);
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
        List<String> idArr = Arrays.asList(ids.split(","));
        menuInfoMapper.deleteBatchIds(idArr);
        clearMenuCache();
        return true;
    }
}
