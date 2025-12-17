package com.alex.finance.cpnRedemptionRecordInfo.service;

import com.alex.api.finance.cpnRedemptionRecordInfo.vo.CpnRedemptionRecordInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.finance.cpnRedemptionRecordInfo.entity.CpnRedemptionRecordInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 消费券核销记录表 (按数量核销) 服务类
 * @author: alex
 * @createDate: 2025-12-17 17:54:00
 * @description: 我是由代码生成器生成
 * @version: 1.0.0
 */
public interface CpnRedemptionRecordInfoService extends IService<CpnRedemptionRecordInfo> {

    Page<CpnRedemptionRecordInfoVo> getPage(Long pageNum, Long pageSize, CpnRedemptionRecordInfoVo cpnRedemptionRecordInfoVo);

    List<CpnRedemptionRecordInfoVo> getList(CpnRedemptionRecordInfoVo cpnRedemptionRecordInfoVo);

    CpnRedemptionRecordInfoVo queryCpnRedemptionRecordInfo(Long id);

    Boolean addCpnRedemptionRecordInfo(CpnRedemptionRecordInfoVo cpnRedemptionRecordInfoVo);

    Boolean updateCpnRedemptionRecordInfo(CpnRedemptionRecordInfoVo cpnRedemptionRecordInfoVo);

    Boolean deleteCpnRedemptionRecordInfo(String ids);
}
