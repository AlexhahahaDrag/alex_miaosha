package com.alex.finance.service.shopStock.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alex.api.finance.vo.shopStock.ShopStockVo;
import com.alex.common.utils.string.StringUtils;
import com.alex.finance.entity.shopStock.ShopStock;
import com.alex.finance.mapper.shopStock.ShopStockMapper;
import com.alex.finance.service.shopStock.ShopStockService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * description:  商店库存表服务实现类
 * author:       alex
 * createDate:   2024-03-12 16:49:20
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class ShopStockServiceImp extends ServiceImpl<ShopStockMapper, ShopStock> implements ShopStockService {

    private final ShopStockMapper shopStockMapper;

    @Override
    public Page<ShopStockVo> getPage(Long pageNum, Long pageSize, ShopStockVo shopStockVo) {
        Page<ShopStockVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return shopStockMapper.getPage(page, shopStockVo);
    }

    @Override
    public ShopStockVo queryShopStock(Long id) {
        return shopStockMapper.queryShopStock(id);
    }

    @Override
    public Boolean addShopStock(ShopStockVo shopStockVo) {
        ShopStock shopStock = new ShopStock();
        BeanUtil.copyProperties(shopStockVo, shopStock);
        shopStockMapper.insert(shopStock);
        return true;
    }

    @Override
    public Boolean updateShopStock(ShopStockVo shopStockVo) {
        ShopStock shopStock = new ShopStock();
        BeanUtil.copyProperties(shopStockVo, shopStock);
        shopStockMapper.updateById(shopStock);
        return true;
    }

    @Override
    public Boolean deleteShopStock(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        shopStockMapper.deleteBatchIds(idArr);
        return true;
    }

    @Override
    public List<ShopStockVo> getShopList(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        return shopStockMapper.getShopList(Arrays.stream(ids.split(",")).map(Long::valueOf).collect(Collectors.toList()));
    }
}
