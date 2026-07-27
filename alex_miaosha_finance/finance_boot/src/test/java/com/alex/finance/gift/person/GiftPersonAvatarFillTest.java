package com.alex.finance.gift.person;

import com.alex.api.finance.gift.person.vo.GiftPersonInfoVo;
import com.alex.api.oss.fileInfo.api.OssApi;
import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.api.user.orgUserInfo.api.OrgUserInfoApi;
import com.alex.api.user.userInfo.api.UserApi;
import com.alex.base.common.Result;
import com.alex.finance.gift.person.service.impl.GiftPersonInfoServiceImp;
import com.alex.finance.gift.personoption.service.GiftPersonRelationOptionService;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GiftPersonAvatarFillTest {

    @Test
    void fillAvatarUrls_setsUrlsWhenOssReturnsSuccess() {
        OssApi ossApi = mock(OssApi.class);
        GiftPersonInfoServiceImp service = newService(ossApi);

        FileInfoVo file = new FileInfoVo();
        file.setId(100L);
        file.setPreUrl("https://cdn.example/avatar.png");
        file.setPreThumbnailUrl("https://cdn.example/avatar-thumb.png");
        when(ossApi.getFileInfo(List.of(100L))).thenReturn(Result.success(List.of(file)));

        GiftPersonInfoVo vo = new GiftPersonInfoVo().setAvatar(100L);
        ReflectionTestUtils.invokeMethod(service, "fillAvatarUrls", vo);

        assertEquals("https://cdn.example/avatar.png", vo.getAvatarUrl());
        assertEquals("https://cdn.example/avatar-thumb.png", vo.getAvatarThumbnailUrl());
    }

    @Test
    void fillAvatarUrls_swallowsOssFailureAndLeavesUrlsNull() {
        OssApi ossApi = mock(OssApi.class);
        GiftPersonInfoServiceImp service = newService(ossApi);
        when(ossApi.getFileInfo(anyList())).thenThrow(new RuntimeException("oss down"));

        GiftPersonInfoVo vo = new GiftPersonInfoVo().setAvatar(200L);
        ReflectionTestUtils.invokeMethod(service, "fillAvatarUrls", vo);

        assertNull(vo.getAvatarUrl());
        assertNull(vo.getAvatarThumbnailUrl());
        assertEquals(200L, vo.getAvatar());
    }

    private static GiftPersonInfoServiceImp newService(OssApi ossApi) {
        return new GiftPersonInfoServiceImp(
                mock(GiftDataScopeSupport.class),
                mock(GiftPersonRelationOptionService.class),
                mock(GiftRecordInfoService.class),
                mock(OrgUserInfoApi.class),
                mock(UserApi.class),
                ossApi);
    }
}
