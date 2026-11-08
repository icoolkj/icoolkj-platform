# 菜单与权限模型

> 版本：V2.2 | 模块：menu-permission | 上游：[docs/README.md](../README.md)

## 概述

本篇定义菜单导航资源与权限码授权资源的分离模型，以及目录（M）、菜单（C）、按钮（F）三类 `sys_menu` 资源。

---

## 最终规则

> **菜单是导航资源，权限码是授权资源**

- 菜单：前端导航与页面组织
- 权限码：后端接口授权 + 前端按钮显隐
- **不以「是否有菜单」代替「是否有权限」**
- 一个菜单（C）可挂多个按钮（F）
- `sys_menu` 同时承载 M / C / F 三类资源
- `sys_role_menu` 统一关联目录、菜单、按钮
- 显示名：`menu_name`（主名称）+ `name_i18n` JSON（按语言解析）；侧栏与角色授权树均走当前语言

### `name_i18n` 英文规范（种子 / 管理端录入）

| 类型 | 英文风格 | 示例 |
|------|----------|------|
| `M` 目录 | 短 Title Case，可含 Management / Governance | System Management、Monitoring & Audit |
| `C` 页面 | Title Case 资源/功能名（复数或短语） | Users、Login Logs、Code Generator、Tenant Policies |
| `F` 按钮 | 短英文动作；**禁止**中文塞进 `en-US` | List / Add / Edit / Delete / View / Export；详情→View，查询→List |

种子审计：菜单 `name_i18n` 英文规范见上表；可用仓库脚本 `scripts/sync-gen-i18n-fe-to-be.cjs` 同步壳层/platform/gen 双端文案。

---

## 资源类型（`menu_type`）

| 值 | 类型 | 用途 |
|----|------|------|
| `M` | 目录 | 导航分组，无路由 |
| `C` | 菜单 | 对应页面路由 |
| `F` | 按钮 | 页面内操作点（新增、删除、导出等） |

### 侧栏信息架构（分组约定）

- 同一父目录下可见 `C` 超过约 **6～8** 个时，应再套一层 `M`（对齐「文件配置中心」「系统管理」二级组）。
- **系统管理** 二级：`组织权限` / `平台元数据` / `国际化` / `展示与策略`；路由与 `permission_code` 不变，只调 `parent_id`。
- 目录 `assign_scope`：子页全 PLATFORM → 父目录 PLATFORM；含租户页 → 父目录 TENANT（树构建会自动补祖先）。
- 壳层：默认菜单排他展开 + 侧栏关键字搜索（布局偏好可关）。
- **产品宇宙**：`sys_menu.product_code` + `sys_product`；侧栏按当前产品过滤，顶栏切换；用户收件箱在壳层「消息」。见 [38-product-universe-console-shell.md](38-product-universe-console-shell.md)。
- **产品树规范**：每产品一个 M 根；**严格 M→C→F**（目录→菜单→按钮）；产品根下可挂概览 C 与二级 M 目录。
- **平台树**：仅平台产品根为顶级；工作流/运维/监控/安全/租户/工具/系统管理均为其子 M 目录（系统管理排最后）。
- **跨产品 duplicate**：同一 `permission_code` 可存在 PLATFORM 与 TENANT 两份菜单（如工作流配置 vs 租户办理入口）；唯一键为 `(permission_code, assign_scope, deleted)`。

---

## 权限校验

### 后端

```java
@PreAuthorize("@authz.hasPermission('system:user:list')")
```

### 前端

根据权限码控制按钮显隐，例如 `v-hasPermi="'system:user:add'"`。

---

## 菜单与按钮关系示例

**用户管理页面**

| 资源 | menu_type | permission_code |
|------|-----------|-----------------|
| 用户管理（页面） | `C` | `system:user:list` |
| 新增按钮 | `F` | `system:user:add` |
| 修改按钮 | `F` | `system:user:update` |
| 删除按钮 | `F` | `system:user:delete` |
| 导出按钮 | `F` | `system:user:export` |

建议：

- 页面级 → **可见** `C`（租户侧栏能看见）
- 操作级 → `F`，挂在对应 **可见** `C` 下
- **禁止**再建整页 `visible=1` 的 C 只为挂权限码（与已有入口重复）
- 例外：待办跳转的审批页可为 hidden `C`（真路由，非权限载体）
- `permission_code` 唯一键为 `(permission_code, assign_scope, deleted)`

---

### sys_menu ID 分段规则（19 位）

```text
4000 + DD(2) + GG(2) + PP(2) + SS(2) + 0000000(7)
```

| 段 | 含义 |
|----|------|
| DD | 业务域：01 仪表盘 / 10 办公 / 08 流程 / 09 OA配置 / 07 基础设施 / 05 监控 / 04 安全 / 03 租户 / 06 工具 / **02 系统管理（靠后）** |
| GG | 二级目录序号；根目录为 `00` |
| PP | 页面（C）序号；目录节点为 `00` |
| SS | 按钮（F）序号；页面/目录为 `00` |

侧栏排序：常用业务域在前，**系统配置类（系统管理）最后**。系统管理内：组织权限 → 展示与策略 → 平台元数据 → 国际化。
修改种子后需 **删库重建**（Flyway 仅 api 启动执行）。

## sys_menu 数据归属

V2.1：`sys_menu` 为**平台统一菜单模板**，不带 `tenant_id`。各租户通过 `sys_role_menu` 授权可见资源子集。

---

## 超级管理员与权限

- 租户超级管理员：可绕过本租户普通权限校验
- 普通用户：必须命中对应 `permission_code`
- 平台超级管理员：跨租户管理时仍受平台级安全规则约束

详见 [06 认证与权限](06-auth-permission.md)。

---

## 相关文档

- [06 认证与权限](06-auth-permission.md)
- [13 多租户设计](13-multi-tenant.md)
- [09 核心模块](09-core-modules.md)
- [11 初始化建议](11-initialization.md)
- [38 产品宇宙 Console Shell](38-product-universe-console-shell.md)

---

## 落地检查清单（Checklist）

- [ ] `sys_menu` 含 M/C/F 三类资源
- [ ] 每个接口绑定 `permission_code`，非菜单 ID
- [ ] 按钮使用 `F` 类型，`parent_id` 指向页面菜单
- [ ] `permission_code` 唯一索引含 `deleted`
- [ ] 前端按钮用权限码控制，不用菜单可见性代替
- [ ] `role_menu` 关联到按钮资源 ID
