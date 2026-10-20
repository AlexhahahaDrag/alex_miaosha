package com.alex.api.user.orgUserInfo.fallback;

import com.alex.api.user.orgUserInfo.api.OrgUserInfoApi;
import com.alex.api.user.orgUserInfo.vo.OrgUserInfoVo;
import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrgUserInfoFallbackFactory implements FallbackFactory<OrgUserInfoApi> {

    @Override
    public OrgUserInfoApi create(Throwable cause) {
        log.error("OrgUserInfoApi feign fallback triggered. cause: {}", cause != null ? cause.getMessage() : "unknown", cause);
        return new OrgUserInfoApi() {
            private <T> Result<T> fallback(String method) {
                return Result.error(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), "用户机构服务不可用 [" + method + "]");
            }

            @Override
            public Result<Page<OrgUserInfoVo>> getOrgUserInfoPage(Long pageNum, Long pageSize, OrgUserInfoVo orgUserInfoVo) {
                return fallback("getOrgUserInfoPage");
            }

            @Override
            public Result<OrgUserInfoVo> queryOrgUserInfo(String id) {
                return fallback("queryOrgUserInfo");
            }

            @Override
            public Result<Boolean> addOrgUserInfo(OrgUserInfoVo orgUserInfoVo) {
                return fallback("addOrgUserInfo");
            }

            @Override
            public Result<Boolean> updateOrgUserInfo(OrgUserInfoVo orgUserInfoVo) {
                return fallback("updateOrgUserInfo");
            }

            @Override
            public Result<Boolean> deleteOrgUserInfo(String ids) {
                return fallback("deleteOrgUserInfo");
            }
        };
    }
}
