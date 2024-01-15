package com.alex.finance.mapper.finance;

import com.alex.api.finance.vo.finance.FinanceInfoVo;
import com.alex.api.user.annotation.DataPermission;
import com.alex.finance.entity.finance.FinanceInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description:  财务信息表 mapper
 * author:       majf
 * createDate:   2022-10-10 18:02:03
 * version:      1.0.0
 */
public interface FinanceInfoMapper extends BaseMapper<FinanceInfo> {

    @DataPermission(table = "finance_info", field = "belong_to")
    IPage<FinanceInfoVo> getPage(IPage<FinanceInfoVo> page, @Param("financeInfoVo") FinanceInfoVo financeInfoVo);

    List<FinanceInfoVo> getList(@Param("financeInfoVo") FinanceInfoVo financeInfoVo);

    FinanceInfoVo queryFinanceInfo(@Param("id") String id);
}
