package com.alex.finance.service.finance;

import com.alex.finance.vo.finance.FinanceInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.finance.entity.finance.FinanceInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 财务信息表 服务类
 * @author: majf
 * @createDate: 2022-10-10 18:02:03
 * @description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface FinanceInfoService extends IService<FinanceInfo> {

    Page<FinanceInfoVo> getPage(Long pageNum, Long pageSize, FinanceInfoVo financeInfoVo);

    List<FinanceInfoVo> getList(FinanceInfoVo financeInfoVo);

    FinanceInfoVo queryFinanceInfo(String id);

    FinanceInfo addFinanceInfo(FinanceInfoVo financeInfoVo);

    FinanceInfo updateFinanceInfo(FinanceInfoVo financeInfoVo);

    Boolean deleteFinanceInfo(List<String> ids);
}
