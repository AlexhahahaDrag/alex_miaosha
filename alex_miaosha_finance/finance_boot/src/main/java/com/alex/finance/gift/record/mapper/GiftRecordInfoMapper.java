package com.alex.finance.gift.record.mapper;

import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.api.finance.gift.summary.vo.GiftAmountTrendVo;
import com.alex.api.finance.gift.summary.vo.GiftDirectionAggVo;
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

    @DataPermission(table = "gift_record_info_t", alias = "r", field = "user_id", orgField = "org_id", scope = DataPermissionScope.ORG_SHARED)
    Page<GiftRecordInfoVo> getPage(@Param("page") Page<GiftRecordInfoVo> page, @Param("query") GiftRecordQuery query);

    @DataPermission(table = "gift_record_info_t", alias = "r", field = "user_id", orgField = "org_id", scope = DataPermissionScope.ORG_SHARED)
    List<GiftRecordInfoVo> getList(@Param("query") GiftRecordQuery query);

    @DataPermission(table = "gift_record_info_t", alias = "r", field = "user_id", orgField = "org_id", scope = DataPermissionScope.ORG_SHARED)
    List<GiftRecordInfo> listEntities(@Param("query") GiftRecordQuery query);

    /** 该查询在 XML 中不带表别名，数据权限按表名限定列（gift_record_info_t.org_id） */
    @DataPermission(table = "gift_record_info_t", field = "user_id", orgField = "org_id", scope = DataPermissionScope.ORG_SHARED)
    BigDecimal sumReturnAmountByRelatedRecordId(@Param("relatedRecordId") Long relatedRecordId);

    /** 按方向 SQL 聚合金额与笔数（summary / analysis overview 用，避免全量拉记录内存聚合） */
    @DataPermission(table = "gift_record_info_t", alias = "r", field = "user_id", orgField = "org_id", scope = DataPermissionScope.ORG_SHARED)
    List<GiftDirectionAggVo> sumDirectionAgg(@Param("query") GiftRecordQuery query);

    /**
     * 按时间粒度 SQL 聚合收支趋势。
     *
     * @param dateFormat MySQL DATE_FORMAT 格式串：月度 %Y-%m / 年度 %Y
     * @param direction  可选方向过滤（null 表示全部往来）
     */
    @DataPermission(table = "gift_record_info_t", alias = "r", field = "user_id", orgField = "org_id", scope = DataPermissionScope.ORG_SHARED)
    List<GiftAmountTrendVo> sumTrendAgg(@Param("dateFormat") String dateFormat, @Param("direction") String direction);
}
