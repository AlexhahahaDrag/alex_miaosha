package com.alex.finance.shopCart.service.impl;

import com.alex.api.finance.shopCart.vo.ShopCartVo;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.FinanceException;
import com.alex.common.utils.string.StringUtils;
import com.alex.finance.shopCart.entity.ShopCart;
import com.alex.finance.shopCart.mapper.ShopCartMapper;
import com.alex.finance.shopCart.service.ShopCartService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

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
        // 根据人员和商品id查询商品是否存在
        // TODO: 2024/4/14 后期修改成按照机构编码校验 
        Wrapper<ShopCart> query = Wrappers.<ShopCart>lambdaQuery()
                .eq(ShopCart::getShopId, shopCartVo.getShopId());
        List<ShopCart> list = this.list(query);
        if (!list.isEmpty()) {
            throw new FinanceException(ResultEnum.FINANCE_NOT_IN_CART);
        }
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

    @Override
    public List<ShopCartVo> list(String ids) {
        List<Long> id = null;
        if(!StringUtils.isEmpty(ids)) {
            id = Arrays.stream(ids.split(",")).map(Long::valueOf).toList();
        }
        return shopCartMapper.list(id);
    }
}
