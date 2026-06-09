package com.alex.finance.gift.relation.service.impl;

import com.alex.api.finance.gift.relation.query.GiftRelationQuery;
import com.alex.api.finance.gift.relation.vo.GiftRelationInfoTVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.relation.entity.GiftRelationInfoT;
import com.alex.finance.gift.relation.mapper.GiftRelationInfoTMapper;
import com.alex.finance.gift.relation.service.GiftRelationInfoTService;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GiftRelationInfoTServiceImp extends ServiceImpl<GiftRelationInfoTMapper, GiftRelationInfoT> implements GiftRelationInfoTService {

    private final GiftDataScopeSupport giftDataScopeSupport;

    @Override
    public Page<GiftRelationInfoTVo> getPage(Long pageNum, Long pageSize, GiftRelationQuery query) {
        return getBaseMapper().getPage(
                new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize),
                query);
    }

    @Override
    public List<GiftRelationInfoTVo> getList(GiftRelationQuery query) {
        return getBaseMapper().getList(query);
    }

    @Override
    public GiftRelationInfoTVo queryGiftRelationInfoT(Long id) {
        GiftRelationInfoT entity = getById(id);
        giftDataScopeSupport.assertRelationAccessible(entity);
        return toVo(entity);
    }

    @Override
    public GiftRelationInfoTVo addGiftRelationInfoT(GiftRelationInfoTVo giftRelationInfoTVo) {
        fillOwner(giftRelationInfoTVo);
        GiftRelationInfoT entity = new GiftRelationInfoT();
        BeanUtils.copyProperties(giftRelationInfoTVo, entity);
        save(entity);
        giftRelationInfoTVo.setId(entity.getId());
        return giftRelationInfoTVo;
    }

    @Override
    public Boolean updateGiftRelationInfoT(GiftRelationInfoTVo giftRelationInfoTVo) {
        if (giftRelationInfoTVo == null || giftRelationInfoTVo.getId() == null) {
            throw GiftExceptions.param("关系ID不能为空");
        }
        GiftRelationInfoT existing = getById(giftRelationInfoTVo.getId());
        giftDataScopeSupport.assertRelationAccessible(existing);
        giftRelationInfoTVo.setUserId(existing.getUserId());
        giftRelationInfoTVo.setOrgId(existing.getOrgId());
        GiftRelationInfoT entity = new GiftRelationInfoT();
        BeanUtils.copyProperties(giftRelationInfoTVo, entity);
        return updateById(entity);
    }

    @Override
    public Boolean deleteGiftRelationInfoT(String ids) {
        if (!StringUtils.hasText(ids)) {
            return true;
        }
        List<Long> idList = parseIds(ids);
        for (Long id : idList) {
            giftDataScopeSupport.assertRelationAccessible(getById(id));
        }
        return removeBatchByIds(idList);
    }

    private void fillOwner(GiftRelationInfoTVo vo) {
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
            throw GiftExceptions.param("关系ID格式不合法");
        }
    }

    private GiftRelationInfoTVo toVo(GiftRelationInfoT entity) {
        if (entity == null) {
            return null;
        }
        GiftRelationInfoTVo vo = new GiftRelationInfoTVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
