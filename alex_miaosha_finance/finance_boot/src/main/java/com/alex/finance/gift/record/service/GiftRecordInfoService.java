package com.alex.finance.gift.record.service;

import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.finance.gift.record.entity.GiftRecordInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

public interface GiftRecordInfoService extends IService<GiftRecordInfo> {

    Page<GiftRecordInfoVo> getPage(Long pageNum, Long pageSize, GiftRecordQuery query);

    List<GiftRecordInfoVo> getList(GiftRecordQuery query);

    GiftRecordSummaryVo getSummary(GiftRecordQuery query);

    GiftRecordInfoVo queryGiftRecordInfo(Long id);

    GiftRecordInfoVo addGiftRecordInfo(GiftRecordInfoVo giftRecordInfoVo);

    Boolean updateGiftRecordInfo(GiftRecordInfoVo giftRecordInfoVo);

    Boolean deleteGiftRecordInfo(String ids);

    BigDecimal calculatePendingReturnAmount(Long receiveRecordId);

    Boolean markReturned(Long receiveRecordId);

    void exportGiftRecordInfo(GiftRecordQuery query, javax.servlet.http.HttpServletResponse response);
}
