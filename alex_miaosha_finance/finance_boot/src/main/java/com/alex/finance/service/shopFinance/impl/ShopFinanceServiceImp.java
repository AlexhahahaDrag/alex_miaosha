package com.alex.finance.service.shopFinance.impl;

import com.alex.finance.entity.shopFinance.ShopFinance;
import com.alex.api.finance.vo.shopFinance.ShopFinanceVo;
import com.alex.finance.mapper.shopFinance.ShopFinanceMapper;
import com.alex.finance.service.shopFinance.ShopFinanceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import cn.hutool.core.bean.BeanUtil;
import com.alex.common.utils.string.StringUtils;

/**
 * <p>
 * description:  商店财务表服务实现类
 * author:       majf
 * createDate:   2024-02-23 21:19:49
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class ShopFinanceServiceImp extends ServiceImpl<ShopFinanceMapper, ShopFinance> implements ShopFinanceService {

    private final ShopFinanceMapper shopFinanceMapper;

    @Override
    public Page<ShopFinanceVo> getPage(Long pageNum, Long pageSize, ShopFinanceVo shopFinanceVo) {
        Page<ShopFinanceVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return shopFinanceMapper.getPage(page, shopFinanceVo);
    }

    @Override
    public ShopFinanceVo queryShopFinance(Long id) {
        return shopFinanceMapper.queryShopFinance(id);
    }

    @Override
    public Boolean addShopFinance(ShopFinanceVo shopFinanceVo) {
        ShopFinance shopFinance = new ShopFinance();
        BeanUtil.copyProperties(shopFinanceVo, shopFinance);
        shopFinanceMapper.insert(shopFinance);
        return true;
    }

    @Override
    public Boolean updateShopFinance(ShopFinanceVo shopFinanceVo) {
        ShopFinance shopFinance = new ShopFinance();
        BeanUtil.copyProperties(shopFinanceVo, shopFinance);
        shopFinanceMapper.updateById(shopFinance);
        return true;
    }

    @Override
    public Boolean deleteShopFinance(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        shopFinanceMapper.deleteBatchIds(idArr);
        return true;
    }
}
