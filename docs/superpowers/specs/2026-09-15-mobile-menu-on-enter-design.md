# Design: 移动端菜单对齐（login-slim /user/menus）

**Date:** 2026-09-15  
**Status:** Approved  
**Module:** `alex_miaosha_mobile`（依赖已落地的 `GET /api/v1/user/menus`）  
**Parent Spec:** `docs/superpowers/specs/2026-09-15-login-slim-menu-warmup-design.md`  
**Approach:** 与 PC 同契约 — 登录不带菜单；进系统守卫拉裁剪菜单；失败可见并回登录

## 0. Decisions (locked)

| 项 | 选择 |
|----|------|
| 范围 | **B**：拉菜单 + 失败 toast + 清会话回登录 |
| 菜单来源 | `GET /api/v1/user/menus`（需 token）；**不**从登录响应取 menuList |
| 拉菜单时机 | 路由守卫：有 token 且 `menuInfo` 为空（或未装动态路由）时拉取 |
| 登录 store | 成功后 `setMenuInfo([])` / 清空；仍写 token、用户、机构、角色、权限码 |
| 失败行为 | `showFailToast` → `resetState()`（含清 token / refreshRouter）→ 导航登录页 |
| 首页快捷功能 | **不改**数据源（继续扫动态路由 `showInHome`）；本轮不做 C |
| 后端 | **不改**（接口与瘦身登录已存在） |
| 匿名全树 / 前端过滤全树 | **不做**（继承 parent spec） |

## 1. Problem

登录瘦身后响应与会话不再含菜单树。PC 已在守卫调用 `/user/menus`；移动端仍从登录体 `setMenuInfo(permissionContext.menuInfo)`，导致 `addRouter` 跳过、管理中心「快捷功能」空白。

## 2. Goals

1. 移动端进系统后能装动态路由，首页快捷功能恢复可见。  
2. 与 PC / parent spec 契约一致：登录瘦身，菜单后置、后端裁剪。  
3. 拉菜单失败时用户可感知，不停留在无菜单空壳。  
4. 最少改动（Ponytail）：复用现有 `addRouter` / `buildPermissionContext`。

## 3. Non-Goals

- 改首页 `router.options.routes` 数据源（方案 C）  
- 后端新接口或改过滤算法  
- 移动端 E2E Midscene 全量（本轮手工冒烟 + checklist 即可）  
- 菜单 CRUD 后主动推送刷新（依赖重新进系统 / 再拉）

## 4. As-is → To-be

### As-is

```
login → setMenuInfo(登录体菜单) → 守卫 addRouter
登录体无菜单后 → menuInfo 空 → addRouter 空操作 → 快捷功能空白
```

### To-be

```
login → token + org/roles/codes；setMenuInfo([])
守卫（有 token，需装路由）→ 若无 menuInfo → GET /user/menus
  → 成功：setMenuInfo → addRouter
  → 失败：toast + resetState → /login
首页：动态路由中 showInHome=1 的项渲染快捷功能（逻辑不变）
```

## 5. Frontend（mobile）

### 5.1 API

- `getUserMenusApi()` 挂在 `src/views/login/api/index.ts`（与 PC 对称）  
- 使用现有 `getData` / `baseService.user` 惯例，path：`/user/menus`  
- 响应解构：`const { code, data, message } = await getUserMenusApi()`

### 5.2 Store

- `login()`：不再用登录体写入真实菜单；`setMenuInfo([])`（或等价清空）  
- 保留 `setMenuInfo` / `getMenuInfo` 供守卫写入  
- 失败路径复用已有 `resetState()`

### 5.3 Router guard

- `beforeEach` 改为 async（或等价可 await 的写法）  
- 条件对齐现有：`getToken` 且（`!getRouteStatus || !isAdded`）  
- 在 `addRouter()` **之前**：若 `!getMenuInfo?.length`，请求 `/user/menus`  
  - `code == '200'` 且 `data?.length` → `setMenuInfo(data)` 再 `addRouter`  
  - 否则 / 抛错 → toast → `resetState()` → `{ name: 'login' }`  
- 有 token 且已有 menuInfo：行为与现网一致，直接 `addRouter`

### 5.4 首页

- `home/index.vue` **本轮不改**  
- 依赖：守卫先完成菜单装载，再进入 dashboard，使 `init()` 时动态路由已在共享 `routes` 数组中

## 6. Docs / checklist

- 更新 mobile `DEVELOPMENT.md` 或 `.cursorrules` 一句：菜单不进登录体，进系统拉 `/user/menus`  
- Checklist：在 `tests/checklists/login-slim-menu-warmup.md` **追加**移动端 M1–M4（不新建文件）  
- 实现后 `npm run graphify:update`（改了 `src`）

## 7. Testing

| # | Case | 期望 |
|---|------|------|
| M1 | 登录成功后进管理中心 | 快捷功能非空（有权限菜单） |
| M2 | 刷新带 token | 仍能拉菜单并显示快捷功能 |
| M3 | `/user/menus` 401/失败 | toast + 回到登录，无空白壳 |
| M4 | 登录响应 | 无巨型 menuList（回归 parent） |

## 8. Risks

| 风险 | 缓解 |
|------|------|
| 守卫 async 改写导致导航竞态 | 对齐 PC：`return { ...to, replace: true }` / 明确 return；单路径测试刷新 |
| 旧 localStorage 残留菜单 | 登录清空 menuInfo；无菜单时仍强制走接口（本设计已要求无 length 才拉；若残留脏数据可在登录强制 `[]`） |
| 首页仍读 `options.routes` | 与现网相同；若装载后仍空，再开 C（本轮不做） |

## 9. File map

| File | Change |
|------|--------|
| `mobile/.../views/login/api/index.ts` | `getUserMenusApi` |
| `mobile/.../store/modules/user/user.ts` | 登录清空菜单 |
| `mobile/.../router/index.ts` | async 拉菜单 + 失败回登录 |
| `DEVELOPMENT.md` / `.cursorrules` | 契约一句 |
| checklist | 移动端冒烟项 |
