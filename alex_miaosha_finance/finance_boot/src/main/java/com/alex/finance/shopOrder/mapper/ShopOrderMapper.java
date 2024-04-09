package com.alex.finance.shopOrder.mapper;

import com.alex.finance.shopOrder.entity.ShopOrder;
import com.alex.api.finance.shopOrder.vo.ShopOrderVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import com.alex.api.user.annotation.DataPermission;

/**
 * description:  商店订单表 mapper
 * author:       alex
 * createDate:   2024-04-09 15:20:01
 * version:      1.0.0
 */
@Mapper
public interface ShopOrderMapper extends BaseMapper<ShopOrder> {

    @DataPermission(table = "t_shop_order")
    Page<ShopOrderVo> getPage(Page<ShopOrderVo> page, @Param("shopOrderVo") ShopOrderVo shopOrderVo);

    ShopOrderVo queryShopOrder(@Param("id") Long id);
}
