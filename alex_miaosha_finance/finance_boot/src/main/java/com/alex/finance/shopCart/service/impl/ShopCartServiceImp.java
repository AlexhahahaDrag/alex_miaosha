package com.alex.finance.shopCart.service.impl;

import com.alex.finance.shopCart.entity.ShopCart;
import com.alex.api.finance.shopCart.vo.ShopCartVo;
import com.alex.finance.shopCart.mapper.ShopCartMapper;
import com.alex.finance.shopCart.service.ShopCartService;
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
 * description:  购物车表服务实现类
 * author:       alex
 * createDate:   2024-04-03 11:36:19
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class ShopCartServiceImp extends ServiceImpl<ShopCartMapper, ShopCart> implements ShopCartService {

    private final ShopCartMapper shopCartMapper;

    @Override
    public Page<ShopCartVo> getPage(Long pageNum, Long pageSize, ShopCartVo shopCartVo) {
        Page<ShopCartVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return shopCartMapper.getPage(page, shopCartVo);
    }

    @Override
    public ShopCartVo queryShopCart(Long id) {
        return shopCartMapper.queryShopCart(id);
    }

    @Override
    public Boolean addShopCart(ShopCartVo shopCartVo) {
        ShopCart shopCart = new ShopCart();
        BeanUtils.copyProperties(shopCartVo, shopCart);
        shopCartMapper.insert(shopCart);
        return true;
    }

    @Override
    public Boolean updateShopCart(ShopCartVo shopCartVo) {
        ShopCart shopCart = new ShopCart();
        BeanUtils.copyProperties(shopCartVo, shopCart);
        shopCartMapper.updateById(shopCart);
        return true;
    }

    @Override
    public Boolean deleteShopCart(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        shopCartMapper.deleteBatchIds(idArr);
        return true;
    }
}
