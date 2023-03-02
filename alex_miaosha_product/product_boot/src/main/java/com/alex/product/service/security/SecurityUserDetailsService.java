package com.alex.product.service.security;

import com.alex.api.user.api.UserApi;
import com.alex.api.user.security.SecurityUserFactory;
import com.alex.api.user.vo.user.TUserVo;
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

    private final UserApi userApi;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TUserVo user = userApi.getUserByUsername(username);
        return SecurityUserFactory.create(user);
    }
}
