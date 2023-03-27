package com.alex.user.utils.jwt;

import com.alex.base.constants.SysConf;
import com.alex.user.utils.security.SecurityUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * description:  jwt工具类
 * author:       alex
 * createDate:   2021/7/25 13:46
 * version:      1.0.0
 */
@Component
@Slf4j
public class JwtTokenUtils {


    /**
     * @param token
     * @param base64Security
     * @description: 解析jwt
     * @author: alex
     * @return: io.jsonwebtoken.Claims
     */
    public Claims parseJwt(String token, String base64Security) {
        try {
            return Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(base64Security)).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            log.error("解析token异常:{}", e.getMessage());
            return null;
        }
    }

    /*
     * @param username       用户名
     * @param adminId        用户id
     * @param roleName       角色
     * @param audience       jwt配置
     * @param issuer         发行人
     * @param TTLMillis      过期时间
     * @param base64Security
     * @description:  生成jwt
     * @author:       alex
     * @return:       java.lang.String
     */
    public String createJwt(String username, Long adminId, String roleName,
                            String audience, String issuer, long TTLMillis, String base64Security) {
        //HS256是一种对称算法，双方之间仅共享一个密钥
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        LocalDateTime now = LocalDateTime.now();
        //生成签名密钥
        byte[] base64Binary = DatatypeConverter.parseBase64Binary(base64Security);
        Key signKey = new SecretKeySpec(base64Binary, signatureAlgorithm.getJcaName());
        //添加构成jwt参数
        JwtBuilder builder = Jwts.builder().setHeaderParam("type", "JWT")
                .claim(SysConf.ADMIN_ID, adminId)
                .claim(SysConf.ROLE, roleName)
                .claim(SysConf.CREATE_TIME, new Date())
                .setSubject(username)
                .setIssuer(issuer)
                .setAudience(audience)
                .signWith(signatureAlgorithm, signKey);
        long nowMillis = System.currentTimeMillis();
        //添加过期时间
        if (TTLMillis >= 0) {
            builder.setExpiration(new Date(nowMillis + TTLMillis));
        }
        //生成jwt
        return builder.compact();
    }

    /**
     * @param token
     * @param base64Security
     * @description: 判断token是否过期
     * @author: alex
     * @return: boolean
     */
    public boolean isExpiration(String token, String base64Security) {
        Claims claims = parseJwt(token, base64Security);
        if (claims == null) {
            return true;
        } else {
            return claims.getExpiration().before(new Date());
        }
    }

    /**
     * @param token
     * @param userDetails
     * @param base64Security
     * @description: 校验token是否有效
     * @author: alex
     * @return: java.lang.Boolean
     */
    public Boolean validateToken(String token, UserDetails userDetails, String base64Security) {
        SecurityUser securityUser = (SecurityUser) userDetails;
        final String username = getUsername(token, base64Security);
        return username.equals(securityUser.getUsername()) && !isExpiration(token, base64Security);
    }

    /**
     * @param token
     * @param base64Security
     * @description: 获取用户名
     * @author: alex
     * @return: java.lang.String
     */
    public String getUsername(String token, String base64Security) {
        return parseJwt(token, base64Security).getSubject();
    }

    /**
     * 从token中获取过期时间
     *
     * @param token
     * @param base64Security
     * @return
     */
    public Date getExpiration(String token, String base64Security) {
        return parseJwt(token, base64Security).getExpiration();
    }

    /**
     * @param token
     * @param base64Security
     * @description: 获取用户id
     * @author: alex
     * @return: java.lang.String
     */
    public Long getUserId(String token, String base64Security) {
        return parseJwt(token, base64Security).get(SysConf.ADMIN_ID, Long.class);
    }

    /**
     * @param token
     * @param base64Security
     * @param TTLMillis
     * @description: 刷新token
     * @author: alex
     * @return: java.lang.String
     */
    public String refreshToken(String token, String base64Security, long TTLMillis) {
        String refreshedToken = null;
        if (!isExpiration(token, base64Security)) {
            try {
                Claims claims = parseJwt(token, base64Security);
                refreshedToken = createJwt(claims.getSubject(), claims.get(SysConf.ADMIN_ID, Long.class),
                        claims.get(SysConf.ROLE, String.class),
                        claims.getAudience(), claims.getIssuer(), TTLMillis, base64Security);
            } catch (Exception e) {
                log.error("刷新token报错：{}", e.getMessage());
                return null;
            }
        }
        return refreshedToken;
    }
}
