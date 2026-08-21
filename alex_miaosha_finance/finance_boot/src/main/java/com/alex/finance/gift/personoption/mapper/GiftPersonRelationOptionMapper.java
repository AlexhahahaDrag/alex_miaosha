package com.alex.finance.gift.personoption.mapper;

import com.alex.api.finance.gift.person.vo.GiftPersonRelationItemVo;
import com.alex.api.finance.gift.person.vo.GiftPersonRelationOptionRowVo;
import com.alex.finance.gift.personoption.entity.GiftPersonRelationOption;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GiftPersonRelationOptionMapper extends BaseMapper<GiftPersonRelationOption> {

    List<GiftPersonRelationItemVo> listSystemPresets();

    List<GiftPersonRelationOptionRowVo> listRelationOptionRows(@Param("userId") Long userId,
                                                               @Param("orgId") Long orgId,
                                                               @Param("isSuper") boolean isSuper);

    Long findOptionIdByRelationType(@Param("userId") Long userId,
                                    @Param("orgId") Long orgId,
                                    @Param("isSuper") boolean isSuper,
                                    @Param("relationType") String relationType);

    int countSystemByRelationCode(@Param("relationCode") String relationCode);
}
