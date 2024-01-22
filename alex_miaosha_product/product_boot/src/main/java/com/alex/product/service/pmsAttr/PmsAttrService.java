package com.alex.product.service.pmsAttr;

import com.alex.api.product.vo.pmsAttr.PmsAttrVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.product.entity.pmsAttr.PmsAttr;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 商品属性 服务类
 * author: alex
 * createDate: 2023-12-28 19:50:53
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface PmsAttrService extends IService<PmsAttr> {

    Page<PmsAttrVo> getPage(Long pageNum, Long pageSize, PmsAttrVo pmsAttrVo);

    PmsAttrVo queryPmsAttr(String id);

    Boolean addPmsAttr(PmsAttrVo pmsAttrVo);

    Boolean updatePmsAttr(PmsAttrVo pmsAttrVo);

    Boolean deletePmsAttr(String ids);
}
