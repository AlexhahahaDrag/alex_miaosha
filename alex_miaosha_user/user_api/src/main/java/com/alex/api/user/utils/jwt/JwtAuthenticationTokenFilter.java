package com.alex.api.user.utils.jwt;

import com.alex.base.constants.SysConf;
import com.alex.common.redis.key.UserKey;
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
        String authHeader = request.getHeader(audience.getTokenHeader());

        if (authHeader != null && authHeader.startsWith(audience.getTokenHead())) {
            log.error("传递过来的token为: {}", authHeader);
            final String token = authHeader.substring(audience.getTokenHead().length());
            // 私钥
            String base64Secret = audience.getBase64Secret();
            
            Long adminId = jwtTokenUtils.getUserId(token, base64Secret);
            // TODO: 2023/2/9 修改为token
            String s = redisUtils.get(UserKey.getById, adminId.toString());
            //校验token
            if (StringUtils.isEmpty(s) || jwtTokenUtils.isExpiration(token, base64Secret)) {
                filterChain.doFilter(request, response);
                return;
            }
            // 获取在线的管理员信息
            String username = jwtTokenUtils.getUsername(token, base64Secret);
            request.setAttribute(SysConf.ADMIN_ID, adminId);
            request.setAttribute(SysConf.USERNAME, username);
            request.setAttribute(SysConf.TOKEN, authHeader);

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
        }
        filterChain.doFilter(request, response);
    }
}
