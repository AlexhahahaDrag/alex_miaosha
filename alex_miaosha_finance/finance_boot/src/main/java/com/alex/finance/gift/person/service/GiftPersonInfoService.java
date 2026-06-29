package com.alex.finance.gift.person.service;

import com.alex.api.finance.gift.person.query.GiftPersonQuery;
import com.alex.api.finance.gift.person.vo.GiftPersonBusinessVo;
import com.alex.api.finance.gift.person.vo.GiftPersonInfoVo;
import com.alex.api.finance.gift.person.vo.GiftPersonProfileVo;
import com.alex.api.finance.gift.person.vo.GiftPersonSummaryVo;
import com.alex.finance.gift.person.entity.GiftPersonInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface GiftPersonInfoService extends IService<GiftPersonInfo> {

    Page<GiftPersonInfoVo> getPage(Long pageNum, Long pageSize, GiftPersonQuery query);

    List<GiftPersonInfoVo> getList(GiftPersonQuery query);

    GiftPersonSummaryVo getSummary();

    Page<GiftPersonBusinessVo> getBusinessPage(Long pageNum, Long pageSize, GiftPersonQuery query);

    GiftPersonProfileVo getProfile(Long id);

    GiftPersonInfoVo queryGiftPersonInfo(Long id);

    GiftPersonInfoVo addGiftPersonInfo(GiftPersonInfoVo giftPersonInfoVo);

    Boolean updateGiftPersonInfo(GiftPersonInfoVo giftPersonInfoVo);

    Boolean deleteGiftPersonInfo(String ids);
}
