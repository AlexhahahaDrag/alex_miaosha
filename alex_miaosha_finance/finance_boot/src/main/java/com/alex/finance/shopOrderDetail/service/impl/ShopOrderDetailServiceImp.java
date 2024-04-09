package com.alex.finance.shopOrderDetail.service.impl;

import com.alex.api.finance.shopOrderDetail.vo.ShopOrderDetailVo;
import com.alex.common.utils.string.StringUtils;
import com.alex.finance.shopOrderDetail.entity.ShopOrderDetail;
import com.alex.finance.shopOrderDetail.mapper.ShopOrderDetailMapper;
import com.alex.finance.shopOrderDetail.service.ShopOrderDetailService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * description:  商店订单明细表服务实现类
 * author:       alex
 * createDate:   2024-04-09 15:35:21
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class ShopOrderDetailServiceImp extends ServiceImpl<ShopOrderDetailMapper, ShopOrderDetail> implements ShopOrderDetailService {

    private final ShopOrderDetailMapper shopOrderDetailMapper;

    @Override
    public Page<ShopOrderDetailVo> getPage(Long pageNum, Long pageSize, ShopOrderDetailVo shopOrderDetailVo) {
        Page<ShopOrderDetailVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return shopOrderDetailMapper.getPage(page, shopOrderDetailVo);
    }

    @Override
    public ShopOrderDetailVo queryShopOrderDetail(Long id) {
        return shopOrderDetailMapper.queryShopOrderDetail(id);
    }

    @Override
    public Boolean addShopOrderDetail(ShopOrderDetailVo shopOrderDetailVo) {
        ShopOrderDetail shopOrderDetail = new ShopOrderDetail();
        BeanUtils.copyProperties(shopOrderDetailVo, shopOrderDetail);
        shopOrderDetailMapper.insert(shopOrderDetail);
        return true;
    }

    @Override
    public Boolean updateShopOrderDetail(ShopOrderDetailVo shopOrderDetailVo) {
        ShopOrderDetail shopOrderDetail = new ShopOrderDetail();
        BeanUtils.copyProperties(shopOrderDetailVo, shopOrderDetail);
        shopOrderDetailMapper.updateById(shopOrderDetail);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchUpdateShopOrderDetail(List<ShopOrderDetailVo> shopOrderDetailVo) {
        List<ShopOrderDetail> shopOrderDetails = shopOrderDetailVo.parallelStream().map(item -> {
            ShopOrderDetail shopOrderDetail = new ShopOrderDetail();
            BeanUtils.copyProperties(item, shopOrderDetail);
            return shopOrderDetail;
        }).toList();
        return saveBatch(shopOrderDetails);
    }

    @Override
    public Boolean deleteShopOrderDetail(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        shopOrderDetailMapper.deleteBatchIds(idArr);
        return true;
    }
}
