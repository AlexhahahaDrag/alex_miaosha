package com.alex.product.service.pmsCategory;

import com.alex.api.product.vo.pmsCategory.PmsCategoryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.product.entity.pmsCategory.PmsCategory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 商品三级分类 服务类
 * author: alex
 * createDate: 2023-03-17 10:06:58
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface PmsCategoryService extends IService<PmsCategory> {

    Page<PmsCategoryVo> getPage(Long pageNum, Long pageSize, PmsCategoryVo pmsCategoryVo);

    PmsCategoryVo queryPmsCategory(String id);

    Boolean addPmsCategory(PmsCategoryVo pmsCategoryVo);

    Boolean updatePmsCategory(PmsCategoryVo pmsCategoryVo);

    Boolean deletePmsCategory(String ids);
}
