package com.alex.finance.couponInfoT.service;

import com.alex.api.finance.couponInfoT.vo.CouponInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.finance.couponInfoT.entity.CouponInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 消费券信息表 服务类
 * @author: alex
 * @createDate: 2025-12-16 15:53:10
 * @description: 我是由代码生成器生成
 * @version: 1.0.0
 */
public interface CouponInfoService extends IService<CouponInfo> {

    Page<CouponInfoVo> getPage(Long pageNum, Long pageSize, CouponInfoVo couponInfoVo);

    List<CouponInfoVo> getList(CouponInfoVo couponInfoVo);

    CouponInfoVo queryCouponInfo(Long id);

    Boolean addCouponInfo(CouponInfoVo couponInfoVo);

    Boolean updateCouponInfo(CouponInfoVo couponInfoVo);

    Boolean deleteCouponInfo(String ids);
}
