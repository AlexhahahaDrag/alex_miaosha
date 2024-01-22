package com.alex.mission.manager.impl;

import com.alex.common.pojo.dto.SeckillGoodsDTO;
import com.alex.mission.mapper.SeckillGoodsMapper;
import com.alex.mission.pojo.entity.SeckillGoods;
import com.alex.mission.manager.SeckillGoodsManager;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeckillGoodsManagerImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods> implements SeckillGoodsManager {

    private final SeckillGoodsMapper seckillGoodsMapper;

    @Override
    public boolean deleteSeckillGoods(Long goodsId) {
        seckillGoodsMapper.delete(Wrappers.<SeckillGoods>lambdaUpdate().eq(SeckillGoods::getGoodsId, goodsId));
        return true;
    }

    @Override
    public boolean addSeckillGoods(SeckillGoodsDTO seckillGoodsDTO) {
        SeckillGoods seckillGoods = getSeckillGoodsDTOToSeckillGoods(seckillGoodsDTO);
        seckillGoodsMapper.insert(seckillGoods);
        return true;
    }

    @Override
    public boolean updateSeckillGoods(SeckillGoodsDTO seckillGoodsDTO) {
        SeckillGoods seckillGoods = getSeckillGoodsDTOToSeckillGoods(seckillGoodsDTO);
        seckillGoodsMapper.update(seckillGoods, Wrappers.<SeckillGoods>lambdaUpdate().eq(SeckillGoods::getGoodsId, seckillGoods.getGoodsId()));
        return true;
    }

    private SeckillGoods getSeckillGoodsDTOToSeckillGoods(SeckillGoodsDTO seckillGoodsDTO, String... ignore) {
        SeckillGoods seckillGoods = new SeckillGoods();
        BeanUtils.copyProperties(seckillGoodsDTO, seckillGoods, ignore);
        return seckillGoods;
    }
}
