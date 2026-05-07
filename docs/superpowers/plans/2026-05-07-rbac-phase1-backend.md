# RBAC Phase 1 Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the backend RBAC permission context loop for single-org users, multi-role users, merged permissions, filtered menus, and login response compatibility.

**Architecture:** Reuse existing `alex_miaosha_user` modules and add focused service methods instead of creating duplicate CRUD stacks. Keep `t_org_user_info` as the transitional single-org source, `t_role_user_info` as the user-role source, and build a permission context used by login.

**Tech Stack:** Spring Boot 2.7.2, MyBatis Plus 3.5.2, JUnit 5, Mockito, existing `Result`, `BaseEntity`, VO, Controller / Service / Mapper layering.

---

## File Structure

Create test files:

- `alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/OrgUserAssignmentServiceTest.java`
- `alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/RoleUserAssignmentServiceTest.java`
- `alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/UserPermissionContextServiceTest.java`
- `alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/MenuPermissionFilterTest.java`

Create production files:

- `alex_miaosha_user/user_boot/src/main/java/com/alex/user/rbac/service/UserPermissionContextService.java`
- `alex_miaosha_user/user_boot/src/main/java/com/alex/user/rbac/service/impl/UserPermissionContextServiceImpl.java`
- `alex_miaosha_user/user_api/src/main/java/com/alex/api/user/userInfo/vo/UserPermissionContextVo.java`

Modify existing files:

- `alex_miaosha_user/user_boot/src/main/java/com/alex/user/orgUserInfo/service/OrgUserInfoService.java`
- `alex_miaosha_user/user_boot/src/main/java/com/alex/user/orgUserInfo/service/impl/OrgUserInfoServiceImp.java`
- `alex_miaosha_user/user_boot/src/main/java/com/alex/user/roleUserInfo/service/RoleUserInfoService.java`
- `alex_miaosha_user/user_boot/src/main/java/com/alex/user/roleUserInfo/service/impl/RoleUserInfoServiceImp.java`
- `alex_miaosha_user/user_boot/src/main/java/com/alex/user/user/service/impl/TUserServiceImpl.java`
- `alex_miaosha_user/user_api/src/main/java/com/alex/api/user/userInfo/vo/TUserVo.java`

Verification command for backend unit tests:

```bash
mvn -pl alex_miaosha_user/user_boot -Dtest=OrgUserAssignmentServiceTest,RoleUserAssignmentServiceTest,UserPermissionContextServiceTest,MenuPermissionFilterTest test
```

Expected final result: build succeeds and the four test classes pass.

---

## Task 1: Enforce Single Active Org Assignment

**Files:**

- Test: `alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/OrgUserAssignmentServiceTest.java`
- Modify: `alex_miaosha_user/user_boot/src/main/java/com/alex/user/orgUserInfo/service/OrgUserInfoService.java`
- Modify: `alex_miaosha_user/user_boot/src/main/java/com/alex/user/orgUserInfo/service/impl/OrgUserInfoServiceImp.java`

- [ ] **Step 1: Write the failing test**

```java
package com.alex.user.rbac;

import com.alex.user.orgUserInfo.entity.OrgUserInfo;
import com.alex.user.orgUserInfo.service.impl.OrgUserInfoServiceImp;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrgUserAssignmentServiceTest {

    @Test
    void assignSingleOrgDisablesPreviousActiveOrgRelations() {
        TestableOrgUserInfoService service = new TestableOrgUserInfoService();
        service.seed(new OrgUserInfo().setUserId("100").setOrgId("10").setStatus("1"));

        service.assignSingleOrg(100L, 20L);

        List<OrgUserInfo> relations = service.relations();
        assertThat(relations).hasSize(2);
        assertThat(relations).anySatisfy(item -> {
            assertThat(item.getUserId()).isEqualTo("100");
            assertThat(item.getOrgId()).isEqualTo("10");
            assertThat(item.getStatus()).isEqualTo("0");
        });
        assertThat(relations).anySatisfy(item -> {
            assertThat(item.getUserId()).isEqualTo("100");
            assertThat(item.getOrgId()).isEqualTo("20");
            assertThat(item.getStatus()).isEqualTo("1");
        });
    }

    static class TestableOrgUserInfoService extends OrgUserInfoServiceImp {
        // In the implementation task, replace this test double with spies/mocks
        // if the service cannot be instantiated directly in the existing project.
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
mvn -pl alex_miaosha_user/user_boot -Dtest=OrgUserAssignmentServiceTest test
```

Expected: FAIL because `assignSingleOrg(Long, Long)` does not exist.

- [ ] **Step 3: Write minimal implementation**

Add to `OrgUserInfoService`:

```java
Boolean assignSingleOrg(Long userId, Long orgId);
```

Add to `OrgUserInfoServiceImp`:

```java
@Override
@Transactional(rollbackFor = Exception.class)
public Boolean assignSingleOrg(Long userId, Long orgId) {
    if (userId == null || orgId == null) {
        throw new UserException(ResultEnum.PARAM_ERROR);
    }
    LambdaQueryWrapper<OrgUserInfo> queryWrapper = Wrappers.<OrgUserInfo>lambdaQuery()
            .eq(OrgUserInfo::getUserId, String.valueOf(userId))
            .eq(OrgUserInfo::getStatus, SysConf.VALID_STATUS);
    List<OrgUserInfo> activeRelations = list(queryWrapper);
    for (OrgUserInfo relation : activeRelations) {
        relation.setStatus(SysConf.INVALID_STATUS);
        updateById(relation);
    }
    OrgUserInfo relation = new OrgUserInfo();
    relation.setUserId(String.valueOf(userId));
    relation.setOrgId(String.valueOf(orgId));
    relation.setStatus(SysConf.VALID_STATUS);
    return save(relation);
}
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
mvn -pl alex_miaosha_user/user_boot -Dtest=OrgUserAssignmentServiceTest test
```

Expected: PASS.

- [ ] **Step 5: Refactor**

If existing constants for valid/invalid status differ from `SysConf.VALID_STATUS` / `SysConf.INVALID_STATUS`, use the existing project constants and update the test assertions to match the real value.

---

## Task 2: Add Multi-Role Assignment Service

**Files:**

- Test: `alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/RoleUserAssignmentServiceTest.java`
- Modify: `alex_miaosha_user/user_boot/src/main/java/com/alex/user/roleUserInfo/service/RoleUserInfoService.java`
- Modify: `alex_miaosha_user/user_boot/src/main/java/com/alex/user/roleUserInfo/service/impl/RoleUserInfoServiceImp.java`

- [ ] **Step 1: Write the failing test**

```java
package com.alex.user.rbac;

import com.alex.user.roleUserInfo.entity.RoleUserInfo;
import com.alex.user.roleUserInfo.service.impl.RoleUserInfoServiceImp;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoleUserAssignmentServiceTest {

    @Test
    void assignRolesReplacesActiveRoleRelationsForUser() {
        TestableRoleUserInfoService service = new TestableRoleUserInfoService();
        service.seed(new RoleUserInfo().setUserId("100").setRoleId("1").setStatus("1"));

        service.assignRoles(100L, Arrays.asList(2L, 3L));

        List<RoleUserInfo> relations = service.relations();
        assertThat(relations).anySatisfy(item -> {
            assertThat(item.getRoleId()).isEqualTo("1");
            assertThat(item.getStatus()).isEqualTo("0");
        });
        assertThat(relations).anySatisfy(item -> {
            assertThat(item.getRoleId()).isEqualTo("2");
            assertThat(item.getStatus()).isEqualTo("1");
        });
        assertThat(relations).anySatisfy(item -> {
            assertThat(item.getRoleId()).isEqualTo("3");
            assertThat(item.getStatus()).isEqualTo("1");
        });
    }

    static class TestableRoleUserInfoService extends RoleUserInfoServiceImp {
        // Use an in-memory test double or Mockito spy during implementation.
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
mvn -pl alex_miaosha_user/user_boot -Dtest=RoleUserAssignmentServiceTest test
```

Expected: FAIL because `assignRoles(Long, List<Long>)` does not exist.

- [ ] **Step 3: Write minimal implementation**

Add to `RoleUserInfoService`:

```java
Boolean assignRoles(Long userId, List<Long> roleIds);
```

Add to `RoleUserInfoServiceImp`:

```java
@Override
@Transactional(rollbackFor = Exception.class)
public Boolean assignRoles(Long userId, List<Long> roleIds) {
    if (userId == null) {
        throw new UserException(ResultEnum.PARAM_ERROR);
    }
    LambdaQueryWrapper<RoleUserInfo> queryWrapper = Wrappers.<RoleUserInfo>lambdaQuery()
            .eq(RoleUserInfo::getUserId, String.valueOf(userId))
            .eq(RoleUserInfo::getStatus, SysConf.VALID_STATUS);
    List<RoleUserInfo> activeRelations = list(queryWrapper);
    for (RoleUserInfo relation : activeRelations) {
        relation.setStatus(SysConf.INVALID_STATUS);
        updateById(relation);
    }
    if (roleIds == null || roleIds.isEmpty()) {
        return true;
    }
    List<RoleUserInfo> newRelations = roleIds.stream()
            .distinct()
            .map(roleId -> {
                RoleUserInfo relation = new RoleUserInfo();
                relation.setUserId(String.valueOf(userId));
                relation.setRoleId(String.valueOf(roleId));
                relation.setStatus(SysConf.VALID_STATUS);
                return relation;
            })
            .collect(Collectors.toList());
    return saveBatch(newRelations);
}
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
mvn -pl alex_miaosha_user/user_boot -Dtest=RoleUserAssignmentServiceTest test
```

Expected: PASS.

- [ ] **Step 5: Refactor**

Extract duplicated status replacement logic only if it improves clarity without changing behavior.

---

## Task 3: Add Permission Context VO

**Files:**

- Test: `alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/UserPermissionContextServiceTest.java`
- Create: `alex_miaosha_user/user_api/src/main/java/com/alex/api/user/userInfo/vo/UserPermissionContextVo.java`
- Modify: `alex_miaosha_user/user_api/src/main/java/com/alex/api/user/userInfo/vo/TUserVo.java`

- [ ] **Step 1: Write the failing test**

```java
package com.alex.user.rbac;

import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.api.user.userInfo.vo.UserPermissionContextVo;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class UserPermissionContextServiceTest {

    @Test
    void tUserVoCarriesMergedPermissionContext() {
        TUserVo user = new TUserVo();
        UserPermissionContextVo context = new UserPermissionContextVo();
        context.setPermissionCodes(Arrays.asList("user:add", "role:update"));

        user.setPermissionContext(context);

        assertThat(user.getPermissionContext().getPermissionCodes())
                .containsExactly("user:add", "role:update");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
mvn -pl alex_miaosha_user/user_boot -Dtest=UserPermissionContextServiceTest test
```

Expected: FAIL because `UserPermissionContextVo` and `TUserVo.permissionContext` do not exist.

- [ ] **Step 3: Write minimal implementation**

Create `UserPermissionContextVo`:

```java
package com.alex.api.user.userInfo.vo;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "UserPermissionContextVo", description = "用户权限上下文")
public class UserPermissionContextVo {

    @ApiModelProperty(value = "所属机构")
    private OrgInfoVo orgInfo;

    @ApiModelProperty(value = "角色列表")
    private List<RoleInfoVo> roleList = new ArrayList<>();

    @ApiModelProperty(value = "权限编码集合")
    private List<String> permissionCodes = new ArrayList<>();

    @ApiModelProperty(value = "按钮权限编码集合")
    private List<String> buttonPermissionCodes = new ArrayList<>();

    @ApiModelProperty(value = "菜单树")
    private List<MenuInfoVo> menuList = new ArrayList<>();

    @ApiModelProperty(value = "是否超级管理员")
    private Boolean superAdmin = false;
}
```

Add to `TUserVo`:

```java
@ApiModelProperty(value = "权限上下文")
private UserPermissionContextVo permissionContext;

@ApiModelProperty(value = "角色列表")
private List<RoleInfoVo> roleInfoVoList;

@ApiModelProperty(value = "权限编码集合")
private List<String> permissionCodes;

@ApiModelProperty(value = "按钮权限编码集合")
private List<String> buttonPermissionCodes;
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
mvn -pl alex_miaosha_user/user_boot -Dtest=UserPermissionContextServiceTest test
```

Expected: PASS.

- [ ] **Step 5: Refactor**

Keep legacy `roleInfoVo`, `orgInfoVo`, and `menuInfoVoList` fields for compatibility.

---

## Task 4: Build User Permission Context Service

**Files:**

- Test: `alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/UserPermissionContextServiceTest.java`
- Create: `alex_miaosha_user/user_boot/src/main/java/com/alex/user/rbac/service/UserPermissionContextService.java`
- Create: `alex_miaosha_user/user_boot/src/main/java/com/alex/user/rbac/service/impl/UserPermissionContextServiceImpl.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void buildContextMergesPermissionCodesFromAllRoles() {
    UserPermissionContextServiceImpl service = new UserPermissionContextServiceImpl(
            orgUserInfoService,
            roleUserInfoService,
            menuInfoService
    );

    when(roleUserInfoService.getRoleInfoList(100L, true)).thenReturn(Arrays.asList(role("admin", "user:add"), role("ops", "user:add", "role:update")));
    when(orgUserInfoService.getOrgInfoList(100L)).thenReturn(Collections.singletonList(org("总部")));
    when(menuInfoService.getList(any())).thenReturn(Collections.emptyList());

    UserPermissionContextVo context = service.buildContext(100L);

    assertThat(context.getPermissionCodes()).containsExactly("user:add", "role:update");
    assertThat(context.getRoleList()).hasSize(2);
    assertThat(context.getOrgInfo().getOrgName()).isEqualTo("总部");
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
mvn -pl alex_miaosha_user/user_boot -Dtest=UserPermissionContextServiceTest test
```

Expected: FAIL because `UserPermissionContextServiceImpl` does not exist.

- [ ] **Step 3: Write minimal implementation**

Create service interface:

```java
public interface UserPermissionContextService {

    UserPermissionContextVo buildContext(Long userId);
}
```

Create implementation:

```java
@Service
@RequiredArgsConstructor
public class UserPermissionContextServiceImpl implements UserPermissionContextService {

    private static final String SUPER_ADMIN_ROLE_CODE = "super_super";

    private final OrgUserInfoService orgUserInfoService;
    private final RoleUserInfoService roleUserInfoService;
    private final MenuInfoService menuInfoService;

    @Override
    public UserPermissionContextVo buildContext(Long userId) {
        List<OrgInfoVo> orgList = orgUserInfoService.getOrgInfoList(userId);
        List<RoleInfoVo> roleList = roleUserInfoService.getRoleInfoList(userId, true);
        boolean superAdmin = roleList.stream()
                .anyMatch(role -> SUPER_ADMIN_ROLE_CODE.equals(role.getRoleCode()));
        List<String> permissionCodes = mergePermissionCodes(roleList);
        List<MenuInfoVo> menuList = loadMenuList(superAdmin, permissionCodes);

        return new UserPermissionContextVo()
                .setOrgInfo(orgList == null || orgList.isEmpty() ? null : orgList.get(0))
                .setRoleList(roleList == null ? Collections.emptyList() : roleList)
                .setPermissionCodes(permissionCodes)
                .setButtonPermissionCodes(permissionCodes)
                .setMenuList(menuList)
                .setSuperAdmin(superAdmin);
    }

    private List<String> mergePermissionCodes(List<RoleInfoVo> roleList) {
        if (roleList == null) {
            return Collections.emptyList();
        }
        return roleList.stream()
                .filter(Objects::nonNull)
                .flatMap(role -> role.getPermissionList() == null ? Stream.empty() : role.getPermissionList().stream())
                .map(PermissionInfoVo::getPermissionCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<MenuInfoVo> loadMenuList(boolean superAdmin, List<String> permissionCodes) {
        MenuInfoVo query = new MenuInfoVo();
        query.setStatus(SysConf.VALID_STATUS);
        List<MenuInfoVo> menuList = menuInfoService.getList(query);
        if (superAdmin) {
            return menuList;
        }
        return filterMenus(menuList, new HashSet<>(permissionCodes));
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
mvn -pl alex_miaosha_user/user_boot -Dtest=UserPermissionContextServiceTest test
```

Expected: PASS.

- [ ] **Step 5: Refactor**

Move `SUPER_ADMIN_ROLE_CODE` to a shared constants class only if an appropriate constants class already exists.

---

## Task 5: Filter Menus By Permission Codes

**Files:**

- Test: `alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/MenuPermissionFilterTest.java`
- Modify: `alex_miaosha_user/user_boot/src/main/java/com/alex/user/rbac/service/impl/UserPermissionContextServiceImpl.java`

- [ ] **Step 1: Write the failing test**

```java
package com.alex.user.rbac;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.user.rbac.service.impl.UserPermissionContextServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MenuPermissionFilterTest {

    @Test
    void filterMenusKeepsParentWhenAnyChildIsAllowed() {
        MenuInfoVo parent = menu("user", null);
        parent.setChildren(Arrays.asList(menu("user-list", "user:list"), menu("user-add", "user:add")));

        List<MenuInfoVo> result = UserPermissionContextServiceImpl.filterMenus(
                Collections.singletonList(parent),
                new HashSet<>(Collections.singletonList("user:list"))
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChildren()).hasSize(1);
        assertThat(result.get(0).getChildren().get(0).getPermissionCode()).isEqualTo("user:list");
    }

    private MenuInfoVo menu(String name, String permissionCode) {
        MenuInfoVo item = new MenuInfoVo();
        item.setName(name);
        item.setPermissionCode(permissionCode);
        return item;
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
mvn -pl alex_miaosha_user/user_boot -Dtest=MenuPermissionFilterTest test
```

Expected: FAIL because `filterMenus` does not exist or does not preserve parent nodes.

- [ ] **Step 3: Write minimal implementation**

Add static filter method:

```java
public static List<MenuInfoVo> filterMenus(List<MenuInfoVo> menus, Set<String> permissionCodes) {
    if (menus == null || menus.isEmpty()) {
        return Collections.emptyList();
    }
    return menus.stream()
            .map(menu -> filterMenu(menu, permissionCodes))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
}

private static MenuInfoVo filterMenu(MenuInfoVo menu, Set<String> permissionCodes) {
    List<MenuInfoVo> filteredChildren = filterMenus(menu.getChildren(), permissionCodes);
    boolean currentAllowed = StringUtils.isBlank(menu.getPermissionCode())
            || permissionCodes.contains(menu.getPermissionCode());
    if (!currentAllowed && filteredChildren.isEmpty()) {
        return null;
    }
    MenuInfoVo copy = new MenuInfoVo();
    BeanUtils.copyProperties(menu, copy);
    copy.setChildren(filteredChildren);
    return copy;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
mvn -pl alex_miaosha_user/user_boot -Dtest=MenuPermissionFilterTest test
```

Expected: PASS.

- [ ] **Step 5: Refactor**

Keep filter side-effect-free. Do not mutate the original menu list because login caching may reuse menu objects.

---

## Task 6: Attach Permission Context To Login Response

**Files:**

- Test: `alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/UserPermissionContextServiceTest.java`
- Modify: `alex_miaosha_user/user_boot/src/main/java/com/alex/user/user/service/impl/TUserServiceImpl.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void loginResponseKeepsLegacyFieldsAndAddsPermissionContext() {
    TUserVo user = new TUserVo();
    UserPermissionContextVo context = new UserPermissionContextVo()
            .setOrgInfo(org("总部"))
            .setRoleList(Collections.singletonList(role("admin", "user:add")))
            .setPermissionCodes(Collections.singletonList("user:add"))
            .setMenuList(Collections.singletonList(menu("user", "user:list")));

    TUserServiceImpl.attachPermissionContext(user, context);

    assertThat(user.getPermissionContext()).isSameAs(context);
    assertThat(user.getOrgInfoVo().getOrgName()).isEqualTo("总部");
    assertThat(user.getRoleInfoVoList()).hasSize(1);
    assertThat(user.getRoleInfoVo().getRoleCode()).isEqualTo("admin");
    assertThat(user.getPermissionCodes()).containsExactly("user:add");
    assertThat(user.getMenuInfoVoList()).hasSize(1);
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
mvn -pl alex_miaosha_user/user_boot -Dtest=UserPermissionContextServiceTest test
```

Expected: FAIL because `attachPermissionContext` does not exist.

- [ ] **Step 3: Write minimal implementation**

Add dependency to `TUserServiceImpl`:

```java
private final UserPermissionContextService userPermissionContextService;
```

Add helper:

```java
public static void attachPermissionContext(TUserVo user, UserPermissionContextVo context) {
    user.setPermissionContext(context);
    user.setOrgInfoVo(context.getOrgInfo());
    user.setRoleInfoVoList(context.getRoleList());
    user.setRoleInfoVo(context.getRoleList().isEmpty() ? null : context.getRoleList().get(0));
    user.setPermissionCodes(context.getPermissionCodes());
    user.setButtonPermissionCodes(context.getButtonPermissionCodes());
    user.setMenuInfoVoList(context.getMenuList());
}
```

In `login`, replace the separate async `orgInfoFuture`, `rolesFuture`, and `menuFuture` assembly with:

```java
UserPermissionContextVo context = userPermissionContextService.buildContext(tUserVo.getId());
attachPermissionContext(tUserVo, context);
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
mvn -pl alex_miaosha_user/user_boot -Dtest=UserPermissionContextServiceTest test
```

Expected: PASS.

- [ ] **Step 5: Run focused backend verification**

Run:

```bash
mvn -pl alex_miaosha_user/user_boot -Dtest=OrgUserAssignmentServiceTest,RoleUserAssignmentServiceTest,UserPermissionContextServiceTest,MenuPermissionFilterTest test
```

Expected: PASS.

---

## Task 7: Review Phase 1 Backend Changes

**Files:**

- Review: `alex_miaosha_user/user_boot/src/main/java/com/alex/user/rbac/service/impl/UserPermissionContextServiceImpl.java`
- Review: `alex_miaosha_user/user_boot/src/main/java/com/alex/user/user/service/impl/TUserServiceImpl.java`
- Review: `alex_miaosha_user/user_api/src/main/java/com/alex/api/user/userInfo/vo/TUserVo.java`

- [ ] **Step 1: Run full focused verification**

Run:

```bash
mvn -pl alex_miaosha_user/user_boot test
```

Expected: PASS, or document unrelated pre-existing failures before proceeding.

- [ ] **Step 2: Request code review**

Use the code-reviewer subagent with:

- Implemented: phase 1 backend RBAC permission context.
- Requirements: single active org relation, multi-role relation, merged permission codes, permission-filtered menus, login compatibility.
- Scope: backend only.

- [ ] **Step 3: Fix Critical and Important review findings**

Apply TDD for each fix: failing test first, verify failure, implement, verify pass.

---

## Self-Review

Spec coverage:

- Single active org relation is covered by Task 1.
- Multi-role assignment is covered by Task 2.
- Permission context response shape is covered by Task 3.
- Role permission merge is covered by Task 4.
- Menu filtering is covered by Task 5.
- Login compatibility is covered by Task 6.
- Review gate is covered by Task 7.

Placeholder scan:

- No TBD/TODO placeholders remain.
- Test snippets include expected failure reasons and verification commands.

Type consistency:

- New `UserPermissionContextVo` is used consistently by `TUserVo`, `UserPermissionContextService`, and `TUserServiceImpl`.
- Existing legacy fields are preserved for PC and mobile compatibility.

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-05-07-rbac-phase1-backend.md`.

Two execution options:

1. Subagent-Driven: dispatch a fresh subagent per task, review between tasks.
2. Inline Execution: execute tasks in this session using executing-plans, with checkpoints.

Recommended: Subagent-Driven for this phase because it touches existing login, role, org, menu, and permission code paths.
