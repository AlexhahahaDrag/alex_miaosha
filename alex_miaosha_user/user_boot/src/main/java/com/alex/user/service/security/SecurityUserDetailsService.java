package com.alex.user.service.security;

import com.alex.api.user.security.SecurityUserFactory;
import com.alex.api.user.vo.user.TUserVo;
import com.alex.user.service.user.TUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: alex
 * @createDate: 2023/1/28 22:49
 * @version: 1.0.0
 */
@RequiredArgsConstructor
@Service
public class SecurityUserDetailsService implements UserDetailsService {

    private final TUserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TUserVo user = userService.getUserByUsername(username);
        return SecurityUserFactory.create(user);
    }
}
