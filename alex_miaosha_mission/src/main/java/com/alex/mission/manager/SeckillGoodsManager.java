package com.alex.mission.manager;

import com.alex.common.pojo.dto.SeckillGoodsDTO;
import com.alex.mission.pojo.entity.SeckillGoods;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SeckillGoodsManager extends IService<SeckillGoods> {

    /**
     * @param goodsId
     * @description: 根据商品id删除秒杀信息
     * @author:      majf
     * @createDate:  2022/7/12 14:22
     * @return:      void
    */
    boolean deleteSeckillGoods(Long goodsId);

    /**
     * @param seckillGoodsDTO
     * @description: 新增秒杀信息
     * @author:      majf
     * @createDate:  2022/7/12 14:27
     * @return:      boolean
    */
    boolean addSeckillGoods(SeckillGoodsDTO seckillGoodsDTO);

    /**
     * @param seckillGoodsDTO
     * @description: 更新秒杀信息
     * @author:      majf
     * @createDate:  2022/7/12 14:27
     * @return:      boolean
    */
    boolean updateSeckillGoods(SeckillGoodsDTO seckillGoodsDTO);
}
