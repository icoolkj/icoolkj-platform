# application.yml 配置骨架设计

> 版本：V2.2 | 模块：application-config | 上游：[docs/README.md](../README.md)

## 概述

本篇定义 iCoolKJ Platform `backend/` 工程初始化阶段的配置基线：环境拆分、`icoolkj.*` 配置域、命名规范与 `@ConfigurationProperties` 绑定类。

| 属性 | 说明 |
|------|------|
| 适用项目 | iCoolKJ Platform |
| API 配置路径 | [icoolkj-admin-api/src/main/resources/](../../backend/icoolkj-admin-api/src/main/resources/) |
| Worker 配置路径 | [icoolkj-admin-worker/src/main/resources/](../../backend/icoolkj-admin-worker/src/main/resources/) |
| 关联文档 | [configuration.md](../configuration.md)、[04 目录结构](04-directory-structure.md) |

---

## 1. 配置文件拆分

> **运维速查**：[configuration.md](../configuration.md)（Profile、环境变量、shared 分层）。  
> 以下为配置域设计说明；实际文件布局已演进为 common shared + 模块 profile。

```text
icoolkj-admin-common/src/main/resources/
  application-icoolkj-shared.yml
  application-icoolkj-shared-dev.yml
  application-icoolkj-shared-test.yml
  application-icoolkj-shared-prod.yml

icoolkj-admin-api|worker/src/main/resources/
  application.yml
  application-dev.yml
  application-test.yml   # 仅 api
  application-prod.yml
  logback-spring.xml
```

原则：

- 公共骨架在 `application.yml`
- 环境差异仅在 profile 文件
- 自定义配置统一收口 `icoolkj.*`
- 敏感密钥不进仓库明文（生产用环境变量）

---

## 2. 配置域总览

| 域 | 前缀 | 职责 |
|----|------|------|
| Spring 原生 | `server.*` / `spring.*` | 端口、数据源、Redis、Jackson |
| MyBatis-Plus | `mybatis-plus.*` | Mapper、逻辑删除、主键策略 |
| OpenAPI | `springdoc.*` | API 文档（生产可关闭） |
| 安全 | `icoolkj.security` | 放行 URL、登录重试、多端登录 |
| JWT | `icoolkj.jwt` | 签发、过期、Header |
| 租户 | `icoolkj.tenant` | 开关、平台租户、忽略表/URL |
| 缓存 | `icoolkj.cache` | key 前缀、各域 TTL |
| 文件 | `icoolkj.storage` | MinIO/OSS 等 provider 配置 |
| 异步 | `icoolkj.async` | 环境只配 `provider`；Worker 另配 `consume: true`；队列/重试/DLQ 见 Java 默认 |
| 第三方 | `icoolkj.provider` | 短信/邮件/OCR/签章 |
| 日志 | `icoolkj.logging` | 访问/操作日志、脱敏字段 |

---

## 3. API 模块要点

- `server.servlet.context-path: /api`
- `spring.profiles.active: dev`
- **不跑平台定时任务**（调度只在 worker：`@Scheduled` + `JobRunner`）
- `icoolkj.security.ignore-urls` 含 `/auth/login`、Swagger、健康检查

配置文件：

- [application.yml](../../backend/icoolkj-admin-api/src/main/resources/application.yml)
- [application-dev.yml](../../backend/icoolkj-admin-api/src/main/resources/application-dev.yml)
- [application-test.yml](../../backend/icoolkj-admin-api/src/main/resources/application-test.yml)
- [application-prod.yml](../../backend/icoolkj-admin-api/src/main/resources/application-prod.yml)

---

## 4. Worker 模块要点

- `spring.application.name: icoolkj-admin-worker`
- `server.port: 8081`，`context-path: /worker`
- **平台定时任务执行端**：`ManagedJobCronDispatcher`（按 `sys_job.cron_expression`）→ `JobRunner` → `JobHandler` → `sys_job_log`；立即执行经 Redis List
- **业务异步任务消费端**：Worker `icoolkj.async.consume=true` → `AsyncTaskStreamPoller` → Redis Stream → `DefaultAsyncTaskExecutor`（详见 [34-platform-job.md](34-platform-job.md)）

---

## 5. 环境差异摘要

| 项 | dev | test | prod |
|----|-----|------|------|
| SQL 日志 | stdout | 关闭 | 关闭 |
| JWT secret | 本地占位 | `${JWT_SECRET}` | `${JWT_SECRET}` |
| Swagger | 开启 | 开启 | **关闭** |
| 第三方能力 | 默认关闭 | 按需 | 环境变量控制 |
| 多端登录 | 允许 | 默认 | **禁止** |

---

## 6. @ConfigurationProperties 绑定类

| 类 | 前缀 | 模块 |
|----|------|------|
| `JwtProperties` | `icoolkj.jwt` | framework |
| `TenantProperties` | `icoolkj.tenant` | framework |
| `CacheTtlProperties` | `icoolkj.cache` | framework |
| `SecurityProperties` | `icoolkj.security` | framework |
| `LoggingProperties` | `icoolkj.logging` | framework |
| `StorageProperties` | `icoolkj.storage` | infra |
| `AsyncProperties` | `icoolkj.async` | infra |
| `ProviderProperties` | `icoolkj.provider` | infra |

自动配置入口：

- `FrameworkAutoConfiguration`（framework 层属性）
- `InfraAutoConfiguration`（infra 层属性，避免 framework 依赖 infra）

---

## 7. 命名规范

- 布尔：`enabled`、`strict-mode`、`allow-multi-login`
- 时间：统一 `*-seconds`
- 集合：`ignore-urls`、`sensitive-fields`
- Provider：`default-channel`、`access-key`、`secret-key`

---

## 8. 第一批必落地配置项

1. `spring.datasource`
2. `spring.data.redis`
3. `mybatis-plus`
4. `icoolkj.security`
5. `icoolkj.jwt`
6. `icoolkj.tenant`
7. `icoolkj.cache`
8. `icoolkj.storage`
9. `icoolkj.async`
10. `icoolkj.logging`

---

## 相关文档

- [14 IP 授权](14-ip-access-rule.md)
- [16 日志脱敏](16-log-desensitization.md)
- [06 认证与权限](06-auth-permission.md)

---

## 落地检查清单（Checklist）

- [ ] 四环境 profile 文件齐全
- [ ] 生产密钥通过环境变量注入
- [ ] `icoolkj.*` 无散落魔法 TTL
- [ ] Properties 类与 YAML 前缀一致
- [ ] framework 不依赖 infra 配置类
- [ ] prod 关闭 Swagger 暴露
