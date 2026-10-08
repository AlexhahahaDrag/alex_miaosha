# **Alex Permission Management System**

<p>
  <img src="https://img.shields.io/badge/license-Apache--2.0-blue" alt="license"/>
  <img src="https://img.shields.io/badge/JDK-17%2B-green" alt="jdk"/>
  <a href="https://github.com/AlexhahahaDrag/alex_miaosha"><img src="https://img.shields.io/badge/SpringCloud-2021.0.3-green" alt="springcloud"/></a>
  <a href="https://github.com/AlexhahahaDrag/alex_miaosha"><img src="https://img.shields.io/badge/SpringBoot-2.7.2-green" alt="springboot"/></a>
  <img src="https://img.shields.io/badge/knife4j-3.0.3-green" alt="knife4j"/>
</p>

## 📖 Preface

**Alex Management System** is an open-source enterprise-ready platform based on a microservice architecture and a modern frontend-backend separation design. The project integrates cutting-edge technologies, distributed middleware, and **Domain AI capabilities**, aiming to build a structured, robust, and easily extensible microservice foundation.

<p align="center">
    <a href="https://github.com/AlexhahahaDrag/alex_miaosha">
        <img src="./doc/img/favicon.ico" alt="Alex Logo" style="width:160px;height:160px">
    </a>
</p>

## 🚀 Project Introduction

- **Frontend Clients**:
  - **PC Admin**: Built with Vue 3.5 + Ant Design Vue 4 + TypeScript + Tailwind CSS + Pinia + pnpm.
  - **Mobile Client**: Built with Vue 3.5 + Vant 4 + TypeScript + Pinia + pnpm.
- **Backend Services**: Built using Spring Boot 2.7.x + Spring Cloud 2021.0.x, integrated with MyBatis-Plus, Spring Security, Jasypt encryption, Knife4j API docs, and DeepSeek AI dual-routing engine.

## 🔗 Project Links

- 📦 **Backend GitHub Repository**: [AlexhahahaDrag/alex_miaosha](https://github.com/AlexhahahaDrag/alex_miaosha)
- 📦 **Backend Gitee Repository**: [AlexhahahaDrug/alex_miaosha_backend](https://gitee.com/AlexhahahaDrug/alex_miaosha_backend)
- 🎨 **PC Frontend Repository**: [AlexhahahaDrag/alex_miaosha_front](https://github.com/AlexhahahaDrag/alex_miaosha_front)
- 📱 **Mobile Repository**: [AlexhahahaDrag/alex_miaosha_mobile](https://github.com/AlexhahahaDrag/alex_miaosha_mobile)

## 🧩 Module Breakdown

- **`alex_generator`**: Code generator for automated scaffolding of frontend and backend CRUD code.
- **`alex_miaosha_common`**: Shared core foundation with two tiers: `common_api` (lightweight contracts) and `common_core` (runtime components).
- **`alex_miaosha_finance`**: Finance management module handling gift record tracking, coupon operations, and ledger reconciliation.
- **`alex_miaosha_gateway`**: Unified microservice API Gateway for traffic routing, security checks, and token dyeing.
- **`alex_miaosha_ai`**: AI intelligent analysis center integrating DeepSeek LLM + rule-engine dual routing for gift text parsing, return gift calculation, coupon strategy, product copywriting, and role permission recommendations.
- **`alex_miaosha_oss`**: Object storage service managing MinIO / Aliyun OSS image and attachment storage.
- **`alex_miaosha_product`**: Product catalog and SKU flash-sale stock management.
- **`alex_miaosha_user`**: RBAC user and permission center with data permission scoping.

## 🛠️ Infrastructure & Startup

1. **Prerequisites**: JDK 17+, Maven 3.8+, Nacos 2.x, Redis 6.x, MySQL 8.x, RabbitMQ 3.x.
2. **Startup Sequence**: `Nacos` -> `alex_miaosha_gateway` -> `alex_miaosha_user` -> `alex_miaosha_ai` -> other business services.

## 📚 Technology Stack

### Backend Stack
- Spring Boot 2.7.2, Spring Cloud 2021.0.3, Spring Security
- MyBatis-Plus with Data Permission interceptors
- Nacos (Discovery & Config)
- Redis / Redisson, RabbitMQ
- DeepSeek Large Language Model + Fallback Rule Engine

### Frontend Stack
- Vue 3.5, TypeScript, Vite
- Ant Design Vue 4 (PC), Vant 4 (Mobile)
- Tailwind CSS, Pinia, ECharts 6
- Midscene + Playwright AI smoke testing

## 💖 Epilogue

A huge thanks to the open-source community for providing excellent projects and guiding ideas. Welcome to **Star** and **Fork** to support!
