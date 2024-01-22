package com.alex.mission.mapper;

import com.alex.common.pojo.dto.SeckillGoodsDTO;
import com.alex.mission.pojo.entity.SeckillGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {

    Page<SeckillGoodsDTO> findPage(Page<SeckillGoodsDTO> page, @Param(value = "goodsId") Long goodsId);
}
