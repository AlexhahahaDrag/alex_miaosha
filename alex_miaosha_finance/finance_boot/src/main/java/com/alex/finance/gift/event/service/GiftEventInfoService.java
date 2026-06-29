package com.alex.finance.gift.event.service;

import com.alex.api.finance.gift.event.query.GiftEventQuery;
import com.alex.api.finance.gift.event.vo.GiftEventBusinessVo;
import com.alex.api.finance.gift.event.vo.GiftEventInfoVo;
import com.alex.api.finance.gift.event.vo.GiftEventSummaryVo;
import com.alex.finance.gift.event.entity.GiftEventInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface GiftEventInfoService extends IService<GiftEventInfo> {

    Page<GiftEventInfoVo> getPage(Long pageNum, Long pageSize, GiftEventQuery query);

    List<GiftEventInfoVo> getList(GiftEventQuery query);

    GiftEventSummaryVo getSummary();

    Page<GiftEventBusinessVo> getBusinessPage(Long pageNum, Long pageSize, GiftEventQuery query);

    GiftEventInfoVo queryGiftEventInfo(Long id);

    GiftEventInfoVo addGiftEventInfo(GiftEventInfoVo giftEventInfoVo);

    Boolean updateGiftEventInfo(GiftEventInfoVo giftEventInfoVo);

    Boolean deleteGiftEventInfo(String ids);
}
