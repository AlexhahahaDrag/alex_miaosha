package com.alex.mission.service.impl;

import com.alex.common.obj.SeckillMessage;
import com.alex.common.pojo.vo.WelcomeVo;
import com.alex.mission.manager.GoodsManager;
import com.alex.mission.manager.OrderManager;
import com.alex.mission.manager.SeckillGoodsManager;
import com.alex.mission.service.WelcomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WelcomeServiceImpl implements WelcomeService {

    private final GoodsManager goodsManager;

    private final SeckillGoodsManager seckillGoodsManager;

    private final OrderManager orderManager;

    @Override
    public WelcomeVo welcomeCount() {
        long goodsCount = goodsManager.count();
        long seckillGoodsCount = seckillGoodsManager.count();
        long orderCount = orderManager.count();
        return WelcomeVo.builder().goodsCount(goodsCount).seckillCount(seckillGoodsCount).orderCount(orderCount).build();
    }
}
