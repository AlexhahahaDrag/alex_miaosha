# Mobile Menu On Enter Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 移动端进系统时拉取 `GET /user/menus` 再装动态路由，恢复管理中心「快捷功能」；失败 toast 并回登录。

**Architecture:** 对齐 PC：登录只存 token/用户/机构角色/权限码；守卫在 `addRouter` 前若无 `menuInfo` 则调已有后端接口；失败走 `resetState`。不改首页数据源、不改后端。

**Tech Stack:** Vue3 + Pinia + Vue Router（mobile）+ Vant `showFailToast`；后端 `GET /api/v1/user/menus` 已存在。

**Spec:** `docs/superpowers/specs/2026-09-15-mobile-menu-on-enter-design.md`

## Global Constraints

- 登录响应不含菜单；菜单仅来自需登录的 `/user/menus`
- 失败：toast + `resetState()` + 导航登录页
- 不改 `home/index.vue` 数据源（方案 C 不做）
- 不改后端
- 前端响应解构：`const { code, data, message } = await api()`
- ID 保持 string；不新增依赖
- **不自动 git commit**（除非用户明确要求）
- Windows 下终端用 CMD；改 `src` 后跑 `npm run graphify:update`

## File map

| File | Change |
|------|--------|
| `alex_miaosha_mobile/src/views/login/api/index.ts` | 新增 `getUserMenusApi` |
| `alex_miaosha_mobile/src/store/modules/user/user.ts` | 登录 `setMenuInfo([])` |
| `alex_miaosha_mobile/src/router/index.ts` | async 守卫拉菜单 + 失败回登录 |
| `alex_miaosha_mobile/DEVELOPMENT.md` | 契约一句 |
| `alex_miaosha/tests/checklists/login-slim-menu-warmup.md` | 追加 M1–M4 |

---

### Task 1: API `getUserMenusApi`

**Files:**
- Modify: `F:/workplace/project/myself/frontend/alex_miaosha_mobile/src/views/login/api/index.ts`
- Consumes: `getData` / `baseService` from `@/views/common/api`；`MenuInfoData` from `@/views/user/menuInfo/config`；`ResponseBody` from `@/types/api`
- Produces: `getUserMenusApi(): Promise<ResponseBody<MenuInfoData[]>>`

- [x] **Step 1: 扩展 login api**

将文件改为（保留现有 login/logout，仅增加 menus）：

```typescript
import type { LoginResultData } from '../config';
import request from '@/utils/request/request';
import type { ResponseBody } from '@/types/api';
import { baseService, getData } from '@/views/common/api';
import type { MenuInfoData } from '@/views/user/menuInfo/config';

const baseUrl = '/api/v1';

enum Api {
	login = '/user/login',
	logout = '/user/logout',
	menus = '/user/menus',
}

export interface LoginParams {
	isRememberMe?: boolean;
	username: string;
	password: string;
	type?: string;
}

function transParams(data: LoginParams): URLSearchParams {
	const params = new URLSearchParams();
	for (const item in data) {
		params.append(item, data[`${item}`]);
	}
	return params;
}

export function loginApi(params: LoginParams): Promise<ResponseBody<LoginResultData>> {
	return request.post(baseService.user + baseUrl + Api.login, transParams(params), {
		headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
	});
}

export function logoutApi(): Promise<ResponseBody<boolean>> {
	return request.post(baseService.user + baseUrl + Api.logout);
}

export function getUserMenusApi(): Promise<ResponseBody<MenuInfoData[]>> {
	return getData(baseService.user + Api.menus);
}
```

说明：`getData('/api/am-user/user/menus')` 经 `formatUrl` 插入 `VITE_APP_API_PREFIX`（通常 `api/v1`），最终与 PC 一致打到 `/api/am-user/api/v1/user/menus`。

- [x] **Step 2: 类型检查**

Run（在 mobile 根目录）:

```bat
cd /d F:\workplace\project\myself\frontend\alex_miaosha_mobile
npx vue-tsc --noEmit --pretty false 2>&1 | findstr /i "login/api"
```

Expected: 无 `login/api` 相关报错（若全仓已有无关错误可忽略，只要本文件干净）。

---

### Task 2: Login store 清空菜单

**Files:**
- Modify: `F:/workplace/project/myself/frontend/alex_miaosha_mobile/src/store/modules/user/user.ts`
- Consumes: 现有 `setMenuInfo`、`buildPermissionContext`（仍用于 org/roles/codes）
- Produces: 登录成功后 `menuInfo` 为空数组语义（`setMenuInfo([])` → store 存 `[]`；getter 有 length 0）

- [x] **Step 1: 改 login action 中写菜单的一行**

将：

```typescript
this.setMenuInfo(permissionContext.menuInfo || null);
```

改为：

```typescript
// Menus loaded on enter via GET /user/menus (login slim)
this.setMenuInfo([]);
```

其余 `setUserInfo` / `setToken` / role / org / permissionCodes / `changeRouteStatus(false)` / `refreshRouter()` **保持不变**。

- [x] **Step 2: 手工确认逻辑**

登录成功后 Pinia/`localStorage` 中 `menuInfo` 应为 `[]`，且 `hasMenu === false`。

---

### Task 3: Router 守卫拉菜单 + 失败回登录

**Files:**
- Modify: `F:/workplace/project/myself/frontend/alex_miaosha_mobile/src/router/index.ts`
- Consumes: `getUserMenusApi`；`useUserStore().setMenuInfo` / `resetState` / `getMenuInfo` / `getToken` / `getRouteStatus`；现有 `addRouter`
- Produces: 有 token 且需装路由时，先保证 `menuInfo.length > 0` 再 `addRouter`；失败回登录

- [x] **Step 1: 增加 import**

在文件顶部现有 import 旁增加：

```typescript
import { getUserMenusApi } from '@/views/login/api';
import { showFailToast } from 'vant';
```

- [x] **Step 2: 将 `beforeEach` 改为 async 并在 `addRouter` 前拉菜单**

用下面整块替换现有 `router.beforeEach`（保留其后的 `buildRouteAccess` / `addRouter` 等函数不变）：

```typescript
router.beforeEach(async (to) => {
	const userStore = useUserStore();
	if (to.path == '/login') {
		isAdded = false;
		return true;
	}

	if (userStore.getToken) {
		if (!userStore.getRouteStatus || !isAdded) {
			dynamicRouter = [];
			if (!userStore.getMenuInfo?.length) {
				try {
					const {
						code,
						data,
						message: messageInfo,
					} = await getUserMenusApi();
					if (code == '200' && data?.length) {
						userStore.setMenuInfo(data);
					} else {
						showFailToast(messageInfo || '加载菜单失败');
						userStore.resetState();
						return { name: 'login' };
					}
				} catch (error: unknown) {
					console.error('加载用户菜单失败：', error);
					showFailToast('加载菜单失败，请重新登录');
					userStore.resetState();
					return { name: 'login' };
				}
			}
			addRouter();
			isAdded = true;
			if (routes.length > 5) {
				return { ...to, replace: true };
			}
		}
		return true;
	}
	return { name: 'login' };
});
```

注意：

- 必须在 `addRouter()` **之前**完成 `setMenuInfo`
- `resetState()` 内部已调用 `refreshRouter()`，不要重复清路由逻辑
- 成功路径仍设置 `isAdded = true`，避免重复请求死循环

- [x] **Step 3: lint 本文件相关**

```bat
cd /d F:\workplace\project\myself\frontend\alex_miaosha_mobile
npm run lint -- --no-error-on-unmatched-pattern src/router/index.ts src/views/login/api/index.ts src/store/modules/user/user.ts
```

Expected: exit 0（或仅既有无关 warning；按项目 `--max-warnings` 要求处理到通过）。

---

### Task 4: Docs + checklist + graphify

**Files:**
- Modify: `F:/workplace/project/myself/frontend/alex_miaosha_mobile/DEVELOPMENT.md`
- Modify: `F:/workplace/project/myself/backend/alex_miaosha/tests/checklists/login-slim-menu-warmup.md`
- Run: graphify update under mobile

- [x] **Step 1: DEVELOPMENT.md 在「路由」或权限相关小节加一段**

若无独立「路由与权限」小节，插在文档合适位置（公共约定附近）：

```markdown
- **登录与菜单契约**：登录响应不含菜单树；进入系统时由路由守卫调用 `GET /user/menus`（`getUserMenusApi`）再 `setMenuInfo` + `addRouter`。失败 toast 并 `resetState` 回登录。
```

- [x] **Step 2: checklist 追加移动端**

在 `tests/checklists/login-slim-menu-warmup.md` 的「必测 case」或文末追加：

```markdown
| M1 | 移动端登录后管理中心快捷功能非空 | 手工 | 待测 |
| M2 | 移动端刷新带 token 仍显示快捷功能 | 手工 | 待测 |
| M3 | `/user/menus` 失败 → toast + 回登录 | 手工 | 待测 |
| M4 | 移动端登录响应无巨型 menuList | 手工 | 待测 |
```

- [x] **Step 3: graphify**

```bat
cd /d F:\workplace\project\myself\frontend\alex_miaosha_mobile
npm run graphify:update
```

Expected: graph 更新成功。

- [ ] **Step 4: 手工冒烟（实现者执行）**

1. 重启/确认 user 服务已部署含 `/user/menus` 的版本  
2. 移动端重新登录 → 管理中心「快捷功能」有入口  
3. 刷新页面 → 仍有快捷功能  
4. （可选）临时断网或改错 path 验证失败回登录  

---

## Spec coverage (self-review)

| Spec 项 | Task |
|---------|------|
| getUserMenusApi | T1 |
| 登录清空菜单 | T2 |
| 守卫拉菜单 | T3 |
| 失败 toast + resetState + 登录 | T3 |
| 不改首页 | （无任务，刻意） |
| DEVELOPMENT + checklist + graphify | T4 |
| M1–M4 | T4 Step 4 |

无 TBD；不自动 commit。
