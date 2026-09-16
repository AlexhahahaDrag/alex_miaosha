# PC Login Permission Tidy Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** PC 端 `PermissionContext` 去掉菜单死代码，`setMenuInfo` 用 `?? null`，对齐移动端 tidy（范围 B）。

**Architecture:** 菜单仍只由守卫 `getUserMenusApi` → `setMenuInfo`；permission normalize 只产 org/roles/codes/superAdmin。

**Tech Stack:** Vue3 + Pinia（alex_miaosha_front）+ 现有 permission 脚本测试。

**Spec:** `docs/superpowers/specs/2026-09-15-pc-login-permission-tidy-design.md`

## Global Constraints

- 不改路由守卫 / BASE_ROUTE_COUNT / `/user/menus` 拉取逻辑
- 不改后端、不改 mobile
- **不自动 git commit**
- Windows 用 CMD；改 src 后 `graphify update .` 或 `npm run graphify:update`（若可用）

## File map

| File | Change |
|------|--------|
| `alex_miaosha_front/src/utils/permission/index.ts` | 去 menuList |
| `alex_miaosha_front/src/store/modules/user/user.ts` | `?? null` |
| `alex_miaosha_front/tests/permission/permission-context.test.ts` | 更新断言 |
| `alex_miaosha_front/DEVELOPMENT.md` | 契约一句 |
| `alex_miaosha/tests/checklists/login-slim-menu-warmup.md` | 追加 PC tidy 行 |

---

### Task 1: permission 去菜单

**Files:**
- Modify: `F:/workplace/project/myself/frontend/alex_miaosha_front/src/utils/permission/index.ts`
- Modify: `F:/workplace/project/myself/frontend/alex_miaosha_front/tests/permission/permission-context.test.ts`

- [ ] **Step 1: 改类型与 normalize**

从 `PermissionContext` 删除 `menuList: MenuInfoData[]`。  
从 `LoginAdminWithPermissionContext` 删除 `menuInfoVoList`。  
删除 return 中的：

```typescript
menuList:
  permissionContext.menuList?.length ?
    permissionContext.menuList
    : admin?.menuInfoVoList || [],
```

若 `MenuInfoData` 不再被引用，删除其 import。

- [ ] **Step 2: 改测试**

当前测试含：

```typescript
menuList: [{ id: '1', permissionCode: 'user:list' }],
...
if (context.menuList.length !== 1) {
  throw new Error('...');
}
```

改为：fixture 中去掉 `menuList` / `menuInfoVoList`；断言 `!('menuList' in context)`（或等价）；保留 buttonPermissionCodes / isSuperAdmin 相关断言。

- [ ] **Step 3: 跑测试**

```bat
cd /d F:\workplace\project\myself\frontend\alex_miaosha_front
node --import tsx tests/permission/permission-context.test.ts
```

若项目有 npm script 包装该文件则用 script。Expected: exit 0。

---

### Task 2: setMenuInfo `?? null`

**Files:**
- Modify: `F:/workplace/project/myself/frontend/alex_miaosha_front/src/store/modules/user/user.ts`

- [ ] **Step 1**

将：

```typescript
function setMenuInfo(info: MenuInfoData[]) {
  menuInfo.value = info ? info : null;
  localStorage.setItem('menuInfo', JSON.stringify(menuInfo.value));
}
```

改为：

```typescript
function setMenuInfo(info: MenuInfoData[] | null | undefined) {
  menuInfo.value = info ?? null;
  localStorage.setItem('menuInfo', JSON.stringify(menuInfo.value));
}
```

登录中的 `setMenuInfo([])` 保持不变。

---

### Task 3: Docs + checklist

**Files:**
- Modify: `F:/workplace/project/myself/frontend/alex_miaosha_front/DEVELOPMENT.md`
- Modify: `F:/workplace/project/myself/backend/alex_miaosha/tests/checklists/login-slim-menu-warmup.md`

- [ ] **Step 1: DEVELOPMENT.md**（路由与权限小节附近）

```markdown
- **PermissionContext**：仅 org / roles / permissionCodes / buttonPermissionCodes / superAdmin；**不含**菜单。菜单仅守卫 `GET /user/menus` → `setMenuInfo`。
```

- [ ] **Step 2: checklist 追加**

```markdown
| P-T1 | PC tidy：PermissionContext 无 menuList | 单元 | 待测 |
| P-T2 | PC tidy：登录后侧栏仍正常 | 手工 | 待测 |
```

- [ ] **Step 3: graphify（可选）**

```bat
cd /d F:\workplace\project\myself\frontend\alex_miaosha_front
npm run graphify:update
```

失败则注明，不阻塞。

---

## Spec coverage

| Spec | Task |
|------|------|
| 去 menuList | T1 |
| setMenuInfo ?? | T2 |
| docs/checklist | T3 |
| 不改守卫 | （无任务） |

无 TBD；不自动 commit。
