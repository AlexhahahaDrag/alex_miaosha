package com.alex.finance.message.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 用户个性化消息通知渠道配置实体
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("user_notice_config_t")
@ApiModel(value = "UserNoticeConfig对象", description = "用户个性化消息通知渠道配置表")
public class UserNoticeConfig extends BaseEntity<UserNoticeConfig> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "所属用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "业务类型编码 (对应枚举 NoticeBizType)")
    @TableField("notice_code")
    private String noticeCode;

    @ApiModelProperty(value = "是否接收系统内部站内信：1-是，0-否")
    @TableField("internal_enabled")
    private Boolean internalEnabled;

    @ApiModelProperty(value = "是否接收微信模板消息：1-是，0-否")
    @TableField("wechat_enabled")
    private Boolean wechatEnabled;
}
