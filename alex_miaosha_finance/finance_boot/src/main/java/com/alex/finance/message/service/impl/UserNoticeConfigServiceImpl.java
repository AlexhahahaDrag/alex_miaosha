package com.alex.finance.message.service.impl;

import com.alex.api.finance.message.vo.UserNoticeConfigVo;
import com.alex.common.exception.FinanceException;
import com.alex.finance.message.entity.UserNoticeConfig;
import com.alex.finance.message.enums.NoticeBizType;
import com.alex.finance.message.mapper.UserNoticeConfigMapper;
import com.alex.finance.message.service.UserNoticeConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户个性化消息通知渠道配置服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserNoticeConfigServiceImpl extends ServiceImpl<UserNoticeConfigMapper, UserNoticeConfig>
        implements UserNoticeConfigService {

    @Override
    public boolean isInternalEnabled(Long userId, String noticeCode) {
        if (userId == null || userId <= 0 || StringUtils.isBlank(noticeCode)) {
            return true;
        }

        List<UserNoticeConfig> userConfigs = getUserConfigs(userId);
        if (userConfigs.isEmpty()) {
            // 默认策略：每个人都发送
            return true;
        }

        // 1. 优先检查 GLOBAL 全局总开关
        UserNoticeConfig globalConfig = findConfig(userConfigs, NoticeBizType.GLOBAL.getCode());
        if (globalConfig != null && Boolean.FALSE.equals(globalConfig.getInternalEnabled())) {
            log.debug("用户 [{}] 全局站内信总开关联闭，一票否决 [{}] 站内信发送", userId, noticeCode);
            return false;
        }

        // 2. 检查具体业务子项开关
        UserNoticeConfig specificConfig = findConfig(userConfigs, noticeCode);
        if (specificConfig != null && Boolean.FALSE.equals(specificConfig.getInternalEnabled())) {
            log.debug("用户 [{}] 独立关闭了 [{}] 站内信通知", userId, noticeCode);
            return false;
        }

        return true;
    }

    @Override
    public boolean isWechatEnabled(Long userId, String noticeCode) {
        if (userId == null || userId <= 0 || StringUtils.isBlank(noticeCode)) {
            return true;
        }

        List<UserNoticeConfig> userConfigs = getUserConfigs(userId);
        if (userConfigs.isEmpty()) {
            // 默认策略：每个人都发送
            return true;
        }

        // 1. 优先检查 GLOBAL 全局总开关
        UserNoticeConfig globalConfig = findConfig(userConfigs, NoticeBizType.GLOBAL.getCode());
        if (globalConfig != null && Boolean.FALSE.equals(globalConfig.getWechatEnabled())) {
            log.debug("用户 [{}] 全局微信消息总开关联闭，一票否决 [{}] 微信发送", userId, noticeCode);
            return false;
        }

        // 2. 检查具体业务子项开关
        UserNoticeConfig specificConfig = findConfig(userConfigs, noticeCode);
        if (specificConfig != null && Boolean.FALSE.equals(specificConfig.getWechatEnabled())) {
            log.debug("用户 [{}] 独立关闭了 [{}] 微信模板消息通知", userId, noticeCode);
            return false;
        }

        return true;
    }

    @Override
    public List<UserNoticeConfigVo> getMyConfigs(Long userId) {
        if (userId == null || userId <= 0) {
            return Collections.emptyList();
        }

        List<UserNoticeConfig> existingList = getUserConfigs(userId);
        Map<String, UserNoticeConfig> configMap = existingList.stream()
                .collect(Collectors.toMap(UserNoticeConfig::getNoticeCode, Function.identity(), (k1, k2) -> k1));

        List<UserNoticeConfigVo> resultList = new ArrayList<>(NoticeBizType.values().length);
        for (NoticeBizType bizType : NoticeBizType.values()) {
            UserNoticeConfig existing = configMap.get(bizType.getCode());
            UserNoticeConfigVo vo = new UserNoticeConfigVo();
            vo.setUserId(userId);
            vo.setNoticeCode(bizType.getCode());
            vo.setNoticeName(bizType.getName());
            vo.setNoticeDesc(bizType.getDesc());

            if (existing != null) {
                vo.setId(existing.getId());
                vo.setInternalEnabled(Boolean.TRUE.equals(existing.getInternalEnabled()));
                vo.setWechatEnabled(Boolean.TRUE.equals(existing.getWechatEnabled()));
            } else {
                // 未持久化记录时回退默认开启策略 (1, 1)
                vo.setId(null);
                vo.setInternalEnabled(true);
                vo.setWechatEnabled(true);
            }
            resultList.add(vo);
        }

        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveMyConfig(Long userId, UserNoticeConfigVo vo) {
        if (userId == null || userId <= 0) {
            throw FinanceException.forbidden("无法获取有效用户上下文！");
        }
        if (vo == null || StringUtils.isBlank(vo.getNoticeCode())) {
            throw FinanceException.param("业务通知类型编码不能为空！");
        }
        String noticeCode = vo.getNoticeCode().trim().toUpperCase();
        if (!NoticeBizType.isValidCode(noticeCode)) {
            throw FinanceException.param("非法的业务通知类型编码: " + noticeCode);
        }

        boolean internalEnabled = vo.getInternalEnabled() == null || vo.getInternalEnabled();
        boolean wechatEnabled = vo.getWechatEnabled() == null || vo.getWechatEnabled();

        UserNoticeConfig existing = getOne(new LambdaQueryWrapper<UserNoticeConfig>()
                .eq(UserNoticeConfig::getUserId, userId)
                .eq(UserNoticeConfig::getNoticeCode, noticeCode));

        if (existing != null) {
            existing.setInternalEnabled(internalEnabled);
            existing.setWechatEnabled(wechatEnabled);
            updateById(existing);
        } else {
            UserNoticeConfig newConfig = new UserNoticeConfig();
            newConfig.setUserId(userId);
            newConfig.setNoticeCode(noticeCode);
            newConfig.setInternalEnabled(internalEnabled);
            newConfig.setWechatEnabled(wechatEnabled);
            save(newConfig);
        }

        log.info("用户 [{}] 成功更新业务 [{}] 通知渠道配置: internal={}, wechat={}",
                userId, noticeCode, internalEnabled, wechatEnabled);
        return true;
    }

    private List<UserNoticeConfig> getUserConfigs(Long userId) {
        return list(new LambdaQueryWrapper<UserNoticeConfig>()
                .eq(UserNoticeConfig::getUserId, userId));
    }

    private UserNoticeConfig findConfig(List<UserNoticeConfig> configs, String noticeCode) {
        if (configs == null || configs.isEmpty() || StringUtils.isBlank(noticeCode)) {
            return null;
        }
        for (UserNoticeConfig c : configs) {
            if (noticeCode.equalsIgnoreCase(c.getNoticeCode())) {
                return c;
            }
        }
        return null;
    }
}
