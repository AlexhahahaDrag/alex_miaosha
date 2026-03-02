package com.alex.user.menuInfo.mapper;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.user.menuInfo.entity.MenuInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * description:  菜单管理表mapper
 * author:       alex
 * createDate:   2023-12-19 17:34:23
 * version:      1.0.0
 */
@Mapper
public interface MenuInfoMapper extends BaseMapper<MenuInfo> {

    Page<MenuInfoVo> getPage(Page<MenuInfoVo> page, @Param("menuInfoVo") MenuInfoVo menuInfoVo);

    List<MenuInfoVo> getList(@Param("menuInfoVo") MenuInfoVo menuInfoVo);

    MenuInfoVo queryMenuInfo(@Param("id") String id);
}
