package com.alex.finance.cpnCouponInfo.service.impl;

import com.alex.finance.cpnCouponInfo.entity.CpnCouponInfo;
import com.alex.api.finance.cpnCouponInfo.vo.CpnCouponInfoVo;
import com.alex.finance.cpnCouponInfo.mapper.CpnCouponInfoMapper;
import com.alex.finance.cpnCouponInfo.service.CpnCouponInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import com.alex.common.utils.string.StringUtils;

/**
 * <p>
 * @description:  消费券信息表服务实现类
 * @author:       alex
 * @createDate:   2025-12-17 17:54:42
 * @version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class CpnCouponInfoServiceImp extends ServiceImpl<CpnCouponInfoMapper, CpnCouponInfo> implements CpnCouponInfoService {

    private final CpnCouponInfoMapper cpnCouponInfoMapper;

    @Override
    public Page<CpnCouponInfoVo> getPage(Long pageNum, Long pageSize, CpnCouponInfoVo cpnCouponInfoVo) {
        Page<CpnCouponInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        Page<CpnCouponInfoVo> result = cpnCouponInfoMapper.getPage(page, cpnCouponInfoVo);
        fillExpireStatus(result == null ? null : result.getRecords());
        return result;
    }

    @Override
    public List<CpnCouponInfoVo> getList(CpnCouponInfoVo cpnCouponInfoVo) {
        List<CpnCouponInfoVo> list = cpnCouponInfoMapper.getList(cpnCouponInfoVo);
        fillExpireStatus(list);
        return list;
    }

    @Override
    public CpnCouponInfoVo queryCpnCouponInfo(Long id) {
        CpnCouponInfoVo vo = cpnCouponInfoMapper.queryCpnCouponInfo(id);
        if (vo != null) {
            vo.setExpireStatus(calcExpireStatus(vo.getEndDate(), LocalDateTime.now()));
        }
        return vo;
    }

    @Override
    public Boolean addCpnCouponInfo(CpnCouponInfoVo cpnCouponInfoVo) {
        CpnCouponInfo cpnCouponInfo = new CpnCouponInfo();
        BeanUtils.copyProperties(cpnCouponInfoVo, cpnCouponInfo);
        cpnCouponInfoMapper.insert(cpnCouponInfo);
        return true;
    }

    @Override
    public Boolean updateCpnCouponInfo(CpnCouponInfoVo cpnCouponInfoVo) {
        CpnCouponInfo cpnCouponInfo = new CpnCouponInfo();
        BeanUtils.copyProperties(cpnCouponInfoVo, cpnCouponInfo);
        cpnCouponInfoMapper.updateById(cpnCouponInfo);
        return true;
    }

    @Override
    public Boolean deleteCpnCouponInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        cpnCouponInfoMapper.deleteBatchIds(idArr);
        return true;
    }

    /**
     * 填充“过期倒计时状态”展示字段
     */
    private void fillExpireStatus(List<CpnCouponInfoVo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (CpnCouponInfoVo item : list) {
            if (item == null) {
                continue;
            }
            item.setExpireStatus(calcExpireStatus(item.getEndDate(), now));
            item.setExpireRangeStatus(calcExpireRangeStatus(item.getEndDate(), now));
        }
    }

    /**
     * 计算过期状态：离过期还有三天 / 离过期还有一天 / 离过期还有X小时 / 离过期还有X分钟 / 过期
     */
    private String calcExpireStatus(LocalDateTime endDate, LocalDateTime now) {
        if (endDate == null) {
            return "";
        }
        // 统一使用“分钟”为最小粒度，避免小时/天之间反复换算造成误差
        final long MINUTES_PER_HOUR = 60L;
        final long MINUTES_PER_DAY = 24L * MINUTES_PER_HOUR;

        long minutes = Duration.between(now, endDate).toMinutes();
        if (minutes <= 0L) {
            return "已过期";
        }

        // >= 1天：展示剩余天数（向上取整，避免“还剩1.2天”显示为“剩余1天”造成误解）
        if (minutes >= MINUTES_PER_DAY) {
            long days = (minutes + MINUTES_PER_DAY - 1) / MINUTES_PER_DAY;
            return "剩余" + days + "天";
        }

        // >= 1小时：展示剩余小时（向上取整）
        if (minutes >= MINUTES_PER_HOUR) {
            long hours = (minutes + MINUTES_PER_HOUR - 1) / MINUTES_PER_HOUR;
            return "剩余" + hours + "小时";
        }

        // < 1小时：展示分钟
        return "剩余" + minutes + "分钟";
    }

    /**
     * 计算“过期区间状态”数字编码：
     * - 0：已过期
     * - 1：<1天
     * - 2：1-3天
     * - 3：>3天
     */
    private Integer calcExpireRangeStatus(LocalDateTime endDate, LocalDateTime now) {
        if (endDate == null) {
            return null;
        }

        // 与 calcExpireStatus 保持一致：用分钟做统一粒度
        final long MINUTES_PER_HOUR = 60L;
        final long MINUTES_PER_DAY = 24L * MINUTES_PER_HOUR;
        final long THREE_DAYS_MINUTES = 3L * MINUTES_PER_DAY;

        long minutes = Duration.between(now, endDate).toMinutes();
        if (minutes <= 0L) {
            return 0;
        }
        if (minutes < MINUTES_PER_DAY) {
            return 1;
        }
        if (minutes <= THREE_DAYS_MINUTES) {
            return 2;
        }
        return 3;
    }
}
