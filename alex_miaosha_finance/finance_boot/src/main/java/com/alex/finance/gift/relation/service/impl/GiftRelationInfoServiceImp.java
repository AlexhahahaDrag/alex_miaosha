package com.alex.finance.gift.relation.service.impl;

import com.alex.api.finance.gift.relation.query.GiftRelationQuery;
import com.alex.api.finance.gift.relation.vo.GiftRelationInfoVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.relation.entity.GiftRelationInfo;
import com.alex.finance.gift.relation.mapper.GiftRelationInfoMapper;
import com.alex.finance.gift.relation.service.GiftRelationInfoService;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import com.alex.finance.gift.support.GiftExceptions;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GiftRelationInfoServiceImp extends ServiceImpl<GiftRelationInfoMapper, GiftRelationInfo>
        implements GiftRelationInfoService {

    private final GiftDataScopeSupport giftDataScopeSupport;

    @Override
    public Page<GiftRelationInfoVo> getPage(Long pageNum, Long pageSize, GiftRelationQuery query) {
        return getBaseMapper().getPage(
                new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize),
                query);
    }

    @Override
    public List<GiftRelationInfoVo> getList(GiftRelationQuery query) {
        return getBaseMapper().getList(query);
    }

    @Override
    public GiftRelationInfoVo queryGiftRelationInfo(Long id) {
        GiftRelationInfo entity = getById(id);
        giftDataScopeSupport.assertRelationAccessible(entity);
        return toVo(entity);
    }

    @Override
    public GiftRelationInfoVo addGiftRelationInfo(GiftRelationInfoVo giftRelationInfoVo) {
        fillOwner(giftRelationInfoVo);
        GiftRelationInfo entity = new GiftRelationInfo();
        BeanUtils.copyProperties(giftRelationInfoVo, entity);
        save(entity);
        giftRelationInfoVo.setId(entity.getId());
        return giftRelationInfoVo;
    }

    @Override
    public Boolean updateGiftRelationInfo(GiftRelationInfoVo giftRelationInfoVo) {
        if (giftRelationInfoVo == null || giftRelationInfoVo.getId() == null) {
            throw GiftExceptions.param("关系ID不能为空");
        }
        GiftRelationInfo existing = getById(giftRelationInfoVo.getId());
        giftDataScopeSupport.assertRelationAccessible(existing);
        giftRelationInfoVo.setUserId(existing.getUserId());
        giftRelationInfoVo.setOrgId(existing.getOrgId());
        GiftRelationInfo entity = new GiftRelationInfo();
        BeanUtils.copyProperties(giftRelationInfoVo, entity);
        return updateById(entity);
    }

    @Override
    public Boolean deleteGiftRelationInfo(String ids) {
        if (!StringUtils.hasText(ids)) {
            return true;
        }
        List<Long> idList = parseIds(ids);
        for (Long id : idList) {
            giftDataScopeSupport.assertRelationAccessible(getById(id));
        }
        return removeBatchByIds(idList);
    }

    private void fillOwner(GiftRelationInfoVo vo) {
        TUserVo loginUser = giftDataScopeSupport.requireLoginUser();
        vo.setUserId(loginUser.getId());
        OrgInfoVo orgInfoVo = loginUser.getOrgInfoVo();
        vo.setOrgId(orgInfoVo == null ? loginUser.getOrgId() : orgInfoVo.getId());
    }

    private List<Long> parseIds(String ids) {
        try {
            return Arrays.stream(ids.split(","))
                    .map(s -> s == null ? "" : s.trim())
                    .filter(StringUtils::hasText)
                    .map(Long::valueOf)
                    .toList();
        } catch (NumberFormatException ex) {
            throw GiftExceptions.param("关系ID格式不合法");
        }
    }

    private GiftRelationInfoVo toVo(GiftRelationInfo entity) {
        if (entity == null) {
            return null;
        }
        GiftRelationInfoVo vo = new GiftRelationInfoVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
