package com.alex.finance.shopStockAnalysis.mapper;

import com.alex.api.finance.shopStockAnalysis.vo.ShopStockAmountVo;
import com.alex.api.finance.shopStockAnalysis.vo.ShopStockAnalysisVo;
import com.alex.api.user.annotation.DataPermission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShopStockAnalysisMapper {

    @DataPermission(table = "t_shop_stock")
    ShopStockAnalysisVo getAllShopStockInfo();

    List<ShopStockAmountVo> getAllAmountInfo(@Param("roleCode") String roleCode,
                                             @Param("userId") Long userId,
                                             @Param("orgId") Long orgId);

    @DataPermission(table = "t_shop_finance")
    ShopStockAmountVo getCashAmountInfo();
}
