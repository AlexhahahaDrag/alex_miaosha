package com.alex.finance.shopStockBatch.mapper;

import com.alex.api.finance.shopStockBatch.vo.ShopStockBatchVo;
import com.alex.api.user.annotation.DataPermission;
import com.alex.finance.shopStockBatch.entity.ShopStockBatch;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description:  商店库存批次表 mapper
 * author:       alex
 * createDate:   2024-05-10 17:30:40
 * version:      1.0.0
 */
@Mapper
public interface ShopStockBatchMapper extends BaseMapper<ShopStockBatch> {

    @DataPermission(table = "t_shop_stock_batch")
    Page<ShopStockBatchVo> getPage(Page<ShopStockBatchVo> page, @Param("shopStockBatchVo") ShopStockBatchVo shopStockBatchVo);

    ShopStockBatchVo queryShopStockBatch(@Param("id") Long id);

    @DataPermission(table = "t_shop_stock_batch")
    List<ShopStockBatchVo> getList(@Param("shopStockBatchVo") ShopStockBatchVo shopStockBatchVo);
}
