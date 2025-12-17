package com.alex.finance.userCouponInfo.service.impl;

import com.alex.api.finance.redemptionRecordInfo.vo.RedemptionRecordInfoVo;
import com.alex.api.finance.userCouponInfo.vo.UserCouponInfoVo;
import com.alex.api.finance.userCouponInfo.vo.UserCouponRedeemReq;
import com.alex.common.utils.string.StringUtils;
import com.alex.finance.redemptionRecordInfo.service.RedemptionRecordInfoService;
import com.alex.finance.userCouponInfo.entity.UserCouponInfo;
import com.alex.finance.userCouponInfo.mapper.UserCouponInfoMapper;
import com.alex.finance.userCouponInfo.service.UserCouponInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * @description:  用户消费券库存表 (按数量核销)服务实现类
 * @author:       alex
 * @createDate:   2025-12-17 14:08:13
 * @version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class UserCouponInfoServiceImp extends ServiceImpl<UserCouponInfoMapper, UserCouponInfo> implements UserCouponInfoService {

    private final UserCouponInfoMapper userCouponInfoMapper;

    /**
     * AI Agent: 核销记录服务（生成历史记录）
     */
    private final RedemptionRecordInfoService redemptionRecordInfoService;

    @Override
    public Page<UserCouponInfoVo> getPage(Long pageNum, Long pageSize, UserCouponInfoVo userCouponInfoVo) {
        Page<UserCouponInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return userCouponInfoMapper.getPage(page, userCouponInfoVo);
    }

    @Override
    public List<UserCouponInfoVo> getList(UserCouponInfoVo userCouponInfoVo) {
        return userCouponInfoMapper.getList(userCouponInfoVo);
    }

    @Override
    public UserCouponInfoVo queryUserCouponInfo(Long id) {
        return userCouponInfoMapper.queryUserCouponInfo(id);
    }

    @Override
    public Boolean addUserCouponInfo(UserCouponInfoVo userCouponInfoVo) {
        UserCouponInfo userCouponInfo = new UserCouponInfo();
        BeanUtils.copyProperties(userCouponInfoVo, userCouponInfo);
        userCouponInfoMapper.insert(userCouponInfo);
        // AI Agent: 插入后回填 ID，便于后续在同一请求链路里引用（例如生成核销历史记录）
        if (userCouponInfoVo != null) {
            userCouponInfoVo.setId(userCouponInfo.getId());
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean redeem(UserCouponRedeemReq req) {
        if (req == null) {
            return false;
        }
        // 1) 新增用户券实例（库存表）
        UserCouponInfoVo userCouponInfoVo = new UserCouponInfoVo();
        userCouponInfoVo.setUserId(req.getUserId());
        userCouponInfoVo.setCouponId(req.getCouponId());
        userCouponInfoVo.setStatus(StringUtils.isEmpty(req.getStatus()) ? "USED" : req.getStatus());
        userCouponInfoVo.setReceiveTime(req.getReceiveTime() == null ? LocalDateTime.now() : req.getReceiveTime());
        userCouponInfoVo.setExpireTime(req.getExpireTime());
        addUserCouponInfo(userCouponInfoVo);

        // 2) 写入核销历史记录
        RedemptionRecordInfoVo recordVo = new RedemptionRecordInfoVo();
        recordVo.setUserCouponId(userCouponInfoVo.getId());
        recordVo.setUserId(req.getUserId());
        recordVo.setOrderId(req.getOrderId());
        recordVo.setMerchantId(req.getMerchantId());
        recordVo.setRedemptionValue(req.getRedemptionValue());
        recordVo.setRedemptionQuantity(req.getRedemptionQuantity() == null ? 1 : req.getRedemptionQuantity());
        recordVo.setRedemptionTime(req.getRedemptionTime() == null ? LocalDateTime.now() : req.getRedemptionTime());
        redemptionRecordInfoService.addRedemptionRecordInfo(recordVo);
        return true;
    }

    @Override
    public Boolean updateUserCouponInfo(UserCouponInfoVo userCouponInfoVo) {
        UserCouponInfo userCouponInfo = new UserCouponInfo();
        BeanUtils.copyProperties(userCouponInfoVo, userCouponInfo);
        userCouponInfoMapper.updateById(userCouponInfo);
        return true;
    }

    @Override
    public Boolean deleteUserCouponInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        userCouponInfoMapper.deleteBatchIds(idArr);
        return true;
    }
}
