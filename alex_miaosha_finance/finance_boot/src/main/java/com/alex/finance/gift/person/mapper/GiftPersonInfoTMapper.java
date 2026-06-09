package com.alex.finance.gift.person.mapper;

import com.alex.api.finance.gift.person.query.GiftPersonQuery;
import com.alex.api.finance.gift.person.vo.GiftPersonInfoTVo;
import com.alex.api.user.annotation.DataPermission;
import com.alex.finance.gift.person.entity.GiftPersonInfoT;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GiftPersonInfoTMapper extends BaseMapper<GiftPersonInfoT> {

    @DataPermission(table = "gift_person_info_t", field = "user_id")
    Page<GiftPersonInfoTVo> getPage(Page<GiftPersonInfoTVo> page, @Param("query") GiftPersonQuery query);

    @DataPermission(table = "gift_person_info_t", field = "user_id")
    List<GiftPersonInfoTVo> getList(@Param("query") GiftPersonQuery query);

    @DataPermission(table = "gift_person_info_t", field = "user_id")
    List<GiftPersonInfoT> listEntities(@Param("query") GiftPersonQuery query);
}
