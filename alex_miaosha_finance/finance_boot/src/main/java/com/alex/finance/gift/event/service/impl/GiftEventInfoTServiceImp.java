package com.alex.finance.gift.event.service.impl;

import com.alex.api.finance.gift.event.query.GiftEventQuery;
import com.alex.api.finance.gift.event.vo.GiftEventBusinessVo;
import com.alex.api.finance.gift.event.vo.GiftEventInfoTVo;
import com.alex.api.finance.gift.event.vo.GiftEventSummaryVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.event.entity.GiftEventInfoT;
import com.alex.finance.gift.event.mapper.GiftEventInfoTMapper;
import com.alex.finance.gift.event.service.GiftEventInfoTService;
import com.alex.finance.gift.record.entity.GiftRecordInfoT;
import com.alex.finance.gift.record.service.GiftRecordInfoTService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GiftEventInfoTServiceImp extends ServiceImpl<GiftEventInfoTMapper, GiftEventInfoT> implements GiftEventInfoTService {

    private final UserUtils userUtils;

    @Autowired(required = false)
    private GiftRecordInfoTService giftRecordInfoTService;

    @Override
    public Page<GiftEventInfoTVo> getPage(Long pageNum, Long pageSize, GiftEventQuery query) {
        Page<GiftEventInfoT> entityPage = page(new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize), queryWrapper(query));
        Page<GiftEventInfoTVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public List<GiftEventInfoTVo> getList(GiftEventQuery query) {
        return list(queryWrapper(query)).stream().map(this::toVo).collect(Collectors.toList());
    }

    @Override
    public GiftEventSummaryVo getSummary() {
        List<GiftEventInfoT> events = list(queryWrapper(null));
        List<GiftRecordInfoT> records = listGiftRecordsForAggregate();
        LocalDateTime now = LocalDateTime.now();
        long monthPendingCount = events.stream()
                .filter(event -> event.getEventTime() != null)
                .filter(event -> event.getEventTime().getYear() == now.getYear() && event.getEventTime().getMonth() == now.getMonth())
                .count();
        BigDecimal totalAmount = records.stream().map(this::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        long activePersonCount = records.stream()
                .flatMap(record -> java.util.stream.Stream.of(record.getGiverPersonId(), record.getReceiverPersonId()))
                .filter(Objects::nonNull)
                .distinct()
                .count();
        return new GiftEventSummaryVo()
                .setMonthPendingCount(monthPendingCount)
                .setTotalAmount(totalAmount)
                .setActivePersonCount(activePersonCount);
    }

    @Override
    public Page<GiftEventBusinessVo> getBusinessPage(Long pageNum, Long pageSize, GiftEventQuery query) {
        long current = pageNum == null ? 1 : pageNum;
        long size = pageSize == null ? 10 : pageSize;
        List<GiftEventBusinessVo> rows = list(queryWrapper(query)).stream()
                .map(this::toBusinessVo)
                .collect(Collectors.toList());
        long from = Math.max(0, (current - 1) * size);
        long to = Math.min(rows.size(), from + size);
        Page<GiftEventBusinessVo> page = new Page<>(current, size, rows.size());
        page.setRecords(from >= rows.size() ? List.of() : rows.subList((int) from, (int) to));
        return page;
    }

    @Override
    public GiftEventInfoTVo queryGiftEventInfoT(Long id) {
        return toVo(getById(id));
    }

    @Override
    public GiftEventInfoTVo addGiftEventInfoT(GiftEventInfoTVo giftEventInfoTVo) {
        fillOwner(giftEventInfoTVo);
        GiftEventInfoT entity = new GiftEventInfoT();
        BeanUtils.copyProperties(giftEventInfoTVo, entity);
        save(entity);
        giftEventInfoTVo.setId(entity.getId());
        return giftEventInfoTVo;
    }

    @Override
    public Boolean updateGiftEventInfoT(GiftEventInfoTVo giftEventInfoTVo) {
        GiftEventInfoT entity = new GiftEventInfoT();
        BeanUtils.copyProperties(giftEventInfoTVo, entity);
        return updateById(entity);
    }

    @Override
    public Boolean deleteGiftEventInfoT(String ids) {
        if (!StringUtils.hasText(ids)) {
            return true;
        }
        return removeBatchByIds(Arrays.stream(ids.split(","))
                .filter(StringUtils::hasText)
                .map(Long::valueOf)
                .collect(Collectors.toList()));
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GiftEventInfoT> queryWrapper(GiftEventQuery query) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GiftEventInfoT> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (query == null) {
            return wrapper.orderByDesc(GiftEventInfoT::getEventTime);
        }
        wrapper.like(StringUtils.hasText(query.getKeyword()), GiftEventInfoT::getEventName, query.getKeyword());
        wrapper.eq(StringUtils.hasText(query.getEventType()), GiftEventInfoT::getEventType, query.getEventType());
        wrapper.ge(query.getEventTimeStart() != null, GiftEventInfoT::getEventTime, query.getEventTimeStart());
        wrapper.le(query.getEventTimeEnd() != null, GiftEventInfoT::getEventTime, query.getEventTimeEnd());
        return wrapper.orderByDesc(GiftEventInfoT::getEventTime);
    }

    protected List<GiftRecordInfoT> listGiftRecordsForAggregate() {
        return giftRecordInfoTService == null ? List.of() : giftRecordInfoTService.list();
    }

    private GiftEventBusinessVo toBusinessVo(GiftEventInfoT entity) {
        GiftEventBusinessVo vo = new GiftEventBusinessVo();
        BeanUtils.copyProperties(entity, vo);
        List<GiftRecordInfoT> records = listGiftRecordsForAggregate().stream()
                .filter(record -> entity.getId() != null && entity.getId().equals(record.getEventId()))
                .collect(Collectors.toList());
        BigDecimal giveAmount = records.stream()
                .filter(record -> "GIVE".equals(record.getDirection()) || "RETURN".equals(record.getDirection()))
                .map(this::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal receiveAmount = records.stream()
                .filter(record -> "RECEIVE".equals(record.getDirection()))
                .map(this::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Set<Long> participantIds = records.stream()
                .flatMap(record -> java.util.stream.Stream.of(record.getGiverPersonId(), record.getReceiverPersonId()))
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        vo.setGiveAmount(giveAmount);
        vo.setReceiveAmount(receiveAmount);
        vo.setTotalAmount(giveAmount.add(receiveAmount));
        vo.setParticipantCount((long) participantIds.size());
        vo.setLocationText(StringUtils.hasText(entity.getRemark()) ? entity.getRemark() : "-");
        vo.setEventStatus(entity.getEventTime() != null && entity.getEventTime().isAfter(LocalDateTime.now()) ? "PENDING" : "FINISHED");
        return vo;
    }

    private BigDecimal amount(GiftRecordInfoT record) {
        return record.getAmount() == null ? BigDecimal.ZERO : record.getAmount();
    }

    private void fillOwner(GiftEventInfoTVo vo) {
        TUserVo loginUser = userUtils == null ? null : userUtils.getLoginUser();
        if (loginUser == null) {
            return;
        }
        vo.setUserId(loginUser.getId());
        vo.setOrgId(loginUser.getOrgInfoVo() == null ? loginUser.getOrgId() : loginUser.getOrgInfoVo().getId());
    }

    private GiftEventInfoTVo toVo(GiftEventInfoT entity) {
        if (entity == null) {
            return null;
        }
        GiftEventInfoTVo vo = new GiftEventInfoTVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
