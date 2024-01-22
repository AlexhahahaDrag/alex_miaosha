package com.alex.finance.mapper.dict;

import com.alex.api.finance.vo.dict.DictInfoVo;
import com.alex.finance.entity.dict.DictInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description:  字典表 mapper
 * author:       alex
 * createDate:   2022-10-13 17:47:15
 * version:      1.0.0
 */
@Mapper
public interface DictInfoMapper extends BaseMapper<DictInfo> {

    Page<DictInfoVo> getPage(Page<DictInfoVo> page, @Param("dictInfoVo") DictInfoVo dictInfoVo);

    DictInfoVo queryDictInfo(@Param("id") String id);

    List<DictInfoVo> listByBelong(@Param("belongTo") String[] belongTo);
}
