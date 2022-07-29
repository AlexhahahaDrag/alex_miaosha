package com.alex.mission.pojo.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("seckill_goods")
@ApiModel(value = "SeckillGoods", description = "秒杀商品表")
public class SeckillGoods extends BaseEntity<SeckillGoods> {

    @ApiModelProperty(value = "商品id")
    @TableField("goods_id")
    private Long goodsId;

    @ApiModelProperty(value = "商品库存，-1表示没有限制")
    @TableField("stock_count")
    private Integer stockCount;
}
