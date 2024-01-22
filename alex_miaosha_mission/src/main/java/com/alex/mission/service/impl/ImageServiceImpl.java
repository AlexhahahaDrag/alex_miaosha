package com.alex.mission.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl {
//public class ImageServiceImpl implements ImageService {

//    private final ImageScalaKit imageScalaKit;

//    @Override
//    public ImageKit upload(MultipartFile file, String dir) {
//        ImageDirEnum dirEnum = EnumsUtils.getByCode(dir, ImageDirEnum.class);
//        if (dirEnum == null) {
//            throw new CustomizeException(ResultEnum.IMAGE_ENUM_NOT_FOUND);
//        }
//        return imageScalaKit.upload(file, dirEnum.getValue());
//    }
//
//    @Override
//    public boolean delete(String key) {
//        imageScalaKit.delete(key);
//        return true;
//    }
//
//    @Override
//    public boolean deletes(String[] keys) {
//        for(String key : keys) {
//            imageScalaKit.delete(key);
//        }
//        return true;
//    }
}
