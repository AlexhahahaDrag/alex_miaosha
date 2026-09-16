package com.alex.user.rbac;

import com.alex.api.user.handler.OrgSubtreeLookup;
import com.alex.user.config.MybatisPlusConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Lazy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 防止 MybatisPlusConfig 再次对 OrgSubtreeLookup 构造期强依赖，触发 sqlSessionFactory 环。
 */
public class MybatisPlusConfigOrgSubtreeLazyContractTest {

    @Test
    void mybatisPlusInterceptor_orgSubtreeLookupParam_mustBeLazy() throws Exception {
        Method method = null;
        for (Method m : MybatisPlusConfig.class.getDeclaredMethods()) {
            if ("mybatisPlusInterceptor".equals(m.getName())) {
                method = m;
                break;
            }
        }
        assertTrue(method != null, "mybatisPlusInterceptor bean method must exist");

        boolean foundLazyOrgSubtree = false;
        for (Parameter p : method.getParameters()) {
            if (OrgSubtreeLookup.class.equals(p.getType())
                    && p.getAnnotation(Lazy.class) != null) {
                foundLazyOrgSubtree = true;
            }
        }
        assertTrue(
                foundLazyOrgSubtree,
                "OrgSubtreeLookup param of mybatisPlusInterceptor must be annotated @Lazy");
    }
}
