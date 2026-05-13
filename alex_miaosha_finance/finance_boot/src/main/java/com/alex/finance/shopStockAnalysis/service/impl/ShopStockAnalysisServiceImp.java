package com.alex.finance.shopStockAnalysis.service.impl;

import com.alex.api.finance.shopStockAnalysis.vo.ShopStockAmountVo;
import com.alex.api.finance.shopStockAnalysis.vo.ShopStockAnalysisVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.shopStockAnalysis.mapper.ShopStockAnalysisMapper;
import com.alex.finance.shopStockAnalysis.service.ShopStockAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return shopStockAnalysisMapper.getAllShopStockInfo();
    }

    @Override
    public List<ShopStockAmountVo> getAllAmountInfo() {
        TUserVo loginUser = userUtils.getLoginUser();
        String roleCode = getRoleCode(loginUser);
        return shopStockAnalysisMapper.getAllAmountInfo(roleCode, loginUser.getId(),
                loginUser.getOrgInfoVo() == null ? null : loginUser.getOrgInfoVo().getId());
    }

    @Override
    public ShopStockAmountVo getCashAmountInfo() {
        return shopStockAnalysisMapper.getCashAmountInfo();
    }

    private String getRoleCode(TUserVo loginUser) {
        if (loginUser == null || loginUser.getRoleInfoVoList() == null || loginUser.getRoleInfoVoList().isEmpty()) {
            return null;
        }
        List<RoleInfoVo> roleInfoVoList = loginUser.getRoleInfoVoList();
        String targetRole = null;
        for (RoleInfoVo role : roleInfoVoList) {
            String code = role.getRoleCode();
            if (code == null) {
                continue;
            }
            if (code.contains("super")) {
                return code;
            }
            if (code.contains("admin")) {
                targetRole = code;
            } else if (targetRole == null && code.contains("user")) {
                targetRole = code;
            }
        }
        return targetRole != null ? targetRole : roleInfoVoList.get(0).getRoleCode();
    }
}
