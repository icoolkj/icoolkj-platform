# 官网与租户站目录规范

> 版本：V1.0 | 模块：site-portal | 上游：[docs/README.md](../README.md)

## 概述

仓库内两套**前台 Web 应用**与管理端 `frontend/` 并列，职责分离：

| 目录 | 应用 | 品牌 | 租户 |
|------|------|------|------|
| [`site/`](../../site/README.md) | 平台官网（产品 / 文档 / 许可） | 固定 **iCoolKJ Platform**；**zh-CN / en-US** | 无 |
| [`portal/`](../../portal/README.md) | 租户前台 / 白标 | 运行时解析 | Host / 查询参数 → `tenantCode` |
| `frontend/` | 管理后台 Admin | `sys_brand_pack`（可 OEM） | 登录 `tenantCode` + JWT |

## 仓库位置

```text
icoolkj-platform/
  frontend/     # 管理端（已有）
  site/         # 平台官网
  portal/       # 租户站（一套多租户）
  backend/
  docs/
```

## 硬性约束

1. **禁止** `portal/tenants/{code}/` 或按租户复制工程  
2. **禁止** 官网文案依赖演示环境 OEM 品牌  
3. **禁止** `site` / `portal` 引用 `frontend/src` 业务组件（Gen、工作流设计器等）  
4. 控制台入口以配置外链为准（默认 `https://platform.example.com/`）  
5. **官网 `site/`** 文案与页面结构对齐框架约定：`*.json` 国际化 + 各页 `view.json`（营销 schema，非 `gen-view`）；见 [`site/README.md`](../../site/README.md)

## 本地端口（开发）

| 应用 | 默认端口 |
|------|----------|
| Admin `frontend` | 5173 |
| `site` | 5174 |
| `portal` | 5175 |

## 相关

- 多租户：[13 多租户设计](13-multi-tenant.md)  
- 仓库总目录：[04 目录结构](04-directory-structure.md)  
