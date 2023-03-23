package com.alex.product.service.pmsSkuInfo;

import com.alex.api.product.vo.pmsSkuInfo.PmsSkuInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.product.entity.pmsSkuInfo.PmsSkuInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * sku信息 服务类
 * @author: alex
 * @createDate: 2023-03-17 10:30:27
 * @description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface PmsSkuInfoService extends IService<PmsSkuInfo> {

    Page<PmsSkuInfoVo> getPage(Long pageNum, Long pageSize, PmsSkuInfoVo pmsSkuInfoVo);

    PmsSkuInfoVo queryPmsSkuInfo(String id);

    Boolean addPmsSkuInfo(PmsSkuInfoVo pmsSkuInfoVo);

    Boolean updatePmsSkuInfo(PmsSkuInfoVo pmsSkuInfoVo);

    Boolean deletePmsSkuInfo(String ids);
}
