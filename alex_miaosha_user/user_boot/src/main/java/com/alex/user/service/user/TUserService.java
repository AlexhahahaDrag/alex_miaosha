package com.alex.user.service.user;

import com.alex.api.user.vo.user.TUserVo;
import com.alex.base.common.Result;
import com.alex.user.entity.user.TUser;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 管理员表 服务类
 *
 * @author: alex
 * @createDate: 2022-12-26 17:20:38
 * @description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface TUserService extends IService<TUser> {

    Page<TUserVo> getPage(Long pageNum, Long pageSize, TUserVo tUserVo);

    TUserVo queryTUser(String id);

    TUser addTUser(TUserVo tUserVo);

    TUser updateTUser(TUserVo tUserVo);

    Boolean deleteTUser(String ids);

    Result<Object> login(HttpServletRequest request, String username, String password, Boolean isRemember);

    List<TUserVo> getList(TUserVo tUserVo);

    TUserVo getUserByUsername(String username);

    Result<Boolean> logout();
}
