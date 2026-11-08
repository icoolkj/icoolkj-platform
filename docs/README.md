# iCoolKJ Platform 文档中心

> 架构规范 **V2.2** | 更新：2026-07-17 | 上游：[README.md](../README.md)

平台已进入 **稳态基线**（Flyway V1–V4、Git i18n、`sys_menu.name_i18n`）。文档以 **少而准** 为准：过程/审计类分册已下线，真值优先读 AI-CONTEXT。

## 快速入口

| 读者 | 从这里开始 |
|------|-----------|
| **AI / 速查** | **[AI-CONTEXT.md](AI-CONTEXT.md)** |
| **新人** | [01 定位](architecture/01-overview.md) → [23 区划与租户](architecture/23-region-org.md) → [13 多租户](architecture/13-multi-tenant.md) |
| **落库 / 部署** | [database/README](../backend/database/README.md) → [11 初始化](architecture/11-initialization.md) → [configuration.md](configuration.md) |
| **i18n** | [i18n-standard.md](i18n-standard.md) |
| **等保三级（应用面）** | **[security-mlps-l3-alignment.md](security-mlps-l3-alignment.md)** |
| **进度摘要** | [implementation-status.md](implementation-status.md) |

> 数据库：`backend/database/migration/` **V1–V4**；平台文案 Git `i18n/platform/`；菜单名 `sys_menu.name_i18n`。

---

## 架构分册（保留）

| 编号 | 文档 | 摘要 |
|------|------|------|
| 01 | [项目定位](architecture/01-overview.md) | 平台定位与基线结论 |
| 02 | [技术栈](architecture/02-tech-stack.md) | 前后端、中间件 |
| 03 | [架构原则](architecture/03-principles.md) | 十五大原则（含配置驱动） |
| 04 | [目录结构](architecture/04-directory-structure.md) | 仓库与模块职责 |
| 05 | [命名与元数据](architecture/05-naming-conventions.md) | 包名、版权、Maven |
| 06 | [认证与权限](architecture/06-auth-permission.md) | JWT、RBAC、租户边界 |
| 07 | [数据库规范](architecture/07-database-conventions.md) | 主键、唯一索引含 `deleted` |
| 08 | [API 规范](architecture/08-api-conventions.md) | 前缀、登录 `tenantCode` |
| 09 | [核心模块](architecture/09-core-modules.md) | system / infra / biz |
| 10 | [部署架构](architecture/10-deployment.md) | Docker、Nginx 拓扑 |
| 11 | [初始化](architecture/11-initialization.md) | V1–V4、默认账号、菜单树 |
| 12 | [扩展指南](architecture/12-extension-guide.md) | 业务表与模块扩展 |
| 13 | [多租户](architecture/13-multi-tenant.md) | 一账号一租户、数据分类 |
| 14 | [IP 授权](architecture/14-ip-access-rule.md) | IP 规则与登录安全 |
| 15 | [菜单与权限](architecture/15-menu-permission.md) | M/C/F、`assign_scope`、`name_i18n` |
| 16 | [日志脱敏](architecture/16-log-desensitization.md) | 敏感字段规则 |
| 17 | [国产化兼容](architecture/17-domestic-compatibility.md) | 抽象边界 |
| 20 | [Maven POM](architecture/20-maven-pom-design.md) | 模块依赖与版本 |
| 22 | [application 配置](architecture/22-application-config.md) | `icoolkj.*` 配置域 |
| 23 | [区划与租户](architecture/23-region-org.md) | **V2.0 定稿** |
| 24 | [工作流跨租户](architecture/24-workflow-cross-tenant.md) | 三层租户、L2 组件 |
| 25 | [OA 字段配置](architecture/25-oa-form-field-editor.md) | L0 表格编辑器；L1 暂缓 |
| 27 | [工作流地基契约](architecture/27-workflow-foundation-contracts.md) | Design SoT + 启动变量 |
| 29 | [Design JSON vs BPMN](architecture/29-workflow-design-json-vs-bpmn.md) | UI 吃 JSON；BPMN 编译 |
| 31 | [工作流业务菜单](architecture/31-workflow-biz-menu.md) | 租户菜单与权限归属 |
| 32 | [官网与租户站](architecture/32-site-portal.md) | `site/` / `portal/` |
| 33 | [文件附件约定](architecture/33-file-attachment.md) | AttachmentManager / 目录槽位 / file_ref |
| 34 | [平台定时任务](architecture/34-platform-job.md) | `sys_job*` vs `infra_async_task*`；菜单：定时任务 / 任务日志 |
| 35 | [Excel 导出/导入](architecture/35-excel-export-import.md) | ExportRow 白名单；Gen `isExport`；Import 分通道 |
| 36 | [表单设计器 SDK](architecture/36-form-designer-sdk.md) | V1.1 双模式；OA-Bound `layout_json`；沙箱 ≠ 生产 |
| 37 | [AI 异步任务](architecture/37-ai-async-task.md) | `infra_async_task` + Catalog；业务只写 Handler |
| 38 | [产品宇宙 Shell](architecture/38-product-universe-console-shell.md) | `sys_product` / `product_code`；顶栏切换 + 四概览 |

## 专题（非编号）

| 文档 | 摘要 |
|------|------|
| [configuration.md](configuration.md) | Profile、环境变量 |
| [deployment-linux-host.md](deployment-linux-host.md) | 宿主机生产部署 |
| [security-mlps-l3-alignment.md](security-mlps-l3-alignment.md) | **等保三级应用面对齐**（已落地/缺口/波次） |
| [i18n-standard.md](i18n-standard.md) | shell / platform / gen / 菜单 |
| [workflow-design-nodes.md](workflow-design-nodes.md) | 设计节点能力表 |
| [AI-CONTEXT.md](AI-CONTEXT.md) | 单页真值 |
| [implementation-status.md](implementation-status.md) | 落地摘要 |
| [gen-spec/](../gen-spec/README.md) | 低代码页规范（正文在仓库根 `gen-spec/`） |

**已下线（勿再引用）：** 18 实施建议、19 后端骨架、21 类清单、26/28 钉钉审计、30 配置审计清单、`docs/gen-page-spec.md` 跳转页。

---

## 按角色

- **后端**：06 → 13 → 23 → 07 → [database](../backend/database/README.md) → 04 / 09 / 20  
- **前端**：15 → 08 → 04 → [i18n-standard](i18n-standard.md)  
- **业务扩展**：12 → 09 → [AI-CONTEXT](AI-CONTEXT.md)  
- **OA / 工作流**：24 → 25 → 27 → 29 → 31 → [节点表](workflow-design-nodes.md)

---

## 数据库

| 文件 | 说明 |
|------|------|
| [database/README.md](../backend/database/README.md) | 目录与快速开始 |
| [migration/README.md](../backend/database/migration/README.md) | V1–V4 契约 |
| `V1__schema.sql` | 全量 DDL |
| `V2__platform_seed.sql` | 平台/演示种子（菜单含 `name_i18n`） |

---

## 规范版本

| 版本 | 要点 |
|------|------|
| V2.2 | 架构规范基线 |
| 租户 V2.0 | 企业/单位、区划、部门纯树（23） |
| DB | **V1–V4**；平台文案 Git；菜单 `name_i18n` |

[项目 README](../README.md) · [LICENSE](../LICENSE.txt)
