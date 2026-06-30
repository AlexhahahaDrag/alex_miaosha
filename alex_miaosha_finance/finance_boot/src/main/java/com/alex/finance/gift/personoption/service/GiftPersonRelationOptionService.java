package com.alex.finance.gift.personoption.service;

import com.alex.api.finance.gift.person.vo.GiftPersonRelationOptionsVo;
import com.alex.finance.gift.personoption.entity.GiftPersonRelationOption;
import com.baomidou.mybatisplus.extension.service.IService;

public interface GiftPersonRelationOptionService extends IService<GiftPersonRelationOption> {

    GiftPersonRelationOptionsVo listRelationOptions(Long personId);

    void rememberCustomRelation(Long userId, Long orgId, String relationType);

    String resolveRelationType(Long relationOptionId, Long ownerUserId);

    Long findRelationOptionId(Long userId, String relationType);
}
