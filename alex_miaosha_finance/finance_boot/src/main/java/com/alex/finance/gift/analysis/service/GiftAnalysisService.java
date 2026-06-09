package com.alex.finance.gift.analysis.service;

import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.api.finance.gift.summary.vo.GiftAmountTrendVo;
import com.alex.api.finance.gift.summary.vo.GiftRankingItemVo;
import com.alex.api.finance.gift.summary.vo.GiftRelationDistributionVo;

import java.util.List;

public interface GiftAnalysisService {

    GiftRecordSummaryVo overview();

    List<GiftAmountTrendVo> trend();

    List<GiftRelationDistributionVo> relationDistribution();

    List<GiftRankingItemVo> eventRanking();

    List<GiftRankingItemVo> personRanking();
}
