package com.alex.finance.gift.person.service.impl;

import com.alex.api.finance.gift.person.query.GiftPersonQuery;
import com.alex.api.finance.gift.person.vo.GiftPersonBusinessVo;
import com.alex.api.finance.gift.person.vo.GiftPersonInfoTVo;
import com.alex.api.finance.gift.person.vo.GiftPersonProfileVo;
import com.alex.api.finance.gift.person.vo.GiftPersonSummaryVo;
import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoTVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.person.entity.GiftPersonInfoT;
import com.alex.finance.gift.person.mapper.GiftPersonInfoTMapper;
import com.alex.finance.gift.person.service.GiftPersonInfoTService;
import com.alex.finance.gift.record.service.GiftRecordInfoTService;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GiftPersonInfoTServiceImp extends ServiceImpl<GiftPersonInfoTMapper, GiftPersonInfoT> implements GiftPersonInfoTService {

    private final GiftDataScopeSupport giftDataScopeSupport;

    @Autowired(required = false)
    private GiftRecordInfoTService giftRecordInfoTService;

    @Override
    public Page<GiftPersonInfoTVo> getPage(Long pageNum, Long pageSize, GiftPersonQuery query) {
        return getBaseMapper().getPage(
                new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize),
                query);
    }

    @Override
    public List<GiftPersonInfoTVo> getList(GiftPersonQuery query) {
        return getBaseMapper().getList(query);
    }

    @Override
    public GiftPersonSummaryVo getSummary() {
        long personCount = getBaseMapper().listEntities(null).size();
        List<GiftRecordInfoTVo> records = listGiftRecordsForAggregate();
        BigDecimal yearTotalAmount = records.stream()
                .map(this::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingReturnAmount = records.stream()
                .filter(record -> "RECEIVE".equals(record.getDirection()))
                .filter(record -> record.getReturnedFlag() == null || record.getReturnedFlag() == 0)
                .map(this::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new GiftPersonSummaryVo()
                .setPersonCount(personCount)
                .setYearTotalAmount(yearTotalAmount)
                .setPendingReturnAmount(pendingReturnAmount);
    }

    @Override
    public Page<GiftPersonBusinessVo> getBusinessPage(Long pageNum, Long pageSize, GiftPersonQuery query) {
        long current = pageNum == null ? 1 : pageNum;
        long size = pageSize == null ? 10 : pageSize;
        List<GiftPersonBusinessVo> rows = getBaseMapper().getList(query).stream()
                .map(this::toBusinessVo)
                .collect(Collectors.toList());
        long from = Math.max(0, (current - 1) * size);
        long to = Math.min(rows.size(), from + size);
        Page<GiftPersonBusinessVo> page = new Page<>(current, size, rows.size());
        page.setRecords(from >= rows.size() ? List.of() : rows.subList((int) from, (int) to));
        return page;
    }

    @Override
    public GiftPersonProfileVo getProfile(Long id) {
        GiftPersonInfoT person = getById(id);
        giftDataScopeSupport.assertPersonAccessible(person);
        GiftPersonProfileVo profile = new GiftPersonProfileVo();
        if (person == null) {
            return profile;
        }
        GiftPersonInfoTVo personVo = toVo(person);
        profile.setPerson(toBusinessVo(personVo));
        profile.setRecords(listGiftRecordsForAggregate().stream()
                .filter(record -> personInRecord(record, id))
                .sorted(Comparator.comparing(GiftRecordInfoTVo::getPayTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .collect(Collectors.toList()));
        return profile;
    }

    @Override
    public GiftPersonInfoTVo queryGiftPersonInfoT(Long id) {
        GiftPersonInfoT entity = getById(id);
        giftDataScopeSupport.assertPersonAccessible(entity);
        return toVo(entity);
    }

    @Override
    public GiftPersonInfoTVo addGiftPersonInfoT(GiftPersonInfoTVo giftPersonInfoTVo) {
        fillOwner(giftPersonInfoTVo);
        GiftPersonInfoT entity = new GiftPersonInfoT();
        BeanUtils.copyProperties(giftPersonInfoTVo, entity);
        save(entity);
        giftPersonInfoTVo.setId(entity.getId());
        return giftPersonInfoTVo;
    }

    @Override
    public Boolean updateGiftPersonInfoT(GiftPersonInfoTVo giftPersonInfoTVo) {
        if (giftPersonInfoTVo == null || giftPersonInfoTVo.getId() == null) {
            throw GiftExceptions.param("亲友ID不能为空");
        }
        GiftPersonInfoT existing = getById(giftPersonInfoTVo.getId());
        giftDataScopeSupport.assertPersonAccessible(existing);
        giftPersonInfoTVo.setUserId(existing.getUserId());
        giftPersonInfoTVo.setOrgId(existing.getOrgId());
        GiftPersonInfoT entity = new GiftPersonInfoT();
        BeanUtils.copyProperties(giftPersonInfoTVo, entity);
        return updateById(entity);
    }

    @Override
    public Boolean deleteGiftPersonInfoT(String ids) {
        if (!StringUtils.hasText(ids)) {
            return true;
        }
        List<Long> idList = parseIds(ids);
        for (Long id : idList) {
            giftDataScopeSupport.assertPersonAccessible(getById(id));
        }
        return removeBatchByIds(idList);
    }

    protected List<GiftRecordInfoTVo> listGiftRecordsForAggregate() {
        return giftRecordInfoTService == null
                ? List.of()
                : giftRecordInfoTService.getList(new GiftRecordQuery());
    }

    private void fillOwner(GiftPersonInfoTVo vo) {
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
            throw GiftExceptions.param("亲友ID格式不合法");
        }
    }

    private GiftPersonBusinessVo toBusinessVo(GiftPersonInfoTVo person) {
        GiftPersonBusinessVo vo = new GiftPersonBusinessVo();
        BeanUtils.copyProperties(person, vo);
        List<GiftRecordInfoTVo> personRecords = listGiftRecordsForAggregate().stream()
                .filter(record -> personInRecord(record, person.getId()))
                .toList();
        BigDecimal giveAmount = personRecords.stream()
                .filter(record -> "GIVE".equals(record.getDirection()) || "RETURN".equals(record.getDirection()))
                .map(this::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal receiveAmount = personRecords.stream()
                .filter(record -> "RECEIVE".equals(record.getDirection()))
                .map(this::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Optional<GiftRecordInfoTVo> latest = personRecords.stream()
                .max(Comparator.comparing(GiftRecordInfoTVo::getPayTime, Comparator.nullsFirst(Comparator.naturalOrder())));
        vo.setTotalGiveAmount(giveAmount);
        vo.setTotalReceiveAmount(receiveAmount);
        vo.setNetAmount(receiveAmount.subtract(giveAmount));
        vo.setPendingReturnAmount(receiveAmount.subtract(giveAmount).max(BigDecimal.ZERO));
        latest.ifPresent(record -> {
            vo.setLatestRecordTime(record.getPayTime());
            vo.setLatestDirection(record.getDirection());
        });
        return vo;
    }

    private boolean personInRecord(GiftRecordInfoTVo record, Long personId) {
        return personId != null && (personId.equals(record.getGiverPersonId()) || personId.equals(record.getReceiverPersonId()));
    }

    private BigDecimal amount(GiftRecordInfoTVo record) {
        return record.getAmount() == null ? BigDecimal.ZERO : record.getAmount();
    }

    private GiftPersonInfoTVo toVo(GiftPersonInfoT entity) {
        if (entity == null) {
            return null;
        }
        GiftPersonInfoTVo vo = new GiftPersonInfoTVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
