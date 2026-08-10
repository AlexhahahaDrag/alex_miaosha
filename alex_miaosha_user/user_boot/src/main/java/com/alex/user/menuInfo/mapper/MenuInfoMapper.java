package com.alex.user.menuInfo.mapper;

import com.alex.api.user.annotation.DataPermission;
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

    @DataPermission(table = "t_menu_info", field = "operator", scope = DataPermission.Scope.USER_IDS)
    Page<MenuInfoVo> getPage(Page<MenuInfoVo> page, @Param("menuInfoVo") MenuInfoVo menuInfoVo);

    @DataPermission(table = "t_menu_info", field = "operator", scope = DataPermission.Scope.USER_IDS)
    List<MenuInfoVo> getList(@Param("menuInfoVo") MenuInfoVo menuInfoVo);

    /**
     * RBAC C1 fix (batch2 final review): 与 {@link #getList} 同 SQL，但**有意不加** {@code @DataPermission}。
     * 仅供 {@code MenuInfoServiceImp#getList} 的全量菜单树缓存路径（写入/命中全局 Redis 键
     * {@code menu_all_tree}）调用。该缓存全局共享、供所有登录用户与登录构建上下文复用，
     * 绝不能按调用者的数据范围被截断——否则会污染全局缓存（见批次2终审 C1）。
     * 严禁把这个方法接到任何按调用者身份返回结果的管理端接口上。
     */
    List<MenuInfoVo> getListAll(@Param("menuInfoVo") MenuInfoVo menuInfoVo);

    @DataPermission(table = "t_menu_info", field = "operator", scope = DataPermission.Scope.USER_IDS)
    MenuInfoVo queryMenuInfo(@Param("id") String id);
}
