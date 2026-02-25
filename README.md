# **Alex 权限管理系统**

<p>
  <img src="https://img.shields.io/badge/license-Apache--2.0-blue" alt="license"/>
  <img src="https://img.shields.io/badge/JDK-17%2B-green" alt="jdk"/>
  <a href="https://github.com/AlexhahahaDrag/alex_miaosha"><img src="https://img.shields.io/badge/SpringCloud-2021.0.3-green" alt="springcloud"/></a>
  <a href="https://github.com/AlexhahahaDrag/alex_miaosha"><img src="https://img.shields.io/badge/SpringBoot-2.7.2-green" alt="springboot"/></a>
  <img src="https://img.shields.io/badge/knife4j-3.0.3-green" alt="knife4j"/>
</p>

## 📖 前言

**Alex 管理系统** 是一个基于微服务架构的前后端分离系统，也是用于实践最新技术栈的开源学习项目。本项目在开发过程中结合了诸多前沿技术与主流中间件，旨在打造一个结构清晰、易于扩展的通用微服务后台底座。由于个人精力有限，部分功能仍在持续完善中，如有考虑不周或不妥当之处，欢迎各位大佬提 Issue 或 PR 帮忙指正。

<p align="center">
    <a href="https://github.com/AlexhahahaDrag/alex_miaosha">
        <img src="./doc/img/favicon.ico" alt="Alex Logo" style="width:200px;height:200px">
    </a>
</p>

## 🚀 项目介绍

本项目采用模块化的微服务设计进行代码组织：

- **前端页面**：采用 Vue 3 + Ant Design Vue 3 + TypeScript 进行构建。
- **后端服务**：基于 Spring Boot 2.7.x + Spring Cloud 2021.0.x 构建，搭配 MyBatis-Plus、Spring Security、Jasypt 加密、Knife4j 接口文档等组件进行开发。

## 🔗 项目地址

目前项目代码主要托管在 GitHub 和 Gitee 平台上，欢迎大家 **Star** 和 **Fork** 支持！

- 📦 **后端 GitHub 仓库**：[https://github.com/AlexhahahaDrag/alex_miaosha.git](https://github.com/AlexhahahaDrag/alex_miaosha)
- 📦 **后端 Gitee 仓库**：[https://gitee.com/AlexhahahaDrug/alex_miaosha_backend.git](https://gitee.com/AlexhahahaDrug/alex_miaosha_backend)
- 🎨 **前端 GitHub 仓库**：[https://github.com/AlexhahahaDrag/alex_miaosha_front](https://github.com/AlexhahahaDrag/alex_miaosha_front)

## 🧩 模块划分

本系统划分为以下核心业务与基础模块：

- **`alex_generator`**: 代码生成模块，支持一键生成前后端基础代码，极大提高开发效率。
- **`alex_miaosha_base`**: 基础核心模块，包含项目的基本通用配置与常量封装。
- **`alex_miaosha_common`**: 公共依赖模块，提供全局异常处理、工具类等。
- **`alex_miaosha_finance`**: 财务/账单管理模块，负责相关财务数据的统计与处理。
- **`alex_miaosha_gateway`**: 微服务网关模块，统一流量入口，负责路由转发、鉴权等。
- **`alex_miaosha_mission`**: 任务调度/业务模块。
- **`alex_miaosha_monitor`**: 监控中心模块，用于整合 Spring Boot Admin 及服务健康监控。
- **`alex_miaosha_order`**: 订单模块，负责业务订单的生成与流转控制。
- **`alex_miaosha_oss`**: 文件存储模块，集成对象存储服务，统一管理图片及附件。
- **`alex_miaosha_product`**: 产品管理模块，负责商品、分类、属性等信息的维护。
- **`alex_miaosha_user`**: 用户中心模块，负责用户管理、角色分配、权限校验、机构管理等配置。

## 🛠️ 项目使用与运行环境

核心基础设施依赖如下，建议在进行本地开发前先行启动以下环境及中间件服务：

- **Nacos** (注册中心与配置中心)
- **Redis** (分布式缓存)
- **RabbitMQ** (消息队列中间件)
- **Prometheus** (普罗米修斯监控)

本地微服务启动推荐顺序：
`Nacos` -> `alex_miaosha_gateway` -> `alex_miaosha_user` (用户实权系统) -> `alex_miaosha_monitor` -> 其它业务模块。

## 🌐 站点演示

👉 **演示站点**：[http://mjzp.xyz](http://mjzp.xyz) _(请注意环境是否在线)_

## 📚 技术选型

### 项目架构图

![系统架构设计.png](./doc/img/系统架构设计.png)
_(注：请确保设计图放置在相对系统路径内的 `./doc/img/系统架构设计.png` 中)_

### 后端技术栈

| 技术组件              | 说明                                 | 官方网站                                                      |
| :-------------------- | :----------------------------------- | :------------------------------------------------------------ |
| **Spring Cloud**      | 微服务架构核心框架                   | [Spring Cloud](https://spring.io/projects/spring-cloud)       |
| **Spring Boot**       | 基础应用开发脚手架                   | [Spring Boot](https://spring.io/projects/spring-boot)         |
| **Spring Security**   | 提供完善的认证和授权机制             | [Spring Security](https://spring.io/projects/spring-security) |
| **MyBatis-Plus**      | MyBatis 的增强工具，简化 CRUD        | [Baomidou MP](https://baomidou.com/)                          |
| **Knife4j**           | 基于 Swagger 的 Api 文档增强解决方案 | [Knife4j](https://doc.xiaominfo.com/)                         |
| **Spring Boot Admin** | 微服务节点状态监控                   | [SBA](https://github.com/codecentric/spring-boot-admin)       |
| **Nacos**             | 服务发现、配置管理的综合型解决方案   | [Nacos](https://nacos.io/)                                    |
| **Redis**             | 高性能分布式内存缓存                 | [Redis](https://redis.io/)                                    |
| **RabbitMQ**          | 可靠的企业级消息队列中间件           | [RabbitMQ](https://www.rabbitmq.com/)                         |
| **Prometheus**        | 普罗米修斯系统监控与预警             | [Prometheus](https://prometheus.io/)                          |
| **Arthas**            | Java 线上诊断工具                    | [Arthas](https://arthas.aliyun.com/)                          |

### 前端技术栈

| 技术组件           | 说明                     | 推荐版本 | 官方网站                                             |
| :----------------- | :----------------------- | :------- | :--------------------------------------------------- |
| **Vue**            | 渐进式 JavaScript 框架   | 3.2+     | [Vue.js](https://v3.cn.vuejs.org/)                   |
| **Ant Design Vue** | 企业级 UI 组件库         | v3.x     | [AntDV](https://www.antdv.com/docs/vue/introduce-cn) |
| **TypeScript**     | 强类型的 JavaScript 超集 | -        | [TypeScript](https://www.typescriptlang.org/)        |

## 💡 后续计划 (TODO)

- [ ] 整合并了解 **LVS**（Linux Virtual Server）在系统负载均衡中的实践，学习章文嵩博士主导的开源负载均衡项目。
- [ ] 补充完善相关的 Monitor 与 Common 底层封装代码机制。
- [ ] 增加更多高频业务场景（如：高并发秒杀优化）落地的最佳实践案例验证。

## 💖 结语

非常感谢开源社区诸多的优秀项目和思路指引！希望这个项目能为处于微服务转型探索中的同学们提供一定参考，欢迎大家交流共同进步！
