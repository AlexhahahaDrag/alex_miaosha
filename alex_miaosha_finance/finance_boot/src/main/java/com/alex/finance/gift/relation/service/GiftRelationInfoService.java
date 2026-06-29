package com.alex.finance.gift.relation.service;

import com.alex.api.finance.gift.relation.query.GiftRelationQuery;
import com.alex.api.finance.gift.relation.vo.GiftRelationInfoVo;
import com.alex.finance.gift.relation.entity.GiftRelationInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface GiftRelationInfoService extends IService<GiftRelationInfo> {

    Page<GiftRelationInfoVo> getPage(Long pageNum, Long pageSize, GiftRelationQuery query);

    List<GiftRelationInfoVo> getList(GiftRelationQuery query);

    GiftRelationInfoVo queryGiftRelationInfo(Long id);

    GiftRelationInfoVo addGiftRelationInfo(GiftRelationInfoVo giftRelationInfoVo);

    Boolean updateGiftRelationInfo(GiftRelationInfoVo giftRelationInfoVo);

    Boolean deleteGiftRelationInfo(String ids);
}
