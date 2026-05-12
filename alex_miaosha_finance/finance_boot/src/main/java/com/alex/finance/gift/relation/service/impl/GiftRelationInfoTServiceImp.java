package com.alex.finance.gift.relation.service.impl;

import com.alex.api.finance.gift.relation.query.GiftRelationQuery;
import com.alex.api.finance.gift.relation.vo.GiftRelationInfoTVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.relation.entity.GiftRelationInfoT;
import com.alex.finance.gift.relation.mapper.GiftRelationInfoTMapper;
import com.alex.finance.gift.relation.service.GiftRelationInfoTService;
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

    private final UserUtils userUtils;

    @Override
    public Page<GiftRelationInfoTVo> getPage(Long pageNum, Long pageSize, GiftRelationQuery query) {
        Page<GiftRelationInfoT> entityPage = page(new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize), queryWrapper(query));
        Page<GiftRelationInfoTVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public List<GiftRelationInfoTVo> getList(GiftRelationQuery query) {
        return list(queryWrapper(query)).stream().map(this::toVo).collect(Collectors.toList());
    }

    @Override
    public GiftRelationInfoTVo queryGiftRelationInfoT(Long id) {
        return toVo(getById(id));
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
        GiftRelationInfoT entity = new GiftRelationInfoT();
        BeanUtils.copyProperties(giftRelationInfoTVo, entity);
        return updateById(entity);
    }

    @Override
    public Boolean deleteGiftRelationInfoT(String ids) {
        if (!StringUtils.hasText(ids)) {
            return true;
        }
        return removeBatchByIds(Arrays.asList(ids.split(",")));
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GiftRelationInfoT> queryWrapper(GiftRelationQuery query) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GiftRelationInfoT> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (query == null) {
            return wrapper.orderByDesc(GiftRelationInfoT::getCreateTime);
        }
        wrapper.eq(query.getPersonId() != null, GiftRelationInfoT::getPersonId, query.getPersonId());
        wrapper.eq(query.getRelationPersonId() != null, GiftRelationInfoT::getRelationPersonId, query.getRelationPersonId());
        wrapper.eq(StringUtils.hasText(query.getRelationType()), GiftRelationInfoT::getRelationType, query.getRelationType());
        return wrapper.orderByDesc(GiftRelationInfoT::getCreateTime);
    }

    private void fillOwner(GiftRelationInfoTVo vo) {
        TUserVo loginUser = userUtils == null ? null : userUtils.getLoginUser();
        if (loginUser == null) {
            return;
        }
        vo.setUserId(loginUser.getId());
        vo.setOrgId(loginUser.getOrgInfoVo() == null ? loginUser.getOrgId() : loginUser.getOrgInfoVo().getId());
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
