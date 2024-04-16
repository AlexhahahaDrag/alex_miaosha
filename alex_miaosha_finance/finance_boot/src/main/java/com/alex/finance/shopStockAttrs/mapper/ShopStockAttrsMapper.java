package com.alex.finance.shopStockAttrs.mapper;

import com.alex.finance.shopStockAttrs.entity.ShopStockAttrs;
import com.alex.api.finance.shopStockAttrs.vo.ShopStockAttrsVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import com.alex.api.user.annotation.DataPermission;

/**
 * description:  商店库存属性表 mapper
 * author:       alex
 * createDate:   2024-04-16 19:50:29
 * version:      1.0.0
 */
@Mapper
public interface ShopStockAttrsMapper extends BaseMapper<ShopStockAttrs> {

    @DataPermission(table = "t_shop_stock_attrs")
    Page<ShopStockAttrsVo> getPage(Page<ShopStockAttrsVo> page, @Param("shopStockAttrsVo") ShopStockAttrsVo shopStockAttrsVo);

    ShopStockAttrsVo queryShopStockAttrs(@Param("id") Long id);
}
