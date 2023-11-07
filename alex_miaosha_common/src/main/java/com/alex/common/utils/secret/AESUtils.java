package com.alex.common.utils.secret;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @description:  Java使用AES加密算法进行加密解密
 * @author:       majf
 * @createDate:   2023/9/12 11:51
 * @version:      1.0.0
 */
public class AESUtils {

    private AESUtils () {

    }

    private static Cipher getCipher(String key, String iv, String type) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/CBC/" + type);
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
        IvParameterSpec ips = new IvParameterSpec(iv.getBytes());
        // 初始化为加密模式，并将密钥注入到算法中
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ips);
        return cipher;
    }

    /**
     * AES算法加密
     *
     * @Param:text原文
     * @Param:key密钥
     */
    public static String encrypt(String text, String key, String iv, String type) throws Exception {
        Cipher cipher = getCipher(key, iv, type);
        // 将传入的文本加密
        byte[] encrypted = cipher.doFinal(text.getBytes());
        // 将密文进行Base64编码，方便传输
//        return Base64.getEncoder().encodeToString(encrypted);
        return new String(encrypted);
    }

    /**
     * AES算法解密
     *
     * @Param:base64Encrypted密文
     * @Param:key密钥
     */
    public static String decrypt(String base64Encrypted, String key, String iv, String type) throws Exception {
        Cipher cipher = getCipher(key, iv, type);
        // 将Base64编码的密文解码
//        byte[] encrypted = Base64.getDecoder().decode(base64Encrypted);
        // 解密
        byte[] decrypted = cipher.doFinal(base64Encrypted.getBytes());
        return new String(decrypted);
    }
    public static void main(String[] args) throws Exception {
        //明文
        String text = "123444444444444444";
        //秘钥(需要使用长度为16、24或32的字节数组作为AES算法的密钥，否则就会遇到java.security.InvalidKeyException异常)
        String key = "20230610HelloDog";
        String iv = "1234567890123456";
        String type = "PKCS5Padding";
        //加密，生成密文
        String base64Encrypted = encrypt(text, key, iv, type);
        System.out.println(base64Encrypted);
        //解密，获取明文
        String text2 = decrypt(base64Encrypted, key, iv, type);
        System.out.println(text2);
    }
}
