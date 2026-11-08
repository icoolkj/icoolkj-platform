# 实现状态摘要

> 更新：2026-07-18 | 详规：[AI-CONTEXT.md](AI-CONTEXT.md) · [docs/README.md](README.md)

本文只保留 **落地摘要**；细节与禁止事项见 AI-CONTEXT。

## 已定基线

| 项 | 状态 |
|----|------|
| Flyway | **V1–V4** 空库链；改脚本后删库重建 |
| 平台 i18n | Git `i18n/platform` + DB 可覆盖 |
| 壳层 / 业务页 i18n | Git `i18n/shell`、`i18n/gen` 双端 |
| 菜单多语言 | `sys_menu.name_i18n`（无 `sys_menu_locale`） |
| 租户 / 区划 / 部门 | V2.0 定稿（见 [23](architecture/23-region-org.md)） |
| 认证权限 / 多租户 | 已落地 |
| OA L0 字段配置 | 已落地；L1+ 暂缓（[25](architecture/25-oa-form-field-editor.md)） |
| 工作流地基 | Design SoT + 启动变量（[27](architecture/27-workflow-foundation-contracts.md) / [29](architecture/29-workflow-design-json-vs-bpmn.md)） |
| 等保三级应用面 | **≈76%**（部分计半）；对照 [security-mlps-l3-alignment.md](security-mlps-l3-alignment.md) |

## 等保三级（应用面 · 摘要）

| 状态 | 说明 |
|------|------|
| 已对齐 | **10** 项：租户策略、MFA、三权、审计覆盖/保护（哈希链+归档）、会话互斥、数据完整性等（含 Opaque Refresh / 配置加密 / CORS / 解锁） |
| 部分对齐 | **6** 项：TLS、上传魔数、PII 加密、异地备份、剩余信息文档化、界面脱敏 |
| 未对齐 | **1** 项：程序可信验证 |
| 后续 | 上传魔数、国密、OPS（TLS/异地/WAF）等 |

## 后续（非阻塞）

| 项 | 说明 |
|----|------|
| 区划数据权限 P2 | `data_scope` 6/7/8 |
| 单位下级租户代管 P2 | UI 创建子租户 |
| Excel 导入 / 导出 | 用户管理为金样例（导出脱敏 + 分批导入）；契约见 [35-excel-export-import.md](architecture/35-excel-export-import.md) |
| Docker 全量编排文档化 | compose 已有，新人指引待补 |

## 维护

- 新能力：改 V1/V2 + 对应规范分册 + AI-CONTEXT 禁止项（如有）
- 等保能力变更：同步 [security-mlps-l3-alignment.md](security-mlps-l3-alignment.md)
- 冲突时：以 `migration/` 与 AI-CONTEXT 为准
