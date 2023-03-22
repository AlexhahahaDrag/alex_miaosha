package com.alex.common.common;

import com.alex.common.config.Long2StringSerializer;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class BaseVo<T extends Model<T>> extends Model<T> implements Serializable {

    @ApiModelProperty(value = "id")
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long id;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty("创建人")
    private Long creator;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty("更新人")
    private Long updater;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty("操作人")
    private Long operator;

    @ApiModelProperty("操作时间")
    private Date operateTime;

    @ApiModelProperty("删除人")
    private Long deleter;

    @ApiModelProperty("操作时间")
    private Date deleteTime;

    @ApiModelProperty("是否删除")
    private Integer isDelete;
}
