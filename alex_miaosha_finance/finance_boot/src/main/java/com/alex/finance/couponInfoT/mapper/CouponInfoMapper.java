package com.alex.finance.couponInfoT.mapper;

import com.alex.finance.couponInfoT.entity.CouponInfo;
import com.alex.api.finance.couponInfoT.vo.CouponInfoVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import com.alex.api.user.annotation.DataPermission;
import java.util.List;

/**
 * @description:  消费券信息表 mapper
 * @author:       alex
 * @createDate:   2025-12-16 15:53:10
 * @version:      1.0.0
 */
@Mapper
public interface CouponInfoMapper extends BaseMapper<CouponInfo> {

    @DataPermission(table = "coupon_info_t")
    Page<CouponInfoVo> getPage(Page<CouponInfoVo> page, @Param("couponInfoVo") CouponInfoVo couponInfoVo);

    List<CouponInfoVo> getList(@Param("couponInfoVo") CouponInfoVo couponInfoVo);

    CouponInfoVo queryCouponInfo(@Param("id") Long id);
}
