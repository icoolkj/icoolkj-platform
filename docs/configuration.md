# 配置与环境变量说明

> 适用：`backend/icoolkj-admin-api`、`backend/icoolkj-admin-worker`  
> 关联：[22 application 配置域](architecture/22-application-config.md)（设计骨架）、[10 部署架构](architecture/10-deployment.md)

本文说明 **Profile 切换方式**、**配置文件分层** 与 **生产/测试环境变量**，便于本地开发与 Linux 部署。

---

## 1. 配置文件分层

后端配置分为 **公共层（common）** 与 **模块层（api / worker）**，避免数据源、Redis、JWT 等重复维护。

```text
backend/icoolkj-admin-common/src/main/resources/
  application-icoolkj-shared.yml           # 公共基线（api + worker）
  application-icoolkj-shared-dev.yml       # dev 环境覆盖
  application-icoolkj-shared-test.yml      # test 环境覆盖
  application-icoolkj-shared-prod.yml      # prod 环境覆盖（含环境变量占位）

backend/icoolkj-admin-api/src/main/resources/
  application.yml                          # api 主配置 + import 链
  application-dev.yml                      # api dev 独有
  application-test.yml
  application-prod.yml

backend/icoolkj-admin-worker/src/main/resources/
  application.yml
  application-dev.yml
  application-prod.yml
```

### 加载顺序（后者覆盖前者）

1. `application-icoolkj-shared.yml`
2. `application-icoolkj-shared-${spring.profiles.active}.yml`（optional）
3. 模块 `application.yml`
4. 模块 `application-${spring.profiles.active}.yml`

api / worker 的 `application.yml` 中统一声明：

```yaml
spring:
  profiles:
    active: dev   # 默认 dev，部署时改为 prod 或通过启动参数覆盖
  config:
    import:
      - classpath:application-icoolkj-shared.yml
      - optional:classpath:application-icoolkj-shared-${spring.profiles.active}.yml
```

---

## 2. Profile 用法

| Profile | 用途 | 典型场景 |
|---------|------|----------|
| `dev` | 本地开发 | IDEA 启动、本机 MySQL/Redis |
| `test` | 集成/联调 | CI、测试库（主要 api 使用） |
| `prod` | 生产 | Linux 服务器、Docker、K8s |

### 切换方式

**方式 A：修改 `application.yml`**

```yaml
spring:
  profiles:
    active: prod
```

**方式 B：启动参数（推荐部署使用）**

```bash
# api
java -jar icoolkj-admin-api.jar --spring.profiles.active=prod

# worker
java -jar icoolkj-admin-worker.jar --spring.profiles.active=prod
```

**方式 C：环境变量**

```bash
export SPRING_PROFILES_ACTIVE=prod
```

**方式 D：IDEA Run Configuration**

- VM options：`-Dspring.profiles.active=dev`
- 或 Environment variables：`SPRING_PROFILES_ACTIVE=dev`

### 各 Profile 行为摘要

| 项 | dev | test | prod |
|----|-----|------|------|
| 数据库 | `icoolkj_platform_dev`（本机） | `icoolkj_platform_test` | `${DB_URL}` |
| Redis | 本机 `127.0.0.1:6379` | 测试 Redis | `${REDIS_HOST}` 等 |
| JWT secret | 写死在 shared-dev | `${JWT_SECRET}` | `${JWT_SECRET}` |
| 业务日志级别 | `debug` | `info` | `info` |
| MyBatis SQL 日志（api） | stdout | 关闭 | 关闭 |
| Swagger（api） | 开启 | 开启 | **关闭** |
| 多端登录（api） | 允许 | 默认 | **禁止** |
| 定时 DB 备份（worker） | **关闭** | 按 shared-test | **开启**（默认） |
| 日志保留任务（worker） | 开启 | 开启 | 开启 |

---

## 3. 模块差异（api vs worker）

| 配置项 | api | worker |
|--------|-----|--------|
| 端口 / 路径 | `8080` / `/api` | `8081` / `/worker` |
| Flyway | 开启 | 关闭 |
| Security | 开启 | 关闭 |
| 定时任务 | 无调度 | `ManagedJobCronDispatcher`（`sys_job`） |
| 日志保留天数 | YAML 业务参数 | 同左；执行在 worker 任务 |

Snowflake ID：`icoolkj.id.worker-id` 在 api 为 `1`，worker 为 `2`，请勿在同一环境设为相同值。

---

## 3.1 安全相关配置摘要

### JWT / Refresh

| 属性 | 默认 | 说明 |
|------|------|------|
| `icoolkj.jwt.refresh-token-expire-seconds` | `604800`（7 天） | Opaque refresh 与会话 TTL 对齐 |
| `POST /auth/refresh` | — | 在 ignore-urls 中；轮换 refresh，复用旧 token 吊销会话 |

### CORS

| 属性 | 默认 | 说明 |
|------|------|------|
| `icoolkj.security.cors.allowed-origins` | `[]` | 空 = **不注册**拒绝型 CORS 策略（Vite 代理/同源可用）；真正跨域需显式配置；**永不**用 `*` |
| `allow-credentials` | `true`（有白名单时） | 仅白名单非空时生效 |

CSRF：应用层关闭（无 Cookie Session）；浏览器调用依赖 CORS 白名单 + Bearer JWT。反向代理/Vite 代理同源部署时 `allowed-origins` 可留空。

### 审计留存（`icoolkj.logging.retention` 天数；调度见 `sys_job` / `audit-log-retention`）

| 属性 | 默认 | 说明 |
|------|------|------|
| `login-log-days` / `operation-log-days` | `180` | live 表保留天数（≥6 月口径） |
| `api-access-log-days` | `90` | 接口日志 live 保留 |
| `archive-*-days` | `365` / `365` / `180` | 归档表再保留天数；到期删 archive |

流程：到期 live **INSERT archive → DELETE live**；更旧 archive 再 purge。完整性校验：`GET /system/operationLog/integrity/verify`、`GET /system/loginLog/integrity/verify`、`GET /infra/apiAccessLog/integrity/verify`。

---

## 4. 环境变量一览

### 4.1 生产必配（shared-prod）

| 变量 | 说明 | 示例 |
|------|------|------|
| `DB_URL` | MySQL JDBC URL | `jdbc:mysql://db:3306/icoolkj_platform?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false` |
| `DB_USERNAME` | 数据库用户名 | `icoolkj` |
| `DB_PASSWORD` | 数据库密码 | — |
| `REDIS_HOST` | Redis 主机 | `redis` |
| `REDIS_PASSWORD` | Redis 密码 | — |
| `JWT_SECRET` | JWT 签名密钥（建议 ≥ 32 字节） | — |

### 4.2 生产可选（shared-prod，有默认值）

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `REDIS_PORT` | `6379` | Redis 端口 |
| `REDIS_DB` | `0` | Redis 库索引 |
| `DB_BACKUP_OUTPUT_DIR` | `/var/lib/icoolkj/db-backup` | 备份文件目录 |
| `MYSQLDUMP_COMMAND` | `mysqldump` | mysqldump 可执行文件（PATH 或绝对路径） |
| `AUDIT_LOG_CORE_POOL_SIZE` | `4` | 审计日志异步线程池核心数 |
| `AUDIT_LOG_MAX_POOL_SIZE` | `16` | 最大线程数 |
| `AUDIT_LOG_QUEUE_CAPACITY` | `5000` | 队列容量 |
| `AUDIT_LOG_AWAIT_TERMINATION_SECONDS` | `20` | 关闭时等待秒数 |
| `AUDIT_LOG_THREAD_NAME_PREFIX` | `audit-log-` | 线程名前缀 |
| `CORS_ALLOWED_ORIGINS` | （空） | 浏览器跨域白名单；空则不允许 `*`（同源/网关反代） |

### 4.3 测试环境（shared-test）

| 变量 | 说明 |
|------|------|
| `DB_PASSWORD` | 测试库密码 |
| `REDIS_PASSWORD` | 测试 Redis 密码 |
| `JWT_SECRET` | 测试 JWT 密钥 |

测试库地址由 `application-icoolkj-shared-test.yml` 中的 `DB_URL` / `REDIS_HOST` 环境变量注入，仓库内仅保留 `*.example.com` 占位域名，不含任何真实内网地址。

### 4.4 api 生产独有（application-prod.yml）

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `SMS_ENABLED` | `false` | 短信 Provider |
| `MAIL_ENABLED` | `false` | 邮件 Provider |
| `OCR_ENABLED` | `false` | OCR Provider |
| `SIGN_ENABLED` | `false` | 电子签章 Provider |

Storage（MinIO/OSS）等仍在 api `application.yml` 的 `icoolkj.storage.*` 中配置；生产建议改为环境变量或独立 secret 管理，勿将密钥提交仓库。

生产 profile（`application-icoolkj-shared-prod.yml`）默认开启 `icoolkj.storage.security.malware-scan.enabled=true`（ClamAV，`fail-closed`）；部署时需提供可达的 ClamAV，或显式关闭该开关。

代码生成：`icoolkj.generator.enabled`（控制器装配）与 `icoolkj.generator.disk-write-enabled`（`/tool/gen/genCode` 写盘）。prod 默认 **禁止写盘**（ZIP 下载仍可用）；DB/Redis/JWT 凭据用环境变量，dev 默认值仅在 `application-icoolkj-shared-dev.yml`。

---

## 5. 本地开发默认值（dev）

无需设置环境变量即可启动（需本机 MySQL / Redis 可用）：

| 项 | 值 |
|----|-----|
| 数据库 | `jdbc:mysql://127.0.0.1:3306/icoolkj_platform_dev` |
| DB 用户/密码 | 由 `DB_USER` / `DB_PASSWORD` 环境变量提供 |
| Redis | `127.0.0.1:6379`，密码由 `REDIS_PASSWORD` 提供 |
| JWT secret | `dev-jwt-secret-change-me-at-least-32-bytes` |
| 备份输出目录 | `./data/db-backup` |

### Windows 开发注意：mysqldump

本机若 `mysqldump` 不在 PATH，可在 **api dev** 或本地 `application-local.yml`（自行添加，勿提交）中指定：

```yaml
icoolkj:
  backup:
    mysqldump-command: "C:/Program Files/MySQL/MySQL Server 8.0/bin/mysqldump.exe"
```

Linux 生产默认使用 PATH 中的 `mysqldump`，或通过 `MYSQLDUMP_COMMAND` 指定绝对路径。

---

## 6. 日志文件

| 项 | 说明 |
|----|------|
| 配置项 | `logging.file.path`（shared 中为 `logs`） |
| api 日志 | 进程工作目录下 `logs/icoolkj-admin-api.log` |
| worker 日志 | `logs/icoolkj-admin-worker.log` |
| 滚动策略 | 按天归档，保留 30 天，总上限 2GB |

IDEA 从模块目录启动时，日志通常在：

- `backend/icoolkj-admin-api/logs/`
- `backend/icoolkj-admin-worker/logs/`

`logs/` 已在 `.gitignore` 中忽略。

---

## 7. 数据库备份相关配置

配置前缀：`icoolkj.backup`（绑定类 `DatabaseBackupProperties`）

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `enabled` | `false`（shared）；dev api `true`；dev worker `false` | 是否启用备份能力/调度 |
| `cron` | `0 0 2 * * ?` | worker 定时任务（每天 02:00） |
| `output-dir` | `./data/db-backup` | 备份文件目录 |
| `retention-days` | `14` | 过期文件清理天数 |
| `mysqldump-command` | `mysqldump` | 可执行文件路径 |
| `compress` | `true` | 是否 gzip 压缩 |

- **手动备份**：管理端「数据库备份」菜单，由 **api** 进程调用 mysqldump。
- **定时备份**：由 **worker** 在 prod 下按 cron 执行（dev 下 worker 关闭以免重复）。

---

## 8. 生产部署示例

```bash
export SPRING_PROFILES_ACTIVE=prod

export DB_URL="jdbc:mysql://127.0.0.1:3306/icoolkj_platform?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false"
export DB_USERNAME=icoolkj
export DB_PASSWORD='your-db-password'

export REDIS_HOST=127.0.0.1
export REDIS_PASSWORD='your-redis-password'

export JWT_SECRET='your-production-jwt-secret-at-least-32-chars'

export DB_BACKUP_OUTPUT_DIR=/var/lib/icoolkj/db-backup
export MYSQLDUMP_COMMAND=mysqldump

# 仅 api 需要时
export SMS_ENABLED=false
export MAIL_ENABLED=false

java -jar icoolkj-admin-api.jar &
java -jar icoolkj-admin-worker.jar &
```

Docker / systemd 部署时将上述变量写入 compose、`EnvironmentFile` 或 K8s Secret。

---

## 9. 新增配置时的约定

1. **api 与 worker 都用的**：写入 `application-icoolkj-shared*.yml`。
2. **仅某一模块**：写入对应模块的 `application.yml` 或 `application-{profile}.yml`。
3. **敏感信息**：prod 使用 `${ENV_VAR}`，不要明文进 Git。
4. **自定义业务配置**：统一放在 `icoolkj.*` 前缀下，并配套 `@ConfigurationProperties`（见 [22-application-config.md](architecture/22-application-config.md)）。

---

## 10. 演示环境（只读）

配置前缀：`icoolkj.demo`（`DemoModeProperties` / `DemoModeFilter`）

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `enabled` | `false` | 开启后拦截 POST/PUT/PATCH/DELETE |
| `allow-write-urls` | `/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/password` | 演示环境下仍允许写操作的路径 |

**启用方式**

```bash
# api 启动参数（不要改 application.yml 默认 dev，用 Run Configuration 或命令行切换）
--spring.profiles.active=demo
```

会自动加载 `application-icoolkj-shared-demo.yml`，其中包含：

| 项 | 本地默认 | 生产演示 |
|----|----------|----------|
| `icoolkj.demo.enabled` | `true` | `true` |
| `spring.datasource.*` | `icoolkj_platform_dev` / root | `${DB_URL}` 等环境变量 |
| `icoolkj.jwt.secret` | 内置 demo 占位 | **必须** `${JWT_SECRET}` |

`shared-demo` 与 `shared-dev` 一样是一层 **完整可运行** 的环境覆盖，不是只写 `demo.enabled` 一项。

**行为**

- 后端：`DemoModeFilter` 返回 403 + `error.demo.operation_forbidden`（「演示环境，禁止操作！」）
- 前端：`GET /system/config/bootstrap` 返回 `demoMode: true`，顶栏显示警告条；`GenCrudPage` / `GenTreeCrudPage` 自动只读

公网演示建议配合**独立演示库**，必要时定时从种子库还原。

---

## 11. 常见问题

**Q：改了 profile 但没连上预期数据库？**  
确认启动日志中的 `The following profiles are active: xxx`，以及是否被 `SPRING_PROFILES_ACTIVE` 或命令行参数覆盖。

**Q：shared-dev 没加载？**  
检查 `spring.config.import` 是否包含 `optional:classpath:application-icoolkj-shared-${spring.profiles.active}.yml`，且 `profiles.active` 与文件名后缀一致（`dev` / `prod` / `test`）。

**Q：dev 下 worker 和 api 都会跑定时备份吗？**  
不会。调度只在 worker，启停/cron 由 `sys_job`（`database-backup`）控制。

**Q：Flyway 在哪个进程执行？**  
仅在 **api**（`spring.flyway.enabled: true`）。worker 不负责迁移。
