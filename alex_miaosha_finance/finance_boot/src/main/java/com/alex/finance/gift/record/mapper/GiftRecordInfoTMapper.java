package com.alex.finance.gift.record.mapper;

import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoTVo;
import com.alex.api.user.annotation.DataPermission;
import com.alex.finance.gift.record.entity.GiftRecordInfoT;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GiftRecordInfoTMapper extends BaseMapper<GiftRecordInfoT> {

    @DataPermission(table = "gift_record_info_t", field = "org_id")
    Page<GiftRecordInfoTVo> getPage(Page<GiftRecordInfoTVo> page, @Param("query") GiftRecordQuery query);
}
