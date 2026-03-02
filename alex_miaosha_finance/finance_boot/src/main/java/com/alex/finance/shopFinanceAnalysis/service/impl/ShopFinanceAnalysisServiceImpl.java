package com.alex.finance.shopFinanceAnalysis.service.impl;

import com.alex.api.finance.vo.shopFinanceAnalysis.ShopFinanceAnalysisVo;
import com.alex.api.finance.vo.shopFinanceAnalysis.ShopFinanceChainYearVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.shopFinance.mapper.ShopFinanceMapper;
import com.alex.finance.shopFinanceAnalysis.service.ShopFinanceAnalysisService;
import com.alex.finance.wechat.service.WeChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopFinanceAnalysisServiceImpl implements ShopFinanceAnalysisService {

    private final ShopFinanceMapper shopFinanceMapper;

    private final UserUtils userUtils;

    private final WeChatService weChatService;

    @Override
    public List<ShopFinanceAnalysisVo> getDayShopFinanceInfo(String searchDate) {
        TUserVo loginUser = userUtils.getLoginUser();
        RoleInfoVo roleInfoVo = loginUser.getRoleInfoVo();
        return shopFinanceMapper.getDayShopFinanceInfo(searchDate,
                roleInfoVo.getRoleCode(), loginUser.getId(),
                loginUser.getOrgInfoVo() == null ? null : loginUser.getOrgInfoVo().getId());
    }

    @Override
    public List<ShopFinanceAnalysisVo> getMonthShopFinanceInfo(String searchDate) {
        TUserVo loginUser = userUtils.getLoginUser();
        RoleInfoVo roleInfoVo = loginUser.getRoleInfoVo();
        return shopFinanceMapper.getMonthShopFinanceInfo(searchDate,
                roleInfoVo.getRoleCode(), loginUser.getId(),
                loginUser.getOrgInfoVo() == null ? null : loginUser.getOrgInfoVo().getId());
    }

    @Override
    public List<ShopFinanceAnalysisVo> getPayWayInfo(String searchDate) {
        TUserVo loginUser = userUtils.getLoginUser();
        RoleInfoVo roleInfoVo = loginUser.getRoleInfoVo();
        return shopFinanceMapper.getPayWayInfo(searchDate,
                roleInfoVo.getRoleCode(), loginUser.getId(),
                loginUser.getOrgInfoVo() == null ? null : loginUser.getOrgInfoVo().getId());
    }

    @Override
    public List<ShopFinanceAnalysisVo> getShopNameInfo(String searchDate) {
        TUserVo loginUser = userUtils.getLoginUser();
        RoleInfoVo roleInfoVo = loginUser.getRoleInfoVo();
        return shopFinanceMapper.getShopNameInfo(searchDate, roleInfoVo.getRoleCode(), loginUser.getId(),
                loginUser.getOrgInfoVo() == null ? null : loginUser.getOrgInfoVo().getId());
    }

    @Override
    public ShopFinanceChainYearVo getChainAndYear(String startDate, String endDate) {
        TUserVo loginUser = userUtils.getLoginUser();
        RoleInfoVo roleInfoVo = loginUser.getRoleInfoVo();
        return shopFinanceMapper.getChainAndYear(startDate, endDate, roleInfoVo.getRoleCode(), loginUser.getId(),
                loginUser.getOrgInfoVo() == null ? null : loginUser.getOrgInfoVo().getId());
    }

    @Override
    public void getCurShopFinanceInfo(String startDate, String endDate, String type) throws Exception {
        List<ShopFinanceAnalysisVo> curShopFinanceInfo = shopFinanceMapper.getCurShopFinanceInfo(startDate, endDate,
                null, null, null, type);
        for (ShopFinanceAnalysisVo cur : curShopFinanceInfo) {
            weChatService.sentShopFinanceMessage(cur.getInfoDate() + ("day".equals(type) ? "" : "月"),
                    cur.getSaleAmount(), cur.getSaleNum());
        }
    }

    @Override
    public ShopFinanceChainYearVo getBenefitInfo(String startDate, String endDate, String searchType) {
        TUserVo loginUser = userUtils.getLoginUser();
        RoleInfoVo roleInfoVo = loginUser.getRoleInfoVo();
        return shopFinanceMapper.getBenefitInfo(startDate, endDate, searchType, roleInfoVo.getRoleCode(), loginUser.getId(),
                loginUser.getOrgInfoVo() == null ? null : loginUser.getOrgInfoVo().getId());
    }
}

