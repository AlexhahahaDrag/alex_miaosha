package com.alex.finance.service.dict.impl;

import com.alex.finance.entity.dict.DictInfo;
import com.alex.finance.vo.dict.DictInfoVo;
import com.alex.finance.mapper.dict.DictInfoMapper;
import com.alex.finance.service.dict.DictInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import cn.hutool.core.bean.BeanUtil;
import com.alex.utils.string.StringUtils;
/**
 * <p>
 * @description:  字典表服务实现类
 * @author:       alex
 * @createDate:   2022-10-13 17:47:15
 * @version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class DictInfoServiceImp extends ServiceImpl<DictInfoMapper, DictInfo> implements DictInfoService {

    private final DictInfoMapper dictInfoMapper;

    @Override
    public Page<DictInfoVo> getPage(Long pageNum, Long pageSize, DictInfoVo dictInfoVo) {
        Page<DictInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return dictInfoMapper.getPage(page, dictInfoVo);
    }

    @Override
    public DictInfoVo queryDictInfo(String id) {
        return dictInfoMapper.queryDictInfo(id);
    }

    @Override
    public DictInfo addDictInfo(DictInfoVo dictInfoVo) {
        DictInfo dictInfo = new DictInfo();
        BeanUtil.copyProperties(dictInfoVo, dictInfo);
        dictInfoMapper.insert(dictInfo);
        return dictInfo;
    }

    @Override
    public DictInfo updateDictInfo(DictInfoVo dictInfoVo) {
        DictInfo dictInfo = new DictInfo();
        BeanUtil.copyProperties(dictInfoVo, dictInfo);
        dictInfoMapper.updateById(dictInfo);
        return dictInfo;
    }

    @Override
    public Boolean deleteDictInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        dictInfoMapper.deleteBatchIds(idArr);
        return true;
    }
}
