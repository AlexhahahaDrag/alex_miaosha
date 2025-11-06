package com.alex.user.menuInfo.service;

import com.alex.api.user.vo.menuInfo.MenuInfoVo;
import com.alex.user.menuInfo.entity.MenuInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 菜单管理表服务类
 * author: alex
 * createDate: 2023-12-19 17:34:23
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface MenuInfoService extends IService<MenuInfo> {

    Page<MenuInfoVo> getPage(Long pageNum, Long pageSize, MenuInfoVo menuInfoVo);

    List<MenuInfoVo> getList(MenuInfoVo menuInfoVo);

    MenuInfoVo queryMenuInfo(String id);

    MenuInfoVo addMenuInfo(MenuInfoVo menuInfoVo);

    MenuInfoVo updateMenuInfo(MenuInfoVo menuInfoVo);

    Boolean deleteMenuInfo(String ids);
}
