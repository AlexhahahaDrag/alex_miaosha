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

/** 允许记 N/A 的 "格子:维度" 白名单, 见 spec 5.3 判据选用规则。 */
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
