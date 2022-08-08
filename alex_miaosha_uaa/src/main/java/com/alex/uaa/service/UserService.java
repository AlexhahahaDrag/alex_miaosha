package com.alex.uaa.service;

import com.alex.base.common.Result;
import com.alex.uaa.pojo.vo.LoginParam;
import com.alex.uaa.pojo.vo.RegisterParam;
import com.alex.uaa.pojo.vo.UpdatePasswordParam;

import javax.servlet.http.HttpServletRequest;

/**
 * @description:  user 服务
 * @author:       majf
 * @createDate:   2022/8/8 15:44
 * @version:      1.0.0
 */
public interface UserService {

    /**
     * @param loginParam
     * @description: 登录
     * @author:      majf
     * @return:      com.alex.base.common.Result<java.lang.String>
    */
    Result<String> doLogin(LoginParam loginParam);

    /**
     * @param request
     * @description: 退出
     * @author:      majf
     * @return:      com.alex.base.common.Result<java.lang.String>
    */
    Result<String> doLogout(HttpServletRequest request);

    /**
     * @param registerParam
     * @description: 注册
     * @author:      majf
     * @return:      com.alex.base.common.Result<com.alex.base.enums.ResultEnum>
    */
    Result doRegister(RegisterParam registerParam);

    /**
     * @param updatePasswordParam
     * @description: 修改密码
     * @author:      majf
     * @return:      com.alex.base.common.Result<com.alex.base.enums.ResultEnum>
    */
    Result updatePassword(UpdatePasswordParam updatePasswordParam, HttpServletRequest request);
}
