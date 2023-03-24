package com.alex.product.service.pmsBrand.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alex.api.oss.api.OssApi;
import com.alex.api.oss.vo.fileInfo.FileInfoVo;
import com.alex.api.product.vo.pmsBrand.PmsBrandVo;
import com.alex.base.common.Result;
import com.alex.common.utils.string.StringUtils;
import com.alex.product.entity.pmsBrand.PmsBrand;
import com.alex.product.mapper.pmsBrand.PmsBrandMapper;
import com.alex.product.service.pmsBrand.PmsBrandService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * @description:  品牌服务实现类
 * @author:       alex
 * @createDate:   2023-03-05 21:39:54
 * @version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class PmsBrandServiceImp extends ServiceImpl<PmsBrandMapper, PmsBrand> implements PmsBrandService {

    private final PmsBrandMapper pmsBrandMapper;

    private final OssApi ossApi;

    @Override
    public Page<PmsBrandVo> getPage(Long pageNum, Long pageSize, PmsBrandVo pmsBrandVo) {
        Page<PmsBrandVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        Page<PmsBrandVo> brankPage = pmsBrandMapper.getPage(page, pmsBrandVo);
        List<PmsBrandVo> records = brankPage.getRecords();
        if (records == null || records.isEmpty()) {
            return brankPage;
        }
        List<Long> fileIdList = records.parallelStream()
                .filter(item -> item.getLogo() != null)
                .map(PmsBrandVo::getLogo)
                .collect(Collectors.toList());
        try {
            Result<List<FileInfoVo>> result = ossApi.getFileInfo(fileIdList);
            if ("200".equals(result.getCode()) && result.getData() != null && !result.getData().isEmpty()) {
                Map<Long, List<FileInfoVo>> fileMap = result.getData()
                        .parallelStream()
                        .collect(Collectors.groupingBy(FileInfoVo::getId));
                records.forEach(item -> {
                    List<FileInfoVo> fileInfoVos = fileMap.get(item.getLogo());
                    if (fileInfoVos != null && !fileInfoVos.isEmpty()) {
                        item.setLogoUrl(fileInfoVos.get(0).getPreUrl());
                    }
                });
            }
        } catch (Exception e) {
            log.error("获取用户头像失败！");
        }
        return brankPage;
    }

    @Override
    public PmsBrandVo queryPmsBrand(String id) {
        PmsBrandVo pmsBrandVo = pmsBrandMapper.queryPmsBrand(id);
        //如果不存在logo直接返回数据
        if (pmsBrandVo == null || pmsBrandVo.getLogo() == null) {
            return pmsBrandVo;
        }
        //在文件系统中查询logo
        Result<List<FileInfoVo>> result = ossApi.getFileInfo(Lists.newArrayList(pmsBrandVo.getLogo()));
        if ("200".equals(result.getCode())) {
            pmsBrandVo.setLogoUrl(Optional.ofNullable(result.getData()).map(item -> item.get(0).getPreUrl()).orElse(""));
        }
        return pmsBrandVo;
    }

    @Override
    public Boolean addPmsBrand(PmsBrandVo pmsBrandVo) {
        PmsBrand pmsBrand = new PmsBrand();
        BeanUtil.copyProperties(pmsBrandVo, pmsBrand);
        pmsBrandMapper.insert(pmsBrand);
        return true;
    }

    @Override
    public Boolean updatePmsBrand(PmsBrandVo pmsBrandVo) {
        PmsBrand pmsBrand = new PmsBrand();
        BeanUtil.copyProperties(pmsBrandVo, pmsBrand);
        pmsBrandMapper.updateById(pmsBrand);
        return true;
    }

    @Override
    public Boolean deletePmsBrand(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        pmsBrandMapper.deleteBatchIds(idArr);
        return true;
    }

    @Override
    public String test() {
        return "123";
    }
}
