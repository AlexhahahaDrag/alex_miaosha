# RBAC 成熟度评分卡与缺陷登记册

Date: 2026-08-06
Spec: `docs/superpowers/specs/2026-08-06-rbac-maturity-review-design.md`
Plan: `docs/superpowers/plans/2026-08-06-rbac-maturity-review-execution.md`
Branch: `develop-1.0-feature-org-manage`
Status: 评审进行中

门禁：`node scripts/rbac-scorecard-check.mjs`（分端推进时用 `--end BE|PC|MB`）。

## 1. 读法

- 端代码：`BE` 后端 `alex_miaosha_user`、`PC` `alex_miaosha_front`、`MB` `alex_miaosha_mobile`。
- 模块代码：`ORG` 机构、`USER` 用户、`ROLE` 角色、`MENU` 菜单、`PERM` 权限点、`RELATION` 关系配置、`SCOPE` 数据权限。
- 维度与权重：D1 安全与数据正确性 35%、D2 功能完整度 30%、D3 交互一致性 18%、D4 视觉规范符合度 7%、D5 可测性与回归保护 10%。
- 判据折算：勾中比例 0% → 0；(0%, 30%] → 1；(30%, 55%] → 2；(55%, 75%] → 3；(75%, 95%] → 4；(95%, 100%] → 5。
- 加权总分按**适用维度**归一化，故 `BE` 各格（D4 记 N/A）与前端各格不做单格直接排名，只在模块总分与单维度上横向比较。

## 2. 评分矩阵

<!-- matrix:start -->
| 端 | 模块 | D1 | D2 | D3 | D4 | D5 | 加权总分 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| BE | ORG | 1 | 3 | 4 | N/A | 0 | 42 |
| BE | USER | 3 | 3 | 4 | N/A | 3 | 64 |
| BE | ROLE | 1 | 3 | 3 | N/A | 3 | 45 |
| BE | MENU | 1 | 3 | 3 | N/A | 0 | 38 |
| BE | PERM | 1 | 3 | 3 | N/A | 0 | 38 |
| BE | RELATION | 1 | 3 | 4 | N/A | 2 | 47 |
| BE | SCOPE | 1 | 2 | 4 | N/A | 3 | 42 |
| PC | ORG | 5 | 2 | 3 | 2 | 2 | 65 |
| PC | USER | 5 | 3 | 3 | 2 | 2 | 71 |
| PC | ROLE | 5 | 3 | 3 | 2 | 2 | 71 |
| PC | MENU | 5 | 3 | 3 | 3 | 2 | 72 |
| PC | PERM | 5 | 3 | 3 | 2 | 2 | 71 |
| PC | RELATION | 5 | 3 | 3 | 2 | 2 | 71 |
| PC | SCOPE | 5 | N/A | 2 | N/A | 2 | 73 |
| MB | ORG | TBD | TBD | TBD | TBD | TBD | TBD |
| MB | USER | TBD | TBD | TBD | TBD | TBD | TBD |
| MB | ROLE | TBD | TBD | TBD | TBD | TBD | TBD |
| MB | MENU | TBD | TBD | TBD | TBD | TBD | TBD |
| MB | PERM | TBD | TBD | TBD | TBD | TBD | TBD |
| MB | RELATION | TBD | TBD | TBD | TBD | TBD | TBD |
| MB | SCOPE | TBD | N/A | TBD | N/A | TBD | TBD |
<!-- matrix:end -->

### 2.1 判据勾选明细

每格的勾中/剔除逐条记录在此，供复算。格式：`端-模块 Dn: 勾中 x/y → z 分`，附不勾原因一句。

### BE 判据在后端语境下的解释

D3 的 7 条判据是按前端交互写的，后端按下表对应解释，否则无法复算：

| D3 判据 | 后端解释 |
| --- | --- |
| 1 同类操作容器一致 | 接口风格一致：`POST /page` + `GET` + `POST` + `PUT` + `DELETE` 五件套，分页参数与返回结构统一 |
| 2 危险操作二次确认 | 删除类操作有服务端前置保护（下级存在性、引用完整性） |
| 3 提交 loading 防重 | 写接口挂 `@AvoidRepeatableCommit` |
| 4 表单校验规则 | 入参校验（唯一性、必填、DTO 约束） |
| 5 三端口径一致 | 同一接口能同时满足 PC 与 mobile，无端专属分支 |
| 6 复用共享组件 | 复用 `ServiceImpl` / `Result` / `Page` 等公共基类 |
| 7 命名与规范一致 | 命名规范 + 代码卫生（无乱码文案、无调试输出残留） |

D5 判据 3（自动化定位钩子）对后端一律剔除。判据 5（一条命令跑通）也一律剔除：本机 `JAVA_HOME` 指向缺失的 JDK 8、可用的是 JDK 21，而仓库声明 Java 17 + Lombok 1.18.24，`mvn test` 因 Lombok 与 JDK 21 不兼容而编译失败。这属于本机工具链问题而非 RBAC 缺陷（仓库声明本身自洽），故不计分也不登记，但它使 D5 的取证只能依赖静态 `@Test` 清点——见第 6 节阻塞项。

#### BE-ORG

- D1: 勾中 3/10 → 1 分（30%）。勾中：1 列表挂注解且 `field=id scope=ORG_ID` 语义正确、9 无敏感信息、10 单一写入口。不勾：2 `queryOrgInfo` 裸查无归属校验；3 `updateOrgInfo`/`deleteOrgInfo` 无归属校验；4 机构编码无唯一性校验；5 删除侧有下级机构与绑定用户双重保护，但新增/修改侧无父节点存在性与防环校验，三项只满足一项；6 全类无 `@Transactional`；7 机构变更不失效 `permission_context`；8 角色判定为子串包含（全局）。
- D2: 勾中 5/9 → 3 分（55.6%）。勾中：1 CRUD、2 筛选、3 服务端分页、4 `deleteOrgInfo` 支持逗号串批量、7 关系配置入口（`assignSingleOrg`）。不勾：5 无 tree 接口；6 无独立启停；8 stage1 契约要求的机构树未落地；9 无导出。
- D3: 勾中 6/7 → 4 分（85.7%）。不勾：4 `addOrgInfo` 无入参校验。
- D4: N/A。
- D5: 勾中 0/2 → 0 分。分母剔除判据 3 与 5，剩 1（有可执行测试）与 2（D1 路径覆盖），`rbac` 测试目录下无任何 Org 相关测试。

#### BE-USER

- D1: 勾中 6/10 → 3 分（60%）。勾中：1 `getPage`/`getList` 挂注解且 `field=id scope=USER_IDS` 语义正确、4 username/mobile/email 唯一性校验、5 删除有 `UserDeleteCleanupService` 级联清理、6 有 3 处显式事务、7 改用户与删用户均失效 `permission_context`、10 单一写入口。不勾：2 `queryTUser`/`getUserInfo` 未挂注解；3 更新无归属校验；8 角色判定子串包含；9 `main` 方法打印明文密码。
- D2: 勾中 5/8 → 3 分（62.5%）。剔除：5 树（用户无层级）。不勾：6 无独立启停；8 stage1 的左机构树筛选未落地；9 无导出。
- D3: 勾中 6/7 → 4 分（85.7%）。不勾：7 `TUserServiceImpl` 多处中文乱码。
- D4: N/A。
- D5: 勾中 2/3 → 3 分（66.7%）。剔除 3、5。勾中：1 `UserDeleteCleanupServiceTest` 6 个 `@Test` 可执行、4 测试与实现一致。不勾：2 密码外泄与详情越权无测试覆盖。

#### BE-ROLE

- D1: 勾中 3/10 → 1 分（30%）。勾中：1 `field=operator scope=USER_IDS` 语义正确、7 授权后按用户逐个删 `permission_context`、9 无敏感信息。不勾：2 详情裸查；3 写无归属校验；4 角色编码无唯一性校验；5 删角色不清理 `t_role_permission_info`；6 `RoleInfoServiceImp` 自身写操作无事务；8 子串包含；10 `RolePermissionInfoController` 裸 CRUD 与 `assignPermissions` 双轨。
- D2: 勾中 5/7 → 3 分（71.4%）。剔除：5 树、9 导出。不勾：6 无独立启停；8 stage1 的角色统计列未落地。
- D3: 勾中 5/7 → 3 分（71.4%）。不勾：2 删角色无引用完整性前置保护；4 `addRoleInfo`/`updateRoleInfo` 无入参校验。
- D4: N/A。
- D5: 勾中 2/3 → 3 分（66.7%）。勾中 1（`RolePermissionAssignmentServiceTest` 5 个 `@Test`）、4。不勾 2（越权与级联清理无测试）。

#### BE-MENU

- D1: 勾中 3/10 → 1 分（30%）。勾中：7 菜单变更失效 `menu_all_tree` 缓存、9、10。不勾：1 `MenuInfoMapper` 完全无 `@DataPermission`；2、3 无归属校验；4 无唯一性；5 删父菜单不检查子节点，产生孤儿；6 无事务；8 子串包含。
- D2: 勾中 4/7 → 3 分（57.1%）。剔除：7 关系配置（菜单本身不承载）、9 导出。不勾：5 无管理端 tree 接口；6 无启停；8 目标态未落地。
- D3: 勾中 5/7 → 3 分（71.4%）。不勾：2 删除无子节点保护；4 无入参校验。
- D4: N/A。
- D5: 勾中 0/3 → 0 分。不勾：1 `MenuPermissionFilterTest` 未挂 `@Test`，4 个用例一个都不执行；2；4 测试存在却不执行，与「有测试」的表象不符。

#### BE-PERM

- D1: 勾中 3/10 → 1 分（30%）。勾中：1 挂注解（默认 `field=operator scope=USER_IDS`）、9、10。不勾：2、3 无归属校验；4 权限码无唯一性；5 删权限点不清理 `t_role_permission_info`；6 无事务；7 权限点变更不失效 `permission_context`；8 子串包含。
- D2: 勾中 4/6 → 3 分（66.7%）。剔除：5 树、7 关系配置（在 role-permission）、9 导出。不勾：6 无启停；8 目标态未落地。
- D3: 勾中 5/7 → 3 分（71.4%）。不勾：2、4。
- D4: N/A。
- D5: 勾中 0/3 → 0 分。权限点模块无任何测试。

#### BE-RELATION

- D1: 勾中 2/10 → 1 分（20%）。勾中：6 `assignSingleOrg`/`assignRoles`/`assignPermissions` 均有显式事务与并发锁、9。不勾：1 `OrgUserInfoMapper` 与 `RoleUserInfoMapper` 无 `@DataPermission`（仅 `RolePermissionInfoMapper` 有）；2、3 裸 insert 无任何校验；4 不校验是否已存在有效机构关系；5 不校验 userId/orgId 存在性；7 换机构后不失效 `permission_context`；8 子串包含；10 裸 CRUD 可绕过 `assignSingleOrg` 的唯一有效机构约束。
- D2: 勾中 5/7 → 3 分（71.4%）。剔除：5 树、9 导出。不勾：6 关系有 `status` 字段但无独立启停接口；8 stage1 要求的独立配置页对应能力未落地。
- D3: 勾中 6/7 → 4 分（85.7%）。不勾：2 删除关系无前置保护。
- D5: 勾中 1/3 → 2 分（33.3%）。勾中：1 `RolePermissionAssignmentServiceTest` 可执行。不勾：2 双轨绕过与缓存失效无测试；4 `OrgUserAssignmentServiceTest` 与 `RoleUserAssignmentServiceTest` 共 8 个用例未挂 `@Test`，恰好覆盖本格最关键的唯一有效机构约束却完全不执行。
- D4: N/A。

#### BE-SCOPE

- D1: 勾中 2/7 → 1 分（28.6%）。剔除：4 唯一性、6 事务、10 双轨（本格无业务表写操作）。勾中：1 handler 支持 `USER_IDS` 与 `ORG_ID` 两种 scope 且已挂 5 个 mapper、9。不勾：2 详情查询全都未挂注解；3 handler 不拦写操作；5 `ORG_ID` scope 只按自身 org 过滤，不含子机构；7 `permission_context` 1 小时 TTL 使过滤规则滞后；8 `code.contains("super"/"admin"/"user")` 子串匹配。
- D2: 勾中 1/3 → 2 分（33.3%）。剔除：2、3、4、6、7、9（数据权限不承载列表类能力）。勾中：1 两种 scope 基础能力完整。不勾：5 不支持子机构递归；8 无自定义数据范围、无按角色配置 scope。
- D3: 勾中 4/5 → 4 分（80%）。剔除：2、3。勾中：1 注解口径统一、4 缺失 org 时用恒假条件兜底防泄漏、6、7。不勾：5 前端无从得知当前数据范围，三端对「admin 能看到什么」无统一表达。
- D4: N/A。
- D5: 勾中 2/3 → 3 分（66.7%）。勾中：1 `DataPermissionScopeHandlerTest` 5 个 `@Test`、4。不勾：2 子串角色判定与详情未挂注解这两条 S1 无测试。

### PC 判据适用说明

- D1 全部 7 格用**前端消费侧**判据（5 条）。
- D4 判据 3（卡片化与圆角基准）与 4（触觉反馈）对 PC 一律剔除：`alex_miaosha_front/.cursorrules` 未对 PC 提出卡片化圆角与触觉要求，这两条是 mobile 规范。PC 的 D4 分母为 5 条（加载态、空状态、侧距、无 emoji、无残留与乱码）。
- D1 的 5 条判据在本次取证中全部勾中，故 7 格 D1 一律 5 分。这是 wave2 已落地 ID string 化、`v-permission` 指令、以及 `src/utils/permission/index.ts:44-49` 多角色权限码 uniq 合并的直接结果，不是评分放水。前端在数据范围上的表达缺失落在 D3-5，不在 D1 重复扣分。
- PC-RELATION 按 spec 6.1 处理：三个关系目录只有 `api/index.ts` 而无页面，能力内嵌在用户表单与角色抽屉，D2 不记能力缺失，扣分落在 D3-5 与 D3-6。

#### PC-ORG

- D1: 勾中 5/5 → 5 分。ID 全程 string、`v-permission` 按钮级校验、仅做展示控制、多角色权限码合并、详情表单不回传审计字段。
- D2: 勾中 3/8 → 2 分（37.5%）。剔除：7 关系配置（见上）。勾中：1 CRUD、2 筛选、3 `usePagination` 服务端分页。不勾：4 机构是五个模块里唯一没有 `rowSelection` 与批量删除的；5 无机构树视图；6 无启停入口；8 stage1 机构 Drawer 与左树未落地；9 无导出。
- D3: 勾中 5/7 → 3 分（71.4%）。勾中：1 容器规则清晰（主详情 Modal、关系配置 Drawer）、2 `a-popconfirm` 二次确认、3 提交 loading、4 `rulesRef` 有 orgCode 与 orgName 必填、7 命名规范。不勾：5 PC 无关系配置页而 mobile 有，三端口径不一致；6 `src/components/rbac/*` 零引用。
- D4: 勾中 2/5 → 2 分（40%）。勾中：5 侧距、6 无 emoji。不勾：1 有 `:loading` 但无骨架屏；2 无 `a-empty` 空状态；7 `config/index.ts` 校验提示文案乱码。
- D5: 勾中 2/5 → 2 分（40%）。勾中：1 `scripts/playwright/run-rbac-smoke.mjs` 与 `test:rbac:smoke:local` 存在、5 一条命令可跑。不勾：2 越权与权限码合并无测试；3 无 `data-testid`；4 `stage1-smoke.json` 描述的目标 UI 与现状不符，冒烟脚本只能用文案与 `.ant-*` 类选择器定位。

#### PC-USER

- D1: 勾中 5/5 → 5 分。同 PC-ORG。
- D2: 勾中 5/8 → 3 分（62.5%）。剔除：5 树。勾中：1、2 wave2 已补筛选、3、4 `rowSelection` 与 `batchDelUserManager`、7 机构与角色维护内嵌在用户表单。不勾：6 无启停入口；8 stage1 左机构树筛选未落地；9 无导出。
- D3: 勾中 5/7 → 3 分（71.4%）。不勾：5、6。
- D4: 勾中 2/5 → 2 分（40%）。不勾：1、2、7（列表页残留 `console.log`，`config/index.ts` 文案乱码）。
- D5: 勾中 2/5 → 2 分（40%）。同 PC-ORG。

#### PC-ROLE

- D1: 勾中 5/5 → 5 分。
- D2: 勾中 5/7 → 3 分（71.4%）。剔除：5 树、9 导出。勾中：1、2、3、4、7（`authorizationDetail` 与 `userAssignmentDetail` 两个抽屉是清晰的关系配置入口）。不勾：6 无启停；8 stage1 角色统计列未落地。
- D3: 勾中 5/7 → 3 分（71.4%）。不勾：5、6（权限树用 `src/compoments/menu-tree` 而非 `rbac/RbacPermissionTreePanel`）。
- D4: 勾中 2/5 → 2 分（40%）。不勾：1、2、7。
- D5: 勾中 2/5 → 2 分（40%）。

#### PC-MENU

- D1: 勾中 5/5 → 5 分。
- D2: 勾中 5/7 → 3 分（71.4%）。剔除：7 关系配置、9 导出。勾中：1、2、3、4、5（`subMenuManager` 抽屉提供子菜单层级管理）。不勾：6 无启停；8 目标态未落地。
- D3: 勾中 4/7 → 3 分（57.1%）。不勾：4 `menuInfoDetail/index.vue:239` 的 `rulesRef = reactive({})` 是空对象，必填项完全不校验；5；6。
- D4: 勾中 3/5 → 3 分（60%）。勾中：5、6、7（本模块无 `console.log` 残留也无乱码文案）。不勾：1、2。
- D5: 勾中 2/5 → 2 分（40%）。

#### PC-PERM

- D1: 勾中 5/5 → 5 分。
- D2: 勾中 4/6 → 3 分（66.7%）。剔除：5 树、7 关系配置（在 role-permission）、9 导出。勾中：1、2、3、4。不勾：6、8。
- D3: 勾中 5/7 → 3 分（71.4%）。不勾：5、6。
- D4: 勾中 2/5 → 2 分（40%）。不勾：1、2、7（3 处 `console.log`）。
- D5: 勾中 2/5 → 2 分（40%）。

#### PC-RELATION

- D1: 勾中 5/5 → 5 分。关系维护的宿主页面同样满足 5 条消费侧判据。
- D2: 勾中 5/7 → 3 分（71.4%）。剔除：5 树、9 导出。勾中：1 三套 api 完整、2、3、4、7（入口存在，虽分散）。不勾：6 关系有 status 但无启停入口；8 stage1 的独立机构-用户与用户-角色配置页未落地。
- D3: 勾中 5/7 → 3 分（71.4%）。不勾：5 PC 内嵌而 mobile 独立成页，同一能力三端形态不同；6。
- D4: 勾中 2/5 → 2 分（40%）。按宿主页面评，不勾 1、2、7。
- D5: 勾中 2/5 → 2 分（40%）。

#### PC-SCOPE

- D1: 勾中 5/5 → 5 分。前端未对后端过滤规则做出错误假设，也未越权兜底。
- D2: N/A。
- D3: 勾中 2/4 → 2 分（50%）。剔除：2、3、4（本格无写操作）。勾中：1、7。不勾：5 页面完全不表达当前数据范围，用户看到空列表时无法区分「无数据」与「无权限」，而后端 `ORG_ID` scope 又不含子机构，三端对「管理员能看到什么」无统一口径；6。
- D4: N/A。
- D5: 勾中 2/5 → 2 分（40%）。勾中 1、5。不勾 2 数据范围表达无测试、3、4。

## 3. 缺陷登记册

证据写法 `<repo>/<路径>:<起行>-<止行>`，`repo` 取 `backend` `front` `mobile`，门禁会回仓核对文件与行号。

<!-- registry:start -->
| ID | 标题 | 端 | 模块 | 维度 | 严重级 | 证据 | 影响 | 修复方向 | 成本 | 验收 | 来源 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| RBAC-BE-USER-001 | 生产代码 main 方法打印明文密码 | BE | USER | D1 | S1 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/user/service/impl/TUserServiceImpl.java:195-200 | 该类被反射调用或误触发时明文密码进入标准输出与日志采集 | 删除 main 方法，需要本地试算改用测试类 | S | 静态断言：该文件不含 main 方法与 System.out | 新发现 |
| RBAC-BE-USER-002 | 用户详情与更新接口无数据权限与归属校验 | BE | USER | D1 | S1 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/user/mapper/TUserMapper.java:22-31 | 越权用户按 id 可直读或改写他机构用户，分页已过滤但详情未过滤形成缺口 | queryTUser 与 getUserInfo 挂 @DataPermission，写路径加服务层归属校验 | M | 集成测试：他机构用户 id 查询返回空、更新被拒 | wave1 Non-goals |
| RBAC-BE-USER-003 | 用户无独立启停接口 | BE | USER | D2 | S3 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/user/controller/TUserController.java:50-95 | 停用用户须走完整编辑表单，易误改其他字段 | 增加 PUT status 专用接口，仅接受 id 与目标状态 | S | 集成测试：仅传 id 与 status 即可切换状态 | wave2 与 role-assign-permissions Non-goals |
| RBAC-BE-USER-004 | UserPermissionContextServiceTest 未挂 @Test 不执行 | BE | USER | D5 | S3 | backend/alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/UserPermissionContextServiceTest.java:1-40 | 权限上下文构建这条核心链路的测试形同注释，改坏不会被发现 | 补 @Test 注解并改用 JUnit 断言替换手写静态断言 | S | Surefire 实际执行用例数上升 | 新发现 |
| RBAC-BE-USER-005 | TUserServiceImpl 多处中文文案乱码 | BE | USER | D3 | S4 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/user/service/impl/TUserServiceImpl.java:330-341 | 日志与注释可读性受损，排障时误导 | 按 UTF-8 重新写入受损文案 | S | 静态扫描：源码无 U+FFFD 替换字符 | 新发现 |
| RBAC-BE-ORG-001 | 机构详情与写接口无数据权限与归属校验 | BE | ORG | D1 | S1 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/orgInfo/mapper/OrgInfoMapper.java:20-23 | 分页已按 ORG_ID 过滤，但按 id 查询与更新删除完全不过滤，可越权读写他机构 | queryOrgInfo 挂注解，写路径加归属校验 | M | 集成测试：他机构 id 查询返回空、更新被拒 | wave1 Non-goals |
| RBAC-BE-ORG-002 | 机构新增与修改无唯一性、父节点存在性与防环校验 | BE | ORG | D1 | S2 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/orgInfo/service/impl/OrgInfoServiceImp.java:51-64 | 可造出重名机构与父子成环的机构树，树渲染将无限递归 | 新增改前校验编码唯一、父节点存在、父链不含自身 | M | 单测：重名被拒、自身设为父被拒、成环被拒 | 新发现 |
| RBAC-BE-ORG-003 | 无机构树接口 | BE | ORG | D2 | S3 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/orgInfo/controller/OrgInfoController.java:39-79 | 前端要展示机构树须自行拉全量再拼装，数据量增长后不可用 | 增加 tree 接口，服务端按 parentId 组装并复用数据权限过滤 | M | 集成测试：返回结构含 children 且受数据权限约束 | 新发现 |
| RBAC-BE-ORG-004 | 机构模块无任何自动化测试 | BE | ORG | D5 | S3 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/orgInfo/service/impl/OrgInfoServiceImp.java:51-91 | 删除前置保护这类已实现的正确逻辑无回归保护，后续重构易失守 | 为删除前置保护与新增校验补单测 | M | Surefire 出现 Org 相关用例且通过 | 新发现 |
| RBAC-BE-ROLE-001 | 角色详情与写接口无数据权限与归属校验 | BE | ROLE | D1 | S1 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/roleInfo/mapper/RoleInfoMapper.java:20-24 | 分页按 operator 过滤，详情与写不过滤，可越权改他人角色 | 详情挂注解，写路径加归属校验 | M | 集成测试：他人角色查询返回空、更新被拒 | wave1 Non-goals |
| RBAC-BE-ROLE-002 | 删除角色未级联清理 t_role_permission_info | BE | ROLE | D1 | S2 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/roleInfo/service/impl/RoleInfoServiceImp.java:107-120 | 残留权限关系行，角色 id 复用时会带回旧权限 | 删除角色时在同一事务内失效其权限与用户关系 | S | 单测：删角色后关系行为失效状态 | wave1 Non-goals |
| RBAC-BE-ROLE-003 | 角色新增与修改无唯一性与入参校验 | BE | ROLE | D1 | S2 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/roleInfo/service/impl/RoleInfoServiceImp.java:99-104 | 可造出同名同码角色，权限判定与展示都会歧义 | 新增改前校验角色编码唯一 | S | 单测：重复角色编码被拒 | 新发现 |
| RBAC-BE-MENU-001 | 菜单查询完全无数据权限 | BE | MENU | D1 | S2 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/menuInfo/mapper/MenuInfoMapper.java:1-26 | 任意登录用户可列出全部菜单定义，暴露未授权功能的存在与路径 | 分页与列表挂 @DataPermission，或在服务层按权限码过滤 | M | 集成测试：普通用户看不到未授权菜单 | 新发现 |
| RBAC-BE-MENU-002 | 删除父菜单不检查子节点，产生孤儿菜单 | BE | MENU | D1 | S2 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/menuInfo/service/impl/MenuInfoServiceImp.java:149-157 | 子菜单 parentId 悬空，菜单树渲染缺失或报错 | 删除前校验无有效子节点，或改为级联失效 | S | 单测：删有子节点的菜单被拒 | 新发现 |
| RBAC-BE-MENU-003 | MenuPermissionFilterTest 未挂 @Test，4 个用例不执行 | BE | MENU | D5 | S3 | backend/alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/MenuPermissionFilterTest.java:10-58 | 菜单过滤不污染 Redis 共享 children 这条关键约束无实际保护 | 补 @Test 并改用 JUnit 断言 | S | Surefire 实际执行用例数上升 | 新发现 |
| RBAC-BE-MENU-004 | 无管理端菜单树接口 | BE | MENU | D2 | S3 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/menuInfo/controller/MenuInfoController.java:41-92 | menu_all_tree 缓存只服务登录态，管理端须自行拼树 | 增加管理端 tree 接口，与登录态缓存分开 | M | 集成测试：返回含 children 的树结构 | 新发现 |
| RBAC-BE-PERM-001 | 权限点详情与写接口无归属校验 | BE | PERM | D1 | S2 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/permissionInfo/mapper/PermissionInfoMapper.java:22-28 | 可越权读改他人创建的权限点定义 | 详情挂注解，写路径加归属校验 | M | 集成测试：他人权限点更新被拒 | wave1 Non-goals |
| RBAC-BE-PERM-002 | 删除权限点未清理 t_role_permission_info | BE | PERM | D1 | S2 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/permissionInfo/service/impl/PermissionInfoServiceImp.java:50-93 | 残留关系行，权限点 id 复用时角色会拿到意外权限 | 删除时在同一事务内失效关联关系 | S | 单测：删权限点后关系行失效 | 新发现 |
| RBAC-BE-PERM-003 | 权限码无唯一性校验 | BE | PERM | D1 | S2 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/permissionInfo/service/impl/PermissionInfoServiceImp.java:50-93 | 同一权限码存在多条定义，按码判权时行为不确定 | 新增改前校验权限码唯一 | S | 单测：重复权限码被拒 | 新发现 |
| RBAC-BE-PERM-004 | 权限点模块无任何自动化测试 | BE | PERM | D5 | S3 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/permissionInfo/service/impl/PermissionInfoServiceImp.java:50-93 | 权限码唯一性与关系清理修好后无回归保护 | 补服务层单测 | M | Surefire 出现权限点相关用例 | 新发现 |
| RBAC-BE-RELATION-001 | 关系裸 CRUD 可绕过 assignSingleOrg 的唯一有效机构约束 | BE | RELATION | D1 | S1 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/orgUserInfo/service/impl/OrgUserInfoServiceImp.java:52-74 backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/orgUserInfo/controller/OrgUserInfoController.java:66-74 | assignSingleOrg 用锁与事务保证单用户唯一有效机构，而 POST 裸 insert 完全不校验，可造出多条 status=1 记录使数据权限过滤结果不确定 | 关系表写入口收敛到 assign 语义，裸 CRUD 下线或改为仅内部调用 | M | 集成测试：直接 POST 造第二条有效关系被拒 | 新发现 |
| RBAC-BE-RELATION-002 | 换机构与改角色后不失效 permission_context 缓存 | BE | RELATION | D1 | S2 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/orgUserInfo/service/impl/OrgUserInfoServiceImp.java:78-90 | 缓存 TTL 一小时，期间用户的数据权限仍按旧机构过滤，既可能越权也可能看不到本机构数据 | assignSingleOrg 与 assignRoles 成功后删除该用户的 permission_context | S | 单测：assign 后缓存键被删除 | 新发现 |
| RBAC-BE-RELATION-003 | org-user 与 role-user 分页无数据权限 | BE | RELATION | D1 | S2 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/orgUserInfo/mapper/OrgUserInfoMapper.java:1-27 backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/roleUserInfo/mapper/RoleUserInfoMapper.java:1-28 | 可枚举全部机构-用户与角色-用户绑定关系，间接泄漏组织结构 | 两个 mapper 的分页挂 @DataPermission | S | 集成测试：SQL 含过滤条件且跨机构行不可见 | 新发现 |
| RBAC-BE-RELATION-004 | 历史失效关系行持续累积无清理机制 | BE | RELATION | D2 | S3 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/orgUserInfo/service/impl/OrgUserInfoServiceImp.java:78-90 | 频繁调岗后单用户堆积大量 status=0 行，分页与子查询逐步变慢 | 增加归档或定期清理任务，或改为只保留最近 N 条 | M | 压测或计数断言：调岗 100 次后有效行仍为 1 | role-assign-permissions Risks |
| RBAC-BE-RELATION-005 | 两个关系测试类未挂 @Test，8 个用例不执行 | BE | RELATION | D5 | S3 | backend/alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/OrgUserAssignmentServiceTest.java:18-60 backend/alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/RoleUserAssignmentServiceTest.java:1-40 | 唯一有效机构约束与事务边界这两条本格最关键的保证毫无实际验证 | 补 @Test 并改用 JUnit 断言 | S | Surefire 实际执行用例数上升 8 | 新发现 |
| RBAC-BE-SCOPE-001 | 角色判定用子串包含，可被角色编码命名绕过 | BE | SCOPE | D1 | S1 | backend/alex_miaosha_user/user_api/src/main/java/com/alex/api/user/handler/DataPermissionHandlerImpl.java:92-96 | 名为 superviser 或 badmin 的普通角色会被判为超管或机构管理员，直接拿到全量数据 | 改为与角色编码常量精确相等匹配 | S | 单测：superviser 不被判为 super | 新发现 |
| RBAC-BE-SCOPE-002 | ORG_ID scope 不含子机构，管理员看不到下级 | BE | SCOPE | D1,D2 | S3 | backend/alex_miaosha_user/user_api/src/main/java/com/alex/api/user/handler/DataPermissionHandlerImpl.java:155-160 | 多层机构下管理员无法管理下级机构数据，与机构树能力矛盾 | 支持按机构子树递归的 scope，需先确认产品是否要下级可见 | L | 集成测试：父机构管理员可见子机构行 | wave1 Risks（产品限制） |
| RBAC-BE-SCOPE-003 | 详情查询与写操作完全不经过数据权限 | BE | SCOPE | D1 | S1 | backend/alex_miaosha_user/user_api/src/main/java/com/alex/api/user/handler/DataPermissionHandlerImpl.java:130-140 | handler 只作用于挂注解的方法，全部详情与写路径成为统一的越权缺口 | 建立注解覆盖清单并在测试中断言无遗漏方法 | M | 静态或反射测试：查询与写方法均已覆盖 | wave1 Non-goals |
| RBAC-BE-SCOPE-004 | permission_context 一小时 TTL 使过滤规则滞后 | BE | SCOPE | D1 | S2 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/rbac/service/impl/UserPermissionContextServiceImpl.java:100-110 | 权限变更后最长一小时内过滤仍按旧上下文，与 RELATION-002 叠加放大 | 变更侧主动失效，TTL 仅作兜底 | S | 单测：权限变更后再次构建拿到新上下文 | 新发现 |
| RBAC-PC-ORG-001 | 机构列表无批量选择与批量删除 | PC | ORG | D2 | S3 | front/src/views/user/orgInfo/index.vue:73-145 | 机构是五个管理模块里唯一没有批量能力的，批量维护须逐条点 | 补 rowSelection 与批量删除按钮，复用其余四个模块的现成写法 | S | midscene：勾选两行后批量删除成功 | wave2 Non-goals |
| RBAC-PC-ORG-002 | 无机构树视图，stage1 目标 UI 未落地 | PC | ORG | D2,D3 | S3 | front/tests/midscene/rbac/cases/stage1-smoke.json:1-40 front/src/components/rbac/index.ts:1-11 | 契约文档描述的机构 Drawer 与左树都没实现，共享组件在库零引用，文档态与代码态脱节 | 后端补 tree 接口后接 rbac-permission-tree-panel 与 base-rbac-drawer | L | 静态检查：src/views/user 出现 components/rbac 引用且 stage1 用例通过 | 新发现（组件已由 front 3f3b605 归位本分支） |
| RBAC-PC-ORG-003 | 机构表单校验提示文案乱码 | PC | ORG | D4 | S4 | front/src/views/user/orgInfo/config/index.ts:66-79 | 用户提交空表单时看到乱码提示，直接可见 | 按 UTF-8 重写受损文案 | S | 静态扫描：config 目录无 U+FFFD | 新发现 |
| RBAC-PC-ORG-004 | RBAC 页面无空状态与骨架屏 | PC | ORG | D4 | S4 | front/src/views/user/orgInfo/index.vue:100-150 | 首屏与空结果都只有转圈，用户无法区分加载中与无数据 | 列表补 a-empty 空态与骨架屏占位 | M | 视觉检查加静态检查：出现 a-empty 与骨架屏 | 新发现 |
| RBAC-PC-ORG-005 | RBAC 页面无 data-testid，自动化只能靠文案选择器 | PC | ORG | D5 | S3 | front/scripts/playwright/run-rbac-smoke.mjs:210-240 | 冒烟脚本用 button:has-text 与 .ant-* 类定位，文案或组件版本一变就断，且脚本自身文案已乱码 | 给列表、按钮、表单项补 data-testid 并改写选择器 | M | grep 确认目标页面已有 data-testid 且冒烟改用其定位 | 新发现 |
| RBAC-PC-USER-001 | 用户列表无独立启停入口 | PC | USER | D2 | S3 | front/src/views/user/userManager/index.vue:70-110 | 停用用户须打开完整编辑弹窗 | 列表加状态开关，调后端启停接口 | S | midscene：列表内切换状态成功 | wave2 与 role-assign-permissions Non-goals |
| RBAC-PC-USER-002 | 用户列表残留 console.log 与文案乱码 | PC | USER | D4 | S4 | front/src/views/user/userManager/index.vue:198-220 front/src/views/user/userManager/config/index.ts:98-110 | 生产构建输出调试信息，校验提示乱码用户可见 | 删除 console.log，按 UTF-8 重写文案 | S | 静态扫描：无 console.log 与 U+FFFD | 新发现 |
| RBAC-PC-USER-003 | 用户页无 data-testid | PC | USER | D5 | S3 | front/src/views/user/userManager/index.vue:70-110 | 同 RBAC-PC-ORG-005，用户页是冒烟脚本的主路径，最需要稳定钩子 | 补 data-testid | S | grep 确认覆盖 | 新发现 |
| RBAC-PC-ROLE-001 | 权限树未复用 rbac 共享组件 | PC | ROLE | D3 | S3 | front/src/views/user/roleInfo/authorizationDetail/index.vue:1-40 front/src/components/rbac/index.ts:1-11 | 授权抽屉用 src/compoments/menu-tree 自行实现，与 RbacPermissionTreePanel 双份维护 | 授权抽屉切换到 rbac 共享组件 | M | 静态检查：authorizationDetail 引用 components/rbac | 新发现 |
| RBAC-PC-ROLE-002 | 角色列表残留 console.log 与文案乱码 | PC | ROLE | D4 | S4 | front/src/views/user/roleInfo/index.vue:165-185 front/src/views/user/roleInfo/config/index.ts:51-60 | 同 RBAC-PC-USER-002 | 删除调试输出并修文案 | S | 静态扫描通过 | 新发现 |
| RBAC-PC-ROLE-003 | 角色页无 data-testid | PC | ROLE | D5 | S3 | front/src/views/user/roleInfo/index.vue:45-100 | 授权与分配用户两个抽屉是 stage1 用例的核心断言点，缺钩子无法稳定验收 | 补 data-testid | S | grep 确认覆盖 | 新发现 |
| RBAC-PC-MENU-001 | 菜单详情表单校验规则是空对象 | PC | MENU | D3 | S2 | front/src/views/user/menuInfo/menuInfoDetail/index.vue:239-240 | rulesRef = reactive({}) 使必填项完全不校验，空菜单名与空权限标识可直接落库 | 按后端非空字段补 rules，与其余四模块一致从 config 导入 | S | midscene：提交空表单被拦下 | 新发现 |
| RBAC-PC-MENU-002 | 菜单页无 data-testid | PC | MENU | D5 | S3 | front/src/views/user/menuInfo/index.vue:155-215 | 同 RBAC-PC-ORG-005 | 补 data-testid | S | grep 确认覆盖 | 新发现 |
| RBAC-PC-PERM-001 | 权限点列表残留 console.log | PC | PERM | D4 | S4 | front/src/views/user/permissionInfo/index.vue:200-212 | 生产构建输出调试信息 | 删除 console.log | S | 静态扫描无 console.log | 新发现 |
| RBAC-PC-PERM-002 | 权限点页无 data-testid | PC | PERM | D5 | S3 | front/src/views/user/permissionInfo/index.vue:90-140 | 同 RBAC-PC-ORG-005 | 补 data-testid | S | grep 确认覆盖 | 新发现 |
| RBAC-PC-RELATION-001 | 关系配置无独立页面，入口分散且三端形态不一致 | PC | RELATION | D3 | S3 | front/src/views/user/orgUserInfo/api/index.ts:1-50 front/src/views/user/roleUserInfo/api/index.ts:1-56 | 三套 api 齐备但无页面，PC 内嵌在用户表单与角色抽屉，mobile 却是独立页，同一能力两种形态，用户跨端认知成本高 | 先定产品形态再收敛：要么 PC 补独立配置页，要么 mobile 改为内嵌 | L | 三端形态一致且 stage1 用例通过 | 新发现 |
| RBAC-PC-RELATION-002 | 关系配置宿主页无空状态与调试残留清理 | PC | RELATION | D4,D5 | S4 | front/src/views/user/roleInfo/authorizationDetail/index.vue:1-40 | 关系配置抽屉无空态提示也无 data-testid，无法自动化验收 | 随宿主页面一并补空态与 data-testid | S | 静态检查通过 | 新发现 |
| RBAC-PC-SCOPE-001 | 前端完全不表达当前数据范围 | PC | SCOPE | D3 | S3 | front/src/utils/permission/index.ts:95-105 | 用户看到空列表时无法区分无数据与无权限；后端 ORG_ID scope 又不含子机构，管理员会误以为下级机构没有数据 | 列表页展示当前数据范围提示，与后端 scope 口径对齐 | M | midscene：管理员登录后页面显示数据范围说明 | wave1 Risks（产品限制） |
| RBAC-PC-SCOPE-002 | 数据范围表达无任何测试覆盖 | PC | SCOPE | D5 | S3 | front/tests/midscene/rbac/cases/stage1-smoke.json:1-40 | 数据权限的消费侧行为改坏了不会被发现 | 补一条不同角色登录后可见范围的用例 | M | 新增用例通过 | 新发现 |
<!-- registry:end -->

## 4. 汇总

（Task 5 填：端总分、模块总分、拉后腿的端、Top 风险格）

## 5. 既有 spec 遗留项映射

（Task 5 填）

## 6. 批次归类与阻塞项

（Task 6 填）
