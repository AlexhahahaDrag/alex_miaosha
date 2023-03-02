package com.alex.product.mapper.pmsAttr;

import com.alex.product.entity.pmsAttr.PmsAttr;
import com.alex.api.product.vo.pmsAttr.PmsAttrVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description:  商品属性 mapper
 * @author:       alex
 * @createDate:   2023-03-02 19:15:30
 * @version:      1.0.0
 */
@Mapper
public interface PmsAttrMapper extends BaseMapper<PmsAttr> {

    Page<PmsAttrVo> getPage(Page<PmsAttrVo> page, @Param("pmsAttrVo") PmsAttrVo pmsAttrVo);

    PmsAttrVo queryPmsAttr(@Param("id") String id);
}
