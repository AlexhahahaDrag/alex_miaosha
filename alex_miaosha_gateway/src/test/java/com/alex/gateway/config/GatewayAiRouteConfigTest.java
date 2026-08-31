package com.alex.gateway.config;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GatewayAiRouteConfigTest {

    @Test
    void applicationDev_hasAlexAiRouteAndHttpClientTimeout() {
        Map<String, Object> root = loadYaml("application-dev.yaml");
        Map<String, Object> gateway = nest(root, "spring", "cloud", "gateway");
        assertNotNull(gateway, "spring.cloud.gateway missing");

        Map<String, Object> httpclient = castMap(gateway.get("httpclient"));
        assertNotNull(httpclient, "httpclient missing");
        assertEquals("180s", String.valueOf(httpclient.get("response-timeout")));

        List<Map<String, Object>> routes = castList(gateway.get("routes"));
        assertNotNull(routes);
        Map<String, Object> ai = routes.stream()
                .filter(r -> "alex-ai".equals(r.get("id")))
                .findFirst()
                .orElse(null);
        assertNotNull(ai, "route id=alex-ai missing");
        assertEquals("lb://alex-ai-dev", ai.get("uri"));
        assertTrue(predicatesContainPath(ai, "/am-ai/**"));
        assertTrue(filtersContainStripPrefix1(ai));
    }

    @Test
    void applicationTest_hasAlexAiRoute() {
        assertProfileRoute("application-test.yaml", "lb://alex-ai-test");
    }

    @Test
    void applicationProd_hasAlexAiRoute() {
        assertProfileRoute("application-prod.yaml", "lb://alex-ai-prod");
    }

    private void assertProfileRoute(String resource, String expectedUri) {
        Map<String, Object> root = loadYaml(resource);
        Map<String, Object> gateway = nest(root, "spring", "cloud", "gateway");
        List<Map<String, Object>> routes = castList(gateway.get("routes"));
        Map<String, Object> ai = routes.stream()
                .filter(r -> "alex-ai".equals(r.get("id")))
                .findFirst()
                .orElse(null);
        assertNotNull(ai, resource + " missing alex-ai");
        assertEquals(expectedUri, ai.get("uri"));
        assertTrue(predicatesContainPath(ai, "/am-ai/**"));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadYaml(String name) {
        InputStream in = GatewayAiRouteConfigTest.class.getClassLoader().getResourceAsStream(name);
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
            if (p instanceof Map) {
                Map<String, Object> m = (Map<String, Object>) p;
                if ("Path".equals(String.valueOf(m.get("name")))
                        && String.valueOf(m.get("args")).contains(path)) {
                    return true;
                }
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
            if (f instanceof Map) {
                Map<String, Object> m = (Map<String, Object>) f;
                if ("StripPrefix".equals(String.valueOf(m.get("name")))) {
                    return true;
                }
            }
        }
        return false;
    }
}
