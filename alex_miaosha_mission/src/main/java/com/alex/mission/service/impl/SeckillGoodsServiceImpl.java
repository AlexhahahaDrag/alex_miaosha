package com.alex.mission.service.impl;

import com.alex.base.common.Result;
import com.alex.common.pojo.dto.SeckillGoodsDTO;
import com.alex.common.redis.key.SeckillGoodsKey;
import com.alex.common.redis.manager.RedisService;
import com.alex.mission.manager.SeckillGoodsManager;
import com.alex.mission.mapper.SeckillGoodsMapper;
import com.alex.mission.pojo.entity.SeckillGoods;
import com.alex.mission.service.SeckillGoodsService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    private final SeckillGoodsManager seckillGoodsManager;

    private final RedisService redisService;

    private final SeckillGoodsMapper seckillGoodsMapper;

    @Override
    public int reduceStock(Long goodsId) {
        SeckillGoods seckillGoods = seckillGoodsMapper.selectOne(Wrappers.<SeckillGoods>lambdaQuery().eq(SeckillGoods::getGoodsId, goodsId));
        // TODO: 2022/7/12 测试库存
        reduceStockCount(goodsId, seckillGoods);
        boolean update = seckillGoodsManager.update(seckillGoods, Wrappers.<SeckillGoods>lambdaUpdate().eq(SeckillGoods::getGoodsId, goodsId));
        return update ? 1 : 0;
    }

    @Override
    public Result<Page<SeckillGoodsDTO>> findSeckill(Integer page, Integer pageSize, Long goodsId) {
        Page<SeckillGoodsDTO> pageInfo = new Page<>(page, pageSize);
        Page<SeckillGoodsDTO> seckillGoodsPage = seckillGoodsMapper.findPage(pageInfo, goodsId);
        return Result.success(seckillGoodsPage);
    }

    // TODO: 2022/7/12 库存减1
    private void reduceStockCount(Long goodsId, SeckillGoods seckillGoods) {
        Integer stockCount = redisService.get(SeckillGoodsKey.seckillCount, "" + goodsId, Integer.class);
        seckillGoods.setGoodsStock(stockCount);
    }
}
