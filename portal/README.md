# portal — 租户前台 / 白标站

> 一套代码、多租户运行时解析。  
> **禁止** `portal/tenants/{code}/` 一租户一目录。

## 定位

| 项 | 说明 |
|----|------|
| 访客 | 租户客户 / 公网（按域名进站） |
| 品牌 | 运行时 `sys_brand_pack` / 租户覆盖行（非写死） |
| 租户 | Host / 子域 / 自定义域 → `tenantCode` |
| 与 Admin | 可跳转登录并预填租户；不嵌入管理端 |
| 与官网 | 平台营销在 `site/`；本应用不做产品授权叙事 |

## 目录

```text
portal/
  public/
  src/
    config/portal.ts      # 默认项与控制台基址
    tenant/               # 租户解析与上下文（关键）
      resolveTenant.ts
      types.ts
    components/layout/
    pages/
      home/               # 租户落地页骨架
      login-entry/        # 进入控制台入口（预填租户）
    api/                  # 仅公开 / 前台 API（后期）
    router/
    styles/
    App.vue
    main.ts
  index.html
  package.json
  vite.config.ts
```

## 命令

```bash
cd portal
pnpm install
pnpm dev
pnpm build
```

## 部署

同一套 `portal/dist`；Nginx 按 `server_name` 转发，应用内解析租户。  
示例：`{tenant}.icoolkj.com` → 本应用。

## 禁止

- Git 内按租户复制工程
- 复用 `frontend/` 中后台 CRUD / 流程设计器
- 未登录写敏感业务数据
