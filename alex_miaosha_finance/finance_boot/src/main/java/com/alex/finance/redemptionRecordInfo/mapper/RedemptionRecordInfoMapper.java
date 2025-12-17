package com.alex.finance.redemptionRecordInfo.mapper;

import com.alex.finance.redemptionRecordInfo.entity.RedemptionRecordInfo;
import com.alex.api.finance.redemptionRecordInfo.vo.RedemptionRecordInfoVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import com.alex.api.user.annotation.DataPermission;
import java.util.List;

/**
 * @description:  消费券核销记录表 (按数量核销) mapper
 * @author:       alex
 * @createDate:   2025-12-17 14:08:55
 * @version:      1.0.0
 */
@Mapper
public interface RedemptionRecordInfoMapper extends BaseMapper<RedemptionRecordInfo> {

    @DataPermission(table = "redemption_record_info_t")
    Page<RedemptionRecordInfoVo> getPage(Page<RedemptionRecordInfoVo> page, @Param("redemptionRecordInfoVo") RedemptionRecordInfoVo redemptionRecordInfoVo);

    List<RedemptionRecordInfoVo> getList(@Param("redemptionRecordInfoVo") RedemptionRecordInfoVo redemptionRecordInfoVo);

    RedemptionRecordInfoVo queryRedemptionRecordInfo(@Param("id") Long id);
}
