package com.alex.finance.shopCart.mapper;

import com.alex.api.finance.shopCart.vo.ShopCartVo;
import com.alex.api.user.annotation.DataPermission;
import com.alex.finance.shopCart.entity.ShopCart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description:  购物车表 mapper
 * author:       alex
 * createDate:   2024-04-03 11:36:19
 * version:      1.0.0
 */
@Mapper
public interface ShopCartMapper extends BaseMapper<ShopCart> {

    @DataPermission(table = "t_shop_cart")
    Page<ShopCartVo> getPage(Page<ShopCartVo> page, @Param("shopCartVo") ShopCartVo shopCartVo);

    ShopCartVo queryShopCart(@Param("id") Long id);

    @DataPermission(table = "t_shop_cart")
    List<ShopCartVo> list(@Param("ids") List<Long> ids);
}
