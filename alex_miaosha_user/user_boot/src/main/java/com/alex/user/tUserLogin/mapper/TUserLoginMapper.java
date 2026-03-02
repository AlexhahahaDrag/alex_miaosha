package com.alex.user.tUserLogin.mapper;

import com.alex.api.user.tUserLogin.vo.TUserLoginVo;
import com.alex.user.tUserLogin.entity.TUserLogin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * description:  用户登录表mapper
 * author:       alex
 * createDate:   2023-02-16 14:11:55
 * version:      1.0.0
 */
@Mapper
public interface TUserLoginMapper extends BaseMapper<TUserLogin> {

    Page<TUserLoginVo> getPage(Page<TUserLoginVo> page, @Param("tUserLoginVo") TUserLoginVo tUserLoginVo);

    TUserLoginVo queryTUserLogin(@Param("id") String id);
}
