package com.alex.product.service.pmsSkuInfo.impl;

import com.alex.api.product.vo.pmsSkuInfo.PmsSkuInfoVo;
import com.alex.common.utils.bean.BeanUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.product.entity.pmsSkuInfo.PmsSkuInfo;
import com.alex.product.mapper.pmsSkuInfo.PmsSkuInfoMapper;
import com.alex.product.service.pmsSkuInfo.PmsSkuInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * description:  sku信息服务实现类
 * author:       alex
 * createDate:   2023-03-17 10:30:27
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class PmsSkuInfoServiceImp extends ServiceImpl<PmsSkuInfoMapper, PmsSkuInfo> implements PmsSkuInfoService {

    private final PmsSkuInfoMapper pmsSkuInfoMapper;

    @Override
    public Page<PmsSkuInfoVo> getPage(Long pageNum, Long pageSize, PmsSkuInfoVo pmsSkuInfoVo) {
        Page<PmsSkuInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return pmsSkuInfoMapper.getPage(page, pmsSkuInfoVo);
    }

    @Override
    public PmsSkuInfoVo queryPmsSkuInfo(String id) {
        return pmsSkuInfoMapper.queryPmsSkuInfo(id);
    }

    @Override
    public Boolean addPmsSkuInfo(PmsSkuInfoVo pmsSkuInfoVo) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        BeanUtils.copyProperties(pmsSkuInfoVo, pmsSkuInfo);
        pmsSkuInfoMapper.insert(pmsSkuInfo);
        return true;
    }

    @Override
    public Boolean updatePmsSkuInfo(PmsSkuInfoVo pmsSkuInfoVo) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        BeanUtils.copyProperties(pmsSkuInfoVo, pmsSkuInfo);
        pmsSkuInfoMapper.updateById(pmsSkuInfo);
        return true;
    }

    @Override
    public Boolean deletePmsSkuInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        pmsSkuInfoMapper.deleteBatchIds(idArr);
        return true;
    }
}
