# Flyway 迁移规范（V1–V4 基线）

> **唯一权威来源**：本目录 `V1`–`V4`。构建时由 `icoolkj-admin-api` 复制到 `classpath:db/migration/`。

## 版本链

| 版本 | 文件 | 职责 |
|------|------|------|
| V1 | `V1__schema.sql` | **全量 DDL**（含 `sys_job` / `sys_ai_skill` / OA `layout_json` 等） |
| V2 | `V2__platform_seed.sql` | 租户/用户/角色/菜单/字典/参数/授权；含平台定时任务；含 `notice_publish`；含表单设计器/办公助手菜单；含 AI Skill 种子 |
| V3 | `V3__attachment_demo.sql` | 附件样例 |
| V4 | `V4__general_office.sql` | **通用办事 10 类**平台基础模板（无 design/审批规则）+ **icoolkj 组织与完整工作流样例** |

平台固定文案（`error.*` 等）在 Git `i18n/platform/`，不进 Flyway seed。

## 空库初始化

```bash
mysql -u root -p < backend/database/init/00_reset_database.sql
cd backend && mvn -pl icoolkj-admin-api -am clean compile -DskipTests
# 启动 icoolkj-admin-api
python scripts/verify_flyway_sql_source.py
```

## 约定

1. **只维护 V1–V4**；平台能力写入基线，**不新增** V5+ 增量版本；升级按 **空库重建**。
2. 修改 migration 后 **删库重建** 验证，不要用 `flyway repair`。
3. 权限码 camelCase；字典字段用 `char(1)` 存 `'0'`/`'1'`。
4. 菜单多语言写在 `sys_menu.name_i18n` JSON；Skill 显示名写在 `sys_ai_skill.name_i18n` / `description_i18n`。
5. 全局目录表（无 `tenant_id`）须加入 `icoolkj.tenant.ignore-tables`（如 `sys_menu`、`sys_ai_skill`）。

### sys_menu ID 分段规则（19 位）

```text
4000 + DD(2) + GG(2) + PP(2) + SS(2) + 0000000(7)
```

| 段 | 含义 |
|----|------|
| DD | 业务域：01 仪表盘 / 10 办公 / 08 流程 / 09 OA配置 / 07 基础设施 / 05 监控 / 04 安全 / 03 租户 / 06 工具 / **02 系统管理（靠后）** |
| GG | 二级目录序号；根目录为 `00` |
| PP | 页面（C）序号；目录节点为 `00` |
| SS | 按钮（F）序号；页面/目录为 `00` |

侧栏排序：常用业务域在前，**系统配置类（系统管理）最后**。系统管理内：组织权限 → 展示与策略 → 平台元数据 → 国际化。
修改种子后需 **删库重建**（Flyway 仅 api 启动执行）。
