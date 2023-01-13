package com.alex.base.enums;

/**
 *description:  结果枚举
 *author:       majf
 *createDate:   2022/7/4 17:36
 *version:      1.0.0
 */
public enum ResultEnum implements BaseEnum {

    SUCCESS("200", "success"),
    PARAM_ERROR("400", "param_fail"),

    UNAUTHORIZED("403", "没有权限"),

    REPEAT_COMMIT("40010", "请勿重复提交表单!"),
    /**
     * 服务器模块
     */
    SYSTEM_ERROR("500", "系统异常"),
    SYSTEM_NO_AVAILABLE("500102", "系统服务不存在！"),
    SYSTEM_BUSY("500101", "服务器繁忙，请稍后再试"),

    ACCESS_LIMIT_REACHED("500103", "访问太频繁"),
    REQUIRE_ILLEGAL("500102", "请求非法"),
    ACCESS_TOO_MANY("500104", "当前访问的人数太多，其稍后尝试"),


    /**
     * 用户模块
     */
    MOBILE_NOT_EXIST ("500214", "手机号不存在"),
    PASSWORD_ERROR("500215", "密码错误"),
    EMPTY_USERNAME_OR_PASSWORD("500217", "账号密码不能为空!"),
    LOGIN_ERROR_MORE("500218", "密码输错次数过多，已被锁定30分钟"),
    NO_LOGIN("500216", "请先登录"),
    NO_USERNAME("500219", "用户名不能为空！"),
    NO_MOBILE_EMAIL("500220", "邮箱和手机号至少有一项不能为空!"),

    ERROR_USER_LIST("500221", "获取用户列表数据失败，请联系管理员！"),

    /**
     * 商品模块
     */
    GOODS_CREATE_ERROR("500300", "商品创建失败"),
    GOODS_UPDATE_ERROR("500301", "商品更新失败"),
    GOODS_NOT_EXISTS("500302", "商品不存在"),

    /**
     * 秒杀模块
     */
    SECKILL_OVER("500500", "商品已经秒杀完毕"),
    SECKILL_REPEAT("500501", "不能进行重复秒杀"),
    SECKILL_NO_START("500502", "秒杀还未开始！"),

    /**
     * 注册模块
     */
    REPEATED_REGISTER_MOBILE("500600", "手机号码已注册"),
    REPEATED_REGISTER_USERNAME("500601", "用户名已经被注册"),
    REPEATED_REGISTER_IDENTITY("500602", "身份证已经被注册"),


    /**
     * 订单模块
     */
    ORDER_NOT_EXISTS("60001", "订单不存在"),


    /**
     * 文件模块
     */
    IMAGE_UPLOAD_FAIL("502", "上传图片失败"),
    IMAGE_DELETE_FAIL("503", "删除图片失败"),
    IMAGE_VIOLATION_FAIL("504", "图片违规"),
    IMAGE_ENUM_NOT_FOUND("501", "上传的类型不正确"),

    SYSTEM_UP_ERROR("500", "系统更新中，请稍后尝试！"),

    test("11", "11")
    ;

    ResultEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    private String code;

    private String value;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getValue() {
        return value;
    }
}
