package com.alex.api.user.menuInfo.fallback;

import com.alex.api.user.menuInfo.api.MenuInfoApi;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MenuInfoFallbackFactory implements FallbackFactory<MenuInfoApi> {

    @Override
    public MenuInfoApi create(Throwable cause) {
        throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "menuInfo");
    }
}
