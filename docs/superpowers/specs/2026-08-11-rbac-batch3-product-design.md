# RBAC 批次 3 产品形态设计（锁定）

> 日期：2026-08-11  
> 状态：**已锁定**（用户授权「按合理默认处理」）  
> 依据：`docs/testing/rbac-maturity-scorecard.md` §6.2 + stage1 midscene/契约 + 现网代码勘察

## 1. 四问锁定结论

| # | 问题 | 锁定决策 | 理由 |
| --- | --- | --- | --- |
| 1 | PC 关系配置形态 | **补独立配置页**（机构-用户、用户-角色），与 stage1 / mobile 对齐；用户详情/角色抽屉内的分配保留为快捷入口，不作为唯一形态 | 三端一致；API 与 `RbacDualListSelector` 已就绪；内嵌 alone 无法过 stage1 smoke |
| 2 | 用户页左侧机构树 | **要**。用户管理左树筛选列表；机构管理沿用已有左树并改为消费后端 `/tree` | stage1 smoke 已假定；去掉客户端 `page(1,1000)` 拼树 |
| 3 | `src/components/rbac/*` | **复用接线，不重做** | 四组件已归位零引用；ORG-002/ROLE-001 成本是接线而非设计 |
| 4 | 移动端关系能力 | **完整管理**：picker/列表选择替代手填 Long ID；保留独立页 | 只读会废掉现有入口；与 PC 独立页形态一致 |

## 2. 连带产品口径（SCOPE）

- **`RBAC-BE-SCOPE-002`：启用。** 机构管理员（`admin`）的 `ORG_ID` scope 扩展为「本机构 + 全部子孙机构」；普通 `user` 仍仅本机构；超管不过滤。
- PC **`RBAC-PC-SCOPE-001`**：列表页展示数据范围文案，与上述口径一致（例：超管「全部」/ 机构管理员「本机构及下级」/ 普通用户「仅本人所属机构」）。

## 3. 非目标（本批次不做）

- 批次 4 视觉/乱码/console
- finance 模块角色 contains
- 重写 midscene 账号体系（复用现有 `.env` persona）
- 删除用户表单内 org/role 快捷分配（可并存）

## 4. 验收对齐

评分卡批次 3：Midscene/Playwright + 静态检查 `src/views/user` 引用 `components/rbac`、关系不再手填 ID、三端关系形态一致。
