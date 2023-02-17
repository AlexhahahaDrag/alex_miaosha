package com.alex.common.handler;

import com.alex.base.constants.SysConf;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * description:  请求持有类
 * author:       alex
 * createDate:   2021/7/14 7:13
 * version:      1.0.0
 */
@Slf4j
public class RequestHolder {

    /**
     * @description: 获取request
     * @author: alex
     * @return: javax.servlet.http.HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        log.debug("getRequest: Thread id: {}, name: {}", Thread.currentThread().getId(), Thread.currentThread().getName());
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes == null ? null : requestAttributes.getRequest();
    }

    /**
     * @description: 获取response
     * @author: alex
     * @return: javax.servlet.http.HttpServletResponse
     */
    public static HttpServletResponse getResponse() {
        log.debug("getResponse: Thread id: {}, name: {}", Thread.currentThread().getId(), Thread.currentThread().getName());
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes == null ? null : requestAttributes.getResponse();
    }

    /**
     * @description: 获取session
     * @author: alex
     * @return: javax.servlet.http.HttpSession
     */
    public static HttpSession getSession() {
        log.debug("getSession: Thread id: {}, name: {}", Thread.currentThread().getId(), Thread.currentThread().getName());
        HttpServletRequest request;
        if ((request = getRequest()) == null) {
            return null;
        }
        return request.getSession();
    }

//    /**
//     * @param name
//     * @description:  根据名字获取session的attribute
//     * @author:       alex
//     * @return:       java.lang.Object
//    */
//    public static Object getSession(String name) {
//        log.debug("getSession -- Thread id :{}, name : {}", Thread.currentThread().getId(), Thread.currentThread().getName());
//        ServletRequestAttributes servletRequestAttributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
//        if (null == servletRequestAttributes) {
//            return null;
//        }
//        return servletRequestAttributes.getAttribute(name, RequestAttributes.SCOPE_SESSION);
//    }
//
//    /**
//     * 添加session
//     *
//     * @param name
//     * @param value
//     */
//    public static void setSession(String name, Object value) {
//        log.debug("setSession -- Thread id :{}, name : {}", Thread.currentThread().getId(), Thread.currentThread().getName());
//        ServletRequestAttributes servletRequestAttributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
//        if (null == servletRequestAttributes) {
//            return;
//        }
//        servletRequestAttributes.setAttribute(name, value, RequestAttributes.SCOPE_SESSION);
//    }
//
//    /**
//     * 清除指定session
//     *
//     * @param name
//     * @return void
//     */
//    public static void removeSession(String name) {
//        log.debug("removeSession -- Thread id :{}, name : {}", Thread.currentThread().getId(), Thread.currentThread().getName());
//        ServletRequestAttributes servletRequestAttributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
//        if (null == servletRequestAttributes) {
//            return;
//        }
//        servletRequestAttributes.removeAttribute(name, RequestAttributes.SCOPE_SESSION);
//    }
//
//    /**
//     * 获取所有session key
//     *
//     * @return String[]
//     */
//    public static String[] getSessionKeys() {
//        log.debug("getSessionKeys -- Thread id :{}, name : {}", Thread.currentThread().getId(), Thread.currentThread().getName());
//        ServletRequestAttributes servletRequestAttributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
//        if (null == servletRequestAttributes) {
//            return null;
//        }
//        return servletRequestAttributes.getAttributeNames(RequestAttributes.SCOPE_SESSION);
//    }
//

    /**
     * @description: 获取adminId
     * @author: alex
     * @return: java.lang.String
     */
    public static Long getAdminId() {
        HttpServletRequest request = getRequest();
        return (Long) request.getAttribute(SysConf.ADMIN_ID);
    }

    /*
     * @description:  获取token
     * @author:       alex
     * @return:       java.lang.String
     */
    public static String getAdminToken() {
        HttpServletRequest request = getRequest();
        return (String) request.getAttribute(SysConf.TOKEN);
    }

    /**
     * @description:  检查用户是否登录
     * @author:       alex
     * @return:       java.lang.String
    */
    public static Long checkLogin() {
        Long adminId = getAdminId();
        if (adminId == null) {
            log.error("用户未登录");
            throw new UserException(ResultEnum.USER_NO_LOGIN);
        }
        return adminId;
    }
}
