package com.alex.finance.personalGift.mapper;

import com.alex.finance.personalGift.entity.PersonalGift;
import com.alex.api.finance.personalGift.vo.PersonalGiftVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import com.alex.api.user.annotation.DataPermission;

/**
 * description:  个人随礼信息表 mapper
 * author:       alex
 * createDate:   2024-07-10 10:01:28
 * version:      1.0.0
 */
@Mapper
public interface PersonalGiftMapper extends BaseMapper<PersonalGift> {

    @DataPermission(table = "t_personal_gift")
    Page<PersonalGiftVo> getPage(Page<PersonalGiftVo> page, @Param("personalGiftVo") PersonalGiftVo personalGiftVo);

    PersonalGiftVo queryPersonalGift(@Param("id") Long id);
}
