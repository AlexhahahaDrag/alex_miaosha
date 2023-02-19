package com.alex.api.user.utils.jwt;

import cn.hutool.json.JSONUtil;
import com.alex.api.user.vo.user.OnlineAdmin;
import com.alex.base.constants.SysConf;
import com.alex.common.redis.key.LoginKey;
import com.alex.common.utils.date.DateUtils;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.common.utils.string.StringUtils;
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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: alex
 * @createDate: 2023/1/28 22:10
 * @version: 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final Audience audience;

    private final JwtTokenUtils jwtTokenUtils;

    private final UserDetailsService userDetailsService;

    private final RedisUtils redisUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //得到请求头信息authorization信息
        String tokenUuid = request.getHeader(audience.getTokenHeader());
        // 私钥
        String base64Secret = audience.getBase64Secret();
        String barToken = redisUtils.get(LoginKey.loginUuid, tokenUuid);
        if (StringUtils.isEmpty(barToken) || !barToken.startsWith(audience.getTokenHead())) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = barToken.substring(audience.getTokenHead().length());
        //校验token
        if (StringUtils.isEmpty(token) || jwtTokenUtils.isExpiration(token, base64Secret)) {
            filterChain.doFilter(request, response);
            return;
        }
        // TODO: 2023/2/16 校验token是否正确
        Date expirationDate = jwtTokenUtils.getExpiration(token, base64Secret);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 得到两个日期相差的间隔，秒
        long survivalSecond = DateUtils.diffSecondByTwoDays(sdf.format(expirationDate), DateUtils.getTimeStr(LocalDateTime.now()));
        // 而旧的Token将会在不久之后从Redis中过期,当存活时间小于更新时间，那么将颁发新的Token到客户端，同时重置新的过期时间
        if (survivalSecond < audience.getExpiresSecond()) {
            //生成新的token
            String newToken = audience.getTokenHeader() + jwtTokenUtils.refreshToken(token, base64Secret, audience.getExpiresSecond() * 1000);
            redisUtils.setEx(LoginKey.loginUuid, tokenUuid, newToken, audience.getExpiresSecond(), TimeUnit.SECONDS);
            String onlineAdminStr = redisUtils.get(LoginKey.loginToken, token);
            if (StringUtils.isNotBlank(onlineAdminStr)) {
                OnlineAdmin onlineAdmin = JSONUtil.toBean(onlineAdminStr, OnlineAdmin.class);
                onlineAdmin.setToken(newToken);
                redisUtils.setEx(LoginKey.loginToken, newToken, JSONUtil.toJsonStr(onlineAdmin), audience.getExpiresSecond(), TimeUnit.SECONDS);
            }
        }
        // 获取在线的管理员信息
        Long adminId = jwtTokenUtils.getUserId(token, base64Secret);
        String username = jwtTokenUtils.getUsername(token, base64Secret);
        request.setAttribute(SysConf.ADMIN_ID, adminId);
        request.setAttribute(SysConf.USERNAME, username);
        request.setAttribute(SysConf.TOKEN, token);
        log.info("解析出来用户: {}", username);
        log.info("解析出来的用户Uid: {}", adminId);
        SecurityContextHolder.getContext().getAuthentication();
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
        filterChain.doFilter(request, response);
    }
}
