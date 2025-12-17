package com.alex.finance.redemptionRecordInfo.service;

import com.alex.api.finance.redemptionRecordInfo.vo.RedemptionRecordInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.finance.redemptionRecordInfo.entity.RedemptionRecordInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 消费券核销记录表 (按数量核销) 服务类
 * @author: alex
 * @createDate: 2025-12-17 14:08:55
 * @description: 我是由代码生成器生成
 * @version: 1.0.0
 */
public interface RedemptionRecordInfoService extends IService<RedemptionRecordInfo> {

    Page<RedemptionRecordInfoVo> getPage(Long pageNum, Long pageSize, RedemptionRecordInfoVo redemptionRecordInfoVo);

    List<RedemptionRecordInfoVo> getList(RedemptionRecordInfoVo redemptionRecordInfoVo);

    RedemptionRecordInfoVo queryRedemptionRecordInfo(Long id);

    Boolean addRedemptionRecordInfo(RedemptionRecordInfoVo redemptionRecordInfoVo);

    Boolean updateRedemptionRecordInfo(RedemptionRecordInfoVo redemptionRecordInfoVo);

    Boolean deleteRedemptionRecordInfo(String ids);
}
