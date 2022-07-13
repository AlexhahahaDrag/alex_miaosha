package com.alex.common.utils.qiniu;

import lombok.Builder;
import lombok.Data;

/**
 *description:
 *author:       majf
 *createDate:   2022/7/12 15:51
 *version:      1.0.0
 */
@Data
@Builder
public class ImageKit {

    public static final String thumb_suffix = "-thumb";

    public static final String MIDDLE_SUFFIX = "-middle";

    private String url;

    private String key;

    private String hash;

    //0:标识正常，1:标识非法
    private int code = 0;

    static String getKey(String imageUrl, String domain) {
        return imageUrl.replace(domain.concat("/"), "");
    }
}
