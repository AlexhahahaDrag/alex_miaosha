# RBAC P1 Wave1 Design: User Delete Cleanup + Org/Role Data Permission

Date: 2026-08-03  
Status: Implemented  
Scope: backend `alex_miaosha_user` only  
Packaging: P1 plan **R** — Wave1 = A + B; Wave2 (E/D/C) separate

Confirmed choices:

| Item | Choice |
|------|--------|
| Delete user | **A1** — soft-delete user + invalidate org/role + clear permission_context + kick session |
| Data permission | **B1** — reuse handler roles (super / admin* / user); Org needs ORG_ID scope |
| Order | A → B |

---

## 1. Goals

1. Deleting a user must not leave active org/role bindings, live permission cache, or usable login tokens.
2. Org and Role list pages must respect data permission like user list, without breaking super-admin full access.
3. Keep changes minimal: no mobile, no Wave2 UI work.

## 2. Non-goals

- Frontend ID stringization / batch-delete confirm / user filter fields (Wave2)
- Data permission on Org/Role detail write APIs beyond `getPage` (follow-up)
- Cascading delete of `t_role_permission_info` when deleting roles
- Full org-tree visibility for admins (only own `orgId` in Wave1)

---

## 3. A — User delete cleanup

### 3.1 Entry

`TUserServiceImpl.deleteTUser(String ids)`

### 3.2 Behavior (per userId in `ids`)

Transactional (`@Transactional(rollbackFor = Exception.class)` on `deleteTUser`):

1. Soft-delete users via existing `tUserMapper.deleteBatchIds` (`@TableLogic`).
2. Invalidate active org assignments:
   - `OrgUserInfo` where `userId = id` and `status = VALID` → `status = INVALID` (`updateById`).
3. Invalidate active role assignments:
   - `RoleUserInfo` where `userId = id` and `status = VALID` → `status = INVALID`.
4. Delete Redis `permission_context:{userId}` with `LoginKey.loginKey` (same as sync path).
5. Kick login session(s):
   - Resolve active login records for the user (prefer `TUserLogin` / online mapping already used by logout).
   - For each session: delete `LoginKey.loginUuid` (tokenId) and `LoginKey.loginToken` (barToken), matching `logout` cleanup.
   - Cache/session cleanup failures: log and continue for that user after DB work committed only if we keep “DB first then best-effort Redis”; **preferred:** Redis cleanup inside same service method after DB updates, log errors, do not roll back soft-delete solely for Redis miss (document: DB consistency > cache best-effort).

**Batch policy:** process all ids in one transaction for DB steps; empty `ids` → return true (current behavior).

### 3.3 Tests

Add focused tests (Testable style or Mockito — match nearby rbac tests):

- Active org/role rows become invalid after delete.
- `permission_context` delete invoked per userId.
- Session key deletes invoked when login mapping present.
- Empty ids no-op.

---

## 4. B — Org/Role `@DataPermission`

### 4.1 Problem

`DataPermissionHandlerImpl.getAdminWhere` always builds:

`field IN (SELECT user_id FROM alex_user.t_org_user_info WHERE org_id = ?)`

That fits **user id / operator** columns. It does **not** fit `t_org_info.id`.

### 4.2 Annotation extension

Extend `com.alex.api.user.annotation.DataPermission`:

```java
enum Scope {
    /** field IN (org member user ids) — current admin behavior; user: field = self id */
    USER_IDS,
    /** field = login user's org id for admin and normal user */
    ORG_ID
}

Scope scope() default Scope.USER_IDS;
```

Default `USER_IDS` keeps existing mappers unchanged.

### 4.3 Handler behavior

| Role | `USER_IDS` | `ORG_ID` |
|------|------------|----------|
| super* | no filter | no filter |
| admin* | `field IN (org user_ids subquery)` | `field = loginUser.orgInfoVo.id` (if org missing → degrade to deny-all or user-self; **prefer:** same as today degrade to `getUserWhere` only when USER_IDS; for ORG_ID with null org → `1=0` / impossible equals) |
| user / default | `field = loginUser.id` | `field = loginUser.orgInfoVo.id` (null org → no rows) |

Optional hardening (in scope): org-user subquery add `status = '1' AND is_delete = 0`.

### 4.4 Mapper annotations

| Mapper method | Annotation |
|---------------|------------|
| `OrgInfoMapper.getPage` | `@DataPermission(table = "t_org_info", field = "id", scope = Scope.ORG_ID)` |
| `RoleInfoMapper.getPage` | `@DataPermission(table = "t_role_info", field = "operator", scope = Scope.USER_IDS)` |

Detail / add / update / delete: **out of Wave1** (same as historical user-list focus on page).

### 4.5 Tests

- Handler unit tests for ORG_ID admin/user SQL fragment (or expression shape).
- USER_IDS path still produces InExpression for admin.
- Existing user mapper annotations remain default scope.

---

## 5. File touch list (expected)

**A**

- `TUserServiceImpl.java` (+ inject org/role services if not already)
- Possibly `TUserLogin` mapper/service for session lookup
- New test under `user_boot/src/test/java/com/alex/user/rbac/`

**B**

- `DataPermission.java`
- `DataPermissionHandlerImpl.java`
- `OrgInfoMapper.java`
- `RoleInfoMapper.java`
- Handler tests

---

## 6. Acceptance

1. Delete user → no active org/role rows; permission_context gone; subsequent API with old token fails auth.
2. Org admin page: only own org row(s) for ORG_ID scope; Role page: only roles operated by org members / self.
3. Super admin unchanged full lists.
4. Unit tests green.

## 7. Risks

- Kick-session discovery: if login rows are incomplete, some tokens may linger until TTL — document and use best-effort Redis delete.
- ORG_ID only exposes own org, not children — product limitation of Wave1.
- Annotation cache in handler: after annotation signature change, JVM restart clears cache (ok).

## 8. Self-check

- [x] No TBD / placeholder APIs
- [x] Matches A1 + B1 + packaging R Wave1
- [x] Explicit why Org needs ORG_ID scope
- [x] Wave2 excluded
- [x] Redis vs DB failure policy stated

---

## 9. Next

After review approval → `writing-plans` →  
`docs/superpowers/plans/2026-08-03-rbac-p1-wave1.md`
