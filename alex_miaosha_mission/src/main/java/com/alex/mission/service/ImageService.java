package com.alex.mission.service;

import com.alex.common.utils.qiniu.ImageKit;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    /**
     * @param file
     * @param dir
     * @description: 上传文件
     * @author:      majf
     * @createDate:  2022/7/12 16:44
     * @return:      com.alex.common.utils.qiniu.ImageKit
    */
    ImageKit upload(MultipartFile file, String dir);

    /**
     * @param key
     * @description: 删除文件
     * @author:      majf
     * @createDate:  2022/7/12 16:45
     * @return:      boolean
    */
    boolean delete(String key);

    /**
     * @param keys
     * @description: 批量删除文件
     * @author:      majf
     * @createDate:  2022/7/12 16:45
     * @return:      boolean
    */
    boolean deletes(String[] keys);
}
