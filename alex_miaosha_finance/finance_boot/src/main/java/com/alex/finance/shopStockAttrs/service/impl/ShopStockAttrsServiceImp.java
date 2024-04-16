package com.alex.finance.shopStockAttrs.service.impl;

import com.alex.finance.shopStockAttrs.entity.ShopStockAttrs;
import com.alex.api.finance.shopStockAttrs.vo.ShopStockAttrsVo;
import com.alex.finance.shopStockAttrs.mapper.ShopStockAttrsMapper;
import com.alex.finance.shopStockAttrs.service.ShopStockAttrsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import com.alex.common.utils.string.StringUtils;

/**
 * <p>
 * description:  商店库存属性表服务实现类
 * author:       alex
 * createDate:   2024-04-16 19:50:29
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class ShopStockAttrsServiceImp extends ServiceImpl<ShopStockAttrsMapper, ShopStockAttrs> implements ShopStockAttrsService {

    private final ShopStockAttrsMapper shopStockAttrsMapper;

    @Override
    public Page<ShopStockAttrsVo> getPage(Long pageNum, Long pageSize, ShopStockAttrsVo shopStockAttrsVo) {
        Page<ShopStockAttrsVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return shopStockAttrsMapper.getPage(page, shopStockAttrsVo);
    }

    @Override
    public ShopStockAttrsVo queryShopStockAttrs(Long id) {
        return shopStockAttrsMapper.queryShopStockAttrs(id);
    }

    @Override
    public Boolean addShopStockAttrs(ShopStockAttrsVo shopStockAttrsVo) {
        ShopStockAttrs shopStockAttrs = new ShopStockAttrs();
        BeanUtils.copyProperties(shopStockAttrsVo, shopStockAttrs);
        shopStockAttrsMapper.insert(shopStockAttrs);
        return true;
    }

    @Override
    public Boolean updateShopStockAttrs(ShopStockAttrsVo shopStockAttrsVo) {
        ShopStockAttrs shopStockAttrs = new ShopStockAttrs();
        BeanUtils.copyProperties(shopStockAttrsVo, shopStockAttrs);
        shopStockAttrsMapper.updateById(shopStockAttrs);
        return true;
    }

    @Override
    public Boolean deleteShopStockAttrs(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        shopStockAttrsMapper.deleteBatchIds(idArr);
        return true;
    }
}