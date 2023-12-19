package com.alex.user.service.menuInfo;

import com.alex.api.user.vo.menuInfo.MenuInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.user.entity.menuInfo.MenuInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 菜单管理表 服务类
 * @author: alex
 * @createDate: 2023-12-19 17:34:23
 * @description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface MenuInfoService extends IService<MenuInfo> {

    Page<MenuInfoVo> getPage(Long pageNum, Long pageSize, MenuInfoVo menuInfoVo);

    List<MenuInfoVo> getList(MenuInfoVo menuInfoVo);

    MenuInfoVo queryMenuInfo(String id);

    Boolean addMenuInfo(MenuInfoVo menuInfoVo);

    Boolean updateMenuInfo(MenuInfoVo menuInfoVo);

    Boolean deleteMenuInfo(String ids);
}
