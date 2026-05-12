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

**Alex 管理系统** 是一套基于 **微服务架构** 的现代化前后端分离系统。它不仅是一个企业级的后台底座，更是一个整合了当下主流技术栈（Spring Cloud + Vue 3 + TS）的 **开源学习实战项目**。

本项目旨在探索分布式系统的最佳实践，涵盖了高并发、权限管控、自动化运维等多个维度的技术挑战。尽管部分功能仍在迭代中，但其核心架构设计严谨，具备极高的扩展性与参考价值。

---

## ✨ 核心特性

- 🛡️ **精细权限管控**：整合 Spring Security，实现基于 RBAC 模型的功能权限与数据权限双重校验。
- ⚡ **高性能秒杀优化**：专门的秒杀业务模块，整合 Redis 预热、RabbitMQ 削峰填谷等高并发优化手段。
- 📦 **模块化解耦设计**：清晰的微服务边界划分，支持水平横向扩展。
- 🛠️ **自动化代码生成**：内置 `alex_generator`，一键生成 CRUD 及其前后端代码，开发提速 80%。
- 📊 **全方位监控体系**：集成 Spring Boot Admin、Prometheus 及 Grafana，实时掌控服务状态。
- 🔒 **数据安全保障**：使用 Jasypt 对敏感配置文件进行加密，确保线上生产环境安全。

---

## 🏗️ 系统架构

### 架构示意图

![系统架构设计.png](./doc/img/系统架构设计.png)

> _注：系统架构涵盖了由 Nacos 发现服务到网关转发，再到业务微服务的全链路流程。_

---

## 📦 模块划分

| 模块名称             | 核心功能描述                                                     |
| :------------------- | :--------------------------------------------------------------- |
| **`alex_gateway`**   | **统一网关入口**：路由转发、身份鉴权、流量染色。                 |
| **`alex_user`**      | **用户权限中心**：核心 RBAC 权限实现，包括机构、角色、资源管理。 |
| **`alex_product`**   | **产品配置中心**：维护商品 SKU、类目及属性体系。                 |
| **`alex_order`**     | **订单处理流水**：负责分布式环境下的订单生成与状态机流转。       |
| **`alex_miaosha`**   | **高并发秒杀**：专注于秒杀场景下的性能压测与优化实践。           |
| **`alex_finance`**   | **财务核算中心**：账单流水、流水统计及对账逻辑。                 |
| **`alex_monitor`**   | **运维监控中心**：节点状态感知与实时报警监控。                   |
| **`alex_oss`**       | **对象存储服务**：统一封装 MinIO/阿里云 OSS，管理静态资源。      |
| **`alex_generator`** | **敏捷开发工具**：基于模板引擎，实现代码自动化产出。             |
| **`alex_common`**    | **全局公共组件**：通用的异常处理、日志切面、VO/DTO 定义。        |

---

## 🛠️ 技术选型

### 后端核心

| 技术                 | 选型原因                                        |
| :------------------- | :---------------------------------------------- |
| **SpringBoot 2.7**   | 生态完善，生产级的 Java 应用框架。              |
| **SpringCloud 2021** | 统一的服务治理与微服务通信标准的选型。          |
| **MyBatis-Plus**     | 极简持久层开发，支持 Lambda 表达式。            |
| **Nacos**            | 同时具备服务注册与分布式配置管理能力。          |
| **Redis & RabbitMQ** | 缓存性能与异步流量削峰的核心基石。              |
| **Knife4j**          | 基于 Swagger 的交互式文档，极致的后端调试体验。 |

### 前端核心

| 技术                        | 选型原因                               |
| :-------------------------- | :------------------------------------- |
| **Vue 3 (Composition API)** | 现代化响应式框架，极致的代码组织体验。 |
| **TypeScript**              | 增强代码健壮性与类型自描述性。         |
| **Ant Design Vue v3**       | 企业级 UI 交互标准，组件丰富。         |

---

## 🚀 快速开始

### 1. 环境准备

确保您的机器已安装并启动以下服务：

- [x] **JDK 17+**
- [x] **Maven 3.8+**
- [x] **Nacos 2.x** (建议开启 Discovery & Config)
- [x] **Redis 6.x**
- [x] **MySQL 8.x**

### 2. 启动步骤

1.  **数据库初始化**：执行项目 `script/sql` 目录下的 SQL 脚本。
2.  **配置 Nacos**：将项目中的配置文件上传或配置到 Nacos 中心。
3.  **服务顺序启动**：
    - `alex_miaosha_gateway` (端口: 8080)
    - `alex_miaosha_user` (中心权限服务)
    - 其他业务模块（如 `product`, `order` 等）

---

## 🔗 项目地址

- 📦 **GitHub (Main)**：[AlexhahahaDrag/alex_miaosha](https://github.com/AlexhahahaDrag/alex_miaosha)
- 📦 **Gitee (Mirror)**：[AlexhahahaDrug/alex_miaosha_backend](https://gitee.com/AlexhahahaDrug/alex_miaosha_backend)
- 🎨 **Frontend Repo**：[alex_miaosha_front](https://github.com/AlexhahahaDrag/alex_miaosha_front)
- 📱 **Mobile Repo**：[alex_miaosha_mobile](https://github.com/AlexhahahaDrag/alex_miaosha_mobile)

---

## 🗺️ 后续计划 (Roadmap)

- [ ] **分布式事务集成**：引入 Seata 解决跨服务的事务一致性问题。
- [ ] **容器化部署优化**：提供完整的 Docker-Compose 与 K8s 部署脚本。
- [ ] **监控大屏**：基于 Grafana 打造业务维度的实时监控仪表盘。
- [ ] **LVS 方案探索**：学习并集成 LVS 实现网络层的高可用均衡。

---

## 💖 结语

感谢开源社区提供的诸多优秀思路。如果你觉得这个项目对你有帮助，欢迎 **Star** 关注！
如有任何建议，请随时提交 **Issue** 或 **PR**。

---

## 礼尚往来管理模块

后端已在 `alex_miaosha_finance/finance_boot` 中扩展礼尚往来业务，复用现有用户、组织、RBAC、网关与通用返回体系，不重复建设基础能力。

- 业务边界：亲友档案、关系维护、事由管理、礼金记录、回礼标记、待回礼金额与统计分析。
- 数据表：`gift_person_info_t`、`gift_relation_info_t`、`gift_event_info_t`、`gift_record_info_t`。
- 回礼设计：不单独创建 `gift_return_record_info_t`，统一由 `gift_record_info_t.direction = GIVE | RECEIVE | RETURN` 和 `related_record_id` 表达回礼链路。
- 数据隔离：所有业务表必须带 `org_id`，接口查询、详情、编辑、删除均按 `org_id` 与 `user_id` 权限上下文过滤。
- 权限脚本：`doc/sql/gift_management_permission.sql` 提供菜单与按钮权限初始化，超级管理员按现有前端路由逻辑无需额外授权。
- 表结构脚本：`doc/sql/gift_management_schema.sql` 提供表、索引与字段注释，重点索引覆盖 `org_id`、`event_id`、`giver_person_id`、`receiver_person_id`、`pay_time`、`direction`。
- 验证命令：`mvn clean -pl alex_miaosha_finance/finance_boot -am "-Dtest=GiftRecordBusinessRuleTest,GiftOwnershipTest,GiftStructureTest" -DfailIfNoTests=false test`。
