package com.alex.mission.manager.impl;

import com.alex.mission.manager.OrderManager;
import com.alex.mission.mapper.OrderMapper;
import com.alex.mission.pojo.entity.Order;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OrderManagerImpl extends ServiceImpl<OrderMapper, Order> implements OrderManager {
}
