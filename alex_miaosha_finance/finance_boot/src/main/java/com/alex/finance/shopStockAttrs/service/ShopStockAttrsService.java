package com.alex.finance.shopStockAttrs.service;

import com.alex.api.finance.shopStockAttrs.vo.ShopStockAttrsVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.finance.shopStockAttrs.entity.ShopStockAttrs;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 商店库存属性表 服务类
 * author: alex
 * createDate: 2024-04-16 19:50:29
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface ShopStockAttrsService extends IService<ShopStockAttrs> {

    Page<ShopStockAttrsVo> getPage(Long pageNum, Long pageSize, ShopStockAttrsVo shopStockAttrsVo);

    ShopStockAttrsVo queryShopStockAttrs(Long id);

    Boolean addShopStockAttrs(ShopStockAttrsVo shopStockAttrsVo);

    Boolean updateShopStockAttrs(ShopStockAttrsVo shopStockAttrsVo);

    Boolean deleteShopStockAttrs(String ids);

    Boolean deleteShopStockAttrsByStockId(Long stockId);
}
