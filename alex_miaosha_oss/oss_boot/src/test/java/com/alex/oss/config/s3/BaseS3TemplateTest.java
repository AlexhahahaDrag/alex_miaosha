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
}
