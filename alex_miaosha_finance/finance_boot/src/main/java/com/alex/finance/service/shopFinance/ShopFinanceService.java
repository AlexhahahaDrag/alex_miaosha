package com.alex.finance.service.shopFinance;

import com.alex.api.finance.vo.shopFinance.ShopFinanceVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.finance.entity.shopFinance.ShopFinance;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 商店财务表 服务类
 * author: majf
 * createDate: 2024-02-23 21:19:49
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface ShopFinanceService extends IService<ShopFinance> {

    Page<ShopFinanceVo> getPage(Long pageNum, Long pageSize, ShopFinanceVo shopFinanceVo);

    ShopFinanceVo queryShopFinance(Long id);

    Boolean addShopFinance(ShopFinanceVo shopFinanceVo);

    Boolean updateShopFinance(ShopFinanceVo shopFinanceVo);

    Boolean deleteShopFinance(String ids);
}
