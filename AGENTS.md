## Learned User Preferences

- 提交 Git 记录时，切勿在提交信息中携带 `Co-authored-by: Cursor <cursoragent@cursor.com>` 尾缀信息，并且在 Windows 环境下提交时注意避免中文注释产生乱码。
- 前端处理 ID 数据时，必须避免将其作为 JavaScript `number` 类型处理（以防精度丢失导致低位数字变为 00），前端一律保持为 `string` 字符串类型，后端维持 `Long` 并通过特定注解与序列化机制进行互转。
- 前端发起 API 请求并处理响应时，统一采用对象解构的形式，即 `const { code, data, message } = await api()`，禁止直接通过 `res.code` 链式点读取。
- 前端导入 Pinia 状态或 Vue 组件中的用户、角色、机构相关数据类型时，必须统一参考并在对应页面配置文件夹中导入（例如从 `@/views/user/roleInfo/config`、`@/views/user/menuInfo/config` 等路径导入，避免直接在页面内新增或重复定义类型）。
- 前端开发中，对于已配置由 `unplugin-auto-import` 和 `unplugin-vue-components` 插件自动导入和注册的常用 API（如 `ref`, `computed`, `watch`） and 常用组件（如 Vant、Ant Design Vue），严禁在文件中手动重复显式 `import`。
- 移动端开发中，格式化日期/时间推荐优先引入并使用 `@alex_miaosha_mobile/src/utils/dayjs/index.ts` 中封装好的工具函数。
- 移动端导航栏配置，建议统一使用 `const info = ref<Pick<NavBarConfig, 'title' | 'rightButton' | 'leftPath'>>(...)` 的响应式配置模式。
- 修改关键代码后，必须保证同步更新或运行 graphify 知识图谱以维护最新分析（运行 `graphify update .` 或 `npm run graphify:update` 等），且需要同步更新对应的 `DEVELOPMENT.md` 或 `.cursorrules` 文件。
- 新增功能开发前必须先阅读项目根目录的 `TESTING_STANDARD.md`，按测试金字塔分层落地（后端 单元 70%/集成 25%/AI 5%，PC 端 60/25/15，移动端 55/25/20），并在 `tests/checklists/{feature}.md` 中沿用 `gift.md` 样板补齐字段边界（七点法）、状态机、权限矩阵与"不测理由"。
- AI/E2E 测试统一使用 Midscene + Playwright 组合：所有可交互 DOM 必须挂 `data-testid` 并禁止用中文文案做精确匹配；等待必须用 `waitForResponse` / `waitForSelector` / `aiWaitFor`，禁用 `Thread.sleep` 或 `waitForTimeout`；测试数据需 try/finally 通过 API 清理，禁止直接连 dev 库跑 CRUD 残留脏数据。
- 在制定任何方案（Implementation Plan 或重构方案）后，**必须立即终止工具调用并结束当前 Turn（回合）**，严禁在未获得用户通过 "Proceed" 或显式消息授权前自动执行后续的文件修改或命令运行。

## Learned Workspace Facts

- 后端项目采用微服务和多模块架构，主要包含：`alex_miaosha_user`（用户服务，含 `user_boot` 与 `user_api`）、`alex_miaosha_base`（基础模块）、`alex_miaosha_common`（公共工具）、`alex_miaosha_oss`（OSS 服务）、`alex_miaosha_finance`（财务/礼尚往来微服务，含 `finance_boot` 与 `finance_api`，其中 gift 业务按 `record/person/event/relation` 四个子领域 + `analysis` 聚合分包，对应四张表 `gift_record_info_t/gift_person_info_t/gift_event_info_t/gift_relation_info_t`）。
- 权限上下文构建由 `UserPermissionContextService.buildContext()` 统一负责，聚合了用户的第一有效机构、关联角色列表、菜单树及按钮/API权限码，并可靠地写入 Redis 缓存。
- 组织关系和角色管理逻辑中，机构关系采用单用户唯一有效机构模式（调用 `assignSingleOrg` 时将原有效关系置为 `status=0` 且新增有效关系 `status=1`，需严密事务保障），而角色关系支持多角色绑定（调用 `assignRoles` 时替换并过滤掉 null 等无效角色 ID）。
- 登录逻辑（`TUserServiceImpl.login()`）中，必须通过 `allFutures.join()` 显式同步/等待异步头像获取（`avatarFuture`）及权限上下文构建（`buildContext`）等 CompletableFuture，再装配登录响应并写入 Redis 缓存，以防并发写盘及查询缺失。
- 数据权限控制（`DataPermissionHandlerImpl`）结合 `@DataPermission` 注解，利用 JSqlParser 插件进行 SQL 过滤：超级管理员角色不受限；机构管理员（admin）通过子查询在 `alex_user.t_org_user_info` 中关联其机构 ID 过滤；普通用户通过 `user_id` 精确限制；实现侧已在 `alex_miaosha_user/user_api` 引入基于 `ConcurrentHashMap` 的注解缓存与 `ThreadLocal` 重入保护，避免反射热点与 `getLoginUser` 触发的死循环。
- `@DataPermission` 注解的 SQL 过滤仅在 service 显式调用挂注解的自定义 mapper 方法时生效；若 service 走 MyBatis-Plus `IService` 默认 `page(Page, Wrapper)`/`updateById`/`removeBatchByIds` 等，注解会被绕过、数据权限失效——需要测试用例覆盖跨用户/跨机构访问场景。
- 后端 VO 的 `Long` 主键/外键字段统一通过 `@JsonSerialize(using = Long2StringSerializer.class)` 序列化为前端字符串；前端 `views/finance/gift/api/index.ts` 中由 `normalizeGiftIds` 递归把所有 `*Id` 字段转 String，是\"ID 安全\"前后端契约的护城河，单测必须锁住该行为。
- AI 测试基建：PC 与移动端均采用 `@midscene/web` + `@playwright/test`，case 数据放在 `tests/midscene/{module}/cases/*.json`，执行脚本位于 `scripts/midscene/run-*-smoke.mjs`，分 `smoke/button/flow` 三层并通过 `super_super`/`rbac_user_manager`/`rbac_readonly` 三种 persona 跑权限矩阵；移动端使用 `installHapticProbe` 拦截 `navigator.vibrate` 做副作用断言，并默认配置 390×844 viewport + `isMobile` + `hasTouch`。
- 微服务在 Nacos 中的注册服务名分别为：用户微服务 `alex-miaosha@@alex-user-dev`（主端口 `30006`），OSS 存储微服务 `alex-miaosha@@alex-oss-dev`（主端口 `30009`）。
- Redis 缓存了所有的登录态和共享菜单树（Key 类似 `LoginKey:login:in:menu_all_tree`），在前端/后端做菜单重组或过滤渲染时，严禁修改/污染原始 children 的内存结构以防止 Redis 缓存共享干扰。
