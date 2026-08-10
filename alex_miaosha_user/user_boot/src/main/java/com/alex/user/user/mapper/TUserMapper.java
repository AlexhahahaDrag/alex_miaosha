package com.alex.user.user.mapper;

import com.alex.api.user.annotation.DataPermission;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.user.user.entity.TUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description:  管理员表 mapper
 * author:       alex
 * createDate:   2022-12-26 17:20:38
 * version:      1.0.0
 */
@Mapper
public interface TUserMapper extends BaseMapper<TUser> {

    @DataPermission(field = "id")
    Page<TUserVo> getPage(Page<TUserVo> page, @Param("tUserVo") TUserVo tUserVo);

    @DataPermission(field = "id")
    TUserVo queryTUser(@Param("id") String id);

    @DataPermission(field = "id")
    List<TUserVo> getList(@Param("tUserVo") TUserVo tUserVo);

    @DataPermission(field = "id")
    TUserVo getUserInfo(@Param("tUserVo") TUserVo tUserVo);
}
