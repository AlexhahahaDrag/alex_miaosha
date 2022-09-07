package com.alex.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 *description:  springSecurity中的用户实体类
 *author:       alex
 *createDate:   2021/7/25 14:23
 *version:      1.0.0
 */
public class SecurityUser implements UserDetails {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final Long id;

    private final String username;

    private final String password;

    private final boolean enabled;

    private final Collection<? extends GrantedAuthority> authorities;

    public SecurityUser(Long id, String username, String password, boolean enabled, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    //账户是否未过期
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    //账户是否未锁定
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    //密码是否未过期
    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    //账户是否激活
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public Long getId() {
        return id;
    }
}
