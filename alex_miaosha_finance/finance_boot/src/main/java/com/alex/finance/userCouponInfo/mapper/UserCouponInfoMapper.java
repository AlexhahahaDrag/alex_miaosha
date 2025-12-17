package com.alex.finance.userCouponInfo.mapper;

import com.alex.finance.userCouponInfo.entity.UserCouponInfo;
import com.alex.api.finance.userCouponInfo.vo.UserCouponInfoVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import com.alex.api.user.annotation.DataPermission;
import java.util.List;

/**
 * @description:  用户消费券库存表 (按数量核销) mapper
 * @author:       alex
 * @createDate:   2025-12-17 14:08:13
 * @version:      1.0.0
 */
@Mapper
public interface UserCouponInfoMapper extends BaseMapper<UserCouponInfo> {

    @DataPermission(table = "user_coupon_info_t")
    Page<UserCouponInfoVo> getPage(Page<UserCouponInfoVo> page, @Param("userCouponInfoVo") UserCouponInfoVo userCouponInfoVo);

    List<UserCouponInfoVo> getList(@Param("userCouponInfoVo") UserCouponInfoVo userCouponInfoVo);

    UserCouponInfoVo queryUserCouponInfo(@Param("id") Long id);
}
