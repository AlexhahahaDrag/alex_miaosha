package com.alex.finance.shopOrder.service;

import com.alex.api.finance.shopOrder.vo.ShopOrderVo;
import com.alex.finance.shopOrder.entity.ShopOrder;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 商店订单表 服务类
 * author: alex
 * createDate: 2024-04-09 15:20:01
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface ShopOrderService extends IService<ShopOrder> {

    Page<ShopOrderVo> getPage(Long pageNum, Long pageSize, ShopOrderVo shopOrderVo);

    ShopOrderVo queryShopOrder(Long id);

    Boolean addShopOrder(ShopOrderVo shopOrderVo);

    Boolean updateShopOrder(ShopOrderVo shopOrderVo);

    Boolean deleteShopOrder(String ids);


    Boolean submitOrder(ShopOrderVo shopOrderVo) throws Exception;
}
