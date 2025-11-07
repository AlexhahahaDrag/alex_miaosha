# ContactsUserRelation 集成指南

## 📝 集成步骤

### 1️⃣ 确认项目结构

确保以下文件已正确创建：

```
finance_boot/src/main/java/com/alex/finance/contactsUserRelation/
├── controller/
│   └── ContactsUserRelationController.java
├── entity/
│   └── ContactsUserRelation.java
├── mapper/
│   ├── ContactsUserRelationMapper.java
│   └── ContactsUserRelationMapper.xml
├── service/
│   ├── ContactsUserRelationService.java
│   └── impl/
│       └── ContactsUserRelationServiceImpl.java

finance_api/src/main/java/com/alex/api/finance/contactsUserRelation/
└── vo/
    └── ContactsUserRelationVo.java
```

### 2️⃣ 执行数据库初始化 SQL

运行以下 SQL 脚本创建表：

```bash
# 找到文件：doc/sql/contacts_user_relation.sql
# 在数据库中执行该脚本
```

SQL 脚本会自动创建：

- 表：`t_contacts_user_relation`
- 索引：用户 ID、联系人 ID、组合索引等

### 3️⃣ 验证 Mapper XML 配置

确保 `ContactsUserRelationMapper.xml` 位于正确的位置：

```
finance_boot/src/main/java/com/alex/finance/contactsUserRelation/mapper/ContactsUserRelationMapper.xml
```

或者将其放在 resources 目录：

```
finance_boot/src/main/resources/mapper/finance/contactsUserRelationMapper.xml
```

**如果使用 resources 目录方式**，需要在 `pom.xml` 中配置 MyBatis 扫描路径：

```xml
<mybatis>
    <mapperLocations>
        classpath*:mapper/**/*.xml
    </mapperLocations>
</mybatis>
```

### 4️⃣ 代码注入点集成

#### 在用户服务中集成

找到用户注册或创建逻辑（通常在 `UserServiceImpl`），在用户创建成功后调用：

```java
import com.alex.finance.contactsUserRelation.service.ContactsUserRelationService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl {

    private final ContactsUserRelationService contactsUserRelationService;

    @Transactional(rollbackFor = Exception.class)
    public Boolean createUser(UserVo userVo) {
        // ... 创建用户的逻辑 ...
        User user = new User();
        BeanUtils.copyProperties(userVo, user);
        int inserted = userMapper.insert(user);

        if (inserted > 0) {
            // ✅ 为新用户初始化默认的联系人关系分类
            contactsUserRelationService.initDefaultRelations(user.getId());
            return true;
        }

        return false;
    }
}
```

### 5️⃣ 测试 API 端点

项目启动后，在 Swagger UI 中测试以下端点：

```
POST http://localhost:8080/api/v1/contacts-user-relation/init-default?userId=1
POST http://localhost:8080/api/v1/contacts-user-relation/page
GET http://localhost:8080/api/v1/contacts-user-relation?id=1
POST http://localhost:8080/api/v1/contacts-user-relation
PUT http://localhost:8080/api/v1/contacts-user-relation
DELETE http://localhost:8080/api/v1/contacts-user-relation?ids=1
```

---

## 🔗 相关代码文件引用

### 数据库配置

**文件位置**：`doc/sql/contacts_user_relation.sql`

**主要内容**：

- 表创建语句
- 索引创建语句
- 字段注释说明

### 核心业务类

| 文件                                   | 说明                         |
| -------------------------------------- | ---------------------------- |
| `ContactsUserRelationVo.java`          | 数据传输对象，用于前后端交互 |
| `ContactsUserRelation.java`            | JPA 实体类，对应数据库表     |
| `ContactsUserRelationMapper.java`      | MyBatis Mapper 接口          |
| `ContactsUserRelationMapper.xml`       | MyBatis SQL 映射配置         |
| `ContactsUserRelationService.java`     | 业务服务接口                 |
| `ContactsUserRelationServiceImpl.java` | 业务服务实现                 |
| `ContactsUserRelationController.java`  | REST API 控制器              |

---

## 🛠️ 配置建议

### 1. 日志配置（可选）

在 `application.yml` 中添加 MyBatis 日志：

```yaml
mybatis:
  configuration:
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  mapper-locations: classpath*:mapper/**/*.xml
```

### 2. 分布式事务处理

如果使用分布式事务，确保在关键方法上添加注解：

```java
@Transactional(rollbackFor = Exception.class)
public Boolean addContactsUserRelation(ContactsUserRelationVo vo) {
    // ...
}
```

### 3. 权限控制（可选）

如需添加权限验证，可在 Controller 方法上添加：

```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping(value = "/init-default")
public Result<Boolean> initDefaultRelations(@RequestParam(value = "userId") Long userId) {
    // ...
}
```

---

## 📊 数据流转说明

### 创建联系人关系的流程

```
请求 (ContactsUserRelationVo)
    ↓
Controller.add()
    ↓
Service.addContactsUserRelation() [参数验证]
    ↓
checkDuplicate() [检查唯一性]
    ↓
Mapper.insert() [执行数据库插入]
    ↓
返回成功/失败
```

### 查询联系人关系的流程

```
请求 (userId, pageNum, pageSize, 查询条件)
    ↓
Controller.getPage()
    ↓
Service.getPage() [参数验证]
    ↓
Mapper.getPage() [SQL JOIN 查询]
    ↓
关联联系人表获取名称、电话等信息
    ↓
返回分页数据 (ContactsUserRelationVo列表)
```

---

## ⚡ 性能优化建议

### 1. 索引优化

已创建的索引：

- `idx_user_id`: 单列索引
- `idx_contacts_user_id`: 单列索引
- `idx_user_contacts`: 组合索引（查询关键）
- `uk_user_contacts`: 唯一索引（防重复）

### 2. 查询优化

- 分页查询：默认每页 10 条，可根据实际调整
- 使用组合索引：user_id + contacts_user_id 的查询效率最高
- 避免全表扫描：始终添加用户 ID 作为查询条件

### 3. 缓存建议

```java
// 建议缓存用户的所有关系（可选）
@Cacheable(value = "userRelations", key = "#userId")
public Page<ContactsUserRelationVo> getPage(Long pageNum, Long pageSize, Long userId, ContactsUserRelationVo vo) {
    // ...
}

// 更新时清除缓存
@CacheEvict(value = "userRelations", key = "#vo.userId")
public Boolean updateContactsUserRelation(ContactsUserRelationVo vo) {
    // ...
}
```

---

## 🐛 常见问题排查

### 问题 1：Mapper 找不到

**症状**：`Cannot find class mapping for identity`

**解决**：

1. 确认 `@Mapper` 注解已添加到 `ContactsUserRelationMapper`
2. 检查 `pom.xml` 中是否配置了 MyBatis 依赖
3. 确保 XML 文件的 namespace 与 Mapper 接口全路径一致

### 问题 2：SQL 执行出错

**症状**：`Column 'user_id' not found`

**解决**：

1. 确认已执行 `contacts_user_relation.sql` 初始化脚本
2. 验证表名和列名是否正确
3. 查看数据库连接配置是否正确

### 问题 3：初始化失败

**症状**：调用 `initDefaultRelations` 无响应

**解决**：

1. 检查日志文件，查看具体错误信息
2. 验证 userId 是否有效
3. 确认用户在 `t_user` 表中确实存在

### 问题 4：关系创建失败，提示"关系已存在"

**症状**：`该联系人关系已存在!`

**解决**：

1. 检查是否已经创建过相同的关系
2. 如需修改，请使用 PUT 接口而不是 POST
3. 删除旧关系后重新创建

---

## 📚 扩展建议

### 1. 添加关系历史记录

可创建 `t_contacts_user_relation_history` 表记录关系变更：

```sql
CREATE TABLE t_contacts_user_relation_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    relation_id BIGINT,
    user_id BIGINT,
    operation_type VARCHAR(50), -- 'CREATE', 'UPDATE', 'DELETE'
    old_value JSON,
    new_value JSON,
    operator VARCHAR(64),
    operation_time BIGINT
);
```

### 2. 添加关系统计功能

```java
// 获取用户的重要客户数量
public Long getImportantContactsCount(Long userId) {
    return contactsUserRelationMapper.countByUserAndImportance(userId, 3);
}

// 获取用户最近的活跃关系
public List<ContactsUserRelationVo> getRecentActiveRelations(Long userId, int limit) {
    return contactsUserRelationMapper.queryByUserOrderByLastContact(userId, limit);
}
```

### 3. 添加批量操作

```java
// 批量更新重要程度
@Transactional
public Boolean batchUpdateImportance(List<Long> relationIds, Integer importance) {
    // ...
}

// 批量迁移用户关系
@Transactional
public Boolean batchTransferRelations(Long fromUserId, Long toUserId) {
    // ...
}
```

---

## ✅ 集成检查清单

- [ ] 创建了所有必需的 Java 文件
- [ ] 创建了 Mapper XML 文件并配置正确
- [ ] 执行了数据库初始化 SQL 脚本
- [ ] 在用户服务中集成了 `initDefaultRelations` 调用
- [ ] 测试了所有 6 个 API 端点
- [ ] 验证了数据库表和索引已创建
- [ ] 添加了必要的业务日志
- [ ] 文档已更新
- [ ] 代码风格与项目保持一致
- [ ] 所有类都已标记 "AI Agent" 标签

---

**集成完成时间**：2025-11-07  
**最后更新**：2025-11-07
