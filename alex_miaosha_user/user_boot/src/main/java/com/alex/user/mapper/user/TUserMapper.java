package com.alex.user.mapper.user;

import com.alex.api.user.annotation.DataPermission;
import com.alex.user.entity.user.TUser;
import com.alex.api.user.vo.user.TUserVo;
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

    @DataPermission()
    Page<TUserVo> getPage(Page<TUserVo> page, @Param("tUserVo") TUserVo tUserVo);

    TUserVo queryTUser(@Param("id") String id);

    List<TUserVo> getList(@Param("tUserVo") TUserVo tUserVo);

    TUserVo getUserInfo(@Param("tUserVo") TUserVo tUserVo);
}
