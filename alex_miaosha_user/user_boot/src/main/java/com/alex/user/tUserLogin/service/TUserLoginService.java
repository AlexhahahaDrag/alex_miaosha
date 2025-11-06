package com.alex.user.tUserLogin.service;

import com.alex.api.user.vo.tUserLogin.TUserLoginVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.user.tUserLogin.entity.TUserLogin;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户登录表服务类
 * author: alex
 * createDate: 2023-02-16 14:11:55
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface TUserLoginService extends IService<TUserLogin> {

    Page<TUserLoginVo> getPage(Long pageNum, Long pageSize, TUserLoginVo tUserLoginVo);

    TUserLoginVo queryTUserLogin(String id);

    Boolean addTUserLogin(TUserLoginVo tUserLoginVo);

    Boolean updateTUserLogin(TUserLoginVo tUserLoginVo);

    Boolean deleteTUserLogin(String ids);
}
