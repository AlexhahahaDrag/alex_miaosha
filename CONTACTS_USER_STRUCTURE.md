# 联系人管理模块 (ContactsUser) 完整设计文档

## 📋 模块概述

联系人管理模块用于管理个人的联系人信息，包括朋友、家人、同事等不同关系的联系人。该模块提供了完整的 CRUD 操作和导入功能。

---

## 📁 项目文件结构

```
finance_boot/
└── src/main/java/com/alex/finance/contactsUser/
    ├── controller/
    │   └── ContactsUserController.java          # REST API 控制层
    ├── entity/
    │   └── ContactsUser.java                    # 数据库实体类
    ├── mapper/
    │   ├── ContactsUserMapper.java              # MyBatis Mapper 接口
    │   └── ContactsUserMapper.xml               # MyBatis XML 映射文件
    └── service/
        ├── ContactsUserService.java             # 服务接口
        └── impl/
            └── ContactsUserServiceImpl.java      # 服务实现（待创建）

finance_api/
└── src/main/java/com/alex/api/finance/contactsUser/
    └── vo/
        └── ContactsUserVo.java                  # 值对象（请求/响应）

resources (finance_boot)/
└── db/migration/
    └── V1_0_0__create_contacts_user_table.sql   # 数据库DDL脚本
```

---

## 🗄️ 数据库设计

### 表名：`t_contacts_user`

#### 主要字段说明

| 字段名       | 类型     | 长度 | 必填 | 说明                                    |
| ------------ | -------- | ---- | ---- | --------------------------------------- |
| id           | BIGINT   | -    | ✓    | 主键，自增长                            |
| name         | VARCHAR  | 100  | ✓    | 联系人姓名                              |
| phone        | VARCHAR  | 20   | ✓    | 联系电话                                |
| relationship | VARCHAR  | 50   | ✓    | 关系类型(friend/family/colleague/other) |
| email        | VARCHAR  | 100  | ✗    | 电子邮箱                                |
| address      | VARCHAR  | 500  | ✗    | 联系地址                                |
| remarks      | VARCHAR  | 1000 | ✗    | 备注信息                                |
| is_favorite  | TINYINT  | 1    | ✗    | 常用联系人标记(0/1)                     |
| create_time  | DATETIME | -    | ✗    | 创建时间(自动)                          |
| update_time  | DATETIME | -    | ✗    | 更新时间(自动)                          |
| create_by    | VARCHAR  | 100  | ✗    | 创建人                                  |
| update_by    | VARCHAR  | 100  | ✗    | 修改人                                  |
| del_flag     | TINYINT  | 1    | ✗    | 逻辑删除标记(0/1)                       |

#### 索引设计

- `idx_name` - 姓名索引（查询优化）
- `idx_phone` - 电话索引（快速查找）
- `idx_relationship` - 关系类型索引（分类查询）
- `idx_is_favorite` - 常用联系人索引
- `idx_del_flag` - 删除标记索引（逻辑删除）
- `idx_create_time` - 创建时间索引

---

## 🎯 API 端点设计

基础 URL：`/api/v1/contacts-user`

### 1. 获取分页列表

- **方法**：POST
- **路径**：`/page`
- **参数**：
  ```json
  {
    "pageNum": 1,
    "pageSize": 10,
    "name": "张三",
    "phone": "13800000000",
    "relationship": "friend"
  }
  ```
- **返回**：分页的 ContactsUserVo 列表

### 2. 获取详情

- **方法**：GET
- **路径**：`/?id=1`
- **参数**：id（联系人 ID）
- **返回**：单个 ContactsUserVo 对象

### 3. 新增联系人

- **方法**：POST
- **路径**：`/`
- **参数**：
  ```json
  {
    "name": "张三",
    "phone": "13800000000",
    "relationship": "friend",
    "email": "zhangsan@example.com",
    "address": "北京市朝阳区",
    "remarks": "亲密朋友",
    "isFavorite": 1
  }
  ```
- **返回**：Boolean（成功/失败）

### 4. 修改联系人

- **方法**：PUT
- **路径**：`/`
- **参数**：同新增，需要提供 id
- **返回**：Boolean（成功/失败）

### 5. 删除联系人

- **方法**：DELETE
- **路径**：`/?ids=1,2,3`
- **参数**：ids（多个 ID 用逗号分隔）
- **返回**：Boolean（成功/失败）

### 6. 导入联系人

- **方法**：POST
- **路径**：`/import`
- **参数**：上传 Excel 文件
- **返回**：Boolean（成功/失败）

### 7. 按关系类型查询

- **方法**：GET
- **路径**：`/by-relationship`
- **参数**：
  - `pageNum`：页码
  - `pageSize`：每页大小
  - `relationship`：关系类型
- **返回**：分页的 ContactsUserVo 列表

---

## 💾 关系类型字典

| 值        | 说明 |
| --------- | ---- |
| friend    | 朋友 |
| family    | 家人 |
| colleague | 同事 |
| other     | 其他 |

---

## 📝 类设计说明

### ContactsUser（实体类）

- 对应数据库表 `t_contacts_user`
- 继承 `BaseEntity<ContactsUser>`（基类提供通用字段）
- 使用 Lombok 简化 getter/setter
- 使用 MyBatis Plus 的@TableName 和@TableField 注解

### ContactsUserVo（值对象）

- 用于 API 请求和响应
- 包含 JSR-303 验证注解
- 支持分组验证（Insert/Update）
- 提供 Swagger 文档支持

### ContactsUserService（服务接口）

- 继承 IService<ContactsUser>（MyBatis Plus 基础服务）
- 定义业务方法

### ContactsUserMapper（数据访问层）

- 继承 BaseMapper<ContactsUser>（MyBatis Plus CRUD 操作）
- 支持二级缓存

---

## 🔄 请求/响应示例

### 新增联系人请求

```http
POST /api/v1/contacts-user
Content-Type: application/json

{
  "name": "李四",
  "phone": "13900000000",
  "relationship": "colleague",
  "email": "lisi@example.com",
  "address": "北京市海淀区",
  "remarks": "工作同事",
  "isFavorite": 0
}
```

### 成功响应

```json
{
  "success": true,
  "message": "操作成功",
  "data": true,
  "code": "000000"
}
```

### 查询分页列表请求

```http
POST /api/v1/contacts-user/page
Content-Type: application/json

{
  "pageNum": 1,
  "pageSize": 10,
  "relationship": "friend"
}
```

### 分页响应

```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "张三",
        "phone": "13800000000",
        "relationship": "friend",
        "email": "zhangsan@example.com",
        "address": "北京市朝阳区",
        "remarks": "亲密朋友",
        "isFavorite": 1,
        "createTime": "2025-11-03 10:00:00",
        "updateTime": "2025-11-03 10:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "orders": [],
    "optimizeCountSql": true,
    "searchCount": true,
    "maxLimit": null,
    "countId": null,
    "pages": 1
  },
  "code": "000000"
}
```

---

## ⚙️ 待完成事项

1. **创建 Service 实现类** - `ContactsUserServiceImpl.java`

   - 实现`getPage()`方法（带条件查询）
   - 实现`queryContactsUser()`方法
   - 实现`addContactsUser()`方法
   - 实现`updateContactsUser()`方法
   - 实现`deleteContactsUser()`方法
   - 实现`importContactsUser()`方法（Excel 导入）

2. **添加高级查询功能**

   - 支持多条件组合查询
   - 支持模糊搜索
   - 支持日期范围查询

3. **添加业务逻辑**

   - 数据去重检查（电话号码唯一性）
   - 批量导入验证
   - 联系人分组功能

4. **创建单元测试**
   - Controller 层测试
   - Service 层测试
   - Mapper 层测试

---

## 🔐 权限和安全考虑

- 每个用户只能访问自己的联系人信息
- 支持软删除，数据安全回收
- 需要添加用户 ID 字段用于数据隔离
- 导入文件需要验证和清理

---

## 📚 参考

- 参考了 PersonalGift 模块的设计模式
- 遵循项目的通用架构规范
- 使用 MyBatis Plus 作为 ORM 框架
- 支持 Knife4j 的 API 文档生成
