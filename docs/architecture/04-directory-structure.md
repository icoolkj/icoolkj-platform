# 目录结构

> 版本：V2.2 | 模块：directory-structure | 上游：[docs/README.md](../README.md)

## 概述

本篇定义仓库、前端、后端的目录组织方式与各模块职责边界，确保多项目复制时结构一致。

---

## 仓库目录

```text
icoolkj-platform/
  frontend/     # 前端管理端（Admin）
  site/         # 平台官网（产品 / 文档 / 许可）— 见 [32](32-site-portal.md)
  portal/       # 租户前台 / 白标（一套多租户）— 见 [32](32-site-portal.md)
  backend/      # 后端服务
  docs/         # 产品、架构、数据库、接口、部署文档
  deploy/       # Docker、Nginx、Compose、环境配置模板
  scripts/      # 本地开发、打包、部署辅助脚本
```

---

## 前端目录结构

```text
frontend/
  src/
    api/
    assets/
    components/
    directives/
    layouts/
    router/
    stores/
    composables/
    utils/
    styles/
    i18n/
      index.ts          # 平台 vue-i18n（Git shell + platform；可选 DB frontend）
      shell/            # 壳层 common/layout/login
      platform/         # error/entity.name/validation/notify 基线
      gen/              # 业务页文案
      gen/              # 代码生成 CRUD 页 Git 基线
        {businessName}/
          view.json     # 布局：query / table / form（与语言无关）
          zh-CN.json
          en-US.json
    views/
      login/
      dashboard/
      system/
      tenant/       # V2 新增：租户管理
      monitor/
      tool/
      biz/
    types/
    constants/
  public/
  index.html
  package.json
  vite.config.ts
  tsconfig.json
```

### 前端职责边界

| 目录 | 职责 | 禁止事项 |
|------|------|---------|
| `api/` | 封装 HTTP 请求，对接后端 API | 不得包含业务 UI 逻辑 |
| `views/system/` | 平台系统管理页面 | 不得放置业务领域页面 |
| `views/tenant/` | 租户管理页面（V2） | 不得与 system 用户管理混淆 |
| `views/biz/` | 业务模块页面 | 不得直接操作平台 stores |
| `stores/` | 全局状态（用户、权限、租户、字典） | 不得存储页面级临时状态 |

管理端与 **`site/`（平台官网）**、**`portal/`（租户站）** 分离，见 [32 官网与租户站目录](32-site-portal.md)。

---

## 后端目录结构

```text
backend/
  pom.xml
  icoolkj-admin-api/          # 启动类、Controller、OpenAPI、应用装配
  icoolkj-admin-worker/       # 异步任务消费者、定时任务、文件处理
  icoolkj-admin-common/       # 通用常量、枚举、工具、异常、统一响应
  icoolkj-admin-framework/    # 安全、租户、Redis、MyBatis、日志、AOP、ID、全局配置
  icoolkj-admin-system/       # 用户、角色、菜单、部门、岗位、字典、参数、公告、日志、租户、IP规则、平台定时任务(sys_job)
  icoolkj-admin-infra/        # 文件存储、业务异步任务、第三方集成、缓存能力
  icoolkj-admin-workflow/     # Flowable 工作流（定义、实例、跨租户支持）
  icoolkj-admin-oa/           # OA 办事 / 表单字段配置（L0）
  icoolkj-admin-generator/    # 代码生成（Gen CRUD）
  icoolkj-admin-biz/          # 业务模块
```

### 后端模块职责

| 模块 | 职责 | 依赖方向 |
|------|------|---------|
| `icoolkj-admin-api` | HTTP 入口、应用启动、OpenAPI | 依赖 system、infra、workflow、oa、biz、generator |
| `icoolkj-admin-worker` | 异步消费、平台定时任务执行（`JobRunner`） | 依赖 infra、system、biz |
| `icoolkj-admin-common` | 公共工具、异常、响应体 | 无业务依赖 |
| `icoolkj-admin-framework` | 安全、**租户**、缓存、MyBatis 配置 | 依赖 common |
| `icoolkj-admin-system` | 系统管理领域（含租户、IP 规则） | 依赖 framework |
| `icoolkj-admin-infra` | 基础设施领域 | 依赖 framework |
| `icoolkj-admin-workflow` | 工作流引擎与流程编排 | 依赖 framework（Flowable） |
| `icoolkj-admin-oa` | OA 办事域（表单/实例） | 依赖 workflow、infra、framework |
| `icoolkj-admin-generator` | 代码生成工具 | 依赖 framework（由 api 装配） |
| `icoolkj-admin-biz` | 业务扩展 | 依赖 system、infra |

### 后端职责边界与禁止事项

| 规则 | 说明 |
|------|------|
| `biz` 不得依赖 `api` 的 Controller | 业务 Service 独立于 HTTP 层 |
| `system` 不得依赖 `biz` | 平台层不感知业务 |
| `common` 不得依赖任何业务模块 | 保持最底层无环依赖 |
| 租户上下文通过 `TenantContextHolder` 读取 | 禁止在 Controller 硬编码 tenantId |
| Controller 只做参数校验与调用 Service | 不得在 Controller 写业务逻辑 |

---

## 相关文档

- 上一篇：[03 架构原则](03-principles.md)
- 下一篇：[05 命名与元数据](05-naming-conventions.md)
- 多租户：[13 多租户设计](13-multi-tenant.md)
- 核心模块：[09 核心模块](09-core-modules.md)

---

## 落地检查清单（Checklist）

- [ ] 仓库包含 `frontend/`、`backend/`、`docs/`、`deploy/`、`scripts/` 五个顶层目录
- [ ] 前端 `views/tenant/` 放置租户管理页面
- [ ] 后端 `icoolkj-admin-framework` 包含租户上下文与 MyBatis-Plus 租户插件
- [ ] 模块依赖方向符合「平台不依赖业务」原则
- [ ] 新增业务代码仅放在 `icoolkj-admin-biz` 模块
