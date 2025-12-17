package com.alex.finance.userCouponInfo.service;

import com.alex.api.finance.userCouponInfo.vo.UserCouponInfoVo;
import com.alex.api.finance.userCouponInfo.vo.UserCouponRedeemReq;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.finance.userCouponInfo.entity.UserCouponInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 用户消费券库存表 (按数量核销) 服务类
 * @author: alex
 * @createDate: 2025-12-17 14:08:13
 * @description: 我是由代码生成器生成
 * @version: 1.0.0
 */
public interface UserCouponInfoService extends IService<UserCouponInfo> {

    Page<UserCouponInfoVo> getPage(Long pageNum, Long pageSize, UserCouponInfoVo userCouponInfoVo);

    List<UserCouponInfoVo> getList(UserCouponInfoVo userCouponInfoVo);

    UserCouponInfoVo queryUserCouponInfo(Long id);

    Boolean addUserCouponInfo(UserCouponInfoVo userCouponInfoVo);

    /**
     * AI Agent: 核销消费券（按数量核销）：新增用户券实例并生成核销历史记录
     */
    Boolean redeem(UserCouponRedeemReq req);

    Boolean updateUserCouponInfo(UserCouponInfoVo userCouponInfoVo);

    Boolean deleteUserCouponInfo(String ids);
}
