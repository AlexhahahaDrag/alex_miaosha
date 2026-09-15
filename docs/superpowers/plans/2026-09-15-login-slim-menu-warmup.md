# Login Slim + Menu Warmup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans or implement inline. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Slim login (no menuList in response/session), warm `menu_all_tree` at startup + after CRUD, fetch trimmed menus when entering the PC app.

**Architecture:** Reuse `MenuInfoServiceImp` cache write path as `warmMenuAllTree()`. Login calls `buildContext(userId, false)` skipping menu load. New authenticated `GET /api/v1/user/menus` returns filtered menus. PC router guard loads menus before `addRouter`.

**Tech Stack:** Spring Boot user_boot, Redis `LoginKey`, Vue3 Pinia router.

**Spec:** `docs/superpowers/specs/2026-09-15-login-slim-menu-warmup-design.md`

## Global Constraints

- Login returns org/roles/permission codes; **no menuList**
- No anonymous full menu tree
- Reuse existing filter + `menu_all_tree` key/format
- Do not commit unless user asks
- Mobile deferred

## File map

| File | Change |
|------|--------|
| `MenuInfoService` / `MenuInfoServiceImp` | `warmMenuAllTree()`; clear→warm |
| `MenuCacheWarmupRunner` | ApplicationRunner |
| `UserPermissionContextService` (+impl) | `buildContext(id, includeMenus)`; `listVisibleMenus(id)` |
| `TUserServiceImpl` | login slim + no avatar wait |
| `TUserController` | `GET /menus` |
| `user.ts` + `router/index.ts` + small api | fetch menus on enter |
| `tests/checklists/login-slim-menu-warmup.md` | checklist |

---

### Task 1: Menu warm

- [x] Add `void warmMenuAllTree()` to service; extract write path from `getList` full-query
- [x] `clearMenuCache()` then `warmMenuAllTree()`
- [x] `MenuCacheWarmupRunner` on startup
- [x] Smoke: method callable without NPE when Redis down (log only)

### Task 2: Context + /user/menus

- [x] `buildContext(Long, boolean includeMenus)` — false skips menu future; menuList empty
- [x] Default `buildContext(id)` → `buildContext(id, true)` for backward compat
- [x] `listVisibleMenus(userId)` — tree via getList(status=1) + same filter as today
- [x] `GET /api/v1/user/menus` → Result.success(list)
- [x] Unit test filter / includeMenus false has null/empty menuList

### Task 3: Login slim

- [x] login + fast-path refresh: `buildContext(id, false)`
- [x] `completeLoginResponse`: do not wait avatar (fire-and-forget or skip get)
- [x] Ensure redis loginAdmin/loginToken JSON has empty menuList / menuInfoVoList

### Task 4: Frontend

- [x] login store: stop `setMenuInfo` from login body
- [x] api `getUserMenus`
- [x] router guard: if token && !menus → fetch → setMenuInfo → addRouter

### Task 5: Checklist

- [x] Write `tests/checklists/login-slim-menu-warmup.md`
- [ ] Run focused user_boot tests
