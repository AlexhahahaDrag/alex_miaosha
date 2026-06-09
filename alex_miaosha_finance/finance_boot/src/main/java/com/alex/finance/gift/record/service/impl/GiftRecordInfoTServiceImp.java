package com.alex.finance.gift.record.service.impl;

import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoTVo;
import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.event.entity.GiftEventInfoT;
import com.alex.finance.gift.event.mapper.GiftEventInfoTMapper;
import com.alex.finance.gift.person.entity.GiftPersonInfoT;
import com.alex.finance.gift.person.mapper.GiftPersonInfoTMapper;
import com.alex.finance.gift.record.entity.GiftRecordInfoT;
import com.alex.finance.gift.record.mapper.GiftRecordInfoTMapper;
import com.alex.finance.gift.record.service.GiftRecordInfoTService;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import com.alex.finance.gift.support.GiftExceptions;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GiftRecordInfoTServiceImp extends ServiceImpl<GiftRecordInfoTMapper, GiftRecordInfoT> implements GiftRecordInfoTService {

    private static final String DIRECTION_GIVE = "GIVE";
    private static final String DIRECTION_RECEIVE = "RECEIVE";
    private static final String DIRECTION_RETURN = "RETURN";

    private final GiftDataScopeSupport giftDataScopeSupport;
    private final GiftPersonInfoTMapper giftPersonInfoTMapper;
    private final GiftEventInfoTMapper giftEventInfoTMapper;

    @Override
    public Page<GiftRecordInfoTVo> getPage(Long pageNum, Long pageSize, GiftRecordQuery query) {
        return getBaseMapper().getPage(
                new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize),
                query);
    }

    @Override
    public List<GiftRecordInfoTVo> getList(GiftRecordQuery query) {
        return getBaseMapper().getList(query);
    }

    @Override
    public GiftRecordSummaryVo getSummary(GiftRecordQuery query) {
        List<GiftRecordInfoT> records = getBaseMapper().listEntities(query);
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
        GiftRecordInfoT entity = getById(id);
        giftDataScopeSupport.assertRecordAccessible(entity);
        return toVo(entity);
    }

    @Override
    public GiftRecordInfoTVo addGiftRecordInfoT(GiftRecordInfoTVo giftRecordInfoTVo) {
        validateForSave(giftRecordInfoTVo);
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
        if (giftRecordInfoTVo == null || giftRecordInfoTVo.getId() == null) {
            throw GiftExceptions.param("礼金记录ID不能为空");
        }
        GiftRecordInfoT existing = getById(giftRecordInfoTVo.getId());
        giftDataScopeSupport.assertRecordAccessible(existing);
        giftRecordInfoTVo.setUserId(existing.getUserId());
        giftRecordInfoTVo.setOrgId(existing.getOrgId());
        validateForSave(giftRecordInfoTVo);
        GiftRecordInfoT entity = new GiftRecordInfoT();
        BeanUtils.copyProperties(giftRecordInfoTVo, entity);
        return updateById(entity);
    }

    @Override
    public Boolean deleteGiftRecordInfoT(String ids) {
        if (!StringUtils.hasText(ids)) {
            return true;
        }
        List<Long> idList = parseIds(ids);
        for (Long id : idList) {
            GiftRecordInfoT existing = getById(id);
            giftDataScopeSupport.assertRecordAccessible(existing);
        }
        return removeBatchByIds(idList);
    }

    @Override
    public BigDecimal calculatePendingReturnAmount(Long receiveRecordId) {
        if (receiveRecordId == null) {
            throw GiftExceptions.param("原始收礼记录不能为空");
        }
        GiftRecordInfoT receiveRecord = getById(receiveRecordId);
        if (receiveRecord == null) {
            throw GiftExceptions.param("礼金记录不存在");
        }
        giftDataScopeSupport.assertRecordAccessible(receiveRecord);
        if (!DIRECTION_RECEIVE.equals(receiveRecord.getDirection())) {
            throw GiftExceptions.param("原始记录必须是收礼记录");
        }
        BigDecimal receiveAmount = receiveRecord.getAmount() == null ? BigDecimal.ZERO : receiveRecord.getAmount();
        BigDecimal returnedAmount = getBaseMapper().sumReturnAmountByRelatedRecordId(receiveRecordId);
        BigDecimal pendingAmount = receiveAmount.subtract(returnedAmount == null ? BigDecimal.ZERO : returnedAmount);
        return pendingAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : pendingAmount;
    }

    @Override
    public Boolean markReturned(Long receiveRecordId) {
        if (receiveRecordId == null) {
            throw GiftExceptions.param("原始收礼记录不能为空");
        }
        GiftRecordInfoT receiveRecord = getById(receiveRecordId);
        if (receiveRecord == null) {
            throw GiftExceptions.param("礼金记录不存在");
        }
        giftDataScopeSupport.assertRecordAccessible(receiveRecord);
        if (!DIRECTION_RECEIVE.equals(receiveRecord.getDirection())) {
            throw GiftExceptions.param("仅收礼记录可标记已回礼");
        }
        if (receiveRecord.getReturnedFlag() != null && receiveRecord.getReturnedFlag() == 1) {
            return true;
        }
        GiftRecordInfoT entity = new GiftRecordInfoT();
        entity.setId(receiveRecordId);
        entity.setReturnedFlag(1);
        return updateById(entity);
    }

    private void validateForSave(GiftRecordInfoTVo vo) {
        validateDirection(vo);
        validateAmount(vo);
        validateReferences(vo);
        validateReturnRelation(vo);
    }

    private void validateDirection(GiftRecordInfoTVo vo) {
        if (vo == null || !StringUtils.hasText(vo.getDirection())) {
            throw GiftExceptions.param("礼金方向不能为空");
        }
        if (!DIRECTION_GIVE.equals(vo.getDirection())
                && !DIRECTION_RECEIVE.equals(vo.getDirection())
                && !DIRECTION_RETURN.equals(vo.getDirection())) {
            throw GiftExceptions.param("礼金方向不合法");
        }
        if (DIRECTION_RETURN.equals(vo.getDirection()) && vo.getRelatedRecordId() == null) {
            throw GiftExceptions.param("回礼记录必须关联原始收礼记录");
        }
    }

    private void validateAmount(GiftRecordInfoTVo vo) {
        if (vo == null || vo.getAmount() == null || vo.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw GiftExceptions.param("礼金金额必须大于0");
        }
    }

    private void validateReferences(GiftRecordInfoTVo vo) {
        if (vo == null) {
            return;
        }
        if (vo.getGiverPersonId() != null) {
            GiftPersonInfoT giver = giftPersonInfoTMapper.selectById(vo.getGiverPersonId());
            giftDataScopeSupport.assertPersonAccessible(giver);
        }
        if (vo.getReceiverPersonId() != null) {
            GiftPersonInfoT receiver = giftPersonInfoTMapper.selectById(vo.getReceiverPersonId());
            giftDataScopeSupport.assertPersonAccessible(receiver);
        }
        if (vo.getEventId() != null) {
            GiftEventInfoT event = giftEventInfoTMapper.selectById(vo.getEventId());
            giftDataScopeSupport.assertEventAccessible(event);
        }
    }

    private void validateReturnRelation(GiftRecordInfoTVo vo) {
        if (vo == null || !DIRECTION_RETURN.equals(vo.getDirection())) {
            return;
        }
        Long relatedRecordId = vo.getRelatedRecordId();
        if (vo.getId() != null && vo.getId().equals(relatedRecordId)) {
            throw GiftExceptions.param("回礼记录不能关联自身");
        }
        GiftRecordInfoT related = getById(relatedRecordId);
        if (related == null) {
            throw GiftExceptions.param("关联的收礼记录不存在");
        }
        giftDataScopeSupport.assertRecordAccessible(related);
        if (!DIRECTION_RECEIVE.equals(related.getDirection())) {
            throw GiftExceptions.param("回礼记录只能关联收礼记录");
        }
    }

    private void fillOwner(GiftRecordInfoTVo vo) {
        TUserVo loginUser = giftDataScopeSupport.requireLoginUser();
        vo.setUserId(loginUser.getId());
        OrgInfoVo orgInfoVo = loginUser.getOrgInfoVo();
        vo.setOrgId(orgInfoVo == null ? loginUser.getOrgId() : orgInfoVo.getId());
    }

    private List<Long> parseIds(String ids) {
        try {
            return Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        } catch (NumberFormatException ex) {
            throw GiftExceptions.param("礼金记录ID格式不合法");
        }
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
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
