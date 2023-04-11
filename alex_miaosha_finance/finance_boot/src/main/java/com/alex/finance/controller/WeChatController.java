package com.alex.finance.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wechat")
@Slf4j
@RequiredArgsConstructor
public class WeChatController {


//    @GetMapping("/userInfo")
//    public String userInfo(@RequestParam("code") String code,
//                           @RequestParam("state") String url) {
//
//        WxMpOAuth2AccessToken wxMpOAuth2AccessToken=new WxMpOAuth2AccessToken();
//        try {
//            wxMpOAuth2AccessToken = wxMpService.oauth2getAccessToken(code);
//
//        } catch (WxErrorException e) {
//            log.error("微信网页授权，{}",e);
//            throw new SellException(ResultEnum.WX_MP_ERROR.getCode(),e.getError().getErrorMsg());
//        }
//        String openId = wxMpOAuth2AccessToken.getOpenId();
//        log.info("openId={}",openId);
//        return "redirect:"+url+"?openid="+openId;
//    }
}