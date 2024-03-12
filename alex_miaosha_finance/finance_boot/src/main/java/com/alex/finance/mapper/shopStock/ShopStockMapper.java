package com.alex.finance.mapper.shopStock;

import com.alex.finance.entity.shopStock.ShopStock;
import com.alex.api.finance.vo.shopStock.ShopStockVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import com.alex.api.user.annotation.DataPermission;

/**
 * description:  商店库存表 mapper
 * author:       alex
 * createDate:   2024-03-12 16:49:20
 * version:      1.0.0
 */
@Mapper
public interface ShopStockMapper extends BaseMapper<ShopStock> {

    @DataPermission(table = "t_shop_stock")
    Page<ShopStockVo> getPage(Page<ShopStockVo> page, @Param("shopStockVo") ShopStockVo shopStockVo);

    ShopStockVo queryShopStock(@Param("id") Long id);
}
