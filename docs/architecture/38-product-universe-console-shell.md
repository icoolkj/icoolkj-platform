# 产品宇宙 Console Shell

> 更新：2026-07-30 · 模块：product-universe · 上游：[15-menu-permission.md](15-menu-permission.md) · [AI-CONTEXT.md](../AI-CONTEXT.md)

## 1. 目标

Admin 控制台按 **产品宇宙** 切换导航上下文：顶栏选产品 → 侧栏只展示该产品菜单 → 每产品有概览首页。用户收件箱仍在壳层顶栏「消息」，不作为独立产品。

## 2. 内置产品（v1）

| code | 默认 sort_no | 说明 |
|------|--------------|------|
| 业务扩展 | **1–49** | 预留给后续业务宇宙（靠前） |
| `oa` | 50 | 在线办公 / OA 配置 / 公告与站内信**管理** |
| `files` | 60 | 文件管理与文件配置中心 |
| `ai` | 70 | AI Catalog 与试跑 |
| `platform` | **90+** | 组织权限、运维监控、系统管理（靠后） |

排序约定写在 `sys_product.sort_no`；业务宇宙上线后插在 1–49，不改内置编码。

## 3. 数据模型

| 对象 | 要点 |
|------|------|
| `sys_product` | 产品目录（`code` 唯一、`sort_no`、`name_i18n`）；平台表，`ignore-tables` |
| `sys_menu.product_code` | 菜单归属产品；默认 `platform` |
| 授权 v1 | **无** `sys_tenant_product`；`GET /system/product/mine` = 当前用户已授权菜单上的 distinct `product_code` ∩ 启用产品 |

改 V1/V2 后需 **删库重建**（Flyway 仅 V1–V4）。

## 4. API

| 接口 | 说明 |
|------|------|
| `GET /system/product/mine` | 当前用户可用产品（含 `homePath`） |
| `GET /system/product/list` | 全量产品（菜单管理下拉等，需菜单管理权限） |
| `GET /system/menu/routers?productCode=` | 侧栏树按产品过滤；缺省返回全量（注册动态路由用） |
| `GET /auth/siteMessage/unread-count` | 顶栏未读数 |

首页路径约定：`/product/{platform\|oa\|files\|ai}/overview/index`。

## 5. 前端壳层

- Store：`useProductStore`（偏好 `localStorage` key `icoolkj.ui.product`）；默认优先 `platform`，否则按 `sort_no` / 已存偏好。
- 切换产品：`closeAll` 非固定页签 → `loadSidebarRouters(code)` → 跳转该产品 `homePath`。
- 动态路由：登录后按 **全量** routers 注册；侧栏只吃当前产品树；`meta.productCode` 保留。
- 顶栏顺序：**消息 → 语言 → 全屏 → 布局 → 账号**；产品宇宙为 header 左侧**固定横向菜单**（常驻展示全部可用产品、当前产品高亮，点击切换并跳转产品首页，非下拉）。
- 概览：platform 复用原工作台；oa/files/ai 为 stub 卡片（后续接指标）。

## 6. 菜单管理

「所属产品」字段读写 `product_code`；选项来自 `GET /system/product/list`（Gen `getFieldOptions`，不建重复字典）。

**管理端 UX（v1.5）**

- 菜单管理页按 **产品 Tab** 分区展示与筛选（默认 `platform`），表格内不再重复「所属产品」列。
- 新建/子菜单默认继承当前 Tab 的 `product_code`；后端校验：
  - 顶级节点必须为 `M` 目录；
  - 子节点 `product_code` 必须与父节点一致；
  - `oa` / `files` / `ai` 每产品仅允许一个 `parent_id=0` 的 M 根。

**种子树形（oa / ai / files）**

- 每产品一个 M 根（如 `400024` 办公、`400025` AI、`400021` 文件），概览 C 挂于根下。
- 在线办公、OA 配置、AI 工具树等为根的子节点；**路由 path 不变**（C 页仍用 component_path 注册动态路由）。

**角色授权**

- `GET /system/role/{id}/assignable-menus?productCode=` 支持按产品过滤。
- 前端授权对话框按产品 Tab 分组，支持「全选本产品 / 清空本产品」。

**代码生成**

- `gen_table.options` JSON 扩展：`productCode`、`assignScope`、`parentMenuId`。
- `menu.sql.vm` 输出 `assign_scope` + `product_code`；`TENANT` 菜单自动授租户超管角色。

## 7. 禁止事项

- 勿为 v1 新建独立 `ops` 产品；运维入口留在 `platform`。
- 勿把用户收件箱做成侧栏产品；管理端公告/站内信归属 `oa`。
- 勿新增 Flyway **V5+**；产品种子改 V1/V2。
- 勿用硬编码 menuId 列表表达产品归属；以 `product_code` + `sys_product` 为准。

## 相关文档

- [15 菜单与权限](15-menu-permission.md)
- [06 认证与权限](06-auth-permission.md)
- [09 核心模块](09-core-modules.md)
