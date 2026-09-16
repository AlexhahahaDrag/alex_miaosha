# Design: PC 登录 Permission 去菜单（对齐移动端 tidy）

**Date:** 2026-09-15  
**Status:** Approved  
**Module:** `alex_miaosha_front`  
**Related:**  
- `2026-09-15-login-slim-menu-warmup-design.md`（登录瘦身 + 守卫拉菜单，PC 已落地）  
- `2026-09-15-mobile-login-slim-tidy-design.md`（移动端 permission 去菜单 + `??`）  
**Approach:** Ponytail — 镜像 mobile tidy 范围 B；不重做守卫

## 0. Decisions (locked)

| 项 | 选择 |
|----|------|
| 范围 | **B**：砍 `PermissionContext.menuList` + `setMenuInfo` 用 `?? null` |
| 守卫 / BASE_ROUTE_COUNT / `/user/menus` | **不改**（已齐） |
| 登录拉菜单 | **否** |
| 后端 / 移动端 | **本轮不做** |

## 1. Problem

PC 登录已不写菜单到 store，菜单由守卫 `GET /user/menus` 写入；但 `normalizePermissionContext` 仍解析并返回 `menuList`，与 mobile tidy 前相同——死代码、契约不一致。  
`setMenuInfo` 使用 `info ? info : null`，空数组虽碰巧保留，语义不如 `??` 清晰。

## 2. Goals

1. `PermissionContext` 仅 org / roles / permissionCodes / buttonPermissionCodes / superAdmin。  
2. `setMenuInfo` 用 nullish 合并。  
3. 单测与 DEVELOPMENT 与契约一致。  
4. 最少文件，对齐 mobile 不发明新抽象。

## 3. Non-Goals

- 改路由守卫失败处理 / 强制与 mobile 逐行对齐  
- 改后端  
- 再改 mobile  

## 4. As-is → To-be

### As-is

```
login → normalizePermissionContext(含 menuList:[]) → setMenuInfo([]) → setPermissionContext(仍可能带空 menuList)
守卫 → /user/menus → setMenuInfo(data) → addRouter
```

### To-be

```
login → normalizePermissionContext(无菜单字段) → setMenuInfo([]) → setPermissionContext(无菜单)
守卫 → 不变
```

## 5. Changes

### 5.1 `src/utils/permission/index.ts`

- 删除 `PermissionContext.menuList`
- 删除 `LoginAdminWithPermissionContext.menuInfoVoList`（及对 `menuList` 的读取）
- `normalizePermissionContext` 返回值不再含 `menuList`
- 若 `MenuInfoData` import 仅服务于菜单，删除该 import

### 5.2 `src/store/modules/user/user.ts`

- `setMenuInfo`：`menuInfo.value = info ?? null`（或等价；保持 localStorage 写入）
- 登录 `setMenuInfo([])` 保持不变

### 5.3 Tests

- `tests/permission/permission-context.test.ts`：去掉 `menuList` 偏好断言；可断言无 `menuList` 属性，保留 codes/roles 断言

### 5.4 Docs

- `DEVELOPMENT.md`：PermissionContext 不含菜单；菜单仅守卫 `/user/menus`

## 6. Testing

| # | Case | 期望 |
|---|------|------|
| P1 | 登录后侧栏/动态路由 | 正常 |
| P2 | 刷新带 token | 仍可装路由 |
| P3 | permission-context 测试 | 通过 |

## 7. File map

| File | Change |
|------|--------|
| `src/utils/permission/index.ts` | 去菜单 |
| `src/store/modules/user/user.ts` | `?? null` |
| `tests/permission/permission-context.test.ts` | 更新 |
| `DEVELOPMENT.md` | 一句 |
| checklist（可选追加 PC tidy 行） | 回归 |
