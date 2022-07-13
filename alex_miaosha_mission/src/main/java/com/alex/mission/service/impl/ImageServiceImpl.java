package com.alex.mission.service.impl;

import com.alex.common.enums.ImageDirEnum;
import com.alex.common.enums.ResultEnum;
import com.alex.common.exception.CustomizeException;
import com.alex.common.utils.EnumUtil;
import com.alex.common.utils.qiniu.ImageKit;
import com.alex.common.utils.qiniu.ImageScalaKit;
import com.alex.mission.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final ImageScalaKit imageScalaKit;

    @Override
    public ImageKit upload(MultipartFile file, String dir) {
        ImageDirEnum dirEnum = EnumUtil.getByCode(dir, ImageDirEnum.class);
        if (dirEnum == null) {
            throw new CustomizeException(ResultEnum.IMAGE_ENUM_NOT_FOUND);
        }
        return imageScalaKit.upload(file, dirEnum.getValue());
    }

    @Override
    public boolean delete(String key) {
        imageScalaKit.delete(key);
        return true;
    }

    @Override
    public boolean deletes(String[] keys) {
        for(String key : keys) {
            imageScalaKit.delete(key);
        }
        return true;
    }
}
