package com.alex.finance.gift.person.mapper;

import com.alex.api.finance.gift.person.query.GiftPersonQuery;
import com.alex.api.finance.gift.person.vo.GiftPersonBusinessVo;
import com.alex.api.finance.gift.person.vo.GiftPersonInfoVo;
import com.alex.api.user.annotation.DataPermission;
import com.alex.api.user.annotation.DataPermissionScope;
import com.alex.finance.gift.person.entity.GiftPersonInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GiftPersonInfoMapper extends BaseMapper<GiftPersonInfo> {

    @DataPermission(table = "gift_person_info_t", alias = "p", field = "user_id", orgField = "org_id", scope = DataPermissionScope.ORG_SHARED)
    Page<GiftPersonInfoVo> getPage(@Param("page") Page<GiftPersonInfoVo> page, @Param("query") GiftPersonQuery query);

    @DataPermission(table = "gift_person_info_t", alias = "p", field = "user_id", orgField = "org_id", scope = DataPermissionScope.ORG_SHARED)
    List<GiftPersonInfoVo> getList(@Param("query") GiftPersonQuery query);

    @DataPermission(table = "gift_person_info_t", alias = "p", field = "user_id", orgField = "org_id", scope = DataPermissionScope.ORG_SHARED)
    List<GiftPersonInfo> listEntities(@Param("query") GiftPersonQuery query);

    @DataPermission(table = "gift_person_info_t", alias = "p", field = "user_id", orgField = "org_id", scope = DataPermissionScope.ORG_SHARED)
    Page<GiftPersonBusinessVo> getBusinessPage(Page<GiftPersonBusinessVo> page, @Param("query") GiftPersonQuery query);

    List<String> listDistinctCustomRelationTypes(@Param("userId") Long userId);
}
