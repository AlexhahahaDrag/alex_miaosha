package com.alex.api.finance.message.vo;

import com.alex.common.common.BaseVo;
import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 用户个性化消息渠道配置视图对象
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "UserNoticeConfigVo", description = "用户个性化消息渠道配置视图对象")
public class UserNoticeConfigVo extends BaseVo<UserNoticeConfigVo> {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "所属用户ID")
    private Long userId;

    @ApiModelProperty(value = "业务大类编码 (如 GLOBAL, GIFT_EVENT 等)")
    private String noticeCode;

    @ApiModelProperty(value = "业务大类中文名称")
    private String noticeName;

    @ApiModelProperty(value = "业务大类描述说明")
    private String noticeDesc;

    @ApiModelProperty(value = "是否接收系统内部站内信：1-是，0-否")
    private Boolean internalEnabled;

    @ApiModelProperty(value = "是否接收微信模板消息：1-是，0-否")
    private Boolean wechatEnabled;
}
