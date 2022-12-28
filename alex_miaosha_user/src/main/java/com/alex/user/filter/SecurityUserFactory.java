package com.alex.user.filter;

import com.alex.common.enums.EStatus;
import com.alex.common.security.SecurityUser;
import com.alex.user.entity.user.TUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SpringSecurity用户工厂类
 *
 * @author 陌溪
 * @date 2020年9月19日20:03:25
 */
public final class SecurityUserFactory {

    private SecurityUserFactory() {
    }

    /**
     * 通过管理员Admin，生成一个SpringSecurity用户
     *
     * @param admin
     * @return
     */
    public static SecurityUser create(TUser admin) {
        boolean enabled = EStatus.ENABLE.getCode().equals(admin.getStatus());
        return new SecurityUser(
                admin.getId(),
                admin.getUsername(),
                admin.getPassword(),
                enabled,
                null
        );
    }

    private static List<GrantedAuthority> mapToGrantedAuthorities(List<String> authorities) {
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

}
