# 系统初始化建议

> 版本：V2.2 | 模块：initialization | 上游：[docs/README.md](../README.md)

## 概述

本篇说明平台首次部署初始化清单与 Flyway **V1–V4** 基线规则。改 migration 后 **删库重建**。

---

## 初始化清单

1. 平台管理员语义：`tenant_id='0'`
2. 默认业务租户（企业租户 + 演示单位租户）
3. 国标区划样例（国家 → 省 → 市 → 区县）
4. 平台级部门/岗位（`tenant_id='0'`，供平台管理员归属）
5. 各业务租户根部门/岗位
6. 平台超级管理员用户（`user_type='platform'`，`is_super_admin=1`，归属平台部门）
7. 默认租户管理员用户（归属租户部门与岗位）
8. 平台超级管理员角色（`is_super_role=1`）
9. 默认租户管理员角色、业务开发者角色
10. 平台菜单模板（M/C/F 资源，含区划/租户类型）
11. 基础按钮权限资源（`F` 类型）与角色授权
12. 默认平台字典（含 `sys_tenant_type`、`sys_region_level`）
13. 默认平台参数（含登录安全策略、i18n、AI 能力目录种子连接）
14. 默认平台 IP 规则与公告

---

## 建议菜单树

```text
系统管理
  ├── 租户管理
  ├── 用户管理
  │   ├── [F] 新增 system:user:add
  │   ├── [F] 修改 system:user:update
  │   └── [F] 删除 system:user:delete
  ├── 角色管理
  ├── 菜单管理
  ├── 部门管理
  ├── 岗位管理
  ├── 字典管理
  ├── 参数管理
  └── IP规则管理
```

---

## 初始化脚本

Flyway 基线（空库 V1–V4）：

```text
backend/database/migration/
  V1__schema.sql
  V2__platform_seed.sql      # 平台种子（含 notice_publish）
  V3__attachment_demo.sql
  V4__general_office.sql     # 通用办事平台基础模板 + icoolkj 完整工作流
```

`icoolkj` 测试租户（完整组织 + 10 套工作流）与平台通用办事基础模板已在 V4，无需再执行 `seed/` 手工脚本。

详见 [backend/database/README.md](../../backend/database/README.md)、[AI-CONTEXT.md](../AI-CONTEXT.md)。

### 默认账号

| 账号 | 租户 | 初始密码 |
|------|------|---------|
| `admin` | 平台级 `tenant_id='0'`，登录 `tenantCode=platform` | `StudyOnly@2026` |
| `tenant_admin` | 默认企业租户 `tenantCode=default` | `StudyOnly@2026` |
| `org_admin` | 演示单位租户 `tenantCode=demo_org` | `StudyOnly@2026` |

三账号均 `must_change_password=1`，首次登录需改密。

---

## 初始化数据规则（V2.2）

| 规则 | 说明 |
|------|------|
| 平台级语义 | `tenant_id='0'` 仅作平台级语义，不强制插入 `sys_tenant` |
| 平台组织 | 平台管理员需归属 `tenant_id='0'` 的根部门与岗位 |
| 默认企业租户 | `tenant_code=default`，`tenant_type=ENTERPRISE` |
| 演示单位租户 | `tenant_code=demo_org`，`tenant_type=ORGANIZATION`，根区划=安徽省 |
| 平台管理员 | `admin`，`tenant_id='0'`，`user_type='platform'`，`is_super_admin=1` |
| 租户管理员 | `tenant_admin`，归属默认企业租户 |
| 密码 | BCrypt，`StudyOnly@2026`，`must_change_password=1` |
| 授权原则 | 平台超管全部资源；租户管理员不含 `assign_scope=PLATFORM` 菜单 |
| 平台开户 | 创建业务租户时同一事务：根部门 + 默认岗位 + 超级角色（企业 `tenant_admin` / 单位 `org_admin`）+ 首个管理员；密码取 `security.password.initial`；接口返回 `TenantProvisionVO` 一次性展示明文密码；日常组织由租户管理员自管 |

---

## 建议初始化参数（`tenant_id='0'`）

```text
security.login.max-fail-count=5
security.login.lock-minutes=30
security.password.expire-days=90
security.password.force-change-initial=true
security.login.require-captcha=true
security.password.min-length=10
security.password.require-upper=true
security.password.require-lower=true
security.password.require-number=true
security.password.require-special=true
security.password.initial=StudyOnly@2026
security.ip.default-policy=allow
```

---

## 初始化脚本

```text
backend/database/migration/V1__schema.sql … V2__platform_seed.sql
backend/database/init/00_reset_database.sql
backend/database/README.md
```

---

## 相关文档

- [06 认证与权限](06-auth-permission.md)
- [15 菜单与权限](15-menu-permission.md)
- [14 IP 授权](14-ip-access-rule.md)
- [13 多租户设计](13-multi-tenant.md)
- [AI-CONTEXT.md](../AI-CONTEXT.md) · [migration/README](../../backend/database/migration/README.md)

---

## 落地检查清单（Checklist）

- [ ] 平台超级管理员 `tenant_id='0'`，`user_type='platform'`，已绑定平台根部门/岗位
- [ ] 平台级 `sys_dept` / `sys_post`（`tenant_id='0'`）已初始化
- [ ] 默认企业租户与演示单位租户、区划样例已写入
- [ ] 租户管理员归属默认企业租户，且已绑定部门/岗位
- [ ] 菜单模板含 M/C/F 完整资源
- [ ] 按钮权限 `F` 资源已关联 `role_menu`
- [ ] 平台安全参数已写入 `sys_config`
- [ ] 平台 IP 默认规则已配置
