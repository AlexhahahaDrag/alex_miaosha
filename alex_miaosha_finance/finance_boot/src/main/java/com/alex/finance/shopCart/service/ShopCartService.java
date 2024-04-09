package com.alex.finance.shopCart.service;

import com.alex.api.finance.shopCart.vo.ShopCartVo;
import com.alex.finance.shopCart.entity.ShopCart;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 购物车表 服务类
 * author: alex
 * createDate: 2024-04-03 11:36:19
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface ShopCartService extends IService<ShopCart> {

    Page<ShopCartVo> getPage(Long pageNum, Long pageSize, ShopCartVo shopCartVo);

    ShopCartVo queryShopCart(Long id);

    Boolean addShopCart(ShopCartVo shopCartVo);

    Boolean updateShopCart(ShopCartVo shopCartVo);

    Boolean deleteShopCart(String ids);

    List<ShopCartVo> list(String ids);
}
