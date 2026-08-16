package com.alex.api.finance.gift.person.vo;

import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.common.common.BaseVo;
import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "GiftPersonInfoVo", description = "gift person vo")
public class GiftPersonInfoVo extends BaseVo<GiftPersonInfoVo> {

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "org id")
    private Long orgId;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "user id")
    private Long userId;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "bound system user id")
    private Long bindUserId;

    @ApiModelProperty(value = "person name")
    private String personName;

    @ApiModelProperty(value = "phone")
    private String phone;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "avatar file id")
    private Long avatar;

    /** 只读：OSS 回填，勿写回 entity */
    @ApiModelProperty(value = "avatar file info")
    private FileInfoVo fileInfoVo;

    @ApiModelProperty(value = "relation type")
    private String relationType;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "relation option id")
    private Long relationOptionId;

    @ApiModelProperty(value = "remark")
    private String remark;

    @ApiModelProperty(value = "relation grade: CORE, IMPORTANT, NORMAL, WEAK")
    private String relationGrade;
}
