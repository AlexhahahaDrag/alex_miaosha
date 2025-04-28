package com.alex.finance.prepaidCardInfoT.service;

import com.alex.api.finance.prepaidCardInfoT.vo.PrepaidCardInfoTVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.finance.prepaidCardInfoT.entity.PrepaidCardInfoT;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 消费卡信息表 服务类
 * author: alex
 * createDate: 2025-04-28 20:58:16
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface PrepaidCardInfoTService extends IService<PrepaidCardInfoT> {

    Page<PrepaidCardInfoTVo> getPage(Long pageNum, Long pageSize, PrepaidCardInfoTVo prepaidCardInfoTVo);

    List<PrepaidCardInfoTVo> getList(PrepaidCardInfoTVo prepaidCardInfoTVo);

    PrepaidCardInfoTVo queryPrepaidCardInfoT(Long id);

    Boolean addPrepaidCardInfoT(PrepaidCardInfoTVo prepaidCardInfoTVo);

    Boolean updatePrepaidCardInfoT(PrepaidCardInfoTVo prepaidCardInfoTVo);

    Boolean deletePrepaidCardInfoT(String ids);
}
