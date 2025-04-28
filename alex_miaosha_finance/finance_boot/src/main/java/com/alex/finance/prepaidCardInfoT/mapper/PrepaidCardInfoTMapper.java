package com.alex.finance.prepaidCardInfoT.mapper;

import com.alex.finance.prepaidCardInfoT.entity.PrepaidCardInfoT;
import com.alex.api.finance.prepaidCardInfoT.vo.PrepaidCardInfoTVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import com.alex.api.user.annotation.DataPermission;

/**
 * description:  消费卡信息表 mapper
 * author:       alex
 * createDate:   2025-04-28 20:58:16
 * version:      1.0.0
 */
@Mapper
public interface PrepaidCardInfoTMapper extends BaseMapper<PrepaidCardInfoT> {

    @DataPermission(table = "prepaid_card_info_t")
    Page<PrepaidCardInfoTVo> getPage(Page<PrepaidCardInfoTVo> page, @Param("prepaidCardInfoTVo") PrepaidCardInfoTVo prepaidCardInfoTVo);

    List<PrepaidCardInfoTVo> getList(@Param("prepaidCardInfoTVo") PrepaidCardInfoTVo prepaidCardInfoTVo);

    PrepaidCardInfoTVo queryPrepaidCardInfoT(@Param("id") Long id);
}
