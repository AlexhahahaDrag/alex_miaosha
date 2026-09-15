# Mobile Login Slim Tidy Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 移动端去掉 `buildPermissionContext` 菜单死代码；守卫双开关对齐 PC；`setMenuInfo` 用 `??`。

**Architecture:** permission 只产出 org/roles/codes/superAdmin；菜单仅守卫 `GET /user/menus` → store `menuInfo`；路由就绪用 `hasMenu` + `BASE_ROUTE_COUNT`（删 `isAdded`）。

**Tech Stack:** Vue3 + Pinia + Vue Router（mobile）+ Vitest permission 单测。

**Spec:** `docs/superpowers/specs/2026-09-15-mobile-login-slim-tidy-design.md`

## Global Constraints

- 登录不拉 `/user/menus`；菜单只在守卫拉
- 不改 home 数据源、粒子、后端、PC permission
- 响应解构保持 `const { code, data, message } = await api()`
- **不自动 git commit**（除非用户明确要求）
- 改 `src` 后 `npm run graphify:update`（若环境有 graphify）
- Windows 用 CMD

## File map

| File | Change |
|------|--------|
| `alex_miaosha_mobile/src/utils/permission/index.ts` | 去菜单字段与 resolveMenuList |
| `alex_miaosha_mobile/src/utils/permission/permission.spec.ts` | 无菜单断言即可保留 |
| `alex_miaosha_mobile/src/store/modules/user/user.ts` | `?? null` |
| `alex_miaosha_mobile/src/router/index.ts` | BASE_ROUTE_COUNT，删 isAdded，refresh splice |
| `alex_miaosha_mobile/DEVELOPMENT.md` | 一句 |
| `alex_miaosha/tests/checklists/login-slim-menu-warmup.md` | 追加 tidy 回归 |

---

### Task 1: permission 去菜单

**Files:**
- Modify: `F:/workplace/project/myself/frontend/alex_miaosha_mobile/src/utils/permission/index.ts`
- Test: `F:/workplace/project/myself/frontend/alex_miaosha_mobile/src/utils/permission/permission.spec.ts`

**Produces:** `PermissionContext` 无 `menuList`/`menuInfo`；`normalizePermissionContext` / `buildPermissionContext` 不再解析菜单

- [ ] **Step 1: 改 PermissionContext 与 LoginAdminLike**

从 `PermissionContext` 删除：

```typescript
menuList: MenuInfoData[];
/** 与 menuList 同义，兼容旧 store */
menuInfo: MenuInfoData[];
```

从 `LoginAdminLike` 及嵌套 `permissionContext` 删除所有 `menuList` / `menuInfoVoList` 字段。  
若 `MenuInfoData` import 仅服务于菜单，一并删除该 import。

- [ ] **Step 2: 删除 resolveMenuList；改 normalizePermissionContext**

删除整个 `resolveMenuList` 函数。

`normalizePermissionContext` 内删除：

```typescript
const menuList = resolveMenuList(admin);
```

return 对象删除 `menuList` / `menuInfo` 两行。

- [ ] **Step 3: 跑单测**

```bat
cd /d F:\workplace\project\myself\frontend\alex_miaosha_mobile
npm test -- --run src/utils/permission/permission.spec.ts
```

Expected: PASS（现有用例不依赖菜单字段）。

---

### Task 2: store `?? null`

**Files:**
- Modify: `F:/workplace/project/myself/frontend/alex_miaosha_mobile/src/store/modules/user/user.ts`

- [ ] **Step 1: 改 setMenuInfo**

将：

```typescript
this.menuInfo = info || null;
```

改为：

```typescript
this.menuInfo = info ?? null;
```

登录中的 `this.setMenuInfo([])` **保持不变**（空数组保留，不用 null）。

- [ ] **Step 2: 确认无其它调用依赖 `||` 把空数组变 null**

全仓 `setMenuInfo` 调用点：登录 `[]`、守卫成功写入 `data`——均可。

---

### Task 3: 守卫对齐 PC（删 isAdded）

**Files:**
- Modify: `F:/workplace/project/myself/frontend/alex_miaosha_mobile/src/router/index.ts`

**Consumes:** 现有 `getUserMenusApi` 拉取逻辑  
**Produces:** 无 `isAdded`；`BASE_ROUTE_COUNT` + `getRouteStatus`

- [ ] **Step 1: 在静态 `routes` 定义之后、`createRouter` 之前增加**

```typescript
const BASE_ROUTE_COUNT = routes.length;
```

- [ ] **Step 2: 删除 `let isAdded = false` 及所有 `isAdded` 读写**

含：进 `/login` 时的 `isAdded = false`；成功装路由后的 `isAdded = true`；条件里的 `!isAdded`。

- [ ] **Step 3: 改 beforeEach 条件与成功分支**

将：

```typescript
if (!userStore.getRouteStatus || !isAdded) {
```

改为：

```typescript
if (!userStore.getRouteStatus || routes.length <= BASE_ROUTE_COUNT) {
```

菜单拉取块保持不变（`!getMenuInfo?.length` → api → 失败 resetState）。  
`addRouter()` 之后：

```typescript
if (routes.length > BASE_ROUTE_COUNT) {
	return { ...to, replace: true };
}
```

（用 `BASE_ROUTE_COUNT` 替代魔法数 `5`。）

- [ ] **Step 4: 改 refreshRouter**

在移除 `dynamicRouter` 各 name 后增加：

```typescript
routes.splice(BASE_ROUTE_COUNT);
```

保留 `changeRouteStatus(false)`。删除任何对 `isAdded` 的依赖。

- [ ] **Step 5: eslint 本文件**

```bat
cd /d F:\workplace\project\myself\frontend\alex_miaosha_mobile
npx eslint src/router/index.ts src/store/modules/user/user.ts src/utils/permission/index.ts
```

Expected: 无 error（既有 no-console warning 可保留若仍有 console.error）。

---

### Task 4: Docs + checklist + graphify

**Files:**
- Modify: `F:/workplace/project/myself/frontend/alex_miaosha_mobile/DEVELOPMENT.md`
- Modify: `F:/workplace/project/myself/backend/alex_miaosha/tests/checklists/login-slim-menu-warmup.md`

- [ ] **Step 1: DEVELOPMENT.md 追加**

```markdown
- **PermissionContext**：仅 org / roles / permissionCodes / superAdmin；**不含**菜单。菜单仅路由守卫 `GET /user/menus` → `setMenuInfo`。
- **动态路由就绪**：`hasMenu` + `routes.length > BASE_ROUTE_COUNT`（无模块级 isAdded）。
```

- [ ] **Step 2: checklist 追加**

```markdown
| T1 | tidy：登录后快捷功能仍非空 | 手工 | 待测 |
| T2 | tidy：刷新带 token | 手工 | 待测 |
| T3 | tidy：登出再登可重装路由 | 手工 | 待测 |
| T4 | permission 单测无菜单字段 | 单元 | 待测 |
```

- [ ] **Step 3: graphify（若可用）**

```bat
cd /d F:\workplace\project\myself\frontend\alex_miaosha_mobile
npm run graphify:update
```

若缺模块则在报告注明，不阻塞。

---

## Spec coverage

| Spec | Task |
|------|------|
| 删 resolveMenuList / 菜单字段 | T1 |
| setMenuInfo ?? | T2 |
| 删 isAdded + BASE | T3 |
| 登录不拉菜单 | （无任务，保持） |
| docs/checklist | T4 |

无 TBD；不自动 commit。
