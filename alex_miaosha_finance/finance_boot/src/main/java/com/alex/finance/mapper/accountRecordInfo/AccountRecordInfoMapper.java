package com.alex.finance.mapper.accountRecordInfo;

import com.alex.finance.entity.accountRecordInfo.AccountRecordInfo;
import com.alex.api.finance.vo.accountRecordInfo.AccountRecordInfoVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @description:   mapper
 * @author:       alex
 * @createDate:   2023-04-08 16:27:39
 * @version:      1.0.0
 */
@Mapper
public interface AccountRecordInfoMapper extends BaseMapper<AccountRecordInfo> {

    Page<AccountRecordInfoVo> getPage(Page<AccountRecordInfoVo> page, @Param("accountRecordInfoVo") AccountRecordInfoVo accountRecordInfoVo);

    AccountRecordInfoVo queryAccountRecordInfo(@Param("id") String id);

    List<AccountRecordInfoVo> queryRemindRecordInfo(@Param("dif") Integer dif);
}
