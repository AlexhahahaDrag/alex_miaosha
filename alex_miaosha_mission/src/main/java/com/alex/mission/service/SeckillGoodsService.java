package com.alex.mission.service;

import com.alex.base.common.Result;
import com.alex.common.pojo.dto.SeckillGoodsDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface SeckillGoodsService {

    /**
     * @param goodsId
     * description: 库存减一
     * author:      majf
     * createDate:  2022/7/12 13:38
     * @return:      int
    */
    int reduceStock(Long goodsId);

    /**
     * @param pager
     * @param pageSize
     * @param goodsId
     * description: 分页查询秒杀商品
     * author:      majf
     * createDate:  2022/7/12 13:39
     * @return:      com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.alex.common.pojo.dto.SeckillGoodsDTO>
    */
    Result<Page<SeckillGoodsDTO>> findSeckill(Integer pager, Integer pageSize, Long goodsId);
}
