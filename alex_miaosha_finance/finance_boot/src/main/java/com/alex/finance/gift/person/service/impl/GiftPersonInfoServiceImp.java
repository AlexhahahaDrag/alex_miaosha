package com.alex.finance.gift.person.service.impl;

import com.alex.api.finance.gift.person.query.GiftPersonQuery;
import com.alex.api.finance.gift.person.vo.GiftPersonBusinessVo;
import com.alex.api.finance.gift.person.vo.GiftPersonInfoVo;
import com.alex.api.finance.gift.person.vo.GiftPersonProfileVo;
import com.alex.api.finance.gift.person.vo.GiftPersonSummaryVo;
import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.orgUserInfo.api.OrgUserInfoApi;
import com.alex.api.user.orgUserInfo.vo.OrgUserInfoVo;
import com.alex.api.user.userInfo.api.UserApi;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.base.common.Result;
import com.alex.finance.gift.person.entity.GiftPersonInfo;
import com.alex.finance.gift.person.mapper.GiftPersonInfoMapper;
import com.alex.finance.gift.person.service.GiftPersonInfoService;
import com.alex.finance.gift.personoption.service.GiftPersonRelationOptionService;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import com.alex.finance.gift.support.GiftExceptions;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GiftPersonInfoServiceImp extends ServiceImpl<GiftPersonInfoMapper, GiftPersonInfo>
        implements GiftPersonInfoService {

    private final GiftDataScopeSupport giftDataScopeSupport;
    private final GiftPersonRelationOptionService giftPersonRelationOptionService;
    private final GiftRecordInfoService giftRecordInfoService;
    private final OrgUserInfoApi orgUserInfoApi;
    private final UserApi userApi;

    @Override
    public Page<GiftPersonInfoVo> getPage(Long pageNum, Long pageSize, GiftPersonQuery query) {
        return getBaseMapper().getPage(
                new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize),
                query);
    }

    @Override
    public List<GiftPersonInfoVo> getList(GiftPersonQuery query) {
        return getBaseMapper().getList(query);
    }

    @Override
    public GiftPersonSummaryVo getSummary() {
        long personCount = getBaseMapper().listEntities(null).size();
        List<GiftRecordInfoVo> records = listGiftRecordsForAggregate();
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
        return getBaseMapper().getBusinessPage(
                new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize),
                query);
    }

    @Override
    public GiftPersonProfileVo getProfile(Long id) {
        GiftPersonInfo person = getById(id);
        giftDataScopeSupport.assertPersonAccessible(person);
        GiftPersonProfileVo profile = new GiftPersonProfileVo();
        if (person == null) {
            return profile;
        }
        GiftPersonInfoVo personVo = toVo(person);
        profile.setPerson(toBusinessVo(personVo));
        profile.setRecords(listGiftRecordsForAggregate().stream()
                .filter(record -> personInRecord(record, id))
                .sorted(Comparator.comparing(GiftRecordInfoVo::getPayTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .toList());
        return profile;
    }

    @Override
    public GiftPersonInfoVo queryGiftPersonInfo(Long id) {
        GiftPersonInfo entity = getById(id);
        giftDataScopeSupport.assertPersonAccessible(entity);
        return toVo(entity);
    }

    @Override
    public GiftPersonInfoVo addGiftPersonInfo(GiftPersonInfoVo giftPersonInfoVo) {
        fillOwner(giftPersonInfoVo);
        applyRelationOption(giftPersonInfoVo);
        GiftPersonInfo entity = new GiftPersonInfo();
        BeanUtils.copyProperties(giftPersonInfoVo, entity);
        save(entity);
        giftPersonInfoVo.setId(entity.getId());
        rememberRelationOption(entity);
        return enrichRelationOption(giftPersonInfoVo);
    }

    @Override
    public Boolean updateGiftPersonInfo(GiftPersonInfoVo giftPersonInfoVo) {
        if (giftPersonInfoVo == null || giftPersonInfoVo.getId() == null) {
            throw GiftExceptions.param("亲友ID不能为空");
        }
        GiftPersonInfo existing = getById(giftPersonInfoVo.getId());
        giftDataScopeSupport.assertPersonAccessible(existing);
        giftPersonInfoVo.setUserId(existing.getUserId());
        giftPersonInfoVo.setOrgId(existing.getOrgId());
        applyRelationOption(giftPersonInfoVo);
        GiftPersonInfo entity = new GiftPersonInfo();
        BeanUtils.copyProperties(giftPersonInfoVo, entity);
        boolean updated = updateById(entity);
        if (updated) {
            rememberRelationOption(entity);
        }
        return updated;
    }

    @Override
    public Boolean deleteGiftPersonInfo(String ids) {
        if (!StringUtils.hasText(ids)) {
            return true;
        }
        List<Long> idList = parseIds(ids);
        for (Long id : idList) {
            giftDataScopeSupport.assertPersonAccessible(getById(id));
        }
        return removeBatchByIds(idList);
    }

    @Override
    public List<GiftPersonInfoVo> listOrgMemberOptions(String keyword) {
        TUserVo loginUser = giftDataScopeSupport.requireLoginUser();
        Long orgId = giftDataScopeSupport.loginOrgId(loginUser);
        List<Long> memberUserIds = resolveOrgMemberUserIds(loginUser, orgId);
        return memberUserIds.stream()
                .map(userId -> ensureOrgMemberPerson(orgId, userId, loginUser))
                .filter(Objects::nonNull)
                .filter(vo -> matchesKeyword(vo, keyword))
                .sorted(Comparator.comparing(GiftPersonInfoVo::getPersonName,
                        Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.toList());
    }

    private List<Long> resolveOrgMemberUserIds(TUserVo loginUser, Long orgId) {
        if (orgId == null) {
            return List.of(loginUser.getId());
        }
        if (giftDataScopeSupport.isSuper(loginUser) || giftDataScopeSupport.isAdmin(loginUser)) {
            OrgUserInfoVo query = new OrgUserInfoVo();
            query.setOrgId(String.valueOf(orgId));
            query.setStatus("1");
            Result<Page<OrgUserInfoVo>> result =
                    orgUserInfoApi.getOrgUserInfoPage(1L, 500L, query);
            if (result == null || result.getData() == null || result.getData().getRecords() == null) {
                return List.of(loginUser.getId());
            }
            return result.getData().getRecords().stream()
                    .map(OrgUserInfoVo::getUserId)
                    .filter(StringUtils::hasText)
                    .map(Long::valueOf)
                    .distinct()
                    .collect(Collectors.toList());
        }
        return List.of(loginUser.getId());
    }

    private GiftPersonInfoVo ensureOrgMemberPerson(Long orgId, Long bindUserId, TUserVo loginUser) {
        GiftPersonInfo existing = findOrgMemberPerson(orgId, bindUserId);
        if (existing != null) {
            return toVo(existing);
        }
        GiftPersonInfo entity = new GiftPersonInfo();
        entity.setOrgId(orgId);
        entity.setUserId(bindUserId);
        entity.setBindUserId(bindUserId);
        entity.setPersonName(resolveMemberDisplayName(bindUserId, loginUser));
        save(entity);
        return toVo(entity);
    }

    private GiftPersonInfo findOrgMemberPerson(Long orgId, Long bindUserId) {
        return getOne(Wrappers.<GiftPersonInfo>lambdaQuery()
                .eq(GiftPersonInfo::getIsDelete, 0)
                .eq(GiftPersonInfo::getBindUserId, bindUserId)
                .eq(orgId != null, GiftPersonInfo::getOrgId, orgId)
                .isNull(orgId == null, GiftPersonInfo::getOrgId)
                .last("LIMIT 1"));
    }

    private String resolveMemberDisplayName(Long bindUserId, TUserVo loginUser) {
        if (bindUserId != null && bindUserId.equals(loginUser.getId())) {
            if (StringUtils.hasText(loginUser.getNickName())) {
                return loginUser.getNickName();
            }
            if (StringUtils.hasText(loginUser.getUsername())) {
                return loginUser.getUsername();
            }
        }
        Result<TUserVo> userResult = userApi.queryUser(String.valueOf(bindUserId));
        if (userResult != null && userResult.getData() != null) {
            TUserVo user = userResult.getData();
            if (StringUtils.hasText(user.getNickName())) {
                return user.getNickName();
            }
            if (StringUtils.hasText(user.getUsername())) {
                return user.getUsername();
            }
        }
        return "家庭成员";
    }

    private boolean matchesKeyword(GiftPersonInfoVo vo, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String text = keyword.trim();
        return (vo.getPersonName() != null && vo.getPersonName().contains(text))
                || (vo.getPhone() != null && vo.getPhone().contains(text));
    }

    protected List<GiftRecordInfoVo> listGiftRecordsForAggregate() {
        return giftRecordInfoService == null
                ? List.of()
                : giftRecordInfoService.getList(new GiftRecordQuery());
    }

    private void fillOwner(GiftPersonInfoVo vo) {
        TUserVo loginUser = giftDataScopeSupport.requireLoginUser();
        vo.setUserId(loginUser.getId());
        OrgInfoVo orgInfoVo = loginUser.getOrgInfoVo();
        vo.setOrgId(orgInfoVo == null ? loginUser.getOrgId() : orgInfoVo.getId());
    }

    private void rememberRelationOption(GiftPersonInfo entity) {
        giftPersonRelationOptionService.rememberCustomRelation(
                entity.getUserId(), entity.getOrgId(), entity.getRelationType());
    }

    private void applyRelationOption(GiftPersonInfoVo vo) {
        if (vo.getRelationOptionId() == null) {
            return;
        }
        vo.setRelationType(giftPersonRelationOptionService.resolveRelationType(
                vo.getRelationOptionId(), vo.getUserId()));
    }

    private GiftPersonInfoVo enrichRelationOption(GiftPersonInfoVo vo) {
        if (vo == null) {
            return null;
        }
        vo.setRelationOptionId(giftPersonRelationOptionService.findRelationOptionId(
                vo.getUserId(), vo.getRelationType()));
        return vo;
    }

    private List<Long> parseIds(String ids) {
        try {
            return Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(Long::valueOf)
                    .toList();
        } catch (NumberFormatException ex) {
            throw GiftExceptions.param("亲友ID格式不合法");
        }
    }

    protected GiftPersonBusinessVo toBusinessVo(GiftPersonInfoVo person) {
        GiftPersonBusinessVo vo = new GiftPersonBusinessVo();
        BeanUtils.copyProperties(person, vo);
        List<GiftRecordInfoVo> personRecords = listGiftRecordsForAggregate().stream()
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
        Optional<GiftRecordInfoVo> latest = personRecords.stream()
                .max(Comparator.comparing(GiftRecordInfoVo::getPayTime,
                        Comparator.nullsFirst(Comparator.naturalOrder())));
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

    private boolean personInRecord(GiftRecordInfoVo record, Long personId) {
        return personId != null
                && (personId.equals(record.getGiverPersonId()) || personId.equals(record.getReceiverPersonId()));
    }

    private BigDecimal amount(GiftRecordInfoVo record) {
        return record.getAmount() == null ? BigDecimal.ZERO : record.getAmount();
    }

    private GiftPersonInfoVo toVo(GiftPersonInfo entity) {
        if (entity == null) {
            return null;
        }
        GiftPersonInfoVo vo = new GiftPersonInfoVo();
        BeanUtils.copyProperties(entity, vo);
        return enrichRelationOption(vo);
    }
}
