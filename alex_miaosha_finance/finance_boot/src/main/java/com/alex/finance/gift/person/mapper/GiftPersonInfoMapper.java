package com.alex.finance.gift.person.mapper;

import com.alex.api.finance.gift.person.query.GiftPersonQuery;
import com.alex.api.finance.gift.person.vo.GiftPersonInfoVo;
import com.alex.api.user.annotation.DataPermission;
import com.alex.finance.gift.person.entity.GiftPersonInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GiftPersonInfoMapper extends BaseMapper<GiftPersonInfo> {

    @DataPermission(table = "gift_person_info_t", field = "user_id")
    Page<GiftPersonInfoVo> getPage(Page<GiftPersonInfoVo> page, @Param("query") GiftPersonQuery query);

    @DataPermission(table = "gift_person_info_t", field = "user_id")
    List<GiftPersonInfoVo> getList(@Param("query") GiftPersonQuery query);

    @DataPermission(table = "gift_person_info_t", field = "user_id")
    List<GiftPersonInfo> listEntities(@Param("query") GiftPersonQuery query);
}
