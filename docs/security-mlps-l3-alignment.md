# 等保三级对齐（应用安全）

> 更新：2026-07-18（W2 严格审计 5 项落地后重对齐）  
> 对照：GB/T 22239-2019 三级 · 安全计算环境「应用安全」  
> 产品形态：**多租户 + 国际化** 后台管理框架（`icoolkj-admin-*` / `frontend`）  
> 范围：**不含**网络/主机/物理/管理制度全册  
> 口径：已对齐 = 可演示/可配置证明；部分对齐 = 主体具备仍缺增强；未对齐 = 应用硬缺口；运维举证 = 主要靠部署与制度

非测评机构结论。应用面对齐 ≠ 等保通过。

---

## 0. 多租户职责模型（本平台对齐前提）

| 角色 | 范围 | 安全相关能力 |
|------|------|----------------|
| **平台管理员**（`tenant_id=0`） | 全平台基线 | 平台参数管理、平台 IP 规则、菜单模板、租户开通；设定 **平台默认** 安全策略 |
| **租户管理员**（`tenant_admin` / `org_admin` 等） | **本租户** | **租户策略配置**覆盖登录/密码/IP/MFA 等；本租户 IP 规则；本租户用户/角色/在线踢人；本租户审计日志只读 |
| **三权角色**（`tenant_sys_admin` / `tenant_sec_admin` / `tenant_audit_admin`） | **本租户分权** | 系统 / 安全 / 审计职责分离；同一用户不可同时持有两个及以上三权角色 |
| **普通业务用户** | 本租户业务 | 受租户策略与 RBAC 约束 |

### 配置生效链

```text
租户 sys_config 覆盖  →  平台 sys_config(tenant_id=0)  →  application.yml 兜底
```

- 入口：菜单 **系统管理 → 租户策略配置**（`/system/tenantPolicy`，权限 `system:tenantPolicy:*`，`assign_scope=TENANT`）
- API：`/system/tenant-policy/*`；仅清理 **本租户** Redis 配置缓存
- 平台「参数管理」仍为 `PLATFORM`，负责写入全局默认；租户不可改平台专属键

### 租户管理员可覆盖的安全键（`ConfigKeyRegistry.tenantOverridable=true`）

| 配置键 | 说明 |
|--------|------|
| `security.login.max-fail-count` | 账号连续失败锁定阈值 |
| `security.login.lock-minutes` | 账号锁定分钟数 |
| `security.login.max-fail-count-per-ip` | IP 失败限流阈值 |
| `security.login.ip-lock-minutes` | IP 锁定分钟数 |
| `security.login.require-captcha` | 是否启用登录验证码 |
| `security.login.allow-multi-login` | 是否允许多端登录 |
| `security.login.single-login-kickout-oldest` | 单端时是否踢旧会话 |
| `security.password.expire-days` | 密码有效天数 |
| `security.password.force-change-initial` | 强制改初始密码 |
| `security.password.min-length`（默认 10）/ `require-upper` / `require-lower` / `require-number` / `require-special` | 复杂度（默认全开） |
| `security.password.initial` | 标准初始密码（新建留空/重置密码用；敏感；可租户覆盖） |
| `security.password.history-count` | 密码历史禁止重用次数 |
| `security.mfa.enabled` | 本租户是否启用 TOTP MFA |
| `security.mfa.force-admin` | 超管 / 超级角色 / 三权角色强制绑定 MFA |
| `security.ip.default-policy` | IP 无命中默认 allow/deny |

另：本租户 **IP 访问规则**（`sys_ip_access_rule`）由租户管理员维护；**平台级禁止规则优先于租户允许**（不可被租户绕过）。

### 国际化

- 管理端壳层 / 租户策略页文案：Git **前后端双份** `i18n/gen/tenantPolicy/`（`zh-CN` / `en-US`）
  - FE：`frontend/src/i18n/gen/tenantPolicy/`
  - BE：`backend/icoolkj-admin-system/src/main/resources/i18n/gen/tenantPolicy/`（含 `{gen.tenantPolicy.*}` 校验文案）
- 平台错误键：双端 `i18n/platform/`；壳层：双端 `i18n/shell/`
- 等保能力与租户策略页均需在中英文下可操作、可审计

---

## 1. 对齐概况

| 状态 | 数量 | 说明 |
|------|------|------|
| 已对齐 | **10** | 含租户策略、MFA、三权、审计覆盖/保护、哈希链完整性 |
| 部分对齐 | **6** | Token/TLS、上传、PII 落库加密、备份异地、剩余信息文档化、界面脱敏 |
| 未对齐 | **1** | 程序可信验证（W3） |
| 运维举证 | **1** | WAF/主机/告警等 |

**应用面对齐度（粗算）**：部分项计半、不含运维举证 ≈ **(10 + 3) / 17 ≈ 76%**。

---

## 2. 已落地能力清单

| 能力 | 落地证据 | 租户可配？ |
|------|----------|------------|
| 多端登录互斥 | `ConcurrentLoginOptions`；种子默认 `allow-multi-login=false` | **是** |
| **Opaque Refresh** | Redis 存 refresh 哈希轮换；`POST /auth/refresh`；FE 401 单飞续期；复用旧 refresh → 吊销会话 | — |
| **sys_config 敏感值加密** | `enc:v1:` + AES-GCM（`SensitiveConfigCrypto`）；管理端仍掩码 | 写入敏感键自动加密 |
| **审计哈希链 + 库内归档** | 三类 live 表 `prev_hash`/`row_hash`；`*_archive`；Worker RetentionJob 先归档再删 live | — |
| **CORS 白名单** | `icoolkj.security.cors`；空 origins = 不注册拒绝策略（Vite 代理可用），永不 `*`；prod `CORS_ALLOWED_ORIGINS` | — |
| **用户仅解锁** | `PUT /system/user/{id}/unlock`；清 DB + Redis 失败计数 + 踢会话 | `system:user:unlock` |
| 强制改密 | `ForcePasswordChangeFilter` | force-change-initial **是** |
| 验证码 | V2/prod 默认开 | **是** |
| 密码历史 | `sys_user_password_history` | **是** |
| Security Headers / XSS | `SecurityConfig` + `XssFilter` | — |
| 限制 query `access_token` | 仅下载路径 | — |
| 在线用户强制下线 | `/monitor/online` | 本租户 **是** |
| 审计留存（prod） | live ≥180d（登录/操作）；接口 90d；archive 更长 | — |
| 审计防擅自删 | 日志 API 无删除；仅 RetentionJob 归档后清理 | — |
| 生产关 Swagger | prod 关闭 | — |
| **租户安全策略自助** | `/system/tenant-policy` + `ConfigKeyRegistry` | **是（核心）** |
| **管理用户 MFA (TOTP)** | `/auth/mfa/verify`；`ForceMfaEnrollFilter` | **是** |
| **三权角色模板** | 开户 bootstrap + SOD 互斥 | 模板平台种子 |

> Flyway **V1–V4**（哈希列与 archive 在 V1）：本地验证需 **删库重建**。

---

## 2.1 安全审计子系统（当前真值）

```text
写路径                    保护                         留存
───────                   ────                         ────
登录日志 ──┐              租户级 SHA-256 链           live → *_archive → 超期删 archive
操作日志 ──┼─ AuditIntegrityService ── Redis 链尾     Worker: AuditLogRetentionJob
接口日志 ──┘              SensitiveDataMasker 脱敏     prod: live 180/180/90；archive 365/365/180
                          管理端无 DELETE
```

| 项 | 现状 | 缺口 / 备注 |
|----|------|-------------|
| 覆盖面 | 登录 / 操作（`@OperationLog`）/ 接口访问 | — |
| 租户隔离 | 写入与查询带 `tenant_id` | — |
| 完整性 | 三类均写 `prev_hash`/`row_hash`；归档 purge 后对剩余 live 行 `reanchor`（自 GENESIS） | 只读校验：`GET /system/operationLog/integrity/verify`、`GET /system/loginLog/integrity/verify`、`GET /infra/apiAccessLog/integrity/verify` |
| 归档 | 库内镜像表 + `archived_at` | 非外置 WORM/对象存储；异地备份属 OPS |
| 脱敏 | 请求/响应体敏感字段掩码 | 管理界面字段脱敏仍属后续 |
| 权限 | 三权 `tenant_audit_admin` 日志只读；无 clear | — |
| 调度 | worker `sys_job`（`audit-log-retention`） | — |

测评举证建议：造一条操作日志 → 调 integrity/verify → 展示 archive 表与 Retention 配置。

---

## 3. 完整控点对照

| 领域 | 控点 | 三级要求要点 | 状态 | 现状证据 | 剩余动作 |
|------|------|--------------|------|----------|----------|
| 身份鉴别 | 唯一标识 + 口令策略 | 复杂度、定期更换、失败处理 | **已对齐** | 租户可覆盖；密码历史；强制改密；解锁清 Redis | — |
| 身份鉴别 | 管理用户双因素 | 两种以上鉴别技术 | **已对齐** | TOTP；租户可强制管理员 | — |
| 访问控制 | RBAC + 租户 + 数据权限 | 最小权限、默认拒绝 | **已对齐** | DataScope；IP 平台优先 | 区划 scope P2 |
| 访问控制 | 特权分离（三权） | 系统/安全/审计分离 | **已对齐** | 三权模板 + SOD；`tenant_admin` 应急上帝角色 | 测评用独立三权账号 |
| 访问控制 | 生产暴露面 | 关闭调试入口 | **已对齐** | prod 关 Swagger | — |
| 访问控制 | 租户自治安全策略 | 管辖范围可配 | **已对齐** | 租户策略页 | — |
| 安全审计 | 审计覆盖 | 覆盖鉴别与重要操作 | **已对齐** | 三类日志 + 脱敏 + 租户隔离 | — |
| 安全审计 | 审计保护与留存 | 防擅自删；≥6 月 | **已对齐** | 无删 API；哈希链；live≥180；archive | OPS：异地/WORM 可选 |
| 会话管理 | 会话互斥 / 强制下线 | 会话可控 | **已对齐** | 多端互斥；在线踢人；改密/解锁踢会话 | — |
| 会话管理 | Token / 通信加固 | 超时、传输保密 | **部分对齐** | Opaque Refresh；CORS；Headers；XSS | **OPS：TLS** |
| 入侵防范 | 漏洞与告警 | 修补、告警 | **运维举证** | 登录/IP 限流 | WAF 等 |
| 恶意代码 | 上传防护 | 防恶意代码 | **部分对齐** | 扩展名策略 | 魔数/杀毒 |
| 可信验证 | 程序完整性 | 完整性检测 | **未对齐** | 无 | W3 |
| 数据完整性 | 关键数据完整性 | 审计完整性 | **已对齐** | 三类哈希链；操作日志校验 API | 可补登录/接口校验 API |
| 数据保密性 | 存储与传输保密 | 存储加密 | **部分对齐** | BCrypt；MFA/配置 AES-GCM；日志脱敏 | PII 列加密；国密 |
| 数据备份恢复 | 备份 | 本地+异地 | **部分对齐** | 本地 Backup | OPS：异地 |
| 剩余信息保护 | 清除 | 释放前清除 | **部分对齐** | 会话/黑名单/文件 GC | 文档化流程 |
| 个人信息保护 | 展示与导出 | 脱敏 | **部分对齐** | 日志脱敏 | 界面/导出脱敏 |

---

## 4. 剩余工作队列

### W1（已完成）

| ID | 项 | 状态 |
|----|-----|------|
| W1-7 | 三权角色模板 | **已完成** |
| W1-8 | 管理用户 MFA (TOTP) | **已完成** |

### W2（严格审计 5 项 · 已完成）

| ID | 项 | 状态 |
|----|-----|------|
| W2-Refresh | Opaque Refresh + FE 单飞续期 | **已完成** |
| W2-ConfigEnc | `sys_config` 敏感键 `enc:v1:` | **已完成** |
| W2-AuditChain | 哈希链 + `*_archive` + Retention + 操作日志校验 | **已完成** |
| W2-CORS | CORS 白名单配置化 | **已完成** |
| W2-Unlock | `system:user:unlock` + 清 Redis 失败计数 | **已完成** |

### 后续（非阻塞）

| 优先级 | 项 |
|--------|-----|
| 应用可选 | 登录/接口日志 integrity 校验 API；上传魔数；界面脱敏；PII 落库加密 |
| W3 | 程序可信验证；国密 |
| OPS | TLS 证书；异地备份；WAF/主机加固 |

新增租户可管配置：登记 `ConfigKeyRegistry.tenantOverridable` + 双端 `i18n/gen/tenantPolicy`。

---

## 5. 关键代码与配置索引

| 主题 | 路径 |
|------|------|
| Opaque Refresh | `RefreshTokenService`、`AuthTokenIssuer`、`AuthController` `/auth/refresh`；FE `request.ts` |
| 配置加密 | `SensitiveConfigCrypto`、`AesGcmSecretCipher`；`ConfigAdminServiceImpl` / `TenantPolicyConfigServiceImpl` / `AiConfigServiceImpl` |
| 审计链 | `AuditIntegrityService`；`LoginLogServiceImpl` / `OperationLogRecordListener` / `ApiAccessLogRecordListener` |
| 链校验 | `AuditChainVerifyService`；`GET /system/operationLog/integrity/verify` |
| 归档留存 | `AuditLogRetentionJob`；`icoolkj.logging.retention.*` |
| CORS | `SecurityProperties.Cors`；`SecurityConfig.corsConfigurationSource`；prod `CORS_ALLOWED_ORIGINS` |
| 用户解锁 | `UserController` `/unlock`；`LoginSecurityPolicyService.clearLoginFailCounts` |
| 租户策略 | `TenantPolicyConfigController`；`ConfigKeyRegistry` |
| MFA / 三权 | `MfaServiceImpl`；`SodRoleGuard`；`TenantRoleBootstrapService` |
| 种子 | `V1__schema.sql`（哈希列/archive）；`V2__platform_seed.sql`（安全键/菜单含 unlock） |

相关规范：[06 认证与权限](architecture/06-auth-permission.md) · [13 多租户](architecture/13-multi-tenant.md) · [14 IP 与登录安全](architecture/14-ip-access-rule.md) · [16 日志脱敏](architecture/16-log-desensitization.md) · [configuration.md](configuration.md)

---

## 6. 维护约定

1. 安全能力变更：同步更新 **本文** + [AI-CONTEXT.md](AI-CONTEXT.md) / [implementation-status.md](implementation-status.md)。
2. 新增「租户可管」配置：必须在 `ConfigKeyRegistry` 设 `tenantOverridable=true`，并 **同步** 前后端 `i18n/gen/tenantPolicy` 的 `keys` 中英文标签。
3. 表结构变更：改 Flyway **V1–V4**（勿臆造 V5+），改后删库重建。
4. 测评演示：分别用 **平台管理员** 与 **租户管理员** 各走一遍策略配置与登录安全行为；三权演示勿用同一人兼三角色；审计举证走三类日志 + integrity/verify + archive 表。
5. 前端 `shell.config.json` 的 `blockDevtools` 仅为 **威慑/体验层**（注入脚本清 token 并跳转），**不算**等保可信计算或防逆向控制；生产安全不得依赖该开关。
