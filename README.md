# iCoolKJ Platform

**iCoolKJ 通用开发平台** — 面向企业中后台场景的通用开发底座，支持多租户、安全治理、国产化迁移与多业务系统快速复制扩展。源码公开，供学习与研究使用。

| 属性 | 说明 |
|------|------|
| 项目域名 | [icoolkj.com](https://icoolkj.com) |
| 作者 | icoolkj |
| 联系邮箱 | icoolkj@163.com |
| QQ | 594047667 |
| 项目性质 | 源码公开 / Source Available · 非商业学习研究 |
| 许可协议 | [iCoolKJ 非商业学习研究许可证 v1.0](LICENSE.txt) |
| 架构规范版本 | V2.2 |
| 数据库迁移 | Flyway **V1–V5**（权威：`backend/database/migration/`） |

---

## 许可摘要

> ⚠️ **本项目不是 OSI 认证的开源软件。** 它是「源码公开 / Source Available」，采用自定义的非商业许可，明确**禁止任何商业使用**。请勿将本项目描述为 "Open Source"、"开源软件"，也不要用 MIT / Apache / GPL 等名称指代它。详见 [LICENSE.txt](LICENSE.txt)。

| | 说明 |
|---|---|
| ✅ 允许 | 个人学习、自学研究、学术科研、非营利教学；本地或隔离环境运行、阅读、调试、修改；按协议第 4 条非商业分发；技术选型 PoC（≤90 天、不承载真实业务数据） |
| ❌ 禁止 | 任何商业用途：生产部署、处理真实业务数据、企业内部自用系统、对外交付产品/组件/脚手架、SaaS/托管/咨询/实施等有偿服务、收费培训、政府或企业信息化项目、售卖与再许可、去除署名、商标冒用 |
| 📄 商业授权 | 任何商业使用须**事先取得权利人书面授权**并另行签署商业许可协议。联系：icoolkj <icoolkj@163.com> · QQ 594047667 |
| 🛡️ 无担保 | 按「现状」提供，不保证适用于生产环境，不保证符合等保或行业合规要求 |

「商业用途」与「非商业用途」的完整定义、分发条件、终止与争议解决条款，一律以 [LICENSE.txt](LICENSE.txt) 中文正文为准。

---

## 技术栈速览

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3、TypeScript、Vite、Element Plus、Pinia、Vue Router、Axios、vue-i18n、WangEditor |
| 后端 | Java 17、Spring Boot 3、Spring Security 6、JWT、MyBatis-Plus、Flowable、Redisson、Flyway |
| 中间件 | MySQL 8、Redis、Redis Stream、MinIO、Nginx、Docker |

## 核心设计（V2.2）

- 前后端分离，平台能力与业务能力分离
- 认证：Spring Security 6 + JWT + Redis + RBAC + Tenant Context
- 多租户：一账号一租户，`tenant_id='0'` 表示平台级
- 权限：菜单导航与权限码授权分离，按钮级 `F` 资源
- 安全：IP 规则、登录锁定、密码策略、日志脱敏
- 主键：`varchar(20)` 雪花算法字符串 ID
- 唯一索引：统一包含 `deleted` 支持逻辑删除后重建
- 国产化：关键中间件抽象边界，支持后期替换实现

## 快速开始

本仓库包含 `backend`（10 个 Maven 模块）、`frontend`（管理端）、`site`（官网）、`portal`（租户前台骨架）、`deploy`（Docker 编排）与 `docs`（架构文档）。

1. 阅读 [docs/configuration.md](docs/configuration.md)，按 `.env.example` 准备本地环境变量；
2. 参考 [deploy/docker-compose.dev.yml](deploy/docker-compose.dev.yml) 启动 MySQL / Redis / MinIO；
3. 执行 `backend/database/migration/` 下的 Flyway 迁移；
4. 分别启动 `backend/icoolkj-admin-api` 与 `frontend`。

> **安全提醒**：仓库内所有默认口令、密钥与地址均为占位值。任何部署前必须设置强口令、更换 JWT 密钥、轮换 MinIO 凭据，并删除演示账号。演示/测试数据均为虚构值，切勿用于生产。详见 [SECURITY.md](SECURITY.md)。

## 文档导航

完整架构规范请查阅 **[docs/README.md](docs/README.md)**；AI/速查见 **[docs/AI-CONTEXT.md](docs/AI-CONTEXT.md)**。

| 分类 | 入口 |
|------|------|
| 文档中心 | [docs/README.md](docs/README.md) |
| AI 速查 | [docs/AI-CONTEXT.md](docs/AI-CONTEXT.md) |
| 数据库 | [backend/database/](backend/database/) |
| i18n | [docs/i18n-standard.md](docs/i18n-standard.md) |
| 配置 / 部署 | [docs/configuration.md](docs/configuration.md) · [docs/deployment-linux-host.md](docs/deployment-linux-host.md) |
| 实现摘要 | [docs/implementation-status.md](docs/implementation-status.md) |

> 版本口径：V2.2 表示平台架构规范基线；数据库迁移链独立演进，当前以 [backend/database/README.md](backend/database/README.md) 与 `backend/database/migration/` 为准。

## 仓库结构

```text
icoolkj-platform/
  frontend/     # 前端管理端（Admin）
  site/         # 平台官网（营销 / 文档 / 许可说明）
  portal/       # 租户前台 / 白标（骨架，尚未完整落地）
  backend/      # 后端服务（多模块 Maven，见 docs/architecture/20-maven-pom-design.md）
  docs/         # 产品、架构、数据库、接口、部署文档
  deploy/       # Docker、Nginx、Compose 配置
  scripts/      # 本地开发、打包、部署脚本
  gen-spec/     # 低代码页面规范与校验
  tools/        # 代码生成工具
```

官网与租户站目录规范见 [docs/architecture/32-site-portal.md](docs/architecture/32-site-portal.md)。

## 参与贡献与安全

- 提交贡献前请先阅读 [CONTRIBUTING.md](CONTRIBUTING.md)：所有贡献按本仓库许可协议授权，禁止提交真实密钥、生产数据与受限代码。
- 漏洞报告请走 [SECURITY.md](SECURITY.md) 中的私下渠道，不要在公开 issue 中披露。
- 第三方依赖及其许可证见 [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md)。

## 版权

Copyright (c) icoolkj · <https://icoolkj.com>

本项目依 [iCoolKJ 非商业学习研究许可证 v1.0](LICENSE.txt) 提供，署名与声明保留要求见 [NOTICE.txt](NOTICE.txt)。未明确授予的权利均由权利人保留。
