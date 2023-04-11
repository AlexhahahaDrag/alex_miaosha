package com.alex.finance.service.accountRecordInfo;

import com.alex.api.finance.vo.accountRecordInfo.AccountRecordInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.finance.entity.accountRecordInfo.AccountRecordInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import me.chanjar.weixin.common.error.WxErrorException;

import java.util.List;

/**
 *  服务类
 * @author: alex
 * @createDate: 2023-04-08 16:27:39
 * @description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface AccountRecordInfoService extends IService<AccountRecordInfo> {

    Page<AccountRecordInfoVo> getPage(Long pageNum, Long pageSize, AccountRecordInfoVo accountRecordInfoVo);

    AccountRecordInfoVo queryAccountRecordInfo(String id);

    Boolean addAccountRecordInfo(AccountRecordInfoVo accountRecordInfoVo);

    Boolean updateAccountRecordInfo(AccountRecordInfoVo accountRecordInfoVo);

    Boolean deleteAccountRecordInfo(String ids);

    List<AccountRecordInfoVo> queryRemindRecordInfo(Integer dif) throws WxErrorException;
}
