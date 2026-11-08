# 多租户设计

> 版本：V2.2 | 模块：multi-tenant | 上游：[docs/README.md](../README.md)

## 概述

本篇定义多租户隔离方案、平台级 `tenant_id='0'` 规则、数据分类、参数/字典回退及唯一约束策略。

---

## 默认方案

**共享数据库、共享表、按 `tenant_id` 字段隔离**

---

## 租户类型（V2.3 定稿）

业务租户严格分为两类（见 [23 区划与租户](23-region-org.md)）：

| `tenant_type` | 名称 | 说明 |
|---------------|------|------|
| `ENTERPRISE` | 企业租户 | SaaS 客户、公司；**无** `parent_tenant_id` |
| `ORGANIZATION` | 单位租户 | 政府/事业单位；可有 `parent_tenant_id` 形成单位树 |

共性：

- 均配置 `root_region_id`（服务根区划）
- 组织编制均在租户内 `sys_dept` 部门树
- 一账号一租户不变

---

## 一账号一租户（V2.1 最终约束）

- `sys_user.tenant_id` 直接标识归属租户
- 不引入 `sys_user_tenant` 等多对多表
- 登录时通过 `tenantCode` 定位租户，JWT 固定 `tid`

---

## 平台级租户规则

```text
tenant_id = '0'  →  平台级（禁止使用 null）
```

平台管理员、平台参数、平台字典、平台 IP 规则、平台公告均使用 `tenant_id='0'`。

---

## 租户数据分类

### 1）平台全局表

| 表名 | 说明 |
|------|------|
| `sys_tenant` | 租户主表 |
| `sys_menu` | 平台统一菜单模板（目录/菜单/按钮） |

### 2）租户私有表

`sys_user`、`sys_role`、`sys_dept`、`sys_post`、关联表、全部 `infra_*`、`biz_*` 等，均 `tenant_id not null`。

### 3）平台级 + 租户级覆盖表

`tenant_id='0'` 平台级，具体租户 ID 为租户级：

- `sys_config`、`sys_notice`、`sys_dict_type`、`sys_dict_item`、`sys_ip_access_rule`

---

## 参数读取优先级

```text
1. 先读 tenant_id = 当前租户ID
2. 未命中回退 tenant_id = '0'
```

## 字典读取优先级

同样：优先租户级字典，未命中回退平台级字典。

Redis 缓存：`dict:{tenantId}:{dictType}`

---

## 唯一约束设计（V2.1）

**租户内唯一 + 逻辑删除可重复**，唯一索引统一包含 `deleted`：

| 业务编码 | 唯一索引 |
|---------|---------|
| 租户编码 | `(tenant_code, deleted)` |
| 用户名 | `(tenant_id, username, deleted)` |
| 角色编码 | `(tenant_id, role_code, deleted)` |
| 岗位编码 | `(tenant_id, post_code, deleted)` |
| 部门编码 | `(tenant_id, dept_code, deleted)` |
| 参数键名 | `(tenant_id, config_key, deleted)` |
| 字典类型 | `(tenant_id, dict_type, deleted)` |
| 权限码 | `(permission_code, deleted)` — `sys_menu` 全局唯一 |

目的：逻辑删除后可重建同名数据。

---

## 租户上下文

来源：登录 `tenantCode` → JWT `tid`/`tcode` → Filter → `TenantContextHolder`

---

## 相关文档

- [06 认证与权限](06-auth-permission.md)
- [07 数据库规范](07-database-conventions.md)
- [15 菜单与权限](15-menu-permission.md)

---

## 落地检查清单（Checklist）

- [ ] 无用户-租户多对多表
- [ ] 平台级一律 `tenant_id='0'`
- [ ] 参数/字典读取实现租户优先、平台回退
- [ ] 唯一索引均含 `deleted`
- [ ] 租户插件忽略 `sys_tenant`、`sys_menu`
