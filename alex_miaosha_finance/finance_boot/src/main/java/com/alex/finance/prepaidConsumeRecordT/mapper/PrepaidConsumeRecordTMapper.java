package com.alex.finance.prepaidConsumeRecordT.mapper;

import com.alex.finance.prepaidConsumeRecordT.entity.PrepaidConsumeRecordT;
import com.alex.api.finance.prepaidConsumeRecordT.vo.PrepaidConsumeRecordTVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import com.alex.api.user.annotation.DataPermission;

import java.util.List;

/**
 * description:  消费卡交易记录表 mapper
 * author:       alex
 * createDate:   2025-04-30 08:21:48
 * version:      1.0.0
 */
@Mapper
public interface PrepaidConsumeRecordTMapper extends BaseMapper<PrepaidConsumeRecordT> {

    @DataPermission(table = "prepaid_consume_record_t")
    Page<PrepaidConsumeRecordTVo> getPage(Page<PrepaidConsumeRecordTVo> page, @Param("prepaidConsumeRecordTVo") PrepaidConsumeRecordTVo prepaidConsumeRecordTVo);

    List<PrepaidConsumeRecordTVo> getList(@Param("prepaidConsumeRecordTVo") PrepaidConsumeRecordTVo prepaidConsumeRecordTVo);

    PrepaidConsumeRecordTVo queryPrepaidConsumeRecordT(@Param("id") Long id);
}
