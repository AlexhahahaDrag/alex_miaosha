# RBAC P1 Wave2 Design: Front ID Safety, Batch Confirm, User Filters

Date: 2026-08-03  
Status: Implemented  
Scope: PC `alex_miaosha_front` (+ docs in backend repo)  
Packaging: P1 plan **R** — Wave2 = E → D → C (frontend)

Confirmed choices:

| Item                 | Choice                                                                                     |
| -------------------- | ------------------------------------------------------------------------------------------ |
| ID stringization     | **E3** — user/org/role/permission/menu config IDs as string + remove residual `Number(id)` |
| Batch delete confirm | **D1** — user / role / permission / menu                                                   |
| User list filters    | **C2** — username + status + orgId + roleId                                                |
| Order                | E → D → C                                                                                  |

Wave1 (backend delete cleanup + Org/Role data permission) is separate and already implemented.

---

## 1. Goals

1. Stop treating RBAC entity IDs as JS `number` (Long precision).
2. Require explicit confirmation before batch delete on the four admin list pages.
3. Expose backend-already-supported user page filters: `status`, `orgId`, `roleId`.

## 2. Non-goals

- Mobile changes
- Backend mapper/XML changes for user filters (already present in `TUserMapper.xml`)
- Org batch delete (page has no batch action)
- New enable/disable APIs

---

## 3. E — ID stringization

### 3.1 Type changes

| File                                                           | Change                     |
| -------------------------------------------------------------- | -------------------------- |
| `src/views/user/roleInfo/config/index.ts`                      | `RoleInfoData.id?: string` |
| `src/views/user/menuInfo/config/index.ts`                      | menu id `?: string`        |
| `src/views/user/permissionInfo/permissionInfoListTs.ts`        | `id?: string`              |
| Related nested types in those configs if they use `id: number` | string                     |

`UserManagerInfo.id` is already `string` — leave as-is.

### 3.2 Call-site cleanup

Scan `src/views/user/**` and fix:

- `orgInfo/index.vue`: `Number(selectedKeys[0])` → `String(selectedKeys[0])` (and align `editOrgInfo` param type to `string`)
- `menuInfo/subMenuManager/index.vue`: `delSubMenu(id: string)`
- Any remaining `Number(id)` / `Number(selectedKeys…)` in user views

Prefer `String(x)` when coercing; never `Number` for entity IDs.

### 3.3 Acceptance

- No `id?: number` in user module config/list TS for org/role/menu/permission entities.
- Grep `views/user` has no `Number(` applied to ids/keys used as entity IDs.
- `npm run lint` on touched files when env allows (`jiti` may be broken — note if skipped).

---

## 4. D — Batch delete confirmation

### 4.1 Pages / functions

| Page                       | Function                 |
| -------------------------- | ------------------------ |
| `userManager/index.vue`    | `batchDelUserManager`    |
| `roleInfo/index.vue`       | `batchDelRoleInfo`       |
| `permissionInfo/index.vue` | `batchDelPermissionInfo` |
| `menuInfo/index.vue`       | `batchDelMenuInfo`       |

### 4.2 Behavior

Before calling delete API:

1. If no selection → keep existing warning (`请先选择数据`).
2. Else `Modal.confirm` (Ant Design Vue):
   - title e.g. `确认删除`
   - content includes selected count
   - okText `删除` / okType `danger`
   - onOk → existing delete call
3. Row-level `a-popconfirm` unchanged.

Use auto-imported / project-standard Modal import pattern consistent with nearby pages (explicit `import { Modal, message } from 'ant-design-vue'` if that is existing style).

### 4.3 Acceptance

- Clicking batch delete without confirm no longer fires DELETE immediately.
- Cancel on modal leaves data intact.

---

## 5. C — User list filters

### 5.1 UI (`userManager/index.vue`)

Extend search form beside `username`:

| Field    | Control    | Notes                                                                                                            |
| -------- | ---------- | ---------------------------------------------------------------------------------------------------------------- |
| `status` | `a-select` | dict `is_valid` via `useDictInfo`; `allowClear`                                                                  |
| `orgId`  | `a-select` | options from `getOrgInfoPage({ status: '1' }, 1, 1000)` (or reuse detail loader); value **string**; `allowClear` |
| `roleId` | `a-select` | options from `getRoleInfoPage({ status: '1' }, 1, 1000)`; value **string**; `allowClear`                         |

Bind to `searchInfo`; keep debounce / `usePagination` / reset clearing all four fields.

### 5.2 Types

`UserManagerInfo` add optional `roleId?: string` if missing (backend VO already has `roleId` / `orgId` / `status`).

### 5.3 Status display fix

List tag currently `record.status === 1` — change to treat `'1'` and `1` as valid (e.g. `String(record.status) === '1'`).

### 5.4 Backend

No change. `TUserMapper.xml` already filters `status`, `orgId`, `roleId`.

### 5.5 Acceptance

- Changing filters triggers list refresh with query params present in Network.
- Clearing filters restores broader list.

---

## 6. Expected files

**E:** role/menu/permission configs, orgInfo index, subMenuManager, any other id Number sites under `views/user`.

**D:** four `index.vue` batch handlers.

**C:** `userManager/index.vue`, possibly `userManager/config/index.ts`.

**Docs:** this spec; implementation plan under `docs/superpowers/plans/`.

---

## 7. Risks

- Large option lists (org/role 1000) — acceptable for admin; matches detail page pattern.
- Lint env (`jiti`) may block CI-local lint — document skip vs fix outside Wave2.
- Touching shared types may surface TS errors in unused fields — fix only within `views/user` scope.

## 8. Self-check

- [x] Matches E3 / D1 / C2 and order E→D→C
- [x] No backend filter work claimed
- [x] Wave1 / mobile out of scope
- [x] No TBD placeholders

---

## 9. Next

After review approval → `writing-plans` →  
`docs/superpowers/plans/2026-08-03-rbac-p1-wave2.md`
