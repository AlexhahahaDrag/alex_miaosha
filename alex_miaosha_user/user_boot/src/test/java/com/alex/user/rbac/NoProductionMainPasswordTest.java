package com.alex.user.rbac;

import com.alex.user.user.service.impl.TUserServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;

class NoProductionMainPasswordTest {

    @Test
    void tUserServiceImpl_must_not_declare_main() throws Exception {
        var methods = TUserServiceImpl.class.getDeclaredMethods();
        boolean hasMain = Arrays.stream(methods)
                .anyMatch(m -> "main".equals(m.getName()) && m.getParameterCount() == 1);
        assertFalse(hasMain, "RBAC-BE-USER-001: production main that prints password must be removed");
    }
}
