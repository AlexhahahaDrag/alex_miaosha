package com.alex.finance.gift.person.service.impl;

import com.alex.api.finance.gift.person.query.GiftPersonQuery;
import com.alex.api.finance.gift.person.vo.GiftPersonBusinessVo;
import com.alex.api.finance.gift.person.vo.GiftPersonInfoTVo;
import com.alex.api.finance.gift.person.vo.GiftPersonProfileVo;
import com.alex.api.finance.gift.person.vo.GiftPersonSummaryVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.record.entity.GiftRecordInfoT;
import com.alex.finance.gift.record.service.GiftRecordInfoTService;
import com.alex.finance.gift.person.entity.GiftPersonInfoT;
import com.alex.finance.gift.person.mapper.GiftPersonInfoTMapper;
import com.alex.finance.gift.person.service.GiftPersonInfoTService;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GiftPersonInfoTServiceImp extends ServiceImpl<GiftPersonInfoTMapper, GiftPersonInfoT> implements GiftPersonInfoTService {

    private final UserUtils userUtils;

    @Autowired(required = false)
    private GiftRecordInfoTService giftRecordInfoTService;

    @Override
    public Page<GiftPersonInfoTVo> getPage(Long pageNum, Long pageSize, GiftPersonQuery query) {
        Page<GiftPersonInfoT> entityPage = page(new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize), queryWrapper(query));
        return copyPage(entityPage);
    }

    @Override
    public List<GiftPersonInfoTVo> getList(GiftPersonQuery query) {
        return list(queryWrapper(query)).stream().map(this::toVo).collect(Collectors.toList());
    }

    @Override
    public GiftPersonSummaryVo getSummary() {
        List<GiftPersonInfoT> people = list(queryWrapper(null));
        List<GiftRecordInfoT> records = listGiftRecordsForAggregate();
        BigDecimal yearTotalAmount = records.stream()
                .map(this::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingReturnAmount = records.stream()
                .filter(record -> "RECEIVE".equals(record.getDirection()))
                .filter(record -> record.getReturnedFlag() == null || record.getReturnedFlag() == 0)
                .map(this::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new GiftPersonSummaryVo()
                .setPersonCount((long) people.size())
                .setYearTotalAmount(yearTotalAmount)
                .setPendingReturnAmount(pendingReturnAmount);
    }

    @Override
    public Page<GiftPersonBusinessVo> getBusinessPage(Long pageNum, Long pageSize, GiftPersonQuery query) {
        long current = pageNum == null ? 1 : pageNum;
        long size = pageSize == null ? 10 : pageSize;
        List<GiftPersonBusinessVo> rows = list(queryWrapper(query)).stream()
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
        GiftPersonProfileVo profile = new GiftPersonProfileVo();
        if (person == null) {
            return profile;
        }
        profile.setPerson(toBusinessVo(person));
        profile.setRecords(listGiftRecordsForAggregate().stream()
                .filter(record -> personInRecord(record, id))
                .sorted(Comparator.comparing(GiftRecordInfoT::getPayTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(this::toRecordVo)
                .collect(Collectors.toList()));
        return profile;
    }

    @Override
    public GiftPersonInfoTVo queryGiftPersonInfoT(Long id) {
        return toVo(getById(id));
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
        GiftPersonInfoT entity = new GiftPersonInfoT();
        BeanUtils.copyProperties(giftPersonInfoTVo, entity);
        return updateById(entity);
    }

    @Override
    public Boolean deleteGiftPersonInfoT(String ids) {
        if (!StringUtils.hasText(ids)) {
            return true;
        }
        return removeBatchByIds(Arrays.asList(ids.split(",")));
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GiftPersonInfoT> queryWrapper(GiftPersonQuery query) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GiftPersonInfoT> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (query == null) {
            return wrapper.orderByDesc(GiftPersonInfoT::getCreateTime);
        }
        wrapper.and(StringUtils.hasText(query.getKeyword()), item -> item
                .like(GiftPersonInfoT::getPersonName, query.getKeyword())
                .or()
                .like(GiftPersonInfoT::getPhone, query.getKeyword()));
        wrapper.eq(StringUtils.hasText(query.getRelationType()), GiftPersonInfoT::getRelationType, query.getRelationType());
        return wrapper.orderByDesc(GiftPersonInfoT::getCreateTime);
    }

    private void fillOwner(GiftPersonInfoTVo vo) {
        TUserVo loginUser = userUtils == null ? null : userUtils.getLoginUser();
        if (loginUser == null) {
            return;
        }
        vo.setUserId(loginUser.getId());
        vo.setOrgId(loginUser.getOrgInfoVo() == null ? loginUser.getOrgId() : loginUser.getOrgInfoVo().getId());
    }

    private Page<GiftPersonInfoTVo> copyPage(Page<GiftPersonInfoT> entityPage) {
        Page<GiftPersonInfoTVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return voPage;
    }

    protected List<GiftRecordInfoT> listGiftRecordsForAggregate() {
        return giftRecordInfoTService == null ? List.of() : giftRecordInfoTService.list();
    }

    private GiftPersonBusinessVo toBusinessVo(GiftPersonInfoT entity) {
        GiftPersonBusinessVo vo = new GiftPersonBusinessVo();
        BeanUtils.copyProperties(entity, vo);
        List<GiftRecordInfoT> personRecords = listGiftRecordsForAggregate().stream()
                .filter(record -> personInRecord(record, entity.getId()))
                .collect(Collectors.toList());
        BigDecimal giveAmount = personRecords.stream()
                .filter(record -> "GIVE".equals(record.getDirection()) || "RETURN".equals(record.getDirection()))
                .map(this::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal receiveAmount = personRecords.stream()
                .filter(record -> "RECEIVE".equals(record.getDirection()))
                .map(this::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Optional<GiftRecordInfoT> latest = personRecords.stream()
                .max(Comparator.comparing(GiftRecordInfoT::getPayTime, Comparator.nullsFirst(Comparator.naturalOrder())));
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

    private boolean personInRecord(GiftRecordInfoT record, Long personId) {
        return personId != null && (personId.equals(record.getGiverPersonId()) || personId.equals(record.getReceiverPersonId()));
    }

    private BigDecimal amount(GiftRecordInfoT record) {
        return record.getAmount() == null ? BigDecimal.ZERO : record.getAmount();
    }

    private com.alex.api.finance.gift.record.vo.GiftRecordInfoTVo toRecordVo(GiftRecordInfoT entity) {
        com.alex.api.finance.gift.record.vo.GiftRecordInfoTVo vo = new com.alex.api.finance.gift.record.vo.GiftRecordInfoTVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
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
