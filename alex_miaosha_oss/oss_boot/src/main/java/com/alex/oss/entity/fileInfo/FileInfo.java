package com.alex.oss.entity.fileInfo;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  文件信息表类
 * author:       alex
 * createDate: 2023-01-30 14:08:29
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("file_info")
@ApiModel(value = "FileInfo对象", description = "文件信息表")
public class FileInfo extends BaseEntity<FileInfo>{

    @ApiModelProperty(value = "文件名称")
    @TableField("file_name")
    private String fileName;

    @ApiModelProperty(value = "文件大小")
    @TableField("file_size")
    private Long fileSize;

    @ApiModelProperty(value = "文件类型")
    @TableField("file_type")
    private String fileType;

    @ApiModelProperty(value = "仓库名称")
    @TableField("bucket_name")
    private String bucketName;

    @ApiModelProperty(value = "文件系统")
    @TableField("file_system")
    private String fileSystem;

    @ApiModelProperty(value = "文件系统文件名称")
    @TableField("url")
    private String url;

}
