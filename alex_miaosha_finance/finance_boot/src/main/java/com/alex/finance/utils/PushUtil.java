package com.alex.finance.utils;

import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;

public class PushUtil {

    public static String push() {
        //1，配置
//        WxMpConfigStorage wxStorage = new WxMpConfigStorageHolder();
//        wxStorage.setAppId(PushConfigure.getAppId());
//        wxStorage.setSecret(PushConfigure.getSecret());
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(null);
        // 推送消息
//        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
//                .toUser(PushConfigure.getUserId())
//                .templateId(PushConfigure.getTemplateId())
//                .build();
//        // 配置你的信息
////        long loveDays = MemoryDayUtil.calculationLianAi(PushConfigure.getLoveDate());
////        long birthdays = MemoryDayUtil.calculationBirthday(PushConfigure.getBirthday());
//
//        templateMessage.addData(new WxMpTemplateData("loveDays", loveDays + "", "#FF1493"));
//        templateMessage.addData(new WxMpTemplateData("birthdays", birthdays + "", "#FFA500"));
//
//        String remark = "亲爱的乖乖宝贝，早上好!记得要吃早餐哦，今天也要开心哦 =^_^= ";
//        if (loveDays % 365 == 0) {
//            remark = "\n今天是恋爱" + (loveDays / 365) + "周年纪念日!";
//        }
//        if (birthdays == 0) {
//            remark = "\n今天是生日,生日快乐呀!";
//        }
//        if (loveDays % 365 == 0 && birthdays == 0) {
//            remark = "\n今天是生日,也是恋爱" + (loveDays / 365) + "周年纪念日!";
//        }

//        templateMessage.addData(new WxMpTemplateData("remark", remark, "#FF1493"));
//        templateMessage.addData(new WxMpTemplateData("rainbow", RainbowUtil.getRainbow(), "#FF69B4"));
//        System.out.println(templateMessage.toJson());
//        try {
//            wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
//        } catch (Exception e) {
//            System.out.println("推送失败：" + e.getMessage());
//            return "推送失败：" + e.getMessage();
//        }
        return "推送成功!";
    }
}
