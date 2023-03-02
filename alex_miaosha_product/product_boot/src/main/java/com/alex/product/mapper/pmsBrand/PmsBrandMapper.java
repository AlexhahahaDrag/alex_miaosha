package com.alex.product.mapper.pmsBrand;

import com.alex.product.entity.pmsBrand.PmsBrand;
import com.alex.api.product.vo.pmsBrand.PmsBrandVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description:  品牌 mapper
 * @author:       alex
 * @createDate:   2023-03-02 15:31:52
 * @version:      1.0.0
 */
@Mapper
public interface PmsBrandMapper extends BaseMapper<PmsBrand> {

    Page<PmsBrandVo> getPage(Page<PmsBrandVo> page, @Param("pmsBrandVo") PmsBrandVo pmsBrandVo);

    PmsBrandVo queryPmsBrand(@Param("id") String id);
}
