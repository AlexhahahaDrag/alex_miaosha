package com.alex.user.mapper.tUserLogin;

import com.alex.user.entity.tUserLogin.TUserLogin;
import com.alex.api.user.vo.tUserLogin.TUserLoginVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

/**
 * description:  用户登录表 mapper
 * author:       alex
 * createDate:   2023-02-16 14:11:55
 * version:      1.0.0
 */
@Mapper
public interface TUserLoginMapper extends BaseMapper<TUserLogin> {

    Page<TUserLoginVo> getPage(Page<TUserLoginVo> page, @Param("tUserLoginVo") TUserLoginVo tUserLoginVo);

    TUserLoginVo queryTUserLogin(@Param("id") String id);
}
