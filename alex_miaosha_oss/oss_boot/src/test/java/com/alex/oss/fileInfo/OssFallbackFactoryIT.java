package com.alex.oss.fileInfo;

import com.alex.api.oss.fileInfo.api.OssApi;
import com.alex.api.oss.fileInfo.fallback.OssFallbackFactory;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OpenFeign 跨微服务降级工厂服务治理集成测试
 */
class OssFallbackFactoryIT {

    private final OssFallbackFactory fallbackFactory = new OssFallbackFactory();

    @Test
    @DisplayName("降级工厂契约：返回合法代理对象且不为 null")
    void ossFallbackFactory_shouldCreateSafeProxy() {
        Throwable cause = new RuntimeException("Connection timed out to alex-oss-dev");
        OssApi fallbackApi = fallbackFactory.create(cause);

        assertNotNull(fallbackApi, "Fallback 工厂必须创建非空代理对象");
    }

    @Test
    @DisplayName("降级方法阻断：调用查询/上传/删除等接口时统一抛出规范 SystemException(SYSTEM_NO_AVAILABLE, 'oss')")
    void fallbackMethods_shouldThrowSystemNoAvailable() {
        Throwable cause = new IllegalStateException("Service alex-oss-dev is offline");
        OssApi fallbackApi = fallbackFactory.create(cause);

        // 1. query
        SystemException exQuery = assertThrows(SystemException.class, () -> fallbackApi.query(1001L));
        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), exQuery.getCode());
        assertTrue(exQuery.getMsg().contains("oss") && exQuery.getMsg().contains("系统服务不存在"));

        // 2. getPage
        SystemException exPage = assertThrows(SystemException.class, () -> fallbackApi.getPage(1L, 10L, null));
        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), exPage.getCode());
        assertTrue(exPage.getMsg().contains("oss"));

        // 3. add
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "bytes".getBytes());
        SystemException exAdd = assertThrows(SystemException.class, () -> fallbackApi.add("user", file));
        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), exAdd.getCode());
        assertTrue(exAdd.getMsg().contains("oss"));

        // 4. addBatch
        SystemException exBatch = assertThrows(SystemException.class, () -> fallbackApi.addBatch("user", Collections.singletonList(file)));
        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), exBatch.getCode());
        assertTrue(exBatch.getMsg().contains("oss"));

        // 5. update
        SystemException exUpdate = assertThrows(SystemException.class, () -> fallbackApi.update(1001L, "user", file));
        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), exUpdate.getCode());
        assertTrue(exUpdate.getMsg().contains("oss"));

        // 6. delete
        SystemException exDelete = assertThrows(SystemException.class, () -> fallbackApi.delete("1001,1002"));
        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), exDelete.getCode());
        assertTrue(exDelete.getMsg().contains("oss"));
    }
}
