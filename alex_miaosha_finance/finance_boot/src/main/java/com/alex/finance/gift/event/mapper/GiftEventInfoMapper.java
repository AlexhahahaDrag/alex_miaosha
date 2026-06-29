package com.alex.finance.gift.event.mapper;

import com.alex.api.finance.gift.event.query.GiftEventQuery;
import com.alex.api.finance.gift.event.vo.GiftEventInfoVo;
import com.alex.api.user.annotation.DataPermission;
import com.alex.finance.gift.event.entity.GiftEventInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GiftEventInfoMapper extends BaseMapper<GiftEventInfo> {

    @DataPermission(table = "gift_event_info_t", field = "user_id")
    Page<GiftEventInfoVo> getPage(Page<GiftEventInfoVo> page, @Param("query") GiftEventQuery query);

    @DataPermission(table = "gift_event_info_t", field = "user_id")
    List<GiftEventInfoVo> getList(@Param("query") GiftEventQuery query);

    @DataPermission(table = "gift_event_info_t", field = "user_id")
    List<GiftEventInfo> listEntities(@Param("query") GiftEventQuery query);
}
