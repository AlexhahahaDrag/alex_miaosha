package com.alex.finance.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

//import com.alex.user.entity.user.TUser;

/**
 * @description:
 * @author: alex
 * @createDate: 2022/12/27 22:31
 * @version: 1.0.0
 */
@Service
@RequiredArgsConstructor
public class SecurityUserDetailsService implements UserDetailsService {

//    private final TUserService tUserService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
//        QueryWrapper<TUser> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq(SysConf.USERNAME, username);
//        queryWrapper.last(SysConf.LIMIT_ONE);
//        TUser admin = tUserService.getOne(queryWrapper);
//        if (admin == null) {
//            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
//        } else {
//            //查询出角色信息封装到admin中
////            List<String> roleNames = new ArrayList<>();
////            Role role = roleService.getById(admin.getRoleId());
////            roleNames.add(role.getRoleName());
////            admin.setRoleNames(roleNames);
//            return SecurityUserFactory.create(admin);
//        }
    }
}
