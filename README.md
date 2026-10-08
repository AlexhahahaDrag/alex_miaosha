# **Alex 管理系统 (Alex Authority Management System)**

<p align="center">
    <a href="https://github.com/AlexhahahaDrag/alex_miaosha">
        <img src="./doc/img/favicon.ico" alt="Alex Logo" width="120">
    </a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/License-Apache--2.0-blue.svg?style=for-the-badge" alt="license"/>
  <img src="https://img.shields.io/badge/JDK-17%2B-green.svg?style=for-the-badge" alt="jdk"/>
  <img src="https://img.shields.io/badge/SpringCloud-2021.0.3-green.svg?style=for-the-badge" alt="springcloud"/>
  <img src="https://img.shields.io/badge/SpringBoot-2.7.2-green.svg?style=for-the-badge" alt="springboot"/>
  <a href="https://github.com/AlexhahahaDrag/alex_miaosha/actions/workflows/codeql.yml">
    <img src="https://github.com/AlexhahahaDrag/alex_miaosha/actions/workflows/codeql.yml/badge.svg" alt="CodeQL Status"/>
  </a>
</p>

---

## 📖 前言

**Alex 管理系统** 是一套基于 **微服务架构** 的现代化前后端分离系统。它不仅是一个企业级的后台底座，更是一个整合了当下主流技术栈（Spring Cloud + Vue 3 + TypeScript）与前沿 **领域 AI 赋能** 的 **开源学习实战项目**。

本项目旨在探索分布式系统的最佳实践，涵盖高并发秒杀、RBAC+数据权限精细管控、多领域 AI 智能协同与自动化运维等全链路技术挑战。系统核心架构解耦清晰，具备极高的扩展性与工程参考价值。

---

## ✨ 核心特性

- 🛡️ **精细权限管控**：整合 Spring Security + MyBatis-Plus 数据权限拦截插件，实现基于 RBAC 的功能权限与机构数据权限双重隔离。
- 🤖 **领域 AI 深度赋能**：内置独立 AI 智能分析微服务，采用 **DeepSeek 大模型 + 规则引擎降级** 双路由，全面赋能记账解析、还礼测算、优惠券策划、商品文案润色与角色授权推荐。
- 📦 **微服务解耦设计**：清晰的微服务边界划分（网关、用户、商品、财务、对象存储、AI 分析），支持水平横向扩展与平滑伸缩。
- 🛠️ **自动化敏捷工具**：内置 `alex_generator`，一键生成高质量 CRUD 及其前后端工程代码，开发提速 80%。
- 📊 **全方位监控体系**：集成 Spring Boot Admin、Prometheus 及 Grafana，实时把控微服务健康与运行指标。
- 🔒 **全链路安全防护**：通过 Jasypt 对敏感配置加密，集成 JWT 鉴权与防刷限流，保障生产环境安全。

---

## 🏗️ 系统架构

### 架构示意图

![系统架构设计.png](./doc/img/系统架构设计.png)

> _注：系统架构涵盖由 Nacos 动态服务发现与配置中心到 Spring Cloud Gateway 流量染色路由，再到各业务微服务通信的全链路流程。_

---

## 📦 模块划分

| 模块名称 | 核心功能描述 | 对应服务名 / 默认端口 |
| :--- | :--- | :--- |
| **`alex_gateway`** | **统一网关入口**：路由分发、身份鉴权、跨域处理与流量染色 | `alex-gateway-dev` (8080) |
| **`alex_user`** | **用户权限中心**：RBAC 模型核心实现，机构、角色、菜单与按钮权限管控 | `alex-user-dev` (30006) |
| **`alex_product`** | **产品配置中心**：商品 SKU、类目、规格属性与秒杀库存管理 | `alex-product-dev` (30007) |
| **`alex_finance`** | **财务核算中心**：礼尚往来人情记账、优惠券管理、预付卡与账单流水 | `alex-finance-dev` (30008) |
| **`alex_oss`** | **对象存储服务**：统一封装 MinIO / 阿里云 OSS，纳管文件与图片静态资产 | `alex-oss-dev` (30009) |
| **`alex_ai`** | **智能分析中心**：DeepSeek 大模型与规则引擎双路由，多业务领域智能分析 | `alex-ai-dev` (30010) |
| **`alex_generator`** | **敏捷开发工具**：基于模板引擎的代码生成器，自动化产出标准化代码 | 本地工程工具 |
| **`alex_common`** | **统一公共底座**：分层架构（`common_api` 轻量契约 + `common_core` 核心运行时） | 通用依赖库 |

> _注：微服务接口文档已全面由统一网关 `alex_gateway`（基于 `SwaggerResourceConfig`）动态聚合各子服务文档并接入 Apifox，历史独立的 `alex_miaosha_api_doc` 聚合微服务已废弃下线。_

---

## 🤖 领域 AI 赋能矩阵 (Domain AI)

系统在 `alex_ai` 模块中实现了通用的 AI 分析中枢，通过轻量级双路由设计（优先调用大模型，在无网络或超时时平滑降级至专家规则引擎）：

1. **礼金智能文本解析 (Gift Record Parsing)**：
   - 支持自然语言一键录入（如“上周六参加张三婚礼随礼800元现金”），智能抽取收/送礼人、事由类型、金额、日期、资金流向等核心字段并返回结构化数据。
2. **智能还礼金额测算 (Smart Gift Recommend)**：
   - 综合历史往来记录、事由等级与通胀预期，提供 4 档动态建议金额（0.8 / 1.0 / 1.5 / 2.0 倍率），并结合百元/十元整数进位规则输出合理解释。
3. **优惠券智能营销策划 (Coupon Planning AI)**：
   - 根据活动目标、客单价与预算约束，自动化生成科学的满减/折扣券面额方案与风控阈值。
4. **商品营销文案生成 (Product Copywriting AI)**：
   - 提取商品 SKU 属性、核心卖点标签，一键产出爆款秒杀标题、宣传文案与推荐理由。
5. **角色权限智能匹配 (Role Recommendation AI)**：
   - 输入岗位职责与管理层级描述，智能推荐对应的菜单路由与操作按钮权限清单，辅助安全合规配置。

---

## 🛠️ 技术选型

### 后端核心

| 技术 | 版本 | 选型原因 |
| :--- | :--- | :--- |
| **SpringBoot** | `2.7.2` | 生态成熟稳定，企业级微服务标准框架 |
| **SpringCloud** | `2021.0.3` | 统一的服务治理、Feign 声明式调用与网关标准 |
| **MyBatis-Plus** | `3.5.x` | 极简 CRUD，支持 Lambda 类型安全查询与多租户/数据权限插件 |
| **Nacos** | `2.x` | 动态服务注册中心与统一分布式配置中心 |
| **Redis & Redisson** | `6.x` | 高性能缓存、分布式锁与并发防重令牌 |
| **RabbitMQ** | `3.x` | 异步削峰解耦、秒杀订单延时关单 |
| **Knife4j / OpenAPI** | `3.x` | 交互式 API 契约文档，网关自动聚合各服务 |
| **DeepSeek API** | - | 行业前沿大语言模型底座，结合规则引擎兜底 |

### 前端与移动端

| 平台 | 核心技术栈 | 亮点特性 |
| :--- | :--- | :--- |
| **PC 管理端** (`alex_miaosha_front`) | Vue 3 + Vite + TypeScript + Ant Design Vue 4 + Tailwind CSS | 暗黑模式、AI 交互弹窗、ECharts 6 图表、动态路由、Midscene 冒烟测试 |
| **移动端** (`alex_miaosha_mobile`) | Vue 3.5 + Vite 8 + Vant 4 + Pinia + pnpm | 移动优先交互、AI 语音/文本记账、触觉反馈（Haptic）、夜间暗黑主题 |

---

## 🚀 快速开始

### 1. 环境准备

确保您的本地或测试环境已具备以下基础服务：

- [x] **JDK 17+**
- [x] **Maven 3.8+**
- [x] **Nacos 2.x** (建议配置单机或集群，开启 Discovery & Config)
- [x] **Redis 6.x+**
- [x] **MySQL 8.x**

### 2. 启动步骤

1. **数据库初始化**：执行项目 `doc/sql/` 目录下的 schema 及权限数据脚本。
2. **配置 Nacos**：将项目 `nacos/` 配置文件导入到 Nacos 配置中心，检查 MySQL/Redis 连接信息。
3. **微服务顺序启动**：
   - ① `alex_miaosha_gateway` (统一网关，默认端口 8080)
   - ② `alex_miaosha_user` (用户与 RBAC 权限中心)
   - ③ `alex_miaosha_ai` (AI 智能分析中心)
   - ④ 其他业务微服务（`alex_miaosha_finance`、`alex_miaosha_product`、`alex_miaosha_oss`）

---

## 🎁 礼尚往来管理模块

后端在 `alex_miaosha_finance/finance_boot` 中深度扩展礼尚往来业务，复用 RBAC 权限体系与数据隔离底座：

- **核心子领域划分**：亲友档案（`person`）、事由配置（`eventoption`）、往来事件（`event`）、礼金收支记录（`record`）以及统计分析（`analysis`）。
- **底层数据模型**：
  - `gift_person_info_t`：亲友档案，含系统关系字典与多维标签。
  - `gift_event_type_option_t` / `gift_event_type_user_config_t`：系统内置与组织自定义的事由类型与推荐默认礼金。
  - `gift_event_info_t`：办酒/喜宴/大事记等往来事件。
  - `gift_record_info_t`：收礼、送礼与回礼记录，通过 `direction = GIVE | RECEIVE | RETURN` 及 `related_record_id` 形成完整闭环链路。
- **数据权限隔离**：所有业务操作均绑定 `org_id`，基于 `@DataPermission` 实现组织级与用户级多维过滤。
- **验证与测试命令**：
  ```bash
  # 运行礼尚往来业务规则与归属隔离验证测试
  mvn clean -pl alex_miaosha_finance/finance_boot -am "-Dtest=GiftRecordBusinessRuleTest,GiftOwnershipTest,GiftStructureTest" -DfailIfNoTests=false test
  
  # 运行 AI 领域扩展与智能测算测试
  mvn clean -pl alex_miaosha_ai -am "-Dtest=GiftAiServiceTest,DomainAiExtensionTest" -DfailIfNoTests=false test
  ```

---

## 🔗 项目矩阵

- 📦 **后端仓库 (GitHub)**：[AlexhahahaDrag/alex_miaosha](https://github.com/AlexhahahaDrag/alex_miaosha)
- 📦 **后端镜像 (Gitee)**：[AlexhahahaDrug/alex_miaosha_backend](https://gitee.com/AlexhahahaDrug/alex_miaosha_backend)
- 🎨 **PC 前端仓库**：[AlexhahahaDrag/alex_miaosha_front](https://github.com/AlexhahahaDrag/alex_miaosha_front)
- 📱 **移动端仓库**：[AlexhahahaDrag/alex_miaosha_mobile](https://github.com/AlexhahahaDrag/alex_miaosha_mobile)

---

## 🗺️ 后续路线图 (Roadmap)

- [ ] **分布式事务深度整合**：引入 Seata AT/TCC 模式强化跨微服务资金与库存一致性。
- [ ] **容器化与云原生配置**：提供标准化 Docker Compose 及 Kubernetes Helm Charts 部署方案。
- [ ] **AI 智能客服与意图识别**：扩展大模型函数调用（Function Calling），支持自然语言查账与多轮交互。
- [ ] **全链路压测与稳定性工程**：模拟高并发秒杀场景的流量压测与断路熔断指标评测。

---

## 💖 结语

感谢开源社区提供的诸多优秀项目与思路。如果你觉得本项目对你有帮助，欢迎点亮 **Star** 支持！
如有任何问题或改进建议，欢迎提交 **Issue** 或 **Pull Request**。
