package com.alex.finance.service.accountRecordInfo.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alex.api.finance.vo.accountRecordInfo.AccountCountInfoVo;
import com.alex.api.finance.vo.accountRecordInfo.AccountRecordInfoVo;
import com.alex.common.utils.string.StringUtils;
import com.alex.finance.entity.accountRecordInfo.AccountRecordInfo;
import com.alex.finance.mapper.accountRecordInfo.AccountRecordInfoMapper;
import com.alex.finance.service.accountRecordInfo.AccountRecordInfoService;
import com.alex.finance.service.weixin.WeiXinService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * @description:  服务实现类
 * @author:       alex
 * @createDate:   2023-04-08 16:27:39
 * @version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class AccountRecordInfoServiceImp extends ServiceImpl<AccountRecordInfoMapper, AccountRecordInfo> implements AccountRecordInfoService {

    private final AccountRecordInfoMapper accountRecordInfoMapper;

    @Value("${accountNotice.difDay}")
    private Integer difDay;

    private final WeiXinService weiXinService;

    @Override
    public Page<AccountRecordInfoVo> getPage(Long pageNum, Long pageSize, AccountRecordInfoVo accountRecordInfoVo) {
        Page<AccountRecordInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return accountRecordInfoMapper.getPage(page, accountRecordInfoVo);
    }

    @Override
    public AccountRecordInfoVo queryAccountRecordInfo(String id) {
        return accountRecordInfoMapper.queryAccountRecordInfo(id);
    }

    @Override
    public Boolean addAccountRecordInfo(AccountRecordInfoVo accountRecordInfoVo) {
        AccountRecordInfo accountRecordInfo = new AccountRecordInfo();
        BeanUtil.copyProperties(accountRecordInfoVo, accountRecordInfo);
        accountRecordInfoMapper.insert(accountRecordInfo);
        return true;
    }

    @Override
    public Boolean updateAccountRecordInfo(AccountRecordInfoVo accountRecordInfoVo) {
        AccountRecordInfo accountRecordInfo = new AccountRecordInfo();
        BeanUtil.copyProperties(accountRecordInfoVo, accountRecordInfo);
        accountRecordInfoMapper.updateById(accountRecordInfo);
        return true;
    }

    @Override
    public Boolean deleteAccountRecordInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        accountRecordInfoMapper.deleteBatchIds(idArr);
        return true;
    }

    @Override
    public List<AccountCountInfoVo> queryRemindRecordInfo(Integer dif) throws WxErrorException {
        if (dif == null) {
            dif = difDay;
        }
        List<AccountCountInfoVo> list = accountRecordInfoMapper.queryRemindRecordInfo(dif);
        if (list == null || list.isEmpty()) {
            return null;
        }
        //发送微信消息提醒
        for(AccountCountInfoVo cur : list) {
            weiXinService.sentMessage(cur.getAccountName(), cur.getNum());
        }
        return list;
    }
}
