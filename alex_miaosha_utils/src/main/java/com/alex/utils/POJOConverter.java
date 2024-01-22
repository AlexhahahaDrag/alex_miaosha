package com.alex.utils;

import com.alex.common.pojo.dto.GoodsDTO;
import com.alex.common.pojo.dto.SeckillGoodsDTO;

/**
 *description:  pojo 转化工具
 *author:       majf
 *createDate:   2022/7/12 16:42
 *version:      1.0.0
 */
public class POJOConverter {

    public static SeckillGoodsDTO converter(GoodsDTO goodsDTO) {
        SeckillGoodsDTO seckillGoodsDTO = new SeckillGoodsDTO();
        seckillGoodsDTO.setGoodsId(goodsDTO.getId());
        seckillGoodsDTO.setGoodsStock(goodsDTO.getGoodsStock());
        return seckillGoodsDTO;
    }
}
