package com.alex.user.utils.security;

import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.enums.EStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SpringSecurity 用户工厂类
 *
 * @author majf
 * date 2020年9月19日20:03:25
 */
public final class SecurityUserFactory {

    private SecurityUserFactory() {
    }

    /**
     * 通过管理员Admin，生成一个SpringSecurity用户
     *
     * @param tUserVo
     * return
     */
    public static SecurityUser create(TUserVo tUserVo) {
        return Optional.ofNullable(tUserVo).map(item -> new SecurityUser(
                tUserVo.getId(),
                tUserVo.getUsername(),
                tUserVo.getPassword(),
                EStatus.ENABLE.getCode().toString().equals(tUserVo.getStatus()),
                mapToGrantedAuthorities(null)
        )).orElse(null);
    }

    private static List<GrantedAuthority> mapToGrantedAuthorities(List<String> authorities) {
        if (authorities == null || authorities.isEmpty()) {
            return null;
        }
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

}
