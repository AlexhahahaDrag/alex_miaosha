# Login Slim + Menu Warmup Checklist

> **关联项目**：`alex_miaosha_user`（后端）+ `alex_miaosha_front`（PC）+ `alex_miaosha_mobile`（移动端）
> **关联文档**：`docs/superpowers/specs/2026-09-15-login-slim-menu-warmup-design.md`、`docs/superpowers/specs/2026-09-15-mobile-menu-on-enter-design.md`、`TESTING_STANDARD.md`
> **最后更新**：2026-09-15

---

## 0. 元信息

| 项 | 内容 |
| --- | --- |
| 模块名 | login-slim-menu-warmup |
| 后端路径 | `user_boot`：MenuInfo / UserPermissionContext / TUser |
| PC 路径 | `store/modules/user`、`router/index.ts`、`views/login/api` |
| 移动端路径 | `store/modules/user`、`router/index.ts`、`views/login/api` |
| API | `POST /api/v1/user/login`（瘦身）、`GET /api/v1/user/menus`（需登录） |
| Redis | `LoginKey:login:in:menu_all_tree`、`permission_context:{userId}` |

---

## 1. 字段边界（七点法）

| # | 字段/契约 | 边界 | 期望 |
| --- | --- | --- | --- |
| F1 | login.admin.menuInfoVoList | 登录响应 | 空 / 缺省，无完整菜单树 |
| F2 | login.admin.permissionContext.menuList | 登录响应 | 空列表 |
| F3 | login.admin 机构/角色/权限码 | 登录响应 | 仍同步返回 |
| F4 | Redis login:token JSON | 会话 | 不含大菜单树 |
| F5 | GET /user/menus | 已登录 | 返回裁剪后 `List<MenuInfoVo>` |
| F6 | GET /user/menus | 未登录 | 401/403 |
| F7 | menu_all_tree | 启动后 | 非空（warm 成功时） |

---

## 2. 状态机

| 事件 | 前态 | 后态 |
| --- | --- | --- |
| User 启动 | 无/过期 menu_all_tree | warm 写入全局树 |
| 菜单 CRUD | 旧缓存 | clear + warm |
| 登录成功 | — | 瘦身会话；前端无 menu 路由 |
| 进系统（有 token） | 动态路由未装 | 拉 /user/menus → setMenuInfo → addRouter |

---

## 3. 权限矩阵

| Persona | 登录 | /user/menus | 侧栏 |
| --- | --- | --- | --- |
| super_super | 瘦身成功 | 全量有效菜单 | 全树路由 |
| 普通有权限角色 | 瘦身成功 | 仅权限码匹配菜单 | 裁剪路由 |
| 未登录 | — | 拒绝 | 跳转 login |

---

## 4. 必测 case

| # | Case | 层 | 状态 |
| --- | --- | --- | --- |
| U1 | `buildContext(id,false)` menuList 为空且含权限码 | 单元 | 已加 |
| U2 | `completeLoginResponse` 不等待 avatar | 单元 | 已改 |
| U3 | `refreshLoginPermissionContext` 走 includeMenus=false | 单元 | 已改 |
| I1 | 启动后 Redis 存在 menu_all_tree | 集成/冒烟 | 手工 |
| I2 | 登录响应无巨型 menu JSON | 集成/冒烟 | 手工 |
| I3 | 带 token 调 /user/menus 成功 | 集成/冒烟 | 手工 |
| E1 | PC 登录后侧栏来自 /user/menus | E2E/手工 | 手工 |
| E2 | 管理端 `/menu-info/tree` 仍 scoped | 回归 | 手工 |
| M1 | 移动端登录后管理中心快捷功能非空 | 手工 | 待测 |
| M2 | 移动端刷新带 token 仍显示快捷功能 | 手工 | 待测 |
| M3 | `/user/menus` 失败 → toast + 回登录 | 手工 | 待测 |
| M4 | 移动端登录响应无巨型 menuList | 手工 | 待测 |
| T1 | tidy：登录后快捷功能仍非空 | 手工 | 待测 |
| T2 | tidy：刷新带 token | 手工 | 待测 |
| T3 | tidy：登出再登可重装路由 | 手工 | 待测 |
| T4 | permission 单测无菜单字段 | 单元 | 待测 |
| P-T1 | PC tidy：PermissionContext 无 menuList | 单元 | 待测 |
| P-T2 | PC tidy：登录后侧栏仍正常 | 手工 | 待测 |

---

## 5. 不测理由

| 项 | 理由 |
| --- | --- |
| 匿名全树 | Spec 明确不做 |
| 前端本地过滤全树 | Spec 明确不做 |
| 登录 fire-and-forget 写会话 | Spec 明确不做 |
| 首页改 `getRoutes` 数据源（方案 C） | 移动端 menu-on-enter Spec Non-Goal |

---

## 6. 执行记录

- [x] 后端 Task 1–3 代码落地
- [x] PC 前端 Task 4 守卫拉菜单
- [x] 移动端 Task 1–3：API + store + 守卫对齐 `/user/menus`
- [x] 移动端 Task 4：DEVELOPMENT.md + checklist
- [ ] `mvn test` 聚焦 `UserPermissionContextServiceTest`（本机缺 JDK 17，`JAVA_HOME` 指向 1.8，待环境就绪后执行）
- [ ] 本地冒烟：登录耗时 / 侧栏 / CRUD warm / 移动端 M1–M4
