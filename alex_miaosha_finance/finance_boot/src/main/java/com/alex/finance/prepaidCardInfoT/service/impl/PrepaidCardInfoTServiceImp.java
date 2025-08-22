package com.alex.finance.prepaidCardInfoT.service.impl;

import com.alex.api.finance.prepaidCardInfoT.vo.PrepaidCardConsumeVo;
import com.alex.api.finance.prepaidConsumeRecordT.vo.PrepaidConsumeRecordTVo;
import com.alex.api.finance.prepaidCardInfoT.vo.PrepaidDashboardOverviewVo;
import com.alex.finance.prepaidCardInfoT.entity.PrepaidCardInfoT;
import com.alex.api.finance.prepaidCardInfoT.vo.PrepaidCardInfoTVo;
import com.alex.finance.prepaidCardInfoT.mapper.PrepaidCardInfoTMapper;
import com.alex.finance.prepaidCardInfoT.service.PrepaidCardInfoTService;
import com.alex.finance.prepaidConsumeRecordT.service.PrepaidConsumeRecordTService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import com.alex.common.utils.string.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * description:  消费卡信息表服务实现类
 * author:       alex
 * createDate:   2025-04-30 08:21:48
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class PrepaidCardInfoTServiceImp extends ServiceImpl<PrepaidCardInfoTMapper, PrepaidCardInfoT> implements PrepaidCardInfoTService {

    private static final Logger log = LoggerFactory.getLogger(PrepaidCardInfoTServiceImp.class);

    private final PrepaidCardInfoTMapper prepaidCardInfoTMapper;

    private final PrepaidConsumeRecordTService prepaidConsumeRecordTService;

    @Override
    public Page<PrepaidCardInfoTVo> getPage(Long pageNum, Long pageSize, PrepaidCardInfoTVo prepaidCardInfoTVo) {
        Page<PrepaidCardInfoTVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return prepaidCardInfoTMapper.getPage(page, prepaidCardInfoTVo);
    }

    @Override
    public List<PrepaidCardInfoTVo> getList(PrepaidCardInfoTVo prepaidCardInfoTVo) {
        return prepaidCardInfoTMapper.getList(prepaidCardInfoTVo);
    }

    @Override
    public PrepaidCardInfoTVo queryPrepaidCardInfoT(Long id) {
        return prepaidCardInfoTMapper.queryPrepaidCardInfoT(id);
    }

    @Override
    public PrepaidCardInfoTVo addPrepaidCardInfoT(PrepaidCardInfoTVo prepaidCardInfoTVo) throws Exception {
        PrepaidCardInfoTVo query = new PrepaidCardInfoTVo();
        query.setCardName(prepaidCardInfoTVo.getCardName());
        List<PrepaidCardInfoTVo> list = getList(query);
        if (list != null && list.size() > 0) {
            throw new Exception("卡号已存在");
        }
        PrepaidCardInfoT prepaidCardInfoT = new PrepaidCardInfoT();
        BeanUtils.copyProperties(prepaidCardInfoTVo, prepaidCardInfoT);
        prepaidCardInfoT.setVersion(1);
        prepaidCardInfoTMapper.insert(prepaidCardInfoT);
        prepaidCardInfoTVo.setId(prepaidCardInfoT.getId());
        return prepaidCardInfoTVo;
    }

    @Override
    public Boolean updatePrepaidCardInfoT(PrepaidCardInfoTVo prepaidCardInfoTVo) {
        PrepaidCardInfoT prepaidCardInfoT = new PrepaidCardInfoT();
        BeanUtils.copyProperties(prepaidCardInfoTVo, prepaidCardInfoT);
        prepaidCardInfoTMapper.updateById(prepaidCardInfoT);
        return true;
    }

    @Override
    public PrepaidDashboardOverviewVo dashboardOverview(Long userId) {
        // 总卡数
        PrepaidCardInfoTVo query = new PrepaidCardInfoTVo();
        if (userId != null) {
            query.setUserId(userId);
        }
        List<PrepaidCardInfoTVo> cards = prepaidCardInfoTMapper.getList(query);
        int totalCards = cards == null ? 0 : cards.size();

        // 总余额
        java.math.BigDecimal totalBalance = java.math.BigDecimal.ZERO;
        if (cards != null && !cards.isEmpty()) {
            for (PrepaidCardInfoTVo c : cards) {
                if (c != null && c.getCurrentBalance() != null) {
                    totalBalance = totalBalance.add(c.getCurrentBalance());
                }
            }
        }

        // 本月与上月区间
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate firstDayThisMonth = now.withDayOfMonth(1);
        java.time.LocalDate firstDayNextMonth = firstDayThisMonth.plusMonths(1);
        java.time.LocalDate firstDayLastMonth = firstDayThisMonth.minusMonths(1);

        java.time.LocalDateTime startThisMonth = firstDayThisMonth.atStartOfDay();
        java.time.LocalDateTime endThisMonth = firstDayNextMonth.atStartOfDay();
        java.time.LocalDateTime startLastMonth = firstDayLastMonth.atStartOfDay();
        java.time.LocalDateTime endLastMonth = firstDayThisMonth.atStartOfDay();

        // 汇总（所有卡）
        java.math.BigDecimal monthExpense = java.math.BigDecimal.ZERO;
        java.math.BigDecimal monthRecharge = java.math.BigDecimal.ZERO;
        java.math.BigDecimal lastMonthExpense = java.math.BigDecimal.ZERO;
        java.math.BigDecimal lastMonthRecharge = java.math.BigDecimal.ZERO;

        if (cards != null && !cards.isEmpty()) {
            for (PrepaidCardInfoTVo c : cards) {
                if (c != null && c.getId() != null) {
                    Long cardDbId = c.getId();
                    try {
                        Map<String, Object> thisMonth = prepaidConsumeRecordTService.sumExpenseAndRecharge(cardDbId, startThisMonth, endThisMonth);
                        Map<String, Object> lastMonth = prepaidConsumeRecordTService.sumExpenseAndRecharge(cardDbId, startLastMonth, endLastMonth);
                        
                        // 安全处理Map结果
                        if (thisMonth != null) {
                            monthExpense = monthExpense.add(toBigDecimal(thisMonth.get("expenseAmount")));
                            monthRecharge = monthRecharge.add(toBigDecimal(thisMonth.get("rechargeAmount")));
                        }
                        
                        if (lastMonth != null) {
                            lastMonthExpense = lastMonthExpense.add(toBigDecimal(lastMonth.get("expenseAmount")));
                            lastMonthRecharge = lastMonthRecharge.add(toBigDecimal(lastMonth.get("rechargeAmount")));
                        }
                    } catch (Exception e) {
                        // 记录日志但不影响整体计算
                        log.warn("计算卡ID {} 的消费记录时发生异常: {}", cardDbId, e.getMessage());
                    }
                }
            }
        }

        java.math.BigDecimal monthExpenseMoM = monthExpense.subtract(lastMonthExpense);
        java.math.BigDecimal monthRechargeMoM = monthRecharge.subtract(lastMonthRecharge);

        // 卡数环比：本月新增卡 vs 上月新增卡
        int thisMonthNewCards = 0;
        int lastMonthNewCards = 0;
        if (cards != null && !cards.isEmpty()) {
            for (PrepaidCardInfoTVo c : cards) {
                if (c != null && c.getCreateTime() != null) {
                    try {
                        if (!c.getCreateTime().isBefore(startThisMonth) && c.getCreateTime().isBefore(endThisMonth)) {
                            thisMonthNewCards++;
                        } else if (!c.getCreateTime().isBefore(startLastMonth) && c.getCreateTime().isBefore(endLastMonth)) {
                            lastMonthNewCards++;
                        }
                    } catch (Exception e) {
                        // 记录日志但不影响整体计算
                        log.warn("计算卡创建时间时发生异常: {}", e.getMessage());
                    }
                }
            }
        }
        int totalCardsMoM = thisMonthNewCards - lastMonthNewCards;

        return new PrepaidDashboardOverviewVo()
                .setTotalCards(totalCards)
                .setTotalCardsMoM(totalCardsMoM)
                .setTotalBalance(totalBalance)
                .setTotalBalanceMoM(java.math.BigDecimal.ZERO) // 如需余额环比，可按期初期末余额计算
                .setMonthExpense(monthExpense)
                .setMonthExpenseMoM(monthExpenseMoM)
                .setMonthRecharge(monthRecharge)
                .setMonthRechargeMoM(monthRechargeMoM);
    }

    private java.math.BigDecimal toBigDecimal(Object value) {
        if (value == null) return java.math.BigDecimal.ZERO;
        if (value instanceof java.math.BigDecimal) return (java.math.BigDecimal) value;
        try {
            return new java.math.BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            log.warn("无法将值 {} 转换为BigDecimal，使用默认值0", value);
            return java.math.BigDecimal.ZERO;
        }
    }
    @Override
    public Boolean deletePrepaidCardInfoT(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        prepaidCardInfoTMapper.deleteBatchIds(idArr);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean consumeAndRecharge(PrepaidCardConsumeVo prepaidCardConsumeVo) throws Exception {
        BigDecimal consumeAmount;
        boolean res;
        switch (prepaidCardConsumeVo.getType()) {
            case "consume" -> {
                consumeAmount = prepaidCardConsumeVo.getConsumeAmount().multiply(new BigDecimal("-1"));
                res = consume(prepaidCardConsumeVo);
            }
            case "recharge" -> {
                consumeAmount = prepaidCardConsumeVo.getConsumeAmount();
                res = recharge(prepaidCardConsumeVo);
            }
            default -> throw new RuntimeException("未知操作类型");
        }
        if (res) {
            // 添加消费充值记录
            PrepaidConsumeRecordTVo prepaidConsumeRecordTVo = new PrepaidConsumeRecordTVo();
            prepaidConsumeRecordTVo.setConsumeTime(prepaidCardConsumeVo.getConsumeTime() == null ? LocalDateTime.now() : prepaidCardConsumeVo.getConsumeTime());
            prepaidConsumeRecordTVo.setCardId(prepaidCardConsumeVo.getId());
            prepaidConsumeRecordTVo.setAmount(consumeAmount);
            prepaidConsumeRecordTVo.setDescription(prepaidCardConsumeVo.getDescription() == null ?
                    "recharge".equals(prepaidCardConsumeVo.getType()) ? "充值" : "消费" :
                    prepaidCardConsumeVo.getDescription());
            prepaidConsumeRecordTService.addPrepaidConsumeRecordT(prepaidConsumeRecordTVo);
        }
        return true;
    }

    // 消费
    public Boolean consume(PrepaidCardConsumeVo prepaidCardConsumeVo) throws Exception {
        if (prepaidCardConsumeVo.getId() == null) {
            throw new Exception("卡号不能为空");
        }
        if (prepaidCardConsumeVo.getConsumeAmount() == null || BigDecimal.ZERO.compareTo(prepaidCardConsumeVo.getConsumeAmount()) == 0) {
            throw new Exception("消费金额不能为空");
        }
        // 校验消费金额是否够用
        PrepaidCardInfoTVo prepaidCardInfoTVo = prepaidCardInfoTMapper.queryPrepaidCardInfoT(prepaidCardConsumeVo.getId());
        if (prepaidCardInfoTVo.getCurrentBalance() == null || prepaidCardInfoTVo.getCurrentBalance().compareTo(prepaidCardConsumeVo.getConsumeAmount()) < 0) {
            throw new Exception("卡余额不足");
        }
        BigDecimal consumeAmount = prepaidCardConsumeVo.getConsumeAmount().multiply(new BigDecimal(-1));
        // 消费
        PrepaidCardInfoTVo dbVo = prepaidCardInfoTMapper.queryPrepaidCardInfoT(prepaidCardConsumeVo.getId());
        dbVo.setCurrentBalance(prepaidCardInfoTVo.getCurrentBalance().add(consumeAmount));
        updatePrepaidCardInfoT(dbVo);
        return true;
    }

    // 充值
    public Boolean recharge(PrepaidCardConsumeVo prepaidCardConsumeVo) throws Exception {
        PrepaidCardInfoTVo dbVo;
        if (prepaidCardConsumeVo.getId() == null) {
            // 校验卡编码是否重复
            PrepaidCardInfoTVo query = new PrepaidCardInfoTVo();
            query.setCardId(prepaidCardConsumeVo.getCardId());
            PrepaidCardInfoTVo curDbVo = prepaidCardInfoTMapper.queryPrepaidCardInfo(query);
            if (curDbVo != null) {
                throw new Exception("卡编码重复");
            }
            PrepaidCardInfoTVo prepaidCardInfoTVo = new PrepaidCardInfoTVo();
            prepaidCardInfoTVo.setCardId(prepaidCardConsumeVo.getCardId());
            prepaidCardInfoTVo.setCardName(prepaidCardConsumeVo.getCardName());
            prepaidCardInfoTVo.setInitialBalance(prepaidCardConsumeVo.getConsumeAmount());
            prepaidCardInfoTVo.setCurrentBalance(prepaidCardConsumeVo.getConsumeAmount());
            dbVo = addPrepaidCardInfoT(prepaidCardInfoTVo);
        } else {
            // 根据card_id查询卡是否存在
            dbVo = prepaidCardInfoTMapper.queryPrepaidCardInfoT(prepaidCardConsumeVo.getId());
            if (dbVo == null) {
                throw new Exception("卡不存在");
            }
            dbVo.setCurrentBalance(dbVo.getCurrentBalance().add(prepaidCardConsumeVo.getConsumeAmount()));
            updatePrepaidCardInfoT(dbVo);
        }
        prepaidCardConsumeVo.setId(dbVo.getId());
        return true;
    }
}
