package com.alex.gateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 网关多微服务动态路由与环境配置契约测试
 * 锁定 user / finance / oss / product / ai 5 大微服务的前缀剥离、路径断言与超时
 */
class GatewayFullRouteConfigIT {

    private static final String[] PROFILES = {"application-dev.yaml", "application-test.yaml", "application-prod.yaml"};
    private static final String[] MICROSERVICES = {"user", "finance", "oss", "product", "ai"};

    @Test
    @DisplayName("开发环境：验证 5 大微服务全部配置了 StripPrefix=1 与路径断言")
    void applicationDev_verifyAllFiveMicroserviceRoutes() {
        verifyProfileRoutes("application-dev.yaml", "dev");
    }

    @Test
    @DisplayName("测试环境：验证 5 大微服务全部配置了 StripPrefix=1 与路径断言")
    void applicationTest_verifyAllFiveMicroserviceRoutes() {
        verifyProfileRoutes("application-test.yaml", "test");
    }

    @Test
    @DisplayName("生产环境：验证 5 大微服务全部配置了 StripPrefix=1 与路径断言")
    void applicationProd_verifyAllFiveMicroserviceRoutes() {
        verifyProfileRoutes("application-prod.yaml", "prod");
    }

    @Test
    @DisplayName("超时契约：所有环境均需配置 180s 响应超时（服务 AI 与大数据导出流）及 5000ms 连接超时")
    void allProfiles_verifyHttpClientTimeouts() {
        for (String profileFile : PROFILES) {
            Map<String, Object> root = loadYaml(profileFile);
            Map<String, Object> gateway = nest(root, "spring", "cloud", "gateway");
            assertNotNull(gateway, profileFile + " missing spring.cloud.gateway");

            Map<String, Object> httpclient = castMap(gateway.get("httpclient"));
            assertNotNull(httpclient, profileFile + " missing httpclient config");

            assertEquals(5000, Integer.parseInt(String.valueOf(httpclient.get("connect-timeout"))),
                    profileFile + " connect-timeout must be 5000ms");
            assertEquals("180s", String.valueOf(httpclient.get("response-timeout")),
                    profileFile + " response-timeout must be 180s");
        }
    }

    @Test
    @DisplayName("服务发现配置：所有环境均需启用 locator 动态服务发现与小写服务名")
    void allProfiles_verifyDiscoveryLocator() {
        for (String profileFile : PROFILES) {
            Map<String, Object> root = loadYaml(profileFile);
            Map<String, Object> locator = nest(root, "spring", "cloud", "gateway", "discovery", "locator");
            assertNotNull(locator, profileFile + " missing discovery.locator");
            assertEquals(true, locator.get("enabled"), profileFile + " discovery.locator.enabled must be true");
            assertEquals(true, locator.get("lower-case-service-id"),
                    profileFile + " discovery.locator.lower-case-service-id must be true");
        }
    }

    private void verifyProfileRoutes(String resourceFile, String env) {
        Map<String, Object> root = loadYaml(resourceFile);
        Map<String, Object> gateway = nest(root, "spring", "cloud", "gateway");
        assertNotNull(gateway, resourceFile + " missing spring.cloud.gateway");

        List<Map<String, Object>> routes = castList(gateway.get("routes"));
        assertNotNull(routes, resourceFile + " missing routes list");

        for (String service : MICROSERVICES) {
            String routeId = "alex-" + service;
            String expectedUri = "lb://alex-" + service + "-" + env;
            String expectedPath = "/am-" + service + "/**";

            Map<String, Object> route = routes.stream()
                    .filter(r -> routeId.equals(r.get("id")))
                    .findFirst()
                    .orElse(null);

            assertNotNull(route, resourceFile + " missing route id=" + routeId);
            assertEquals(expectedUri, route.get("uri"), resourceFile + " uri mismatch for " + routeId);
            assertTrue(predicatesContainPath(route, expectedPath),
                    resourceFile + " route " + routeId + " must have predicate Path=" + expectedPath);
            assertTrue(filtersContainStripPrefix1(route),
                    resourceFile + " route " + routeId + " must contain filter StripPrefix=1");
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadYaml(String name) {
        InputStream in = GatewayFullRouteConfigIT.class.getClassLoader().getResourceAsStream(name);
        assertNotNull(in, "classpath resource missing: " + name);
        Object loaded = new Yaml().load(in);
        assertTrue(loaded instanceof Map);
        return (Map<String, Object>) loaded;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> nest(Map<String, Object> root, String... keys) {
        Map<String, Object> cur = root;
        for (String k : keys) {
            Object next = cur.get(k);
            if (!(next instanceof Map)) {
                return null;
            }
            cur = (Map<String, Object>) next;
        }
        return cur;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object o) {
        return o instanceof Map ? (Map<String, Object>) o : null;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> castList(Object o) {
        return o instanceof List ? (List<Map<String, Object>>) o : null;
    }

    @SuppressWarnings("unchecked")
    private static boolean predicatesContainPath(Map<String, Object> route, String path) {
        Object preds = route.get("predicates");
        if (!(preds instanceof List)) {
            return false;
        }
        for (Object p : (List<?>) preds) {
            if (p instanceof String && ((String) p).contains("Path=" + path)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static boolean filtersContainStripPrefix1(Map<String, Object> route) {
        Object filters = route.get("filters");
        if (!(filters instanceof List)) {
            return false;
        }
        for (Object f : (List<?>) filters) {
            if (f instanceof String && ((String) f).contains("StripPrefix=1")) {
                return true;
            }
        }
        return false;
    }
}
