package com.alex.api.user.orgInfo.fallback;

import com.alex.api.user.orgInfo.api.OrgInfoApi;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrgInfoFallbackFactory implements FallbackFactory<OrgInfoApi> {

    @Override
    public OrgInfoApi create(Throwable cause) {
        log.error("OrgInfoApi feign fallback triggered. cause: {}", cause != null ? cause.getMessage() : "unknown", cause);
        return new OrgInfoApi() {
            private <T> Result<T> fallback(String method) {
                return Result.error(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), "机构服务不可用 [" + method + "]");
            }

            @Override
            public Result<Page<OrgInfoVo>> getOrgInfoPage(Long pageNum, Long pageSize, OrgInfoVo orgInfoVo) {
                return fallback("getOrgInfoPage");
            }

            @Override
            public Result<OrgInfoVo> queryOrgInfo(String id) {
                return fallback("queryOrgInfo");
            }

            @Override
            public Result<Boolean> addOrgInfo(OrgInfoVo orgInfoVo) {
                return fallback("addOrgInfo");
            }

            @Override
            public Result<Boolean> updateOrgInfo(OrgInfoVo orgInfoVo) {
                return fallback("updateOrgInfo");
            }

            @Override
            public Result<Boolean> deleteOrgInfo(String ids) {
                return fallback("deleteOrgInfo");
            }
        };
    }
}
