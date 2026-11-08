# site — 平台官网（产品 / 文档 / 许可说明）

> 品牌固定：**iCoolKJ Platform**  
> 国际化：**zh-CN**（默认路径） / **en-US**（`/en/...`）  
> 与管理后台 `frontend/`、租户站 `portal/` 分离。  
> 规范对齐 Admin：**JSON 文案 + view.json 驱动页面结构**（不引用 Gen / Element Plus）。

## 定位

| 项 | 说明 |
|----|------|
| 访客 | 公网访客 |
| 品牌 | 仅 iCoolKJ Platform |
| 主 CTA | 了解许可与使用 |
| 次 CTA | 进入控制台（外链） |
| 文档 | `/docs` 六大类宣传文档（中英） |
| 色彩 | 与 Admin 同源主色 `#1677ff`；支持浅色 / 深色 |
| 后端 | 纯静态 |

## 框架约定（site）

| 层 | 路径 | 职责 |
|----|------|------|
| 壳文案 | `src/i18n/shell/{zh-CN,en-US}.json` | meta / nav / footer |
| 页面文案 | `src/i18n/pages/{page}/{zh-CN,en-US}.json` | 纯文案，不含路由结构 |
| 文档文案 | `src/i18n/docs/{zh-CN,en-US}.json` | 文档中心 |
| 页面结构 | `src/i18n/pages/{page}/view.json` | section 顺序、链接、`labelKey`（对齐 login view.json） |
| 运行时 | `useSiteView(page)` | 读取 view.json；组件用 `t()` / `tm()` 取文案 |

**禁止**：`site` 引用 `frontend` Gen / 工作流设计器等业务组件（见 `docs/architecture/32-site-portal.md`）。

## 文档六大类

| 路径 | 分类 |
|------|------|
| `/docs` | 文档中心 |
| `/docs/overview` | 平台概览 |
| `/docs/capabilities` | 产品能力 |
| `/docs/security` | 安全与合规 |
| `/docs/solutions` | 场景与方案 |
| `/docs/delivery` | 许可与使用范围 |
| `/docs/resources` | 资源与支持 |

英文前缀：`/en/docs/...`。

## 目录

```text
site/
  src/
    config/{site,docs}.ts
    i18n/
      index.ts
      shell/{zh-CN,en-US}.json
      docs/{zh-CN,en-US}.json
      pages/{home,product,...}/{view.json,zh-CN.json,en-US.json}
    composables/{useSiteView,useSiteTheme}.ts
    components/{layout,home,docs}/
    pages/{home,product,docs,license,delivery,about}/
    router/
    styles/index.css
```

## 命令

```bash
cd site
pnpm install
pnpm dev      # http://localhost:5174
pnpm build
```

英文示例：`http://localhost:5174/en` 、`/en/docs/security`

## 部署

Nginx 指向 `site/dist`。控制台外链见 `src/config/site.ts` → `consoleUrl`。
