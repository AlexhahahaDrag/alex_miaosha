package com.alex.finance.gift.analysis.service;

import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.api.finance.gift.summary.vo.GiftAmountTrendVo;
import com.alex.api.finance.gift.summary.vo.GiftRankingItemVo;
import com.alex.api.finance.gift.summary.vo.GiftRelationDistributionVo;

import java.util.List;

public interface GiftAnalysisService {

    /**
     * 统计总览。
     *
     * @param direction 可选方向过滤（RECEIVE/GIVE/RETURN，null 或非法值为全部）
     */
    GiftRecordSummaryVo overview(String direction);

    /**
     * 收支趋势。
     *
     * @param period    统计粒度：year 按年，其余按月
     * @param direction 可选方向过滤
     */
    List<GiftAmountTrendVo> trend(String period, String direction);

    List<GiftRelationDistributionVo> relationDistribution();

    List<GiftRankingItemVo> eventRanking();

    List<GiftRankingItemVo> personRanking();
}
