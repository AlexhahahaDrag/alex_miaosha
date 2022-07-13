package com.alex.common.enums;

/**
 *description:  结果枚举
 *author:       majf
 *createDate:   2022/7/4 17:36
 *version:      1.0.0
 */
public enum ResultEnum implements BaseEnum {

    SUCCESS("200", "success"),
    PARAM_ERROR("400", "param_fail"),
    SYSTEM_ERROR("500", "system_fail"),
    //商品模块
    GOODS_CREATE_ERROR("500", "商品创建失败"),
    GOODS_UPDATE_ERROR("500", "商品更新失败"),
    GOODS_NOT_EXISTS("500", "商品不存在"),
    //订单模块
    ORDER_NOT_EXISTS("60001", "订单不存在"),
    //文件模块
    IMAGE_UPLOAD_FAIL("502", "上传图片失败"),
    IMAGE_DELETE_FAIL("503", "删除图片失败"),
    IMAGE_VIOLATION_FAIL("504", "图片违规"),
    IMAGE_ENUM_NOT_FOUND("501", "上传的类型不正确"),
    //用户模块
    MOBILE_NOT_EXIST ("500214", "手机号不存在"),
    PASSWORD_ERROR("500215", "密码错误"),
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
