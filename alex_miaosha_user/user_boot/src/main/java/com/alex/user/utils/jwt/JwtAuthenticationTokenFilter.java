package com.alex.user.utils.jwt;

import com.alex.user.service.user.TUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * description:
 * author: alex
 * createDate: 2023/1/28 22:10
 * version: 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final TUserService userService;

    private final Audience audience;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //得到请求头信息authorization信息
        String tokenUuid = request.getHeader(audience.getTokenHeader());
        userService.authToken(tokenUuid);
        filterChain.doFilter(request, response);
    }
}
