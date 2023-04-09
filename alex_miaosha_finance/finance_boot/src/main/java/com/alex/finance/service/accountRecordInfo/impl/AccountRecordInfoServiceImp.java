package com.alex.finance.service.accountRecordInfo.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alex.api.finance.vo.accountRecordInfo.AccountRecordInfoVo;
import com.alex.common.utils.string.StringUtils;
import com.alex.finance.entity.accountRecordInfo.AccountRecordInfo;
import com.alex.finance.mapper.accountRecordInfo.AccountRecordInfoMapper;
import com.alex.finance.service.accountRecordInfo.AccountRecordInfoService;
import com.alex.finance.utils.MemoryDayUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

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
    public List<AccountRecordInfoVo> queryRemindRecordInfo(Integer dif) {
        if (dif == null) {
            dif = difDay;
        }
        List<AccountRecordInfoVo> list = accountRecordInfoMapper.queryRemindRecordInfo(dif);
        //发送微信消息提醒
//        String rainbow = RainbowUtil.getRainbow();
//        Weather weather = WeatherUtil.getWeather();
        long l = MemoryDayUtil.calculationBirthday("1991-11.25");
        return list;
    }
}
