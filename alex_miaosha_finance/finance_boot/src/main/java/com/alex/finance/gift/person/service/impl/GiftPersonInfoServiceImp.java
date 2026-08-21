package com.alex.finance.gift.person.service.impl;

import com.alex.api.finance.gift.person.query.GiftPersonQuery;
import com.alex.api.finance.gift.person.vo.GiftPersonBusinessVo;
import com.alex.api.finance.gift.person.vo.GiftPersonInfoVo;
import com.alex.api.finance.gift.person.vo.GiftPersonProfileVo;
import com.alex.api.finance.gift.person.vo.GiftPersonSummaryVo;
import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.api.finance.gift.summary.vo.GiftRelationDistributionVo;
import com.alex.api.oss.fileInfo.api.OssApi;
import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.orgUserInfo.api.OrgUserInfoApi;
import com.alex.api.user.orgUserInfo.vo.OrgUserInfoVo;
import com.alex.api.user.userInfo.api.UserApi;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.base.common.Result;
import com.alex.base.constants.SysConf;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GiftPersonInfoServiceImp extends ServiceImpl<GiftPersonInfoMapper, GiftPersonInfo>
        implements GiftPersonInfoService {

    private final GiftDataScopeSupport giftDataScopeSupport;
    private final GiftPersonRelationOptionService giftPersonRelationOptionService;
    private final GiftRecordInfoService giftRecordInfoService;
    private final OrgUserInfoApi orgUserInfoApi;
    private final UserApi userApi;
    private final OssApi ossApi;

    @Override
    public Page<GiftPersonInfoVo> getPage(Long pageNum, Long pageSize, GiftPersonQuery query) {
        Page<GiftPersonInfoVo> page = getBaseMapper().getPage(
                new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize),
                query);
        fillAvatarUrls(page.getRecords());
        return page;
    }

    @Override
    public List<GiftPersonInfoVo> getList(GiftPersonQuery query) {
        List<GiftPersonInfoVo> list = getBaseMapper().getList(query);
        fillAvatarUrls(list);
        return list;
    }

    @Override
    public GiftPersonSummaryVo getSummary() {
        List<GiftPersonInfoVo> people = getList(new GiftPersonQuery());
        long personCount = people.size();
        List<GiftRecordInfoVo> records = listGiftRecordsForAggregate();

        BigDecimal receiveSum = records.stream()
                .filter(record -> "RECEIVE".equals(record.getDirection()))
                .map(this::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal giveSum = records.stream()
                .filter(record -> "GIVE".equals(record.getDirection()) || "RETURN".equals(record.getDirection()))
                .map(this::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netAmount = receiveSum.subtract(giveSum);

        long activeCount = 0;
        long pendingMaintenanceCount = 0;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime ninetyDaysAgo = now.minusDays(90);
        LocalDateTime oneEightyDaysAgo = now.minusDays(180);

        for (GiftPersonInfoVo person : people) {
            java.util.Optional<GiftRecordInfoVo> latest = records.stream()
                    .filter(r -> personInRecord(r, person.getId()))
                    .max(java.util.Comparator.comparing(GiftRecordInfoVo::getPayTime,
                            java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder())));

            if (latest.isPresent()) {
                LocalDateTime payTime = latest.get().getPayTime();
                if (payTime != null) {
                    if (payTime.isAfter(ninetyDaysAgo)) {
                        activeCount++;
                    } else if (payTime.isBefore(oneEightyDaysAgo)) {
                        pendingMaintenanceCount++;
                    }
                } else {
                    pendingMaintenanceCount++;
                }
            } else {
                pendingMaintenanceCount++;
            }
        }

        // 年度往来总额：仅统计当前自然年内发生（payTime）的记录，payTime 为空的不计入
        int currentYear = now.getYear();
        BigDecimal yearTotalAmount = records.stream()
                .filter(record -> record.getPayTime() != null && record.getPayTime().getYear() == currentYear)
                .map(this::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingReturnAmount = records.stream()
                .filter(record -> "RECEIVE".equals(record.getDirection()))
                .filter(record -> record.getReturnedFlag() == null || record.getReturnedFlag() == 0)
                .map(this::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new GiftPersonSummaryVo()
                .setPersonCount(personCount)
                .setNetAmount(netAmount)
                .setActiveCount(activeCount)
                .setPendingMaintenanceCount(pendingMaintenanceCount)
                .setYearTotalAmount(yearTotalAmount)
                .setPendingReturnAmount(pendingReturnAmount);
    }

    @Override
    public Page<GiftPersonBusinessVo> getBusinessPage(Long pageNum, Long pageSize, GiftPersonQuery query) {
        Page<GiftPersonBusinessVo> page = getBaseMapper().getBusinessPage(
                new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize),
                query);
        if (page.getRecords() != null) {
            LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
            LocalDateTime oneEightyDaysAgo = LocalDateTime.now().minusDays(180);
            for (GiftPersonBusinessVo vo : page.getRecords()) {
                if (vo.getLatestRecordTime() != null) {
                    if (vo.getLatestRecordTime().isAfter(ninetyDaysAgo)) {
                        vo.setRelationStatus("ACTIVE");
                    } else if (vo.getLatestRecordTime().isBefore(oneEightyDaysAgo)) {
                        vo.setRelationStatus("DISTANT");
                    } else {
                        vo.setRelationStatus("GENERAL");
                    }
                } else {
                    vo.setRelationStatus("DISTANT");
                }
            }
        }
        fillAvatarUrls(page.getRecords());
        return page;
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

    /** 事务边界：主表落库 + rememberRelationOption 写关系词典表，需保持原子。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 家庭组成员选项（懒建档）：成员首次出现时自动创建其亲友档案。
     * <p>
     * 性能：已建档成员一次 IN 批量查询取回，仅缺档成员逐个走 Feign 取展示名并插库；
     * 事务：可能发生多条插入，整体包在同一事务内。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<GiftPersonInfoVo> listOrgMemberOptions(String keyword) {
        TUserVo loginUser = giftDataScopeSupport.requireLoginUser();
        boolean isSuper = giftDataScopeSupport.isSuper(loginUser);
        Long orgId = giftDataScopeSupport.loginOrgId(loginUser);
        List<Long> memberUserIds = resolveOrgMemberUserIds(loginUser, orgId);
        Map<Long, GiftPersonInfo> existingByBindUserId = listOrgMemberPersons(orgId, memberUserIds, isSuper);
        return memberUserIds.stream()
                .map(userId -> {
                    GiftPersonInfo existing = existingByBindUserId.get(userId);
                    return existing != null
                            ? toVo(existing)
                            : createOrgMemberPerson(orgId, userId, loginUser);
                })
                .filter(Objects::nonNull)
                .filter(vo -> matchesKeyword(vo, keyword))
                .sorted(Comparator.comparing(GiftPersonInfoVo::getPersonName,
                        Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.toList());
    }

    private List<Long> resolveOrgMemberUserIds(TUserVo loginUser, Long orgId) {
        if (giftDataScopeSupport.isSuper(loginUser)) {
            // 超级管理员：查询系统内全部用户
            Result<List<TUserVo>> userListRes = userApi.getList(new TUserVo());
            if (userListRes != null && userListRes.getData() != null) {
                return userListRes.getData().stream()
                        .map(TUserVo::getId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());
            }
            return List.of(loginUser.getId());
        }
        if (orgId == null) {
            return List.of(loginUser.getId());
        }
        if (giftDataScopeSupport.isAdmin(loginUser)) {
            // 家庭组管理员：查询当前机构（家庭组）下全部有效成员
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

    /** 关系类型分布：SQL GROUP BY 聚合，数据权限由 mapper 注解过滤。 */
    @Override
    public List<GiftRelationDistributionVo> getRelationDistribution() {
        return getBaseMapper().countRelationDistribution();
    }

    /** 批量查询已建档的家庭组成员亲友（key=bindUserId；同一成员多行时保留最早一行，等价原 LIMIT 1 语义）。 */
    private Map<Long, GiftPersonInfo> listOrgMemberPersons(Long orgId, List<Long> memberUserIds, boolean isSuper) {
        if (memberUserIds == null || memberUserIds.isEmpty()) {
            return Map.of();
        }
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GiftPersonInfo> wrapper =
                Wrappers.<GiftPersonInfo>lambdaQuery()
                        .eq(GiftPersonInfo::getIsDelete, 0)
                        .in(GiftPersonInfo::getBindUserId, memberUserIds);
        if (!isSuper) {
            wrapper.eq(orgId != null, GiftPersonInfo::getOrgId, orgId)
                    .isNull(orgId == null, GiftPersonInfo::getOrgId);
        }
        List<GiftPersonInfo> persons = list(wrapper);
        Map<Long, GiftPersonInfo> byBindUserId = new java.util.LinkedHashMap<>();
        for (GiftPersonInfo person : persons) {
            byBindUserId.putIfAbsent(person.getBindUserId(), person);
        }
        return byBindUserId;
    }

    /** 为缺档成员创建亲友档案（仅在成员首次进入选项列表时触发一次）。 */
    private GiftPersonInfoVo createOrgMemberPerson(Long orgId, Long bindUserId, TUserVo loginUser) {
        GiftPersonInfo entity = new GiftPersonInfo();
        entity.setOrgId(orgId);
        entity.setUserId(bindUserId);
        entity.setBindUserId(bindUserId);
        entity.setPersonName(resolveMemberDisplayName(bindUserId, loginUser));
        save(entity);
        return toVo(entity);
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
                    .map(s -> s == null ? "" : s.trim())
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

        // Calculate relationStatus
        if (vo.getLatestRecordTime() != null) {
            LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
            LocalDateTime oneEightyDaysAgo = LocalDateTime.now().minusDays(180);
            if (vo.getLatestRecordTime().isAfter(ninetyDaysAgo)) {
                vo.setRelationStatus("ACTIVE");
            } else if (vo.getLatestRecordTime().isBefore(oneEightyDaysAgo)) {
                vo.setRelationStatus("DISTANT");
            } else {
                vo.setRelationStatus("GENERAL");
            }
        } else {
            vo.setRelationStatus("DISTANT");
        }

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
        GiftPersonInfoVo enriched = enrichRelationOption(vo);
        fillAvatarUrls(enriched);
        return enriched;
    }

    private void fillAvatarUrls(GiftPersonInfoVo vo) {
        if (vo == null || vo.getAvatar() == null) {
            return;
        }
        try {
            Result<List<FileInfoVo>> fileInfo = ossApi.getFileInfo(List.of(vo.getAvatar()));
            if (fileInfo != null
                    && SysConf.RESULT_SUCCESS.equals(fileInfo.getCode())
                    && fileInfo.getData() != null
                    && !fileInfo.getData().isEmpty()) {
                vo.setFileInfoVo(fileInfo.getData().get(0));
            }
        } catch (Exception e) {
            log.error("获取亲友头像文件错误：{}", e.getMessage());
        }
    }

    private void fillAvatarUrls(Collection<? extends GiftPersonInfoVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> fileIdList = records.stream()
                .map(GiftPersonInfoVo::getAvatar)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (fileIdList.isEmpty()) {
            return;
        }
        try {
            Result<List<FileInfoVo>> result = ossApi.getFileInfo(fileIdList);
            if (result != null
                    && SysConf.RESULT_SUCCESS.equals(result.getCode())
                    && result.getData() != null
                    && !result.getData().isEmpty()) {
                Map<Long, List<FileInfoVo>> fileMap = result.getData().stream()
                        .collect(Collectors.groupingBy(FileInfoVo::getId));
                records.forEach(item -> {
                    List<FileInfoVo> fileInfoVos = fileMap.get(item.getAvatar());
                    if (fileInfoVos != null && !fileInfoVos.isEmpty()) {
                        item.setFileInfoVo(fileInfoVos.get(0));
                    }
                });
            }
        } catch (Exception e) {
            log.error("批量获取亲友头像失败！", e);
        }
    }
}
