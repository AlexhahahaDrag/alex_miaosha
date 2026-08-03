# Role Permission Batch Assign (assign-permissions) Design

Date: 2026-08-03  
Status: Implemented  
Scope: backend `alex_miaosha_user` + PC `alex_miaosha_front` role authorization path

## 1. Problem

PC `authorizationDetail` / `roleInfoDetail` put `permissionList` into `POST/PUT /role-info`, but `RoleInfoServiceImp.addRoleInfo` / `updateRoleInfo` only persist the role main table and **ignore `permissionList`**.  
Result: UI shows success; `t_role_permission_info` does not update.

## 2. Goals

1. Provide a batch permission assign API symmetric to `POST /role-info/assign-users`.
2. Front authorization entries call only the new API; role main-data save must not carry `permissionList`.
3. After assign, invalidate related users' `permission_context` cache.
4. Cover core semantics with unit tests in the style of `RoleUserAssignmentServiceTest`.

## 3. Non-goals

- Org / Role Mapper `@DataPermission`
- Cascade cleanup on user delete
- Mobile `rolePermissionInfo` page changes
- Dedicated enable/disable role API

## 4. API

```
POST ${api.version}/role-info/assign-permissions
Content-Type: application/json

{
  "roleId": 1234567890123456789,
  "permissionIds": [1, 2, 3]
}
```

Response: `Result<Boolean>` (same as `assign-users`).

Request DTO `RolePermissionAssignRequest`:

| Field | Type | Notes |
|------|------|------|
| roleId | Long | required |
| permissionIds | List\<Long\> | null / empty allowed (clear only) |

## 5. Backend behavior

### 5.1 Placement

| Layer | Duty |
|-------|------|
| `RoleInfoController.assignPermissions` | Accept request, delegate |
| `RoleInfoService.assignPermissions` | Validate role exists → relation service → clear cache |
| `RolePermissionInfoService.assignPermissions` | Full replace of role-permission rows |

### 5.2 `assignPermissions` semantics (aligned with `assignRoles`)

1. `roleId == null` → `SystemException(PARAM_ERROR)`
2. Active rows for role (`status = VALID`) → set `status = INVALID` via `updateById`
3. `permissionIds` null/empty (or empty after filter) → invalidate only, return true
4. Otherwise dedupe (`LinkedHashSet`) + drop nulls → `saveBatch` with String `roleId`/`permissionId`, `status = VALID`
5. `@Transactional(rollbackFor = Exception.class)` on relation service method

### 5.3 Cache invalidation

After successful assign, list active `RoleUserInfo` for the role and delete Redis keys  
`permission_context:{userId}` via `LoginKey.loginKey` (same shape as `TUserServiceImpl`).

### 5.4 Role main-data APIs

`addRoleInfo` / `updateRoleInfo` continue to ignore `permissionList`.  
**Implemented addition:** `addRoleInfo` returns created role id as `String` so front never resolves id via `roleCode` page LIKE query.

Permission writes go only through `assign-permissions`.

## 6. Frontend (PC)

### 6.1 API

- `assignRolePermissions(roleId: string, permissionIds: string[])`
- `getPermissionInfoList(params?)` for add-mode permission tree
- IDs stay `string`; never `Number(id)`

### 6.2 `authorizationDetail`

Save only calls `assignRolePermissions`. No `addRoleInfo` / `editRoleInfo`.

### 6.3 `roleInfoDetail`

- Save role main fields without `permissionList`
- Edit: `editRoleInfo` then `assignRolePermissions(String(id), ...)`
- Add: `addRoleInfo` → use returned `data` as role id → `assignRolePermissions`
- Add-mode tree: `getPermissionInfoList()` (no `getRoleInfoDetail('1')` stub)

## 7. Tests

`RolePermissionAssignmentServiceTest`: invalidate+insert, empty clear, dedupe/null filter, null roleId throws, `@Transactional` present.

## 8. Acceptance

1. Auth drawer save updates `t_role_permission_info` (valid/invalid rows).
2. Role edit PUT body has no `permissionList`; permission change hits `assign-permissions`.
3. Bound users lose `permission_context:{userId}` after assign (or see updated perms after rebuild).
4. Unit tests pass; front lint when env available.

## 9. Risks / notes

- Entity stores `role_id` / `permission_id` as String; API uses Long + `String.valueOf`.
- Historical invalid rows accumulate (same as org/role assign pattern).
- Mobile intermediate CRUD pages unchanged.

## 10. Self-check

- [x] No TBD placeholders
- [x] Matches chosen design: dedicated assign-permissions + front dual entry + no permissionList on save
- [x] Cache key documented
- [x] New-role id via add response (not roleCode LIKE lookup)
- [x] Mobile / Org data-permission out of scope
