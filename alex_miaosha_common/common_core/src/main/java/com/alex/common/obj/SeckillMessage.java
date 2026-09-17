package com.alex.common.obj;

import lombok.Builder;
import lombok.Data;

/**
 *description:  秒杀消息类
 *author:       majf
 *createDate:   2022/7/14 10:03
 *version:      1.0.0
 */
@Data
@Builder
public class SeckillMessage {

    private Long userId;

    private Long goodsId;
}
