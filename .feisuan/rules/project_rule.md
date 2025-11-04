
# 开发规范指南

为保证代码质量、可维护性、安全性与可扩展性，请在开发过程中严格遵循以下规范。

## 一、项目基础信息

### 工作目录
- **工作区路径**: `E:\workspace\myself\backend\alex_miaosha`

### 技术栈要求

- **主框架**：Spring Boot 2.7.2
- **语言版本**：Java 17
- **构建工具**：Maven
- **代码作者**：Administrator
- **第一语言**：中文

### 目录结构

```
alex_miaosha
├── alex_generator
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       ├── alex
│           │       │   └── generator
│           │       │       ├── config
│           │       │       ├── controller
│           │       │       ├── service
│           │       │       │   └── impl
│           │       │       └── vo
│           │       └── baomidou
│           │           └── mybatisplus
│           │               └── generator
│           │                   ├── config
│           │                   │   ├── builder
│           │                   │   └── po
│           │                   └── engine
│           └── resources
│               └── templates
│                   ├── mobile
│                   └── vue
├── alex_miaosha_api_doc
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── alex
│           │           └── apidoc
│           │               └── config
│           └── resources
├── alex_miaosha_base
│   └── src
│       └── main
│           └── java
│               └── com
│                   └── alex
│                       └── base
│                           ├── common
│                           ├── constants
│                           └── enums
├── alex_miaosha_common
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── alex
│           │           └── common
│           │               ├── annotations
│           │               │   └── user
│           │               ├── common
│           │               ├── config
│           │               │   └── redis
│           │               ├── constants
│           │               │   ├── message
│           │               │   └── redis
│           │               ├── enums
│           │               ├── exception
│           │               │   └── handler
│           │               ├── handler
│           │               ├── obj
│           │               ├── pojo
│           │               │   ├── dto
│           │               │   └── vo
│           │               ├── redis
│           │               │   └── key
│           │               ├── utils
│           │               │   ├── bean
│           │               │   ├── date
│           │               │   ├── redis
│           │               │   ├── secret
│           │               │   └── string
│           │               └── validator
│           │                   └── group
│           └── resources
├── alex_miaosha_finance
│   ├── finance_api
│   │   └── src
│   │       └── main
│   │           └── java
│   │               └── com
│   │                   └── alex
│   │                       └── api
│   │                           └── finance
│   │                               ├── api
│   │                               │   ├── fallback
│   │                               │   ├── shopFinance
│   │                               │   └── shopStock
│   │                               ├── contactsUser
│   │                               │   └── vo
│   │                               ├── personalGift
│   │                               │   ├── api
│   │                               │   └── vo
│   │                               ├── prepaidCardInfoT
│   │                               │   ├── api
│   │                               │   └── vo
│   │                               ├── prepaidConsumeRecordT
│   │                               │   ├── api
│   │                               │   └── vo
│   │                               ├── shopCart
│   │                               │   ├── api
│   │                               │   └── vo
│   │                               ├── shopOrder
│   │                               │   ├── api
│   │                               │   └── vo
│   │                               ├── shopOrderDetail
│   │                               │   ├── api
│   │                               │   └── vo
│   │                               ├── shopStockAnalysis
│   │                               │   ├── api
│   │                               │   └── vo
│   │                               ├── shopStockAttrs
│   │                               │   ├── api
│   │                               │   └── vo
│   │                               ├── shopStockBatch
│   │                               │   ├── api
│   │                               │   └── vo
│   │                               └── vo
│   │                                   ├── accountRecordInfo
│   │                                   ├── dict
│   │                                   ├── finance
│   │                                   ├── financeAnalysis
│   │                                   ├── shopFinance
│   │                                   ├── shopFinanceAnalysis
│   │                                   └── shopStock
│   └── finance_boot
│       └── src
│           └── main
│               ├── java
│               │   └── com
│               │       └── alex
│               │           ├── api
│               │           │   └── finance
│               │           │       └── contactsUser
│               │           │           └── vo
│               │           └── finance
│               │               ├── config
│               │               ├── contactsUser
│               │               │   ├── controller
│               │               │   ├── entity
│               │               │   ├── mapper
│               │               │   └── service
│               │               │       └── impl
│               │               ├── controller
│               │               │   ├── accountRecordInfo
│               │               │   ├── analysis
│               │               │   ├── dict
│               │               │   ├── finance
│               │               │   ├── shopFinance
│               │               │   ├── shopFinanceAnalysis
│               │               │   ├── shopStock
│               │               │   └── wechat
│               │               ├── entity
│               │               │   ├── accountRecordInfo
│               │               │   ├── dict
│               │               │   ├── finance
│               │               │   ├── shopFinance
│               │               │   └── shopStock
│               │               ├── handler
│               │               ├── job
│               │               ├── mapper
│               │               │   ├── accountRecordInfo
│               │               │   ├── dict
│               │               │   ├── finance
│               │               │   ├── financeAnalysis
│               │               │   ├── shopFinance
│               │               │   └── shopStock
│               │               ├── personalGift
│               │               │   ├── controller
│               │               │   ├── entity
│               │               │   ├── mapper
│               │               │   └── service
│               │               │       └── impl
│               │               ├── prepaidCardInfoT
│               │               │   ├── controller
│               │               │   ├── entity
│               │               │   ├── mapper
│               │               │   └── service
│               │               │       └── impl
│               │               ├── prepaidConsumeRecordT
│               │               │   ├── controller
│               │               │   ├── entity
│               │               │   ├── mapper
│               │               │   └── service
│               │               │       └── impl
│               │               ├── service
│               │               │   ├── accountRecordInfo
│               │               │   │   └── impl
│               │               │   ├── analysis
│               │               │   │   └── impl
│               │               │   ├── dict
│               │               │   │   └── impl
│               │               │   ├── finance
│               │               │   │   └── impl
│               │               │   ├── shopFinance
│               │               │   │   └── impl
│               │               │   ├── shopFinanceAnalysis
│               │               │   │   └── impl
│               │               │   ├── shopStock
│               │               │   │   └── impl
│               │               │   └── weixin
│               │               │       └── impl
│               │               ├── shopCart
│               │               │   ├── controller
│               │               │   ├── entity
│               │               │   ├── mapper
│               │               │   └── service
│               │               │       └── impl
│               │               ├── shopOrder
│               │               │   ├── controller
│               │               │   ├── entity
│               │               │   ├── mapper
│               │               │   └── service
│               │               │       └── impl
│               │               ├── shopOrderDetail
│               │               │   ├── controller
│               │               │   ├── entity
│               │               │   ├── mapper
│               │               │   └── service
│               │               │       └── impl
│               │               ├── shopStockAnalysis
│               │               │   ├── controller
│               │               │   ├── mapper
│               │               │   └── service
│               │               │       └── impl
│               │               ├── shopStockAttrs
│               │               │   ├── controller
│               │               │   ├── entity
│               │               │   ├── mapper
│               │               │   └── service
│               │               │       └── impl
│               │               ├── shopStockBatch
│               │               │   ├── controller
│               │               │   ├── entity
│               │               │   ├── mapper
│               │               │   └── service
│               │               │       └── impl
│               │               └── utils
│               └── resources
│                   ├── db
│                   │   └── migration
│                   └── mapper
├── alex_miaosha_gateway
│   ├── src
│   │   └── main
│   │       ├── java
│   │       │   └── com
│   │       │       └── alex
│   │       │           └── gateway
│   │       │               ├── config
│   │       │               ├── fallback
│   │       │               ├── filter
│   │       │               ├── handler
│   │       │               └── utils
│   │       └── resources
│   └── web
│       └── WEB-INF
├── alex_miaosha_mission
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── alex
│           │           └── mission
│           │               ├── config
│           │               ├── controller
│           │               ├── manager
│           │               │   └── impl
│           │               ├── mapper
│           │               ├── pojo
│           │               │   ├── entity
│           │               │   └── vo
│           │               ├── rabbitmq
│           │               │   ├── ackmodel
│           │               │   │   └── manual
│           │               │   └── constants
│           │               └── service
│           │                   └── impl
│           └── resources
│               └── mapper
├── alex_miaosha_monitor
│   ├── src
│   │   └── main
│   │       ├── java
│   │       │   └── com
│   │       │       └── alex
│   │       │           └── monitor
│   │       └── resources
│   └── web
│       └── WEB-INF
├── alex_miaosha_order
│   ├── order_api
│   │   └── api
│   │       └── fallback
│   └── order_boot
│       └── src
│           └── main
│               ├── java
│               │   └── com.alex.order
│               └── resources
├── alex_miaosha_oss
│   ├── oss_api
│   │   └── src
│   │       └── main
│   │           └── java
│   │               └── com
│   │                   └── alex
│   │                       └── api
│   │                           └── oss
│   │                               ├── api
│   │                               │   └── fallback
│   │                               └── vo
│   │                                   └── fileInfo
│   └── oss_boot
│       └── src
│           └── main
│               ├── java
│               │   └── com
│               │       └── alex
│               │           └── oss
│               │               ├── config
│               │               │   ├── minio
│               │               │   ├── mybatisplus
│               │               │   └── swagger
│               │               ├── controller
│               │               │   └── fileInfo
│               │               ├── entity
│               │               │   └── fileInfo
│               │               ├── mapper
│               │               │   └── fileInfo
│               │               ├── service
│               │               │   ├── fileInfo
│               │               │   │   └── impl
│               │               │   └── minio
│               │               │       └── impl
│               │               └── vo
│               └── resources
├── alex_miaosha_product
│   ├── product_api
│   │   └── src
│   │       └── main
│   │           └── java
│   │               └── com
│   │                   └── alex
│   │                       └── api
│   │                           └── product
│   │                               ├── api
│   │                               │   └── pmsAttr
│   │                               └── vo
│   │                                   ├── pmsAttr
│   │                                   ├── pmsBrand
│   │                                   ├── pmsCategory
│   │                                   ├── pmsShopProduct
│   │                                   ├── pmsShopWantProduct
│   │                                   ├── pmsSkuInfo
│   │                                   └── product
│   │                                       └── jd
│   └── product_boot
│       └── src
│           └── main
│               ├── java
│               │   └── com
│               │       └── alex
│               │           └── product
│               │               ├── config
│               │               ├── controller
│               │               │   ├── pmsAttr
│               │               │   ├── pmsBrand
│               │               │   ├── pmsCategory
│               │               │   ├── pmsShopProduct
│               │               │   ├── pmsShopWantProduct
│               │               │   ├── pmsSkuInfo
│               │               │   └── shopProduct
│               │               ├── entity
│               │               │   ├── pmsAttr
│               │               │   ├── pmsBrand
│               │               │   ├── pmsCategory
│               │               │   ├── pmsShopProduct
│               │               │   ├── pmsShopWantProduct
│               │               │   └── pmsSkuInfo
│               │               ├── enums
│               │               ├── job
│               │               ├── mapper
│               │               │   ├── pmsAttr
│               │               │   ├── pmsBrand
│               │               │   ├── pmsCategory
│               │               │   ├── pmsShopProduct
│               │               │   ├── pmsShopWantProduct
│               │               │   └── pmsSkuInfo
│               │               └── service
│               │                   ├── pmsAttr
│               │                   │   └── impl
│               │                   ├── pmsBrand
│               │                   │   └── impl
│               │                   ├── pmsCategory
│               │                   │   └── impl
│               │                   ├── pmsShopProduct
│               │                   │   └── impl
│               │                   ├── pmsShopWantProduct
│               │                   │   └── impl
│               │                   ├── pmsSkuInfo
│               │                   │   └── impl
│               │                   └── shopProduct
│               │                       ├── impl
│               │                       └── jd
│               │                           └── impl
│               └── resources
├── alex_miaosha_user
│   ├── user_api
│   │   └── src
│   │       └── main
│   │           └── java
│   │               └── com
│   │                   └── alex
│   │                       └── api
│   │                           └── user
│   │                               ├── annotation
│   │                               ├── api
│   │                               │   ├── fallback
│   │                               │   ├── menuInfo
│   │                               │   ├── orgInfo
│   │                               │   ├── orgUserInfo
│   │                               │   ├── permissionInfo
│   │                               │   ├── roleInfo
│   │                               │   ├── rolePermissionInfo
│   │                               │   └── roleUserInfo
│   │                               ├── handler
│   │                               ├── user
│   │                               └── vo
│   │                                   ├── menuInfo
│   │                                   ├── orgInfo
│   │                                   ├── orgUserInfo
│   │                                   ├── permissionInfo
│   │                                   ├── roleInfo
│   │                                   ├── rolePermissionInfo
│   │                                   ├── roleUserInfo
│   │                                   ├── tUserLogin
│   │                                   └── user
│   └── user_boot
│       └── src
│           ├── main
│           │   ├── java
│           │   │   └── com
│           │   │       └── alex
│           │   │           └── user
│           │   │               ├── config
│           │   │               ├── controller
│           │   │               │   ├── menuInfo
│           │   │               │   ├── orgInfo
│           │   │               │   ├── orgUserInfo
│           │   │               │   ├── permissionInfo
│           │   │               │   ├── roleInfo
│           │   │               │   ├── rolePermissionInfo
│           │   │               │   ├── roleUserInfo
│           │   │               │   ├── tUserLogin
│           │   │               │   └── user
│           │   │               ├── entity
│           │   │               │   ├── menuInfo
│           │   │               │   ├── orgInfo
│           │   │               │   ├── orgUserInfo
│           │   │               │   ├── permissionInfo
│           │   │               │   ├── roleInfo
│           │   │               │   ├── rolePermissionInfo
│           │   │               │   ├── roleUserInfo
│           │   │               │   ├── tUserLogin
│           │   │               │   └── user
│           │   │               ├── mapper
│           │   │               │   ├── menuInfo
│           │   │               │   ├── orgInfo
│           │   │               │   ├── orgUserInfo
│           │   │               │   ├── permissionInfo
│           │   │               │   ├── roleInfo
│           │   │               │   ├── rolePermissionInfo
│           │   │               │   ├── roleUserInfo
│           │   │               │   ├── tUserLogin
│           │   │               │   └── user
│           │   │               ├── service
│           │   │               │   ├── menuInfo
│           │   │               │   │   └── impl
│           │   │               │   ├── online
│           │   │               │   ├── orgInfo
│           │   │               │   │   └── impl
│           │   │               │   ├── orgUserInfo
│           │   │               │   │   └── impl
│           │   │               │   ├── permissionInfo
│           │   │               │   │   └── impl
│           │   │               │   ├── roleInfo
│           │   │               │   │   └── impl
│           │   │               │   ├── rolePermissionInfo
│           │   │               │   │   └── impl
│           │   │               │   ├── roleUserInfo
│           │   │               │   │   └── impl
│           │   │               │   ├── security
│           │   │               │   ├── token
│           │   │               │   ├── tUserLogin
│           │   │               │   │   └── impl
│           │   │               │   └── user
│           │   │               │       └── impl
│           │   │               └── utils
│           │   │                   ├── jwt
│           │   │                   └── security
│           │   └── resources
│           │       └── city
│           └── test
│               └── java
├── alex_miaosha_utils
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── alex
│           │           └── utils
│           │               ├── aspact
│           │               ├── check
│           │               ├── enums
│           │               ├── handler
│           │               └── interceptor
│           └── resources
│               └── city
├── city
├── doc
│   ├── img
│   └── sql
└── logs
    ├── alex-finance-dev
    ├── alex-gateway-dev
    ├── alex-generator-dev
    ├── alex-oss-dev
    ├── alex-product-dev
    ├── alex-user-dev
    └── APP_NAME_IS_UNDEFINED
```

## 二、分层架构规范

| 层级        | 职责说明                         | 开发约束与注意事项                                               |
|-------------|----------------------------------|----------------------------------------------------------------|
| **Controller** | 处理 HTTP 请求与响应，定义 API 接口 | 不得直接访问数据库，必须通过 Service 层调用                  |
| **Service**    | 实现业务逻辑、事务管理与数据校验   | 必须通过 Repository 层访问数据库；返回 DTO 而非 Entity（除非必要） |
| **Repository** | 数据库访问与持久化操作             | 继承 `JpaRepository`；使用 `@EntityGraph` 避免 N+1 查询问题     |
| **Entity**     | 映射数据库表结构                   | 不得直接返回给前端（需转换为 DTO）；包名统一为 `entity`         |

### 接口与实现分离

- 所有接口实现类需放在接口所在包下的 `impl` 子包中。

## 三、安全与性能规范

### 输入校验

- 使用 `@Valid` 与 JSR-303 校验注解（如 `@NotBlank`, `@Size` 等）
  - 注意：Spring Boot 3.x 中校验注解位于 `jakarta.validation.constraints.*`

- 禁止手动拼接 SQL 字符串，防止 SQL 注入攻击。

### 事务管理

- `@Transactional` 注解仅用于 **Service 层**方法。
- 避免在循环中频繁提交事务，影响性能。

## 四、代码风格规范

### 命名规范

| 类型       | 命名方式             | 示例                  |
|------------|----------------------|-----------------------|
| 类名       | UpperCamelCase       | `UserServiceImpl`     |
| 方法/变量  | lowerCamelCase       | `saveUser()`          |
| 常量       | UPPER_SNAKE_CASE     | `MAX_LOGIN_ATTEMPTS`  |

### 注释规范

- 所有类、方法、字段需添加 **Javadoc** 注释。

### 类型命名规范（阿里巴巴风格）

| 后缀 | 用途说明                     | 示例         |
|------|------------------------------|--------------|
| DTO  | 数据传输对象                 | `UserDTO`    |
| DO   | 数据库实体对象               | `UserDO`     |
| BO   | 业务逻辑封装对象             | `UserBO`     |
| VO   | 视图展示对象                 | `UserVO`     |
| Query| 查询参数封装对象             | `UserQuery`  |

### 实体类简化工具

- 使用 Lombok 注解替代手动编写 getter/setter/构造方法：
  - `@Data`
  - `@NoArgsConstructor`
  - `@AllArgsConstructor`

## 五、扩展性与日志规范

### 接口优先原则

- 所有业务逻辑通过接口定义（如 `UserService`），具体实现放在 `impl` 包中（如 `UserServiceImpl`）。

### 日志记录

- 使用 `@Slf4j` 注解代替 `System.out.println`

## 六、编码原则总结

| 原则       | 说明                                       |
|------------|--------------------------------------------|
| **SOLID**  | 高内聚、低耦合，增强可维护性与可扩展性     |
| **DRY**    | 避免重复代码，提高复用性                   |
| **KISS**   | 保持代码简洁易懂                           |
| **YAGNI**  | 不实现当前不需要的功能                     |
| **OWASP**  | 防范常见安全漏洞，如 SQL 注入、XSS 等      |

## 七、通用依赖规则

1. **核心依赖**：
   - `spring-boot-starter-web`
   - `spring-boot-starter-data-jpa`
   - `lombok`
   - `mybatis-plus-boot-starter`
   - `spring-boot-starter-data-redis`
   - `spring-boot-starter-security`
   - `spring-cloud-starter-openfeign`
   - `spring-cloud-starter-gateway`

2. **数据库相关**：
   - MySQL 8.0.28
   - Druid 连接池
   - MyBatis Plus 3.5.2
   - ShardingSphere 5.2.1 (用于分库分表)

3. **缓存相关**：
   - Redis 2.7.0
   - 使用 Spring Data Redis

4. **API文档**：
   - Knife4j 3.0.3

5. **安全认证**：
   - JWT 认证 (java-jwt 和 jjwt)
   - Spring Security 5.7.3

6. **其他重要依赖**：
   - MinIO 8.5.1 (对象存储)
   - XXL-JOB 2.3.1 (分布式任务调度)
   - WeChat SDK 4.4.0 (微信相关)
   - Guava 30.1-jre
   - FastJSON 2.0.11
   - Jasypt 3.0.4 (加密)
   - IP2Region 2.6.6 (IP地址定位)
   - Prometheus 1.10.2 (监控指标)
   - SkyWalking 8.9.0 (链路追踪)
   - JustAuth 1.16.5 (第三方登录)

7. **构建工具**：
   - Maven 3.x
   - Java 17

8. **测试工具**：
   - JUnit 5
   - Mockito
   - TestNG

9. **日志系统**：
   - Logback
   - SkyWalking APM Toolkit

10. **部署和监控**：
    - Spring Boot Admin 2.6.8
    - Actuator
    - Micrometer + Prometheus
