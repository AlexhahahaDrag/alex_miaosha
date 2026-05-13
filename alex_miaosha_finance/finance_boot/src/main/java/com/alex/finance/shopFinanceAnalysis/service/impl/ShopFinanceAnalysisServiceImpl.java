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
        String roleCode = getRoleCode(loginUser);
        return shopFinanceMapper.getDayShopFinanceInfo(searchDate,
                roleCode, loginUser.getId(),
                loginUser.getOrgInfoVo() == null ? null : loginUser.getOrgInfoVo().getId());
    }

    @Override
    public List<ShopFinanceAnalysisVo> getMonthShopFinanceInfo(String searchDate) {
        TUserVo loginUser = userUtils.getLoginUser();
        String roleCode = getRoleCode(loginUser);
        return shopFinanceMapper.getMonthShopFinanceInfo(searchDate,
                roleCode, loginUser.getId(),
                loginUser.getOrgInfoVo() == null ? null : loginUser.getOrgInfoVo().getId());
    }

    @Override
    public List<ShopFinanceAnalysisVo> getPayWayInfo(String searchDate) {
        TUserVo loginUser = userUtils.getLoginUser();
        String roleCode = getRoleCode(loginUser);
        return shopFinanceMapper.getPayWayInfo(searchDate,
                roleCode, loginUser.getId(),
                loginUser.getOrgInfoVo() == null ? null : loginUser.getOrgInfoVo().getId());
    }

    @Override
    public List<ShopFinanceAnalysisVo> getShopNameInfo(String searchDate) {
        TUserVo loginUser = userUtils.getLoginUser();
        String roleCode = getRoleCode(loginUser);
        return shopFinanceMapper.getShopNameInfo(searchDate, roleCode, loginUser.getId(),
                loginUser.getOrgInfoVo() == null ? null : loginUser.getOrgInfoVo().getId());
    }

    @Override
    public ShopFinanceChainYearVo getChainAndYear(String startDate, String endDate) {
        TUserVo loginUser = userUtils.getLoginUser();
        String roleCode = getRoleCode(loginUser);
        return shopFinanceMapper.getChainAndYear(startDate, endDate, roleCode, loginUser.getId(),
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
        String roleCode = getRoleCode(loginUser);
        return shopFinanceMapper.getBenefitInfo(startDate, endDate, searchType, roleCode, loginUser.getId(),
                loginUser.getOrgInfoVo() == null ? null : loginUser.getOrgInfoVo().getId());
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

