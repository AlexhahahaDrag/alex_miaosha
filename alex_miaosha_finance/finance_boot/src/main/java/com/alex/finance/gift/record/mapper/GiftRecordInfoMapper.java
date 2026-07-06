package com.alex.finance.gift.record.mapper;

import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.api.user.annotation.DataPermission;
import com.alex.api.user.annotation.DataPermissionScope;
import com.alex.finance.gift.record.entity.GiftRecordInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface GiftRecordInfoMapper extends BaseMapper<GiftRecordInfo> {

    @DataPermission(table = "gift_record_info_t", field = "user_id", orgField = "org_id", scope = DataPermissionScope.ORG_SHARED)
    Page<GiftRecordInfoVo> getPage(@Param("page") Page<GiftRecordInfoVo> page, @Param("query") GiftRecordQuery query);

    @DataPermission(table = "gift_record_info_t", field = "user_id", orgField = "org_id", scope = DataPermissionScope.ORG_SHARED)
    List<GiftRecordInfoVo> getList(@Param("query") GiftRecordQuery query);

    @DataPermission(table = "gift_record_info_t", field = "user_id", orgField = "org_id", scope = DataPermissionScope.ORG_SHARED)
    List<GiftRecordInfo> listEntities(@Param("query") GiftRecordQuery query);

    BigDecimal sumReturnAmountByRelatedRecordId(@Param("relatedRecordId") Long relatedRecordId);
}
