# 联系人关系管理模块（ContactsUserRelation）使用指南

## 📋 模块概述

`ContactsUserRelation` 是一个用于维护**用户与联系人之间的关系**的管理模块。每个用户可以为其对应的联系人进行多维度的关系分类和标签管理。

### 核心特性

- ✅ 支持为每个用户创建自己的联系人关系
- ✅ 支持按关系标签分类（如：重要客户、潜在客户等）
- ✅ 支持重要程度设置（1-普通，2-重要，3-非常重要）
- ✅ 支持记录最后联系时间
- ✅ 系统自动为新用户初始化默认关系分类
- ✅ 支持灵活的查询和筛选

---

## 🗄️ 数据库表设计

### 表结构：`t_contacts_user_relation`

```sql
-- 表字段说明
id                  | 主键ID | bigint | 自增
user_id             | 用户ID | bigint | 关键字段（索引）
contacts_user_id    | 联系人ID | bigint | 关键字段（索引）
relationship_tag    | 关系标签 | varchar(100) | 如：重要客户、潜在客户等
importance          | 重要程度 | tinyint | 1-普通，2-重要，3-非常重要
last_contact_time   | 最后联系时间 | bigint | 时间戳
remarks             | 备注信息 | varchar(500) | 自定义备注
creator             | 创建人 | varchar(64) | 审计字段
create_time         | 创建时间 | bigint | 审计字段
updater             | 更新人 | varchar(64) | 审计字段
update_time         | 更新时间 | bigint | 审计字段
operator            | 操作人 | varchar(64) | 审计字段
operate_time        | 操作时间 | bigint | 审计字段
deleter             | 删除人 | varchar(64) | 审计字段
delete_time         | 删除时间 | bigint | 审计字段
is_delete           | 是否删除 | tinyint | 0-否，1-是（逻辑删除）
```

### 索引说明

- `idx_user_id`: 用户 ID 单列索引
- `idx_contacts_user_id`: 联系人 ID 单列索引
- `idx_user_contacts`: 用户+联系人组合索引
- `uk_user_contacts`: 用户+联系人+删除标记的组合唯一索引（防止重复）

---

## 📂 项目文件结构

```
finance_boot/src/main/java/com/alex/finance/contactsUserRelation/
├── controller/
│   └── ContactsUserRelationController.java    # Rest API 控制器
├── entity/
│   └── ContactsUserRelation.java              # JPA 实体类
├── mapper/
│   ├── ContactsUserRelationMapper.java        # MyBatis Mapper 接口
│   └── ContactsUserRelationMapper.xml         # MyBatis SQL 映射
├── service/
│   ├── ContactsUserRelationService.java       # 服务层接口
│   └── impl/
│       └── ContactsUserRelationServiceImpl.java # 服务层实现

finance_api/src/main/java/com/alex/api/finance/contactsUserRelation/
└── vo/
    └── ContactsUserRelationVo.java            # 数据传输对象

doc/sql/
└── contacts_user_relation.sql                 # 数据库初始化脚本
```

---

## 🚀 快速开始

### 1️⃣ 创建数据库表

执行 SQL 脚本创建表：

```bash
# 复制 doc/sql/contacts_user_relation.sql 中的 SQL 语句
# 在数据库中执行即可
```

或使用 MySQL 命令：

```bash
mysql -u root -p 数据库名 < doc/sql/contacts_user_relation.sql
```

### 2️⃣ 初始化用户的默认关系

新用户注册时，调用以下接口为其创建默认的联系人关系分类：

**请求 URL：**

```
POST /api/v1/contacts-user-relation/init-default
```

**请求参数：**

```json
{
  "userId": 1 // 用户ID
}
```

**响应示例：**

```json
{
  "code": "0000",
  "message": "操作成功",
  "data": true
}
```

**默认创建的关系分类：**
| 标签 | 重要程度 | 说明 |
|------|--------|------|
| 重要客户 | 3 | 级别最高 |
| 潜在客户 | 2 | 中等级别 |
| 合作伙伴 | 2 | 中等级别 |
| 家庭成员 | 2 | 中等级别 |
| 工作同事 | 1 | 级别最低 |
| 朋友 | 1 | 级别最低 |

---

## 📡 API 接口文档

### 接口列表

#### 1. 获取联系人关系分页列表

```
POST /api/v1/contacts-user-relation/page
```

**请求体：**

```json
{
  "pageNum": 1,
  "pageSize": 10,
  "userId": 1,
  "vo": {
    "relationshipTag": "重要客户", // 可选，按标签筛选
    "importance": 3, // 可选，按重要程度筛选
    "contactsUserName": "张三", // 可选，按联系人名称搜索
    "contactsUserPhone": "13800000000", // 可选，按联系人电话搜索
    "keyword": "搜索关键字" // 可选，模糊搜索名称/电话/备注
  }
}
```

**响应示例：**

```json
{
  "code": "0000",
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "contactsUserId": 10,
        "relationshipTag": "重要客户",
        "importance": 3,
        "lastContactTime": 1699344000000,
        "remarks": "VIP客户",
        "contactsUserName": "张三",
        "contactsUserPhone": "13800000001",
        "createTime": 1699344000000,
        "updateTime": 1699344000000
      }
    ],
    "total": 15,
    "size": 10,
    "current": 1,
    "pages": 2
  }
}
```

---

#### 2. 获取联系人关系详情

```
GET /api/v1/contacts-user-relation?id=1
```

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 关系 ID |

**响应示例：**

```json
{
  "code": "0000",
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 1,
    "contactsUserId": 10,
    "relationshipTag": "重要客户",
    "importance": 3,
    "lastContactTime": 1699344000000,
    "remarks": "VIP客户",
    "contactsUserName": "张三",
    "contactsUserPhone": "13800000001",
    "createTime": 1699344000000,
    "updateTime": 1699344000000
  }
}
```

---

#### 3. 新增联系人关系

```
POST /api/v1/contacts-user-relation
```

**请求体：**

```json
{
  "userId": 1, // 必填，用户ID
  "contactsUserId": 10, // 必填，联系人ID
  "relationshipTag": "重要客户", // 必填，关系标签
  "importance": 3, // 可选，重要程度（1-3）
  "lastContactTime": 1699344000000, // 可选，最后联系时间
  "remarks": "VIP客户，需重点维护" // 可选，备注
}
```

**响应示例：**

```json
{
  "code": "0000",
  "message": "操作成功",
  "data": true
}
```

---

#### 4. 修改联系人关系

```
PUT /api/v1/contacts-user-relation
```

**请求体：**

```json
{
  "id": 1, // 必填，关系ID
  "userId": 1, // 必填，用户ID
  "contactsUserId": 10, // 必填，联系人ID
  "relationshipTag": "潜在客户", // 可选，新的关系标签
  "importance": 2, // 可选，新的重要程度
  "lastContactTime": 1699344000000, // 可选，最后联系时间
  "remarks": "更新的备注信息" // 可选，备注
}
```

**响应示例：**

```json
{
  "code": "0000",
  "message": "操作成功",
  "data": true
}
```

---

#### 5. 删除联系人关系

```
DELETE /api/v1/contacts-user-relation?ids=1,2,3
```

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | String | 是 | 关系 ID 集合，多个 ID 用逗号分隔 |

**响应示例：**

```json
{
  "code": "0000",
  "message": "操作成功",
  "data": true
}
```

---

#### 6. 初始化用户默认联系人关系

```
POST /api/v1/contacts-user-relation/init-default
```

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户 ID |

**响应示例：**

```json
{
  "code": "0000",
  "message": "操作成功",
  "data": true
}
```

---

## 🔍 使用场景示例

### 场景 1：用户注册后初始化默认关系

```java
// 用户注册流程中调用
POST /api/v1/contacts-user-relation/init-default?userId=1

// 响应成功后，系统会为用户创建 6 个默认的关系分类
```

### 场景 2：维护现有联系人关系

```java
// 1. 获取用户的所有关系
POST /api/v1/contacts-user-relation/page
{
  "pageNum": 1,
  "pageSize": 10,
  "userId": 1,
  "vo": {}
}

// 2. 修改某个联系人的关系
PUT /api/v1/contacts-user-relation
{
  "id": 1,
  "userId": 1,
  "contactsUserId": 10,
  "relationshipTag": "潜在客户",
  "importance": 2,
  "lastContactTime": 1699344000000
}

// 3. 删除关系
DELETE /api/v1/contacts-user-relation?ids=1
```

### 场景 3：按关系筛选联系人

```java
// 获取用户的所有"重要客户"
POST /api/v1/contacts-user-relation/page
{
  "pageNum": 1,
  "pageSize": 10,
  "userId": 1,
  "vo": {
    "relationshipTag": "重要客户",
    "importance": 3
  }
}

// 获取用户最近联系过的人
POST /api/v1/contacts-user-relation/page
{
  "pageNum": 1,
  "pageSize": 10,
  "userId": 1,
  "vo": {
    "keyword": "张"  // 名字包含"张"
  }
}
```

---

## 💡 最佳实践

### 1. 新用户注册时初始化关系

```java
// 在用户注册或首次登录时调用
@Transactional
public void onUserRegistered(Long userId) {
    contactsUserRelationService.initDefaultRelations(userId);
}
```

### 2. 定期更新最后联系时间

```java
// 在用户与联系人发生交互时更新
public void updateLastContactTime(Long relationId) {
    ContactsUserRelationVo vo = new ContactsUserRelationVo();
    vo.setId(relationId);
    vo.setLastContactTime(System.currentTimeMillis());
    contactsUserRelationService.updateContactsUserRelation(vo);
}
```

### 3. 使用关键字搜索而非精确匹配

```java
// 推荐：使用关键字搜索
POST /api/v1/contacts-user-relation/page
{
  "vo": {
    "keyword": "张"  // 模糊搜索，可找到"张三"、"李张四"等
  }
}

// 不推荐：使用精确匹配
{
  "vo": {
    "contactsUserName": "张三"  // 只能精确匹配"张三"
  }
}
```

---

## ⚠️ 注意事项

1. **数据一致性**：在删除联系人时，需要同时处理相关的联系人关系记录（建议使用级联删除或手动清理）

2. **性能优化**：当用户关系数量很大时，建议：

   - 使用分页查询而不是一次加载全部
   - 添加合适的业务缓存
   - 定期清理逻辑删除的过期数据

3. **并发控制**：同一个用户同一个联系人的关系记录应该唯一，系统已通过数据库唯一索引保证

4. **时间戳**：`lastContactTime` 建议使用毫秒级时间戳，便于计算和排序

5. **审计日志**：所有操作都会记录创建人、更新人等审计信息，便于追溯

---

## 🔧 常见问题

### Q1: 如何快速获取用户的所有重要客户？

```java
POST /api/v1/contacts-user-relation/page
{
  "pageNum": 1,
  "pageSize": 100,
  "userId": 1,
  "vo": {
    "relationshipTag": "重要客户",
    "importance": 3
  }
}
```

### Q2: 同一个用户可以有多个相同的联系人关系吗？

否，系统使用组合唯一索引（user_id + contacts_user_id）防止重复。如需更新关系，请使用 PUT 接口。

### Q3: 删除是物理删除还是逻辑删除？

是**逻辑删除**，通过 `is_delete` 字段标记。这样可以保留历史记录用于审计。

### Q4: 如何批量初始化多个用户的默认关系？

```java
List<Long> userIds = Arrays.asList(1L, 2L, 3L);
for (Long userId : userIds) {
    contactsUserRelationService.initDefaultRelations(userId);
}
```

---

## 📞 技术支持

如有任何问题或建议，请联系开发团队。

---

**最后更新时间**：2025-11-07  
**AI Agent 标记**：✅ 已标记
