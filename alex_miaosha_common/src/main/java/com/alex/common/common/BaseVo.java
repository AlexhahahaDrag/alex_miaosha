package com.alex.common.common;

import com.alex.common.config.Long2StringSerializer;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseVo<T extends Model<T>> extends Model<T> implements Serializable {

    @ApiModelProperty(value = "id")
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long id;

    @ApiModelProperty("创建人")
    private Long creator;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern ="yyyy-MM-dd HH:mm:ss",timezone ="GMT+8")
    private LocalDateTime createTime;

    @ApiModelProperty("更新人")
    private Long updater;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern ="yyyy-MM-dd HH:mm:ss",timezone ="GMT+8")
    private LocalDateTime updateTime;

    @ApiModelProperty("操作人")
    private Long operator;

    @ApiModelProperty("操作时间")
    @JsonFormat(pattern ="yyyy-MM-dd HH:mm:ss",timezone ="GMT+8")
    private LocalDateTime operateTime;
}
