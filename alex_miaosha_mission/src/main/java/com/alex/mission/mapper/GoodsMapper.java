package com.alex.mission.mapper;

import com.alex.common.pojo.dto.GoodsDTO;
import com.alex.mission.pojo.entity.Goods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

public interface GoodsMapper extends BaseMapper<Goods> {

    Page<GoodsDTO> findGoodsPage(Page<GoodsDTO> page, @Param(value = "goodsName") String goodsName);
}
