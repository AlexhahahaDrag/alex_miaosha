package com.alex.finance.gift.eventoption.service;

import com.alex.api.finance.gift.event.vo.GiftEventTypeOptionsVo;
import com.alex.finance.gift.eventoption.entity.GiftEventTypeOption;
import com.baomidou.mybatisplus.extension.service.IService;

import com.alex.api.finance.gift.event.vo.GiftRecordRecommendAmountVo;

public interface GiftEventTypeOptionService extends IService<GiftEventTypeOption> {

    GiftEventTypeOptionsVo listEventTypeOptions();

    String resolveEventType(Long eventTypeOptionId, Long orgId);

    Long findEventTypeOptionId(Long orgId, String eventType);

    void rememberCustomEventType(Long orgId, Long userId, String eventType);

    GiftRecordRecommendAmountVo getRecommendAmount(Long personId, String eventType, String direction);
}
