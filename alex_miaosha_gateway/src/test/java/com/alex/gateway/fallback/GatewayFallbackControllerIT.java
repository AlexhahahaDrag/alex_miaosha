package com.alex.gateway.fallback;

import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 网关熔断与异常降级控制器契约测试
 */
class GatewayFallbackControllerIT {

    private final FallbackController fallbackController = new FallbackController();

    @Test
    @DisplayName("网关默认降级回调：返回规范 Result 结构并绑定 SYSTEM_UP_ERROR 错误码")
    void defaultFallback_shouldReturnSystemUpError() {
        Result<ResultEnum> result = fallbackController.fallback();

        assertNotNull(result, "降级回调返回结果不可为空");
        assertEquals(ResultEnum.SYSTEM_UP_ERROR.getCode(), result.getCode(), "错误码必须与 SYSTEM_UP_ERROR 一致");
        assertEquals(ResultEnum.SYSTEM_UP_ERROR.getValue(), result.getMessage(), "错误信息必须与 SYSTEM_UP_ERROR 一致");
        assertNull(result.getData(), "默认降级回调 data 字段应为 null");
    }

    @Test
    @DisplayName("网关降级契约：验证错误码非 200 且提示语具备微服务未启动或网络异常语义")
    void defaultFallback_code_and_message_contract() {
        Result<ResultEnum> result = fallbackController.fallback();

        assertNotEquals("200", result.getCode());
        assertTrue(result.getMessage().contains("系统更新中") || result.getMessage().contains("请稍后尝试"),
                "错误描述需明确提示服务更新中或请稍后重试: " + result.getMessage());
    }
}
