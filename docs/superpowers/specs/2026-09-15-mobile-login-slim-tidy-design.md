# Design: 移动端登录路径瘦身（permission 去菜单 + 守卫对齐 PC）

**Date:** 2026-09-15  
**Status:** Approved  
**Module:** `alex_miaosha_mobile`  
**Related:** `2026-09-15-mobile-menu-on-enter-design.md`（菜单已由守卫拉 `/user/menus`）  
**Approach:** Ponytail — 删死代码、双开关对齐 PC，不抽新抽象

## 0. Decisions (locked)

| 项 | 选择 |
|----|------|
| `buildPermissionContext` 菜单 | **删除**：`resolveMenuList` + `PermissionContext.menuList/menuInfo` + LoginAdminLike 菜单字段 |
| store `menuInfo` | **保留**；仅守卫 `/user/menus` 写入；登录 `setMenuInfo([])` |
| `setMenuInfo` | `this.menuInfo = info ?? null` |
| 守卫双开关 | 删模块级 `isAdded`；对齐 PC：`BASE_ROUTE_COUNT` + `hasMenu`/`getRouteStatus` |
| 登录是否拉菜单 | **否**；仍只在守卫拉 |
| 首页 / 粒子 / PC / 后端 | **本轮不做** |

## 1. Problem

登录瘦身后菜单不进登录体，但 `normalizePermissionContext` 仍解析 `menuList`，登录侧又立刻 `setMenuInfo([])` 丢掉——死代码、误导。  
移动端守卫额外有 `isAdded`，与 PC 的 `BASE_ROUTE_COUNT` 双条件不一致，增加认知成本。

## 2. Goals

1. permission 工具只负责 org/roles/codes/superAdmin。  
2. 守卫装路由判定与 PC 同构。  
3. `setMenuInfo` 对空数组语义诚实（`??`）。  
4. 最少文件、不引入 composable。

## 3. Non-Goals

- 登录 action 内调用 `getUserMenusApi`  
- 改 `home/index.vue` 数据源  
- 同步改 PC `normalizePermissionContext`（可另开）  
- 后端变更  

## 4. As-is → To-be

### As-is

```
login → buildPermissionContext(含空/死菜单) → setMenuInfo([])
守卫 → isAdded + hasMenu → 拉 /user/menus → addRouter
```

### To-be

```
login → buildPermissionContext(无菜单字段) → setMenuInfo([])
守卫 → !hasMenu || routes.length <= BASE → 拉 /user/menus → addRouter
refreshRouter → splice(BASE) + hasMenu=false
```

## 5. Changes

### 5.1 `src/utils/permission/index.ts`

- 删除 `resolveMenuList`
- `PermissionContext` 去掉 `menuList` / `menuInfo`
- `LoginAdminLike` / 嵌套 `permissionContext` 去掉菜单相关可选字段
- `normalizePermissionContext` 返回值不再带菜单

### 5.2 `src/store/modules/user/user.ts`

- `setMenuInfo`: `this.menuInfo = info ?? null`
- `login`: 仍 `setMenuInfo([])`；继续用 `buildPermissionContext` 写 role/org/codes/superAdmin

### 5.3 `src/router/index.ts`

- `const BASE_ROUTE_COUNT = routes.length`（在静态 routes 定义后）
- 删除 `isAdded` 及 login 路径上的赋值
- `beforeEach` 条件：`!userStore.getRouteStatus || routes.length <= BASE_ROUTE_COUNT`
- `refreshRouter`：移除动态路由后 `routes.splice(BASE_ROUTE_COUNT)`，`changeRouteStatus(false)`
- 菜单拉取与失败回登录逻辑保持现网（已落地的 mobile-menu-on-enter）

### 5.4 Tests / docs

- `permission.spec.ts`：断言不依赖菜单字段  
- `DEVELOPMENT.md` 一句：permission 上下文不含菜单；菜单仅守卫 `/user/menus`  
- checklist 追加回归项或并入现有 login-slim checklist  

## 6. Testing

| # | Case | 期望 |
|---|------|------|
| T1 | 登录后管理中心快捷功能 | 非空 |
| T2 | 刷新带 token | 仍有快捷功能 |
| T3 | 登出再登 | 路由可重装 |
| T4 | `/user/menus` 失败 | toast + 回登录 |
| T5 | `npm test` / permission unit | 通过 |

## 7. File map

| File | Change |
|------|--------|
| `src/utils/permission/index.ts` | 去菜单 |
| `src/utils/permission/permission.spec.ts` | 如需 |
| `src/store/modules/user/user.ts` | `?? null` |
| `src/router/index.ts` | BASE + 删 isAdded |
| `DEVELOPMENT.md` | 一句 |
| checklist（backend tests） | 回归项 |
