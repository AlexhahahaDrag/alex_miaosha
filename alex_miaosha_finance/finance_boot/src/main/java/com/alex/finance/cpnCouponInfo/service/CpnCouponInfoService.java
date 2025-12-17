package com.alex.finance.cpnCouponInfo.service;

import com.alex.api.finance.cpnCouponInfo.vo.CpnCouponInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.finance.cpnCouponInfo.entity.CpnCouponInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 消费券信息表 服务类
 * @author: alex
 * @createDate: 2025-12-17 17:54:42
 * @description: 我是由代码生成器生成
 * @version: 1.0.0
 */
public interface CpnCouponInfoService extends IService<CpnCouponInfo> {

    Page<CpnCouponInfoVo> getPage(Long pageNum, Long pageSize, CpnCouponInfoVo cpnCouponInfoVo);

    List<CpnCouponInfoVo> getList(CpnCouponInfoVo cpnCouponInfoVo);

    CpnCouponInfoVo queryCpnCouponInfo(Long id);

    Boolean addCpnCouponInfo(CpnCouponInfoVo cpnCouponInfoVo);

    Boolean updateCpnCouponInfo(CpnCouponInfoVo cpnCouponInfoVo);

    Boolean deleteCpnCouponInfo(String ids);
}
