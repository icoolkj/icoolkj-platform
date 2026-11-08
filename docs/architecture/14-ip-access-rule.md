# IP 授权与登录安全

> 版本：V2.2 | 模块：ip-access-rule | 上游：[docs/README.md](../README.md)

## 概述

本篇定义 IP 访问规则模型、执行顺序、平台级优先策略及登录安全配置参数。

---

## IP 授权目标

- 登录 IP 白名单/黑名单
- 后台访问 IP 授权
- 开放接口访问限制
- 平台级与租户级规则
- 临时封禁与授权（`start_at` / `end_at`）

---

## 规则模型

数据表：`sys_ip_access_rule`，`tenant_id='0'` 为平台级。

| 字段 | 值 | 含义 |
|------|-----|------|
| `match_type` | `1`/`2`/`3` | 单IP / IP段 / CIDR |
| `rule_type` | `1`/`2` | 允许 / 禁止 |
| `apply_scope` | `1`/`2`/`3`/`4` | 登录 / 后台 / 开放接口 / 全部 |

---

## 规则执行顺序

1. 查询平台级规则（`tenant_id='0'`）
2. 查询租户级规则
3. 合并，按 `priority_no` **倒序**
4. 命中禁止 → 拒绝
5. 命中允许 → 放行
6. 无命中 → 默认策略

### 安全约束（V2.1）

> **平台级禁止规则优先级高于租户级允许规则**

- 超级管理员**不能**绕过平台级禁止 IP 规则

---

## 登录安全策略参数

纳入 `sys_config`（平台级 `tenant_id='0'`）：

| 参数键 | 说明 | 建议默认值 |
|--------|------|-----------|
| `security.login.max-fail-count` | 连续登录失败最大次数 | `5` |
| `security.login.lock-minutes` | 锁定分钟数 | `30` |
| `security.password.expire-days` | 密码有效天数 | `90` |
| `security.password.force-change-initial` | 强制修改初始密码 | `true` |
| `security.ip.default-policy` | IP 无命中默认策略 | `allow` |

可扩展：

```text
security.login.require-captcha=false
security.password.min-length=10
security.password.require-upper=true
security.password.require-number=true
security.password.require-special=false
```

### 与 sys_user 字段联动

| 字段 | 用途 |
|------|------|
| `login_fail_count` | 失败次数累计 |
| `locked_until` | 达到阈值后锁定截止时间 |
| `must_change_password` | 初始密码/重置后强制改密 |
| `pwd_updated_at` | 配合 `expire-days` 判断过期 |

Redis：`auth:login:fail:{tenantId}:{username}`

---

## 相关文档

- [06 认证与权限](06-auth-permission.md)
- [13 多租户设计](13-multi-tenant.md)
- [11 初始化建议](11-initialization.md)

---

## 落地检查清单（Checklist）

- [ ] 平台禁止规则优先于租户允许规则
- [ ] 超级管理员不能绕过平台级禁止 IP
- [ ] 登录失败达阈值写入 `locked_until`
- [ ] 密码过期与强制改密在登录流程校验
- [ ] 安全参数从 `sys_config` 读取，支持租户覆盖
- [ ] Redis 失败计数与 DB `login_fail_count` 一致
