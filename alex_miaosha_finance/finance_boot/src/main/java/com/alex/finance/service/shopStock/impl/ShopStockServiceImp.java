package com.alex.finance.service.shopStock.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alex.api.finance.vo.saleOrder.SaleOrderVo;
import com.alex.api.finance.vo.shopStock.ShopStockVo;
import com.alex.base.constants.SysConf;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.FinanceException;
import com.alex.common.utils.string.StringUtils;
import com.alex.finance.entity.shopFinance.ShopFinance;
import com.alex.finance.entity.shopStock.ShopStock;
import com.alex.finance.mapper.shopStock.ShopStockMapper;
import com.alex.finance.service.shopFinance.ShopFinanceService;
import com.alex.finance.service.shopStock.ShopStockService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
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
@Slf4j
public class ShopStockServiceImp extends ServiceImpl<ShopStockMapper, ShopStock> implements ShopStockService {

    private final ShopStockMapper shopStockMapper;

    private final ShopFinanceService shopFinanceService;

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
        // TODO (majf) 2024/3/22 11:52 生成商品编码  时间 + 地点 + 类别 + 序号
        // shopStock.setCode(DateUtil.format(new Date(), "yyyyMMddHHmmssSSS") + shopStockVo.getLocation() + shopStockVo.getCategory() + shopStockVo.getSeq());
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
        if (StringUtils.isEmpty(ids)) {
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submitOrder(SaleOrderVo saleOrderVo) throws Exception {
        List<ShopStockVo> shopStockVoList = saleOrderVo.getShopStockVoList();
        if (shopStockVoList == null || shopStockVoList.isEmpty()) {
            throw new FinanceException(ResultEnum.PARAM_ERROR);
        }
        // // TODO (majf) 2024/3/22 13:55 校验库存
        List<Long> stockIdList = shopStockVoList.parallelStream()
                .map(ShopStockVo::getId)
                .toList();
        List<ShopStock> shopStockList = shopStockMapper.selectBatchIds(stockIdList);
        log.info("库存:{}", JSONObject.toJSONString(shopStockList));
        if (shopStockList == null || shopStockList.isEmpty() || shopStockList.size() != shopStockVoList.size()) {
            throw new FinanceException(ResultEnum.FINANCE_NOT_EXISTS);
        }
        Map<Long, ShopStock> saleMap = shopStockList.parallelStream()
                .collect(Collectors.toMap(ShopStock::getId, item -> item));
        List<String> noSaveList = shopStockVoList.parallelStream().filter(item -> {
                    ShopStock dbStock = saleMap.get(item.getId());
                    dbStock.setSaleNum(dbStock.getSaleNum().subtract(item.getSaleNum()));
                    return BigDecimal.ZERO.compareTo(dbStock.getSaleNum()) > 0;
                }).map(ShopStockVo::getOldShopCode)
                .toList();
        if (!noSaveList.isEmpty()) {
            throw new FinanceException(ResultEnum.FINANCE_NOT_SAVE);
        }
        // TODO (majf) 2024/3/22 13:55 保存订单数据

        updateBatchById(shopStockList);
        // TODO (majf) 2024/3/22 13:56 添加数据到销售表中
        // 获取总的销售金额
        BigDecimal sum = shopStockVoList.parallelStream()
                .map(item -> item.getSaleNum().multiply(item.getSaleAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean needReallocation = saleOrderVo.getSumSaleAmount().compareTo(sum) != 0;
        BigDecimal sumNum  = BigDecimal.ZERO;
        BigDecimal avg = BigDecimal.ZERO;
        if (needReallocation) {
            sumNum = shopStockVoList.parallelStream().map(ShopStockVo::getSaleNum).reduce(BigDecimal.ZERO, BigDecimal::add);
            avg = sum.subtract(saleOrderVo.getSumSaleAmount()).divide(sumNum, RoundingMode.DOWN);
        }
        BigDecimal finalSumNum = sumNum;
        BigDecimal finalAvg = avg;
        AtomicBoolean first = new AtomicBoolean(true);
        List<ShopFinance> shopFinanceList = shopStockVoList.stream().map(item -> {
            // TODO (majf) 2024/3/22 15:53 是否关联销售单
            // TODO (majf) 2024/3/22 15:54 添加库存id
            log.info("订单信息：{}", JSONObject.toJSONString(item));
            ShopFinance shopFinance = new ShopFinance();
            BeanUtils.copyProperties(item, shopFinance, "id");
            shopFinance.setSaleAmount(getSaleAmount(item.getSaleAmount(), item.getSaleNum(), needReallocation, first.get(),
                    sum, saleOrderVo.getSumSaleAmount(), finalAvg, finalSumNum));
            first.set(false);
            shopFinance.setIncomeAndExpenses(SysConf.INCOME_STATUS);
            shopFinance.setIncomeAndExpenses(saleOrderVo.getIncomeAndExpenses());
            // TODO (majf) 2024/3/22 15:55 测试将来修改成有效
            shopFinance.setIsValid(SysConf.INVALID_STATUS);
            return shopFinance;
        }).toList();
        shopFinanceService.saveBatch(shopFinanceList);
        return true;
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
