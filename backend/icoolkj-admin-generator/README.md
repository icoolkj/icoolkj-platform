# icoolkj-admin-generator

Apache Velocity 代码生成器，适配 iCoolKJ Platform 多模块架构（Java + Vue3/TS）。

## 生成产物与路径规范

生成时以**项目根目录**为基准，预览 / 下载 zip / 写入磁盘均使用下列相对路径。预览第一页为 `_OUTPUT_PATHS.md` 路径清单。

### Java（业务层 `icoolkj-admin-{backendModule}`）

| 类型 | 相对路径 |
|------|----------|
| Entity | `backend/icoolkj-admin-{backendModule}/src/main/java/{packageName}/domain/{EntityName}.java` |
| Mapper | `backend/icoolkj-admin-{backendModule}/src/main/java/{packageName}/mapper/{ClassName}Mapper.java` |
| Mapper XML | `backend/icoolkj-admin-{backendModule}/src/main/resources/mapper/{backendModule}/{ClassName}Mapper.xml` |
| PageQuery | `backend/icoolkj-admin-{backendModule}/src/main/java/{packageName}/dto/{ClassName}PageQuery.java` |
| CreateRequest | `backend/icoolkj-admin-{backendModule}/src/main/java/{packageName}/dto/{ClassName}CreateRequest.java` |
| UpdateRequest | `backend/icoolkj-admin-{backendModule}/src/main/java/{packageName}/dto/{ClassName}UpdateRequest.java` |
| VO | `backend/icoolkj-admin-{backendModule}/src/main/java/{packageName}/vo/{ClassName}VO.java` |
| Convert | `backend/icoolkj-admin-{backendModule}/src/main/java/{packageName}/convert/{ClassName}Convert.java` |
| Service | `backend/icoolkj-admin-{backendModule}/src/main/java/{packageName}/service/{ClassName}Service.java` |
| ServiceImpl | `backend/icoolkj-admin-{backendModule}/src/main/java/{packageName}/service/impl/{ClassName}ServiceImpl.java` |

默认 `packageName` = `com.icoolkj.platform.{backendModule}`（如 system / infra）。

### Java（API 层 `icoolkj-admin-api`）

| 类型 | 相对路径 |
|------|----------|
| Controller | `backend/icoolkj-admin-api/src/main/java/com/icoolkj/platform/api/controller/{apiModule}/{ClassName}Controller.java` |

### 前端（`frontend/src`）

| 类型 | 相对路径 |
|------|----------|
| API | `frontend/src/api/{apiModule}/{businessName}.ts` |
| Types | `frontend/src/types/{apiModule}/{businessName}.ts` |
| 页面 | `frontend/src/views/{apiModule}/{businessName}/index.vue` |
| Gen i18n zh-CN (frontend) | `frontend/src/i18n/gen/{businessName}/zh-CN.json` |
| Gen i18n en-US (frontend) | `frontend/src/i18n/gen/{businessName}/en-US.json` |
| Gen i18n 基线模板（codegen） | 双端 `i18n/gen/_codegen/{zh-CN,en-US}.json`（`GenI18nBuilder` 填充，禁止 Java 写死文案） |
| Gen i18n zh-CN (backend) | `backend/icoolkj-admin-{backendModule}/src/main/resources/i18n/gen/{businessName}/zh-CN.json` |
| Gen i18n en-US (backend) | `backend/icoolkj-admin-{backendModule}/src/main/resources/i18n/gen/{businessName}/en-US.json` |
| Gen view (frontend) | `frontend/src/i18n/gen/{businessName}/view.json` |
| Gen view (backend) | `backend/icoolkj-admin-{backendModule}/src/main/resources/i18n/gen/{businessName}/view.json` |

菜单 SQL：`backend/database/migration/gen/{businessName}_menu.sql`

写入磁盘后另存路径清单：`gen/{tableName}/OUTPUT_PATHS.md`

### 配置字段说明

在代码生成编辑页配置，决定上述路径中的占位符：

| 字段 | 说明 | 示例 |
|------|------|------|
| `backendModule` | Java 业务 Maven 模块 | `system` |
| `apiModule` | Controller 包名 + 前端目录一级 | `system` |
| `businessName` | 前端文件名与 views 二级目录，camelCase | `ipAccessRule` |
| `className` / `EntityName` | 由表名推导的 Java 类名 | `IpAccessRule` |
| `permissionPrefix` | 权限前缀（camelCase，格式 `module:resource`） | `system:ipAccessRule` |
| `routePath` | 菜单路由（不含 layout） | `/system/ipAccessRule` |

**示例**（IP 访问规则）：`backendModule=system`，`apiModule=system`，`businessName=ipAccessRule` → 页面落在 `frontend/src/views/system/ipAccessRule/index.vue`，Java 仍在 `icoolkj-admin-system`。

## 前端 UI 规范

生成的列表页对齐平台标准：

- **薄壳**：`gen-page-shell` → `GenCrudPage` / `GenTreeCrudPage`（模板：`vm/vue/v3/index.vue.vm`）
- 布局分区在 **`GenListLayout`**（封闭 slot，见 `gen-spec/gen-page-spec.md`）；生成页禁止外层再包 `PageHeader`
- `RightToolbar` 刷新与折叠搜索（由 `GenListLayout` 默认提供）
- 搜索区默认最多 **4** 个查询字段（可在列配置中调整 `isQuery` 与排序）
- **Gen i18n 基线模板**：双端 `i18n/gen/_codegen/{locale}.json`；`GenI18nBuilder` 仅占位填充（`{functionName}`/`{label}`/`{maxLength}`），禁止 Java 写死中英文案
- **Gen i18n 运行时（P0 Git-only）**：CRUD 页 `import` `frontend/src/i18n/gen/{businessName}/` 下 JSON；**不**请求 `/gen/i18n/messages` 或 merge `gen_i18n_message`
- **`view.json`**：同上，仅 Git bundle；**不**请求 `/gen/view/by-business`
- **后端 Bean Validation**：`{gen.{businessName}.{field}.rule}` 仅 classpath JSON；DB `gen_i18n_message` 不参与运行时
- **生成器写 Git JSON**：仅在「**生成代码** / 写入磁盘」时（`writeGenI18nLocaleFiles` + `writeGenViewFile`）；导入、保存字段 Tab、列表「同步」**不再**改 Git
- **列表「同步」**：仅把 MySQL 表结构同步到 `gen_table_column`，不写 Git、不写运行时
- **i18n/view Tab「同步」**：仅更新 DB 草稿（`gen_i18n_message` / `gen_view_config`），不写 Git、不参与运行时
- **字典类型**：不在 `GenUtils` 里按列名推断；`dictType` 只在字段 Tab 配置，保存后写入 `gen_table_column` → `view.json`
- **view 合并规则**：保存/同步时 `component`、`dictType`、`insert`/`edit` 等**列配置属性**以字段 Tab 为准；`width`/`span` 等布局微调保留已有值
- **保存字段 Tab** 会同步写入 `view.json`（前后端 i18n 目录）与 `gen_view_config`，无需再点「生成代码」才更新布局
- **前端运行时**：`GenCrudPage` 读取 `view.json` + i18n JSON；生成页为薄壳（`gen-page-shell` → `GenCrudPage`）。布局分区在 `GenListLayout`（封闭 slot：`#table-top` / `#toolbar-extra` / `#row-actions` 等）；非 CRUD 列表页直接用 `GenListLayout`
- **操作按钮尺寸**：全局 `UI_BUTTON_SIZE=default`（`App.vue` ConfigProvider）；搜索/工具栏/弹窗 footer 用普通 `el-button`；表格行内操作用 `link` 类型；**禁止**在 `el-button` 上写 `size`（登录页 `el-form size="large"` 除外）。布局组件：`GenCrudPage` / `GenTreeCrudPage` / `GenMasterDetailPage`
- **后端 Bean Validation**：`CreateRequest` / `UpdateRequest` 注解键 `{gen.{businessName}.{field}.required|maxLength}`，由 `GenI18nExtendedMessageResolver` 从 classpath JSON 基线 + `gen_i18n_message` 解析
- 代码生成「国际化」Tab 仍编辑 DB；保存后清除前端缓存，刷新页面即生效

### 前端代码风格（ESLint + Prettier）

生成的 `api.ts` / `types.ts` / `index.vue` / `*.json` **必须**可直接通过：

```bash
cd frontend && pnpm lint && pnpm format:check
```

对齐 `frontend/.prettierrc.json` 与 `frontend/eslint.config.mjs`：

| 项 | 要求 |
|----|------|
| 引号 / 分号 | 单引号、无分号 |
| `printWidth` | 100；类型 import 一律多行（见 `vm/ts/api.ts.vm`） |
| 换行 | **始终 LF**（`GenTextNormalize` + `GenJsonWriter`，不依赖 OS） |
| 文件末尾 | 保留换行 |
| Vue 壳 | `script setup` + `gen-page-shell` + `GenCrudPage`/`GenTreeCrudPage` 薄壳；不手写整页 CRUD |

手改生成物后应再跑 `pnpm format`，避免与生成器基线漂移。

## API

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/tool/gen/page` | `tool:gen:list` | 分页列表 |
| GET | `/tool/gen/preview/{id}` | `tool:gen:preview` | 预览（key 为相对路径） |
| GET | `/tool/gen/genCode/{tableName}` | `tool:gen:code` | 写入项目目录 |
| GET | `/tool/gen/download/{tableName}` | `tool:gen:code` | 单表 zip |
| GET | `/gen/i18n/messages/by-business/{businessName}` | 登录即可 | **已废弃** — 运行时 Git-only，CRUD 页不再调用 |
| GET | `/gen/i18n/messages/{tableId}` | 登录即可 | **已废弃** — 同上 |
| GET | `/gen/view/by-business/{businessName}` | 登录即可 | **已废弃** — 运行时 Git-only |

## 模板目录

```
src/main/resources/vm/
  java/     Entity, Mapper, Service, Controller, DTO, VO, Convert
  xml/      Mapper XML
  ts/       api.ts, types.ts
  vue/v3/   index.vue（gen-page-shell + GenCrudPage / GenTreeCrudPage）
  sql/      menu.sql.vm
```
