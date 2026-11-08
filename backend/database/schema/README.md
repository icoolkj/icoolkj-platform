# DDL 参考分册（只读 / 可能滞后）

> **唯一权威：** `../migration/V1__schema.sql`（及 V2–V4 种子）。本目录**不得**作为建库依据。
>
> 2026-07-25：V1 已对齐 OA 列表索引、`wf_*` 唯一键含 `deleted`、归档表租户索引、站内信收件唯一约束。若本目录与 V1 冲突，以 V1 为准；后续优先废弃或由脚本从 V1 生成同步。

| 文件 | 范围 |
|------|------|
| `sys_tables.sql` | 系统核心表（可能缺租户层次字段） |
| `infra_tables.sql` | 基础设施表 |
| `oa_tables.sql` / `wf_tables.sql` 等 | 业务域拆分参考 |

完整表结构请以 `../migration/V1__schema.sql` 为准。
