package com.alex.user.manager.impl;

import com.alex.user.manager.UserManager;
import com.alex.user.mapper.UserMapper;
import com.alex.user.pojo.entity.User;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserManagerImpl extends ServiceImpl<UserMapper, User> implements UserManager {
}
