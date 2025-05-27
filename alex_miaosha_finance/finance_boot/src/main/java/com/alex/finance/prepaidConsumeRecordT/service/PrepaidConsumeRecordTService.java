package com.alex.finance.prepaidConsumeRecordT.service;

import com.alex.api.finance.prepaidConsumeRecordT.vo.PrepaidConsumeRecordTVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.finance.prepaidConsumeRecordT.entity.PrepaidConsumeRecordT;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 消费卡交易记录表 服务类
 * author: alex
 * createDate: 2025-04-30 08:21:48
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface PrepaidConsumeRecordTService extends IService<PrepaidConsumeRecordT> {

    Page<PrepaidConsumeRecordTVo> getPage(Long pageNum, Long pageSize, PrepaidConsumeRecordTVo prepaidConsumeRecordTVo);

    List<PrepaidConsumeRecordTVo> getList(PrepaidConsumeRecordTVo prepaidConsumeRecordTVo);

    PrepaidConsumeRecordTVo queryPrepaidConsumeRecordT(Long id);

    Boolean addPrepaidConsumeRecordT(PrepaidConsumeRecordTVo prepaidConsumeRecordTVo);

    Boolean updatePrepaidConsumeRecordT(PrepaidConsumeRecordTVo prepaidConsumeRecordTVo);

    Boolean deletePrepaidConsumeRecordT(String ids);
}
