package com.alex.user.rbac;

import com.alex.api.user.handler.DataPermissionHandlerImpl;
import com.alex.api.user.handler.OrgSubtreeLookup;
import com.alex.api.user.user.UserUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 防止再次引入双构造导致 Gateway 无参回退启动失败。
 */
public class DataPermissionHandlerCtorContractTest {

    @Test
    void mustHaveSinglePublicConstructor_userUtilsAndOrgSubtreeLookup() {
        Constructor<?>[] ctors = DataPermissionHandlerImpl.class.getConstructors();
        assertEquals(1, ctors.length, "must keep a single public ctor for Spring");
        Class<?>[] params = ctors[0].getParameterTypes();
        assertEquals(2, params.length);
        assertEquals(UserUtils.class, params[0]);
        assertEquals(OrgSubtreeLookup.class, params[1]);
    }
}
