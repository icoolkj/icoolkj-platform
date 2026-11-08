# Maven 父子模块 POM 设计

> 版本：V2.2 | 模块：maven-pom-design | 上游：[docs/README.md](../README.md)

## 概述

本篇定义 iCoolKJ Platform `backend/` 工程 Maven 父子模块 `pom.xml` 的正式设计基线，统一依赖版本、模块边界与构建方式，为多租户、安全、国产化替换提供稳定工程底座。

| 属性 | 说明 |
|------|------|
| 适用项目 | iCoolKJ Platform |
| 技术基线 | Java 17 / Spring Boot 3.2.5 / Spring Security 6 / MyBatis-Plus / Redis / JWT |
| 工程路径 | [backend/](../../backend/) |
| 关联文档 | [04 目录结构](04-directory-structure.md)、[09 核心模块](09-core-modules.md) |

---

## 1. 模块结构

```text
backend/
  pom.xml                          # 父工程（聚合 + 版本管理）
  icoolkj-admin-api/pom.xml        # API 启动与 Controller
  icoolkj-admin-worker/pom.xml     # Worker 启动与异步消费
  icoolkj-admin-common/pom.xml     # 公共模块
  icoolkj-admin-framework/pom.xml  # 框架层
  icoolkj-admin-system/pom.xml     # 系统域
  icoolkj-admin-infra/pom.xml      # 基础设施适配层
  icoolkj-admin-workflow/pom.xml   # 工作流（Flowable）
  icoolkj-admin-oa/pom.xml         # OA 办事
  icoolkj-admin-generator/pom.xml  # 代码生成
  icoolkj-admin-biz/pom.xml        # 业务扩展层
```

模块依赖关系：

```text
api -> system, infra, workflow, oa, generator, framework, common, biz
worker -> system, infra, framework, common, biz
biz -> system, infra, framework, common
oa -> workflow, infra, framework, common
workflow -> framework, common (+ Flowable)
generator -> framework, common（由 api 装配）
system -> framework, common
infra -> framework, common
framework -> common
common -> 无业务依赖
```

原则：

- `common` 只放纯公共能力
- `framework` 放平台运行时基础设施
- `system` 放通用系统域能力
- `infra` 放外部中间件与第三方抽象实现
- `workflow` / `oa` 放流程与办事能力（平台能力包，非项目定制 biz）
- `generator` 放代码生成工具
- `biz` 放具体业务模块
- `api` 和 `worker` 作为启动装配层

> 父工程 `<modules>` 按编译依赖顺序排列：`common` → `framework` → `system` → `infra` → `workflow` → `oa` → `biz` → `api` / `worker` → `generator`。

---

## 2. 父工程 `backend/pom.xml`

父工程职责：

- 聚合所有子模块
- 统一版本号与 `dependencyManagement`
- 统一插件管理（compiler、spring-boot、surefire）
- 统一编码、Java 17、打包规则

关键属性：

| 属性 | 版本 |
|------|------|
| Spring Boot | 3.2.5 |
| Spring Cloud | 2023.0.1 |
| MyBatis-Plus | 3.5.5 |
| JJWT | 0.12.5 |
| Redisson | 3.27.2 |
| Springdoc | 2.5.0 |
| MapStruct | 1.5.5.Final |
| Hutool | 5.8.27 |
| MySQL Connector/J | 8.3.0 |
| MinIO | 8.5.10 |

父工程额外收口：

- 内部模块（`icoolkj-admin-*`）纳入 `dependencyManagement`，子模块引用时省略版本号
- `minio` 版本统一由父工程管理

完整文件见：[backend/pom.xml](../../backend/pom.xml)

---

## 3. 子模块 POM 摘要

### 3.1 icoolkj-admin-common

常量、枚举、异常、工具、统一响应体。不引入 Web、数据库、中间件重依赖。

依赖：`spring-boot-starter`、`lombok`、`hutool`、`jackson`、`jakarta.validation-api`、`commons-lang3`

### 3.2 icoolkj-admin-framework

Security、JWT、Redis、MyBatis-Plus、租户、AOP、日志、ID 生成等平台运行时基础层。

依赖：`common` + `web` + `security` + `validation` + `aop` + `data-redis` + `redisson` + `mybatis-plus` + `jjwt`

### 3.3 icoolkj-admin-system

用户、角色、菜单、租户、字典、参数、日志、IP 规则等系统域。

依赖：`common` + `framework` + `validation` + `mapstruct`

### 3.4 icoolkj-admin-infra

文件存储、异步任务总线、第三方 Provider、访问日志与外部调用日志。

依赖：`common` + `framework` + `minio`（**仅此模块引入 MinIO SDK**）

`spring-boot-starter-web` 使用 `provided` 作用域，避免模块边界失真。

### 3.5 icoolkj-admin-workflow

Flowable 流程定义/实例、跨租户支持、与业务绑定表。

依赖：`common` + `framework` + `flowable`

### 3.6 icoolkj-admin-oa

OA 办事表单字段配置与实例，启动工作流。

依赖：`common` + `framework` + `workflow` + `infra`

### 3.7 icoolkj-admin-generator

代码生成（表元数据 → 前后端脚手架）。

依赖：`common` + `framework`（由 `api` 引入并暴露工具 API）

### 3.8 icoolkj-admin-biz

具体项目业务，依赖系统能力与抽象基础设施。

依赖：`common` + `framework` + `system` + `infra`

### 3.9 icoolkj-admin-api

主启动类、Controller、OpenAPI、对外 REST。含 `spring-boot-maven-plugin`。

依赖：全部平台模块 + `web` + `actuator` + `springdoc` + **`mysql-connector-j`（runtime）**

### 3.10 icoolkj-admin-worker

异步消费、定时任务、后台处理。含 `spring-boot-maven-plugin`。

依赖：全部平台模块（通常不含 generator）+ `actuator` + **`mysql-connector-j`（runtime）**  
调度：Spring `@Scheduled` + `JobRunner`（见 [34-platform-job.md](34-platform-job.md)）；当前无独立调度引擎依赖。

---

## 4. 依赖设计说明

### 4.1 为什么 `api` 和 `worker` 都依赖 `biz`

两者都是启动装配层：`api` 负责对外接口入口，`worker` 负责后台异步入口。业务逻辑不写于启动模块，但需扫描、装配业务 Bean。

### 4.2 为什么 `system` 不依赖 `infra`

系统域应依赖框架层抽象，而非直接依赖外部实现。文件上传走 `FileStorageService`，异步任务走 `AsyncTaskBus`。

更长期可新增 `icoolkj-admin-interfaces` 专门存放跨模块抽象接口。

### 4.3 为什么不建议所有模块都直接依赖 `web`

- `common` 不应依赖 `web`
- `system`、`biz` 原则上只需 Spring 基础能力
- 真正需要 Web 容器的是 `api` 和部分 `framework`

便于后期拆分服务、Worker 独立部署。

### 4.4 数据库驱动放在启动层

`mysql-connector-j` 仅声明在 `api` 与 `worker` 模块（`runtime`），符合国产化下「驱动切换控制在启动层与数据源配置层」原则。后期切换达梦、KingbaseES 等，优先改启动模块与配置，不改 `biz`/`system` 依赖结构。

---

## 5. 国产化兼容下的 POM 约束

1. 不在业务模块直接引入具体厂商 SDK
2. 对象存储 SDK 只进入 `infra`
3. 消息队列 SDK 只进入 `infra`
4. OCR、短信、邮件、电子签章 SDK 只进入 `infra`
5. 数据库驱动切换控制在 `api` / `worker` 与数据源配置层

后期切换 MySQL → 达梦、MinIO → 国产对象存储、Redis Stream → RocketMQ 时，尽量不改 `biz`/`system` 模块依赖结构。

---

## 6. 后续可选优化

### icoolkj-admin-starter

对外输出统一自动配置，便于多项目复用。

### icoolkj-admin-interfaces

存放 `FileStorageService`、`AsyncTaskBus`、`SmsProvider` 等抽象接口，避免 `system`/`biz` 对 `infra` 形成编译期实现依赖。

---

## 7. 构建验证

```bash
cd backend
mvn validate
mvn clean install -DskipTests
```

---

## 相关文档

- [04 目录结构](04-directory-structure.md)
- [09 核心模块](09-core-modules.md)
- [17 国产化兼容设计](17-domestic-compatibility.md)
- [04 目录结构](04-directory-structure.md)

---

## 落地检查清单（Checklist）

- [ ] 父工程 `groupId` 为 `com.icoolkj.platform`，版本 `1.0.0-SNAPSHOT`
- [ ] 七子模块 POM 均已创建且 `parent` 指向正确
- [ ] 子模块间依赖方向符合分层约束（`system` 不依赖 `infra`）
- [ ] MinIO 依赖仅在 `infra` 模块
- [ ] MySQL 驱动仅在 `api` / `worker` 启动模块
- [ ] `mvn validate` 通过
- [ ] 厂商 SDK 未渗入 `common` / `system` / `biz`
