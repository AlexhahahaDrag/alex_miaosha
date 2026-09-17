package com.alex.common.utils.ip;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证 IpUtils:
 * 1. DCL 延迟加载 ip2region.xdb，并发安全性与单例性
 * 2. getIpAddr 多级代理 Header 解析
 * 3. 城市信息解析有效性
 * 4. 浏览器与操作系统类型解析
 */
class IpUtilsLazyLoadingTest {

    @Test
    @DisplayName("验证 getIpAddr 在各种 HTTP Header 场景下正确解析 IP")
    void testGetIpAddrHeaderResolution() throws Exception {
        // 1. null 请求
        assertEquals("", IpUtils.getIpAddr(null), "null 请求应返回空字符串");

        // 2. x-forwarded-for 多 IP 代理
        MockHttpServletRequest req1 = new MockHttpServletRequest();
        req1.addHeader("x-forwarded-for", "203.0.113.195, 70.41.3.18, 150.172.238.178");
        assertEquals("203.0.113.195", IpUtils.getIpAddr(req1), "多级代理时应提取首个真实客户端IP");

        // 3. Proxy-Client-IP
        MockHttpServletRequest req2 = new MockHttpServletRequest();
        req2.addHeader("Proxy-Client-IP", "198.51.100.1");
        assertEquals("198.51.100.1", IpUtils.getIpAddr(req2));

        // 4. WL-Proxy-Client-IP
        MockHttpServletRequest req3 = new MockHttpServletRequest();
        req3.addHeader("WL-Proxy-Client-IP", "198.51.100.2");
        assertEquals("198.51.100.2", IpUtils.getIpAddr(req3));

        // 5. RemoteAddr
        MockHttpServletRequest req4 = new MockHttpServletRequest();
        req4.setRemoteAddr("10.0.0.1");
        assertEquals("10.0.0.1", IpUtils.getIpAddr(req4));
    }

    @Test
    @DisplayName("验证 Searcher DCL 懒加载机制及其在高并发下的单例安全性")
    void testLazySearcherConcurrentSingleton() throws Exception {
        int threadCount = 20;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        List<Future<Searcher>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(pool.submit(() -> {
                startLatch.await();
                return IpUtils.getSearcher();
            }));
        }

        // 20 个线程瞬间并发调用
        startLatch.countDown();
        endLatch.await(5, TimeUnit.SECONDS);

        Searcher firstInstance = null;
        for (Future<Searcher> future : futures) {
            Searcher s = future.get(5, TimeUnit.SECONDS);
            assertNotNull(s, "Searcher 实例不应为 null");
            if (firstInstance == null) {
                firstInstance = s;
            } else {
                assertSame(firstInstance, s, "并发加载必须返回同一个 Searcher 单例");
            }
        }
        pool.shutdown();
    }

    @Test
    @DisplayName("验证 getCityInfo 正确解析外部 IP 城市数据")
    void testGetCityInfoResolution() throws Exception {
        String cityInfo = IpUtils.getCityInfo("114.114.114.114");
        assertNotNull(cityInfo, "城市信息不应为 null");
        assertTrue(cityInfo.contains("中国"), "114.114.114.114 归属地应包含中国");

        String localInfo = IpUtils.getCityInfo("127.0.0.1");
        assertNotNull(localInfo, "本地 IP 也应有返回信息");
    }

    @Test
    @DisplayName("验证 getOsAndBrowserInfo 解析客户端操作系统与浏览器")
    void testGetOsAndBrowserInfo() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        Map<String, String> info = IpUtils.getOsAndBrowserInfo(request);
        assertEquals("Windows", info.get("OS"));
        assertTrue(info.get("BROWSER").contains("Chrome"));
    }
}
