package com.alex.common.common;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity<T extends Model<T>> extends Model<T> implements Serializable {

    @ApiModelProperty(value = "id")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty("创建人")
    @TableField(value = "creator", fill = FieldFill.INSERT, updateStrategy = FieldStrategy.DEFAULT)
    private Long creator;

    @ApiModelProperty("创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT, updateStrategy = FieldStrategy.DEFAULT)
    private LocalDateTime createTime;

    @ApiModelProperty("更新人")
    @TableField(value = "updater", fill = FieldFill.UPDATE, updateStrategy = FieldStrategy.DEFAULT)
    private Long updater;

    @ApiModelProperty("更新时间")
    @TableField(value = "update_time", fill = FieldFill.UPDATE, updateStrategy = FieldStrategy.DEFAULT)
    private LocalDateTime updateTime;

    @ApiModelProperty("操作人")
    @TableField(value = "operator", fill = FieldFill.INSERT_UPDATE, updateStrategy = FieldStrategy.DEFAULT)
    private Long operator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operate_time", fill = FieldFill.INSERT_UPDATE, updateStrategy = FieldStrategy.DEFAULT)
    private LocalDateTime operateTime;

    @ApiModelProperty("删除人")
    @TableField(value = "deleter")
    private Long deleter;

    @ApiModelProperty("操作时间")
    @TableField(value = "delete_time")
    private LocalDateTime deleteTime;

    @ApiModelProperty("是否删除")
    @TableLogic
    @TableField(value = "is_delete", fill = FieldFill.INSERT, select = false)
    private Integer isDelete;
}
