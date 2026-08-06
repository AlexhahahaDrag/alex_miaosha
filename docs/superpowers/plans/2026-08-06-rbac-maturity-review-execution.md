# RBAC 成熟度评审执行 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 按 `docs/superpowers/specs/2026-08-06-rbac-maturity-review-design.md` 的判据，把三端 21 格 RBAC 成熟度评分、缺陷登记册与批次归类落成一份门禁可校验的评分卡，作为后续四个修复批次的派活输入。

**Architecture:** 评分卡是单一 markdown 数据源（`docs/testing/rbac-maturity-scorecard.md`），矩阵、登记册、遗留项映射、批次表各自包在 HTML 注释锚点内；一个零依赖 node 脚本解析这些表，复算加权总分、校验低分格与登记册双向可追溯、并回到三个仓验证每条证据的文件与行号真实存在。先写门禁再填数据，按端分批把门禁从红转绿。

**Tech Stack:** Node ESM 脚本（无第三方依赖）、ripgrep 静态取证、Maven Surefire 取测试用例数、markdown 落盘。三仓路径由脚本常量解析，可用环境变量覆盖。

## Global Constraints

以下为 spec 的项目级约束，每个任务都隐含包含，数值逐字照抄，不得就地改口径：

- 本计划**不修改任何业务代码**。允许写入的文件只有：评分卡、门禁脚本、spec 与本计划自身。
- 维度权重：D1 安全与数据正确性 35%、D2 功能完整度 30%、D3 交互一致性 18%、D4 视觉规范符合度 7%、D5 可测性与回归保护 10%。
- 判据折算：勾中比例 0% → 0 分；(0%, 30%] → 1 分；(30%, 55%] → 2 分；(55%, 75%] → 3 分；(75%, 95%] → 4 分；(95%, 100%] → 5 分。判据不适用时从分母剔除。
- D1 有两套判据：`BE` 的 7 格一律用「后端」判据；`PC` 与 `MB` 的全部 14 格一律用「前端消费侧」判据。
- D2 第 9 条「导出能力」仅 `ORG`、`USER` 两模块适用，其余模块该条从分母剔除。
- D4 按各端自己的 `.cursorrules` 评；D4 第 4 条「触觉反馈」仅 `MB` 适用。
- 单格总分 = Σ(维度分 × 权重) ÷ 适用权重之和 ÷ 5 × 100，取整到 0–100。
- 严重级：S1 可被利用的越权/数据错乱/静默数据丢失/敏感信息外泄；S2 数据一致性风险/关键校验缺失/权限判定不可靠；S3 功能缺失/目标态未落地/交互不一致；S4 视觉与体验打磨/代码卫生。
- 生产代码中的 `main` 方法打印密码定为 **S1**，不得降级为 S4。
- 登记册 ID 格式 `RBAC-<BE|PC|MB>-<模块>-<三位序号>`，模块取 `ORG` `USER` `ROLE` `MENU` `PERM` `RELATION` `SCOPE`。
- 证据字段必须写成 `<repo>/<相对路径>:<起行>-<止行>`，`repo` 取 `backend` `front` `mobile`，门禁会回到对应仓核对文件与行号。
- 提交信息**不得包含** `Co-authored-by: Cursor <cursoragent@cursor.com>`；Windows 环境下提交信息用英文，避免中文乱码。

### N/A 白名单（对 spec 5.3 / 6.1 的执行细化）

维度分只允许在下列格子记 `N/A`，其余一律必须给 0–5 的数字：

| 格子 | 允许 N/A 的维度 | 理由 |
| --- | --- | --- |
| `BE` 的全部 7 格 | D4 | 后端无视觉呈现 |
| `PC-SCOPE`、`MB-SCOPE` | D2、D4 | 前端不承载数据权限功能本体，也无对应视觉 |

`BE` 各格的 D4 **必须**为 `N/A`（不是可选）。此细化同步回写 spec，见 Task 1 Step 6。

---

### Task 1: 门禁脚本与评分卡骨架

建立唯一数据源与它的校验器。本任务结束时门禁必须是**红**的——矩阵全是待填值，这正是后面三个任务要转绿的目标。

**Files:**
- Create: `scripts/rbac-scorecard-check.mjs`
- Create: `docs/testing/rbac-maturity-scorecard.md`
- Modify: `docs/superpowers/specs/2026-08-06-rbac-maturity-review-design.md`（5.3 节末尾追加 N/A 白名单）

**Interfaces:**
- Consumes: 无（首个任务）。
- Produces:
  - 门禁命令 `node scripts/rbac-scorecard-check.mjs [--end BE|PC|MB]`，退出码 0 通过 / 1 有违规。
  - 评分卡锚点 `<!-- matrix:start -->` … `<!-- matrix:end -->`、`<!-- registry:start -->` … `<!-- registry:end -->`。
  - 矩阵表列顺序固定为：`端 | 模块 | D1 | D2 | D3 | D4 | D5 | 加权总分`（8 列）。
  - 登记册表列顺序固定为：`ID | 标题 | 端 | 模块 | 维度 | 严重级 | 证据 | 影响 | 修复方向 | 成本 | 验收 | 来源`（12 列）。
  - 端代码 `BE` `PC` `MB`；模块代码 `ORG` `USER` `ROLE` `MENU` `PERM` `RELATION` `SCOPE`。

- [ ] **Step 1: 写门禁脚本**

创建 `scripts/rbac-scorecard-check.mjs`：

```javascript
#!/usr/bin/env node
/**
 * RBAC 评分卡门禁。
 *
 * 校验 docs/testing/rbac-maturity-scorecard.md 的内部一致性与证据真实性：
 *   1. 评分矩阵覆盖 3 端 x 7 模块，无缺格无重复
 *   2. 维度分取值合法；N/A 只出现在白名单格子；BE 的 D4 必须 N/A
 *   3. 加权总分与按适用权重归一化的复算值一致
 *   4. 任一维度分 <= 2 的格子，登记册必须有对应 (端, 模块, 维度) 条目
 *   5. 登记册 ID 唯一且与端/模块列自洽；严重级、成本取值合法；关键字段非空
 *   6. 证据的 repo/path:line 可解析，且文件存在、行号未越界
 *
 * 用法:
 *   node scripts/rbac-scorecard-check.mjs            # 全量
 *   node scripts/rbac-scorecard-check.mjs --end BE   # 只校验单端, 供分端推进时用
 *
 * 退出码: 0 通过 / 1 有违规
 */
import { existsSync, readFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const HERE = dirname(fileURLToPath(import.meta.url));
const BACKEND_ROOT = resolve(HERE, '..');
const SCORECARD = join(BACKEND_ROOT, 'docs', 'testing', 'rbac-maturity-scorecard.md');

/** 证据前缀 -> 仓根目录。仓不在本机时跳过存在性校验, 只给提示。 */
const REPO_ROOTS = {
	backend: BACKEND_ROOT,
	front:
		process.env.RBAC_FRONT_ROOT ??
		resolve(BACKEND_ROOT, '..', '..', 'frontend', 'alex_miaosha_front'),
	mobile:
		process.env.RBAC_MOBILE_ROOT ??
		resolve(BACKEND_ROOT, '..', '..', 'frontend', 'alex_miaosha_mobile'),
};

const ENDS = ['BE', 'PC', 'MB'];
const MODULES = ['ORG', 'USER', 'ROLE', 'MENU', 'PERM', 'RELATION', 'SCOPE'];
const DIMS = ['D1', 'D2', 'D3', 'D4', 'D5'];
const WEIGHTS = { D1: 0.35, D2: 0.3, D3: 0.18, D4: 0.07, D5: 0.1 };
const SEVERITIES = ['S1', 'S2', 'S3', 'S4'];
const COSTS = ['S', 'M', 'L'];
const ID_RE = /^RBAC-(BE|PC|MB)-(ORG|USER|ROLE|MENU|PERM|RELATION|SCOPE)-(\d{3})$/;
const EVIDENCE_RE = /(backend|front|mobile)\/([^\s:;,，、]+):(\d+)(?:-(\d+))?/g;

/** 允许记 N/A 的 "格子:维度" 白名单, 见 plan 的 Global Constraints。 */
const NA_ALLOWED = new Set([
	...MODULES.map((module) => `BE-${module}:D4`),
	'PC-SCOPE:D2',
	'PC-SCOPE:D4',
	'MB-SCOPE:D2',
	'MB-SCOPE:D4',
]);

const argEndIndex = process.argv.indexOf('--end');
const argEnd = argEndIndex >= 0 ? process.argv[argEndIndex + 1] : null;
if (argEnd && !ENDS.includes(argEnd)) {
	console.error(`--end 只接受 ${ENDS.join(' / ')}`);
	process.exit(1);
}

const errors = [];
const warns = [];

if (!existsSync(SCORECARD)) {
	console.error(`找不到评分卡: ${SCORECARD}`);
	process.exit(1);
}
const raw = readFileSync(SCORECARD, 'utf8');

/** 取锚点之间的表格数据行, 已剔除表头与分隔行。 */
function tableRows(marker) {
	const start = raw.indexOf(`<!-- ${marker}:start -->`);
	const end = raw.indexOf(`<!-- ${marker}:end -->`);
	if (start < 0 || end < 0 || end < start) {
		errors.push(`缺少锚点 <!-- ${marker}:start --> / <!-- ${marker}:end -->`);
		return [];
	}
	const rows = raw
		.slice(start, end)
		.split('\n')
		.map((line) => line.trim())
		.filter((line) => line.startsWith('|'))
		.map((line) =>
			line
				.replace(/^\|/, '')
				.replace(/\|$/, '')
				.split('|')
				.map((cell) => cell.trim()),
		)
		.filter((cells) => !cells.every((cell) => cell === '' || /^:?-{3,}:?$/.test(cell)));
	return rows.slice(1);
}

/** 行数缓存, 避免同一文件反复读盘。 */
const lineCountCache = new Map();
function lineCount(absPath) {
	if (!lineCountCache.has(absPath)) {
		lineCountCache.set(absPath, readFileSync(absPath, 'utf8').split('\n').length);
	}
	return lineCountCache.get(absPath);
}

/** 证据串里每个 repo/path:line 都要真实存在且行号在文件范围内。 */
function checkEvidence(id, evidence) {
	const hits = [...evidence.matchAll(EVIDENCE_RE)];
	if (hits.length === 0) {
		errors.push(`${id} 证据不含 repo/path:line 形态: ${evidence}`);
		return;
	}
	for (const [, repo, relPath, fromRaw, toRaw] of hits) {
		const root = REPO_ROOTS[repo];
		if (!existsSync(root)) {
			warns.push(`证据所在仓不在本机, 跳过存在性校验: ${repo}`);
			continue;
		}
		const abs = join(root, relPath);
		if (!existsSync(abs)) {
			errors.push(`${id} 证据文件不存在: ${repo}/${relPath}`);
			continue;
		}
		const total = lineCount(abs);
		const from = Number(fromRaw);
		const to = Number(toRaw ?? fromRaw);
		if (from < 1 || to < from || to > total) {
			errors.push(
				`${id} 证据行号越界: ${repo}/${relPath}:${fromRaw}-${toRaw ?? fromRaw} (文件共 ${total} 行)`,
			);
		}
	}
}

/** 按适用维度归一化权重后折算到 0-100。 */
function weightedTotal(scores) {
	const applicable = DIMS.filter((dim) => scores[dim] !== 'N/A');
	const weightSum = applicable.reduce((sum, dim) => sum + WEIGHTS[dim], 0);
	const scoreSum = applicable.reduce((sum, dim) => sum + Number(scores[dim]) * WEIGHTS[dim], 0);
	return Math.round((scoreSum / weightSum / 5) * 100);
}

// ---------- 评分矩阵 ----------
const matrix = new Map();
for (const cells of tableRows('matrix')) {
	if (cells.length < 8) {
		errors.push(`矩阵行列数不足 8: ${cells.join(' | ')}`);
		continue;
	}
	const [end, module, ...rest] = cells;
	if (!ENDS.includes(end)) {
		errors.push(`矩阵端代码非法: ${end}`);
		continue;
	}
	if (!MODULES.includes(module)) {
		errors.push(`矩阵模块代码非法: ${module}`);
		continue;
	}
	const key = `${end}-${module}`;
	if (matrix.has(key)) {
		errors.push(`矩阵重复格: ${key}`);
		continue;
	}
	const scores = {};
	DIMS.forEach((dim, index) => {
		scores[dim] = rest[index];
	});
	matrix.set(key, { end, module, scores, total: rest[5] });
}

const scopedEnds = argEnd ? [argEnd] : ENDS;
for (const end of scopedEnds) {
	for (const module of MODULES) {
		const key = `${end}-${module}`;
		const cell = matrix.get(key);
		if (!cell) {
			errors.push(`矩阵缺格: ${key}`);
			continue;
		}
		let allParsed = true;
		for (const dim of DIMS) {
			const value = cell.scores[dim];
			const isNa = value === 'N/A';
			const isScore = /^[0-5]$/.test(value);
			if (!isNa && !isScore) {
				errors.push(`${key} ${dim} 取值非法(应为 0-5 或 N/A): "${value}"`);
				allParsed = false;
				continue;
			}
			if (isNa && !NA_ALLOWED.has(`${key}:${dim}`)) {
				errors.push(`${key} ${dim} 不在 N/A 白名单内`);
			}
			if (end === 'BE' && dim === 'D4' && !isNa) {
				errors.push(`${key} D4 按 spec 必须记 N/A`);
			}
		}
		if (allParsed) {
			const expect = weightedTotal(cell.scores);
			const actual = Number(cell.total);
			if (!Number.isFinite(actual) || Math.abs(actual - expect) > 1) {
				errors.push(`${key} 加权总分应为 ${expect}, 实际 "${cell.total}"`);
			}
		}
	}
}

// ---------- 缺陷登记册 ----------
const registry = [];
const seenIds = new Set();
for (const cells of tableRows('registry')) {
	if (cells.length < 12) {
		errors.push(`登记册行列数不足 12: ${cells.join(' | ')}`);
		continue;
	}
	const [id, title, end, module, dimsRaw, severity, evidence, impact, direction, cost, accept, source] =
		cells;
	const matched = ID_RE.exec(id);
	if (!matched) {
		errors.push(`登记册 ID 不合规: "${id}"`);
		continue;
	}
	if (seenIds.has(id)) {
		errors.push(`登记册 ID 重复: ${id}`);
		continue;
	}
	seenIds.add(id);
	if (matched[1] !== end) {
		errors.push(`${id} ID 中的端(${matched[1]})与端列(${end})不一致`);
	}
	if (matched[2] !== module) {
		errors.push(`${id} ID 中的模块(${matched[2]})与模块列(${module})不一致`);
	}
	const dims = dimsRaw
		.split(/[,，/]/)
		.map((dim) => dim.trim())
		.filter(Boolean);
	if (dims.length === 0) {
		errors.push(`${id} 维度列为空`);
	}
	for (const dim of dims) {
		if (!DIMS.includes(dim)) {
			errors.push(`${id} 维度非法: ${dim}`);
		}
	}
	if (!SEVERITIES.includes(severity)) {
		errors.push(`${id} 严重级非法: ${severity}`);
	}
	if (!COSTS.includes(cost)) {
		errors.push(`${id} 成本非法: ${cost}`);
	}
	for (const [label, value] of [
		['标题', title],
		['影响', impact],
		['修复方向', direction],
		['验收', accept],
		['来源', source],
	]) {
		if (!value || value === 'TBD') {
			errors.push(`${id} ${label} 为空或仍是 TBD`);
		}
	}
	if (!argEnd || argEnd === end) {
		checkEvidence(id, evidence);
	}
	registry.push({ id, end, module, dims, severity });
}

// ---------- 低分格必须在登记册留痕 ----------
for (const end of scopedEnds) {
	for (const module of MODULES) {
		const cell = matrix.get(`${end}-${module}`);
		if (!cell) {
			continue;
		}
		for (const dim of DIMS) {
			if (!/^[0-2]$/.test(cell.scores[dim])) {
				continue;
			}
			const covered = registry.some(
				(item) => item.end === end && item.module === module && item.dims.includes(dim),
			);
			if (!covered) {
				errors.push(`${end}-${module} ${dim}=${cell.scores[dim]} 未在登记册留下条目`);
			}
		}
	}
}

// ---------- 输出 ----------
const scopeLabel = argEnd ? `端 ${argEnd}` : '全量';
for (const warn of new Set(warns)) {
	console.warn(`[warn] ${warn}`);
}
if (errors.length > 0) {
	console.error(`[fail] ${scopeLabel} 门禁未通过, 共 ${errors.length} 项:`);
	for (const error of errors) {
		console.error(`  - ${error}`);
	}
	process.exit(1);
}
console.log(`[pass] ${scopeLabel} 门禁通过: 矩阵 ${matrix.size} 格, 登记册 ${registry.length} 条`);
```

- [ ] **Step 2: 写评分卡骨架**

创建 `docs/testing/rbac-maturity-scorecard.md`。矩阵 21 格先全部填 `TBD`，登记册留空表头——这两处就是后续任务要转绿的红点：

```markdown
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
| BE | ORG | TBD | TBD | TBD | N/A | TBD | TBD |
| BE | USER | TBD | TBD | TBD | N/A | TBD | TBD |
| BE | ROLE | TBD | TBD | TBD | N/A | TBD | TBD |
| BE | MENU | TBD | TBD | TBD | N/A | TBD | TBD |
| BE | PERM | TBD | TBD | TBD | N/A | TBD | TBD |
| BE | RELATION | TBD | TBD | TBD | N/A | TBD | TBD |
| BE | SCOPE | TBD | TBD | TBD | N/A | TBD | TBD |
| PC | ORG | TBD | TBD | TBD | TBD | TBD | TBD |
| PC | USER | TBD | TBD | TBD | TBD | TBD | TBD |
| PC | ROLE | TBD | TBD | TBD | TBD | TBD | TBD |
| PC | MENU | TBD | TBD | TBD | TBD | TBD | TBD |
| PC | PERM | TBD | TBD | TBD | TBD | TBD | TBD |
| PC | RELATION | TBD | TBD | TBD | TBD | TBD | TBD |
| PC | SCOPE | TBD | N/A | TBD | N/A | TBD | TBD |
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

（评审执行时按端追加）

## 3. 缺陷登记册

证据写法 `<repo>/<路径>:<起行>-<止行>`，`repo` 取 `backend` `front` `mobile`，门禁会回仓核对文件与行号。

<!-- registry:start -->
| ID | 标题 | 端 | 模块 | 维度 | 严重级 | 证据 | 影响 | 修复方向 | 成本 | 验收 | 来源 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
<!-- registry:end -->

## 4. 汇总

（Task 5 填：端总分、模块总分、拉后腿的端、Top 风险格）

## 5. 既有 spec 遗留项映射

（Task 5 填）

## 6. 批次归类与阻塞项

（Task 6 填）
```

- [ ] **Step 3: 跑门禁确认是红的**

Run: `node scripts/rbac-scorecard-check.mjs`

Expected: 退出码 1，**恰好 94 项**违规，全部是 `取值非法`：

```
[fail] 全量 门禁未通过, 共 94 项:
  - BE-ORG D1 取值非法(应为 0-5 或 N/A): "TBD"
  ...
```

94 的来源：`BE` 7 格各 4 个待填维度（D4 为 N/A）= 28；`PC` 六格各 5 个 + `PC-SCOPE` 3 个 = 33；`MB` 同为 33。数字不对说明骨架表格被编辑器改坏了（少行、多列、或 `N/A` 写成了 `NA`），先修表格再往下走。

- [ ] **Step 4: 验证单端参数可用**

Run: `node scripts/rbac-scorecard-check.mjs --end BE`

Expected: 退出码 1，**恰好 28 项**，全部以 `BE-` 开头，不出现 `PC-` 或 `MB-`。这证明后面三个任务能各自独立转绿。

- [ ] **Step 5: 验证权重复算与 N/A 白名单**

临时把 `BE | ORG` 一行改成 `| BE | ORG | 3 | 3 | 3 | N/A | 3 | 60 |`。

Run: `node scripts/rbac-scorecard-check.mjs --end BE`

Expected: 违规降到 24 项，`BE-ORG` 不再出现——全维 3 分归一化后正是 60。

再把该行总分从 `60` 改成 `99`：

Expected: 多出一条 `BE-ORG 加权总分应为 60, 实际 "99"`。

再把该行 D4 从 `N/A` 改成 `2`：

Expected: 多出一条 `BE-ORG D4 按 spec 必须记 N/A`。

三项都确认后，把该行改回全 `TBD` 与 `N/A`。

- [ ] **Step 6: 把 N/A 白名单回写 spec**

在 spec `docs/superpowers/specs/2026-08-06-rbac-maturity-review-design.md` 的 5.3 节「判据选用规则（消除歧义）」列表末尾追加两条：

```markdown
- N/A 只允许出现在白名单格子：`BE` 全部 7 格的 D4（必须 N/A）；`PC-SCOPE` 与 `MB-SCOPE` 的 D2 与 D4（前端不承载数据权限功能本体与其视觉）。其余格子一律必须给 0–5 的数字。
- 前端 `SCOPE` 格的 D1 用前端消费侧判据、D3 评三端口径一致性、D5 评是否有测试覆盖数据范围的表达，D2 与 D4 记 N/A。
```

- [ ] **Step 7: 提交**

```bash
git add scripts/rbac-scorecard-check.mjs docs/testing/rbac-maturity-scorecard.md docs/superpowers/specs/2026-08-06-rbac-maturity-review-design.md docs/superpowers/plans/2026-08-06-rbac-maturity-review-execution.md
git commit -m "docs: add rbac scorecard skeleton and gate script"
```

---

### Task 2: backend 七格取证与评分

对 `alex_miaosha_user` 的 7 个模块逐格取证、打分、登记缺陷，直到 `--end BE` 转绿。

**Files:**
- Modify: `docs/testing/rbac-maturity-scorecard.md`（矩阵 `BE-*` 七行、2.1 节明细、登记册 `RBAC-BE-*` 条目）

**Interfaces:**
- Consumes: Task 1 的门禁命令、锚点与表列顺序。
- Produces: 登记册中全部 `RBAC-BE-<模块>-<序号>` 条目，Task 5 汇总与 Task 6 批次归类会引用这些 ID。

- [ ] **Step 1: 跑取证命令，把原始输出留在手边**

在 `f:\workplace\project\myself\backend\alex_miaosha` 下依次执行。每条都标注了它服务于哪条判据、以及怎么判定：

| # | 判据 | 命令 | 判定 |
| --- | --- | --- | --- |
| 1 | D1-1 / D1-2 数据权限覆盖面 | `rg -n "@DataPermission" alex_miaosha_user/user_boot/src/main/java/com/alex/user -g "*Mapper.java"` | 逐模块看：只有 `getPage` 挂注解则 D1-1 勾、D1-2 不勾；`scope` 参数与列语义不符也记 D1-1 不勾 |
| 2 | D1-3 写操作归属校验 | `rg -n "public .*(add|update|delete)" alex_miaosha_user/user_boot/src/main/java/com/alex/user -g "*ServiceImp*.java"` | 对每个写方法读实现，看是否校验目标行归属当前用户/机构 |
| 3 | D1-4 / D1-5 唯一性与层级完整性 | `rg -n "selectCount|exists|checkExist|parentId|getParentId" alex_miaosha_user/user_boot/src/main/java/com/alex/user/orgInfo` | 无唯一性查询 → D1-4 不勾；无父节点存在性与防环 → D1-5 不勾 |
| 4 | D1-6 事务边界 | `rg -n "@Transactional\|TransactionTemplate" alex_miaosha_user/user_boot/src/main/java/com/alex/user -g "*ServiceImp*.java"` | 多表写操作未落在显式事务内 → 不勾 |
| 5 | D1-7 缓存失效口径 | `rg -n "buildContext\|permission_context\|LoginKey\|redis" alex_miaosha_user/user_boot/src/main/java/com/alex/user -g "*.java"` | 权限变更路径（授权、改机构、删用户）未触发缓存失效 → 不勾 |
| 6 | D1-8 角色判定 | `rg -n "contains\|equals\|equalsIgnoreCase" alex_miaosha_user/user_api/src/main/java/com/alex/api/user/handler/DataPermissionHandlerImpl.java` | 用 `contains("admin")` 之类子串匹配 → 不勾（子串会让 `xxadminxx` 命中） |
| 7 | D1-9 敏感信息外泄 | `rg -n "public static void main\|System.out.println" alex_miaosha_user/user_boot/src/main/java/com/alex/user -g "*.java"` | 生产代码里打印密码 → 不勾，且按 Global Constraints 定 **S1** |
| 8 | D1-10 双轨写入口 | `rg -n "assignSingleOrg" alex_miaosha_user/user_boot/src/main/java/com/alex/user -g "*.java"` 与 `rg -n "RequestMapping\|PostMapping" alex_miaosha_user/user_boot/src/main/java/com/alex/user/orgUserInfo -g "*Controller.java"` | 既有受约束的 `assignSingleOrg`，又有裸 CRUD 控制器能写同一张表 → 不勾 |
| 9 | D2-1 / D2-5 CRUD 与树 | `rg -n "PostMapping\|GetMapping\|PutMapping\|DeleteMapping" alex_miaosha_user/user_boot/src/main/java/com/alex/user -g "*Controller.java"` | 按模块清点接口；`ORG` 无 tree 接口 → D2-5 不勾 |
| 10 | D2-4 / D2-6 批量与启停 | `rg -n "batch\|Batch\|status\|enable\|disable" alex_miaosha_user/user_boot/src/main/java/com/alex/user -g "*Controller.java"` | 无批量删除接口 → D2-4 不勾；无独立启停接口 → D2-6 不勾 |
| 11 | D5-1 测试可执行性 | `rg -c "@Test" alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac` | 对比 `rbac` 目录下 java 文件总数：未出现在结果里的类等于一个用例都不跑 |
| 12 | D5-1 实际用例数 | `mvn -q -pl alex_miaosha_user/user_boot -am test -DfailIfNoTests=false` | 记下 `Tests run:` 数字，作为 D5-1 的硬证据 |
| 13 | D5-2 关键路径覆盖 | `rg -l "" alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac -g "*.java"` | 逐个看测试类覆盖的是哪条 D1 判据；无对应测试的 D1 缺陷条目在验收字段里注明「需先补测试」 |

命令 4/5/6 在 PowerShell 里的 `|` 需要转义为 `\|`（已写在表内），在 bash 里去掉反斜杠即可。

- [ ] **Step 2: 按判据折算填矩阵七行**

把 `BE-*` 七行的 `TBD` 换成 0–5 的数字与复算后的加权总分，D4 保持 `N/A`。同时在 2.1 节按下面格式逐格记录明细，不能只写分数：

```markdown
#### BE-ORG

- D1: 勾中 4/10 → 2 分。不勾：D1-2 详情查询无数据权限（`getOrgInfoById` 未挂注解）；D1-4 机构编码无唯一性校验；D1-5 无父节点存在性与防环；D1-8 角色判定为子串包含；D1-10 `OrgUserInfo` 裸 CRUD 与 `assignSingleOrg` 双轨；D1-9 见 USER 模块。
- D2: 勾中 5/8 → 3 分。第 9 条导出适用但未实现；剔除条：无。不勾：D2-4 无批量删除、D2-5 无 tree 接口、D2-9 无导出。
- D3: 勾中 x/y → n 分。（后端 D3 只评接口层一致性：命名、分页参数、返回结构是否与同端其他模块一致）
- D4: N/A。
- D5: 勾中 1/5 → 1 分。仅结构性测试, 无 D1 路径覆盖。
```

加权总分可用门禁反推：先随便填一个数，跑门禁，它会直接告诉你 `加权总分应为 N`。

- [ ] **Step 3: 写登记册 BE 条目**

每个 ≤2 分的维度至少一条条目，否则门禁报 `未在登记册留下条目`。示例格式（证据行号必须是你 Step 1 里实际看到的，不能照抄）：

```markdown
| RBAC-BE-USER-001 | 生产代码 main 方法打印密码 | BE | USER | D1 | S1 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/user/service/impl/TUserServiceImpl.java:120-130 | 该类被反射调用或误触发时密码进入日志 | 删除 main 方法，需要本地验证改用测试类 | S | grep 断言该文件无 main 方法与密码打印 | 新发现 |
| RBAC-BE-ORG-001 | 机构详情查询无数据权限 | BE | ORG | D1 | S1 | backend/alex_miaosha_user/user_boot/src/main/java/com/alex/user/orgInfo/mapper/OrgInfoMapper.java:20-40 | 越权用户可按 id 直读他机构数据 | 详情查询挂 @DataPermission 或在服务层加归属校验 | M | 集成测试：他机构 id 查询返回空或 403 | wave1 Non-goals |
```

「来源」字段对 spec 第 10 节列出的 8 条遗留项必须写出处（`wave1 Non-goals`、`wave2 Non-goals`、`role-assign-permissions Risks` 等），其余写 `新发现`。

- [ ] **Step 4: 转绿**

Run: `node scripts/rbac-scorecard-check.mjs --end BE`

Expected: `[pass] 端 BE 门禁通过: 矩阵 21 格, 登记册 N 条`

若报 `证据行号越界` 或 `证据文件不存在`，说明证据是凭记忆写的——回 Step 1 重新取证，不要改门禁去迁就证据。

- [ ] **Step 5: 提交**

```bash
git add docs/testing/rbac-maturity-scorecard.md
git commit -m "docs: score backend rbac modules with defect registry"
```

---

### Task 3: PC 七格取证与评分

对 `alex_miaosha_front` 的 7 个模块取证打分，直到 `--end PC` 转绿。

**Files:**
- Modify: `docs/testing/rbac-maturity-scorecard.md`（矩阵 `PC-*` 七行、2.1 节明细、登记册 `RBAC-PC-*` 条目）

**Interfaces:**
- Consumes: Task 1 的门禁与表结构；Task 2 已写入的 `RBAC-BE-*` 条目（序号独立编号，不接续）。
- Produces: 全部 `RBAC-PC-<模块>-<序号>` 条目。

- [ ] **Step 1: 跑取证命令**

在 `f:\workplace\project\myself\frontend\alex_miaosha_front` 下执行。注意 D1 用**前端消费侧**判据，不是后端那套：

| # | 判据 | 命令 | 判定 |
| --- | --- | --- | --- |
| 1 | D1-1 ID 全程 string | `rg -n "Number\(|parseInt\(|\+id" src/views/user` | 对实体 ID 做数值转换 → 不勾（大 Long 会丢精度，低位变 00） |
| 2 | D1-2 按钮级权限 | `rg -n "hasPermission\|v-permission\|usePermission\|permissionCode" src/views/user src/utils src/store` | 只有路由级校验、页内按钮不校验 → 不勾 |
| 3 | D1-4 多角色权限合并 | `rg -n "roles\[0\]\|roleList\[0\]" src` | 取首个角色而非合并全部 → 不勾 |
| 4 | D1-5 payload 不回传只读字段 | `rg -n "createTime\|updateTime\|createBy" src/views/user -g "*.vue"` | 表单把审计字段原样回传 → 不勾 |
| 5 | D2-1 / D2-2 CRUD 与筛选 | `rg -n "export const" src/views/user -g "api/index.ts"` 与 `rg -n "a-form-item" src/views/user -g "*.vue"` | 后端支持的筛选字段前端未暴露 → D2-2 不勾 |
| 6 | D2-3 服务端分页 | `rg -n "pageNum\|pageSize\|current\|usePagination" src/views/user` | 全量拉取后前端切片 → 不勾 |
| 7 | D2-4 批量操作 | `rg -n "selectedRowKeys\|rowSelection\|batchDelete" src/views/user` | 无批量入口 → 不勾 |
| 8 | D2-6 启停入口 | `rg -n "status" src/views/user -g "*.vue"` | 状态只能进编辑表单改、无列表内独立开关 → 不勾 |
| 9 | D2-8 目标态落地 | `rg -n "components/rbac" src` | 无输出说明 `src/components/rbac/*` 四个组件在库但零引用，stage1 目标 UI 未落地 → 不勾 |
| 10 | D3-1 容器一致性 | `rg -n "a-modal\|a-drawer" src/views/user -g "*.vue"` | 同端同类操作 Modal 与 Drawer 混用 → 不勾 |
| 11 | D3-2 危险操作二次确认 | `rg -n "a-popconfirm\|Modal.confirm" src/views/user` | 删除无确认 → 不勾 |
| 12 | D3-3 提交 loading 防重 | `rg -n ":loading\|confirmLoading" src/views/user -g "*.vue"` | 提交按钮无 loading → 不勾 |
| 13 | D3-4 表单校验 | `rg -n "rules\|reactive\(\{\s*\}\)" src/views/user -g "*.vue"` | 校验规则为空对象 → 不勾 |
| 14 | D3-6 / D3-7 复用与命名 | `rg -n "compoments/menu-tree" src/views/user` 与 `rg -n "<[A-Z][A-Za-z]+" src/views/user -g "*.vue"` | 重复实现权限树 → D3-6 不勾；模板里用 PascalCase 标签 → D3-7 不勾 |
| 15 | D4-1 / D4-2 加载与空态 | `rg -n "a-empty\|#emptyText\|a-skeleton\|v-ant-loading" src/views/user` | 无空状态或无骨架屏 → 不勾 |
| 16 | D4-7 调试残留与乱码 | `rg -n "console\.log\|debugger\|\u{FFFD}" src/views/user` | 有残留 → 不勾 |
| 17 | D5-1 / D5-3 测试与钩子 | `rg -c "data-testid" src/views/user` 与 `rg -n "user\|org\|role" tests/midscene/rbac/cases/stage1-smoke.json` | 无 `data-testid` → D5-3 不勾；用例文件在库但没有定位抓手可跑 → D5-1 不勾 |
| 18 | D5-5 一条命令跑通 | `rg -n "\"test\|midscene\|vitest" package.json` | 无对应 npm script → 不勾 |

`PC-SCOPE` 格只评 D1（前端消费侧）、D3、D5，D2 与 D4 记 `N/A`。取证聚焦一点：前端是否对后端过滤规则做了错误假设，例如假定管理员能看到子机构数据。用 `rg -n "org\|机构" src/views/user -g "*.vue"` 找相关文案与逻辑。

- [ ] **Step 2: 填矩阵七行与 2.1 明细**

格式同 Task 2 Step 2。`PC-RELATION` 按 spec 6.1 特殊处理：`orgUserInfo` / `roleUserInfo` / `rolePermissionInfo` 三个目录只有 `api/index.ts` 而无页面，关系维护内嵌在用户表单与角色抽屉里，这**不在 D2 记为能力缺失**，而由 D3 交互一致性承担扣分。用下面命令确认现状再评：

```bash
rg --files src/views/user/orgUserInfo src/views/user/roleUserInfo src/views/user/rolePermissionInfo
```

Expected: 只列出 `api/index.ts` 一类文件，无 `index.vue`。

- [ ] **Step 3: 写登记册 PC 条目**

证据 `repo` 前缀写 `front`。示例：

```markdown
| RBAC-PC-MENU-001 | 菜单详情表单校验规则为空对象 | PC | MENU | D3 | S2 | front/src/views/user/menuInfo/menu-info-detail/index.vue:40-60 | 必填项缺失可直接提交，脏数据落库 | 按后端非空字段补 reactive rules | S | 提交空表单被拦下的 midscene 用例 | 新发现 |
| RBAC-PC-ORG-002 | rbac 共享组件在库但零引用 | PC | ORG | D2,D3 | S3 | front/src/components/rbac/index.ts:1-11 | stage1 目标 UI 未落地，组件与页面双份维护 | 机构抽屉接 base-rbac-drawer，权限树接 rbac-permission-tree-panel | M | 静态检查：src/views/user 下出现 components/rbac 引用 | 新发现（组件已由 3f3b605 归位到本分支） |
```

- [ ] **Step 4: 转绿**

Run: `node scripts/rbac-scorecard-check.mjs --end PC`

Expected: `[pass] 端 PC 门禁通过`

- [ ] **Step 5: 提交**

```bash
git add docs/testing/rbac-maturity-scorecard.md
git commit -m "docs: score pc rbac modules with defect registry"
```

---

### Task 4: mobile 七格取证与评分

对 `alex_miaosha_mobile` 的 7 个模块取证打分，直到 `--end MB` 转绿。

**Files:**
- Modify: `docs/testing/rbac-maturity-scorecard.md`（矩阵 `MB-*` 七行、2.1 节明细、登记册 `RBAC-MB-*` 条目）

**Interfaces:**
- Consumes: Task 1 的门禁与表结构。
- Produces: 全部 `RBAC-MB-<模块>-<序号>` 条目。

- [ ] **Step 1: 跑取证命令**

在 `f:\workplace\project\myself\frontend\alex_miaosha_mobile` 下执行。`MB-USER` 评的是个人信息页而非管理页，判据里「批量」「启停」等管理能力对该格从分母剔除：

| # | 判据 | 命令 | 判定 |
| --- | --- | --- | --- |
| 1 | D1-1 ID 全程 string | `rg -n "Number\(|parseInt\(" src/views/user` | 有数值转换 → 不勾 |
| 2 | D1-2 按钮级权限 | `rg -n "hasPermission\|usePermission" src` 与 `rg --files src/composables` | `composables` 下无 `usePermission.ts`、页内无按钮级校验 → 不勾 |
| 3 | D1-4 多角色合并 | `rg -n "roles" src/utils/permission/index.ts` | `buildPermissionContext` 只取首个角色 → 不勾（多角色用户会丢权限） |
| 4 | D2-1 CRUD 完整度 | `rg --files src/views/user` | 逐模块看是否有列表 + 详情 + 提交 |
| 5 | D2-3 服务端分页 | `rg -n "usePagination" src/views/user` | 未用 `usePagination` 而自建 `ref({ current: 1 })` → 不勾（也违反 `.cursorrules`） |
| 6 | D2-7 关系配置语义 | `rg -n "userId\|orgId\|roleId" src/views/user/orgUserInfo src/views/user/roleUserInfo src/views/user/rolePermissionInfo -g "*.vue"` | 详情页要求手填 ID 而非选择器 → 不勾 |
| 7 | D3-2 危险操作确认 | `rg -n "showConfirmDialog\|van-dialog" src/views/user` | 删除无确认 → 不勾 |
| 8 | D3-3 提交防重 | `rg -n "loading" src/views/user -g "*.vue"` | 提交按钮无 loading → 不勾 |
| 9 | D3-6 复用共享组件 | `rg -n "CommonList\|CommonPullRefresh\|common-list\|common-pull-refresh" src/views/user` | 未复用 `src/views/components` 下封装 → 不勾 |
| 10 | D4-1 / D4-2 骨架屏与空态 | `rg -n "van-skeleton\|van-empty" src/views/user` | 用 Loading 文本代替骨架屏 → 不勾 |
| 11 | D4-3 / D4-5 卡片化与侧距 | `rg -n "border-radius\|padding" src/views/user -g "*.vue"` | 圆角未达 16px 基准、列表容器无 `0 16px` 侧距 → 不勾 |
| 12 | D4-4 触觉反馈 | `rg -n "vibrate" src/views/user` | 关键按钮无 `navigator.vibrate?.(50)` → 不勾 |
| 13 | D4-6 / D4-7 图标与乱码 | `rg -n "\u{FFFD}" src/views/user` 与 `rg -n "\\$\\{" src/views/user -g "*.vue"` | 有替换字符即乱码文案；模板字面量写在非模板串里是 bug，两者都记 → 不勾 |
| 14 | D5-1 测试存在性 | `cmd /c "dir /b tests"` | 目录不存在 → D5-1、D5-2、D5-5 全不勾 |
| 15 | D5-3 定位钩子 | `rg -c "data-testid" src/views/user` | 无输出 → 不勾 |
| 16 | D2-8 目标态落地 | 逐条对照 `docs/testing/mobile-rbac-visibility-checklist.md` | 清单要求的可见性控制未落地 → 不勾 |

- [ ] **Step 2: 填矩阵七行与 2.1 明细**

格式同 Task 2 Step 2。`MB-USER` 在明细里显式写出剔除了哪些管理类判据，例如：

```markdown
#### MB-USER

- D2: 勾中 3/6 → 3 分。剔除条：D2-4 批量、D2-6 启停、D2-9 导出（该格为个人信息页，非管理页）。不勾：D2-8 目标态未落地。
```

- [ ] **Step 3: 写登记册 MB 条目**

证据 `repo` 前缀写 `mobile`。示例：

```markdown
| RBAC-MB-SCOPE-001 | 权限上下文只取首个角色 | MB | SCOPE | D1 | S2 | mobile/src/utils/permission/index.ts:19-37 | 多角色用户丢失其余角色权限，页面误隐藏入口 | 合并全部角色的权限码去重后写入上下文 | S | 单测：双角色用户的权限码为两者并集 | 新发现 |
| RBAC-MB-RELATION-001 | 关系配置详情要求手填 ID | MB | RELATION | D2,D3 | S3 | mobile/src/views/user/orgUserInfo/orgUserInfoDetail/index.vue:1-60 | 用户需自行记住 Long ID，实际不可用 | 换成 van-picker 拉取机构/用户列表选择 | M | midscene：不输入 ID 也能完成一次绑定 | 移动端全部 RBAC 变更（三份 spec 一致 Non-goals） |
```

- [ ] **Step 4: 转绿**

Run: `node scripts/rbac-scorecard-check.mjs --end MB`

Expected: `[pass] 端 MB 门禁通过`

- [ ] **Step 5: 提交**

```bash
git add docs/testing/rbac-maturity-scorecard.md
git commit -m "docs: score mobile rbac modules with defect registry"
```

---

### Task 5: 汇总与遗留项映射

三端分数齐了才能算端总分与模块总分。本任务同时把 spec 第 10 节的 8 条遗留项映射到具体登记册 ID，并给这个映射加机器校验——防止「遗留项被当成新发现重复讨论」这件事在文档里悄悄发生。

**Files:**
- Modify: `scripts/rbac-scorecard-check.mjs`（新增 `legacy` 表校验）
- Modify: `docs/testing/rbac-maturity-scorecard.md`（第 4、5 节）

**Interfaces:**
- Consumes: Task 2/3/4 写入的全部登记册 ID 与 21 格分数。
- Produces: `<!-- legacy:start -->` / `<!-- legacy:end -->` 锚点，两列表格 `遗留项 | 登记册 ID`；端总分与模块总分。

- [ ] **Step 1: 先加门禁（会红）**

在 `scripts/rbac-scorecard-check.mjs` 的「低分格必须在登记册留痕」段之后、「输出」段之前插入：

```javascript
// ---------- 既有 spec 遗留项映射 ----------
/** spec 第 10 节列出的 8 条遗留项, 每条都必须映射到至少一个登记册条目。 */
const LEGACY_ITEMS = [
	'Org/Role 详情与写接口的数据权限',
	'删除角色时级联清理',
	'管理员仅可见本机构',
	'无独立启停接口',
	'机构无批量删除',
	'移动端全部 RBAC 变更',
	'历史失效关系行持续累积',
	'PC stage1 目标 UI 未落地',
];
if (!argEnd) {
	const legacyRows = tableRows('legacy');
	const mapped = new Map();
	for (const cells of legacyRows) {
		if (cells.length < 2) {
			errors.push(`遗留项映射行列数不足 2: ${cells.join(' | ')}`);
			continue;
		}
		const [item, idsRaw] = cells;
		const ids = idsRaw
			.split(/[,，\s]+/)
			.map((id) => id.trim())
			.filter(Boolean);
		if (ids.length === 0) {
			errors.push(`遗留项 "${item}" 未映射任何登记册 ID`);
		}
		for (const id of ids) {
			if (!seenIds.has(id)) {
				errors.push(`遗留项 "${item}" 引用了不存在的条目 ${id}`);
			}
		}
		mapped.set(item, ids);
	}
	for (const item of LEGACY_ITEMS) {
		const hit = [...mapped.keys()].some((key) => key.includes(item) || item.includes(key));
		if (!hit) {
			errors.push(`spec 第 10 节遗留项未出现在映射表: ${item}`);
		}
	}
}
```

Run: `node scripts/rbac-scorecard-check.mjs`

Expected: 退出码 1，报 `缺少锚点 <!-- legacy:start -->` 与 8 条 `遗留项未出现在映射表`。

- [ ] **Step 2: 填遗留项映射表**

把评分卡第 5 节替换为：

```markdown
## 5. 既有 spec 遗留项映射

下列条目来自既有三份 spec 的 Non-goals / Risks，已在登记册中登记，**不得作为新发现重复讨论**。

<!-- legacy:start -->
| 遗留项 | 登记册 ID |
| --- | --- |
| Org/Role 详情与写接口的数据权限（仅 getPage 已覆盖） | RBAC-BE-ORG-001, RBAC-BE-ROLE-001 |
| 删除角色时级联清理 t_role_permission_info | RBAC-BE-ROLE-002 |
| 管理员仅可见本机构，无法看子机构 | RBAC-BE-SCOPE-001 |
| 无独立启停接口 | RBAC-BE-USER-002 |
| 机构无批量删除 | RBAC-BE-ORG-002 |
| 移动端全部 RBAC 变更 | RBAC-MB-RELATION-001 |
| 历史失效关系行持续累积 | RBAC-BE-RELATION-001 |
| PC stage1 目标 UI 未落地 | RBAC-PC-ORG-002 |
<!-- legacy:end -->
```

上表的 ID 是示意，**必须换成 Task 2/3/4 实际写下的 ID**；若某遗留项在评分时没被登记，先回去补登记册条目，不要在这里编 ID——门禁会拦住不存在的 ID。

- [ ] **Step 3: 算端总分与模块总分**

填评分卡第 4 节。端总分 = 该端 7 格加权总分的算术平均；模块总分 = 该模块 3 端加权总分的算术平均：

```markdown
## 4. 汇总

### 4.1 端总分

| 端 | 7 格平均 | 最弱格 | 最弱维度 |
| --- | --- | --- | --- |
| BE | 58 | BE-RELATION 41 | D5 |
| PC | 49 | PC-RELATION 38 | D5 |
| MB | 33 | MB-SCOPE 25 | D1 |

### 4.2 模块总分

| 模块 | BE | PC | MB | 模块平均 | 拉后腿的端 |
| --- | --- | --- | --- | --- | --- |
| ORG | 55 | 52 | 34 | 47 | MB |

### 4.3 Top 风险格

按 D1 分值升序取前 5 格，每格附对应的 S1/S2 条目 ID。

### 4.4 结论

一段话回答「当前 RBAC 完善到什么程度」：给出三端总分区间、最薄弱维度、以及 S1 条目数量。
```

表内数字为格式示意，填入实际值。因 `BE` 无 D4、前端 `SCOPE` 有 N/A，4.2 的跨端比较只在模块平均层面解读，不对单格排名——这一句要写进 4.4。

- [ ] **Step 4: 全量转绿**

Run: `node scripts/rbac-scorecard-check.mjs`

Expected: `[pass] 全量 门禁通过: 矩阵 21 格, 登记册 N 条`

- [ ] **Step 5: 提交**

```bash
git add scripts/rbac-scorecard-check.mjs docs/testing/rbac-maturity-scorecard.md
git commit -m "docs: add rbac scorecard rollup and legacy mapping gate"
```

---

### Task 6: 批次归类与阻塞项决策清单

把登记册切成 5 个批次，并让门禁保证「没有条目掉队、没有条目被重复排进两个批次」。批次 3 的产品形态决策在此显式挂成阻塞项。

**Files:**
- Modify: `scripts/rbac-scorecard-check.mjs`（新增 `batches` 表校验）
- Modify: `docs/testing/rbac-maturity-scorecard.md`（第 6 节、Status 改为已完成）

**Interfaces:**
- Consumes: Task 2/3/4 的全部登记册 ID 与严重级、Task 5 的汇总结论。
- Produces: `<!-- batches:start -->` / `<!-- batches:end -->` 锚点，三列表格 `批次 | 条目 ID | 验收手段`；后续每批次的 writing-plans 以此表为输入。

- [ ] **Step 1: 先加门禁（会红）**

在 `scripts/rbac-scorecard-check.mjs` 的遗留项映射段之后插入：

```javascript
// ---------- 批次归属 ----------
/** 批次与严重级的对应关系, 见 spec 第 8 节。批次 0 是前置项, 不绑严重级。 */
const BATCH_SEVERITY = { '批次 1': 'S1', '批次 2': 'S2', '批次 3': 'S3', '批次 4': 'S4' };
if (!argEnd) {
	const assigned = new Map();
	for (const cells of tableRows('batches')) {
		if (cells.length < 3) {
			errors.push(`批次表行列数不足 3: ${cells.join(' | ')}`);
			continue;
		}
		const [batch, idsRaw, accept] = cells;
		if (!accept) {
			errors.push(`${batch} 未写验收手段`);
		}
		for (const id of idsRaw.split(/[,，\s]+/).filter(Boolean)) {
			if (assigned.has(id)) {
				errors.push(`${id} 被重复归入 ${assigned.get(id)} 与 ${batch}`);
				continue;
			}
			assigned.set(id, batch);
			const item = registry.find((entry) => entry.id === id);
			if (!item) {
				errors.push(`${batch} 引用了不存在的条目 ${id}`);
				continue;
			}
			const expectSeverity = BATCH_SEVERITY[batch];
			if (expectSeverity && item.severity !== expectSeverity) {
				errors.push(`${id} 严重级 ${item.severity} 与 ${batch} 要求的 ${expectSeverity} 不符`);
			}
		}
	}
	for (const item of registry) {
		if (!assigned.has(item.id)) {
			errors.push(`${item.id} 未归入任何批次`);
		}
	}
}
```

Run: `node scripts/rbac-scorecard-check.mjs`

Expected: 退出码 1，报 `缺少锚点 <!-- batches:start -->`，以及每一条登记册条目的 `未归入任何批次`。

- [ ] **Step 2: 填批次表与阻塞项**

把评分卡第 6 节替换为：

```markdown
## 6. 批次归类与阻塞项

严格按严重级推进，不允许跨批次挑低成本项。批次 0 不修缺陷，只为后三批提供回归保护与定位抓手。

<!-- batches:start -->
| 批次 | 条目 ID | 验收手段 |
| --- | --- | --- |
| 批次 0 | RBAC-BE-USER-003, RBAC-PC-ORG-003 | 后端 Tests run 数字上升；grep 确认目标页面已有 data-testid |
| 批次 1 | RBAC-BE-USER-001, RBAC-BE-ORG-001 | 自动化测试断言：数据权限 SQL 片段、越权访问被拒 |
| 批次 2 | RBAC-MB-SCOPE-001 | 单元测试 + 集成测试 |
| 批次 3 | RBAC-PC-ORG-002, RBAC-MB-RELATION-001 | Midscene 用例通过 + 静态检查共享组件已被引用 |
| 批次 4 | RBAC-MB-ORG-002 | 静态检查 + 人工过一遍 |
<!-- batches:end -->

上表 ID 为示意，填入实际条目。批次 0 收纳的是「补 @Test 注解」「补 data-testid」这两类本身不修缺陷的前置项，它们在登记册中的严重级按其实际影响填（通常 S3/S4），因此批次 0 不参与严重级一致性校验。

### 6.1 阻塞项：批次 3 的产品形态决策

`tests/midscene/rbac/cases/stage1-smoke.json` 与 `docs/testing/rbac-stage1-midscene-test-design.md` 描述了一套目标 UI：用户页左侧机构树、机构 Drawer 契约、角色统计列、权限差异预览、独立的机构-用户与用户-角色配置页。这批资产已由前端仓 commit `3f3b605` 从 `codex/gift-management-module` 归位到本分支。

**执行批次 3 前必须先确认该设计是否仍然采纳。** 未确认即开工的风险是照旧文档改完又不满意，返工成本 L。需决策的问题：

1. PC 关系配置是否改为独立页面，还是保持内嵌在用户表单与角色抽屉？
2. 用户页是否引入左侧机构树布局？
3. `src/components/rbac/*` 四个组件是否作为最终形态复用，还是重新设计？
4. 移动端关系配置是否需要完整管理能力，还是只保留查看？

### 6.2 各批次的 plan 落盘位置

| 批次 | plan 路径 |
| --- | --- |
| 批次 0 | `alex_miaosha/docs/superpowers/plans/2026-08-06-rbac-batch0-regression-hooks.md` |
| 批次 1 | `alex_miaosha/docs/superpowers/plans/2026-08-06-rbac-batch1-s1.md` |
| 批次 2 | `alex_miaosha/docs/superpowers/plans/2026-08-06-rbac-batch2-s2.md` |
| 批次 3 | 前端条目落各前端仓 `docs/superpowers/plans/`，后端条目落后端仓 |
| 批次 4 | 同批次 3 |
```

- [ ] **Step 3: 全量转绿**

Run: `node scripts/rbac-scorecard-check.mjs`

Expected: `[pass] 全量 门禁通过: 矩阵 21 格, 登记册 N 条`

若报 `严重级 Sx 与 批次 N 要求的 Sy 不符`，不要改批次表迁就——回登记册确认严重级定级是否正确，S1 的定义是「可被利用的越权、数据错乱、静默数据丢失、敏感信息外泄」。

- [ ] **Step 4: 把 Status 改为已完成**

评分卡头部 `Status: 评审进行中` 改为：

```markdown
Status: 评审完成（21 格已评分，N 条缺陷已登记并归入批次）
```

`N` 填实际条目数。

- [ ] **Step 5: 提交并推送**

```bash
git add scripts/rbac-scorecard-check.mjs docs/testing/rbac-maturity-scorecard.md
git commit -m "docs: assign rbac defects to batches with blocking decisions"
git push origin HEAD
```

---

## 后续计划的边界

本计划只产出评审结论。批次 0–4 的修复计划**必须等评分卡完成后再写**——登记册条目未知时写不出带确切文件路径与测试代码的任务。每批次一份独立 plan，落盘位置见评分卡 6.2 节。

批次 3 在 6.1 节的四个决策未回答前不得开工。

## 自检

- [x] spec 覆盖：判据与权重（Global Constraints）、21 格矩阵（Task 1 骨架 + Task 2/3/4 填充）、登记册 12 字段（Task 1 表结构 + Task 2/3/4 填充）、严重级定级（Global Constraints + Task 6 门禁）、五批次路线图（Task 6）、遗留项映射（Task 5）、产出物落盘（Task 1 与 Task 6.2）、批次 3 阻塞项（Task 6.1）
- [x] 无 TBD / 「稍后补充」/「类似 Task N」；每个 Step 都给出可直接执行的命令或完整代码
- [x] 类型与命名一致：端代码 `BE`/`PC`/`MB`、模块代码七个、锚点名 `matrix`/`registry`/`legacy`/`batches`、门禁参数 `--end` 在各任务间一致
- [x] 门禁脚本三次增量修改（Task 1 建、Task 5 加 legacy、Task 6 加 batches）的插入位置均明确指向已存在的代码段
- [x] 每个任务以「跑门禁转绿 + 提交」收尾，可独立 review
- [x] 已声明不改业务代码，允许写入的文件白名单明确
- [x] 门禁脚本已在临时 fixture 上实跑验证（node v22）：加权复算、N/A 白名单、BE 的 D4 强制 N/A、证据回仓核对文件与行号、低分格覆盖、ID 与端列自洽、legacy/batches 锚点缺失，七类校验均按预期触发；Task 1 Step 3/4/5 的期望违规数由该次实跑推算
