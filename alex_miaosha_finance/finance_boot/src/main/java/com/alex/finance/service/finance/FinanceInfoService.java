package com.alex.finance.service.finance;

import com.alex.api.finance.vo.finance.FinanceInfoVo;
import com.alex.finance.entity.finance.FinanceInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 财务信息表 服务类
 * @author: majf
 * @createDate: 2022-10-10 18:02:03
 * @description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface FinanceInfoService extends IService<FinanceInfo> {

    IPage<FinanceInfoVo> getPage(Long pageNum, Long pageSize, FinanceInfoVo financeInfoVo);

    List<FinanceInfoVo> getList(FinanceInfoVo financeInfoVo);

    FinanceInfoVo queryFinanceInfo(String id);

    FinanceInfo addFinanceInfo(FinanceInfoVo financeInfoVo);

    FinanceInfo updateFinanceInfo(FinanceInfoVo financeInfoVo);

    Boolean deleteFinanceInfo(String ids);

    boolean importFinance(MultipartFile file) throws Exception;
}
