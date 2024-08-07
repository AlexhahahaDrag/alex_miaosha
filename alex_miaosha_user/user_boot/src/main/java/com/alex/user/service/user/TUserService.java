package com.alex.user.service.user;

import com.alex.api.user.vo.user.TUserVo;
import com.alex.base.common.Result;
import com.alex.user.entity.tUserLogin.TUserLogin;
import com.alex.user.entity.user.TUser;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import me.zhyd.oauth.request.AuthRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 管理员表 服务类
 *
 * author: alex
 * createDate: 2022-12-26 17:20:38
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface TUserService extends IService<TUser> {

    Page<TUserVo> getPage(Long pageNum, Long pageSize, TUserVo tUserVo) throws Exception;

    TUserVo queryTUser(String id);

    TUser addTUser(TUserVo tUserVo);

    TUser updateTUser(TUserVo tUserVo);

    Boolean deleteTUser(String ids);

    Map<String, Object> login(HttpServletRequest request, String username, String password, Boolean isRemember) throws Exception;

    List<TUserVo> getList(TUserVo tUserVo);

    TUserVo getUserByUsername(String username);

    Result<Boolean> logout(HttpServletRequest request);

    void addOnLineAdmin(TUserLogin userLogin, long expiration) throws Exception;

    /**
     * @param token
     * description: 校验token
     * author:      alex
     * return:      java.lang.Boolean
    */
    Boolean authToken(String token);

    AuthRequest getAuthRequest(String appName);
}
