package com.alex.oss.config.s3;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import okhttp3.Headers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 验证 BaseS3Template 的 Bucket 内存缓存、流未提前关闭以及预签名 URL 域名映射替换能力
 */
class BaseS3TemplateTest {

    static class TestS3Template extends BaseS3Template {
        public void setTestClient(MinioClient client, String publicUrl) {
            this.minioClient = client;
            this.publicUrl = publicUrl;
        }
    }

    @Test
    @DisplayName("验证未配置参数时优雅跳过初始化且 isInitialized 返回 false")
    void testGracefulInitSkip() {
        TestS3Template template = new TestS3Template();
        assertFalse(template.isInitialized());
        // 传入 null 参数，不应抛出断言崩溃
        template.initClient(null, null, null, null, null, false, null);
        assertFalse(template.isInitialized());
    }

    @Test
    @DisplayName("验证 Bucket 缓存机制：第二次调用 existBucket 不再发起网络探测")
    void testBucketCaching() throws Exception {
        MinioClient mockClient = mock(MinioClient.class);
        when(mockClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        TestS3Template template = new TestS3Template();
        template.setTestClient(mockClient, null);

        // 首次检查
        template.existBucket("my-bucket");
        assertTrue(template.getKnownBuckets().contains("my-bucket"));
        verify(mockClient, times(1)).bucketExists(any(BucketExistsArgs.class));

        // 第二次检查（命中内存缓存，不再调用底层 SDK）
        template.existBucket("my-bucket");
        verify(mockClient, times(1)).bucketExists(any(BucketExistsArgs.class));
    }

    @Test
    @DisplayName("验证 fileDownload 返回的流是处于开启状态且可正常读取的（锁定 Stream Closed Bug 防回归）")
    void testFileDownloadStreamIsOpen() throws Exception {
        MinioClient mockClient = mock(MinioClient.class);
        byte[] content = "hello oss stream".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(content);
        GetObjectResponse mockResponse = new GetObjectResponse(
                Headers.of(),
                "bucket",
                "region",
                "file.txt",
                bais
        );
        when(mockClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        TestS3Template template = new TestS3Template();
        template.setTestClient(mockClient, null);

        InputStream returnedStream = template.fileDownload("bucket", "file.txt");
        assertNotNull(returnedStream);

        // 读取流内容，验证未被提前关闭
        byte[] buffer = new byte[100];
        int readBytes = returnedStream.read(buffer);
        assertEquals(content.length, readBytes);
        assertEquals("hello oss stream", new String(buffer, 0, readBytes, StandardCharsets.UTF_8));

        // 消费端负责关闭
        returnedStream.close();
    }

    @Test
    @DisplayName("验证 preview 预签名 URL 在配置 publicUrl 时平滑映射为对外域名")
    void testPreviewPublicUrlMapping() throws Exception {
        MinioClient mockClient = mock(MinioClient.class);
        String originUrl = "http://115.190.181.243:9000/alex-miaosha/avatar.png?X-Amz-Signature=123456";
        when(mockClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn(originUrl);

        TestS3Template template = new TestS3Template();
        template.setTestClient(mockClient, "https://oss.mycorp.com");

        String previewUrl = template.preview("alex-miaosha", "avatar.png");
        assertNotNull(previewUrl);
        // 物理主机地址被成功替换为公网 CDN 域名，保留 Query 参数
        assertTrue(previewUrl.startsWith("https://oss.mycorp.com/alex-miaosha/avatar.png?X-Amz-Signature=123456"));
    }

    @Test
    @DisplayName("验证仅当 isPublic == true 时生成免签持久直链")
    void testPublicBucketDirectUrl() throws Exception {
        MinioClient mockClient = mock(MinioClient.class);

        TestS3Template template = new TestS3Template();
        template.setTestClient(mockClient, "https://cdn.example.com");

        // 1. 显式传入 isPublic = true (配置了 publicUrl)
        String userUrl = template.preview("user-bucket", "user/2026-09-19/avatar.png", true);
        assertEquals("https://cdn.example.com/user-bucket/user/2026-09-19/avatar.png", userUrl);

        // 2. 业务名称 user 显式传入 isPublic = true
        String userShortUrl = template.preview("user", "/avatar.jpg", true);
        assertEquals("https://cdn.example.com/user/avatar.jpg", userShortUrl);

        // 3. 显式传入 isPublic = true (未配置 publicUrl，回退到 url:port)
        template.setPublicUrl(null);
        template.setUrl("115.190.181.243");
        template.setPort(3900);
        template.setSecure(false);
        String goodsUrl = template.preview("goods-bucket", "goods/sku1.jpg", true);
        assertEquals("http://115.190.181.243:3900/goods-bucket/goods/sku1.jpg", goodsUrl);

        // 验证对于 isPublic = true，底层的 getPresignedObjectUrl 从未被调用
        verify(mockClient, never()).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    @DisplayName("验证配置 Nginx publicUrl 时中文文件名被正确 URL 编码且移除 3900 端口")
    void testChineseCharactersUrlEncodedWithNginxPublicUrl() throws Exception {
        MinioClient mockClient = mock(MinioClient.class);
        TestS3Template template = new TestS3Template();
        template.setTestClient(mockClient, "http://115.190.181.243");

        String rawChineseKey = "user/2026-03-08/微信图片_2026-03-08_141653_644_1772977303808.jpg";
        String directUrl = template.preview("user-bucket", rawChineseKey, true);

        assertEquals("http://115.190.181.243/user-bucket/user/2026-03-08/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_2026-03-08_141653_644_1772977303808.jpg", directUrl);

        // 验证已编码的 URL 不会发生二次编码 (Double-encoding prevention)
        String alreadyEncodedKey = "user/2026-03-08/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_2026-03-08_141653_644_1772977303808.jpg";
        String secondCallUrl = template.preview("user-bucket", alreadyEncodedKey, true);
        assertEquals("http://115.190.181.243/user-bucket/user/2026-03-08/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_2026-03-08_141653_644_1772977303808.jpg", secondCallUrl);
    }

    @Test
    @DisplayName("验证当 isPublic 不是 true 时（false 或 null），不论什么桶一律生成带过期时效的 S3 预签名直链")
    void testDynamicIsPublicMatrix() throws Exception {
        MinioClient mockClient = mock(MinioClient.class);
        String presignedOriginUrl = "http://115.190.181.243:3900/user-bucket/avatar.png?X-Amz-Signature=exp123";
        when(mockClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn(presignedOriginUrl);

        TestS3Template template = new TestS3Template();
        template.setTestClient(mockClient, "https://cdn.example.com");

        // 1. 公共桶 (user-bucket) + isPublic=false -> 生成带时效签名的预签名链接
        String signedUserUrl = template.preview("user-bucket", "avatar.png", false);
        assertNotNull(signedUserUrl);
        assertTrue(signedUserUrl.startsWith("https://cdn.example.com/user-bucket/avatar.png?X-Amz-Signature=exp123"));
        verify(mockClient, times(1)).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));

        // 2. 公共桶 (user-bucket) + isPublic=null (未传) -> 依然生成带时效签名的预签名链接
        String defaultUserUrl = template.preview("user-bucket", "avatar.png", null);
        assertNotNull(defaultUserUrl);
        assertTrue(defaultUserUrl.startsWith("https://cdn.example.com/user-bucket/avatar.png?X-Amz-Signature=exp123"));
        verify(mockClient, times(2)).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));

        // 3. 只有显式 isPublic=true -> 生成免签持久直链
        String unexpiredUserUrl = template.preview("user-bucket", "avatar.png", true);
        assertEquals("https://cdn.example.com/user-bucket/avatar.png", unexpiredUserUrl);
        // getPresignedObjectUrl 仍然是 2 次
        verify(mockClient, times(2)).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));

        // 4. 私有桶 (finance-bucket) + isPublic=null -> 走预签名
        when(mockClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn("http://115.190.181.243:3900/finance-bucket/report.pdf?X-Amz-Signature=sig456");
        String defaultPrivateUrl = template.preview("finance-bucket", "report.pdf", null);
        assertNotNull(defaultPrivateUrl);
        assertTrue(defaultPrivateUrl.startsWith("https://cdn.example.com/finance-bucket/report.pdf?X-Amz-Signature=sig456"));
        verify(mockClient, times(3)).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }
}
