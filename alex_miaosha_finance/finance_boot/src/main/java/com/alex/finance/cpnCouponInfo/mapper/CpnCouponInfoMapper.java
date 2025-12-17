package com.alex.finance.cpnCouponInfo.mapper;

import com.alex.finance.cpnCouponInfo.entity.CpnCouponInfo;
import com.alex.api.finance.cpnCouponInfo.vo.CpnCouponInfoVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import com.alex.api.user.annotation.DataPermission;
import java.util.List;

/**
 * @description:  消费券信息表 mapper
 * @author:       alex
 * @createDate:   2025-12-17 17:54:42
 * @version:      1.0.0
 */
@Mapper
public interface CpnCouponInfoMapper extends BaseMapper<CpnCouponInfo> {

    @DataPermission(table = "cpn_coupon_info_t")
    Page<CpnCouponInfoVo> getPage(Page<CpnCouponInfoVo> page, @Param("cpnCouponInfoVo") CpnCouponInfoVo cpnCouponInfoVo);

    List<CpnCouponInfoVo> getList(@Param("cpnCouponInfoVo") CpnCouponInfoVo cpnCouponInfoVo);

    CpnCouponInfoVo queryCpnCouponInfo(@Param("id") Long id);
}
