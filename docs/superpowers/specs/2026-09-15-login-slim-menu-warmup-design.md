# Design: 登录瘦身 + 菜单预热 + 进系统再拉裁剪菜单

**Date:** 2026-09-15  
**Status:** Approved  
**Module:** `alex_miaosha_user` + Gateway 白名单（仅登录态菜单接口如需）+ PC `alex_miaosha_front`  
**Approach:** Ponytail 收紧版 — 复用现有 `menu_all_tree` / `buildContext` 过滤；不公开全树；不大改架构

## 0. Decisions (locked)

| 项 | 选择 |
|----|------|
| 登录返回 | 同步返回鉴权必需：`token`、用户基本信息、机构、角色、权限码 / 按钮码 / superAdmin；**不含** `menuList` |
| 头像 | 登录不阻塞 OSS；URL 可空，进系统后再补（现有能力即可） |
| 菜单交付 | **不进登录响应**；进入系统（有 token）时再调**需登录**的裁剪菜单接口 |
| 全局菜单 Redis | 启动预热 `menu_all_tree`；菜单 CRUD 后 clear + 立即 warm |
| 免 token 完整全树 | **不做**（Ponytail 收紧；曾选 A 已撤销） |
| 前端过滤全树 | **不做**；过滤留在后端（复用现有权限裁剪逻辑） |
| 会话 Redis | `login:token` / `login:admin` 存**瘦身** `TUserVo`（无大菜单树） |

## 1. Problem

登录 StopWatch 显示「Redis 缓存写入」可达 ~14s（大 `TUserVo` JSON，含权限上下文与菜单）。  
`buildContext` 依赖全量菜单树；`menu_all_tree` 仅懒加载写入，冷启动首次登录易打库。  
PC 前端当前从登录响应取 `menuList` 装动态路由；改为进系统后再拉菜单需改守卫/登录 store，但可保持后端过滤。

## 2. Goals

1. 缩短登录路径：响应与会话缓存不再携带完整菜单树。  
2. 启动即预热全局 `menu_all_tree`；变更后尽快恢复缓存，避免长空窗。  
3. 有 token 首次进入系统时拉取**按用户裁剪**的菜单并装路由。  
4. 复用现有代码路径，最少新概念（Ponytail）。

## 3. Non-Goals

- 匿名/白名单开放完整 `menu_all_tree` 内容  
- 前端用权限码本地过滤全树  
- 登录 fire-and-forget 写会话（去掉 `join`）  
- 移动端对齐（本轮可延后，实现计划标注）  
- 重写 RBAC / 新缓存框架  
- 管理端 `/menu-info/tree`（scoped）改走全局缓存

## 4. As-is → To-be

### As-is

```
login → buildContext(org+roles+全树过滤菜单) → 大 TUserVo 写 Redis → 前端用 menuList 装路由
menu_all_tree：首次全量 getList 时懒写；CRUD clear
```

### To-be

```
启动 → warmMenuAllTree() → Redis menu_all_tree
login → 校验/JWT → buildContext 瘦身（无 menuList 进响应/会话）→ 瘦身 TUserVo 写 Redis
进系统（router 守卫，已登录且未装路由）→ GET 用户菜单（读预热全树 + 权限裁剪）→ setMenuInfo / addRouter
菜单 CRUD → clearMenuCache + warmMenuAllTree()
```

## 5. Backend

### 5.1 Menu warm（复用）

- Key：`LoginKey:login:in:menu_all_tree`（与现 `MenuInfoServiceImp` 一致）  
- 实现：抽出/复用现有「`getListAll` + 组树 + `setEx`」为 `warmMenuAllTree()`（或等价包一层，禁止第二套序列化格式）  
- 触发：
  - User 服务 `ApplicationRunner`（或等价启动钩子）调用一次  
  - 现有 `clearMenuCache()` 之后立即 warm（add/update/delete）  
- 管理端 scoped tree：**禁止**读写该 key（保持现状）

### 5.2 Login slim

- `TUserServiceImpl.login`：
  - 仍同步 `buildContext` 获取 org / roles / permissionCodes 等  
  - **登录响应与写入 `login:token`/`login:admin` 的 JSON 不含 `menuList`**（`permissionContext.menuList` 置空或不序列化）  
  - 头像：不等待 OSS（或保持极短超时；默认不等）  
- `loginUuid` 等鉴权必需 key 仍同步写入后再返回  
- Fast-path 已登录刷新：同样不把大菜单写回会话 blob

### 5.3 用户菜单接口（需登录）

- 新接口建议：`GET ${api.version}/user/menus`（或挂在现有 user/permission 资源下，实现计划钉死 path）  
- 鉴权：与其它业务相同（Gateway JWT / User 鉴权），**不进**匿名白名单  
- 行为：当前用户 → 权限码（可从 `permission_context` 缓存或现场 build）→ 读 `menu_all_tree`（未命中则 warm/回源）→ 复用现有树过滤逻辑 → 返回 `List<MenuInfoVo>`（或包在简单 Result 里）  
- 不发明新过滤算法；从 `UserPermissionContextServiceImpl` 抽私有过滤为可复用方法（最小搬迁）

### 5.4 缓存失效

| 事件 | 动作 |
|------|------|
| 菜单 CRUD | clear + warm `menu_all_tree` |
| 角色/权限/机构变更 | 清 `permission_context:{userId}`（现有）；会话已无菜单则不必为菜单去刷 `login:token` |
| 权限变更后侧栏 | 依赖前端再拉 `/user/menus` 或重新进系统；可选后续：主动清前端持久化（本轮可不做） |

## 6. Frontend（PC）

- 登录成功：存 `token`、瘦身 `admin`、org/role/权限码；**不** `setMenuInfo` 自登录体  
- 路由守卫：已登录且动态路由未就绪 → 调 `/user/menus` → `setMenuInfo` → `addRouter`  
- 失败：提示错误，避免空白权限静默失败  
- 头像：缺省展示占位；有机会再请求用户信息补全（可用现接口，不强制本轮新 API）

## 7. Gateway

- **不为**全树加匿名白名单  
- 若用户菜单 path 已在通用鉴权链上，通常无需改；仅当 path 被误拦时按现有 `am-user` 模式排查（实现时冒烟）

## 8. Testing

### Checklist

`tests/checklists/login-slim-menu-warmup.md`

- [ ] 启动后 Redis 存在非空 `menu_all_tree`  
- [ ] 登录响应无 `menuList` / 无巨型菜单 JSON  
- [ ] 登录后会话鉴权可用（后续带 token 请求成功）  
- [ ] 进系统后侧栏路由来自 `/user/menus`  
- [ ] 未登录调 `/user/menus` → 401/403  
- [ ] 菜单 CRUD 后 warm 恢复；新节点对有权限用户可见  
- [ ] 管理端 `/menu-info/tree` 仍 scoped、不写全局缓存  

### Automated

- Warm：启动或单测调用后 key 有数据（可用嵌入式/Mock Redis 或集成测）  
- 过滤：给定权限码子集，裁剪结果不含无权限节点（单测抽过滤方法）  
- 登录序列化：瘦身 VO 不含 menuList（单测）  

### Success Criteria

1. 登录路径不再因整棵菜单写入会话而长时间阻塞（相对现状明显下降；具体秒数依赖 Redis 环境）  
2. 冷启动后首次进系统菜单读缓存为主  
3. 无匿名全树泄露  

## 9. Risks

| 风险 | 缓解 |
|------|------|
| 前端短暂无菜单 | 守卫等待 menus 完成再 addRouter；loading 态 |
| Warm 失败 | 读路径保持现有懒回源；打日志不阻断启动 |
| 权限变更后本地仍旧菜单 | 文档说明需重新拉 menus / 重进；本轮可不做推送 |
| 抽过滤方法回归 | 单测锁住 super_super 与普通角色行为 |

## 10. Open Notes

- 精确 REST path 与 VO 包装在实现计划钉死  
- 移动端是否同构：本轮默认延后  
- `permission_context` Redis 是否仍缓存带 menuList 的完整上下文：可继续缓存完整上下文供服务端用，但**登录 HTTP 与 login:token 不下发 menuList**（实现计划二选一，默认：context 缓存可含菜单，响应剥离）
