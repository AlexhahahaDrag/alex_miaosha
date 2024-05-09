package com.alex.finance.shopStockAnalysis.mapper;

import com.alex.api.finance.shopStockAnalysis.vo.ShopStockAmountVo;
import com.alex.api.finance.shopStockAnalysis.vo.ShopStockAnalysisVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShopStockAnalysisMapper {

    ShopStockAnalysisVo getAllShopStockInfo(@Param("roleCode") String roleCode,
                                            @Param("userId") Long userId,
                                            @Param("orgId") Long orgId);

    List<ShopStockAmountVo> getAllAmountInfo(@Param("roleCode") String roleCode,
                                             @Param("userId") Long userId,
                                             @Param("orgId") Long orgId);

    ShopStockAmountVo getCashAmountInfo(@Param("roleCode") String roleCode,
                                        @Param("userId") Long userId,
                                        @Param("orgId") Long orgId);
}
