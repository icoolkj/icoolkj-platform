# 架构原则

> 版本：V2.2 | 模块：principles | 上游：[docs/README.md](../README.md)

## 概述

本篇列出 iCoolKJ Platform 的十五大核心架构原则，所有设计与实现决策应与此保持一致。

---

## 十五大架构原则

### 1. 前后端分离

前端与后端独立开发、独立部署，通过 RESTful API 通信。前端不直接访问数据库。

### 2. 平台能力与业务能力分离

- **平台层**（`system`、`infra`、`framework`）：用户、权限、租户、字典、日志、文件等通用能力
- **业务层**（`biz`）：项目管理、客户管理等领域特有逻辑

> 平台模块不得依赖业务模块；业务模块可依赖平台模块。

### 3. 统一 ID 规范

全平台主键统一为 `varchar(20)` 雪花算法字符串，Java 类型 `String`，前端类型 `string`。

### 4. 统一权限模型

采用 RBAC（用户-角色-菜单/资源），接口权限通过 `@PreAuthorize` 控制，数据权限通过角色数据范围 + 租户数据范围 + AOP 实现。

### 5. 统一 API 响应格式

所有接口返回 `{ code, message, data }` 结构，`code = 0` 表示成功。

### 6. 统一数据库命名规范

表名使用前缀（`sys_`、`infra_`、`biz_` 等），字段使用 snake_case，审计字段统一命名。

### 7. 统一审计字段

核心业务表必须包含 `created_by`、`created_at`、`updated_by`、`updated_at`、`deleted`。

### 8. 统一租户隔离规范

采用共享库共享表，按 `tenant_id` 字段隔离。平台表分三类：平台全局表、租户私有表、平台+租户覆盖表。详见 [13 多租户设计](13-multi-tenant.md)。

### 9. 统一安全治理能力

提供 IP 白名单/黑名单、平台级与租户级访问规则，支持登录、后台访问、开放接口等场景。详见 [14 IP 授权](14-ip-access-rule.md)。

### 10. 统一部署结构

标准部署包含 Nginx、Frontend、API、Worker、MySQL、Redis、MinIO，配置模板位于 `deploy/`。

### 11. 统一代码组织方式

后端按 Maven 多模块组织，前端按 `api`、`views`、`stores` 等目录组织，包名统一 `com.icoolkj.platform`。

### 12. 支持项目复制式开发

平台可作为模板复制到新项目，通过扩展 `biz` 模块快速启动新业务系统。

### 13. 配置驱动（JSON 优先）

产品行为与用户可见策略以 **JSON / 键值配置** 为源：工作流 `process_config` + `design_json`、OA 字段快照、`sys_config`、字典与 i18n。  
部署 YAML 只承载密钥与中间件。引擎 XML/BPMN、魔法前缀、产品话术不得散落在 Service/Emitter 字面量中——契约类（如 `BpmnElementIds`）与 MessageSource / Git i18n 键。

---

## 相关文档

- 上一篇：[02 技术栈](02-tech-stack.md)
- 下一篇：[04 目录结构](04-directory-structure.md)
- 多租户：[13 多租户设计](13-multi-tenant.md)
- IP 授权：[14 IP 授权](14-ip-access-rule.md)
- 国产化：[17 国产化兼容设计](17-domestic-compatibility.md)
- 数据库规范：[07 数据库规范](07-database-conventions.md)

---

## 落地检查清单（Checklist）

- [ ] 新增功能确认属于平台层还是业务层，放置正确模块
- [ ] 所有实体主键使用 `String` 类型 `varchar(20)`
- [ ] 租户私有表包含 `tenant_id` 字段
- [ ] 所有 API 响应使用统一 `R<T>` 包装类
- [ ] 核心业务表包含完整审计字段
- [ ] 权限标识遵循 `模块:资源:动作` 格式
- [ ] IP 访问规则遵循平台级/租户级分层模型
- [ ] 业务代码不直接 import 具体中间件 SDK
