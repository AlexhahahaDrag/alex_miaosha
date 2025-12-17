package com.alex.finance.couponInfo.mapper;

import com.alex.finance.couponInfo.entity.CouponInfo;
import com.alex.api.finance.couponInfo.vo.CouponInfoVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import com.alex.api.user.annotation.DataPermission;
import java.util.List;

/**
 * @description:  消费券信息表 mapper
 * @author:       alex
 * @createDate:   2025-12-17 11:43:14
 * @version:      1.0.0
 */
@Mapper
public interface CouponInfoMapper extends BaseMapper<CouponInfo> {

    @DataPermission(table = "coupon_info_t")
    Page<CouponInfoVo> getPage(Page<CouponInfoVo> page, @Param("couponInfoVo") CouponInfoVo couponInfoVo);

    List<CouponInfoVo> getList(@Param("couponInfoVo") CouponInfoVo couponInfoVo);

    CouponInfoVo queryCouponInfo(@Param("id") Long id);
}
