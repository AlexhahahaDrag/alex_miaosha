package com.alex.finance.gift.relation.mapper;

import com.alex.api.finance.gift.relation.query.GiftRelationQuery;
import com.alex.api.finance.gift.relation.vo.GiftRelationInfoVo;
import com.alex.api.user.annotation.DataPermission;
import com.alex.api.user.annotation.DataPermissionScope;
import com.alex.finance.gift.relation.entity.GiftRelationInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GiftRelationInfoMapper extends BaseMapper<GiftRelationInfo> {

    @DataPermission(table = "gift_relation_info_t", field = "user_id", orgField = "org_id", scope = DataPermissionScope.ORG_SHARED)
    Page<GiftRelationInfoVo> getPage(Page<GiftRelationInfoVo> page, @Param("query") GiftRelationQuery query);

    @DataPermission(table = "gift_relation_info_t", field = "user_id", orgField = "org_id", scope = DataPermissionScope.ORG_SHARED)
    List<GiftRelationInfoVo> getList(@Param("query") GiftRelationQuery query);
}
