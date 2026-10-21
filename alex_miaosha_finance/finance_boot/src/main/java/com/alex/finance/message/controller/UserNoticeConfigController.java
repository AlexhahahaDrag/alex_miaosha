package com.alex.finance.message.controller;

import com.alex.api.finance.message.vo.UserNoticeConfigVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.api.user.userInfo.UserUtils;
import com.alex.base.common.Result;
import com.alex.common.exception.FinanceException;
import com.alex.finance.message.service.UserNoticeConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户个性化消息通知渠道配置接口
 */
@Api(value = "用户消息通知渠道配置", tags = {"用户消息通知渠道配置"})
@RestController
@RequestMapping("/user-notice-config")
@Slf4j
@RequiredArgsConstructor
public class UserNoticeConfigController {

    private final UserNoticeConfigService userNoticeConfigService;
    private final UserUtils userUtils;

    @ApiOperation(value = "获取当前登录用户的 8 大业务消息通知渠道配置")
    @GetMapping("/my-configs")
    public Result<List<UserNoticeConfigVo>> getMyConfigs() {
        Long loginUserId = getRequiredLoginUserId();
        List<UserNoticeConfigVo> configs = userNoticeConfigService.getMyConfigs(loginUserId);
        return Result.success(configs);
    }

    @ApiOperation(value = "保存/更新当前登录用户的特定业务通知渠道配置")
    @PutMapping("/save-my-config")
    public Result<Boolean> saveMyConfig(@RequestBody UserNoticeConfigVo vo) {
        Long loginUserId = getRequiredLoginUserId();
        Boolean success = userNoticeConfigService.saveMyConfig(loginUserId, vo);
        return Result.success(success);
    }

    private Long getRequiredLoginUserId() {
        TUserVo loginUser = userUtils.getLoginUser();
        if (loginUser == null || loginUser.getId() == null) {
            throw FinanceException.forbidden("未检测到登录用户，无法读取或更新个性化配置！");
        }
        return loginUser.getId();
    }
}
