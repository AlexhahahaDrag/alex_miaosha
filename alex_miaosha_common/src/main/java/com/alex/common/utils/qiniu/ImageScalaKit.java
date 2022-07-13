package com.alex.common.utils.qiniu;

import cn.hutool.core.lang.UUID;
import com.alex.common.config.qiniu.QiNiuProperties;
import com.alex.common.enums.ResultEnum;
import com.alex.common.exception.CustomizeException;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.model.DefaultPutRet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

/**
 *description:  七牛工具类
 *author:       majf
 *createDate:   2022/7/12 14:33
 *version:      1.0.0
 */
@Component
@RequiredArgsConstructor
public class ImageScalaKit {

    private final QiNiuUploadKit qiNiuUploadKit;

    private final QiNiuProperties qiNiuProperties;

    /**
     * @param file
     * @param prefix
     * @description: 上传图片
     * @author:      majf
     * @createDate:  2022/7/12 16:15
     * @return:      com.alex.common.utils.qiniu.ImageKit
    */
    public ImageKit upload(MultipartFile file, String prefix) {
        ImageKit imageKit;
        try {
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            fileExtension(file);
            DefaultPutRet ret = qiNiuUploadKit.upload(file.getBytes(), prefix + "/" + uuid);
            return initResult(ret);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CustomizeException(ResultEnum.IMAGE_UPLOAD_FAIL);
        }
    }

    /**
     * @param imgUrl
     * @description: 删除图片
     * @author:      majf
     * @createDate:  2022/7/12 16:15
     * @return:      void
    */
    public void delete(String imgUrl) {
        String key = ImageKit.getKey(imgUrl, qiNiuProperties.getDomain());
        try {
            qiNiuUploadKit.delete(key);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param ret
     * @description: 初始化结果
     * @author:      majf
     * @createDate:  2022/7/12 16:15
     * @return:      com.alex.common.utils.qiniu.ImageKit
    */
    private ImageKit initResult(DefaultPutRet ret) {
        return ImageKit.builder().key(ret.key).hash(ret.hash).url(qiNiuProperties.getDomain() + ret.key).build();
    }

    /**
     * @param file
     * @description: 获取图片后缀名
     * @author:      majf
     * @createDate:  2022/7/12 16:12
     * @return:      java.lang.String
    */
    private String fileExtension(MultipartFile file) {
        int index = Objects.requireNonNull(file.getOriginalFilename()).lastIndexOf(".");
        if (index == -1 || file.getOriginalFilename().length() == 0) {
            return "";
        }
        return file.getOriginalFilename().substring(index);
    }
}
