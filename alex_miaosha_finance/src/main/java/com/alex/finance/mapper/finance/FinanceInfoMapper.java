package com.alex.finance.mapper.finance;

import com.alex.finance.entity.finance.FinanceInfo;
import com.alex.finance.vo.finance.FinanceInfoVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description:  财务信息表 mapper
 * @author:       majf
 * @createDate:   2022-10-10 16:56:00
 * @version:      1.0.0
 */
@Mapper
public interface FinanceInfoMapper extends BaseMapper<FinanceInfo> {

    Page<FinanceInfoVo> getPage(Page<FinanceInfoVo> page, @Param("financeInfoVo") FinanceInfoVo financeInfoVo);

    List<FinanceInfoVo> getList(@Param("financeInfoVo") FinanceInfoVo financeInfoVo);

    FinanceInfoVo queryFinanceInfo(@Param("id") String id);
}
