package com.alex.finance.gift.analysis.service;

import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.api.finance.gift.summary.vo.GiftAmountTrendVo;
import com.alex.api.finance.gift.summary.vo.GiftPersonBalanceVo;
import com.alex.api.finance.gift.summary.vo.GiftRankingItemVo;
import com.alex.api.finance.gift.summary.vo.GiftRelationDistributionVo;
import com.alex.api.finance.gift.summary.vo.GiftReturnRecommendationReq;
import com.alex.api.finance.gift.summary.vo.GiftReturnRecommendationVo;

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

    /**
     * 亲友互赠收支平衡矩阵。
     *
     * @param relationType 可选关系类型过滤
     * @param searchKey    可选搜索关键字（亲友姓名）
     * @return 亲友互赠平衡矩阵列表
     */
    List<GiftPersonBalanceVo> balanceMatrix(String relationType, String searchKey);

    /**
     * AI 智能回礼档位测算与推荐。
     *
     * @param req 回礼测算请求
     * @return 推荐回礼档位与贺词
     */
    GiftReturnRecommendationVo recommendReturnGift(GiftReturnRecommendationReq req);

    /**
     * 导出亲友互赠收支平衡矩阵与汇总 Excel 报表。
     *
     * @param relationType 可选关系圈层过滤
     * @param searchKey    可选搜索关键字（亲友姓名）
     * @param response     HTTP 响应
     */
    void exportBalanceMatrix(String relationType, String searchKey, javax.servlet.http.HttpServletResponse response);
}

