package com.alex.finance.cpnUserCouponInfo.service.impl;

import com.alex.api.finance.cpnCouponInfo.vo.CpnCouponInfoVo;
import com.alex.api.finance.cpnUserCouponInfo.vo.CpnUserCouponInfoVo;
import com.alex.api.finance.cpnUserCouponInfo.vo.CpnUserCouponRedeemReq;
import com.alex.common.enums.CpnUserCouponStatusEnum;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.FinanceException;
import com.alex.common.utils.string.StringUtils;
import com.alex.finance.cpnCouponInfo.mapper.CpnCouponInfoMapper;
import com.alex.finance.cpnRedemptionRecordInfo.entity.CpnRedemptionRecordInfo;
import com.alex.finance.cpnRedemptionRecordInfo.service.CpnRedemptionRecordInfoService;
import com.alex.finance.cpnUserCouponInfo.entity.CpnUserCouponInfo;
import com.alex.finance.cpnUserCouponInfo.mapper.CpnUserCouponInfoMapper;
import com.alex.finance.cpnUserCouponInfo.service.CpnUserCouponInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * <p>
 *
 * @description: 用户消费券库存表 (按数量核销)服务实现类
 * @author: alex
 * @createDate: 2025-12-17 17:55:32
 * @version: 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CpnUserCouponInfoServiceImp extends ServiceImpl<CpnUserCouponInfoMapper, CpnUserCouponInfo> implements CpnUserCouponInfoService {

    private final CpnUserCouponInfoMapper cpnUserCouponInfoMapper;

    private final CpnRedemptionRecordInfoService cpnRedemptionRecordInfoService;

    private final CpnCouponInfoMapper cpnCouponInfoMapper;

    private final Executor taskExecutor;

    @Override
    public Page<CpnUserCouponInfoVo> getPage(Long pageNum, Long pageSize, CpnUserCouponInfoVo cpnUserCouponInfoVo) {
        Page<CpnUserCouponInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return cpnUserCouponInfoMapper.getPage(page, cpnUserCouponInfoVo);
    }

    @Override
    public List<CpnUserCouponInfoVo> getList(CpnUserCouponInfoVo cpnUserCouponInfoVo) {
        return cpnUserCouponInfoMapper.getList(cpnUserCouponInfoVo);
    }

    @Override
    public CpnUserCouponInfoVo queryCpnUserCouponInfo(Long id) {
        return cpnUserCouponInfoMapper.queryCpnUserCouponInfo(id);
    }

    @Override
    public Boolean addCpnUserCouponInfo(CpnUserCouponInfoVo cpnUserCouponInfoVo) {
        CpnUserCouponInfo cpnUserCouponInfo = new CpnUserCouponInfo();
        BeanUtils.copyProperties(cpnUserCouponInfoVo, cpnUserCouponInfo);
        cpnUserCouponInfoMapper.insert(cpnUserCouponInfo);
        cpnUserCouponInfoVo.setId(cpnUserCouponInfo.getId());
        return true;
    }

    @Override
    public Boolean updateCpnUserCouponInfo(CpnUserCouponInfoVo cpnUserCouponInfoVo) {
        CpnUserCouponInfo cpnUserCouponInfo = new CpnUserCouponInfo();
        BeanUtils.copyProperties(cpnUserCouponInfoVo, cpnUserCouponInfo);
        cpnUserCouponInfoMapper.updateById(cpnUserCouponInfo);
        return true;
    }

    @Override
    public Boolean deleteCpnUserCouponInfo(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        cpnUserCouponInfoMapper.deleteBatchIds(idArr);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean redeem(CpnUserCouponRedeemReq req) {
        // 基础参数校验（Controller 已做 @Validated，这里再兜底避免空指针）
        if (req == null || req.getUserId() == null || req.getCouponId() == null || req.getRedemptionQuantity() == null) {
            throw new FinanceException(ResultEnum.PARAM_ERROR);
        }
        if (req.getRedemptionQuantity() <= 0) {
            throw new FinanceException(ResultEnum.PARAM_ERROR);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime redeemTime = req.getRedemptionTime() == null ? now : req.getRedemptionTime();

        // 1) 先通过 CpnCouponInfoMapper.getList 判断是否存在“可核销数量”
        CpnCouponInfoVo couponQuery = new CpnCouponInfoVo();
        couponQuery.setId(req.getCouponId());
        List<CpnCouponInfoVo> couponInfoList = cpnCouponInfoMapper.getList(couponQuery);
        if (couponInfoList == null || couponInfoList.isEmpty()) {
            throw new FinanceException(ResultEnum.FINANCE_NOT_EXISTS);
        }
        Integer remainingQuantity = couponInfoList.get(0).getRemainingQuantity();
        int remaining = remainingQuantity == null ? 0 : remainingQuantity;
        if (remaining < req.getRedemptionQuantity()) {
            throw new FinanceException(ResultEnum.FINANCE_NOT_SAVE);
        }

        // 2) 每次核销都新增一条券明细记录（status = USED），并将其 id 作为 user_coupon_id 写入核销历史
        // 说明：此模式下 cpn_user_coupon_info_t 更偏“核销明细/流水”，不再对既有记录做状态更新。
        CpnUserCouponInfoVo updateVo = new CpnUserCouponInfoVo()
                .setUserId(req.getUserId())
                .setStatus(CpnUserCouponStatusEnum.USED.getCode())
                .setRedemptionQuantity(req.getRedemptionQuantity())
                .setCouponId(req.getCouponId())
                .setReceiveTime(now);
        boolean saved = addCpnUserCouponInfo(updateVo);
        if (!saved) {
            throw new FinanceException(ResultEnum.SYSTEM_ERROR);
        }

        // 3) 写入核销历史记录
        CpnRedemptionRecordInfo record = new CpnRedemptionRecordInfo();
        record.setUserCouponId(updateVo.getId());
        record.setUserId(req.getUserId());
        record.setOrderId(req.getCouponId());
        record.setMerchantId(req.getMerchantId());
        record.setRedemptionValue(req.getRedemptionValue());
        record.setRedemptionTime(redeemTime);
        record.setRedemptionQuantity(req.getRedemptionQuantity());
        record.setRemarks(StringUtils.isNotEmpty(req.getRemarks()) ? req.getRemarks() : "核销");

        // 异步处理：核销历史记录写入
        taskExecutor.execute(() -> {
            try {
                cpnRedemptionRecordInfoService.save(record);
            } catch (Exception e) {
                log.error("异步写入核销历史记录失败，userCouponId={}", record.getUserCouponId(), e);
            }
        });
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelRedeem(CpnUserCouponRedeemReq req) {
        if (req == null || req.getCouponId() == null) {
            throw new FinanceException(ResultEnum.PARAM_ERROR);
        }
        CpnUserCouponInfoVo cpnUserCouponInfoVo = new CpnUserCouponInfoVo();
        cpnUserCouponInfoVo.setStatus(CpnUserCouponStatusEnum.UNUSED.getCode());
        // 更新券明细记录
        updateCpnUserCouponInfo(cpnUserCouponInfoVo);

        CpnRedemptionRecordInfo record = new CpnRedemptionRecordInfo();
        record.setUserCouponId(req.getUserCouponId());
        record.setUserId(req.getUserId());
        record.setOrderId(req.getCouponId());
        record.setRedemptionTime(LocalDateTime.now());
        record.setRedemptionQuantity(-req.getRedemptionQuantity());
        record.setRemarks(StringUtils.isNotEmpty(req.getRemarks()) ? req.getRemarks() : "取消核销");

        // 写入取消核销历史记录
        taskExecutor.execute(() -> {
            try {
                cpnRedemptionRecordInfoService.save(record);
            } catch (Exception e) {
                log.error("异步写入取消核销历史记录失败，userCouponId={}", record.getUserCouponId(), e);
            }
        });
        return true;
    }
}
