package com.alex.finance.message.service;

import com.alex.api.finance.message.vo.UserNoticeConfigVo;
import com.alex.finance.message.entity.UserNoticeConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户个性化消息通知渠道配置服务
 */
public interface UserNoticeConfigService extends IService<UserNoticeConfig> {

    /**
     * 判断是否向目标用户发送系统内部站内信
     *
     * @param userId     目标用户ID
     * @param noticeCode 业务大类编码
     * @return true-允许发送；false-用户已关闭
     */
    boolean isInternalEnabled(Long userId, String noticeCode);

    /**
     * 判断是否向目标用户发送微信模板消息
     *
     * @param userId     目标用户ID
     * @param noticeCode 业务大类编码
     * @return true-允许发送；false-用户已关闭
     */
    boolean isWechatEnabled(Long userId, String noticeCode);

    /**
     * 获取指定用户的全量 8 大业务通知渠道配置清单（平滑合并默认模板与用户覆盖）
     *
     * @param userId 目标用户ID
     * @return 8 大业务大类配置 VO 列表
     */
    List<UserNoticeConfigVo> getMyConfigs(Long userId);

    /**
     * 保存或更新当前用户的指定业务通知渠道配置
     *
     * @param userId 目标用户ID
     * @param vo     前端入参配置
     * @return 操作结果
     */
    Boolean saveMyConfig(Long userId, UserNoticeConfigVo vo);
}
