package com.alex.finance.shopOrderDetail.mapper;

import com.alex.finance.shopOrderDetail.entity.ShopOrderDetail;
import com.alex.api.finance.shopOrderDetail.vo.ShopOrderDetailVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import com.alex.api.user.annotation.DataPermission;

/**
 * description:  商店订单明细表 mapper
 * author:       alex
 * createDate:   2024-04-09 15:35:21
 * version:      1.0.0
 */
@Mapper
public interface ShopOrderDetailMapper extends BaseMapper<ShopOrderDetail> {

    @DataPermission(table = "t_shop_order_detail")
    Page<ShopOrderDetailVo> getPage(Page<ShopOrderDetailVo> page, @Param("shopOrderDetailVo") ShopOrderDetailVo shopOrderDetailVo);

    ShopOrderDetailVo queryShopOrderDetail(@Param("id") Long id);
}
