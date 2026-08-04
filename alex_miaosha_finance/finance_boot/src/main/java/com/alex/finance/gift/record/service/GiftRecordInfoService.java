package com.alex.finance.gift.record.service;

import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.api.finance.gift.summary.vo.GiftAmountTrendVo;
import com.alex.finance.gift.record.entity.GiftRecordInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

public interface GiftRecordInfoService extends IService<GiftRecordInfo> {

    Page<GiftRecordInfoVo> getPage(Long pageNum, Long pageSize, GiftRecordQuery query);

    List<GiftRecordInfoVo> getList(GiftRecordQuery query);

    GiftRecordSummaryVo getSummary(GiftRecordQuery query);

    /**
     * 收支趋势 SQL 聚合。
     *
     * @param period    统计粒度：year 按年，其余按月
     * @param direction 可选方向过滤（null / 非法值视为全部）
     */
    List<GiftAmountTrendVo> getTrend(String period, String direction);

    GiftRecordInfoVo queryGiftRecordInfo(Long id);

    GiftRecordInfoVo addGiftRecordInfo(GiftRecordInfoVo giftRecordInfoVo);

    Boolean updateGiftRecordInfo(GiftRecordInfoVo giftRecordInfoVo);

    Boolean deleteGiftRecordInfo(String ids);

    BigDecimal calculatePendingReturnAmount(Long receiveRecordId);

    Boolean markReturned(Long receiveRecordId);

    void exportGiftRecordInfo(GiftRecordQuery query, javax.servlet.http.HttpServletResponse response);
}
