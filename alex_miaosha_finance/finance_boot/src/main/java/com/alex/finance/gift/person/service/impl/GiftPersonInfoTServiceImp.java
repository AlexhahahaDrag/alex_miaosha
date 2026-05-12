package com.alex.finance.gift.person.service.impl;

import com.alex.api.finance.gift.person.query.GiftPersonQuery;
import com.alex.api.finance.gift.person.vo.GiftPersonInfoTVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.person.entity.GiftPersonInfoT;
import com.alex.finance.gift.person.mapper.GiftPersonInfoTMapper;
import com.alex.finance.gift.person.service.GiftPersonInfoTService;
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
public class GiftPersonInfoTServiceImp extends ServiceImpl<GiftPersonInfoTMapper, GiftPersonInfoT> implements GiftPersonInfoTService {

    private final UserUtils userUtils;

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

    private GiftPersonInfoTVo toVo(GiftPersonInfoT entity) {
        if (entity == null) {
            return null;
        }
        GiftPersonInfoTVo vo = new GiftPersonInfoTVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
