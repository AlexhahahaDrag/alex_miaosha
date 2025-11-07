# 🤖 Cursor AI Agent 代码生成规范

本文件定义了该项目中 AI Agent 应遵循的代码生成规范。

---

## 📋 核心规范

### 1. 数据库表命名规范

#### ✅ 所有新建表的后缀统一为 `_info_t`

```sql
-- 正确示例
CREATE TABLE `contacts_user_relation_info_t` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  ...
) ENGINE=InnoDB;

-- 错误示例 ❌
CREATE TABLE `t_contacts_user_relation` (...)
CREATE TABLE `contacts_relation` (...)
```

**规则：**

- 表名全小写，单词间用下划线 `_` 分隔
- 末尾统一后缀：`_info_t`
- SQL 脚本中添加 `-- AI Agent` 标记

---

### 2. VO 中 Long 类型字段序列化 ⭐ 关键

#### ✅ 所有 Long 类型字段必须添加序列化注解

```java
// 导入语句
import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

// 应用到所有 Long 类型字段
@JsonSerialize(using = Long2StringSerializer.class)
@ApiModelProperty(value = "字段描述")
private Long fieldName;
```

**原因：** 解决前端 JavaScript 数字精度丢失问题

**适用范围：**
| 字段类型 | 添加注解 | 备注 |
|---------|--------|------|
| Long | ✅ **必须** | 所有 Long 类型 |
| Integer | ❌ 否 | 范围足够 |
| String | ❌ 否 | 字符串类型 |
| BigDecimal | ❌ 否 | 大数字用此 |

**错误 vs 正确：**

```java
// ❌ 错误 - 缺少序列化注解
@ApiModelProperty(value = "用户ID")
private Long userId;

// ✅ 正确
@JsonSerialize(using = Long2StringSerializer.class)
@ApiModelProperty(value = "用户ID，关联 user_info_t 表")
private Long userId;
```

---

### 3. 代码标记规范

#### ✅ 所有 AI 生成的代码必须标记

在类注释中添加 `AI Agent` 标记：

```java
/**
 * description:  联系人关系分类字典 Mapper
 * author:       alex
 * createDate:   2025-11-07 10:00:00
 * version:      1.0.0
 * AI Agent  ← 必须添加此标记
 */
@Mapper
public interface ContactsUserRelationMapper extends BaseMapper<ContactsUserRelation> {
    // ...
}
```

---

### 4. 注释规范

#### ✅ 字段注释应包含关联表信息

```java
// ❌ 不清晰
@ApiModelProperty(value = "用户ID")
private Long userId;

// ✅ 清晰 - 包含关联表信息
@JsonSerialize(using = Long2StringSerializer.class)
@ApiModelProperty(value = "用户ID，关联 user_info_t 表")
private Long userId;
```

**规则：**

- 对于外键字段，注释中明确说明关联的表
- 对于枚举字段，说明字典类型
- 对于时间字段，说明时间格式（如：毫秒级时间戳）

---

### 5. SQL 脚本规范

#### ✅ 表创建脚本规范

```sql
-- 联系人关系分类字典表 - SQL 脚本
-- author: alex
-- createDate: 2025-11-07
-- description: 用于维护关系分类字典，包括系统公共分类和用户自定义分类

-- AI Agent  ← 添加此标记
CREATE TABLE IF NOT EXISTS `contacts_user_relation_info_t` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID，为空表示公共字典，有值表示用户自定义分类',
  `relationship_tag` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '关系标签',
  ...
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='联系人关系分类字典表';
```

**规则：**

- 表头注释包含：表名、脚本说明、作者、创建日期、描述
- 添加 `-- AI Agent` 标记（如果是 AI 生成）
- 每个字段有清晰的注释

---

### 6. Mapper XML 规范

#### ✅ 查询关联未删除的数据

```xml
<!-- ✅ 正确 - 添加未删除条件 -->
<select id="getPage" resultMap="VoResultMap">
    SELECT ... FROM t_table a
    LEFT JOIN other_table b ON a.id = b.id AND b.is_delete = 0
    WHERE a.is_delete = 0
</select>

<!-- ❌ 错误 - 缺少未删除条件 -->
<select id="getPage" resultMap="VoResultMap">
    SELECT ... FROM t_table a
    LEFT JOIN other_table b ON a.id = b.id
    WHERE a.is_delete = 0
</select>
```

---

## 📝 完整示例

### Entity 类示例

```java
package com.alex.finance.example.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  示例实体
 * author:       alex
 * createDate:   2025-11-07 10:00:00
 * version:      1.0.0
 * AI Agent
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("example_info_t")
@ApiModel(value = "Example对象", description = "示例表")
public class Example extends BaseEntity<Example> {

    @ApiModelProperty(value = "用户ID，关联 user_info_t 表")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "用户名")
    @TableField("name")
    private String name;
}
```

### VO 类示例

```java
package com.alex.api.finance.example.vo;

import com.alex.common.common.BaseVo;
import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  示例 VO
 * author:       alex
 * createDate:   2025-11-07 10:00:00
 * version:      1.0.0
 * AI Agent
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "ExampleVo", description = "示例 VO")
public class ExampleVo extends BaseVo<ExampleVo> {

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "用户ID，关联 user_info_t 表")
    private Long userId;

    @ApiModelProperty(value = "用户名")
    private String name;
}
```

---

## ✅ 生成代码前检查清单

- [ ] 表名是否以 `_info_t` 结尾
- [ ] 所有 Long 字段是否添加了 `@JsonSerialize(using = Long2StringSerializer.class)` 注解
- [ ] 是否导入了正确的序列化类
- [ ] 类注释中是否添加了 `AI Agent` 标记
- [ ] 字段注释是否包含关联表信息
- [ ] Mapper XML 中的 LEFT JOIN 是否添加了 `AND table.is_delete = 0` 条件
- [ ] 是否有 linter 错误

---

## 📚 参考文件

项目中已有的示例：

- `PersonalGiftVo.java` - Long 序列化注解示例
- `ContactsUserRelationVo.java` - 完整的 VO 类示例
- `contacts_user_relation.sql` - SQL 脚本示例
- `ContactsUserRelationMapper.xml` - Mapper XML 示例

---

## 📞 更新记录

| 日期       | 版本  | 内容                   |
| ---------- | ----- | ---------------------- |
| 2025-11-07 | 1.0.0 | 初版创建，定义核心规范 |
