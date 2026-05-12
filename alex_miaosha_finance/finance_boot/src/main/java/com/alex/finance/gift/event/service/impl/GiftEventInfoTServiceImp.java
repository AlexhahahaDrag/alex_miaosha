package com.alex.finance.gift.event.service.impl;

import com.alex.api.finance.gift.event.query.GiftEventQuery;
import com.alex.api.finance.gift.event.vo.GiftEventInfoTVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.event.entity.GiftEventInfoT;
import com.alex.finance.gift.event.mapper.GiftEventInfoTMapper;
import com.alex.finance.gift.event.service.GiftEventInfoTService;
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
public class GiftEventInfoTServiceImp extends ServiceImpl<GiftEventInfoTMapper, GiftEventInfoT> implements GiftEventInfoTService {

    private final UserUtils userUtils;

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
        return removeBatchByIds(Arrays.asList(ids.split(",")));
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
