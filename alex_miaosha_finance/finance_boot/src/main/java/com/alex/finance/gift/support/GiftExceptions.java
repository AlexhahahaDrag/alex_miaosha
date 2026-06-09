package com.alex.finance.gift.support;

import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.FinanceException;

/**
 * Gift 模块统一业务异常工厂，与 finance 其他子域一致使用 {@link FinanceException}。
 */
public final class GiftExceptions {

    private GiftExceptions() {
    }

    /** 参数 / 业务规则校验失败（HTTP 体 code=400）。 */
    public static FinanceException param(String message) {
        return new FinanceException(ResultEnum.PARAM_ERROR.getCode(), message);
    }

    /** 数据归属 / 权限不足（HTTP 体 code=403）。 */
    public static FinanceException forbidden(String message) {
        return new FinanceException(ResultEnum.UNAUTHORIZED.getCode(), message);
    }

    /** 未登录（HTTP 体 code=60004）。 */
    public static FinanceException notLogin() {
        return new FinanceException(ResultEnum.USER_NO_LOGIN);
    }
}
