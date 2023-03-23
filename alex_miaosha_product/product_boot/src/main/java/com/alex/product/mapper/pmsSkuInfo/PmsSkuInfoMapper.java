package com.alex.product.mapper.pmsSkuInfo;

import com.alex.product.entity.pmsSkuInfo.PmsSkuInfo;
import com.alex.api.product.vo.pmsSkuInfo.PmsSkuInfoVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description:  sku信息 mapper
 * @author:       alex
 * @createDate:   2023-03-17 10:30:26
 * @version:      1.0.0
 */
@Mapper
public interface PmsSkuInfoMapper extends BaseMapper<PmsSkuInfo> {

    Page<PmsSkuInfoVo> getPage(Page<PmsSkuInfoVo> page, @Param("pmsSkuInfoVo") PmsSkuInfoVo pmsSkuInfoVo);

    PmsSkuInfoVo queryPmsSkuInfo(@Param("id") String id);
}
