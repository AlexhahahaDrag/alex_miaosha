package com.alex.user.rbac;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.user.rbac.service.impl.UserPermissionContextServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class MenuPermissionFilterTest {

    @Test
    public void testKeepsParentWithOnlyAllowedChildWhenParentPermissionDoesNotMatch() {
        MenuInfoVo addUser = menu("AddUser", "user:add");
        MenuInfoVo deleteUser = menu("DeleteUser", "user:delete");
        MenuInfoVo parent = menu("Users", "user:manage");
        parent.setChildren(Arrays.asList(addUser, deleteUser));

        List<MenuInfoVo> filtered = UserPermissionContextServiceImpl.filterMenusByPermissionCodes(
                Collections.singletonList(parent),
                Collections.singletonList("user:add")
        );

        assertEquals(1, filtered.size(), "parent should be retained when a child is visible");
        assertEquals("Users", filtered.get(0).getName(), "retained parent should be returned");
        assertEquals(1, filtered.get(0).getChildren().size(), "only matching children should remain");
        assertSame(addUser, filtered.get(0).getChildren().get(0), "matching child should be retained");
    }

    @Test
    public void testDoesNotMutateOriginalChildrenWhenFiltering() {
        MenuInfoVo addUser = menu("AddUser", "user:add");
        MenuInfoVo deleteUser = menu("DeleteUser", "user:delete");
        List<MenuInfoVo> originalChildren = Arrays.asList(addUser, deleteUser);
        MenuInfoVo parent = menu("Users", null);
        parent.setChildren(originalChildren);

        List<MenuInfoVo> filtered = UserPermissionContextServiceImpl.filterMenusByPermissionCodes(
                Collections.singletonList(parent),
                Collections.singletonList("user:add")
        );

        assertSame(originalChildren, parent.getChildren(), "original children list reference should not be replaced");
        assertEquals(2, parent.getChildren().size(), "original children should remain intact");
        assertEquals(1, filtered.get(0).getChildren().size(), "filtered copy should have only visible children");
    }

    @Test
    public void testReturnsEmptyListWhenPermissionCodesAreEmpty() {
        MenuInfoVo parent = menu("Users", null);
        parent.setChildren(Collections.singletonList(menu("AddUser", "user:add")));

        List<MenuInfoVo> filtered = UserPermissionContextServiceImpl.filterMenusByPermissionCodes(
                Collections.singletonList(parent),
                Collections.emptyList()
        );

        assertEquals(0, filtered.size(), "empty permission codes should produce no visible menus");
    }

    @Test
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
        MenuInfoVo m = new MenuInfoVo();
        m.setName(name);
        m.setPermissionCode(permissionCode);
        return m;
    }
}
