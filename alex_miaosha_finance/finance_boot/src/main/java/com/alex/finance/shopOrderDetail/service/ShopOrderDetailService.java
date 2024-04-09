package com.alex.finance.shopOrderDetail.service;

import com.alex.api.finance.shopOrderDetail.vo.ShopOrderDetailVo;
import com.alex.finance.shopOrderDetail.entity.ShopOrderDetail;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商店订单明细表 服务类
 * author: alex
 * createDate: 2024-04-09 15:35:21
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface ShopOrderDetailService extends IService<ShopOrderDetail> {

    Page<ShopOrderDetailVo> getPage(Long pageNum, Long pageSize, ShopOrderDetailVo shopOrderDetailVo);

    ShopOrderDetailVo queryShopOrderDetail(Long id);

    Boolean addShopOrderDetail(ShopOrderDetailVo shopOrderDetailVo);

    Boolean updateShopOrderDetail(ShopOrderDetailVo shopOrderDetailVo);

    Boolean batchUpdateShopOrderDetail(List<ShopOrderDetailVo> shopOrderDetailVo);

    Boolean deleteShopOrderDetail(String ids);
}
