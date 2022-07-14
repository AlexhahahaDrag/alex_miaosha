package com.alex.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;

/**
 *description:  国密sm3加密解密工具
 *
 *
 * Hutool针对Bouncy Castle做了简化包装，用于实现国密算法中的SM2、SM3、SM4。
 *
 * 国密算法工具封装包括：
 *
 * 非对称加密和签名：SM2
 * 摘要签名算法：SM3
 * 对称加密：SM4
 * 国密算法需要引入Bouncy Castle库的依赖。
 *author:       majf
 *createDate:   2022/7/14 17:14
 *version:      1.0.0
 */
public class SM3Utils {

    public static String sm3(String str) {
        return SmUtil.sm3(str);
    }

    private static final String salt = "3a41dx1d";

    public static String inputPassToPass(String inputPass) {
        String str = salt.charAt(0) + salt.charAt(2) + inputPass + salt.charAt(5) + salt.charAt(4);
        return sm3(str);
    }

    public static String fromPassToDBPass(String fromPass, String salt) {
        String str = salt.charAt(0) + salt.charAt(2) + fromPass + salt.charAt(5) + salt.charAt(4);
        return sm3(str);
    }

    // TODO: 2022/7/14 报错 
    public static void main(String[] args) {
        String text = "我是一段测试aaaa";

        SM2 sm2 = SmUtil.sm2();
        // 公钥加密，私钥解密
        String encryptStr = sm2.encryptBcd(text, KeyType.PublicKey);
        String decryptStr = StrUtil.utf8Str(sm2.decryptFromBcd(encryptStr, KeyType.PrivateKey));
        System.out.println(decryptStr);
    }
}
