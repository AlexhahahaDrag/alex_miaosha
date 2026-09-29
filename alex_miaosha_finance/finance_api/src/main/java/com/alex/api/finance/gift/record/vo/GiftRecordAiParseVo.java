package com.alex.api.finance.gift.record.vo;

import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "GiftRecordAiParseVo", description = "自然语言记账智能解析结果")
public class GiftRecordAiParseVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "亲友姓名")
    private String personName;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "匹配到的已有亲友ID（若无则为null）")
    private Long personId;

    @ApiModelProperty(value = "是否为新亲友建议（系统中尚未建档）")
    private Boolean isNewPerson;

    @ApiModelProperty(value = "关系类型编码（如 relative, friend, colleague 等）")
    private String relationType;

    @ApiModelProperty(value = "关系类型展示名称（如 发小、长辈、同事）")
    private String relationName;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "匹配到的事由ID（若无则为null）")
    private Long eventId;

    @ApiModelProperty(value = "事由类型（如 wedding, birthday, baby_full_moon 等）")
    private String eventType;

    @ApiModelProperty(value = "事由展示名称（如 婚礼、满月酒、寿宴）")
    private String eventTypeName;

    @ApiModelProperty(value = "礼金金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "流水方向：GIVE（随礼出） / RECEIVE（收礼进）")
    private String direction;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "发生时间")
    private LocalDateTime payTime;

    @ApiModelProperty(value = "地点/酒楼")
    private String location;

    @ApiModelProperty(value = "提取的备注信息")
    private String remark;

    @ApiModelProperty(value = "原始解析自然语言文本")
    private String rawText;
}
