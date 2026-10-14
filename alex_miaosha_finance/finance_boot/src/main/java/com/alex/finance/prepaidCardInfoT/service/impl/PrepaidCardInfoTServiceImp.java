package com.alex.finance.prepaidCardInfoT.service.impl;

import com.alex.common.utils.date.DateUtils;
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
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.FinanceException;
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

    private static final String TYPE_CONSUME = "consume";
    private static final String TYPE_RECHARGE = "recharge";
    private static final String DESC_CONSUME = "消费";
    private static final String DESC_RECHARGE = "充值";

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
    public PrepaidCardInfoTVo addPrepaidCardInfoT(PrepaidCardInfoTVo prepaidCardInfoTVo) {
        PrepaidCardInfoTVo query = new PrepaidCardInfoTVo();
        query.setCardName(prepaidCardInfoTVo.getCardName());
        List<PrepaidCardInfoTVo> list = getList(query);
        if (list != null && !list.isEmpty()) {
            throw new FinanceException(ResultEnum.PARAM_ERROR.getCode(), "卡号已存在");
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
        int rows = prepaidCardInfoTMapper.updateById(prepaidCardInfoT);
        return rows > 0;
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
        BigDecimal totalBalance = calculateTotalBalance(cards);

        // 本月与上月区间
        java.time.LocalDate now = DateUtils.now().toLocalDate();
        java.time.LocalDate firstDayThisMonth = now.withDayOfMonth(1);
        LocalDateTime startThisMonth = firstDayThisMonth.atStartOfDay();
        LocalDateTime endThisMonth = firstDayThisMonth.plusMonths(1).atStartOfDay();
        LocalDateTime startLastMonth = firstDayThisMonth.minusMonths(1).atStartOfDay();
        LocalDateTime endLastMonth = startThisMonth;

        // 汇总（所有卡）
        BigDecimal[] monthlyAmounts = calculateMonthlyAmounts(cards, startThisMonth, endThisMonth, startLastMonth, endLastMonth);
        BigDecimal monthExpense = monthlyAmounts[0];
        BigDecimal monthRecharge = monthlyAmounts[1];
        BigDecimal lastMonthExpense = monthlyAmounts[2];
        BigDecimal lastMonthRecharge = monthlyAmounts[3];

        BigDecimal monthExpenseMoM = monthExpense.subtract(lastMonthExpense);
        BigDecimal monthRechargeMoM = monthRecharge.subtract(lastMonthRecharge);

        // 卡数环比：本月新增卡 vs 上月新增卡
        int thisMonthNewCards = countNewCards(cards, startThisMonth, endThisMonth);
        int lastMonthNewCards = countNewCards(cards, startLastMonth, endLastMonth);
        int totalCardsMoM = thisMonthNewCards - lastMonthNewCards;

        return new PrepaidDashboardOverviewVo()
                .setTotalCards(totalCards)
                .setTotalCardsMoM(totalCardsMoM)
                .setTotalBalance(totalBalance)
                .setTotalBalanceMoM(BigDecimal.ZERO) // 如需余额环比，可按期初期末余额计算
                .setMonthExpense(monthExpense)
                .setMonthExpenseMoM(monthExpenseMoM)
                .setMonthRecharge(monthRecharge)
                .setMonthRechargeMoM(monthRechargeMoM);
    }

    private BigDecimal calculateTotalBalance(List<PrepaidCardInfoTVo> cards) {
        if (cards == null || cards.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (PrepaidCardInfoTVo c : cards) {
            if (c != null && c.getCurrentBalance() != null) {
                total = total.add(c.getCurrentBalance());
            }
        }
        return total;
    }

    private BigDecimal[] calculateMonthlyAmounts(List<PrepaidCardInfoTVo> cards,
                                                  LocalDateTime startThisMonth, LocalDateTime endThisMonth,
                                                  LocalDateTime startLastMonth, LocalDateTime endLastMonth) {
        BigDecimal monthExpense = BigDecimal.ZERO;
        BigDecimal monthRecharge = BigDecimal.ZERO;
        BigDecimal lastMonthExpense = BigDecimal.ZERO;
        BigDecimal lastMonthRecharge = BigDecimal.ZERO;

        if (cards != null && !cards.isEmpty()) {
            for (PrepaidCardInfoTVo c : cards) {
                if (c == null || c.getId() == null) {
                    continue;
                }
                Long cardDbId = c.getId();
                try {
                    Map<String, Object> thisMonth = prepaidConsumeRecordTService.sumExpenseAndRecharge(cardDbId, startThisMonth, endThisMonth);
                    if (thisMonth != null) {
                        monthExpense = monthExpense.add(toBigDecimal(thisMonth.get("expenseAmount")));
                        monthRecharge = monthRecharge.add(toBigDecimal(thisMonth.get("rechargeAmount")));
                    }
                    Map<String, Object> lastMonth = prepaidConsumeRecordTService.sumExpenseAndRecharge(cardDbId, startLastMonth, endLastMonth);
                    if (lastMonth != null) {
                        lastMonthExpense = lastMonthExpense.add(toBigDecimal(lastMonth.get("expenseAmount")));
                        lastMonthRecharge = lastMonthRecharge.add(toBigDecimal(lastMonth.get("rechargeAmount")));
                    }
                } catch (Exception e) {
                    log.warn("计算卡ID {} 的消费记录时发生异常: {}", cardDbId, e.getMessage());
                }
            }
        }
        return new BigDecimal[]{monthExpense, monthRecharge, lastMonthExpense, lastMonthRecharge};
    }

    private int countNewCards(List<PrepaidCardInfoTVo> cards, LocalDateTime start, LocalDateTime end) {
        if (cards == null || cards.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (PrepaidCardInfoTVo c : cards) {
            if (c != null && c.getCreateTime() != null) {
                try {
                    LocalDateTime created = c.getCreateTime();
                    if (!created.isBefore(start) && created.isBefore(end)) {
                        count++;
                    }
                } catch (Exception e) {
                    log.warn("计算卡创建时间时发生异常: {}", e.getMessage());
                }
            }
        }
        return count;
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
        prepaidCardInfoTMapper.deleteByIds(idArr);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean consumeAndRecharge(PrepaidCardConsumeVo prepaidCardConsumeVo) {
        BigDecimal consumeAmount;
        boolean res;
        String type = prepaidCardConsumeVo.getType();
        if (TYPE_CONSUME.equals(type)) {
            consumeAmount = prepaidCardConsumeVo.getConsumeAmount().negate();
            res = consume(prepaidCardConsumeVo);
        } else if (TYPE_RECHARGE.equals(type)) {
            consumeAmount = prepaidCardConsumeVo.getConsumeAmount();
            res = recharge(prepaidCardConsumeVo);
        } else {
            throw new FinanceException(ResultEnum.PARAM_ERROR.getCode(), "未知操作类型");
        }
        if (res) {
            // 添加消费充值记录
            PrepaidConsumeRecordTVo prepaidConsumeRecordTVo = new PrepaidConsumeRecordTVo();
            prepaidConsumeRecordTVo.setConsumeTime(prepaidCardConsumeVo.getConsumeTime() == null ? DateUtils.now() : prepaidCardConsumeVo.getConsumeTime());
            prepaidConsumeRecordTVo.setCardId(prepaidCardConsumeVo.getId());
            prepaidConsumeRecordTVo.setAmount(consumeAmount);
            prepaidConsumeRecordTVo.setDescription(resolveRecordDescription(prepaidCardConsumeVo.getDescription(), type));
            prepaidConsumeRecordTService.addPrepaidConsumeRecordT(prepaidConsumeRecordTVo);
        }
        return true;
    }

    private String resolveRecordDescription(String description, String type) {
        if (description != null) {
            return description;
        }
        return TYPE_RECHARGE.equals(type) ? DESC_RECHARGE : DESC_CONSUME;
    }

    // 消费
    public Boolean consume(PrepaidCardConsumeVo prepaidCardConsumeVo) {
        if (prepaidCardConsumeVo.getId() == null) {
            throw new FinanceException(ResultEnum.PARAM_ERROR.getCode(), "卡号不能为空");
        }
        if (prepaidCardConsumeVo.getConsumeAmount() == null || prepaidCardConsumeVo.getConsumeAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new FinanceException(ResultEnum.PARAM_ERROR.getCode(), "消费金额必须大于0");
        }
        // 校验消费金额是否够用
        PrepaidCardInfoTVo prepaidCardInfoTVo = prepaidCardInfoTMapper.queryPrepaidCardInfoT(prepaidCardConsumeVo.getId());
        if (prepaidCardInfoTVo == null) {
            throw new FinanceException(ResultEnum.PARAM_ERROR.getCode(), "卡不存在");
        }
        if (prepaidCardInfoTVo.getCurrentBalance() == null || prepaidCardInfoTVo.getCurrentBalance().compareTo(prepaidCardConsumeVo.getConsumeAmount()) < 0) {
            throw new FinanceException(ResultEnum.PARAM_ERROR.getCode(), "卡余额不足");
        }
        // 消费扣减
        prepaidCardInfoTVo.setCurrentBalance(prepaidCardInfoTVo.getCurrentBalance().subtract(prepaidCardConsumeVo.getConsumeAmount()));
        Boolean updated = updatePrepaidCardInfoT(prepaidCardInfoTVo);
        if (!Boolean.TRUE.equals(updated)) {
            throw new FinanceException("500", "卡信息并发更新冲突，请重试");
        }
        return true;
    }

    // 充值
    public Boolean recharge(PrepaidCardConsumeVo prepaidCardConsumeVo) {
        if (prepaidCardConsumeVo.getConsumeAmount() == null || prepaidCardConsumeVo.getConsumeAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new FinanceException(ResultEnum.PARAM_ERROR.getCode(), "充值金额必须大于0");
        }
        PrepaidCardInfoTVo dbVo;
        if (prepaidCardConsumeVo.getId() == null) {
            // 校验卡编码是否重复
            PrepaidCardInfoTVo query = new PrepaidCardInfoTVo();
            query.setCardId(prepaidCardConsumeVo.getCardId());
            PrepaidCardInfoTVo curDbVo = prepaidCardInfoTMapper.queryPrepaidCardInfo(query);
            if (curDbVo != null) {
                throw new FinanceException(ResultEnum.PARAM_ERROR.getCode(), "卡编码重复");
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
                throw new FinanceException(ResultEnum.PARAM_ERROR.getCode(), "卡不存在");
            }
            dbVo.setCurrentBalance(dbVo.getCurrentBalance().add(prepaidCardConsumeVo.getConsumeAmount()));
            Boolean updated = updatePrepaidCardInfoT(dbVo);
            if (!Boolean.TRUE.equals(updated)) {
                throw new FinanceException("500", "卡信息并发更新冲突，请重试");
            }
        }
        prepaidCardConsumeVo.setId(dbVo.getId());
        return true;
    }
}
