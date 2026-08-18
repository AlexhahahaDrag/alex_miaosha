# Alex Miaosha 系统架构设计

> 基于原有架构设计图，结合当前代码实际模块梳理而成。可维护、可版本化。

---

## 1. 系统整体架构

```mermaid
graph TB
    %% ========== 客户端层 ==========
    subgraph Client ["客户端层"]
        PC_Admin["系统后台 PC"]
        PC_Owner["货主后台 PC"]
        Mobile_Owner["货主移动端"]
        Mobile_Driver["司机移动端"]
    end

    %% ========== 负载均衡层 ==========
    subgraph LB ["负载均衡"]
        Nginx["Nginx + Keepalived"]
    end

    %% ========== 网关层 ==========
    subgraph GW ["网关"]
        direction LR
        GW_Func["黑白名单 · 身份认证 · 限流"]
        Gateway["Gateway 网关"]
    end

    %% ========== 业务服务层 ==========
    subgraph Biz ["业务服务"]
        direction LR
        subgraph UserCluster ["用户服务集群"]
            User1["用户服务"]
            User2["用户服务"]
            User3["用户服务"]
        end
        subgraph SeckillCluster ["秒杀系统服务集群"]
            Seckill1["秒杀系统服务"]
            Seckill2["秒杀系统服务"]
            Seckill3["秒杀系统服务"]
        end
        UserCluster -- "Feign" --- SeckillCluster
    end

    subgraph BizExt ["业务扩展服务"]
        direction LR
        Finance["财务服务 (Finance)"]
        Product["商品服务 (Product)"]
        Order["订单服务 (Order)"]
        OSS_Svc["文件服务 (OSS)"]
        AI_Svc["AI 服务 (Agent)"]
        Mission["任务服务 (Mission)"]
    end

    subgraph Unified ["统一管理"]
        direction LR
        Scheduler["统一调度管理"]
        Permission["统一权限管理"]
        LogMgr["统一日志管理"]
    end

    %% ========== 监控&保护 ==========
    subgraph Monitor ["监控 & 保护"]
        SkyWalking["SkyWalking 监控"]
        Sleuth["Sleuth 服务链路追踪"]
        Sentinel["Alibaba Sentinel 熔断降级"]
        ELK["ELK 日志管理"]
    end

    %% ========== 基础模块 ==========
    subgraph Base ["基础模块 (Common / Base / Utils)"]
        direction LR
        MultiDS["多数据源"]
        Auth["认证"]
        AuthZ["授权"]
        DistCache["分布式缓存"]
        Schedule["调度"]
        Logging["日志"]
        RedisUtil["Redis"]
        Toolkit["工具包"]
        DistLock["分布式锁"]
        Idempotent["幂等性"]
        MQBase["MQ"]
        OSSBase["OSS"]
        DistTx["分布式事务"]
        Dispatch["分发"]
    end

    %% ========== 支撑服务 ==========
    subgraph Support ["支撑服务"]
        direction LR
        Nacos["Nacos 服务注册发现集群"]
        NacosConfig["分布式配置中心集群"]
        SkyWalkingS["SkyWalking 监控"]
        Seata["Seata 分布式服务"]
        DispatchSvc["分发服务"]
        MsgCenter["消息中心"]
    end

    %% ========== 数据库层 ==========
    subgraph DB ["数据库层"]
        direction LR
        MySQL["MySQL 集群"]
        RedisCluster["Redis 集群"]
        OSSStorage["OSS 存储 (Minio)"]
        RabbitMQ["RabbitMQ 集群"]
        ES["ES 集群"]
    end

    %% ========== 发版部署 ==========
    subgraph Deploy ["发版部署"]
        Docker["Docker + K8s"]
        DynScale["动态扩/缩容"]
        AppDeploy["应用发布"]
        DeployMonitor["监控"]
        ImageMgr["镜像治理"]
    end

    %% ========== 连线 ==========
    Client --> LB
    LB --> GW
    GW --> Biz
    GW --> BizExt
    Biz --> Base
    BizExt --> Base
    Biz --> Support
    BizExt --> Support
    Base --> DB
    Support --> DB
    Monitor -.- Biz
    Monitor -.- BizExt
    Deploy -.- Biz
    Deploy -.- BizExt
```

---

## 2. 核心技术栈

| 维度 | 技术选型 | 版本 |
| :--- | :--- | :--- |
| **核心框架** | Spring Boot / Spring Cloud / Java | 2.7.2 / 2021.0.3 / 17 |
| **微服务治理** | Nacos (注册 + 配置), OpenFeign, LoadBalancer | 2021.1 / 3.1.3 |
| **网关** | Spring Cloud Gateway | 3.1.3 |
| **持久层** | MyBatis Plus, Druid, ShardingSphere | 3.5.2 / 1.2.15 / 5.2.1 |
| **缓存** | Redis (Spring Data Redis) | 2.7.0 |
| **消息队列** | RabbitMQ (Spring AMQP) | 2.7.0 |
| **安全** | Spring Security, JWT (Auth0 + JJWT), 国密 SM (BouncyCastle) | 5.7.3 / 4.0.0 / 1.78 |
| **对象存储** | Minio | 8.5.1 |
| **监控链路** | SkyWalking (APM Toolkit Logback), Prometheus, Actuator, SBA | 8.9.0 / 1.10.2 |
| **任务调度** | XXL-JOB | 2.3.1 |
| **API 文档** | Knife4j (Swagger) | 3.0.3 |
| **工具库** | Lombok, Guava, Gson, FastJSON, EasyPOI, Jasypt | - |
| **三方集成** | 微信 (weixin-java-mp), ip2region | 4.4.0 / 2.6.6 |
| **CI/CD** | Drone CI, Docker | - |

---

## 3. 模块职责矩阵

### 3.1 业务服务模块

| 模块 | 目录 | 子模块 | 端口 | 核心职责 |
| :--- | :--- | :--- | :--- | :--- |
| **网关** | `alex_miaosha_gateway` | - | 30001 | 统一入口、白名单校验、身份认证、响应加密、限流(预留)、跨域、Swagger 聚合 |
| **用户** | `alex_miaosha_user` | `user_api` / `user_boot` | 30006 | 用户管理、RBAC 权限、登录认证、Token 刷新、在线用户管理 |
| **商品** | `alex_miaosha_product` | `product_api` / `product_boot` | 30007 | 商品管理、秒杀库存预热、库存扣减 |
| **财务** | `alex_miaosha_finance` | `finance_api` / `finance_boot` | 30008 | 财务流水、账目管理 |
| **OSS** | `alex_miaosha_oss` | `oss_api` / `oss_boot` | 30009 | 文件上传/下载、Minio 对象存储 |
| **订单** | `alex_miaosha_order` | `order_api` / `order_boot` | - | 秒杀订单、排队、支付状态 |
| **AI** | `alex_miaosha_ai` | `ai_api` / `ai_boot` | - | 统一 AI 能力封装 (大模型集成) |
| **任务** | `alex_miaosha_mission` | - | - | 任务/活动管理 |
| **监控** | `alex_miaosha_monitor` | - | 30099 | Spring Boot Admin、服务健康检查 |

### 3.2 基础支撑模块

| 模块 | 目录 | 核心能力 |
| :--- | :--- | :--- |
| **Common** | `alex_miaosha_common` | Redis 工具、加解密(AES/SM)、统一异常处理、Feign 配置、序列化、Redis Key 定义、秒杀消息体 |
| **Base** | `alex_miaosha_base` | 通用 Result 封装、基础实体/VO |
| **Utils** | `alex_miaosha_utils` | 字符串、日期、Bean 工具类 |
| **API Doc** | `alex_miaosha_api_doc` | Knife4j 文档聚合配置 |
| **Generator** | `alex_generator` | MyBatis Plus 代码生成器 |

---

## 4. 网关核心逻辑

```mermaid
flowchart TD
    A["请求进入 Gateway"] --> B{"匹配文档白名单?"}
    B -- "是" --> C["直接放行"]
    B -- "否" --> D{"匹配接口白名单?"}
    D -- "是" --> E["放行 + 响应加密"]
    D -- "否" --> F["提取 Token"]
    F --> G["Feign 调用 UserApi.authToken 验证"]
    G --> H{"认证通过?"}
    H -- "是" --> E
    H -- "否" --> I["返回 403 (加密)"]
    E --> J{"响应为文件?"}
    J -- "是" --> K["直接返回原始数据"]
    J -- "否" --> L["响应体 AES 加密后返回"]
```

---

## 5. CI/CD 流水线 (Drone)

```mermaid
flowchart LR
    A["代码推送 master"] --> B["Maven 构建打包"]
    B --> C["拷贝 JAR + Dockerfile"]
    C --> D["SSH 远程部署"]
    D --> D1["Monitor :30099"]
    D --> D2["Gateway :30001"]
    D --> D3["Finance :30008"]
    D --> D4["User :30006"]
    D --> D5["OSS :30009"]
    D --> D6["Product :30007"]
    D1 & D2 & D3 & D4 & D5 & D6 --> E["统一日志挂载"]
    E --> F["邮件通知"]
```

**日志统一挂载策略**：

所有微服务容器的 `/logs` 目录统一映射到宿主机：

```
宿主机: /usr/local/soft/alex_miaosha/drone/alex_miaosha/logs/
├── alex-user-prod/          # User 服务日志
│   ├── alex-user-prod-info.log
│   └── alex-user-prod-error.log
├── alex-gateway-prod/       # Gateway 服务日志
├── alex-finance-prod/       # Finance 服务日志
├── alex-monitor-prod/       # Monitor 服务日志
├── alex-oss-prod/           # OSS 服务日志
└── alex-product-prod/       # Product 服务日志
```

---

## 6. 数据架构

```mermaid
flowchart TB
    subgraph Services ["业务服务"]
        User["用户服务"]
        Product["商品服务"]
        Order["订单服务"]
        Finance["财务服务"]
    end

    subgraph Cache ["缓存层"]
        Redis["Redis 集群"]
    end

    subgraph MQ_Layer ["消息队列"]
        RabbitMQ["RabbitMQ 集群"]
    end

    subgraph Storage ["存储层"]
        MySQL_User["alex_user (MySQL)"]
        MySQL_Product["alex_product (MySQL)"]
        MySQL_Order["alex_order (MySQL)"]
        MySQL_Finance["alex_finance (MySQL)"]
        Minio["Minio 对象存储"]
    end

    User --> Redis
    User --> MySQL_User
    Product --> Redis
    Product --> MySQL_Product
    Order --> Redis
    Order --> RabbitMQ
    Order --> MySQL_Order
    Finance --> MySQL_Finance
    RabbitMQ --> Product
```

---

*文档更新日期：2026-05-07 · 基于项目实际代码与原架构设计图*

---

## 7. 礼尚往来管理架构补充

礼尚往来管理属于 `alex_miaosha_finance` 业务域，运行在 `finance_boot` 服务内，继续复用现有网关鉴权、用户体系、组织体系、RBAC 权限体系、MyBatis Plus 与统一返回结构。

### 7.1 模块分层

```text
Controller -> Service -> Mapper -> MySQL
DTO / Query / VO 作为接口边界
Entity 仅用于持久化层，统一 extends BaseEntity<T>
```

Controller 禁止直接返回 Entity；业务接口均使用 DTO、Query、VO 分层承载参数和响应。Service 层使用 `LambdaQueryWrapper`、`LambdaUpdateWrapper`、`Page`、`IService`、`ServiceImpl`，避免字符串字段 SQL 和大量 XML。

### 7.2 数据模型

```mermaid
flowchart LR
    Org["org_id 数据隔离"] --> Person["gift_person_info_t 亲友（含 relation_type 关系）"]
    Person --> Event["gift_event_info_t 事由"]
    Event --> Record["gift_record_info_t 礼金记录"]
    Record --> Return["direction=RETURN 回礼记录"]
```

`gift_record_info_t` 同时承载随礼、收礼、回礼三类流水。回礼通过 `direction = RETURN`、`related_record_id` 与原收礼记录建立关联，通过 `returned_flag` 标记原记录是否已回礼。这样页面和统计口径保持一张流水表，避免回礼记录与礼金记录在查询、权限、导出上的重复实现。

### 7.3 权限与隔离

- 菜单权限：`gift:dashboard`、`gift:person`、`gift:event`、`gift:record`、`gift:return`、`gift:analysis`。
- 按钮权限：`gift:view`、`gift:add`、`gift:edit`、`gift:delete`、`gift:export`。
- 数据权限：通过 `org_id` 隔离个人账本、家庭账本、企业账本；需要用户级边界时使用 `user_id`。
- 超级管理员：沿用现有前端路由与后端权限逻辑，不依赖业务角色授权。

### 7.4 性能设计

礼金记录按 10w+ 数据量设计，列表接口使用分页查询和组合索引，统计接口按组织、时间、方向聚合。高频统计可接入 Redis 缓存，缓存键必须包含 `org_id` 与统计维度，避免跨组织污染。
