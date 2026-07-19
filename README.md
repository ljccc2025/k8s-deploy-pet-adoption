# k8s-deploy-pet-adoption

宠物领养全栈微服务系统，用于学习 Spring Boot 微服务、Docker Compose 和 Kubernetes 部署。

## 第一阶段目标

第一阶段目标是在本地通过 Docker Compose 跑通前端、网关、后端服务、PostgreSQL 和 RabbitMQ，形成后续功能开发、接口联调和部署实践的基础环境。

当前仓库仅初始化基础结构与文档。根 Maven 聚合配置已经声明后续服务模块，但任务 1 不创建模块目录，因此当前阶段不要求 Maven 聚合构建通过。

## 本地运行

后续 Docker Compose 配置完成后，可使用以下命令启动本地环境：

```bash
cp .env.example .env
docker compose up --build
```

## 前端设计方向

管理端 UI 方向采用“安静、克制、工作台式后台审核体验”，服务于申请审核、状态追踪和运营处理等高频工作流。

用户前台保持同一套清晰可信的设计语言，强调宠物信息可读性、领养流程透明度和操作反馈一致性。
