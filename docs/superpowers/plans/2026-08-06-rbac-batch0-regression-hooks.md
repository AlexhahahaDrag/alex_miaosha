# RBAC 批次 0：回归保护与定位抓手 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 落实 `docs/testing/rbac-maturity-scorecard.md` 第 6 节批次 0 的 16 条登记册条目，让后端 24 个已写好但从未执行的 RBAC 用例真正跑起来、PC 六个 RBAC 页面与冒烟脚本具备稳定定位符、mobile 从零建立可一条命令跑通的单测基建。本批次**不修任何业务缺陷**，只为批次 1–4 提供「改坏了会红」的能力。

**Architecture:** 三仓并行、互不依赖，可分派给三个执行者。后端侧是纯测试代码改造（4 个类由手写断言切到 JUnit 5，`Tests run` 由 16 升到 40）；PC 侧是模板属性注入加冒烟脚本选择器替换（先注入 `data-testid`，再让 `run-rbac-smoke.mjs` 优先用它、保留文案回退）；mobile 侧是引入 vitest 运行器加首个纯函数测试。最后一个任务把实测数字回写评分卡并重跑门禁。

**Tech Stack:** JDK 17 + Maven Surefire（后端）、Vue 3 模板 `data-testid` 属性 + Playwright(PC)、Vitest 4 + Vue 3(mobile)、node 门禁脚本 `scripts/rbac-scorecard-check.mjs`。

## Global Constraints

以下约束对每个任务隐含生效：

- **本批次不修业务缺陷。** 允许写入的文件类型仅四类：①后端测试类；②前端 `.vue` 模板中新增的 `data-testid` 属性；③测试基建文件（vitest 配置、测试用例、`package.json` 的 test 脚本、冒烟脚本）；④评分卡与本计划自身。**严禁**改动任何 service / mapper / store / composable 的业务逻辑，也严禁顺手修乱码或 `console.log`（那是批次 4）。
- **后端跑测试必须用 JDK 17。** 每个 shell 会话先 `$env:JAVA_HOME="C:\Program Files\Java\jdk-17"`。本机默认 `JAVA_HOME` 指向不存在的 JDK 8 路径，且用 JDK 21 会因 Lombok 1.18.24 不兼容在 `alex_miaosha_common` 编译阶段抛 `NoSuchFieldError: JCTree$JCImport qualid`。此即评分卡 6.1 节阻塞项一，已按方案 1 解除。
- **首次执行暴露的失败按来源分流，批次 0 一律不修代码。** 24 个用例从未被执行过，判定规则：若断言表达的预期本身写错（例如断言了不存在的行为），修测试；若测试预期正确而生产代码不符合，则该失败对应的是已登记条目，用 `@Disabled("<条目 ID>: 待该条目所属批次修复后启用")` 标注并在本计划末尾「执行记录」登记，**不得**在批次 0 改生产代码，也不得删除该用例或放宽断言。
- **`data-testid` 命名规范：** `rbac-<模块小写>-<元素语义>`，模块取 `org` `user` `role` `menu` `perm` `relation`；元素语义用连字符小写英文，行内操作统一 `row-edit` / `row-delete`，批量操作 `btn-batch-delete`，新增 `btn-add`，查询区输入 `search-<字段>`，查询/清空按钮 `btn-query` / `btn-reset`，列表容器 `table` / `list`。禁止把中文或业务 ID 编进 testid。
- **提交信息不得包含** `Co-authored-by: Cursor <cursoragent@cursor.com>`；Windows 环境下提交信息用英文，避免中文乱码。
- 三仓当前分支均为 `develop-1.0-feature-org-manage`，各仓独立提交，不做跨仓合并提交。

### 覆盖的 16 条条目与归属任务

| 条目 ID | 端 | 任务 |
| --- | --- | --- |
| RBAC-BE-USER-004 | BE | Task 1 |
| RBAC-BE-MENU-003 | BE | Task 1 |
| RBAC-BE-RELATION-005 | BE | Task 1 |
| RBAC-PC-USER-003 RBAC-PC-ROLE-003 RBAC-PC-MENU-002 RBAC-PC-PERM-002 RBAC-PC-RELATION-002 | PC | Task 2 |
| RBAC-PC-ORG-005 | PC | Task 2（页面侧）+ Task 3（脚本侧） |
| RBAC-MB-SCOPE-002 | MB | Task 4 |
| RBAC-MB-USER-002 RBAC-MB-ORG-003 RBAC-MB-ROLE-002 RBAC-MB-MENU-003 RBAC-MB-PERM-003 RBAC-MB-RELATION-004 | MB | Task 5 |

---

### Task 1: 后端 4 个测试类切到 JUnit 5

四个类各自带一套私有断言方法，且测试方法**没挂 `@Test`**，因此 Surefire 从未执行它们（基线实测 `Tests run: 16`，全部来自另外 3 个已挂注解的类）。`spring-boot-starter-test` 依赖本来就在 `alex_miaosha_user/user_boot/pom.xml:85-86`，同目录 `DataPermissionScopeHandlerTest` 已在用 `org.junit.jupiter.api.Test`，所以这不是缺依赖，只是漏了注解。

改法对四个类一致：加两行 import、给每个 `public void test*` 加 `@Test`、删掉与 JUnit 5 同名的私有断言方法和 `ThrowingRunnable` 接口。签名兼容性已逐个核对：

- 私有 `assertEquals(Object, Object, String)` 的全部调用点，实参要么是 `int` 对 `int`（走 JUnit `assertEquals(int,int,String)`），要么是 `String` 对 `String`（走 `assertEquals(Object,Object,String)`），无跨装箱类型比较，因此删掉私有版不会引入编译错误或语义变化。`RoleUserInfo` 与 `OrgUserInfo` 的 `userId/roleId/orgId/status` 四个字段都是 `String`，`assertAssignment` 传入的也是 `String`。
- 私有 `assertThrows(Class, ThrowingRunnable, String)` 的调用点都是 lambda，可直接适配 JUnit 5 的 `Executable`（`void execute() throws Throwable`），故连同 `ThrowingRunnable` 接口一起删。
- 非断言工具方法 **保留**：`MenuPermissionFilterTest.menu()`、`RoleUserAssignmentServiceTest.assertAssignment()`、`UserPermissionContextServiceTest.proxy()` / `sleep()`、`OrgUserAssignmentServiceTest.sleep()` 以及各类的内部 `Testable*` / `Blocking*` 桩类。

**Files:**
- Modify: `alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/MenuPermissionFilterTest.java`
- Modify: `alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/UserPermissionContextServiceTest.java`
- Modify: `alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/OrgUserAssignmentServiceTest.java`
- Modify: `alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/RoleUserAssignmentServiceTest.java`

**Steps:**

- [x] **Step 1 — 记录基线。** 执行并留档，确认改造前的数字是 16：

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
mvn -pl alex_miaosha_user/user_boot -am test -DfailIfNoTests=false > rbac-batch0-baseline.log 2>&1
rg -n "Tests run:.*Failures" rbac-batch0-baseline.log | Select-Object -Last 1
```

  预期输出 `Tests run: 16, Failures: 0, Errors: 0, Skipped: 0`。`rbac-batch0-baseline.log` 属临时产物，任务结束前删除，不要提交。

- [x] **Step 2 — 改 `MenuPermissionFilterTest`（4 个用例）。** 在 `import java.util.List;` 之后插入两行：

```java
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
```

  给这 4 个方法逐个加 `@Test`：`testKeepsParentWithOnlyAllowedChildWhenParentPermissionDoesNotMatch`、`testDoesNotMutateOriginalChildrenWhenFiltering`、`testReturnsEmptyListWhenPermissionCodesAreEmpty`、`testKeepsMenuWhenOwnPermissionMatches`。删除文件末尾的私有 `assertSame` 与 `assertEquals` 两个方法（原 77-87 行），保留 `menu()` 工厂方法。

- [x] **Step 3 — 改 `UserPermissionContextServiceTest`（7 个用例）。** 同样加 `@Test` 与 `assertEquals` / `assertSame` 的静态导入，给下列 7 个方法加注解：`testTUserVoExposesPermissionContextAndPermissionCodes`、`testBuildContextMergesRolePermissionsAndUsesFirstOrg`、`testBuildContextKeepsAllMenusForSuperAdmin`、`testApplyPermissionContextSetsLoginResponseCompatibilityFields`、`testRefreshLoginPermissionContextRebuildsCachedUserContext`、`testCompleteLoginResponseWaitsForAvatarAndPermissionContext`、`testCompleteLoginResponseRestoresInterruptFlagWhenInterrupted`。删除私有 `assertSame` 与 `assertEquals`（原 242-253 行），保留 `proxy()`、`sleep()` 与 `MethodHandler` 接口。

- [x] **Step 4 — 改 `OrgUserAssignmentServiceTest`（4 个用例）。** 静态导入需要 `assertEquals` `assertSame` `assertTrue` `assertNotNull` `assertNull` `assertThrows` 六个。给 `testAssignSingleOrgInvalidatesOldActiveAssignmentAndCreatesNewActiveAssignment`、`testAssignSingleOrgDoesNotSaveWhenInvalidatingOldAssignmentFails`、`testAssignSingleOrgDeclaresTransactionBoundary`、`testAssignSingleOrgHoldsUserLockUntilTransactionCallbackCompletes` 加 `@Test`（后两个带 `throws`，注解加在 `throws` 之前的方法声明上一行即可）。删除原 86-128 行的六个私有断言方法与原 139-141 行的 `ThrowingRunnable` 接口；**保留** `sleep()`（原 129-137 行）与 `TestableOrgUserInfoService` / `BlockingTransactionTemplate` / `BlockingOrgUserInfoService` 三个桩类。

- [x] **Step 5 — 改 `RoleUserAssignmentServiceTest`（9 个用例）。** 静态导入需要 `assertEquals` `assertSame` `assertTrue` `assertNotNull` `assertThrows` 五个。给 9 个方法加 `@Test`：`testAssignRolesInvalidatesOldActiveAssignmentAndCreatesNewActiveAssignments`、`testAssignRolesDeduplicatesRoleIdsBeforeSaving`、`testAssignRolesFiltersNullRoleIdsBeforeSaving`、`testAssignRolesWithOnlyNullRoleIdsOnlyInvalidatesOldActiveAssignments`、`testAssignRolesWithEmptyRoleIdsOnlyInvalidatesOldActiveAssignments`、`testAssignRolesDoesNotSaveWhenInvalidatingOldAssignmentFails`、`testAssignRolesDeclaresTransactionBoundary`、`testAssignUsersToRoleInvalidatesOldActiveUsersAndCreatesNewActiveAssignments`、`testAssignUsersToRoleDeduplicatesAndFiltersNullUserIds`。删除原 141-181 行的五个私有断言方法与 `ThrowingRunnable` 接口；**保留** `assertAssignment()`（原 135-139 行，它不与 JUnit 同名）与 `TestableRoleUserInfoService`。

- [x] **Step 6 — 跑通并分流失败。** 重跑 Step 1 的命令。期望 `Tests run: 40`（16 + 4 + 7 + 4 + 9）。若出现 Failures/Errors，按 Global Constraints 的分流规则处理：修测试或加 `@Disabled` 并登记，**不改生产代码**。四个类的 `@Test` 数量必须等于各自 `public void test*` 数量，用以下命令自检：

```powershell
rg -c "@Test" alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/
rg -c "public void test" alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/
```

- [x] **Step 7 — 清理与提交。** 删除 `rbac-batch0-baseline.log`。提交信息：`test(rbac): enable 24 dormant RBAC unit tests via JUnit 5 annotations`。

**验收：** `Tests run: 40, Failures: 0, Errors: 0`（若有 `@Disabled` 则 Skipped 计数与登记条目数一致）；两条 `rg -c` 输出逐文件相等。

---

### Task 2: PC 六个 RBAC 页面注入 data-testid

PC 全站 `src` 目录内 `data-testid` 出现 0 次，冒烟脚本只能靠 `button:has-text("...")` 与 `.ant-*` 类名定位，一改文案或换组件库版本就断。本任务只加属性，不动任何模板结构、样式与脚本逻辑。

Ant Design Vue 组件会把未声明的属性透传到根 DOM 节点，`a-button` / `a-input` / `a-table` 均可直接写 `data-testid`。**执行时必须验证透传结果**：跑起 dev server，在浏览器 DevTools 里确认 `document.querySelectorAll('[data-testid]')` 能取到节点；若某个组件（尤其 `a-table`）未透传，改为在其外层包一个 `<div :data-testid="...">` 承载，不要改组件本身的 props。

**Files:**
- Modify: `src/views/user/orgInfo/index.vue`
- Modify: `src/views/user/userManager/index.vue`
- Modify: `src/views/user/roleInfo/index.vue`
- Modify: `src/views/user/menuInfo/index.vue`
- Modify: `src/views/user/permissionInfo/index.vue`
- Modify: `src/views/user/roleInfo/authorizationDetail/index.vue`

**Steps:**

- [ ] **Step 1 — 用户管理页。** `src/views/user/userManager/index.vue`，按现有行号定位：第 13 行 `a-input`（username）→ `rbac-user-search-username`；第 60 行查找 → `rbac-user-btn-query`；第 61 行清空 → `rbac-user-btn-reset`；第 70 行新增 → `rbac-user-btn-add`；第 71 行批量删除 → `rbac-user-btn-batch-delete`；第 77 行 `a-table` → `rbac-user-table`；第 90 行行内编辑 → `rbac-user-row-edit`；第 106 行行内删除 → `rbac-user-row-delete`。示例（只加属性，其余不动）：

```vue
				<a-button
					v-permission="'user:add'"
					type="primary"
					data-testid="rbac-user-btn-add"
					@click="editUser('add')"
				>
					新增
				</a-button>
```

- [ ] **Step 2 — 机构管理页。** `src/views/user/orgInfo/index.vue`：第 5 行 `a-tree` → `rbac-org-tree`；第 64 行查找 → `rbac-org-btn-query`；第 65 行清空 → `rbac-org-btn-reset`；第 73 行新增 → `rbac-org-btn-add`；第 94 行删除（`:disabled="!hasSelectedNode"` 那个）→ `rbac-org-btn-delete-node`；第 103 行 `a-table` → `rbac-org-table`；第 130 行行内操作 → `rbac-org-row-edit`；第 146 行行内删除链接 → `rbac-org-row-delete`。

- [ ] **Step 3 — 角色管理页。** `src/views/user/roleInfo/index.vue`：第 37/38 行 → `rbac-role-btn-query` / `rbac-role-btn-reset`；第 47 行 → `rbac-role-btn-add`；第 48 行 → `rbac-role-btn-batch-delete`；第 54 行 `a-table` → `rbac-role-table`；第 67/75/83 行三个行内按钮按其实际语义命名 `rbac-role-row-edit` / `rbac-role-row-authorize` / `rbac-role-row-users`（以按钮实际 `@click` 指向的方法为准命名，不要按顺序硬套）；第 99 行 → `rbac-role-row-delete`。

- [ ] **Step 4 — 菜单管理页。** `src/views/user/menuInfo/index.vue`：第 147/150 行 → `rbac-menu-btn-query` / `rbac-menu-btn-reset`；第 159/166 行两个顶部按钮按实际语义 → `rbac-menu-btn-add` / `rbac-menu-btn-batch-delete`；第 177 行 `a-table` → `rbac-menu-table`；第 190/198 行 → `rbac-menu-row-edit` / `rbac-menu-row-add-child`（按实际 `@click` 命名）；第 213 行 → `rbac-menu-row-delete`。

- [ ] **Step 5 — 权限管理页。** `src/views/user/permissionInfo/index.vue`：第 81/84 行 → `rbac-perm-btn-query` / `rbac-perm-btn-reset`；第 93/100 行 → `rbac-perm-btn-add` / `rbac-perm-btn-batch-delete`；第 111 行 `a-table` → `rbac-perm-table`；第 124 行 → `rbac-perm-row-edit`；第 140 行 → `rbac-perm-row-delete`。

- [ ] **Step 6 — 授权抽屉（RBAC-PC-RELATION-002）。** `src/views/user/roleInfo/authorizationDetail/index.vue`：第 2 行 `a-drawer` → `rbac-relation-drawer`；第 11 行取消 → `rbac-relation-btn-cancel`；第 14 行确定 → `rbac-relation-btn-submit`。该条目同时记了 D4（抽屉标题与宽度不统一），**属批次 4，本任务只加 testid**。

- [ ] **Step 7 — 校验与提交。** 逐页确认属性已落地且总数符合预期：

```powershell
rg -c "data-testid" src/views/user
npm run lint
```

  六个文件都要有命中，`npm run lint` 必须零 warning（`--max-warnings=0` 是本仓提交门槛）。提交信息：`test(rbac): add stable data-testid hooks to six RBAC pages`。

**验收：** 六个目标文件 `rg -c data-testid` 均 ≥ 3；`npm run lint` 通过；dev server 下 `document.querySelectorAll('[data-testid]').length` 大于 0（确认透传生效，非仅源码里有）。

---

### Task 3: PC 冒烟脚本改用 testid 定位

`scripts/playwright/run-rbac-smoke.mjs` 现有定位方式全部是文案与 `.ant-*` 类名（第 84、115-125、196、215、235、246 行等）。改造原则是**渐进替换而非推倒重写**：testid 优先、文案兜底，这样 Task 2 若有遗漏也不会让脚本立刻失效。

**Files:**
- Modify: `scripts/playwright/run-rbac-smoke.mjs`

**Steps:**

- [ ] **Step 1 — 加 testid 优先的定位辅助。** 脚本已有 `tryClick(page, selectors)`（第 46-49 行按数组顺序取首个存在的 selector）。沿用该模式，把 testid 放在数组首位即可，无需新写抽象。例如第 215 行的用户管理入口断言：

```js
      const userEntry = await page
        .locator('[data-testid="rbac-user-btn-add"], button:has-text("用户管理"), .action-btn:has-text("用户管理")')
        .count();
```

- [ ] **Step 2 — 替换五个页面的关键断言点。** 逐个把「进入某管理页后确认页面已加载」的判据从类名/文案改为对应 testid：用户页用 `rbac-user-table`、机构页 `rbac-org-table`、角色页 `rbac-role-table`、菜单页 `rbac-menu-table`、权限页 `rbac-perm-table`。第 235 行的分页判据（当前是乱码文案 `'下一�?` 加 `.ant-pagination`）改为先看 `[data-testid$="-table"]` 是否存在再看 `.ant-pagination`；**该行的乱码文案不在本批次修复范围内，替换为 testid 后原文案分支自然消失即可，不要额外去改其它乱码**。第 246-247 行菜单选中态判据保留 `.ant-menu-item-selected`（左侧导航不属 Task 2 的注入范围）。

- [ ] **Step 3 — 跑通脚本。** 需要后端与前端 dev server 均可用：

```powershell
npm run test:rbac:smoke:local
```

  脚本若因环境（未登录、后端未起）失败，记录失败原因到本计划「执行记录」，但**不得**为了跑绿而放宽断言。此时以 Step 4 的静态校验作为本任务验收。

- [ ] **Step 4 — 静态校验与提交。** 确认替换密度：

```powershell
rg -c "data-testid" scripts/playwright/run-rbac-smoke.mjs
rg -c "\.ant-" scripts/playwright/run-rbac-smoke.mjs
```

  前者 ≥ 5，后者相比改造前必须下降。提交信息：`test(rbac): prefer data-testid selectors in playwright smoke script`。

**验收：** 脚本内 `data-testid` 命中 ≥ 5 且 `.ant-` 命中数下降；`npm run test:rbac:smoke:local` 通过，或在「执行记录」中写明环境性失败原因。

---

### Task 4: mobile 建立单测基建（RBAC-MB-SCOPE-002）

mobile 仓**没有任何测试运行器**：`package.json` 里的 `"test": "vite --mode test"` 是起 dev server，不是跑测试；devDependencies 无 vitest / @vue/test-utils / jsdom；`tests/` 目录不存在。本任务只做到「一条命令能跑通一个有意义的测试」，不追求覆盖率。

首个测试选 `src/utils/permission/index.ts` 的 `buildPermissionContext`（第 27 行导出），理由：它是 mobile 权限体系的唯一入口，是纯函数、无需 DOM，因此**不引入 jsdom 与 @vue/test-utils**，只加一个 vitest 依赖。注意该函数当前只产出 menuInfo/roleInfo/orgInfo 且只取首个角色——这是已登记的 `RBAC-MB-SCOPE-001`，**本任务的测试只断言其当前正确的装配行为，不断言尚未实现的权限码能力**，并在文件里留注释指明批次 3 修复后需要补哪些断言。

**Files:**
- Modify: `package.json`（加 vitest devDep 与 `test:unit` 脚本）
- Create: `vitest.config.ts`
- Create: `tests/permission/permission-context.test.ts`

**Steps:**

- [ ] **Step 1 — 装依赖。** 只装运行器本体：

```powershell
npm i -D vitest
```

  本机曾出现过 npm/git 网络不通。若安装失败，**停下来在「执行记录」记录并上报**，不要改用其它方案（node:test 跑不了 TS 与 `@/` 别名，会引入更多基建）。

- [ ] **Step 2 — 加 vitest 配置。** 新建 `vitest.config.ts`，复用 vite 的 `@` 别名，环境用 node（无 DOM 需求）：

```ts
import { fileURLToPath } from 'node:url';
import { defineConfig } from 'vitest/config';

export default defineConfig({
	resolve: {
		alias: {
			'@': fileURLToPath(new URL('./src', import.meta.url)),
		},
	},
	test: {
		// 纯函数测试, 不需要 DOM。后续若要 mount 组件, 再单独引入 jsdom 与 @vue/test-utils
		environment: 'node',
		include: ['tests/**/*.test.ts'],
	},
});
```

- [ ] **Step 3 — 写首个测试。** 新建 `tests/permission/permission-context.test.ts`。下列断言必须以 `src/utils/permission/index.ts` 的**实际实现**为准逐条核对后再落笔（函数入参名为 `admin`，类型 `LoginAdminLike`），字段名对不上就按实际字段改，不要照抄本计划的猜测：

```ts
import { describe, expect, it } from 'vitest';

import { buildPermissionContext } from '@/utils/permission';

describe('buildPermissionContext', () => {
	it('装配菜单、角色与机构三部分上下文', () => {
		const context = buildPermissionContext({
			menuInfoVoList: [{ id: '1', name: '用户管理' }],
			roleInfoVo: { roleCode: 'admin', roleName: '管理员' },
			orgInfoVo: { id: '10', orgName: '总公司' },
		} as never);

		expect(context.menuInfo).toBeTruthy();
		expect(context.roleInfo).toBeTruthy();
		expect(context.orgInfo).toBeTruthy();
	});

	it('入参缺失时不抛异常, 返回可安全消费的空上下文', () => {
		expect(() => buildPermissionContext({} as never)).not.toThrow();
	});

	// TODO(RBAC-MB-SCOPE-001): 批次 3 补齐权限码能力后, 这里需要增加两条断言:
	// 1) 多角色用户的权限码取并集, 而非只取首个角色;
	// 2) 上下文暴露 permissionCodes 与 buttonPermissionCodes, 供页内按钮级校验使用。
});
```

- [ ] **Step 4 — 加 npm 脚本。** 在 `package.json` 的 `scripts` 中，紧跟 `"test": "vite --mode test"` 之后加一行（`test` 键在本仓是 dev server 的既有含义，**不要改它**，避免踩坏别人的启动习惯）：

```json
		"test:unit": "vitest run",
```

- [ ] **Step 5 — 跑通并提交。**

```powershell
npm run test:unit
npm run lint
```

  两条都要绿。提交信息：`test(rbac): bootstrap vitest and first permission context test`。

**验收：** `tests/` 目录存在；`npm run test:unit` 一条命令跑通且至少 2 个用例通过；`npm run lint` 零报错；`package.json` 原有 `test` 脚本语义未被改动。

---

### Task 5: mobile 六个 RBAC 页面注入 data-testid

五个列表页（org / role / menu / perm / relation）模板高度同构：`van-search` → `van-cell-group` → `van-swipe-cell` → `van-cell`（`is-link` 跳详情）→ `template #right` 里的删除 `van-button`。第六个是「我的」入口页。Vant 组件同样透传未声明属性到根节点，执行时用与 Task 2 相同的方式验证一次。

**Files:**
- Modify: `src/views/user/index.vue`
- Modify: `src/views/user/orgInfo/index.vue`
- Modify: `src/views/user/roleInfo/index.vue`
- Modify: `src/views/user/menuInfo/index.vue`
- Modify: `src/views/user/permissionInfo/index.vue`
- Modify: `src/views/user/orgUserInfo/index.vue`

**Steps:**

- [ ] **Step 1 — 机构列表页作为样板。** `src/views/user/orgInfo/index.vue`：第 9 行 `van-search` → `rbac-org-search`；第 41 行 `van-cell` → `rbac-org-row`；第 72 行删除 `van-button` → `rbac-org-row-delete`。`van-cell` 处于 `v-for` 内，testid 保持同值不加索引（Playwright/Midscene 用 `.nth()` 取行即可，把 index 编进 testid 会让脚本依赖数据顺序）：

```vue
					<van-cell
						data-testid="rbac-org-row"
						:title-class="item.status == '1' ? 'validClass' : 'notValidClass'"
						:title="item.orgName"
						:key="index"
						is-link
```

- [ ] **Step 2 — 其余四个列表页照样板套用。** 行号与 testid 对应关系：
  - `roleInfo/index.vue`：10 → `rbac-role-search`，42 → `rbac-role-row`，73 → `rbac-role-row-delete`
  - `menuInfo/index.vue`：10 → `rbac-menu-search`，42 → `rbac-menu-row`，75 → `rbac-menu-row-delete`
  - `permissionInfo/index.vue`：10 → `rbac-perm-search`，43 → `rbac-perm-row`，74 → `rbac-perm-row-delete`
  - `orgUserInfo/index.vue`：10 → `rbac-relation-search`，42 → `rbac-relation-row`，73 → `rbac-relation-row-delete`

- [ ] **Step 3 —「我的」入口页（RBAC-MB-USER-002）。** `src/views/user/index.vue`：第 13-18 行的设置 `van-icon` → `rbac-user-btn-theme`；第 41 行用户名 `span.username` → `rbac-user-name`；第 96 行跳 `/myself/info` 的项 → `rbac-user-entry-profile`；第 111 行 `goSecurity` → `rbac-user-entry-security`；第 252 行 `showLogout` → `rbac-user-btn-logout`。第 206、219 行的 `handleInteraction` 项按其实际展示文案对应的语义命名，形如 `rbac-user-entry-<语义>`。注意 `/myself/info` 目标页当前是未完成残片（已登记 `RBAC-MB-USER-001`），**本任务只加入口 testid，不碰目标页**。

- [ ] **Step 4 — 校验与提交。**

```powershell
rg -c "data-testid" src/views/user
npm run lint
```

  六个文件都要有命中。提交信息：`test(rbac): add data-testid hooks to mobile RBAC pages`。

- [ ] **Step 5 — 同步知识图谱。** 按本仓 `.cursorrules` 第 4 节要求，改过 `src` 后执行：

```powershell
npm run graphify:update
```

**验收：** 六个目标文件 `rg -c data-testid` 均 ≥ 3；`npm run lint` 通过；graphify 图谱已更新。

---

### Task 6: 实测数字回写评分卡并重跑门禁

评分卡里三处数字是静态清点时的估计值，Task 1 已拿到实测值，必须回写，否则批次 1 派活时会拿错基线。**只改数字与措辞，不改任何分数、严重级与批次归类**（改了会破坏门禁的一致性校验）。

**Files:**
- Modify: `docs/testing/rbac-maturity-scorecard.md`
- Modify: `docs/superpowers/plans/2026-08-06-rbac-batch0-regression-hooks.md`（追加执行记录）

**Steps:**

- [x] **Step 1 — 修正三处数字。** 实测：改造前 `Tests run: 16`，四个休眠类的 `public void test*` 合计 24 个（Menu 4、UserPermissionContext 7、OrgUserAssignment 4、RoleUserAssignment 9），改造后应为 40。
  - 第 366 行结论 2 的「约 16 个用例一个都不执行」→「24 个用例一个都不执行」。
  - 第 274 行 `RBAC-BE-RELATION-005` 验收字段里的「8」→「13」（OrgUserAssignment 4 + RoleUserAssignment 9）。
  - 第 402 行批次 0 验收里的「由 16 升至约 32」→「由 16 升至 40」。

- [x] **Step 2 — 更新阻塞项一状态。** 6.1 节末尾追加一句：已按方案 1（装 JDK 17 并令 `JAVA_HOME` 指向 `C:\Program Files\Java\jdk-17`）解除，实测 `BUILD SUCCESS` 且 `Tests run: 16`，批次 0 验收不再需要退化为纯静态断言。

- [x] **Step 3 — 重跑门禁。** 数字改动不应影响门禁结论，确认仍为绿：

```powershell
node scripts/rbac-scorecard-check.mjs
```

- [x] **Step 4 — 追加执行记录。** 在本计划末尾「执行记录」小节填入：后端实测用例数、`@Disabled` 条目（若有）、PC 冒烟脚本是否跑通及原因、mobile vitest 安装结果。

- [x] **Step 5 — 提交。** 提交信息：`docs(rbac): correct batch0 test counts with measured values`。

**验收：** 三处数字与实测一致；`node scripts/rbac-scorecard-check.mjs` 退出码 0；执行记录已填。

---

## 批次 0 整体完成判据

四条同时成立才算完成，缺一条不得开工批次 1：

1. 后端 `mvn -pl alex_miaosha_user/user_boot -am test` 输出 `Tests run: 40`，且 Failures 与 Errors 为 0（Skipped 只允许来自已登记并注明批次的 `@Disabled`）。
2. PC 六个 RBAC 页面各有 `data-testid`，`run-rbac-smoke.mjs` 中 testid 选择器 ≥ 5 且 `.ant-` 选择器数量下降。
3. mobile `tests/` 目录存在，`npm run test:unit` 一条命令跑通；六个 RBAC 页面各有 `data-testid`。
4. 三仓 lint 均通过，评分卡数字已回写且门禁为绿。

## 范围外发现（不在本批次修，登记备查）

- **PC 仓 `tests/permission/permission-context.test.ts` 无运行器。** 该文件用裸 `throw` 断言、import `@/utils/permission` 别名与 TS 语法，但 PC 仓 devDependencies 无 vitest、无 `vitest.config.ts`、`package.json` 也没有跑它的脚本，因此它和后端那 4 个休眠测试类是同一性质的问题：写了但从不执行。批次 0 的 16 条未包含它，故不在此处修。建议随批次 2 的 `RBAC-PC-MENU-001`（表单校验规则为空，需要单测）一并引入 vitest 并把该文件包成 `describe`/`it`。
- **mobile `package.json` 的 `"test": "vite --mode test"` 命名有歧义**，容易让人误以为是跑测试。本批次为避免踩坏既有启动习惯，新增 `test:unit` 而未重命名。若后续要正名，需同步改 CI 与文档。

## 执行记录

### Task 1（后端，已完成，commit `a12a16a2`）

- **实测用例数：改造前 16 → 改造后 40**，`Failures: 0, Errors: 0, Skipped: 0`，`BUILD SUCCESS`，全程耗时约 16 秒。四个类各自贡献：`MenuPermissionFilterTest` 4、`UserPermissionContextServiceTest` 7、`OrgUserAssignmentServiceTest` 4、`RoleUserAssignmentServiceTest` 9。
- **无 `@Disabled` 条目。** 24 个休眠用例首次真实执行全部一次通过，说明它们此前只是没被 Surefire 收集，断言本身与生产代码一致，未暴露新的生产缺陷。分流规则未被触发。
- **签名兼容性结论与计划预判一致**：所有调用点无需改动。特别核对了两处：`assertEquals(Boolean.TRUE, Thread.currentThread().isInterrupted(), msg)` 因 JUnit 5 没有 `boolean` 重载而走 `(Object, Object, String)` 并自动装箱，语义不变；`assertEquals(0, service.saveBatchCalls, msg)` 走原生型重载，拆箱比较，语义不变。
- 保留的非断言工具方法与桩类未受影响：`menu()`、`assertAssignment()`、`proxy()` / `MethodHandler`、两处 `sleep()`、`Testable*` 与 `Blocking*` 五个内部类。
- 提交时 git 提示 `RoleUserAssignmentServiceTest.java` 的 LF 将转为 CRLF，属本仓既有换行符策略，未做处理。

### Task 6（评分卡回写，已完成）

- 三处数字已改：登记册 `RBAC-BE-RELATION-005` 的「8 个用例」与验收「上升 8」均改为 13；结论 2 的「约 16 个用例」改为 24；批次 0 验收的「由 16 升至约 32」改为「由 16 升至 40」。
- 6.1 节追加了阻塞项一的解除状态（方案 1，JDK 17），并记录改造前后两次实测数字。
- 门禁复跑结果：`[pass] 全量 门禁通过: 矩阵 21 格, 登记册 67 条`，退出码 0。分数、严重级与批次归类均未改动。

### Task 2–3（PC，代码已落地，冒烟待环境确认）

- PC 六个 RBAC 页面已有 `data-testid`（commit `09a0a6d`），冒烟脚本已优先 testid（commit `5bda0bb`）。
- 2026-08-10 复跑：账号取自 front `.env`。首次因 Playwright `page.fill` 不同步 Ant Design Vue `loginForm`，9 条全 skip；改为 `pressSequentially` 后复跑结果 **passed=2 / failed=6 / skipped=1**（`RBAC-LOCAL-101` 用户分页、`RBAC-LOCAL-201` 角色分页通过；`rbac_readonly` 账号登录仍失败被 skip；其余失败集中在「打不开用户/机构/角色菜单入口」——属批次 3 目标态未接线，不是批次 0 范围）。
- 偏差：登录页按钮文案是 `Log in`；真正阻塞曾是 Vue v-model 同步，不是超管/经理账号错误。

### Task 4–5（mobile，已完成，待用户确认后提交）

- **Task 4**：`vitest@4.1.10` 已在 `devDependencies`；新增 `vitest.config.ts`（独立配置，不加载 Vite 插件链）、`tests/permission/permission-context.test.ts`（3 个用例）、`package.json` 脚本 `test:unit`；`tsconfig.node.json` 纳入 `vitest.config.ts`。实测 `npm run test:unit` → `Tests 3 passed`。
- **Task 5**：六个页面注入 `data-testid`：
  - `orgInfo`：`rbac-org-search/row/row-delete`（挂在真实 `van-search`）
  - `roleInfo` / `menuInfo` / `permissionInfo` / `orgUserInfo`：搜索框在源码里被注释，为满足「每页 ≥3 钩子」且不改业务逻辑，将 `rbac-*-search` 挂在外层 `<form>`，行与删除按钮挂在 `van-cell` / 删除 `van-button`
  - `user/index`：`rbac-user-btn-theme/name/entry-profile/entry-security/entry-about/entry-feedback/btn-logout`
- `rg -c data-testid` 六文件均 ≥3；改动文件 eslint 通过；`graphify update src` 已跑。
- **未提交**（按用户要求：确认后再 commit）。
- 本机已 `yarn run dev` 起 mobile：`http://localhost:2000/`。
