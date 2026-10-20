package com.alex.oss.config.s3;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OSS 预签名直链、免签持久化直链、防盗链时效与 URL 编码契约测试
 */
class OssPresignedUrlAndContractIT {

    static class TestS3Template extends BaseS3Template {
        public void setTestClient(MinioClient client, String publicUrl) {
            this.minioClient = client;
            this.publicUrl = publicUrl;
        }

        public void setTestClientWithEndpoint(MinioClient client, String url, Integer port, Boolean secure) {
            this.minioClient = client;
            this.url = url;
            this.port = port;
            this.secure = secure;
        }
    }

    private MinioClient mockClient;
    private TestS3Template template;

    @BeforeEach
    void setUp() {
        mockClient = mock(MinioClient.class);
        template = new TestS3Template();
        template.setTestClient(mockClient, "https://oss.mycorp.com");
    }

    @Test
    @DisplayName("免签持久直链：isPublic=true 时生成无签名 CDN 静态直链且不触发 MinIO 签名调用")
    void preview_withPublicTrue_shouldGeneratePermanentUrlWithoutSignature() throws Exception {
        String url = template.preview("user-bucket", "user/2026-10-20/avatar.png", true);

        assertNotNull(url);
        assertEquals("https://oss.mycorp.com/user-bucket/user/2026-10-20/avatar.png", url);
        assertFalse(url.contains("X-Amz-Signature"), "免签直链不得携带预签名参数");

        // 强断言：底层 MinIO getPresignedObjectUrl 一次都未被调用
        verify(mockClient, never()).getPresignedObjectUrl(any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false})
    @DisplayName("私有防盗链直链：isPublic=false 时必须调用 MinIO 预签名 SDK 并设置 3600 秒时效")
    void preview_withPublicFalse_shouldInvokePresignedSdkWith1HourExpiry(Boolean isPublic) throws Exception {
        String internalSignedUrl = "http://minio-internal:9000/finance-bucket/invoice.pdf?X-Amz-Signature=secret123";
        when(mockClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn(internalSignedUrl);

        String resultUrl = template.preview("finance-bucket", "finance/2026-10-20/invoice.pdf", isPublic);

        assertNotNull(resultUrl);
        // 验证域名平滑映射至外部 publicUrl
        assertTrue(resultUrl.startsWith("https://oss.mycorp.com/finance-bucket/invoice.pdf?X-Amz-Signature=secret123"));

        ArgumentCaptor<GetPresignedObjectUrlArgs> captor = ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
        verify(mockClient).getPresignedObjectUrl(captor.capture());

        GetPresignedObjectUrlArgs args = captor.getValue();
        assertEquals("finance-bucket", args.bucket());
        assertEquals("finance/2026-10-20/invoice.pdf", args.object());
        assertEquals(Method.GET, args.method());
    }

    @Test
    @DisplayName("默认防盗链直链：isPublic 未传 (null) 时一律按私有安全桶处理生成预签名直链")
    void preview_withNullPublic_shouldDefaultToPresignedUrl() throws Exception {
        String originUrl = "http://127.0.0.1:9000/private-bucket/secret.doc?X-Amz-Signature=abc";
        when(mockClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn(originUrl);

        String resultUrl = template.preview("private-bucket", "secret.doc", null);

        assertNotNull(resultUrl);
        assertTrue(resultUrl.contains("X-Amz-Signature=abc"));
        verify(mockClient, times(1)).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("空对象键容错：objectKey 为空时安全返回 null 且不触发网络调用")
    void preview_withBlankObjectKey_shouldReturnNullWithoutCallingSdk(String blankKey) throws Exception {
        String url = template.preview("user-bucket", blankKey, true);
        assertNull(url);
        verify(mockClient, never()).getPresignedObjectUrl(any());
    }

    @Test
    @DisplayName("状态防呆：客户端未初始化时调用 preview 必须抛出 IllegalStateException")
    void preview_notInitialized_mustThrowIllegalStateException() {
        TestS3Template uninitTemplate = new TestS3Template();
        assertThrows(IllegalStateException.class, () ->
                uninitTemplate.preview("bucket", "file.png", true)
        );
    }

    @Test
    @DisplayName("URL 编码契约：encodePathKey 必须保留路径斜杠并转义中文与空格且避免双重编码")
    void encodePathKey_preservesPathSeparatorsAndEncodesChineseAndSpaces() {
        // 1. 中文与空格编码
        String rawKey = "user/2026/测试 文件.png";
        String encoded = BaseS3Template.encodePathKey(rawKey);
        assertEquals("user/2026/%E6%B5%8B%E8%AF%95%20%E6%96%87%E4%BB%B6.png", encoded);

        // 2. 避免双重编码（已编码的 %20 不会被二次转义成 %2520）
        String alreadyEncoded = "goods/item%20detail.jpg";
        String encodedTwice = BaseS3Template.encodePathKey(alreadyEncoded);
        assertEquals("goods/item%20detail.jpg", encodedTwice);

        // 3. 空白与 null 保护
        assertNull(BaseS3Template.encodePathKey(null));
        assertEquals("", BaseS3Template.encodePathKey(""));
    }

    @Test
    @DisplayName("端点兜底降级：未配置 publicUrl 时根据 endpoint 与 secure 自动拼装免签直链")
    void buildPublicDirectUrl_fallbackToEndpointWhenNoPublicUrl() {
        TestS3Template fallbackTemplate = new TestS3Template();
        fallbackTemplate.setTestClientWithEndpoint(mockClient, "192.168.1.100", 9000, false);

        String url = fallbackTemplate.buildPublicDirectUrl("goods-bucket", "goods/item_1.jpg");
        assertEquals("http://192.168.1.100:9000/goods-bucket/goods/item_1.jpg", url);

        // 开启 HTTPS
        fallbackTemplate.setTestClientWithEndpoint(mockClient, "minio.internal", 443, true);
        String httpsUrl = fallbackTemplate.buildPublicDirectUrl("goods-bucket", "goods/item_2.jpg");
        assertEquals("https://minio.internal/goods-bucket/goods/item_2.jpg", httpsUrl);
    }
}
