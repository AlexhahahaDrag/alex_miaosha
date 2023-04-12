package com.alex.finance.service.weixin.impl;

import com.alex.finance.config.WechatAccountConfig;
import com.alex.finance.service.weixin.WeiXinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

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

    public String sentMessage(String account, Long num) throws WxErrorException {
        String result = getToken();
        List<WxMpTemplateData> dataList = Lists.newArrayList();
        dataList.add(new WxMpTemplateData("account", account, "#A9A9A9"));
        dataList.add(new WxMpTemplateData("num", num.toString(), "#A9A9A9"));
        dataList.add(new WxMpTemplateData("type", "猫超卡", "#A9A9A9"));
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .templateId(wechatAccountConfig.getAccountTimeOutTemplateId())
                .toUser(wechatAccountConfig.getUserId())
                .url(wechatAccountConfig.getUrl() + "/wxaapi/newtmpl/getpubtemplatetitles?access_token=" + result)
                .data(dataList)
                .build();
        // TODO: 2023/4/12 添加查询用户列表 
        String msgId = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        WxMpTemplateMessage templateMessage1 = WxMpTemplateMessage.builder()
                .templateId(wechatAccountConfig.getAccountTimeOutTemplateId())
                .toUser("")
                .url(wechatAccountConfig.getUrl() + "/wxaapi/newtmpl/getpubtemplatetitles?access_token=" + result)
                .data(dataList)
                .build();
        String msgId1 = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage1);
        log.info("微信网页授权，获取code，result={}", result);
        wxMpService.getCurrentAutoReplyInfo().getKeywordAutoReplyInfo();
        return msgId1;
    }
}
