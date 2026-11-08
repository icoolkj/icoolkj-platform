# 技术栈

> 版本：V2.2 | 模块：tech-stack | 上游：[docs/README.md](../README.md)

## 概述

本篇定义 iCoolKJ Platform 前后端及基础中间件的技术选型，所有子项目应遵循此技术栈，避免引入异构框架。

---

## 前端技术栈

| 技术 | 用途 |
|------|------|
| Vue 3 | 前端框架 |
| TypeScript | 类型安全 |
| Vite | 构建工具 |
| Element Plus | UI 组件库 |
| Pinia | 状态管理 |
| Vue Router | 路由管理 |
| Axios | HTTP 客户端 |
| vue-i18n | 国际化 |
| ECharts | 图表可视化 |

---

## 后端技术栈

| 技术 | 用途 |
|------|------|
| Java 17 | 运行时 |
| Spring Boot 3 | 应用框架 |
| Spring Security 6 | 认证与授权 |
| JWT | 访问令牌载体 |
| MyBatis-Plus | ORM 增强 |
| MyBatis XML | 复杂 SQL |
| Flyway | 数据库版本迁移 |
| Hibernate Validator | 参数校验 |
| Jackson | JSON 序列化 |
| Lombok | 代码简化 |
| Springdoc OpenAPI | API 文档 |

---

## 基础中间件

| 中间件 | 用途 |
|--------|------|
| MySQL 8 | 主数据库 |
| Redis | 缓存、会话、权限缓存 |
| Redis Stream | 异步消息队列 |
| MinIO | 对象存储（可替换 OSS） |
| Nginx | 反向代理、静态资源 |
| Docker / Docker Compose | 容器化部署 |

---

## 国产化兼容目标

当前默认技术栈用于快速落地，架构设计必须预留后期国产化适配能力，兼容以下方向：

| 类别 | 兼容方向 |
|------|---------|
| 国产数据库 | 达梦、KingbaseES、openGauss、GaussDB、OceanBase、TiDB |
| 国产操作系统 | 银河麒麟、统信 UOS、openEuler |
| 国产 CPU 架构 | 鲲鹏、飞腾、龙芯等 |
| 国产第三方能力 | 对象存储、消息队列、短信、邮件、电子签章、OCR 等 |

> 业务层禁止深度绑定具体中间件实现，详见 [17 国产化兼容设计](17-domestic-compatibility.md)。

---

## 相关文档

- 上一篇：[01 项目定位](01-overview.md)
- 下一篇：[03 架构原则](03-principles.md)
- 目录结构：[04 目录结构](04-directory-structure.md)
- 部署：[10 部署架构](10-deployment.md)
- 国产化：[17 国产化兼容设计](17-domestic-compatibility.md)

---

## 落地检查清单（Checklist）

- [ ] 前端 `package.json` 使用 Vue 3 + Vite + TypeScript
- [ ] 后端 `pom.xml` 使用 Java 17 + Spring Boot 3
- [ ] 数据库迁移使用 Flyway，不使用手工 DDL 变更
- [ ] 对象存储默认 MinIO，通过配置可切换 OSS
- [ ] API 文档使用 Springdoc OpenAPI 自动生成
