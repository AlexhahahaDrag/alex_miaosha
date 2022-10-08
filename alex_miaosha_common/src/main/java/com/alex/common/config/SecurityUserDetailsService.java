package com.alex.common.config;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 将SpringSecurity中的用户管理和数据库的管理员对应起来
 *
 * @author 陌溪
 * @date 2020/9/14 10:43
 */
@Service
public class SecurityUserDetailsService implements UserDetailsService {

//    @Autowired
//    private AdminService adminService;
//
//    @Autowired
//    private RoleService roleService;

    /**
     * @param username 浏览器输入的用户名【需要保证用户名的唯一性】
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
// TODO: 2022/9/26 添加权限认证
//        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq(SQLConf.USERNAME, username);
//        queryWrapper.last(SysConf.LIMIT_ONE);
//        Admin admin = adminService.getOne(queryWrapper);
//        if (admin == null) {
//            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
//        } else {
//            //查询出角色信息封装到admin中
//            List<String> roleNames = new ArrayList<>();
//            Role role = roleService.getById(admin.getRoleId());
//            roleNames.add(role.getRoleName());
//            admin.setRoleNames(roleNames);
//            return SecurityUserFactory.create(admin);
//        }
        return null;
    }
}
