package com.alex.finance.cpnUserCouponInfo.mapper;

import com.alex.finance.cpnUserCouponInfo.entity.CpnUserCouponInfo;
import com.alex.api.finance.cpnUserCouponInfo.vo.CpnUserCouponInfoVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import com.alex.api.user.annotation.DataPermission;
import java.util.List;

/**
 * @description:  用户消费券库存表 (按数量核销) mapper
 * @author:       alex
 * @createDate:   2025-12-17 17:55:32
 * @version:      1.0.0
 */
@Mapper
public interface CpnUserCouponInfoMapper extends BaseMapper<CpnUserCouponInfo> {

    @DataPermission(table = "cpn_user_coupon_info_t")
    Page<CpnUserCouponInfoVo> getPage(Page<CpnUserCouponInfoVo> page, @Param("cpnUserCouponInfoVo") CpnUserCouponInfoVo cpnUserCouponInfoVo);

    List<CpnUserCouponInfoVo> getList(@Param("cpnUserCouponInfoVo") CpnUserCouponInfoVo cpnUserCouponInfoVo);

    CpnUserCouponInfoVo queryCpnUserCouponInfo(@Param("id") Long id);
}
