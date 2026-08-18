package com.alex.finance.cpnRedemptionRecordInfo.mapper;

import com.alex.finance.cpnRedemptionRecordInfo.entity.CpnRedemptionRecordInfo;
import com.alex.api.finance.cpnRedemptionRecordInfo.vo.CpnRedemptionRecordInfoVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import com.alex.api.user.annotation.DataPermission;
import java.util.List;

/**
 * @description:  消费券核销记录表 (按数量核销) mapper
 * @author:       alex
 * @createDate:   2025-12-17 17:54:00
 * @version:      1.0.0
 */
@Mapper
public interface CpnRedemptionRecordInfoMapper extends BaseMapper<CpnRedemptionRecordInfo> {

    @DataPermission(table = "cpn_redemption_record_info_t")
    Page<CpnRedemptionRecordInfoVo> getPage(@Param("page") Page<CpnRedemptionRecordInfoVo> page, @Param("cpnRedemptionRecordInfoVo") CpnRedemptionRecordInfoVo cpnRedemptionRecordInfoVo);

    List<CpnRedemptionRecordInfoVo> getList(@Param("cpnRedemptionRecordInfoVo") CpnRedemptionRecordInfoVo cpnRedemptionRecordInfoVo);

    CpnRedemptionRecordInfoVo queryCpnRedemptionRecordInfo(@Param("id") Long id);
}
