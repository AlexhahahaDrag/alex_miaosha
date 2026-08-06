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
