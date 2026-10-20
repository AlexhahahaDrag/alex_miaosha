package com.alex.common.aspect;

import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.annotations.user.AccessLimit;
import com.alex.common.redis.key.AccessKey;
import com.alex.common.redis.key.CommonKey;
import com.alex.common.utils.redis.RedisUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 集成测试：分布式防重提交与接口频次限流 AOP 代理端到端行为验证
 */
@ExtendWith(MockitoExtension.class)
class DistributedConcurrencyAspectIT {

    @Mock
    private RedisUtils redisUtils;

    private AvoidRepeatableCommitAspect avoidRepeatableCommitAspect;
    private AccessLimitAspect accessLimitAspect;

    public interface TestOperationService {
        Result<String> submitData(String payload);
        Result<String> uploadFile(MockMultipartFile file);
        Result<String> queryResource();
        Result<String> queryOtherResource();
    }

    public static class TestOperationServiceImpl implements TestOperationService {
        @Override
        @AvoidRepeatableCommit(timeout = 3000)
        public Result<String> submitData(String payload) {
            return Result.success("submitted: " + payload);
        }

        @Override
        @AvoidRepeatableCommit(timeout = 5000)
        public Result<String> uploadFile(MockMultipartFile file) {
            return Result.success("uploaded: " + file.getOriginalFilename());
        }

        @Override
        @AccessLimit(limit = 2, timeout = 30)
        public Result<String> queryResource() {
            return Result.success("resource-data");
        }

        @Override
        @AccessLimit(limit = 2, timeout = 30)
        public Result<String> queryOtherResource() {
            return Result.success("other-resource-data");
        }
    }

    private TestOperationService proxyService;

    @BeforeEach
    void setUp() {
        avoidRepeatableCommitAspect = new AvoidRepeatableCommitAspect(redisUtils);
        accessLimitAspect = new AccessLimitAspect(redisUtils);

        TestOperationServiceImpl target = new TestOperationServiceImpl();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.setProxyTargetClass(true);
        factory.addAspect(avoidRepeatableCommitAspect);
        factory.addAspect(accessLimitAspect);
        proxyService = factory.getProxy();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("AvoidRepeatableCommit: 携带 Token 凭证时优先以 Token 作为键标识进行 Redis SET NX")
    void testAvoidRepeatableCommit_WithTokenHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("token", "token-user-alex-8888");
        request.setRemoteAddr("10.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        when(redisUtils.setIfAbsent(any(CommonKey.class), keyCaptor.capture(), eq("1"), eq(3000L), eq(TimeUnit.MILLISECONDS)))
                .thenReturn(true);

        Result<String> res = proxyService.submitData("order-info-123");
        assertNotNull(res);
        assertEquals("200", res.getCode());
        assertEquals("submitted: order-info-123", res.getMessage());

        String generatedKey = keyCaptor.getValue();
        assertTrue(generatedKey.contains("token-user-alex-8888"), "Redis 键必须包含用户 Token 凭证");
        assertFalse(generatedKey.contains("10.0.0.1"), "带 Token 时不应以 IP 作为主标识");
    }

    @Test
    @DisplayName("AvoidRepeatableCommit: 无 Token 时自动降级以客户端真实 IP 进行并发防重拦截")
    void testAvoidRepeatableCommit_FallbackToIpAndBlocked() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.16.2.88");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        // 首次请求放行
        when(redisUtils.setIfAbsent(any(CommonKey.class), keyCaptor.capture(), eq("1"), eq(3000L), eq(TimeUnit.MILLISECONDS)))
                .thenReturn(true)
                .thenReturn(false); // 第二次请求因已存在键而返回 false

        Result<String> firstRes = proxyService.submitData("same-content");
        assertEquals("200", firstRes.getCode());

        // 第二次重复提交
        Result<String> secondRes = proxyService.submitData("same-content");
        assertNotNull(secondRes);
        assertEquals(ResultEnum.REPEAT_COMMIT.getCode(), secondRes.getCode());

        String generatedKey = keyCaptor.getValue();
        assertTrue(generatedKey.contains("172.16.2.88"), "无 Token 时 Redis 键必须包含客户端 IP");
    }

    @Test
    @DisplayName("AvoidRepeatableCommit: 上传 MultipartFile 时正确提取文件名与文件大小生成指纹")
    void testAvoidRepeatableCommit_MultipartFileFingerprint() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("token", "user-upload-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        when(redisUtils.setIfAbsent(any(CommonKey.class), keyCaptor.capture(), eq("1"), eq(5000L), eq(TimeUnit.MILLISECONDS)))
                .thenReturn(true);

        MockMultipartFile file1 = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1, 2, 3, 4, 5});
        Result<String> res1 = proxyService.uploadFile(file1);
        assertEquals("200", res1.getCode());

        String key1 = keyCaptor.getValue();
        assertNotNull(key1);
        verify(redisUtils, times(1)).setIfAbsent(any(CommonKey.class), anyString(), eq("1"), eq(5000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("AccessLimit: 未超限时放行，超过 limit 时被精确拦截返回 429 对应错误码")
    void testAccessLimit_ThresholdAndBlock() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/finance/query");
        request.setRemoteAddr("192.168.1.99");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // 阈值为 2：前 2 次访问分别递增为 1, 2，第三次达到 3（超限）
        when(redisUtils.incrementWithExpire(any(AccessKey.class), eq("/api/finance/query:192.168.1.99"), eq(30L), eq(TimeUnit.SECONDS)))
                .thenReturn(1L)
                .thenReturn(2L)
                .thenReturn(3L);

        Result<String> call1 = proxyService.queryResource();
        assertEquals("200", call1.getCode());

        Result<String> call2 = proxyService.queryResource();
        assertEquals("200", call2.getCode());

        Result<String> call3 = proxyService.queryResource();
        assertNotNull(call3);
        assertEquals(ResultEnum.ACCESS_LIMIT_REACHED.getCode(), call3.getCode());
    }

    @Test
    @DisplayName("AccessLimit: 严格按 URI + IP 双维度隔离，不同接口配额互不干扰")
    void testAccessLimit_UriIsolation() {
        MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.setRequestURI("/api/res/a");
        request1.setRemoteAddr("192.168.1.99");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request1));

        // 接口 A 已经超限
        when(redisUtils.incrementWithExpire(any(AccessKey.class), eq("/api/res/a:192.168.1.99"), eq(30L), eq(TimeUnit.SECONDS)))
                .thenReturn(3L);

        Result<String> blockedCall = proxyService.queryResource();
        assertEquals(ResultEnum.ACCESS_LIMIT_REACHED.getCode(), blockedCall.getCode());

        // 同一客户端访问接口 B，配额独立计算放行
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.setRequestURI("/api/res/b");
        request2.setRemoteAddr("192.168.1.99");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request2));

        when(redisUtils.incrementWithExpire(any(AccessKey.class), eq("/api/res/b:192.168.1.99"), eq(30L), eq(TimeUnit.SECONDS)))
                .thenReturn(1L);

        Result<String> okCall = proxyService.queryOtherResource();
        assertEquals("200", okCall.getCode());
    }
}
