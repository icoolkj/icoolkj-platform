# 业务扩展指南

> 版本：V2.2 | 模块：extension-guide | 上游：[docs/README.md](../README.md)

## 概述

本篇说明业务模块扩展原则，V2.1 要求唯一索引含 `deleted`。

---

## 扩展原则

1. 主键 `varchar(20)` 雪花字符串
2. 业务表 `tenant_id varchar(20) not null`
3. 完整审计五元字段
4. 唯一业务键：`uk_(tenant_id, 业务字段, deleted)`
5. API `/api/biz/*`，权限 `模块:资源:动作`

---

## 业务表通用字段模板

```sql
id varchar(20) not null comment '主键ID',
tenant_id varchar(20) not null comment '租户ID',
-- 业务字段 ...
created_by varchar(20) null comment '创建人',
created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
updated_by varchar(20) null comment '更新人',
updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
deleted tinyint(1) not null default 0 comment '是否删除',
primary key (id),
unique key uk_pm_project_code (tenant_id, project_code, deleted),
key idx_pm_project_tenant_id (tenant_id)
```

---

## 扩展示例

| 领域 | 表名 | API |
|------|------|-----|
| 项目管理 | `pm_project` | `/api/biz/pm/project` |
| 客户管理 | `crm_customer` | `/api/biz/crm/customer` |
| 知识库 | `kb_article` | `/api/biz/kb/article` |
| 工单 | `wk_ticket` | `/api/biz/wk/ticket` |

---

## 禁止事项

- 禁止省略 `tenant_id`
- 禁止唯一索引不含 `deleted`
- 禁止一用户跨租户
- 禁止用菜单可见性代替权限码校验

---

## 相关文档

- [13 多租户设计](13-multi-tenant.md)
- [07 数据库规范](07-database-conventions.md)
- [15 菜单与权限](15-menu-permission.md)

---

## 落地检查清单（Checklist）

- [ ] 业务表含 `tenant_id` 与审计字段
- [ ] 唯一索引格式 `(tenant_id, field, deleted)`
- [ ] 租户插件对业务表生效
- [ ] 按钮权限在 `sys_menu` 注册为 `F` 资源
