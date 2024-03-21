package com.alex.finance.mapper.shopStock;

import com.alex.api.finance.vo.shopStock.ShopStockVo;
import com.alex.api.user.annotation.DataPermission;
import com.alex.finance.entity.shopStock.ShopStock;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    @DataPermission(table = "t_shop_stock")
    List<ShopStockVo> getShopList(@Param("ids") List<Long> ids);
}
