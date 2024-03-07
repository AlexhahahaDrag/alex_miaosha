package com.alex.finance.service.shopFinanceAnalysis.impl;

import com.alex.api.finance.vo.accountRecordInfo.AccountCountInfoVo;
import com.alex.api.finance.vo.shopFinanceAnalysis.ShopFinanceAnalysisVo;
import com.alex.api.finance.vo.shopFinanceAnalysis.ShopFinanceChainYearVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.vo.roleInfo.RoleInfoVo;
import com.alex.api.user.vo.user.TUserVo;
import com.alex.finance.mapper.shopFinance.ShopFinanceMapper;
import com.alex.finance.service.shopFinanceAnalysis.ShopFinanceAnalysisService;
import com.alex.finance.service.weixin.WeiXinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopFinanceAnalysisServiceImpl implements ShopFinanceAnalysisService {

    private final ShopFinanceMapper shopFinanceMapper;

    private final UserUtils userUtils;

    private final WeiXinService weiXinService;

    @Override
    public List<ShopFinanceAnalysisVo> getDayShopFinanceInfo(String searchDate) {
        TUserVo loginUser = userUtils.getLoginUser();
        RoleInfoVo roleInfoVo = loginUser.getRoleInfoVo();
        return shopFinanceMapper.getDayShopFinanceInfo(searchDate,
                roleInfoVo.getRoleCode(), loginUser.getId(),
                loginUser.getOrgInfoVo().getId());
    }

    @Override
    public List<ShopFinanceAnalysisVo> getMonthShopFinanceInfo(String searchDate) {
        TUserVo loginUser = userUtils.getLoginUser();
        RoleInfoVo roleInfoVo = loginUser.getRoleInfoVo();
        return shopFinanceMapper.getMonthShopFinanceInfo(searchDate,
                roleInfoVo.getRoleCode(), loginUser.getId(),
                loginUser.getOrgInfoVo().getId());
    }

    @Override
    public List<ShopFinanceAnalysisVo> getPayWayInfo(String searchDate) {
        TUserVo loginUser = userUtils.getLoginUser();
        RoleInfoVo roleInfoVo = loginUser.getRoleInfoVo();
        return shopFinanceMapper.getPayWayInfo(searchDate,
                roleInfoVo.getRoleCode(), loginUser.getId(),
                loginUser.getOrgInfoVo().getId());
    }

    @Override
    public List<ShopFinanceAnalysisVo> getShopNameInfo(String searchDate) {
        TUserVo loginUser = userUtils.getLoginUser();
        RoleInfoVo roleInfoVo = loginUser.getRoleInfoVo();
        return shopFinanceMapper.getShopNameInfo(searchDate,roleInfoVo.getRoleCode(), loginUser.getId(),
                loginUser.getOrgInfoVo().getId());
    }

    @Override
    public ShopFinanceChainYearVo getChainAndYear(String searchDate) {
        TUserVo loginUser = userUtils.getLoginUser();
        RoleInfoVo roleInfoVo = loginUser.getRoleInfoVo();
        return shopFinanceMapper.getChainAndYear(searchDate, roleInfoVo.getRoleCode(), loginUser.getId(),
                loginUser.getOrgInfoVo().getId());
    }

    @Override
    public void getCurShopFinanceInfo(String startDate, String endDate, String type) throws Exception {
        TUserVo loginUser = userUtils.getLoginUser();
        List<ShopFinanceAnalysisVo> curShopFinanceInfo;
        if (loginUser == null) {
            curShopFinanceInfo = shopFinanceMapper.getCurShopFinanceInfo(startDate, endDate,
                    null, null, null);
        } else {
            RoleInfoVo roleInfoVo = loginUser.getRoleInfoVo();
            curShopFinanceInfo = shopFinanceMapper.getCurShopFinanceInfo(startDate, endDate,
                    roleInfoVo.getRoleCode(), loginUser.getId(),
                    loginUser.getOrgInfoVo().getId());
        }
        for(ShopFinanceAnalysisVo cur : curShopFinanceInfo) {
            weiXinService.sentShopFinanceMessage(cur.getInfoDate() + ("day".equals(type) ? "日" : "月"),
                    cur.getSaleAmount(), cur.getSaleNum());
        }
    }
}
