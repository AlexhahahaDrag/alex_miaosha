package com.alex.gateway.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewayWebSecurityConfigTest {

    @Test
    void whiteList_containsAmAiPath() throws Exception {
        List<String> whiteList = readWhiteList();
        assertTrue(
                whiteList.contains("/am-ai/**"),
                "whiteList must contain /am-ai/** (peer of /am-user/**); actual: " + whiteList);
        assertTrue(
                whiteList.contains("/am-user/**"),
                "whiteList must contain /am-user/**; actual: " + whiteList);
    }

    private static List<String> readWhiteList() throws Exception {
        Field field = GatewayWebSecurityConfig.class.getDeclaredField("whiteList");
        field.setAccessible(true);
        String[] entries = (String[]) field.get(null);
        return Arrays.asList(entries);
    }
}
