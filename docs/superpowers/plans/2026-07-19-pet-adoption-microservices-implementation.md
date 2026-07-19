# 宠物领养微服务系统第一阶段实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 构建一个可通过 Docker Compose 在本地完整跑通的宠物领养全栈微服务系统。

**架构：** 前端只访问 `api-gateway`，网关转发到后端服务。核心业务由 `auth-service`、`user-service`、`pet-service` 和 `adoption-service` 跑通，`notification-service` 通过 RabbitMQ 消费事件。`file-service`、`admin-service` 和 `recommendation-service` 在第一阶段提供可运行的最小能力。

**技术栈：** Java 21、Spring Boot 3.5.x、Spring Cloud Gateway、Spring Data JPA、Flyway、RabbitMQ、PostgreSQL、React、TypeScript、Vite、Docker Compose。

---

## 文件结构

计划创建或修改以下文件和目录：

| 路径 | 职责 |
| --- | --- |
| `README.md` | 项目介绍、本地运行命令、服务端口和验收流程 |
| `.editorconfig` | 统一基础编辑器格式 |
| `.env.example` | Docker Compose 环境变量示例 |
| `pom.xml` | Maven 聚合根项目，统一 Java 版本和依赖版本 |
| `libs/common/pom.xml` | Java 公共库模块 |
| `libs/common/src/main/java/com/petadoption/common/**` | 通用响应、错误模型、请求头常量、事件名 |
| `services/*/pom.xml` | 每个后端服务的 Maven 模块配置 |
| `services/*/src/main/java/com/petadoption/*/**` | 各服务业务代码 |
| `services/*/src/main/resources/application.yml` | 各服务配置模板 |
| `services/*/src/main/resources/db/migration/**` | 各服务 Flyway 迁移脚本 |
| `services/*/Dockerfile` | 后端服务镜像构建 |
| `frontend/**` | React + TypeScript 前端应用 |
| `frontend/Dockerfile` | 前端生产镜像构建 |
| `docker-compose.yml` | 本地全量容器编排 |
| `docs/local-development.md` | 本地开发模式说明 |
| `docs/manual-test-checklist.md` | 手动验收清单 |

服务目录固定为：

```text
services/
  api-gateway/
  auth-service/
  user-service/
  pet-service/
  adoption-service/
  notification-service/
  file-service/
  admin-service/
  recommendation-service/
```

---

## 任务 1：建立仓库基础结构

**文件：**
- 修改：`README.md`
- 创建：`.editorconfig`
- 创建：`.env.example`
- 创建：`.gitignore`
- 创建：`docs/local-development.md`
- 创建：`docs/manual-test-checklist.md`
- 创建：`pom.xml`

- [ ] **步骤 1：编写根 Maven 配置**

在 `pom.xml` 中声明根项目。根项目只做版本管理，不放业务代码。任务 1 不创建模块目录，因此不要激活 `<modules>`；只用注释记录未来模块清单，保证根目录 `mvn -q validate` 可以通过。

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.petadoption</groupId>
  <artifactId>pet-adoption-platform</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <packaging>pom</packaging>

  <properties>
    <java.version>21</java.version>
    <spring-boot.version>3.5.9</spring-boot.version>
    <spring-cloud.version>2025.0.0</spring-cloud.version>
  </properties>

  <!--
    Future modules will be added when their directories are created:
    libs/common
    services/api-gateway
    services/auth-service
    services/user-service
    services/pet-service
    services/adoption-service
    services/notification-service
    services/file-service
    services/admin-service
    services/recommendation-service
  -->
</project>
```

- [ ] **步骤 2：编写基础文档**

`README.md` 必须包含：

README 需要写明：

- 项目名称：`k8s-deploy-pet-adoption`。
- 项目定位：宠物领养全栈微服务系统，用于学习 Spring Boot 微服务、Docker Compose 和 Kubernetes 部署。
- 第一阶段目标：本地通过 Docker Compose 跑通前端、网关、后端服务、PostgreSQL 和 RabbitMQ。
- 本地运行命令：先执行 `cp .env.example .env`，再执行 `docker compose up --build`。

- [ ] **步骤 3：运行基础校验**

运行：

```bash
git status --short
mvn -q validate
```

预期：只出现本任务创建或修改的文件。

预期：`mvn -q validate` PASS。

- [ ] **步骤 4：Commit**

```bash
git add README.md .editorconfig .env.example .gitignore docs/local-development.md docs/manual-test-checklist.md pom.xml
git commit -m "chore(项目): 初始化微服务仓库结构"
```

---

## 任务 2：创建 Java 公共库

**文件：**
- 创建：`libs/common/pom.xml`
- 创建：`libs/common/src/main/java/com/petadoption/common/api/ApiResponse.java`
- 创建：`libs/common/src/main/java/com/petadoption/common/api/ErrorResponse.java`
- 创建：`libs/common/src/main/java/com/petadoption/common/security/AuthHeaders.java`
- 创建：`libs/common/src/main/java/com/petadoption/common/events/AdoptionEvents.java`
- 创建：`libs/common/src/test/java/com/petadoption/common/api/ApiResponseTest.java`

- [ ] **步骤 0：创建 common 模块 POM，再加入根 Maven modules**

先创建 `libs/common/pom.xml` 的最小可解析 POM：

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>com.petadoption</groupId>
    <artifactId>pet-adoption-platform</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <relativePath>../../pom.xml</relativePath>
  </parent>

  <artifactId>common</artifactId>

  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>
</project>
```

再修改根 `pom.xml`，把 `libs/common` 加入 `<modules>`。此时模块 POM 已存在，激活该模块不会破坏 `mvn validate`。

```xml
<modules>
  <module>libs/common</module>
</modules>
```

- [ ] **步骤 1：编写失败测试**

```java
package com.petadoption.common.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {
  @Test
  void successWrapsData() {
    ApiResponse<String> response = ApiResponse.success("ok");

    assertThat(response.success()).isTrue();
    assertThat(response.data()).isEqualTo("ok");
    assertThat(response.message()).isEqualTo("success");
  }

  @Test
  void errorResponseContainsCodeAndMessage() {
    ErrorResponse response = ErrorResponse.of("CODE", "message");

    assertThat(response.code()).isEqualTo("CODE");
    assertThat(response.message()).isEqualTo("message");
  }
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：

```bash
mvn -pl libs/common test
```

预期：FAIL，原因是 `ApiResponse` 尚不存在。

- [ ] **步骤 3：实现公共类型**

```java
package com.petadoption.common.api;

public record ApiResponse<T>(boolean success, String message, T data) {
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, "success", data);
  }

  public static <T> ApiResponse<T> failure(String message) {
    return new ApiResponse<>(false, message, null);
  }
}
```

```java
package com.petadoption.common.api;

public record ErrorResponse(String code, String message) {
  public static ErrorResponse of(String code, String message) {
    return new ErrorResponse(code, message);
  }
}
```

```java
package com.petadoption.common.security;

public final class AuthHeaders {
  public static final String USER_ID = "X-User-Id";
  public static final String USER_ROLE = "X-User-Role";

  private AuthHeaders() {
  }
}
```

```java
package com.petadoption.common.events;

public final class AdoptionEvents {
  public static final String SUBMITTED = "adoption.submitted";
  public static final String APPROVED = "adoption.approved";
  public static final String REJECTED = "adoption.rejected";
  public static final String CANCELLED = "adoption.cancelled";

  private AdoptionEvents() {
  }
}
```

- [ ] **步骤 4：运行测试验证通过**

运行：

```bash
mvn -pl libs/common test
```

预期：PASS。

- [ ] **步骤 5：Commit**

```bash
git add libs/common pom.xml
git commit -m "feat(公共库): 添加通用响应和事件常量"
```

---

## 任务 3：搭建后端服务骨架

**文件：**
- 修改：`pom.xml`
- 创建：`services/*/pom.xml`
- 创建：`services/*/src/main/java/com/petadoption/*/*Application.java`
- 创建：`services/*/src/main/resources/application.yml`
- 创建：`services/*/src/test/java/com/petadoption/*/*ApplicationTests.java`

- [ ] **步骤 1：创建所有服务 Maven POM**

先创建所有 `services/*/pom.xml`，确认这些 POM 文件都存在且可解析，再修改根 `pom.xml` 加入 Maven modules。若先把不存在的服务目录加入根 `<modules>`，`mvn test` 会因为模块无法解析而失败。

`api-gateway` 使用 Spring Cloud Gateway / WebFlux 专用 POM；其余业务服务使用普通 Spring MVC Web 模板。所有 Spring Boot 服务模块必须声明 `spring-boot-maven-plugin` 的 `repackage` execution，确保未使用 `spring-boot-starter-parent` 时 `mvn package` 仍能产出可通过 `java -jar` 运行的可执行 jar。插件版本由根 POM 的 `pluginManagement` 管理。

业务服务 `pom.xml` 使用相同模式。以 `pet-service` 为例：

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>com.petadoption</groupId>
    <artifactId>pet-adoption-platform</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <relativePath>../../pom.xml</relativePath>
  </parent>
  <artifactId>pet-service</artifactId>

  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
      <groupId>com.petadoption</groupId>
      <artifactId>common</artifactId>
      <version>${project.version}</version>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <executions>
          <execution>
            <goals>
              <goal>repackage</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

`api-gateway` 使用 WebFlux Gateway starter，不使用普通 `spring-boot-starter-web` MVC 模板：

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>com.petadoption</groupId>
    <artifactId>pet-adoption-platform</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <relativePath>../../pom.xml</relativePath>
  </parent>
  <artifactId>api-gateway</artifactId>

  <dependencies>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-gateway-server-webflux</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
      <groupId>com.petadoption</groupId>
      <artifactId>common</artifactId>
      <version>${project.version}</version>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <executions>
          <execution>
            <goals>
              <goal>repackage</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

- [ ] **步骤 2：将服务加入根 Maven modules**

所有服务 POM 文件创建完成后，修改根 `pom.xml` 的 `<modules>`。最终至少包含：

```xml
<modules>
  <module>libs/common</module>
  <module>services/api-gateway</module>
  <module>services/auth-service</module>
  <module>services/user-service</module>
  <module>services/pet-service</module>
  <module>services/adoption-service</module>
  <module>services/notification-service</module>
  <module>services/file-service</module>
  <module>services/admin-service</module>
  <module>services/recommendation-service</module>
</modules>
```

- [ ] **步骤 3：创建应用入口**

```java
package com.petadoption.pet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PetServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(PetServiceApplication.class, args);
  }
}
```

- [ ] **步骤 4：配置健康检查**

每个 `application.yml` 设置服务名、端口和 Actuator：

```yaml
spring:
  application:
    name: pet-service

server:
  port: 8083

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

- [ ] **步骤 5：运行后端聚合测试**

运行：

```bash
mvn test
```

预期：所有服务上下文加载测试 PASS。

- [ ] **步骤 6：Commit**

```bash
git add services pom.xml
git commit -m "feat(后端): 搭建 Spring Boot 微服务骨架"
```

---

## 任务 4：编写本地基础设施 Compose

**文件：**
- 创建：`docker-compose.yml`
- 修改：`.env.example`

- [ ] **步骤 1：定义 PostgreSQL 和 RabbitMQ**

`docker-compose.yml` 先只包含基础设施：

```yaml
services:
  postgres:
    image: postgres:17-alpine
    environment:
      POSTGRES_DB: pet_adoption
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER} -d pet_adoption"]
      interval: 5s
      timeout: 3s
      retries: 10

  rabbitmq:
    image: rabbitmq:4-management-alpine
    environment:
      RABBITMQ_DEFAULT_USER: ${RABBITMQ_USER}
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_PASSWORD}
    ports:
      - "5672:5672"
      - "15672:15672"
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "ping"]
      interval: 5s
      timeout: 5s
      retries: 10
```

- [ ] **步骤 2：补充环境变量示例**

`.env.example` 内容：

```dotenv
POSTGRES_USER=pet
POSTGRES_PASSWORD=pet
RABBITMQ_USER=pet
RABBITMQ_PASSWORD=pet
JWT_SECRET=local-dev-secret-change-me
```

- [ ] **步骤 3：验证基础设施启动**

运行：

```bash
cp .env.example .env
docker compose up -d postgres rabbitmq
docker compose ps
```

预期：`postgres` 和 `rabbitmq` 状态为 healthy。

- [ ] **步骤 4：Commit**

```bash
git add docker-compose.yml .env.example
git commit -m "chore(基础设施): 添加 PostgreSQL 和 RabbitMQ 编排"
```

---

## 任务 5：实现 auth-service 注册登录

**文件：**
- 修改：`services/auth-service/pom.xml`
- 修改：`services/auth-service/src/main/resources/application.yml`
- 创建：`services/auth-service/src/main/resources/db/migration/V1__create_auth_tables.sql`
- 创建：`services/auth-service/src/main/java/com/petadoption/auth/account/**`
- 创建：`services/auth-service/src/main/java/com/petadoption/auth/security/**`
- 创建：`services/auth-service/src/test/java/com/petadoption/auth/account/AuthControllerTest.java`

- [ ] **步骤 1：编写注册登录接口测试**

```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
  @Autowired MockMvc mockMvc;

  @Test
  void registerAndLoginReturnsToken() throws Exception {
    mockMvc.perform(post("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"email":"user@example.com","password":"Password123","role":"USER"}
        """))
      .andExpect(status().isOk());

    mockMvc.perform(post("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"email":"user@example.com","password":"Password123"}
        """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
  }
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：

```bash
mvn -pl services/auth-service test
```

预期：FAIL，原因是接口和实体尚不存在。

- [ ] **步骤 3：创建账号表**

```sql
CREATE SCHEMA IF NOT EXISTS auth_schema;

CREATE TABLE auth_schema.accounts (
  id UUID PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(32) NOT NULL,
  created_at TIMESTAMP NOT NULL
);
```

- [ ] **步骤 4：实现注册、登录和 JWT 签发**

核心类型：

```java
public record RegisterRequest(String email, String password, String role) {}
public record LoginRequest(String email, String password) {}
public record TokenResponse(String accessToken) {}
```

`AuthController` 路径：

```java
@RestController
@RequestMapping("/api/v1/auth")
class AuthController {
  private final AuthService authService;

  AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  ApiResponse<Void> register(@RequestBody RegisterRequest request) {
    authService.register(request);
    return ApiResponse.success(null);
  }

  @PostMapping("/login")
  ApiResponse<TokenResponse> login(@RequestBody LoginRequest request) {
    return ApiResponse.success(authService.login(request));
  }
}
```

- [ ] **步骤 5：运行测试验证通过**

运行：

```bash
mvn -pl services/auth-service test
```

预期：PASS。

- [ ] **步骤 6：Commit**

```bash
git add services/auth-service
git commit -m "feat(认证): 实现注册登录和 JWT 签发"
```

---

## 任务 6：实现 user-service 用户资料

**文件：**
- 修改：`services/user-service/pom.xml`
- 创建：`services/user-service/src/main/resources/db/migration/V1__create_user_tables.sql`
- 创建：`services/user-service/src/main/java/com/petadoption/user/profile/**`
- 创建：`services/user-service/src/test/java/com/petadoption/user/profile/UserProfileControllerTest.java`

- [ ] **步骤 1：编写用户资料测试**

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserProfileControllerTest {
  @Autowired MockMvc mockMvc;

  @Test
  void createAndReadProfile() throws Exception {
    mockMvc.perform(put("/api/v1/users/me/profile")
        .header("X-User-Id", "11111111-1111-1111-1111-111111111111")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"displayName":"Alice","phone":"13800000000","city":"上海","housing":"自有住房"}
        """))
      .andExpect(status().isOk());

    mockMvc.perform(get("/api/v1/users/me/profile")
        .header("X-User-Id", "11111111-1111-1111-1111-111111111111"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.displayName").value("Alice"));
  }
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：

```bash
mvn -pl services/user-service test
```

预期：FAIL。

- [ ] **步骤 3：实现资料表和接口**

```sql
CREATE SCHEMA IF NOT EXISTS user_schema;

CREATE TABLE user_schema.user_profiles (
  user_id UUID PRIMARY KEY,
  display_name VARCHAR(100) NOT NULL,
  phone VARCHAR(30) NOT NULL,
  city VARCHAR(100) NOT NULL,
  housing VARCHAR(255) NOT NULL,
  updated_at TIMESTAMP NOT NULL
);
```

接口：

```java
@PutMapping("/me/profile")
ApiResponse<UserProfileResponse> upsert(@RequestHeader(AuthHeaders.USER_ID) UUID userId,
                                        @RequestBody UpsertProfileRequest request) {
  return ApiResponse.success(profileService.upsert(userId, request));
}
```

- [ ] **步骤 4：运行测试验证通过**

```bash
mvn -pl services/user-service test
```

- [ ] **步骤 5：Commit**

```bash
git add services/user-service
git commit -m "feat(用户): 实现领养人资料维护"
```

---

## 任务 7：实现 pet-service 宠物浏览和管理

**文件：**
- 修改：`services/pet-service/pom.xml`
- 创建：`services/pet-service/src/main/resources/db/migration/V1__create_pet_tables.sql`
- 创建：`services/pet-service/src/main/resources/db/migration/V2__seed_pets.sql`
- 创建：`services/pet-service/src/main/java/com/petadoption/pet/catalog/**`
- 创建：`services/pet-service/src/test/java/com/petadoption/pet/catalog/PetControllerTest.java`

- [ ] **步骤 1：编写宠物列表和详情测试**

```java
@SpringBootTest
@AutoConfigureMockMvc
class PetControllerTest {
  @Autowired MockMvc mockMvc;

  @Test
  void listAvailablePets() throws Exception {
    mockMvc.perform(get("/api/v1/pets?status=AVAILABLE"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data").isArray());
  }
}
```

- [ ] **步骤 2：运行测试验证失败**

```bash
mvn -pl services/pet-service test
```

- [ ] **步骤 3：创建宠物表**

```sql
CREATE SCHEMA IF NOT EXISTS pet_schema;

CREATE TABLE pet_schema.pets (
  id UUID PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  type VARCHAR(50) NOT NULL,
  gender VARCHAR(20) NOT NULL,
  age_months INT NOT NULL,
  city VARCHAR(100) NOT NULL,
  health_status VARCHAR(255) NOT NULL,
  adoption_status VARCHAR(32) NOT NULL,
  image_url VARCHAR(500),
  description TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);
```

- [ ] **步骤 4：实现列表、详情和状态更新接口**

接口路径：

```text
GET /api/v1/pets
GET /api/v1/pets/{id}
POST /api/v1/admin/pets
PUT /api/v1/admin/pets/{id}
PATCH /api/v1/internal/pets/{id}/adoption-status
```

- [ ] **步骤 5：运行测试验证通过**

```bash
mvn -pl services/pet-service test
```

- [ ] **步骤 6：Commit**

```bash
git add services/pet-service
git commit -m "feat(宠物): 实现宠物浏览和管理接口"
```

---

## 任务 8：实现 adoption-service 申请流程

**文件：**
- 修改：`services/adoption-service/pom.xml`
- 创建：`services/adoption-service/src/main/resources/db/migration/V1__create_adoption_tables.sql`
- 创建：`services/adoption-service/src/main/java/com/petadoption/adoption/application/**`
- 创建：`services/adoption-service/src/main/java/com/petadoption/adoption/client/**`
- 创建：`services/adoption-service/src/main/java/com/petadoption/adoption/messaging/**`
- 创建：`services/adoption-service/src/test/java/com/petadoption/adoption/application/AdoptionApplicationControllerTest.java`

- [ ] **步骤 1：编写提交申请测试**

```java
@SpringBootTest
@AutoConfigureMockMvc
class AdoptionApplicationControllerTest {
  @Autowired MockMvc mockMvc;

  @Test
  void submitApplication() throws Exception {
    mockMvc.perform(post("/api/v1/adoptions")
        .header("X-User-Id", "11111111-1111-1111-1111-111111111111")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {"petId":"22222222-2222-2222-2222-222222222222","reason":"我有稳定时间照顾它","experience":"曾经养过猫"}
        """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
  }
}
```

- [ ] **步骤 2：运行测试验证失败**

```bash
mvn -pl services/adoption-service test
```

- [ ] **步骤 3：创建申请表**

```sql
CREATE SCHEMA IF NOT EXISTS adoption_schema;

CREATE TABLE adoption_schema.adoption_applications (
  id UUID PRIMARY KEY,
  pet_id UUID NOT NULL,
  user_id UUID NOT NULL,
  reason TEXT NOT NULL,
  experience TEXT NOT NULL,
  status VARCHAR(32) NOT NULL,
  reviewer_id UUID,
  review_comment TEXT,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);
```

- [ ] **步骤 4：实现申请接口和状态流转**

接口路径：

```text
POST /api/v1/adoptions
GET /api/v1/adoptions/me
GET /api/v1/admin/adoptions
POST /api/v1/admin/adoptions/{id}/approve
POST /api/v1/admin/adoptions/{id}/reject
POST /api/v1/adoptions/{id}/cancel
```

事件载荷：

```java
public record AdoptionEvent(
  String eventType,
  UUID applicationId,
  UUID petId,
  UUID userId,
  Instant occurredAt
) {}
```

- [ ] **步骤 5：运行测试验证通过**

```bash
mvn -pl services/adoption-service test
```

- [ ] **步骤 6：Commit**

```bash
git add services/adoption-service
git commit -m "feat(领养): 实现申请提交和审核流程"
```

---

## 任务 9：实现 notification-service 事件消费

**文件：**
- 修改：`services/notification-service/pom.xml`
- 创建：`services/notification-service/src/main/resources/db/migration/V1__create_notification_tables.sql`
- 创建：`services/notification-service/src/main/java/com/petadoption/notification/**`
- 创建：`services/notification-service/src/test/java/com/petadoption/notification/NotificationConsumerTest.java`

- [ ] **步骤 1：编写事件消费测试**

```java
@SpringBootTest
class NotificationConsumerTest {
  @Autowired NotificationService notificationService;

  @Test
  void createsNotificationFromAdoptionEvent() {
    AdoptionEvent event = new AdoptionEvent(
      "adoption.approved",
      UUID.randomUUID(),
      UUID.randomUUID(),
      UUID.fromString("11111111-1111-1111-1111-111111111111"),
      Instant.now()
    );

    Notification notification = notificationService.createFrom(event);

    assertThat(notification.userId()).isEqualTo(event.userId());
    assertThat(notification.message()).contains("通过");
  }
}
```

- [ ] **步骤 2：运行测试验证失败**

```bash
mvn -pl services/notification-service test
```

- [ ] **步骤 3：创建通知表和消费者**

```sql
CREATE SCHEMA IF NOT EXISTS notification_schema;

CREATE TABLE notification_schema.notifications (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  event_type VARCHAR(100) NOT NULL,
  message VARCHAR(500) NOT NULL,
  read_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL
);
```

消费者方法：

```java
@RabbitListener(queues = "adoption.notifications")
void handleAdoptionEvent(AdoptionEvent event) {
  notificationService.createFrom(event);
}
```

- [ ] **步骤 4：实现通知查询接口**

```text
GET /api/v1/notifications/me
POST /api/v1/notifications/{id}/read
```

- [ ] **步骤 5：运行测试验证通过**

```bash
mvn -pl services/notification-service test
```

- [ ] **步骤 6：Commit**

```bash
git add services/notification-service
git commit -m "feat(通知): 消费领养事件并生成站内通知"
```

---

## 任务 10：实现 api-gateway 路由和鉴权透传

**文件：**
- 修改：`services/api-gateway/pom.xml`
- 修改：`services/api-gateway/src/main/resources/application.yml`
- 创建：`services/api-gateway/src/main/java/com/petadoption/gateway/security/**`
- 创建：`services/api-gateway/src/test/java/com/petadoption/gateway/security/GatewayAuthFilterTest.java`

- [ ] **步骤 1：编写网关鉴权测试**

```java
@SpringBootTest
class GatewayAuthFilterTest {
  @Test
  void publicAuthRoutesDoNotRequireToken() {
    RouteSecurity security = new RouteSecurity();

    assertThat(security.isPublicPath("/api/v1/auth/login")).isTrue();
    assertThat(security.isPublicPath("/api/v1/pets")).isTrue();
    assertThat(security.isPublicPath("/api/v1/adoptions")).isFalse();
  }
}
```

- [ ] **步骤 2：运行测试验证失败**

```bash
mvn -pl services/api-gateway test
```

- [ ] **步骤 3：实现路由配置**

```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          routes:
            - id: auth-service
              uri: ${AUTH_SERVICE_URL:http://localhost:8081}
              predicates:
                - Path=/api/v1/auth/**
            - id: pet-service
              uri: ${PET_SERVICE_URL:http://localhost:8083}
              predicates:
                - Path=/api/v1/pets/**,/api/v1/admin/pets/**
```

- [ ] **步骤 4：实现 JWT 校验和请求头透传**

公开路径：

```java
boolean isPublicPath(String path) {
  return path.startsWith("/api/v1/auth/")
      || path.equals("/api/v1/pets")
      || path.startsWith("/api/v1/pets/");
}
```

下游请求头：

```text
X-User-Id: <user-id>
X-User-Role: USER|ADMIN
```

- [ ] **步骤 5：运行测试验证通过**

```bash
mvn -pl services/api-gateway test
```

- [ ] **步骤 6：Commit**

```bash
git add services/api-gateway
git commit -m "feat(网关): 添加路由和 JWT 鉴权透传"
```

---

## 任务 11：实现 frontend 核心页面

**文件：**
- 创建：`frontend/package.json`
- 创建：`frontend/vite.config.ts`
- 创建：`frontend/src/main.tsx`
- 创建：`frontend/src/app/App.tsx`
- 创建：`frontend/src/api/client.ts`
- 创建：`frontend/src/pages/**`
- 创建：`frontend/src/components/**`
- 创建：`frontend/src/styles.css`

- [ ] **步骤 1：创建 Vite React TypeScript 项目**

运行：

```bash
npm create vite@latest frontend -- --template react-ts
```

如果 `frontend` 目录已经由执行者手动创建，则保留目录并补齐 Vite 模板文件。

- [ ] **步骤 2：实现 API 客户端**

```typescript
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export async function apiGet<T>(path: string): Promise<T> {
  const token = localStorage.getItem('accessToken');
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });

  if (!response.ok) {
    throw new Error(`Request failed: ${response.status}`);
  }

  const body = await response.json();
  return body.data as T;
}
```

- [ ] **步骤 3：实现页面路由**

页面固定为：

```text
/login
/register
/pets
/pets/:id
/applications
/notifications
/admin
/admin/pets
/admin/adoptions
```

- [ ] **步骤 4：实现核心页面状态**

`PetListPage` 至少包含：

```typescript
type PetSummary = {
  id: string;
  name: string;
  type: string;
  gender: string;
  ageMonths: number;
  city: string;
  adoptionStatus: string;
  imageUrl?: string;
};
```

- [ ] **步骤 5：运行前端构建**

运行：

```bash
cd frontend
npm install
npm run build
```

预期：PASS，生成 `frontend/dist`。

- [ ] **步骤 6：Commit**

```bash
git add frontend
git commit -m "feat(前端): 实现宠物领养核心页面"
```

---

## 任务 12：补齐 file、admin、recommendation 最小能力

**文件：**
- 创建：`services/file-service/src/main/java/com/petadoption/file/**`
- 创建：`services/file-service/src/main/resources/db/migration/V1__create_file_tables.sql`
- 创建：`services/admin-service/src/main/java/com/petadoption/admin/**`
- 创建：`services/recommendation-service/src/main/java/com/petadoption/recommendation/**`
- 创建：对应测试文件

- [ ] **步骤 1：实现 file-service 元数据接口**

接口：

```text
POST /api/v1/files
GET /api/v1/files/{id}
```

元数据表：

```sql
CREATE SCHEMA IF NOT EXISTS file_schema;

CREATE TABLE file_schema.files (
  id UUID PRIMARY KEY,
  original_name VARCHAR(255) NOT NULL,
  content_type VARCHAR(100) NOT NULL,
  storage_path VARCHAR(500) NOT NULL,
  created_at TIMESTAMP NOT NULL
);
```

- [ ] **步骤 2：实现 admin-service 聚合接口**

接口：

```text
GET /api/v1/admin/dashboard
```

响应模型：

```java
public record DashboardSummary(
  long totalPets,
  long availablePets,
  long pendingApplications,
  long totalUsers
) {}
```

- [ ] **步骤 3：实现 recommendation-service 规则推荐**

接口：

```text
GET /api/v1/recommendations/pets?petId=<id>
```

规则：

```java
List<PetSummary> recommend(PetSummary current, List<PetSummary> candidates) {
  return candidates.stream()
      .filter(candidate -> !candidate.id().equals(current.id()))
      .filter(candidate -> candidate.type().equals(current.type()) || candidate.city().equals(current.city()))
      .limit(6)
      .toList();
}
```

- [ ] **步骤 4：运行三个服务测试**

```bash
mvn -pl services/file-service,services/admin-service,services/recommendation-service test
```

预期：PASS。

- [ ] **步骤 5：Commit**

```bash
git add services/file-service services/admin-service services/recommendation-service
git commit -m "feat(扩展服务): 添加文件后台和推荐最小能力"
```

---

## 任务 13：容器化所有服务并完善 Compose

**文件：**
- 创建：`services/*/Dockerfile`
- 创建：`frontend/Dockerfile`
- 修改：`docker-compose.yml`
- 修改：`.env.example`

- [ ] **步骤 1：为后端服务创建 Dockerfile**

每个服务 Dockerfile 使用相同模式，以 `pet-service` 为例：

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
ARG JAR_FILE=target/pet-service-0.1.0-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

- [ ] **步骤 2：为前端创建 Dockerfile**

```dockerfile
FROM node:24-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.29-alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
```

- [ ] **步骤 3：将服务加入 Docker Compose**

服务依赖使用健康检查条件：

```yaml
auth-service:
  build:
    context: .
    dockerfile: services/auth-service/Dockerfile
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/pet_adoption
    SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER}
    SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD}
    JWT_SECRET: ${JWT_SECRET}
  depends_on:
    postgres:
      condition: service_healthy
```

- [ ] **步骤 4：运行全量容器验收**

```bash
mvn package -DskipTests
docker compose up --build
```

预期：

- `frontend` 可访问。
- `api-gateway` 可访问 `/actuator/health`。
- 各服务健康检查返回 UP。
- PostgreSQL 和 RabbitMQ 为 healthy。

- [ ] **步骤 5：Commit**

```bash
git add docker-compose.yml .env.example services frontend
git commit -m "chore(容器): 添加服务镜像和完整 Compose 编排"
```

---

## 任务 14：执行端到端验收并完善文档

**文件：**
- 修改：`README.md`
- 修改：`docs/local-development.md`
- 修改：`docs/manual-test-checklist.md`

- [ ] **步骤 1：运行后端测试**

```bash
mvn test
```

预期：PASS。

- [ ] **步骤 2：运行前端构建**

```bash
cd frontend
npm run build
```

预期：PASS。

- [ ] **步骤 3：运行全量 Compose**

```bash
docker compose up --build
```

预期：所有容器启动成功。

- [ ] **步骤 4：执行手动验收**

在 `docs/manual-test-checklist.md` 中逐项记录：

```markdown
- [ ] 用户注册成功。
- [ ] 用户登录后获得 token。
- [ ] 用户可以浏览宠物列表。
- [ ] 用户可以提交领养申请。
- [ ] 管理员可以审核申请。
- [ ] 用户可以看到通知。
- [ ] RabbitMQ 管理页面可以看到队列。
- [ ] 每个服务 `/actuator/health` 返回 UP。
```

- [ ] **步骤 5：Commit**

```bash
git add README.md docs/local-development.md docs/manual-test-checklist.md
git commit -m "docs(验收): 完善本地运行和测试说明"
```

---

## 自检结果

规格覆盖：

- 服务边界：任务 3、5、6、7、8、9、10、12 覆盖。
- 数据隔离：任务 5、6、7、8、9、12 覆盖各服务 schema。
- 同步通信：任务 8、10、12 覆盖。
- 异步通信：任务 8、9 覆盖。
- 前端页面：任务 11 覆盖。
- 本地运行：任务 4、13、14 覆盖。
- 测试验收：任务 2 到任务 14 均包含测试或验收步骤。
- Kubernetes 迁移边界：任务 13 通过 Dockerfile、环境变量和健康检查打基础。

红线扫描：

- 计划中没有未完成标记或空白任务。
- 每个任务都有明确文件、命令、预期结果和 commit 步骤。

范围控制：

- 本计划只覆盖第一阶段本地完整跑通。
- Kubernetes YAML、Prometheus、Grafana、Kafka 和 FastAPI 推荐服务拆分不进入本计划。
