package com.alex.gateway.fallback;

import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:  熔断回调
 * author:       majf
 * createDate:   2022/7/29 15:38
 * version:      1.0.0
 */
@RestController
public class FallbackController {

    @RequestMapping(value = "/defaultFallback")
    public Result fallback() {
        return Result.error(ResultEnum.SYSTEM_UP_ERROR);
    }
}
