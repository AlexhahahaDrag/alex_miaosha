package com.alex.finance.wechat.service.impl;

import com.alex.common.utils.string.StringUtils;
import com.alex.finance.config.WechatAccountConfig;
import com.alex.finance.wechat.service.WeChatService;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpCurrentAutoReplyInfo;
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
public class WeChatServiceImpl implements WeChatService {

    private static final String TEMPLATE_TEXT_COLOR = "#A9A9A9";

    private final WechatAccountConfig wechatAccountConfig;

    private final WxMpService wxMpService;

    @Override
    public String getToken() throws WxErrorException {
        return wxMpService.getAccessToken();
    }

    @Override
    public void sentMessage(String account, Long num) throws WxErrorException {
        sendRemindTemplateMessage(account, num == null ? "0" : num.toString(), "猫超卡");
    }

    @Override
    public void sendRemindTemplateMessage(String itemName, String numText, String remindType) throws WxErrorException {
        if (wechatAccountConfig == null || StringUtils.isEmpty(wechatAccountConfig.getAccountTimeOutTemplateId())) {
            log.debug("未配置微信提醒模板 accountTimeOutTemplateId，跳过微信模板消息推送: type={}, item={}", remindType, itemName);
            return;
        }
        String token = getToken();
        List<WxMpTemplateData> dataList = Lists.newArrayList();
        dataList.add(new WxMpTemplateData("account", itemName == null ? "" : itemName, TEMPLATE_TEXT_COLOR));
        dataList.add(new WxMpTemplateData("num", numText == null ? "" : numText, TEMPLATE_TEXT_COLOR));
        dataList.add(new WxMpTemplateData("type", remindType == null ? "系统提醒" : remindType, TEMPLATE_TEXT_COLOR));
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .templateId(wechatAccountConfig.getAccountTimeOutTemplateId())
                .url(wechatAccountConfig.getUrl() + "/wxaapi/newtmpl/getpubtemplatetitles?access_token=" + token)
                .data(dataList)
                .build();
        // 查询用户列表
        WxMpUserList wxMpUserList = wxMpService.getUserService().userList(null);
        String targetOpenIds = wechatAccountConfig.getAccountUserId();
        if (wxMpUserList != null && wxMpUserList.getTotal() > 0 && StringUtils.isNotEmpty(targetOpenIds)) {
            for (String userId : wxMpUserList.getOpenids()) {
                if (!targetOpenIds.contains(userId + ",")) {
                    continue;
                }
                templateMessage.setToUser(userId);
                wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
            }
        }
        WxMpCurrentAutoReplyInfo.KeywordAutoReplyInfo keywordAutoReplyInfo = wxMpService.getCurrentAutoReplyInfo().getKeywordAutoReplyInfo();
        log.info("发送微信到期提醒[{}]结果：{}", remindType, JSONObject.toJSONString(keywordAutoReplyInfo));
    }

    @Override
    public void sentShopFinanceMessage(String infoDate, BigDecimal saleAmount, BigDecimal saleNum) throws WxErrorException {
        String token = getToken();
        List<WxMpTemplateData> dataList = Lists.newArrayList();
        dataList.add(new WxMpTemplateData("infoDate", infoDate, "#00FF00"));
        dataList.add(new WxMpTemplateData("saleAmount", saleAmount.toString(), "#00FF00"));
        dataList.add(new WxMpTemplateData("saleNum", saleNum.toString(), "#00FF00"));
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .templateId(wechatAccountConfig.getShopFinanceTemplateId())
                .url(wechatAccountConfig.getUrl() + "/wxaapi/newtmpl/getpubtemplatetitles?access_token=" + token)
                .data(dataList)
                .build();
        // 查询用户列表
        WxMpUserList wxMpUserList = wxMpService.getUserService().userList(null);
        if (wxMpUserList.getTotal() > 0) {
            for (String userId : wxMpUserList.getOpenids()) {
                if (!wechatAccountConfig.getShopFinanceUserId().contains(userId + ",")) {
                    continue;
                }
                templateMessage.setToUser(userId);
                wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
            }
        }
        WxMpCurrentAutoReplyInfo.KeywordAutoReplyInfo keywordAutoReplyInfo = wxMpService.getCurrentAutoReplyInfo().getKeywordAutoReplyInfo();
        log.info("发送产品结果：{}", JSONObject.toJSONString(keywordAutoReplyInfo));
    }
}
