package com.alex.finance.gift.personoption.service.impl;

import com.alex.api.finance.gift.person.vo.GiftPersonRelationItemVo;
import com.alex.api.finance.gift.person.vo.GiftPersonRelationOptionRowVo;
import com.alex.api.finance.gift.person.vo.GiftPersonRelationOptionsVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.person.entity.GiftPersonInfo;
import com.alex.finance.gift.person.mapper.GiftPersonInfoMapper;
import com.alex.finance.gift.personoption.entity.GiftPersonRelationOption;
import com.alex.finance.gift.personoption.mapper.GiftPersonRelationOptionMapper;
import com.alex.finance.gift.personoption.service.GiftPersonRelationOptionService;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import com.alex.finance.gift.support.GiftExceptions;
import com.alex.finance.gift.support.GiftRelationOptionConstants;
import com.alex.finance.gift.support.GiftRelationPresetSupport;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GiftPersonRelationOptionServiceImp
        extends ServiceImpl<GiftPersonRelationOptionMapper, GiftPersonRelationOption>
        implements GiftPersonRelationOptionService {

    private static final int MAX_LABEL_LENGTH = 20;

    private final GiftDataScopeSupport giftDataScopeSupport;
    private final GiftPersonInfoMapper giftPersonInfoMapper;
    private final GiftRelationPresetSupport giftRelationPresetSupport;

    @Override
    public GiftPersonRelationOptionsVo listRelationOptions(Long personId) {
        TUserVo loginUser = giftDataScopeSupport.requireLoginUser();
        boolean isSuper = giftDataScopeSupport.isSuper(loginUser);
        Long orgId = giftDataScopeSupport.loginOrgId(loginUser);
        Long ownerUserId = personId != null ? resolveOwnerUserId(personId) : loginUser.getId();

        backfillFromPersonHistory(ownerUserId, orgId, isSuper);
        return toRelationOptionsVo(getBaseMapper().listRelationOptionRows(ownerUserId, orgId, isSuper));
    }

    private GiftPersonRelationOptionsVo toRelationOptionsVo(List<GiftPersonRelationOptionRowVo> rows) {
        List<GiftPersonRelationItemVo> presets = new ArrayList<>();
        List<GiftPersonRelationItemVo> customs = new ArrayList<>();
        java.util.Set<String> customNames = new java.util.HashSet<>();
        if (rows != null) {
            for (GiftPersonRelationOptionRowVo row : rows) {
                GiftPersonRelationItemVo item = new GiftPersonRelationItemVo()
                        .setId(row.getId())
                        .setName(row.getRelationLabel());
                if (GiftRelationOptionConstants.OPTION_TYPE_SYSTEM.equals(row.getOptionType())) {
                    presets.add(item);
                    continue;
                }
                if (GiftRelationOptionConstants.OPTION_TYPE_CUSTOM.equals(row.getOptionType())) {
                    if (row.getRelationLabel() != null && customNames.add(row.getRelationLabel().trim())) {
                        customs.add(item);
                    }
                }
            }
        }
        return new GiftPersonRelationOptionsVo()
                .setPresets(giftRelationPresetSupport.ensurePresets(presets))
                .setCustoms(customs);
    }

    @Override
    public String resolveRelationType(Long relationOptionId, Long ownerUserId) {
        if (relationOptionId == null) {
            return null;
        }
        GiftPersonRelationOption option = getById(relationOptionId);
        if (option == null || option.getIsDelete() != null && option.getIsDelete() == 1) {
            throw GiftExceptions.param("关系选项不存在");
        }
        if (GiftRelationOptionConstants.OPTION_TYPE_SYSTEM.equals(option.getOptionType())) {
            return option.getRelationCode();
        }
        if (GiftRelationOptionConstants.OPTION_TYPE_CUSTOM.equals(option.getOptionType())) {
            TUserVo loginUser = giftDataScopeSupport.requireLoginUser();
            if (giftDataScopeSupport.isSuper(loginUser)) {
                return option.getRelationLabel();
            }
            Long myOrgId = giftDataScopeSupport.loginOrgId(loginUser);
            if (myOrgId != null && myOrgId.equals(option.getOrgId())) {
                return option.getRelationLabel();
            }
            if (ownerUserId != null && ownerUserId.equals(option.getUserId())) {
                return option.getRelationLabel();
            }
            if (loginUser.getId().equals(option.getUserId())) {
                return option.getRelationLabel();
            }
            throw GiftExceptions.forbidden("无权使用该自定义关系");
        }
        throw GiftExceptions.param("关系选项类型不合法");
    }

    @Override
    public Long findRelationOptionId(Long userId, String relationType) {
        if (!StringUtils.hasText(relationType)) {
            return null;
        }
        TUserVo loginUser = giftDataScopeSupport.requireLoginUser();
        boolean isSuper = giftDataScopeSupport.isSuper(loginUser);
        Long orgId = giftDataScopeSupport.loginOrgId(loginUser);
        Long targetUserId = userId != null ? userId : loginUser.getId();
        return getBaseMapper().findOptionIdByRelationType(targetUserId, orgId, isSuper, relationType.trim());
    }

    @Override
    public void rememberCustomRelation(Long userId, Long orgId, String relationType) {
        if (!StringUtils.hasText(relationType) || giftRelationPresetSupport.isPresetCode(relationType)) {
            return;
        }
        String label = relationType.trim();
        if (label.length() > MAX_LABEL_LENGTH) {
            throw GiftExceptions.param("自定义关系最多20个字符");
        }
        if (giftRelationPresetSupport.isPresetLabel(label)) {
            throw GiftExceptions.param("请从常用关系中选择「" + label + "」");
        }
        upsertLabel(userId, orgId, label);
    }

    private Long resolveOwnerUserId(Long personId) {
        if (personId == null) {
            return giftDataScopeSupport.requireLoginUser().getId();
        }
        GiftPersonInfo person = giftPersonInfoMapper.selectById(personId);
        giftDataScopeSupport.assertPersonAccessible(person);
        return person.getUserId();
    }

    private void backfillFromPersonHistory(Long userId, Long orgId, boolean isSuper) {
        List<String> labels = giftPersonInfoMapper.listDistinctCustomRelationTypes(userId, orgId, isSuper);
        if (labels == null || labels.isEmpty()) {
            return;
        }
        Long targetOrgId = orgId;
        if (targetOrgId == null) {
            GiftPersonInfo sample = giftPersonInfoMapper.selectOne(new LambdaQueryWrapper<GiftPersonInfo>()
                    .eq(GiftPersonInfo::getUserId, userId)
                    .eq(GiftPersonInfo::getIsDelete, 0)
                    .last("LIMIT 1"));
            targetOrgId = sample == null ? null : sample.getOrgId();
        }
        for (String label : labels) {
            if (StringUtils.hasText(label)) {
                upsertLabelIfAbsent(userId, targetOrgId, label.trim());
            }
        }
    }

    private void upsertLabel(Long userId, Long orgId, String label) {
        GiftPersonRelationOption existing = findActiveCustomOption(userId, label);
        LocalDateTime now = LocalDateTime.now();
        if (existing != null) {
            existing.setLastUsedTime(now);
            updateById(existing);
            return;
        }
        GiftPersonRelationOption option = new GiftPersonRelationOption();
        option.setUserId(userId);
        option.setOrgId(orgId);
        option.setOptionType(GiftRelationOptionConstants.OPTION_TYPE_CUSTOM);
        option.setRelationLabel(label);
        option.setSortOrder(0);
        option.setLastUsedTime(now);
        save(option);
    }

    private void upsertLabelIfAbsent(Long userId, Long orgId, String label) {
        if (findActiveCustomOption(userId, label) != null) {
            return;
        }
        GiftPersonRelationOption option = new GiftPersonRelationOption();
        option.setUserId(userId);
        option.setOrgId(orgId);
        option.setOptionType(GiftRelationOptionConstants.OPTION_TYPE_CUSTOM);
        option.setRelationLabel(label);
        option.setSortOrder(0);
        option.setLastUsedTime(LocalDateTime.now());
        save(option);
    }

    private GiftPersonRelationOption findActiveCustomOption(Long userId, String label) {
        return getOne(new LambdaQueryWrapper<GiftPersonRelationOption>()
                .eq(GiftPersonRelationOption::getUserId, userId)
                .eq(GiftPersonRelationOption::getOptionType, GiftRelationOptionConstants.OPTION_TYPE_CUSTOM)
                .eq(GiftPersonRelationOption::getRelationLabel, label)
                .last("LIMIT 1"));
    }
}
