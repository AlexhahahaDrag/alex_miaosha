package com.alex.finance.service.weixin.impl;

import com.alex.finance.config.WechatAccountConfig;
import com.alex.finance.service.weixin.WeiXinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpUserList;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeiXinServiceImpl implements WeiXinService {

    private final WechatAccountConfig wechatAccountConfig;

    private final WxMpService wxMpService;

    public String getToken() throws WxErrorException {
        return wxMpService.getAccessToken();
    }

    public void sentMessage(String account, Long num) throws WxErrorException {
        String token = getToken();
        List<WxMpTemplateData> dataList = Lists.newArrayList();
        dataList.add(new WxMpTemplateData("account", account, "#A9A9A9"));
        dataList.add(new WxMpTemplateData("num", num.toString(), "#A9A9A9"));
        dataList.add(new WxMpTemplateData("type", "猫超卡", "#A9A9A9"));
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .templateId(wechatAccountConfig.getAccountTimeOutTemplateId())
                .url(wechatAccountConfig.getUrl() + "/wxaapi/newtmpl/getpubtemplatetitles?access_token=" + token)
                .data(dataList)
                .build();
        // 查询用户列表
        WxMpUserList wxMpUserList = wxMpService.getUserService().userList(null);
        if (wxMpUserList.getTotal() > 0) {
            for (String userId : wxMpUserList.getOpenids()) {
                if (!wechatAccountConfig.getAccountUserId().contains(userId)) {
                    continue;
                }
                templateMessage.setToUser(userId);
                wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
            }
        }
        wxMpService.getCurrentAutoReplyInfo().getKeywordAutoReplyInfo();
    }

    @Override
    public void sentShopFinanceMessage(String date, BigDecimal saleAmount, BigDecimal saleNum) throws WxErrorException {
        String token = getToken();
        List<WxMpTemplateData> dataList = Lists.newArrayList();
        dataList.add(new WxMpTemplateData("date", date, "#A9A9A9"));
        dataList.add(new WxMpTemplateData("saleAmount", saleAmount.toString(), "#A9A9A9"));
        dataList.add(new WxMpTemplateData("saleNum", saleNum.toString(), "#A9A9A9"));
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .templateId(wechatAccountConfig.getShopFinanceTemplateId())
                .url(wechatAccountConfig.getUrl() + "/wxaapi/newtmpl/getpubtemplatetitles?access_token=" + token)
                .data(dataList)
                .build();
        // 查询用户列表
        WxMpUserList wxMpUserList = wxMpService.getUserService().userList(null);
        if (wxMpUserList.getTotal() > 0) {
            for (String userId : wxMpUserList.getOpenids()) {
                if (!wechatAccountConfig.getShopFinanceUserId().contains(userId)) {
                    continue;
                }
                templateMessage.setToUser(userId);
                wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
            }
        }
        wxMpService.getCurrentAutoReplyInfo().getKeywordAutoReplyInfo();
    }
}
