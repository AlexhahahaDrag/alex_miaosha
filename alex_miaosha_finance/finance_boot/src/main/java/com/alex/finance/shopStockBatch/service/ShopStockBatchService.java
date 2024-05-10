package com.alex.finance.shopStockBatch.service;

import com.alex.api.finance.shopStockBatch.vo.ShopStockBatchVo;
import com.alex.finance.shopStockBatch.entity.ShopStockBatch;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商店库存批次表 服务类
 * author: alex
 * createDate: 2024-05-10 17:30:40
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface ShopStockBatchService extends IService<ShopStockBatch> {

    Page<ShopStockBatchVo> getPage(Long pageNum, Long pageSize, ShopStockBatchVo shopStockBatchVo);

    ShopStockBatchVo queryShopStockBatch(Long id);

    Boolean addShopStockBatch(ShopStockBatchVo shopStockBatchVo);

    Boolean updateShopStockBatch(ShopStockBatchVo shopStockBatchVo);

    Boolean deleteShopStockBatch(String ids);

    List<ShopStockBatchVo> getList(ShopStockBatchVo shopStockBatchVo);
}
