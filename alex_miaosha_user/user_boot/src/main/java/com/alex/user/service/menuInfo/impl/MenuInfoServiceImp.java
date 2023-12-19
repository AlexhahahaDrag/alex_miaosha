package com.alex.user.service.menuInfo.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alex.api.user.vo.menuInfo.MenuInfoVo;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.entity.menuInfo.MenuInfo;
import com.alex.user.mapper.menuInfo.MenuInfoMapper;
import com.alex.user.service.menuInfo.MenuInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * @description: 菜单管理表服务实现类
 * @author: alex
 * @createDate: 2023-12-19 17:34:23
 * @version: 1.0.0
 */
@Service
@RequiredArgsConstructor
public class MenuInfoServiceImp extends ServiceImpl<MenuInfoMapper, MenuInfo> implements MenuInfoService {

    private final MenuInfoMapper menuInfoMapper;

    @Override
    public Page<MenuInfoVo> getPage(Long pageNum, Long pageSize, MenuInfoVo menuInfoVo) {
        Page<MenuInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return menuInfoMapper.getPage(page, menuInfoVo);
    }

    @Override
    public List<MenuInfoVo> getList(MenuInfoVo menuInfoVo) {
        List<MenuInfoVo> list = menuInfoMapper.getList(menuInfoVo);
        if (list == null || list.isEmpty()) {
            return null;
        }
        Map<Long, List<MenuInfoVo>> menuMap = list.stream()
                .filter(item -> item.getParentId() != null)
                .collect(Collectors.groupingBy(MenuInfoVo::getParentId));
        List<MenuInfoVo> result = list.stream().filter(item -> item.getParentId() == null).map(item -> {
            item.setChildren(getChildren(item.getId(), menuMap));
            return item;
        }).collect(Collectors.toList());
        return result;
    }

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

    @Override
    public Boolean addMenuInfo(MenuInfoVo menuInfoVo) {
        MenuInfo menuInfo = new MenuInfo();
        BeanUtil.copyProperties(menuInfoVo, menuInfo);
        menuInfoMapper.insert(menuInfo);
        return true;
    }

    @Override
    public Boolean updateMenuInfo(MenuInfoVo menuInfoVo) {
        MenuInfo menuInfo = new MenuInfo();
        BeanUtil.copyProperties(menuInfoVo, menuInfo);
        menuInfoMapper.updateById(menuInfo);
        return true;
    }

    @Override
    public Boolean deleteMenuInfo(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        menuInfoMapper.deleteBatchIds(idArr);
        return true;
    }
}
