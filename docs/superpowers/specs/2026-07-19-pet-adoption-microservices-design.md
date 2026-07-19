# 宠物领养全栈微服务系统设计

## 1. 背景与目标

本项目是一个用于教学、作品集和简历展示的宠物领养平台。第一阶段目标不是直接上线生产环境，而是构建一个可以在本地完整跑通的全栈微服务系统，并为后续迁移到 Kubernetes 留出清晰边界。

第一阶段采用 `Spring Boot 多服务 + Docker Compose` 方案，兼顾业务闭环、微服务拆分、容器化运行和后续 Kubernetes 学习。后续可以在此基础上补充 Dockerfile、Deployment、Service、Ingress、ConfigMap、Secret、PVC、HPA 和可观测性配置。

## 2. 第一阶段范围

第一阶段必须完成以下目标：

- 前端、API 网关、后端微服务、PostgreSQL 和 RabbitMQ 可以通过 Docker Compose 在本地完整启动。
- 用户可以注册、登录、浏览宠物、提交领养申请、查看申请状态和通知。
- 管理员可以维护宠物档案、审核领养申请并查看基础统计。
- 后端服务具备健康检查接口，方便后续映射为 Kubernetes `readinessProbe` 和 `livenessProbe`。
- 服务配置通过环境变量暴露，方便后续迁移到 ConfigMap 和 Secret。
- 文档明确后续 Kubernetes 迁移路径，但第一阶段不强制写完整 YAML。

为了控制实施复杂度，第一阶段按纵向闭环推进：

1. 先搭建仓库结构、公共约定、前端、网关、PostgreSQL 和 RabbitMQ。
2. 再实现 `auth-service`、`user-service`、`pet-service` 和 `adoption-service` 的核心链路。
3. 然后接入 `notification-service`，验证异步事件。
4. 最后补齐 `file-service`、`admin-service` 和 `recommendation-service` 的第一版能力。

这意味着所有服务都会有明确边界和可运行骨架，但功能优先级以“注册登录 -> 浏览宠物 -> 提交申请 -> 管理员审核 -> 生成通知”的闭环为准。

第一阶段不包含以下内容：

- 真实支付、物流、短信或邮件供应商接入。
- 复杂推荐算法。
- 分布式事务框架。
- 完整生产级监控告警。
- 多租户和复杂权限模型。

## 3. 技术栈

| 层级 | 选型 | 说明 |
| --- | --- | --- |
| 前端 | React + TypeScript + Vite | 开发快，适合作品集展示和管理后台页面 |
| API 网关 | Spring Cloud Gateway | 统一入口、路由、鉴权透传和基础限流 |
| 后端微服务 | Java 21 + Spring Boot | 适合展示企业级微服务能力 |
| 数据库 | PostgreSQL | 本地单实例，多 schema 隔离 |
| 数据迁移 | Flyway | 每个服务维护自己的迁移脚本 |
| ORM | Spring Data JPA | 降低样例项目复杂度，便于聚焦业务 |
| 消息队列 | RabbitMQ | 用于领养事件和通知消费 |
| 认证 | JWT + RBAC | 第一阶段仅包含 `USER` 和 `ADMIN` 角色 |
| 本地编排 | Docker Compose | 支持本地一键验收 |
| API 文档 | OpenAPI / Swagger | 每个服务自动生成接口文档 |
| 可观测性基础 | Spring Boot Actuator | 健康检查、指标和后续 Prometheus 接入基础 |

## 4. 服务边界

### 4.1 frontend

`frontend` 是用户和管理员的浏览器入口。它只访问 `api-gateway`，不直接访问任何后端微服务。

主要页面包括：

- 登录 / 注册页。
- 宠物列表页。
- 宠物详情页。
- 我的申请页。
- 我的通知页。
- 管理后台首页。
- 宠物管理页。
- 申请审核页。

### 4.2 api-gateway

`api-gateway` 是所有 HTTP 请求的统一入口。

职责：

- 路由前端请求到对应后端服务。
- 处理 CORS。
- 校验 JWT。
- 将用户身份和角色通过请求头透传给下游服务。
- 提供基础限流扩展点。

非职责：

- 不直接写业务数据。
- 不承载核心业务逻辑。
- 不直接访问数据库。

### 4.3 auth-service

`auth-service` 负责账号和认证。

职责：

- 用户注册。
- 用户登录。
- JWT 签发和刷新。
- 密码哈希。
- 角色管理。

第一阶段角色仅包含：

- `USER`：普通领养用户。
- `ADMIN`：平台管理员。

### 4.4 user-service

`user-service` 负责用户业务资料。

职责：

- 用户基础资料。
- 联系方式。
- 领养人资料。
- 用户资料完整性校验。

账号信息与用户资料分离：账号和密码归 `auth-service` 管理，用户业务档案归 `user-service` 管理。

### 4.5 pet-service

`pet-service` 负责宠物档案。

职责：

- 宠物列表和详情。
- 宠物分类、性别、年龄、健康状态和领养状态。
- 宠物图片引用。
- 管理员维护宠物档案。

宠物状态建议包括：

- `AVAILABLE`：可领养。
- `PENDING`：申请处理中。
- `ADOPTED`：已领养。
- `UNAVAILABLE`：暂不可领养。

### 4.6 adoption-service

`adoption-service` 负责领养申请流程。

职责：

- 用户提交领养申请。
- 用户查看自己的申请。
- 管理员审核申请。
- 用户取消申请。
- 记录申请状态变化。
- 发布领养相关事件。

申请状态建议包括：

- `SUBMITTED`：已提交。
- `UNDER_REVIEW`：审核中。
- `APPROVED`：已通过。
- `REJECTED`：已拒绝。
- `CANCELLED`：已取消。

### 4.7 notification-service

`notification-service` 通过 RabbitMQ 消费事件并生成通知。

第一阶段事件包括：

- `adoption.submitted`
- `adoption.approved`
- `adoption.rejected`
- `adoption.cancelled`

第一阶段通知可以写入站内通知表或日志。后续可以扩展为邮件、短信或 WebSocket 推送。

### 4.8 file-service

`file-service` 负责文件上传和文件元数据。

第一阶段使用本地挂载目录保存文件，数据库保存文件元数据。后续可以替换为 MinIO、S3 或其他对象存储。

职责：

- 上传宠物图片。
- 查询文件元数据。
- 为宠物档案提供图片引用。

### 4.9 admin-service

`admin-service` 为后台页面提供聚合接口。

职责：

- 聚合待审核申请。
- 聚合宠物统计。
- 聚合用户统计。
- 为后台首页提供汇总数据。

`admin-service` 不拥有核心业务数据，只聚合其他服务的数据。

### 4.10 recommendation-service

`recommendation-service` 负责宠物推荐。

第一阶段采用规则推荐，例如：

- 同类型宠物推荐。
- 同城市宠物推荐。
- 相近年龄宠物推荐。
- 根据用户申请记录排除已申请宠物。

第二阶段可以将该服务替换为 Python + FastAPI，实现多语言微服务展示。

## 5. 数据设计

第一阶段使用一个 PostgreSQL 实例，按服务隔离 schema：

| 服务 | schema |
| --- | --- |
| auth-service | `auth_schema` |
| user-service | `user_schema` |
| pet-service | `pet_schema` |
| adoption-service | `adoption_schema` |
| notification-service | `notification_schema` |
| file-service | `file_schema` |

每个服务只读写自己的 schema。跨服务数据通过 REST API 或事件传递，不直接跨 schema 查询。

这种设计能降低本地部署复杂度，同时保留后续拆成独立数据库的边界。

## 6. 通信设计

### 6.1 同步通信

同步通信采用 HTTP REST。

主要路径：

- `frontend` -> `api-gateway`
- `api-gateway` -> 后端微服务
- `adoption-service` -> `pet-service`
- `adoption-service` -> `user-service`
- `admin-service` -> 各业务服务
- `recommendation-service` -> `pet-service`

示例流程：

1. 用户提交领养申请。
2. `adoption-service` 调用 `pet-service` 校验宠物是否可领养。
3. `adoption-service` 调用 `user-service` 校验用户资料是否完整。
4. 校验通过后，`adoption-service` 创建申请记录。

### 6.2 异步通信

异步通信采用 RabbitMQ。

主要路径：

- `adoption-service` 发布领养事件。
- `notification-service` 消费领养事件并生成通知。
- 后续 `recommendation-service` 可以消费事件并更新推荐依据。

第一阶段不引入 Kafka，避免基础设施过重。Kafka 可以作为第二阶段扩展点。

### 6.3 一致性策略

系统采用最终一致性，不引入分布式事务。

示例：

1. 管理员审核通过申请。
2. `adoption-service` 将申请状态改为 `APPROVED`。
3. `adoption-service` 调用 `pet-service` 或发布事件，使宠物状态变为 `ADOPTED`。
4. `notification-service` 消费事件并生成用户通知。

如果下游处理失败，第一阶段通过日志和状态记录暴露问题；后续可以引入重试表、死信队列（Dead Letter Queue）或补偿任务。

## 7. 核心业务流程

### 7.1 注册登录流程

1. 用户在前端注册账号。
2. `auth-service` 保存账号和密码哈希。
3. 用户登录后获得 JWT。
4. 前端后续请求携带 `Authorization: Bearer <token>`。
5. `api-gateway` 校验 token，并将用户 ID 和角色透传给下游服务。

### 7.2 宠物浏览流程

1. 用户打开宠物列表页。
2. 前端请求 `api-gateway`。
3. `api-gateway` 转发到 `pet-service`。
4. `pet-service` 返回可领养宠物列表。
5. 用户进入详情页，查看宠物信息、图片和推荐列表。

### 7.3 领养申请流程

1. 用户在宠物详情页提交申请。
2. `adoption-service` 校验宠物状态和用户资料。
3. 校验通过后创建申请。
4. `adoption-service` 发布 `adoption.submitted` 事件。
5. `notification-service` 生成通知。
6. 管理员在后台审核申请。
7. 审核结果触发 `adoption.approved` 或 `adoption.rejected` 事件。

### 7.4 后台管理流程

1. 管理员登录后台。
2. 前端通过 `api-gateway` 访问 `admin-service`。
3. `admin-service` 聚合宠物、用户和申请数据。
4. 管理员维护宠物信息或审核申请。

## 8. 本地运行设计

第一阶段支持两种本地运行模式。

### 8.1 基础设施容器化

只用 Docker Compose 启动 PostgreSQL 和 RabbitMQ。后端服务在本机 IDE 中运行。

适用场景：

- 单服务调试。
- 断点调试。
- 快速修改后端代码。

### 8.2 全量容器化

用 Docker Compose 启动前端、网关、所有后端服务、PostgreSQL 和 RabbitMQ。

适用场景：

- 本地验收。
- Linux 虚拟机运行。
- 迁移 Kubernetes 前的基线检查。

## 9. 测试与验收

第一阶段测试保持实用，重点覆盖核心流程。

后端测试：

- 每个服务至少覆盖核心 Controller 和 Service。
- `adoption-service` 覆盖提交申请、审核申请和取消申请。
- `notification-service` 覆盖 RabbitMQ 事件消费。

前端测试：

- 登录页、宠物列表页、宠物详情页、申请页和后台审核页具备基础测试。
- 核心表单校验和页面跳转可验证。

手动验收清单：

- Docker Compose 可以完整启动。
- 用户可以注册和登录。
- 用户可以浏览宠物并提交申请。
- 管理员可以审核申请。
- 用户可以看到审核通知。
- RabbitMQ 中能看到事件流转。
- 每个后端服务健康检查可访问。

## 10. Kubernetes 迁移边界

第一阶段要为 Kubernetes 迁移保留以下约束：

- 每个服务独立端口、独立配置、独立健康检查。
- 所有环境差异通过环境变量配置。
- 服务不依赖本地绝对路径。
- 文件上传目录支持挂载卷。
- 数据库连接、RabbitMQ 连接、JWT 密钥等配置可以转为 Secret 或 ConfigMap。
- Docker Compose 服务名与后续 Kubernetes Service 名称尽量保持一致。

后续 Kubernetes 学习路线：

1. 为每个服务编写 Dockerfile。
2. 构建并推送镜像。
3. 编写 Deployment 和 Service。
4. 编写 ConfigMap 和 Secret。
5. 为 PostgreSQL、RabbitMQ 和文件目录设计 PVC。
6. 配置 Ingress。
7. 配置 readinessProbe 和 livenessProbe。
8. 配置 HPA。
9. 接入 Prometheus 和 Grafana。

## 11. 分阶段路线

### 第一阶段：本地完整跑通

- 完成前端、网关和核心后端服务。
- 完成 Docker Compose。
- 跑通注册、登录、浏览、申请、审核和通知流程。
- 编写 README 和本地运行文档。

### 第二阶段：Kubernetes 迁移

- 编写 Dockerfile。
- 编写 Kubernetes YAML。
- 在 Linux 虚拟机或本地集群中部署。
- 整理部署教程。

### 第三阶段：简历增强

- 将 `recommendation-service` 改为 Python + FastAPI。
- 引入 Prometheus 和 Grafana。
- 增加 RabbitMQ 死信队列和重试机制。
- 编写系统架构图、部署图和故障排查文档。

## 12. 成功标准

本设计成功的标准是：

- 系统可以作为完整微服务作品集展示。
- 本地可以通过 Docker Compose 跑通核心业务闭环。
- 服务边界清晰，适合后续迁移到 Kubernetes。
- 文档可以支撑后续部署教程和简历描述。
- 第一阶段复杂度可控，不会因为过早引入过多基础设施而阻塞业务闭环。
