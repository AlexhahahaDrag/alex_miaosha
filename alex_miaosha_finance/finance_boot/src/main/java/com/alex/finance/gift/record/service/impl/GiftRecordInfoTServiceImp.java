package com.alex.finance.gift.record.service.impl;

import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoTVo;
import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.record.entity.GiftRecordInfoT;
import com.alex.finance.gift.record.mapper.GiftRecordInfoTMapper;
import com.alex.finance.gift.record.service.GiftRecordInfoTService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GiftRecordInfoTServiceImp extends ServiceImpl<GiftRecordInfoTMapper, GiftRecordInfoT> implements GiftRecordInfoTService {

    private static final String DIRECTION_GIVE = "GIVE";
    private static final String DIRECTION_RECEIVE = "RECEIVE";
    private static final String DIRECTION_RETURN = "RETURN";

    private final UserUtils userUtils;

    @Override
    public Page<GiftRecordInfoTVo> getPage(Long pageNum, Long pageSize, GiftRecordQuery query) {
        Page<GiftRecordInfoT> entityPage = page(new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize), queryWrapper(query));
        Page<GiftRecordInfoTVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public List<GiftRecordInfoTVo> getList(GiftRecordQuery query) {
        return list(queryWrapper(query)).stream().map(this::toVo).collect(Collectors.toList());
    }

    @Override
    public GiftRecordSummaryVo getSummary(GiftRecordQuery query) {
        List<GiftRecordInfoT> records = list(queryWrapper(query));
        BigDecimal giveAmount = sumByDirection(records, DIRECTION_GIVE);
        BigDecimal receiveAmount = sumByDirection(records, DIRECTION_RECEIVE);
        BigDecimal returnAmount = sumByDirection(records, DIRECTION_RETURN);
        return new GiftRecordSummaryVo()
                .setGiveAmount(giveAmount)
                .setReceiveAmount(receiveAmount)
                .setReturnAmount(returnAmount)
                .setNetAmount(receiveAmount.subtract(giveAmount).subtract(returnAmount))
                .setRecordCount((long) records.size());
    }

    @Override
    public GiftRecordInfoTVo queryGiftRecordInfoT(Long id) {
        return toVo(getById(id));
    }

    @Override
    public GiftRecordInfoTVo addGiftRecordInfoT(GiftRecordInfoTVo giftRecordInfoTVo) {
        validateDirection(giftRecordInfoTVo);
        fillOwner(giftRecordInfoTVo);
        GiftRecordInfoT entity = new GiftRecordInfoT();
        BeanUtils.copyProperties(giftRecordInfoTVo, entity);
        if (DIRECTION_RECEIVE.equals(entity.getDirection()) && entity.getReturnedFlag() == null) {
            entity.setReturnedFlag(0);
            giftRecordInfoTVo.setReturnedFlag(0);
        }
        save(entity);
        giftRecordInfoTVo.setId(entity.getId());
        return giftRecordInfoTVo;
    }

    @Override
    public Boolean updateGiftRecordInfoT(GiftRecordInfoTVo giftRecordInfoTVo) {
        GiftRecordInfoT entity = new GiftRecordInfoT();
        BeanUtils.copyProperties(giftRecordInfoTVo, entity);
        return updateById(entity);
    }

    @Override
    public Boolean deleteGiftRecordInfoT(String ids) {
        if (!StringUtils.hasText(ids)) {
            return true;
        }
        return removeBatchByIds(Arrays.asList(ids.split(",")));
    }

    @Override
    public BigDecimal calculatePendingReturnAmount(Long receiveRecordId) {
        if (receiveRecordId == null) {
            throw new IllegalArgumentException("原始收礼记录不能为空");
        }
        GiftRecordInfoT receiveRecord = getById(receiveRecordId);
        if (receiveRecord == null || !DIRECTION_RECEIVE.equals(receiveRecord.getDirection())) {
            throw new IllegalArgumentException("原始记录必须是收礼记录");
        }
        BigDecimal receiveAmount = receiveRecord.getAmount() == null ? BigDecimal.ZERO : receiveRecord.getAmount();
        BigDecimal returnedAmount = list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GiftRecordInfoT>()
                .eq(GiftRecordInfoT::getRelatedRecordId, receiveRecordId)
                .eq(GiftRecordInfoT::getDirection, DIRECTION_RETURN))
                .stream()
                .map(GiftRecordInfoT::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingAmount = receiveAmount.subtract(returnedAmount);
        return pendingAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : pendingAmount;
    }

    @Override
    public Boolean markReturned(Long receiveRecordId) {
        if (receiveRecordId == null) {
            throw new IllegalArgumentException("原始收礼记录不能为空");
        }
        GiftRecordInfoT entity = new GiftRecordInfoT();
        entity.setId(receiveRecordId);
        entity.setReturnedFlag(1);
        return updateById(entity);
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GiftRecordInfoT> queryWrapper(GiftRecordQuery query) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GiftRecordInfoT> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (query == null) {
            return wrapper.orderByDesc(GiftRecordInfoT::getPayTime);
        }
        wrapper.eq(query.getEventId() != null, GiftRecordInfoT::getEventId, query.getEventId());
        wrapper.eq(query.getGiverPersonId() != null, GiftRecordInfoT::getGiverPersonId, query.getGiverPersonId());
        wrapper.eq(query.getReceiverPersonId() != null, GiftRecordInfoT::getReceiverPersonId, query.getReceiverPersonId());
        wrapper.eq(StringUtils.hasText(query.getDirection()), GiftRecordInfoT::getDirection, query.getDirection());
        wrapper.ge(query.getPayTimeStart() != null, GiftRecordInfoT::getPayTime, query.getPayTimeStart());
        wrapper.le(query.getPayTimeEnd() != null, GiftRecordInfoT::getPayTime, query.getPayTimeEnd());
        wrapper.ge(query.getAmountMin() != null, GiftRecordInfoT::getAmount, query.getAmountMin());
        wrapper.le(query.getAmountMax() != null, GiftRecordInfoT::getAmount, query.getAmountMax());
        return wrapper.orderByDesc(GiftRecordInfoT::getPayTime);
    }

    private void validateDirection(GiftRecordInfoTVo vo) {
        if (vo == null || !StringUtils.hasText(vo.getDirection())) {
            throw new IllegalArgumentException("礼金方向不能为空");
        }
        if (!DIRECTION_GIVE.equals(vo.getDirection())
                && !DIRECTION_RECEIVE.equals(vo.getDirection())
                && !DIRECTION_RETURN.equals(vo.getDirection())) {
            throw new IllegalArgumentException("礼金方向不合法");
        }
        if (DIRECTION_RETURN.equals(vo.getDirection()) && vo.getRelatedRecordId() == null) {
            throw new IllegalArgumentException("回礼记录必须关联原始收礼记录");
        }
    }

    private void fillOwner(GiftRecordInfoTVo vo) {
        TUserVo loginUser = userUtils == null ? null : userUtils.getLoginUser();
        if (loginUser == null) {
            return;
        }
        vo.setUserId(loginUser.getId());
        vo.setOrgId(loginUser.getOrgInfoVo() == null ? loginUser.getOrgId() : loginUser.getOrgInfoVo().getId());
    }

    private GiftRecordInfoTVo toVo(GiftRecordInfoT entity) {
        if (entity == null) {
            return null;
        }
        GiftRecordInfoTVo vo = new GiftRecordInfoTVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setPaymentMethod("-");
        vo.setHandlerName("-");
        return vo;
    }

    private BigDecimal sumByDirection(List<GiftRecordInfoT> records, String direction) {
        return records.stream()
                .filter(record -> direction.equals(record.getDirection()))
                .map(GiftRecordInfoT::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
