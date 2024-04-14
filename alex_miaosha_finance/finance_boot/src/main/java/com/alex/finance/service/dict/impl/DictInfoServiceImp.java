package com.alex.finance.service.dict.impl;

import com.alex.api.finance.vo.dict.DictInfoVo;
import com.alex.common.exception.FinanceException;
import com.alex.common.redis.key.DictKey;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.finance.entity.dict.DictInfo;
import com.alex.finance.mapper.dict.DictInfoMapper;
import com.alex.finance.service.dict.DictInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * <p>
 * description: 字典表服务实现类
 * author: alex
 * createDate: 2022-10-13 17:47:15
 * version: 1.0.0
 */
@Service
@RequiredArgsConstructor
public class DictInfoServiceImp extends ServiceImpl<DictInfoMapper, DictInfo> implements DictInfoService {

    private final DictInfoMapper dictInfoMapper;

    private final RedisUtils redisUtils;

    @PostConstruct
    public void initDictInfo() {
        initDictRedis();
    }

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
        long count = getTypeCodeCount(dictInfoVo);
        if (count > 0) {
            throw new FinanceException("500", "字典编码已存在");
        }
        DictInfo dictInfo = new DictInfo();
        BeanUtils.copyProperties(dictInfoVo, dictInfo);
        dictInfoMapper.insert(dictInfo);
        initDictRedis();
        return dictInfo;
    }

    @Override
    public DictInfo updateDictInfo(DictInfoVo dictInfoVo) {
        long count = getTypeCodeCount(dictInfoVo);
        if (count > 0) {
            throw new FinanceException("500", "字典编码已存在");
        }
        DictInfo dictInfo = new DictInfo();
        BeanUtils.copyProperties(dictInfoVo, dictInfo);
        dictInfoMapper.updateById(dictInfo);
        initDictRedis();
        return dictInfo;
    }

    private Long getTypeCodeCount(DictInfoVo dictInfoVo) {
        LambdaQueryWrapper<DictInfo> query = Wrappers.<DictInfo>lambdaQuery()
                .eq(DictInfo::getTypeCode, dictInfoVo.getTypeCode())
                .eq(DictInfo::getBelongTo, dictInfoVo.getBelongTo())
                .eq(DictInfo::getIsDelete, 0);
        if (dictInfoVo.getId() != null) {
            query.ne(DictInfo::getId, dictInfoVo.getId());
        }
        return this.count(query);
    }

    @Override
    public Boolean deleteDictInfo(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        dictInfoMapper.deleteBatchIds(idArr);
        initDictRedis();
        return true;
    }

    @Override
    public DictInfo queryDictInfoByTypeName(String typeName) {
        LambdaQueryWrapper<DictInfo> query = Wrappers.<DictInfo>lambdaQuery().eq(DictInfo::getTypeName, typeName);
        return this.getOne(query);
    }

    @Override
    public DictInfo queryDictInfoByTypeCode(String typeCode) {
        LambdaQueryWrapper<DictInfo> query = Wrappers.<DictInfo>lambdaQuery().eq(DictInfo::getTypeName, typeCode);
        return this.getOne(query);
    }

    @Override
    public List<DictInfoVo> listByBelong(String belongTo) {
        String[] arr = null;
        if (!StringUtils.isEmpty(belongTo)) {
            arr = belongTo.split(",");
        }
        List<DictInfoVo> res = Lists.newArrayList();
        if (arr != null) {
            List<String> needSearch = Lists.newArrayList();
            for (String key : arr) {
                List<DictInfoVo> dictInfoVo = redisUtils.getList(DictKey.dictKey + ":" + key, DictInfoVo.class);
                if (dictInfoVo == null) {
                    needSearch.add(key);
                } else {
                    res.addAll(dictInfoVo);
                }
            }
            if (!needSearch.isEmpty()) {
                List<DictInfoVo> dictInfoVos = dictInfoMapper.listByBelong(needSearch.toArray(new String[0]));
                res.addAll(dictInfoVos);
                addDictToRedis(dictInfoVos);
            }
        } else {
            List<DictInfoVo> dictAll = redisUtils.keysList(DictKey.dictKey, DictInfoVo.class);
            if (dictAll == null || dictAll.isEmpty()) {
                dictAll = dictInfoMapper.listByBelong(null);
            }
            res.addAll(dictAll);
        }
        return res;
    }

    public boolean initDictRedis() {
        List<DictInfoVo> dictInfoList = dictInfoMapper.listByBelong(null);
        if (dictInfoList == null || dictInfoList.isEmpty()) {
            return true;
        }
        // 批量删除redis中的字典数据
        // TODO (majf) 2024/3/15 15:42 后期修改成lua脚本的方式
        dictInfoList.forEach(dictInfo -> redisUtils.delete(DictKey.dictKey, dictInfo.getBelongTo()));
        // 添加数据到redis中
        addDictToRedis(dictInfoList);
        return true;
    }

    public void addDictToRedis(List<DictInfoVo> dictInfoList) {
        Map<String, List<DictInfoVo>> dictMap = dictInfoList.parallelStream()
                .collect(Collectors.groupingBy(DictInfoVo::getBelongTo));
        for (Map.Entry<String, List<DictInfoVo>> entry : dictMap.entrySet()) {
            redisUtils.set(DictKey.dictKey, entry.getKey(), entry.getValue(), 0);
        }
    }
}
