# 功能清单

## 礼尚往来管理

礼尚往来管理模块位于财务服务中，用于管理随礼、收礼、回礼与人情统计。模块复用现有用户体系、组织体系、RBAC 权限体系和管理端/移动端框架，不新增独立账号、组织或权限基础设施。

### 后端能力

- 亲友管理：维护亲友姓名、电话、关系标签、备注、所属组织与所属用户。
- 关系管理：维护亲友之间的关系边界，支持后续统计与查询。
- 事由管理：维护婚宴、满月、乔迁、生日等礼金事件，并统计参与人数与金额。
- 礼金记录：使用 `gift_record_info_t` 统一保存随礼、收礼、回礼流水。
- 回礼分析：按 `收到金额 - 已回金额 = 待回金额` 计算待回礼金额。
- 统计分析：支持月度、年度、收礼排行、随礼排行、人情净值与趋势统计。
- Excel 导出：管理端礼金记录可按筛选条件导出。

### 表结构

- `gift_person_info_t`
- `gift_relation_info_t`
- `gift_event_info_t`
- `gift_record_info_t`

所有业务表均使用 `_info_t` 后缀，字段使用 `snake_case`，时间字段使用 `LocalDateTime`，实体继承 `com.alex.common.common.BaseEntity`，逻辑删除复用 BaseEntity 中的 `is_delete`。

### 权限标识

- 菜单：`gift:dashboard`、`gift:person`、`gift:event`、`gift:record`、`gift:return`、`gift:analysis`
- 按钮：`gift:view`、`gift:add`、`gift:edit`、`gift:delete`、`gift:export`

权限初始化 SQL 位于 `doc/sql/gift_management_permission.sql`，表结构 SQL 位于 `doc/sql/gift_management_schema.sql`。
