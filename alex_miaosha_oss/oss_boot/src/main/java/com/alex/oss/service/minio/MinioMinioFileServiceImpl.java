package com.alex.oss.service.minio;

import cn.hutool.core.io.FileUtil;
import com.alex.api.oss.vo.fileInfo.FileInfoVo;
import com.alex.base.constants.SysConf;
import com.alex.common.enums.BucketNameEnum;
import com.alex.common.enums.FileSystemTypeEnum;
import com.alex.common.utils.date.DateUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.oss.config.minio.MinioTemplate;
import com.alex.oss.service.MinioFileService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
public class MinioMinioFileServiceImpl implements MinioFileService {

    private final MinioTemplate minioTemplate;

    private static final String YYYYMMDD = "YYYY-MM-DD";
    @Override
    public FileInfoVo uploadFile(MultipartFile file, String type) throws Exception {
        FileInfoVo fileVo = new FileInfoVo();
        String fileName = file.getOriginalFilename();
        fileVo.setFileName(fileName);
        fileVo.setFileSize(file.getSize());
        fileVo.setFileType(fileName.substring(fileName.lastIndexOf('.') + 1));
        String bucketName = getBucket(type);
        fileVo.setBucketName(bucketName);
        fileVo.setFileSystem(FileSystemTypeEnum.MINIO.getCode());
        String separator = System.getProperty("file.separator");
        String filename = type + separator + DateUtils.getNowTimeStr(YYYYMMDD) + separator + FileUtil.getPrefix(fileName)  + SysConf.UNDERLINE + DateUtils.getNowTimeLong() + SysConf.POINT + FileUtil.getSuffix(fileName);
        fileVo.setUrl(fileName);
        InputStream inputStream = file.getInputStream();
        Map<String, String> upload = minioTemplate.upload(bucketName, filename, inputStream, file.getContentType());
        fileVo.setFileName(upload.get("url"));
        return fileVo;
    }

    @Override
    public boolean deleteFile(List<String> filePath, String type) throws Exception {
        if (filePath == null || filePath.isEmpty()) {
            throw new Exception();
        }
        Map<String, String> stringStringMap = minioTemplate.removeObjects(getBucket(type), filePath);
        return stringStringMap.get("mes") != null;
    }

//    @Override
//    public InputStream fileDownload(String type, String fileName, Boolean delete, HttpServletResponse response) {
//        minioTemplate.fileDownload(getBucket(type), fileName, delete, response);
//        return null;
//    }

    private String getBucket(String type) {
        String bucketName;
        switch (StringUtils.isEmpty(type) ? "" : type) {
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
