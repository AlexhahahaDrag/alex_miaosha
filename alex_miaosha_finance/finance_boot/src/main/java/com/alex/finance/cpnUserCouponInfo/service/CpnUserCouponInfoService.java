package com.alex.finance.cpnUserCouponInfo.service;

import com.alex.api.finance.cpnUserCouponInfo.vo.CpnUserCouponInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.finance.cpnUserCouponInfo.entity.CpnUserCouponInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 用户消费券库存表 (按数量核销) 服务类
 * @author: alex
 * @createDate: 2025-12-17 17:55:32
 * @description: 我是由代码生成器生成
 * @version: 1.0.0
 */
public interface CpnUserCouponInfoService extends IService<CpnUserCouponInfo> {

    Page<CpnUserCouponInfoVo> getPage(Long pageNum, Long pageSize, CpnUserCouponInfoVo cpnUserCouponInfoVo);

    List<CpnUserCouponInfoVo> getList(CpnUserCouponInfoVo cpnUserCouponInfoVo);

    CpnUserCouponInfoVo queryCpnUserCouponInfo(Long id);

    Boolean addCpnUserCouponInfo(CpnUserCouponInfoVo cpnUserCouponInfoVo);

    Boolean updateCpnUserCouponInfo(CpnUserCouponInfoVo cpnUserCouponInfoVo);

    Boolean deleteCpnUserCouponInfo(String ids);
}
