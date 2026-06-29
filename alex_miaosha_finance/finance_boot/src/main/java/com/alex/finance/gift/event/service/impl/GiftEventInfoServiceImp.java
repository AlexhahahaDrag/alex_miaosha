package com.alex.finance.gift.event.service.impl;

import com.alex.api.finance.gift.event.query.GiftEventQuery;
import com.alex.api.finance.gift.event.vo.GiftEventBusinessVo;
import com.alex.api.finance.gift.event.vo.GiftEventInfoVo;
import com.alex.api.finance.gift.event.vo.GiftEventSummaryVo;
import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.event.entity.GiftEventInfo;
import com.alex.finance.gift.event.mapper.GiftEventInfoMapper;
import com.alex.finance.gift.event.service.GiftEventInfoService;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import com.alex.finance.gift.support.GiftExceptions;
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
public class GiftEventInfoServiceImp extends ServiceImpl<GiftEventInfoMapper, GiftEventInfo> implements GiftEventInfoService {

    private final GiftDataScopeSupport giftDataScopeSupport;

    @Autowired(required = false)
    private GiftRecordInfoService giftRecordInfoService;

    @Override
    public Page<GiftEventInfoVo> getPage(Long pageNum, Long pageSize, GiftEventQuery query) {
        return getBaseMapper().getPage(
                new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize),
                query);
    }

    @Override
    public List<GiftEventInfoVo> getList(GiftEventQuery query) {
        return getBaseMapper().getList(query);
    }

    @Override
    public GiftEventSummaryVo getSummary() {
        List<GiftEventInfo> events = getBaseMapper().listEntities(null);
        List<GiftRecordInfoVo> records = listGiftRecordsForAggregate();
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
        List<GiftEventBusinessVo> rows = getList(query).stream()
                .map(this::toBusinessVo)
                .toList();
        long from = Math.max(0, (current - 1) * size);
        long to = Math.min(rows.size(), from + size);
        Page<GiftEventBusinessVo> page = new Page<>(current, size, rows.size());
        page.setRecords(from >= rows.size() ? List.of() : rows.subList((int) from, (int) to));
        return page;
    }

    @Override
    public GiftEventInfoVo queryGiftEventInfo(Long id) {
        GiftEventInfo entity = getById(id);
        giftDataScopeSupport.assertEventAccessible(entity);
        return toVo(entity);
    }

    @Override
    public GiftEventInfoVo addGiftEventInfo(GiftEventInfoVo giftEventInfoVo) {
        fillOwner(giftEventInfoVo);
        GiftEventInfo entity = new GiftEventInfo();
        BeanUtils.copyProperties(giftEventInfoVo, entity);
        save(entity);
        giftEventInfoVo.setId(entity.getId());
        return giftEventInfoVo;
    }

    @Override
    public Boolean updateGiftEventInfo(GiftEventInfoVo giftEventInfoVo) {
        if (giftEventInfoVo == null || giftEventInfoVo.getId() == null) {
            throw GiftExceptions.param("事由ID不能为空");
        }
        GiftEventInfo existing = getById(giftEventInfoVo.getId());
        giftDataScopeSupport.assertEventAccessible(existing);
        giftEventInfoVo.setUserId(existing.getUserId());
        giftEventInfoVo.setOrgId(existing.getOrgId());
        GiftEventInfo entity = new GiftEventInfo();
        BeanUtils.copyProperties(giftEventInfoVo, entity);
        return updateById(entity);
    }

    @Override
    public Boolean deleteGiftEventInfo(String ids) {
        if (!StringUtils.hasText(ids)) {
            return true;
        }
        List<Long> idList = parseIds(ids);
        for (Long id : idList) {
            giftDataScopeSupport.assertEventAccessible(getById(id));
        }
        return removeBatchByIds(idList);
    }

    protected List<GiftRecordInfoVo> listGiftRecordsForAggregate() {
        return giftRecordInfoService == null
                ? List.of()
                : giftRecordInfoService.getList(new GiftRecordQuery());
    }

    private List<Long> parseIds(String ids) {
        try {
            return Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(Long::valueOf)
                    .toList();
        } catch (NumberFormatException ex) {
            throw GiftExceptions.param("事由ID格式不合法");
        }
    }

    private GiftEventBusinessVo toBusinessVo(GiftEventInfoVo event) {
        GiftEventBusinessVo vo = new GiftEventBusinessVo();
        BeanUtils.copyProperties(event, vo);
        List<GiftRecordInfoVo> records = listGiftRecordsForAggregate().stream()
                .filter(record -> event.getId() != null && event.getId().equals(record.getEventId()))
                .toList();
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
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        vo.setGiveAmount(giveAmount);
        vo.setReceiveAmount(receiveAmount);
        vo.setTotalAmount(giveAmount.add(receiveAmount));
        vo.setParticipantCount((long) participantIds.size());
        vo.setLocationText(StringUtils.hasText(event.getRemark()) ? event.getRemark() : "-");
        vo.setEventStatus(event.getEventTime() != null && event.getEventTime().isAfter(LocalDateTime.now()) ? "PENDING" : "FINISHED");
        return vo;
    }

    private BigDecimal amount(GiftRecordInfoVo record) {
        return record.getAmount() == null ? BigDecimal.ZERO : record.getAmount();
    }

    private void fillOwner(GiftEventInfoVo vo) {
        TUserVo loginUser = giftDataScopeSupport.requireLoginUser();
        vo.setUserId(loginUser.getId());
        OrgInfoVo orgInfoVo = loginUser.getOrgInfoVo();
        vo.setOrgId(orgInfoVo == null ? loginUser.getOrgId() : orgInfoVo.getId());
    }

    private GiftEventInfoVo toVo(GiftEventInfo entity) {
        if (entity == null) {
            return null;
        }
        GiftEventInfoVo vo = new GiftEventInfoVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
