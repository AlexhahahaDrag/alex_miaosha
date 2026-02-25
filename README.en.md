# **Alex Permission Management System**

<p>
  <img src="https://img.shields.io/badge/license-Apache--2.0-blue" alt="license"/>
  <img src="https://img.shields.io/badge/JDK-17%2B-green" alt="jdk"/>
  <a href="https://github.com/AlexhahahaDrag/alex_miaosha"><img src="https://img.shields.io/badge/SpringCloud-2021.0.3-green" alt="springcloud"/></a>
  <a href="https://github.com/AlexhahahaDrag/alex_miaosha"><img src="https://img.shields.io/badge/SpringBoot-2.7.2-green" alt="springboot"/></a>
  <img src="https://img.shields.io/badge/knife4j-3.0.3-green" alt="knife4j"/>
</p>

## 📖 Preface

**Alex Management System** is an open-source learning project based on microservice architecture and a frontend-backend separation design. It serves as a practical playground for applying the latest technology stacks. The project integrates various cutting-edge technologies and mainstream middleware, aiming to build a clear, well-structured, and easily extensible general-purpose microservice backbone. Due to limited time and energy, some features are still continuously being improved. If there are oversight or inappropriate implementations, suggestions, Issues, or Pull Requests from the community are highly welcome and appreciated.

<p align="center">
    <a href="https://github.com/AlexhahahaDrag/alex_miaosha">
        <img src="./doc/img/favicon.ico" alt="Alex Logo" style="width:200px;height:200px">
    </a>
</p>

## 🚀 Project Introduction

This project organizes code using a modular microservice design:

- **Frontend Pages**: Built with Vue 3 + Ant Design Vue 3 + TypeScript.
- **Backend Services**: Built using Spring Boot 2.7.x + Spring Cloud 2021.0.x, integrated with components like MyBatis-Plus, Spring Security, Jasypt for encryption, and Knife4j for API documentation.

## 🔗 Project Links

The project code is currently hosted on GitHub and Gitee. Welcome to **Star** and **Fork** to support!

- 📦 **Backend GitHub Repository**: [https://github.com/AlexhahahaDrag/alex_miaosha.git](https://github.com/AlexhahahaDrag/alex_miaosha)
- 📦 **Backend Gitee Repository**: [https://gitee.com/AlexhahahaDrug/alex_miaosha_backend.git](https://gitee.com/AlexhahahaDrug/alex_miaosha_backend)
- 🎨 **Frontend GitHub Repository**: [https://github.com/AlexhahahaDrag/alex_miaosha_front](https://github.com/AlexhahahaDrag/alex_miaosha_front)

## 🧩 Module Breakdown

The system is divided into the following core business and basic modules:

- **`alex_generator`**: Code generation module, supports one-click generation of basic frontend and backend code, greatly improving development efficiency.
- **`alex_miaosha_base`**: Core infrastructure module, containing basic common configurations and encapsulated constants.
- **`alex_miaosha_common`**: Public dependency module, providing global exception handling, utilities, etc.
- **`alex_miaosha_finance`**: Finance/Billing management module, responsible for related financial data statistics and processing.
- **`alex_miaosha_gateway`**: Microservice API Gateway module, unifying traffic entry, responsible for routing, forwarding, authentication, etc.
- **`alex_miaosha_mission`**: Task scheduling / Business module.
- **`alex_miaosha_monitor`**: Monitoring center module, integrating Spring Boot Admin and service health monitoring.
- **`alex_miaosha_order`**: Order module, responsible for generating and controlling the flow of business orders.
- **`alex_miaosha_oss`**: File storage module, integrating object storage services to centrally manage images and attachments.
- **`alex_miaosha_product`**: Product management module, maintaining information related to goods, categories, attributes, etc.
- **`alex_miaosha_user`**: User center module, handling user management, role assignment, permission validation, organization management, etc.

## 🛠️ Usage & Runtime Environment

The core infrastructure dependencies are as follows. It is recommended to start the following environment and middleware services before engaging in local development:

- **Nacos** (Service Registry and Configuration Center)
- **Redis** (Distributed Cache)
- **RabbitMQ** (Message Broker)
- **Prometheus** (System Monitoring)

Recommended startup sequence for local microservices:
`Nacos` -> `alex_miaosha_gateway` -> `alex_miaosha_user` (User Auth System) -> `alex_miaosha_monitor` -> Other business modules.

## 🌐 Live Demo

👉 **Demo Site**: [http://mjzp.xyz](http://mjzp.xyz) _(Please check if the environment is online)_

## 📚 Technology Stack

### System Architecture Diagram

![System Architecture Design.png](./doc/img/系统架构设计.png)
_(Note: Please ensure the original architecture diagram is placed in the relative path `./doc/img/系统架构设计.png`)_

### Backend Technology Stack

| Technology Component  | Description                                                               | Official Website                                              |
| :-------------------- | :------------------------------------------------------------------------ | :------------------------------------------------------------ |
| **Spring Cloud**      | Core framework for microservice architecture                              | [Spring Cloud](https://spring.io/projects/spring-cloud)       |
| **Spring Boot**       | Scaffolding for basic application development                             | [Spring Boot](https://spring.io/projects/spring-boot)         |
| **Spring Security**   | Provides comprehensive authentication and authorization mechanisms        | [Spring Security](https://spring.io/projects/spring-security) |
| **MyBatis-Plus**      | An enhanced toolkit for MyBatis, simplifying CRUD operations              | [Baomidou MP](https://baomidou.com/)                          |
| **Knife4j**           | Swagger-based API documentation enhancement solution                      | [Knife4j](https://doc.xiaominfo.com/)                         |
| **Spring Boot Admin** | Microservice node status monitoring                                       | [SBA](https://github.com/codecentric/spring-boot-admin)       |
| **Nacos**             | Comprehensive solution for service discovery and configuration management | [Nacos](https://nacos.io/)                                    |
| **Redis**             | High-performance distributed in-memory cache                              | [Redis](https://redis.io/)                                    |
| **RabbitMQ**          | Reliable enterprise-grade message queue middleware                        | [RabbitMQ](https://www.rabbitmq.com/)                         |
| **Prometheus**        | System monitoring and alerting                                            | [Prometheus](https://prometheus.io/)                          |
| **Arthas**            | Java online diagnostic tool                                               | [Arthas](https://arthas.aliyun.com/)                          |

### Frontend Technology Stack

| Technology Component | Description                           | Version | Official Website                                     |
| :------------------- | :------------------------------------ | :------ | :--------------------------------------------------- |
| **Vue**              | Progressive JavaScript framework      | 3.2+    | [Vue.js](https://v3.cn.vuejs.org/)                   |
| **Ant Design Vue**   | Enterprise-class UI component library | v3.x    | [AntDV](https://www.antdv.com/docs/vue/introduce-cn) |
| **TypeScript**       | Strongly typed JavaScript superset    | -       | [TypeScript](https://www.typescriptlang.org/)        |

## 💡 Future Plans (TODO)

- [ ] Integrate and learn the practices of **LVS** (Linux Virtual Server) for system load balancing, studying the open-source project led by Dr. Wensong Zhang.
- [ ] Enhance and refine the relevant underlying encapsulation mechanisms for Monitor and Common modules.
- [ ] Add and verify more best-practice cases for high-frequency business scenarios (e.g., highly concurrent flash sales optimization).

## 💖 Epilogue

A huge thanks to the open-source community for providing excellent projects and guiding thoughts! I hope this project can serve as a reference for developers exploring the transition to microservices. Let's communicate and make progress together!
