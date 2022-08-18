package com.alex.common.common;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseEntity<T extends Model<T>> extends Model<T> implements Serializable {

    @ApiModelProperty(value = "id")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty("创建时间")
    @TableField(value = "created_at", fill = FieldFill.INSERT, updateStrategy = FieldStrategy.DEFAULT)
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE, updateStrategy = FieldStrategy.DEFAULT)
    private LocalDateTime updatedAt;

    @ApiModelProperty("是否删除")
    @TableLogic
    private Integer isDelete;
}
