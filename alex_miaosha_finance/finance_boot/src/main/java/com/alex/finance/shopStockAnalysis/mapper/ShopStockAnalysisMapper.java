package com.alex.finance.shopStockAnalysis.mapper;

import com.alex.api.finance.vo.shopFinanceAnalysis.ShopStockAnalysisVo;
import org.apache.ibatis.annotations.Param;

public interface ShopStockAnalysisMapper {

    ShopStockAnalysisVo getAllShopStockInfo(@Param("roleCode") String roleCode,
                                            @Param("userId") Long userId,
                                            @Param("orgId") Long orgId);
}
