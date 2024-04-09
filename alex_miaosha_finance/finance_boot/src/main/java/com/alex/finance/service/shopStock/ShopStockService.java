package com.alex.finance.service.shopStock;

import com.alex.api.finance.vo.shopStock.ShopStockVo;
import com.alex.finance.entity.shopStock.ShopStock;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商店库存表 服务类
 * author: alex
 * createDate: 2024-03-12 16:49:20
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface ShopStockService extends IService<ShopStock> {

    Page<ShopStockVo> getPage(Long pageNum, Long pageSize, ShopStockVo shopStockVo);

    ShopStockVo queryShopStock(Long id);

    Boolean addShopStock(ShopStockVo shopStockVo);

    Boolean updateShopStock(ShopStockVo shopStockVo);

    Boolean deleteShopStock(String ids);

    List<ShopStockVo> getShopList(String ids);
}
