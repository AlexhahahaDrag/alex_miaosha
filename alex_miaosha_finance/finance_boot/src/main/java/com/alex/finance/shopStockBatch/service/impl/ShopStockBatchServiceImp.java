package com.alex.finance.shopStockBatch.service.impl;

import com.alex.api.finance.shopStockBatch.vo.ShopStockBatchVo;
import com.alex.common.utils.string.StringUtils;
import com.alex.finance.shopStockBatch.entity.ShopStockBatch;
import com.alex.finance.shopStockBatch.mapper.ShopStockBatchMapper;
import com.alex.finance.shopStockBatch.service.ShopStockBatchService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * description:  商店库存批次表服务实现类
 * author:       alex
 * createDate:   2024-05-10 17:30:40
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class ShopStockBatchServiceImp extends ServiceImpl<ShopStockBatchMapper, ShopStockBatch> implements ShopStockBatchService {

    private final ShopStockBatchMapper shopStockBatchMapper;

    @Override
    public Page<ShopStockBatchVo> getPage(Long pageNum, Long pageSize, ShopStockBatchVo shopStockBatchVo) {
        Page<ShopStockBatchVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return shopStockBatchMapper.getPage(page, shopStockBatchVo);
    }

    @Override
    public ShopStockBatchVo queryShopStockBatch(Long id) {
        return shopStockBatchMapper.queryShopStockBatch(id);
    }

    @Override
    public Boolean addShopStockBatch(ShopStockBatchVo shopStockBatchVo) {
        ShopStockBatch shopStockBatch = new ShopStockBatch();
        BeanUtils.copyProperties(shopStockBatchVo, shopStockBatch);
        shopStockBatchMapper.insert(shopStockBatch);
        return true;
    }

    @Override
    public Boolean updateShopStockBatch(ShopStockBatchVo shopStockBatchVo) {
        ShopStockBatch shopStockBatch = new ShopStockBatch();
        BeanUtils.copyProperties(shopStockBatchVo, shopStockBatch);
        shopStockBatchMapper.updateById(shopStockBatch);
        return true;
    }

    @Override
    public Boolean deleteShopStockBatch(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        shopStockBatchMapper.deleteBatchIds(idArr);
        return true;
    }

    @Override
    public List<ShopStockBatchVo> getList(ShopStockBatchVo shopStockBatchVo) {
        return shopStockBatchMapper.getList(shopStockBatchVo);
    }
}
