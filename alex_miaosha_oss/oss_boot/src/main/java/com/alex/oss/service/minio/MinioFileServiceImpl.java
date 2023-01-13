package com.alex.oss.service.minio;

import cn.hutool.core.io.FileUtil;
import com.alex.base.constants.SysConf;
import com.alex.common.enums.BucketNameEnum;
import com.alex.oss.config.minio.MinioTemplate;
import com.alex.oss.service.FileService;
import com.alex.oss.vo.FileInfoVo;
import com.alex.utils.date.DateUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * description:
 * author:       majf
 * createDate:   2023/1/12 14:48
 * version:      1.0.0
 */
@Service
@AllArgsConstructor
public class MinioFileServiceImpl implements FileService {

    private final MinioTemplate minioTemplate;

    @Override
    public FileInfoVo uploadFile(MultipartFile file, String type) throws Exception {
        String fileName = file.getOriginalFilename();
        String filename = FileUtil.getPrefix(fileName)  + SysConf.UNDERLINE + DateUtils.getNowTimeLong() + SysConf.POINT + FileUtil.getSuffix(fileName);
        InputStream inputStream = file.getInputStream();
        Map<String, String> upload = minioTemplate.upload(getBucket(type), filename, inputStream, file.getContentType());
        FileInfoVo fileInfoVo = new FileInfoVo();
        fileInfoVo.setFileName(upload.get("url"));
        return fileInfoVo;
    }

    @Override
    public boolean deleteFile(List<String> filePath, String type) throws Exception {
        if (filePath == null || filePath.isEmpty()) {
            throw new Exception();
        }
        Map<String, String> stringStringMap = minioTemplate.removeObjects(getBucket(type), filePath);
        return stringStringMap.get("mes") != null;
    }

    @Override
    public InputStream fileDownload(String type, String fileName, Boolean delete, HttpServletResponse response) {
        minioTemplate.fileDownload(getBucket(type), fileName, delete, response);
        return null;
    }

    private String getBucket(String type) {
        String bucketName;
        switch (type) {
            case "user" :
                bucketName = BucketNameEnum.USER_BUCKET.getValue();
                break;
            case "goods" :
                bucketName = BucketNameEnum.GOODS_BUCKET.getValue();
                break;
            default:
                bucketName = BucketNameEnum.COMMON_BUCKET.getValue();
        }
        return bucketName;
    }
}
