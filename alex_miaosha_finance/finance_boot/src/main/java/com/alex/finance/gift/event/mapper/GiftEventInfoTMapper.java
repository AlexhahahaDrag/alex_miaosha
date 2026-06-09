package com.alex.finance.gift.event.mapper;

import com.alex.api.finance.gift.event.query.GiftEventQuery;
import com.alex.api.finance.gift.event.vo.GiftEventInfoTVo;
import com.alex.api.user.annotation.DataPermission;
import com.alex.finance.gift.event.entity.GiftEventInfoT;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GiftEventInfoTMapper extends BaseMapper<GiftEventInfoT> {

    @DataPermission(table = "gift_event_info_t", field = "user_id")
    Page<GiftEventInfoTVo> getPage(Page<GiftEventInfoTVo> page, @Param("query") GiftEventQuery query);

    @DataPermission(table = "gift_event_info_t", field = "user_id")
    List<GiftEventInfoTVo> getList(@Param("query") GiftEventQuery query);

    @DataPermission(table = "gift_event_info_t", field = "user_id")
    List<GiftEventInfoT> listEntities(@Param("query") GiftEventQuery query);
}
