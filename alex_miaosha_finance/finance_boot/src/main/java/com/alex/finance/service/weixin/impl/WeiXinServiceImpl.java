package com.alex.finance.service.weixin.impl;

import com.alex.finance.config.WechatAccountConfig;
import com.alex.finance.service.weixin.WeiXinService;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.apache.commons.compress.utils.Lists;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
//        List<WxMpUser> wxMpUsers = wxMpService.getUserService().userInfoList((List<String>) null);
//        WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(wechatAccountConfig.getUrl(), wechatAccountConfig.getAppId(), wechatAccountConfig.getSecret());
//        wxMpService.getOAuth2Service().getUserInfo(accessToken, null);
        String msgId = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
//        WxMpTemplateMessage templateMessage1 = WxMpTemplateMessage.builder()
//                .templateId(wechatAccountConfig.getAccountTimeOutTemplateId())
//                .toUser("")
//                .url(wechatAccountConfig.getUrl() + "/wxaapi/newtmpl/getpubtemplatetitles?access_token=" + result)
//                .data(dataList)
//                .build();
//        String msgId1 = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage1);
        log.info("微信网页授权，获取code，result={}", result);
//        wxMpService.getCurrentAutoReplyInfo().getKeywordAutoReplyInfo();


//        // 创建消息对象
//        WxMpCustomMessage message = WxMpCustomMessage.TEXT().toUser("openid").content("Hello World").build();
//
//// 调用消息发送API，发送消息到指定用户
//        wxMpService.customMessageSend(message);
        String accessToken;
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + wechatAccountConfig.getAppId() + "&secret=" + wechatAccountConfig.getSecret();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        JSONObject jsonObject = JSONObject.parseObject(response.getBody());
        accessToken = jsonObject.getString("access_token");

        String openId = "ol6WP5zaolNHhOuuouTOzq6_eNto";
        String msg = "Hello, world!";

        // 发送消息
        String url1 = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + result;
        RestTemplate restTemplate1 = new RestTemplate();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("touser", openId);
        requestBody.put("msgtype", "text");
        Map<String, Object> text = new HashMap<>();
        text.put("content", msg);
        requestBody.put("text", text);
        ResponseEntity<String> response1 = restTemplate1.postForEntity(url1, requestBody, String.class);
        return msgId;
    }
}
