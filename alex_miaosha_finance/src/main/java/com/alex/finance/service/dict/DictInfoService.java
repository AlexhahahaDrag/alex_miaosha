package com.alex.finance.service.dict;

import com.alex.finance.entity.dict.DictInfo;
import com.alex.finance.vo.dict.DictInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 字典表 服务类
 * @author: alex
 * @createDate: 2022-10-13 17:47:15
 * @description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface DictInfoService extends IService<DictInfo> {

    Page<DictInfoVo> getPage(Long pageNum, Long pageSize, DictInfoVo dictInfoVo);

    DictInfoVo queryDictInfo(String id);

    DictInfo addDictInfo(DictInfoVo dictInfoVo);

    DictInfo updateDictInfo(DictInfoVo dictInfoVo);

    Boolean deleteDictInfo(String ids);

    DictInfo queryDictInfoByTypeName(String typeName);

    DictInfo queryDictInfoByTypeCode(String typeCode);
}
