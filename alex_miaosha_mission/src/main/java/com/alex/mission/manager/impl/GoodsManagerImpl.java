package com.alex.mission.manager.impl;

import com.alex.mission.mapper.GoodsMapper;
import com.alex.mission.pojo.entity.Goods;
import com.alex.mission.manager.GoodsManager;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class GoodsManagerImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsManager {

}
