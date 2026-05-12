package com.alex.finance.gift.event.service;

import com.alex.api.finance.gift.event.query.GiftEventQuery;
import com.alex.api.finance.gift.event.vo.GiftEventInfoTVo;
import com.alex.finance.gift.event.entity.GiftEventInfoT;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface GiftEventInfoTService extends IService<GiftEventInfoT> {

    Page<GiftEventInfoTVo> getPage(Long pageNum, Long pageSize, GiftEventQuery query);

    List<GiftEventInfoTVo> getList(GiftEventQuery query);

    GiftEventInfoTVo queryGiftEventInfoT(Long id);

    GiftEventInfoTVo addGiftEventInfoT(GiftEventInfoTVo giftEventInfoTVo);

    Boolean updateGiftEventInfoT(GiftEventInfoTVo giftEventInfoTVo);

    Boolean deleteGiftEventInfoT(String ids);
}
