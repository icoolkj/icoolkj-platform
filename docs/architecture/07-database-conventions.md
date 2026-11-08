# 数据库规范

> 版本：V2.2 | 模块：database-conventions | 上游：[docs/README.md](../README.md)

## 概述

本篇定义主键、审计、租户字段、唯一索引（含 `deleted`）、状态枚举及表前缀规范。

---

## 主键规范

`varchar(20)` 雪花字符串，Java `String`，前端 `string`。

---

## 审计字段规范

主业务表统一五元审计字段。纯关联表、日志流水表可按轻量模型处理。

```sql
created_by, created_at, updated_by, updated_at, deleted
```

**审计 ≠ 数据归属。** `created_by` / `updated_by` 只记录谁写过库，**默认不作**行级数据权限（L3）列。

### 归属字段（业务 owner）

| 字段 | 语义 | L3 |
|------|------|-----|
| `dept_id` | 归属部门 | 是 |
| `user_id` | 归属业务用户 | 是 |
| `created_by` | 审计创建人 | 默认否 |

示例：`infra_file` 用 `user_id` 表示上传人，并走标准审计五元；禁止 `uploaded_by` / `uploaded_at`。

域别名列（`starter_user_id` / `sender_id` / `operator_id`）写入时同步标准归属列：

| 表 | 域列 | 标准归属 |
|----|------|----------|
| `oa_instance` | `starter_user_id` | `user_id` + `dept_id` |
| `wf_biz_process` | `starter_user_id` | `user_id` + `dept_id` |
| `sys_site_message` | `sender_id` | `user_id` |
| `infra_async_task` | — | `user_id` |
| `tool_ai_chat_session` / `tool_ai_ddl_record` | `operator_id` | `user_id` |

子表继承父行归属。目录/配置类表不加 `user_id`/`dept_id`。

**用户发起 vs 系统作业：** 用户 API 创建必须写真实归属并走 `OwnershipColumns`。平台定时跑批写入 `sys_job_log`（手动触发记 `trigger_user_id`，Cron 为空），**禁止**写入 `infra_async_task` 冒充异步任务。

**禁止** `@DataScope(userColumn="created_by")` / `updated_by`：审计列不是 L3。框架 `DataScopeColumns.assertLegal` 会直接拒绝。

---

## 租户字段规范

| 类型 | 定义 |
|------|------|
| 租户私有表 | `tenant_id varchar(20) not null` |
| 平台+租户覆盖表 | `tenant_id varchar(20) not null default '0'` |
| 平台全局表 | 无 `tenant_id`（`sys_tenant`、`sys_menu`） |

> 平台级统一 `tenant_id='0'`，禁止用 `null`。

---

## 唯一索引策略（V2.1）

**租户内唯一 + 逻辑删除可重复**，唯一索引统一包含 `deleted`：

```sql
unique key uk_sys_user_tenant_username (tenant_id, username, deleted)
unique key uk_sys_role_tenant_role_code (tenant_id, role_code, deleted)
unique key uk_sys_config_tenant_config_key (tenant_id, config_key, deleted)
unique key uk_sys_menu_permission_code (permission_code, deleted)
unique key uk_sys_tenant_tenant_code (tenant_code, deleted)
```

业务表扩展：

```sql
unique key uk_xxx (tenant_id, 业务字段, deleted)
```

---

## 用户安全字段（V2.1 新增）

`sys_user` 补充：

| 字段 | 说明 |
|------|------|
| `is_super_admin` | 是否超级管理员 |
| `login_fail_count` | 连续登录失败次数 |
| `locked_until` | 锁定截止时间 |
| `must_change_password` | 是否必须修改密码 |
| `user_type` | `system` / `platform` |

`sys_role` 补充：`is_super_role`

---

## 状态字段规范

| 字段 | 值 |
|------|-----|
| `status` | `0` 正常 `1` 停用 |
| `visible` | `0` 显示 `1` 隐藏 |
| `menu_type` | `M` 目录 `C` 菜单 `F` 按钮 |

---

## 规范速查表

| 类别 | 规范 | 禁止事项 |
|------|------|---------|
| 唯一索引 | 含 `deleted` | 禁止不含 deleted 的业务唯一索引 |
| 平台级 | `tenant_id='0'` | 禁止用 null |
| 主键 | `varchar(20)` | 禁止 BIGINT 自增 |

---

## 相关文档

- [13 多租户设计](13-multi-tenant.md)
- [15 菜单与权限](15-menu-permission.md)
- [12 扩展指南](12-extension-guide.md)

---

## 落地检查清单（Checklist）

- [ ] 所有业务唯一索引含 `deleted`
- [ ] 平台级 `tenant_id='0'`
- [ ] `sys_user` 含安全相关字段
- [ ] 日志表参数字段注释标明「脱敏后」
- [ ] 复杂 SQL 集中在 Mapper XML，业务层不拼接方言 SQL
- [ ] 需 L3 的业务表有 `user_id`/`dept_id`；目录/配置表没有
- [ ] `@DataScope` 不用 `created_by`/`updated_by`；配置表不标 `@DataScope`

---

## 国产化数据库兼容注意（V2.2）

后期可能适配达梦、KingbaseES、openGauss、GaussDB、OceanBase、TiDB 等国产数据库，当前 DDL 基于 MySQL 8，实施时需注意：

| 项 | 建议 |
|----|------|
| SQL 方言 | 优先 ANSI SQL，避免 MySQL 专属函数渗入业务层 |
| `datetime(3)` | 各库精度语法可能不同，Flyway 可按库类型拆分脚本 |
| `on update current_timestamp(3)` | 自动更新语法需方言适配 |
| 唯一索引与 `NULL` | 各库对 NULL 唯一性处理可能不同 |
| 分页 | 分页语法集中在 Mapper 层，预留方言切换点 |
| JSON | 按目标库能力评估，避免业务层强依赖 MySQL JSON 函数 |

详见 [17 国产化兼容设计](17-domestic-compatibility.md)。
