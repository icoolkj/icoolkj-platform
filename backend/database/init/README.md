# 数据库初始化指南

## 一键初始化

```bash
mysql -u root -p < backend/database/init/00_reset_database.sql
cd backend && mvn -pl icoolkj-admin-api -am clean compile -DskipTests
# 启动 icoolkj-admin-api，Flyway 自动执行 V1–V2
```

## 初始化内容

| 阶段 | Flyway | 内容 |
|------|--------|------|
| 建表 | V1 | 全量 DDL（用户/角色/菜单/`name_i18n`/字典/日志/生成器/AI/工作流/OA/i18n 语言目录等） |
| 平台种子 | V2 | 演示租户、区划、账号、菜单（含 `name_i18n`）、角色授权、字典/参数/IP/公告 |

平台固定文案在 Git `i18n/platform/`（不进 Flyway）。菜单多语言在 `sys_menu.name_i18n`（无 `sys_menu_locale`）。

## 默认账号

| 项 | 值 |
|----|-----|
| 平台租户 | `tenant_id=0`，登录 `tenantCode=platform` |
| 超级管理员 | `admin` / `StudyOnly@2026` |
| 企业租户管理员 | `tenant_admin` / `StudyOnly@2026`（`tenantCode=default`） |
| 单位租户管理员 | `org_admin` / `StudyOnly@2026`（`tenantCode=demo_org`） |

强制改密由 `security.password.force-change-initial`（租户策略可覆盖，缺省回落平台；种子默认 `false`）与用户 `must_change_password` 共同判定；种子账号默认不强制。

## 可选手工种子

基线完成后可导入（不进 Flyway），见 [../seed/README.md](../seed/README.md)。

速查：[migration/README.md](../migration/README.md) · [docs/AI-CONTEXT.md](../../../docs/AI-CONTEXT.md)
