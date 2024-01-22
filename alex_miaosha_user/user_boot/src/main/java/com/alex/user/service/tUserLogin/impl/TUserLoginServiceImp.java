package com.alex.user.service.tUserLogin.impl;

import com.alex.user.entity.tUserLogin.TUserLogin;
import com.alex.api.user.vo.tUserLogin.TUserLoginVo;
import com.alex.user.mapper.tUserLogin.TUserLoginMapper;
import com.alex.user.service.tUserLogin.TUserLoginService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import cn.hutool.core.bean.BeanUtil;
import com.alex.common.utils.string.StringUtils;

/**
 * <p>
 * description:  用户登录表服务实现类
 * author:       alex
 * createDate:   2023-02-16 14:11:55
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class TUserLoginServiceImp extends ServiceImpl<TUserLoginMapper, TUserLogin> implements TUserLoginService {

    private final TUserLoginMapper tUserLoginMapper;

    @Override
    public Page<TUserLoginVo> getPage(Long pageNum, Long pageSize, TUserLoginVo tUserLoginVo) {
        Page<TUserLoginVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return tUserLoginMapper.getPage(page, tUserLoginVo);
    }

    @Override
    public TUserLoginVo queryTUserLogin(String id) {
        return tUserLoginMapper.queryTUserLogin(id);
    }

    @Override
    public Boolean addTUserLogin(TUserLoginVo tUserLoginVo) {
        TUserLogin tUserLogin = new TUserLogin();
        BeanUtil.copyProperties(tUserLoginVo, tUserLogin);
        tUserLoginMapper.insert(tUserLogin);
        return true;
    }

    @Override
    public Boolean updateTUserLogin(TUserLoginVo tUserLoginVo) {
        TUserLogin tUserLogin = new TUserLogin();
        BeanUtil.copyProperties(tUserLoginVo, tUserLogin);
        tUserLoginMapper.updateById(tUserLogin);
        return true;
    }

    @Override
    public Boolean deleteTUserLogin(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        tUserLoginMapper.deleteBatchIds(idArr);
        return true;
    }
}
