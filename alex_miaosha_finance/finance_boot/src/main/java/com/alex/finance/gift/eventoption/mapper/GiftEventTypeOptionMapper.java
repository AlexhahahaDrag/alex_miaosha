package com.alex.finance.gift.eventoption.mapper;

import com.alex.api.finance.gift.event.vo.GiftEventTypeItemVo;
import com.alex.api.finance.gift.event.vo.GiftEventTypeOptionRowVo;
import com.alex.finance.gift.eventoption.entity.GiftEventTypeOption;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GiftEventTypeOptionMapper extends BaseMapper<GiftEventTypeOption> {

    List<GiftEventTypeItemVo> listSystemPresets();

    List<GiftEventTypeOptionRowVo> listEventTypeOptionRows(@Param("orgId") Long orgId);

    Long findOptionIdByEventType(@Param("orgId") Long orgId,
                                 @Param("eventType") String eventType);

    int countSystemByEventCode(@Param("eventCode") String eventCode);
}
