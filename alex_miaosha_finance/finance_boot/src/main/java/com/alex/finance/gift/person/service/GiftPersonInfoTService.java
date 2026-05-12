package com.alex.finance.gift.person.service;

import com.alex.api.finance.gift.person.query.GiftPersonQuery;
import com.alex.api.finance.gift.person.vo.GiftPersonInfoTVo;
import com.alex.finance.gift.person.entity.GiftPersonInfoT;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface GiftPersonInfoTService extends IService<GiftPersonInfoT> {

    Page<GiftPersonInfoTVo> getPage(Long pageNum, Long pageSize, GiftPersonQuery query);

    List<GiftPersonInfoTVo> getList(GiftPersonQuery query);

    GiftPersonInfoTVo queryGiftPersonInfoT(Long id);

    GiftPersonInfoTVo addGiftPersonInfoT(GiftPersonInfoTVo giftPersonInfoTVo);

    Boolean updateGiftPersonInfoT(GiftPersonInfoTVo giftPersonInfoTVo);

    Boolean deleteGiftPersonInfoT(String ids);
}
