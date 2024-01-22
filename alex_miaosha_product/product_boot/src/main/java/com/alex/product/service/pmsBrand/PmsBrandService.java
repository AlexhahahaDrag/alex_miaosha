package com.alex.product.service.pmsBrand;

import com.alex.api.product.vo.pmsBrand.PmsBrandVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.product.entity.pmsBrand.PmsBrand;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 品牌 服务类
 * author: alex
 * createDate: 2023-03-05 21:39:54
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface PmsBrandService extends IService<PmsBrand> {

    Page<PmsBrandVo> getPage(Long pageNum, Long pageSize, PmsBrandVo pmsBrandVo);

    PmsBrandVo queryPmsBrand(String id);

    Boolean addPmsBrand(PmsBrandVo pmsBrandVo);

    Boolean updatePmsBrand(PmsBrandVo pmsBrandVo);

    Boolean deletePmsBrand(String ids);

    String test();
}
