package com.alex.finance.shopOrder.service.impl;

import com.alex.api.finance.shopOrder.vo.ShopOrderVo;
import com.alex.api.finance.shopOrderDetail.vo.ShopOrderDetailVo;
import com.alex.base.constants.SysConf;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.FinanceException;
import com.alex.common.utils.string.StringUtils;
import com.alex.finance.entity.shopFinance.ShopFinance;
import com.alex.finance.entity.shopStock.ShopStock;
import com.alex.finance.service.shopFinance.ShopFinanceService;
import com.alex.finance.service.shopStock.ShopStockService;
import com.alex.finance.shopOrder.entity.ShopOrder;
import com.alex.finance.shopOrder.mapper.ShopOrderMapper;
import com.alex.finance.shopOrder.service.ShopOrderService;
import com.alex.finance.shopOrderDetail.service.ShopOrderDetailService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * <p>
 * description:  商店订单表服务实现类
 * author:       alex
 * createDate:   2024-04-09 15:20:01
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShopOrderServiceImp extends ServiceImpl<ShopOrderMapper, ShopOrder> implements ShopOrderService {

    private final ShopOrderMapper shopOrderMapper;

    private final ShopStockService shopStockService;

    private final Executor taskExecutor;

    private final ShopFinanceService shopFinanceService;

    private final ShopOrderDetailService shopOrderDetailService;

    @Override
    public Page<ShopOrderVo> getPage(Long pageNum, Long pageSize, ShopOrderVo shopOrderVo) {
        Page<ShopOrderVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return shopOrderMapper.getPage(page, shopOrderVo);
    }

    @Override
    public ShopOrderVo queryShopOrder(Long id) {
        return shopOrderMapper.queryShopOrder(id);
    }

    @Override
    public Boolean addShopOrder(ShopOrderVo shopOrderVo) {
        ShopOrder shopOrder = new ShopOrder();
        BeanUtils.copyProperties(shopOrderVo, shopOrder);
        shopOrderMapper.insert(shopOrder);
        return true;
    }

    @Override
    public Boolean updateShopOrder(ShopOrderVo shopOrderVo) {
        ShopOrder shopOrder = new ShopOrder();
        BeanUtils.copyProperties(shopOrderVo, shopOrder);
        shopOrderMapper.updateById(shopOrder);
        return true;
    }

    @Override
    public Boolean deleteShopOrder(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        shopOrderMapper.deleteBatchIds(idArr);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submitOrder(ShopOrderVo shopOrderVo) {
        List<ShopOrderDetailVo> shopOrderDetailVoList = shopOrderVo.getShopOrderDetailVoList();
        if (shopOrderDetailVoList == null || shopOrderDetailVoList.isEmpty()) {
            throw new FinanceException(ResultEnum.PARAM_ERROR);
        }
        List<Long> stockIdList = shopOrderDetailVoList.parallelStream()
                .map(ShopOrderDetailVo::getShopStockId)
                .toList();
        List<ShopStock> shopStockList = shopStockService.listByIds(stockIdList);
        if (shopStockList == null || shopStockList.isEmpty() || shopStockList.size() != shopOrderDetailVoList.size()) {
            throw new FinanceException(ResultEnum.FINANCE_NOT_EXISTS);
        }
        Map<Long, ShopStock> saleMap = shopStockList.parallelStream()
                .collect(Collectors.toMap(ShopStock::getId, item -> item));
        List<String> noSaveList = shopOrderDetailVoList.parallelStream().filter(item -> {
                    ShopStock dbStock = saleMap.get(item.getShopStockId());
                    dbStock.setSaleNum(dbStock.getSaleNum().subtract(item.getSaleNum()));
                    return BigDecimal.ZERO.compareTo(dbStock.getSaleNum()) > 0;
                }).map(ShopOrderDetailVo::getShopName)
                .toList();
        if (!noSaveList.isEmpty()) {
            throw new FinanceException(ResultEnum.FINANCE_NOT_SAVE);
        }
        // 1.保存订单数据
        // 计算商品总数
        BigDecimal saleCount = shopOrderDetailVoList.parallelStream().map(ShopOrderDetailVo::getSaleNum).reduce(BigDecimal.ZERO, BigDecimal::add);
        ShopOrder shopOrder = new ShopOrder();
        BeanUtils.copyProperties(shopOrderVo, shopOrder);
        shopOrder.setSaleCount(saleCount);
        shopOrder.insert();
        shopOrderVo.setId(shopOrder.getId());
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        taskExecutor.execute(() -> {
            try {
                RequestContextHolder.setRequestAttributes(attributes);
                //2. 更新订单明细
                shopOrderDetailVoList.forEach(item -> item.setOrderId(shopOrder.getId()));
                shopOrderDetailService.batchUpdateShopOrderDetail(shopOrderDetailVoList);
            } catch (Exception e) {
                // TODO (majf) 2024/4/9 15:53 将来插入到日志表中，方便补偿数据
                log.error("更新订单明细失败：{}", e.getMessage());
            }
        });
        taskExecutor.execute(() -> {
            try {
                RequestContextHolder.setRequestAttributes(attributes);
                //2. 更新库存信息
                shopStockService.updateBatchById(shopStockList);
            } catch (Exception e) {
                log.error("更新库存信息失败：{}", e.getMessage());
            }
        });
        taskExecutor.execute(() -> {
            try {
                //2. 更新销售信息
                RequestContextHolder.setRequestAttributes(attributes);
                saveFinanceInfo(shopOrderVo);
            } catch (Exception e) {
                log.error("更新库存信息失败：{}", e.getMessage());
            }
        });
        // TODO (majf) 2024/4/9 16:02 减少购物车中的信息
        return true;
    }

    private void saveFinanceInfo(ShopOrderVo shopOrderVo) {
        List<ShopOrderDetailVo> shopOrderDetailVoList = shopOrderVo.getShopOrderDetailVoList();
        // 获取总的销售金额
        BigDecimal sum = shopOrderDetailVoList.parallelStream()
                .map(item -> item.getSaleNum().multiply(item.getSaleAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean needReallocation = shopOrderVo.getSaleAmount().compareTo(sum) != 0;
        BigDecimal sumNum  = BigDecimal.ZERO;
        BigDecimal avg = BigDecimal.ZERO;
        if (needReallocation) {
            sumNum = shopOrderDetailVoList.parallelStream().map(ShopOrderDetailVo::getSaleNum).reduce(BigDecimal.ZERO, BigDecimal::add);
            avg = sum.subtract(shopOrderVo.getSaleAmount()).divide(sumNum, RoundingMode.DOWN);
        }
        BigDecimal finalSumNum = sumNum;
        BigDecimal finalAvg = avg;
        AtomicBoolean first = new AtomicBoolean(true);
        List<ShopFinance> shopFinanceList = shopOrderDetailVoList.stream().map(item -> {
            log.info("订单信息：{}", JSONObject.toJSONString(item));
            ShopFinance shopFinance = new ShopFinance();
            BeanUtils.copyProperties(item, shopFinance, "id");
            shopFinance.setSaleAmount(getSaleAmount(item.getSaleAmount(), item.getSaleNum(), needReallocation, first.get(),
                    sum, shopOrderVo.getSaleAmount(), finalAvg, finalSumNum));
            first.set(false);
            shopFinance.setIncomeAndExpenses(SysConf.INCOME_STATUS);
            shopFinance.setIncomeAndExpenses(shopOrderVo.getPayWay());
            // TODO (majf) 2024/3/22 15:55 测试将来修改成有效
            shopFinance.setIsValid(SysConf.INVALID_STATUS);
            shopFinance.setShopOrderId(shopOrderVo.getId());
            shopFinance.setIncomeAndExpenses(SysConf.INCOME_STATUS);
            return shopFinance;
        }).toList();
        shopFinanceService.saveBatch(shopFinanceList);
    }

    private BigDecimal getSaleAmount(BigDecimal sale, BigDecimal saleNum, boolean needReallocation, boolean first,
                                     BigDecimal sum, BigDecimal sumSaleAmount, BigDecimal avg, BigDecimal sumNum){
        BigDecimal saleAmount = sale.multiply(saleNum);
        if (needReallocation) {
            BigDecimal rest;
            if (first) {
                rest = sum.subtract(sumSaleAmount).subtract(avg.multiply(sumNum.subtract(BigDecimal.ONE)));
            } else {
                rest = avg;
            }
            saleAmount = saleAmount.subtract(rest);
        }
        return saleAmount;
    }
}
