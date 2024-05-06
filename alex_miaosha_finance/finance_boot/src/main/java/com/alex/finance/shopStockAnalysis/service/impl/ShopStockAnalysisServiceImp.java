package com.alex.finance.shopStockAnalysis.service.impl;

import com.alex.api.finance.vo.shopFinanceAnalysis.ShopStockAnalysisVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.vo.roleInfo.RoleInfoVo;
import com.alex.api.user.vo.user.TUserVo;
import com.alex.finance.shopStockAnalysis.mapper.ShopStockAnalysisMapper;
import com.alex.finance.shopStockAnalysis.service.ShopStockAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * description:  商品库存分析服务实现类
 * author:       majf
 * createDate:   2024/5/6 18:03
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShopStockAnalysisServiceImp implements ShopStockAnalysisService {

    private final ShopStockAnalysisMapper shopStockAnalysisMapper;

    private final UserUtils userUtils;

    @Override
    public ShopStockAnalysisVo getAllShopStockInfo() {
        TUserVo loginUser = userUtils.getLoginUser();
        RoleInfoVo roleInfoVo = loginUser.getRoleInfoVo();
        return shopStockAnalysisMapper.getAllShopStockInfo(roleInfoVo.getRoleCode(), loginUser.getId(),
                loginUser.getOrgInfoVo() == null ? null : loginUser.getOrgInfoVo().getId());
    }
}
