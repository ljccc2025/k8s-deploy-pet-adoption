# 本地开发

本项目第一阶段面向本地完整验收，目标是通过 Docker Compose 跑通前端、网关、后端服务、PostgreSQL 和 RabbitMQ。

## 环境变量

先复制示例配置：

```bash
cp .env.example .env
```

`.env` 只用于本地开发，不应提交到仓库。

## 模式一：基础设施容器化

基础设施容器化模式只通过 Docker Compose 启动 PostgreSQL、RabbitMQ 等依赖服务，前端和各 Spring Boot 服务在本机开发环境中运行。

该模式适合日常开发和调试，便于使用 IDE 断点、热更新和本地命令行工具。

## 模式二：全量容器化

全量容器化模式通过 Docker Compose 同时启动前端、网关、后端服务、PostgreSQL 和 RabbitMQ。

该模式更接近第一阶段验收环境，适合验证服务编排、网络连通、环境变量、健康检查和跨服务调用。

第一阶段先从全量 Compose 验收出发，确保本地环境可以用一条命令完整启动：

```bash
docker compose up --build
```

当前任务仅建立仓库基础结构，暂不提供 Docker Compose 配置和服务实现。
