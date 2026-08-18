# 开发规范补充

## 礼尚往来管理模块

本文件补充礼尚往来后端模块的落地约束。若与上层仓库规范冲突，以更高优先级的 `DEVELOPMENT.md` 为准。

### 实体与表

- 所有业务实体必须 `extends BaseEntity<T>`。
- 禁止重复定义 `id`、`creator`、`create_time`、`updater`、`update_time`、`operator`、`operate_time`、`deleter`、`delete_time`、`is_delete`。
- 业务表统一以 `_info_t` 结尾，字段统一使用 `snake_case`。
- 时间字段统一使用 `LocalDateTime`。
- 逻辑删除统一使用 `@TableLogic` 对应的基础字段，不新增 `delete_flag` 或业务删除状态。

### 查询与分层

- Controller 只能接收 DTO、Query，并返回 VO 或统一结果对象，禁止直接返回 Entity。
- Service 层使用 `IService`、`ServiceImpl`、`LambdaQueryWrapper`、`LambdaUpdateWrapper` 和 `Page`。
- 禁止在 ServiceImpl 中拼接字符串 SQL；复杂查询优先封装为清晰的 Mapper 方法。
- 所有列表、详情、编辑、删除、统计、导出均必须带 `org_id` 数据隔离条件。

### 数据权限（与 org RBAC 对齐）

- `@DataPermission.scope()` 使用独立枚举 `DataPermissionScope`：`USER_OWNER`/`USER_IDS`（管理员机构成员子查询、普通用户本人）、`ORG_SHARED`（礼尚往来家庭共享）、`ORG_ID`（机构树，管理员扩子孙）。
- 角色判定必须精确匹配 `RbacRoleCodes`（`super_super` / `admin` / `user`），禁止 `code.contains("super|admin|user")`。
- 礼尚往来 mapper 使用 `scope = ORG_SHARED`，并配置 `alias` 与 `orgField`；删除用户/角色/菜单/权限/机构走 org 分支的 ownership + 级联，不使用 gift 的裸 `deleteByIds`。

### 回礼实现

回礼不单独拆表，统一记录在 `gift_record_info_t`：

- `direction = GIVE` 表示随礼。
- `direction = RECEIVE` 表示收礼。
- `direction = RETURN` 表示回礼。
- `related_record_id` 关联原收礼记录。
- `returned_flag` 标记原收礼记录是否已完成回礼。

### 验证

礼尚往来后端变更至少执行：

```bash
mvn clean -pl alex_miaosha_finance/finance_boot -am "-Dtest=GiftRecordBusinessRuleTest,GiftOwnershipTest,GiftStructureTest" -DfailIfNoTests=false test
```

### 空指针与类型安全

- 在进行 Stream 映射操作时，避免直接使用可能带有 `@Nullable` 属性的实例方法引用（如 `String::trim`），因为编译器（如 ECJ 等）会对方法引用的接收者 `this` 进行非空校验，从而报 `Null type safety: parameter 'this' ... needs unchecked conversion` 的警告或错误。
- 推荐使用显式的 Lambda 表达式或带非空校验的表达式进行转换，例如：`.map(s -> s == null ? "" : s.trim())`。

