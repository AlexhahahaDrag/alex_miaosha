package com.alex.finance.gift.relation.service;

import com.alex.api.finance.gift.relation.query.GiftRelationQuery;
import com.alex.api.finance.gift.relation.vo.GiftRelationInfoTVo;
import com.alex.finance.gift.relation.entity.GiftRelationInfoT;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface GiftRelationInfoTService extends IService<GiftRelationInfoT> {

    Page<GiftRelationInfoTVo> getPage(Long pageNum, Long pageSize, GiftRelationQuery query);

    List<GiftRelationInfoTVo> getList(GiftRelationQuery query);

    GiftRelationInfoTVo queryGiftRelationInfoT(Long id);

    GiftRelationInfoTVo addGiftRelationInfoT(GiftRelationInfoTVo giftRelationInfoTVo);

    Boolean updateGiftRelationInfoT(GiftRelationInfoTVo giftRelationInfoTVo);

    Boolean deleteGiftRelationInfoT(String ids);
}
