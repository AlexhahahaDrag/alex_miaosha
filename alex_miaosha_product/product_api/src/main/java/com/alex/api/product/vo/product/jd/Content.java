package com.alex.api.product.vo.product.jd;

import lombok.*;

/**
 * @description:
 * @author:      alex
 * @createTime:  2021/2/26 15:01
 * @version:     1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Content {

    private String image;

    private String price;

    private String name;

    private String shop;

    private String icons;

    private String productUrl;

    private String source;

    private String searchKey;

    private String skuId;
}
