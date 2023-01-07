package com.alex.user.filter;

import com.alex.base.constants.SysConf;
import com.alex.common.redis.key.UserKey;
import com.alex.utils.date.DateUtils;
import com.alex.utils.jwt.Audience;
import com.alex.utils.jwt.JwtTokenUtils;
import com.alex.utils.redis.RedisUtils;
import com.alex.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * description:
 * author:       majf
 * createDate:   2022/12/27 17:10
 * version:      1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final Audience audience;

    private final RedisUtils redisUtils;

    private final JwtTokenUtils jwtTokenUtils;

    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //得到请求头信息authorization信息
        String authHeader = request.getHeader(audience.getTokenHeader());

        if (authHeader != null && authHeader.startsWith(audience.getTokenHead())) {
            log.error("传递过来的token为: {}", authHeader);
            final String token = authHeader.substring(audience.getTokenHead().length());
            // 私钥
            String base64Secret = audience.getBase64Secret();
            // 获取在线的管理员信息
            if (!jwtTokenUtils.isExpiration(token, base64Secret)) {
                //得到过期时间
                Date expiration = jwtTokenUtils.getExpiration(token, base64Secret);
                Instant instant = expiration.toInstant();
                ZoneId zoneId = ZoneId.systemDefault();
                LocalDateTime expirationDate = LocalDateTime.ofInstant(instant, zoneId);
                LocalDateTime nowDate = LocalDateTime.now();
                // 得到两个日期相差的间隔，秒
                long survivalSecond = DateUtils.diffSecondByTwoDays(nowDate, expirationDate) / 1000;
                // 当存活时间小于更新时间，那么将颁发新的Token到客户端，同时重置新的过期时间
                // 而旧的Token将会在不久之后从Redis中过期
                if (survivalSecond <  audience.getRefreshSecond()) {
                    // 生成一个新的Token
                    String newToken = audience.getTokenHead() + jwtTokenUtils.refreshToken(token, base64Secret, audience.getExpiresSecond() * 1000);
                    // 生成新的token，发送到客户端
                    // 随机生成一个TokenUid，用于换取Token令牌
                    String tokenUid = StringUtils.getUUID();
                    // 将新token赋值，用于后续使用
                    authHeader = newToken;
                    // 维护 uuid - token 互相转换的Redis集合【主要用于在线用户管理】
                    redisUtils.expire(UserKey.getById, "aa",newToken, audience.getExpiresSecond());
                }
            } else {
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtTokenUtils.getUsername(token, base64Secret);
            Long adminId = jwtTokenUtils.getUserId(token, base64Secret);
            //把adminUid存储到request中
            String finalAuthHeader = authHeader;

            request.setAttribute(SysConf.ADMIN_ID, adminId);
            request.setAttribute(SysConf.USERNAME, username);
            request.setAttribute(SysConf.TOKEN, authHeader);

            log.info("解析出来用户: {}", username);
            log.info("解析出来的用户Uid: {}", adminId);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 通过用户名加载SpringSecurity用户
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                // 校验Token的有效性
                if (jwtTokenUtils.validateToken(token, userDetails, base64Secret)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    //以后可以security中取得SecurityUser信息
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
