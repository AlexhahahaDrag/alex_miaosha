package com.alex.finance.finance.service;

import com.alex.api.finance.vo.finance.UniversalFinanceAiParseReq;
import com.alex.api.finance.vo.finance.UniversalFinanceAiParseVo;

/**
 * description: 通用智能记账服务 (日常收支、购物小票明细、预付卡流水解析)
 * author: alex
 * createDate: 2026-10-19
 * version: 1.0.0
 */
public interface UniversalFinanceAiService {

    /**
     * 解析自然语言、小票或预付卡流水文本，返回结构化记账结果
     *
     * @param req 解析请求
     * @return 解析后的结构化记账 VO
     */
    UniversalFinanceAiParseVo parseLedger(UniversalFinanceAiParseReq req);
}
