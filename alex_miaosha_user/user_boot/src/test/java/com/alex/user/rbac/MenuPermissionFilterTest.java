package com.alex.user.rbac;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.user.rbac.service.impl.UserPermissionContextServiceImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MenuPermissionFilterTest {

    public void testKeepsParentWithOnlyAllowedChildWhenParentPermissionDoesNotMatch() {
        MenuInfoVo addUser = menu("AddUser", "user:add");
        MenuInfoVo deleteUser = menu("DeleteUser", "user:delete");
        MenuInfoVo parent = menu("Users", "user:manage")
                .setChildren(Arrays.asList(addUser, deleteUser));

        List<MenuInfoVo> filtered = UserPermissionContextServiceImpl.filterMenusByPermissionCodes(
                Collections.singletonList(parent),
                Collections.singletonList("user:add")
        );

        assertEquals(1, filtered.size(), "parent should be retained when a child is visible");
        assertEquals("Users", filtered.get(0).getName(), "retained parent should be returned");
        assertEquals(1, filtered.get(0).getChildren().size(), "only matching children should remain");
        assertSame(addUser, filtered.get(0).getChildren().get(0), "matching child should be retained");
    }

    public void testDoesNotMutateOriginalChildrenWhenFiltering() {
        MenuInfoVo addUser = menu("AddUser", "user:add");
        MenuInfoVo deleteUser = menu("DeleteUser", "user:delete");
        List<MenuInfoVo> originalChildren = Arrays.asList(addUser, deleteUser);
        MenuInfoVo parent = menu("Users", null).setChildren(originalChildren);

        List<MenuInfoVo> filtered = UserPermissionContextServiceImpl.filterMenusByPermissionCodes(
                Collections.singletonList(parent),
                Collections.singletonList("user:add")
        );

        assertSame(originalChildren, parent.getChildren(), "original children list reference should not be replaced");
        assertEquals(2, parent.getChildren().size(), "original children should remain intact");
        assertEquals(1, filtered.get(0).getChildren().size(), "filtered copy should have only visible children");
    }

    public void testReturnsEmptyListWhenPermissionCodesAreEmpty() {
        MenuInfoVo parent = menu("Users", null)
                .setChildren(Collections.singletonList(menu("AddUser", "user:add")));

        List<MenuInfoVo> filtered = UserPermissionContextServiceImpl.filterMenusByPermissionCodes(
                Collections.singletonList(parent),
                Collections.emptyList()
        );

        assertEquals(0, filtered.size(), "empty permission codes should produce no visible menus");
    }

    public void testKeepsMenuWhenOwnPermissionMatches() {
        MenuInfoVo parent = menu("Users", "user:list");

        List<MenuInfoVo> filtered = UserPermissionContextServiceImpl.filterMenusByPermissionCodes(
                Collections.singletonList(parent),
                Collections.singletonList("user:list")
        );

        assertEquals(1, filtered.size(), "directly matching menu should be retained");
        assertEquals("Users", filtered.get(0).getName(), "retained menu should preserve menu data");
    }

    private static MenuInfoVo menu(String name, String permissionCode) {
        return new MenuInfoVo()
                .setName(name)
                .setPermissionCode(permissionCode);
    }

    private static void assertSame(Object expected, Object actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + ", expected: " + expected + ", actual: " + actual);
        }
    }
}
