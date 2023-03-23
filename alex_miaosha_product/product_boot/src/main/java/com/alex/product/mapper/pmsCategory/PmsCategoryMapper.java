package com.alex.product.mapper.pmsCategory;

import com.alex.product.entity.pmsCategory.PmsCategory;
import com.alex.api.product.vo.pmsCategory.PmsCategoryVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description:  商品三级分类 mapper
 * @author:       alex
 * @createDate:   2023-03-17 10:06:58
 * @version:      1.0.0
 */
@Mapper
public interface PmsCategoryMapper extends BaseMapper<PmsCategory> {

    Page<PmsCategoryVo> getPage(Page<PmsCategoryVo> page, @Param("pmsCategoryVo") PmsCategoryVo pmsCategoryVo);

    PmsCategoryVo queryPmsCategory(@Param("id") String id);
}
