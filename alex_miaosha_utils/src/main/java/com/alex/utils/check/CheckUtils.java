package com.alex.utils.check;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 *description:  校验工具类
 *author:       alex
 *createDate:   2021/7/26 5:23
 *version:      1.0.0
 */
@Slf4j
public class CheckUtils {

    private static String CHECK_EMAIL_REGEX = "^(\\w+([-.][A-Za-z0-9]+)*){3,18}@\\w+([-.][A-Za-z0-9]+)*\\.\\w+([-.][A-Za-z0-9]+)*$";

    private static String CHECK_PHONE_REGEX = "/^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$";

    /**
     * @param email
     * description:  校验邮箱
     * author:       alex
     * return:       boolean
    */
    public static boolean checkEmail(String email) {
        return check(email, CHECK_EMAIL_REGEX);
    }

    /**
     * @param phone
     * description:  校验电话号
     * author:       alex
     * return:       boolean
    */
    public static boolean checkPhone(String phone) {
        return check(phone, CHECK_PHONE_REGEX);
    }

    /**
     * @param info
     * @param regex
     * description:  根据正则表达式校验信息
     * author:       alex
     * return:       boolean
    */
    public static boolean check(String info, String regex) {
        boolean res = false;
        try {
            Pattern compile = Pattern.compile(regex);
            res = compile.matcher(info).matches();
        } catch (Exception e) {
            log.error("校验邮箱失败，{}", e.getMessage());
            return false;
        }
        return res;
    }
}