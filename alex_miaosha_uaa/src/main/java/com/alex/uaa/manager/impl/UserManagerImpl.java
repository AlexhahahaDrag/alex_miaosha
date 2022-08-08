package com.alex.uaa.manager.impl;

import com.alex.uaa.manager.UserManager;
import com.alex.uaa.mapper.UserMapper;
import com.alex.uaa.pojo.entity.User;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserManagerImpl extends ServiceImpl<UserMapper, User> implements UserManager {
}
