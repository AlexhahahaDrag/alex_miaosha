package com.alex.api.oss.vo.fileInfo;

import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  文件信息表Vo
 * author:       alex
 * createDate:   2023-01-30 14:08:29
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "FileInfoVo", description = "文件信息表Vo")
public class FileInfoVo extends BaseVo<FileInfoVo>{

    @ApiModelProperty(value = "文件名称")
    private String fileName;

    @ApiModelProperty(value = "文件大小")
    private Long fileSize;

    @ApiModelProperty(value = "文件类型")
    private String fileType;

    @ApiModelProperty(value = "仓库名称")
    private String bucketName;

    @ApiModelProperty(value = "文件系统")
    private String fileSystem;

    @ApiModelProperty(value = "文件系统文件名称")
    private String url;

    @ApiModelProperty(value = "预览url")
    private String preUrl;

    @ApiModelProperty(value = "缩略图url")
    private String thumbnailUrl;
}
