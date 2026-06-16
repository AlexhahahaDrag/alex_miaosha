package com.alex.finance.gift.record.service;

import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoTVo;
import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.finance.gift.record.entity.GiftRecordInfoT;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

public interface GiftRecordInfoTService extends IService<GiftRecordInfoT> {

    Page<GiftRecordInfoTVo> getPage(Long pageNum, Long pageSize, GiftRecordQuery query);

    List<GiftRecordInfoTVo> getList(GiftRecordQuery query);

    GiftRecordSummaryVo getSummary(GiftRecordQuery query);

    GiftRecordInfoTVo queryGiftRecordInfoT(Long id);

    GiftRecordInfoTVo addGiftRecordInfoT(GiftRecordInfoTVo giftRecordInfoTVo);

    Boolean updateGiftRecordInfoT(GiftRecordInfoTVo giftRecordInfoTVo);

    Boolean deleteGiftRecordInfoT(String ids);

    BigDecimal calculatePendingReturnAmount(Long receiveRecordId);

    Boolean markReturned(Long receiveRecordId);

    void exportGiftRecordInfoT(GiftRecordQuery query, javax.servlet.http.HttpServletResponse response);
}
