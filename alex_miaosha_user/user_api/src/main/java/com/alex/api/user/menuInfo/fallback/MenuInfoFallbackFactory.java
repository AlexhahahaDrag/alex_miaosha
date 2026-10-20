package com.alex.api.user.menuInfo.fallback;

import com.alex.api.user.menuInfo.api.MenuInfoApi;
import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MenuInfoFallbackFactory implements FallbackFactory<MenuInfoApi> {

    @Override
    public MenuInfoApi create(Throwable cause) {
        log.error("MenuInfoApi feign fallback triggered. cause: {}", cause != null ? cause.getMessage() : "unknown", cause);
        return new MenuInfoApi() {
            private <T> Result<T> fallback(String method) {
                return Result.error(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), "菜单服务不可用 [" + method + "]");
            }

            @Override
            public Result<Page<MenuInfoVo>> getMenuInfoPage(Long pageNum, Long pageSize, MenuInfoVo menuInfoVo) {
                return fallback("getMenuInfoPage");
            }

            @Override
            public Result<MenuInfoVo> queryMenuInfo(String id) {
                return fallback("queryMenuInfo");
            }

            @Override
            public Result<Boolean> updateMenuInfo(MenuInfoVo menuInfoVo) {
                return fallback("updateMenuInfo");
            }

            @Override
            public Result<Boolean> deleteMenuInfo(String ids) {
                return fallback("deleteMenuInfo");
            }
        };
    }
}
