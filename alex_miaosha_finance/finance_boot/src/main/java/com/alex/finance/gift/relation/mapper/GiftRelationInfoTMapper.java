package com.alex.finance.gift.relation.mapper;

import com.alex.api.finance.gift.relation.query.GiftRelationQuery;
import com.alex.api.finance.gift.relation.vo.GiftRelationInfoTVo;
import com.alex.api.user.annotation.DataPermission;
import com.alex.finance.gift.relation.entity.GiftRelationInfoT;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GiftRelationInfoTMapper extends BaseMapper<GiftRelationInfoT> {

    @DataPermission(table = "gift_relation_info_t", field = "user_id")
    Page<GiftRelationInfoTVo> getPage(Page<GiftRelationInfoTVo> page, @Param("query") GiftRelationQuery query);

    @DataPermission(table = "gift_relation_info_t", field = "user_id")
    List<GiftRelationInfoTVo> getList(@Param("query") GiftRelationQuery query);
}
