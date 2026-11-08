# Gen Page Spec（页面级生成规范）

> **Phase A**：规范包 + 金样例 + 校验脚本。运行时仍以 Git `frontend/src/i18n/gen/{pageKey}/` 为唯一来源；本规范用于 AI 输入、人工评审与未来 `compile-page-spec → generateCode` 管线。

## 1. 核心概念

| 概念 | 说明 |
|------|------|
| **pageKey** | 菜单页标识，等于 Git 目录名（如 `user`、`dict`），也是 Vue 页 `business-name` |
| **businessName** | 单表 CRUD 实体名（如 `user`）；主从页 master/detail 各有独立 businessName |
| **view.json** | 与语言无关的布局（query / table / form / tree / masterDetail） |
| **zh-CN.json / en-US.json** | 文案 + Bean Validation 约束文案；`meta.constraints` 供后端校验 |
| **page-spec.yaml** | 页面意图的**生成器输入**（表、字段、布局、扩展），不是运行时文件 |

### 与 DB 草稿的关系

- `gen_view_config` / `gen_i18n_message`：生成器 UI 中的**草稿**，可手动同步。
- **运行时与 Bean Validation 不读 DB**；发布 = 写入 Git 并随前端/后端 classpath 部署。
- 未来配置中心只做 **Git ↔ DB 草稿** 对比，不做与单表生成器的三方 diff。

## 2. 目录约定

```
gen-spec/                         # 规范包（AI 读取入口）
  README.md
  gen-page-spec.md
  schemas/
  golden/{pageKey}/
    page-spec.yaml
    expected/
      view.json
      zh-CN.json
      en-US.json
  prompts/
  validate.mjs

frontend/src/i18n/gen/{pageKey}/  # 运行时（与 golden/expected 对齐）
```

## 3. page-spec.yaml 结构

```yaml
specVersion: "1.0"
pageKey: user                    # 必填，= Git 目录名
layout: standard-crud            # standard-crud | tree-crud | master-detail
vueVariant: default              # default | userExtended | masterDetail | treeDept ...

meta:
  apiModule: system
  backendModule: system
  permissionPrefix: system:user
  className: User                # 页面级 PascalCase（主从页用页面级名称如 Dict）

# 单表 layout 使用 entity；master-detail 使用 master + detail
entity:
  tableName: sys_user
  businessName: user
  functionName: 用户

# master-detail 示例见 gen-spec/golden/dict/page-spec.yaml

extensions:                      # 可选；compile-page-spec 会写入 view.json.extensions（rowActions/dialogs）
  rowActions:
    - assignRoles                # 或完整对象：id / handler / dialog / allowOnReadOnly / showWhen / disabledWhen
  dialogs:
    - GenUserRoleDialog
  treeSources:                   # 写入 view.json.extensions.treeSources；apiKey 须在 treeSourceRegistry 登记
    dept:
      apiKey: pageDept
      labelField: deptName
      parentField: parentId
      rootValue: "0"
      rootLabelKey: treeRoot
```

### 字段清单 `fields`

每条字段至少包含 `javaField`；生成器会从列元数据推导 query/table/form 组件。金样例中为校验交叉引用，列出 view 中出现的全部 `javaField`：

```yaml
fields:
  - javaField: username
    javaType: String
    columnComment: 用户名
    isQuery: true
    queryType: LIKE
    isList: true
    isInsert: true
    isEdit: true
    htmlType: input
    dictType: null
```

主从页在 `master.fields` / `detail.fields` 下分别声明。

### meta.businessName 一致性

- **view.json** `meta.businessName` 必须等于 **pageKey**（页面级）。
- **i18n** `meta.businessName` 同样应等于 pageKey；历史 dict 页 zh-CN 仍为 `dictType`，金样例保留现状，校验脚本会 **warn**。

## 4. 布局类型（页面形态）

> 形态目录见 `frontend/src/components/gen/registry/page-patterns.ts`（`GEN_PAGE_PATTERNS`）。  
> 运行时约定见 `frontend/src/components/gen/README.md`。

### 4.1 GenRuntimePage 已路由（codegen 首选）

| layout | 形态 ID | 形态积木 | 业务页入口 |
|--------|---------|----------|------------|
| `standard-crud` / `custom-form` | P01 / P02 / P08 | `GenPatternStandardCrud` | `<GenRuntimePage page-key="…" />` |
| `tree-crud` | P03 | `GenPatternTreeCrud` | 同上 |
| `master-detail` | P04 | `GenPatternMasterDetail` | 同上 |
| `tree-list` | P05 | `GenPatternTreeList` | 同上 |

API：`defineGenCrudResource` / `defineReadOnlyGenCrudResource` / `defineMasterDetailGenCrudResource`（见各 `api/*.ts` 尾部）。

### 4.2 Schema 预留 / 约定式（index.vue 手写壳）

| layout | 形态 ID | 运行时做法 |
|--------|---------|------------|
| `custom-form` | P08 | `GenRuntimePage` + `custom-form` + `@open-form` |
| `custom` | P09 | `GenListLayout` + 嵌套 `GenRuntimePage hide-header` |
| `workbench` / `custom-profile` | P10 | `GenListLayout` + `plain` |
| `card-catalog` | P11 | 规划 · 手写卡片网格 |
| `region-org` | P12 | 规划 · 收敛 tree-crud / tree-list |
| `master-tree` | P06 | 规划 · masterDetail.detailLayout |

`vueVariant` 描述 index.vue 相对模板的扩展，不改变 view.json schema。

### 操作按钮（与 Gen 布局组件一致）

| 场景 | 写法 |
|------|------|
| 搜索 / 工具栏 / 弹窗确认 | 普通 `el-button`，**不写** `size`（全局 `default`） |
| 表格行内编辑/删除 | `el-button link type="primary|danger"` |
| 自定义 slot | 见下方封闭约定，与布局组件内置操作一致 |

生成页为 **薄壳**：根节点 `gen-page-shell` → 唯一主布局组件；标题/搜索/表格由框架渲染；扩展仅通过 **封闭 slot** / props / **view.extensions 积木**。禁止外层再包 `PageHeader` 或手写 `page-container` 自建壳。

**配置驱动产品形态（主航道）**

| 能力 | 驱动方式 |
|------|----------|
| 查询 / 表格 / 表单 / 只读 | `view.json` |
| 文案与校验提示 | `zh-CN.json` / `en-US.json` |
| 布局编排 | `GenCrudPage` / `GenTreeCrudPage` / `GenMasterDetailPage` / `GenListLayout` |
| 积木（可复用） | `GenDataTable`、`GenEntityForm`、`GenFormShell`、`GenFormField` |
| 特殊行操作 / 对话框 | `view.extensions` → `gen/registry` **注册表**（handler / dialog id） |
| API + 权限（Gen 运行时） | 同文件 `api/{module}/{entity}.ts` 导出 `GEN_RESOURCE_KEY` + `genCrudResource` |

标准 CRUD 目标：**只配 view.json + 薄壳绑 API**。禁止在业务页复制表格/表单布局。

**全路由页必须套用模板组件**（开发套用约定）：

| 页面类型 | 模板 | 说明 |
|----------|------|------|
| 标准 / 只读 CRUD | `GenCrudPage` | 含 view.json |
| 树形 CRUD | `GenTreeCrudPage` | |
| 主从 | `GenMasterDetailPage` | |
| 自定义列表 / 目录 / 工具表单 | `GenListLayout` | 封闭 slot；可无 view.json |
| 工作台 / 个人中心 / 全屏对话 | `GenListLayout` + `plain` | 仅页头 + 自由内容 |
| 审批详情 | `gen-page-shell` + `WorkflowApproveLayout` | 业务槽位自定义 |

内嵌 `GenCrudPage` 仅允许在外层已是 `GenListLayout` 且需隐藏重复页头时使用 `hide-header`（如库备份 Tab）；禁止手写 `PageHeader` + `hide-header`。

**代码生成模板**必须与此一致：`backend/icoolkj-admin-generator/.../vm/vue/v3/index.vue.vm`
（`tplCategory=tree` → `GenTreeCrudPage` + `createDefaults`；否则 → `GenCrudPage`；均包在 `gen-page-shell` 内）。

生成页禁止在模板里写 `size="small"`；布局由 `GenCrudPage` / `GenTreeCrudPage` / `GenMasterDetailPage`（或非 CRUD 的 `GenListLayout`）统一渲染。

### 封闭 slot 约定（避免 GenCrudPage 膨胀）

布局分区只放在 **`GenListLayout`**。`GenCrudPage` / 自定义列表页只转发或使用下列插槽，**不要**再往 `GenCrudPage` 增加布局类 slot。

| Slot | 位置 | 用途 | 谁提供 |
|------|------|------|--------|
| `#header-extra` | 页头右侧 | 页级次要操作（少用） | `GenListLayout` |
| `#search` | 搜索卡 | 查询表单 | `GenListLayout`（CRUD 内置） |
| `#table-top` | 表格卡、工具栏上方 | 分类 Tab 等列表筛选条 | `GenListLayout` → CRUD 转发 |
| `#toolbar` | 工具栏左侧主区 | 新增等主按钮 | `GenListLayout`（CRUD 内置） |
| `#toolbar-extra` | 工具栏左侧附加 | 刷新缓存、批量删除等 | `GenListLayout` → CRUD 转发 |
| `#toolbar-right` | 工具栏右侧 | 覆盖默认 `RightToolbar` | `GenListLayout` |
| default | 表格主体 / plain 自由内容 | `el-table` 或自定义 | `GenListLayout` |
| `#footer` | 表格下方 | 分页 | `GenListLayout`（CRUD 内置） |
| `#row-actions` | 行操作列 | CRUD 行级扩展（非布局） | **仅** `GenCrudPage` |

`GenListLayout` 额外 props：`plain`（仅页头+内容，无 search/table-card）、`hideHeader`、`showSearch`、`showRightToolbar`。

规则：

1. 需要新「区块」→ 先改 `GenListLayout`，再让 CRUD 转发；禁止在 `GenCrudPage` 模板里直接插 `el-card` / `PageHeader`。
2. 非标准分页 CRUD（如租户策略、文件管理、发起申请）→ `gen-page-shell` + **`GenListLayout`** + 业务内容，不手写 `page-container` 壳。
3. 业务逻辑（打开弹窗、调 API）留在薄壳页；布局与分区样式留在 `GenListLayout`。
4. **登录页除外**；其余菜单/路由页均须落在上文「全路由页必须套用模板组件」表之一。

### view.extensions 积木约定（特殊交互）

运行时文件 `view.json` 可声明：

```json
"extensions": {
  "rowActions": [
    { "id": "detail", "labelKey": "detailAction", "dialog": "GenNoticeDetailDialog", "dialogIdProp": "noticeId", "allowOnReadOnly": true },
    { "id": "edit", "builtin": "edit", "permKey": "update" },
    { "id": "assignRoles", "labelKey": "assignRoles", "permKey": "update", "dialog": "GenUserRoleDialog" },
    { "id": "resetPassword", "labelKey": "resetPassword", "permKey": "resetPassword", "type": "warning", "handler": "user.resetPassword" },
    { "id": "delete", "builtin": "delete", "permKey": "delete", "type": "danger" }
  ],
  "dialogs": ["GenNoticeDetailDialog", "GenUserRoleDialog"]
}
```

| 字段 | 说明 |
|------|------|
| `builtin` | 框架内置 `edit` / `delete` |
| `handler` | 全局注册（`gen/registry`）或薄壳 `actionHandlers` |
| `dialog` | 须 `registerDialogWidget`；并列入 `extensions.dialogs` 以便挂载（**详情积木**同此契约） |
| `dialogIdProp` | 详情/对话框主键 prop 名（如 `noticeId`、`messageId`；默认 `id`） |
| `dialogRowKey` | 行上取主键的字段（默认 `id`） |
| `labelKey` / `permKey` | 对应 i18n `gp` 与薄壳 `perm` 对象键 |
| `allowOnReadOnly` | `true` 时在只读页仍显示（详情 / 只读列表删除等） |
| `showWhen` | `{ field, values }`：仅当行字段命中时显示 |
| `disabledWhen` | `{ field, values }`：命中时按钮禁用（如系统内置配置不可删） |

复杂行级可见性（多字段/业务状态）可用薄壳 prop `rowActionVisible`；逻辑操作可用 `actionHandlers`。

`form.mode`：`dialog`（默认）/ `drawer` / `page`（整页表单，列表与表单互斥切换）。

**详情积木契约**（`frontend/src/components/gen/dialogs/detail` + 业务详情组件）：

1. 在 `registerPlatformBlocks` 登记 id（如 `GenAiInvokeLogDetailDialog`、`GenNoticeDetailDialog`）
2. Props：`modelValue` + 主键 prop + 可选 `title`；建议 `inheritAttrs: false`
3. `view.extensions`：`rowActions[].dialog` + `dialogs[]`；薄壳不再手写详情弹窗
4. `#row-actions` 与 extensions **叠加**（条件按钮如审批留在 slot，勿重复声明详情）

已注册详情：`GenNoticeDetailDialog`、`GenSiteMessageDetailDialog`、`GenAiInvokeLogDetailDialog`、`GenAiDdlLogDetailDialog`、`GenAiChatLogDetailDialog`。

**AI / 生成器规则**：`extensions` 只能引用**已注册**的 handler/dialog id；不得编造组件名或在 page-spec 里输出自由 Vue。新积木先注册，再写进 view。page-spec 的 `extensions` 经 `compile-page-spec` 写入双端 `view.json`。

金样例：`user`（`assignRoles` + `resetPassword`）；详情：`notice` / `siteMessage` / `aiInvokeLog`。

### 只读审计页 / 工具型自定义页

| 类型 | 形态 | view.json | 说明 |
|------|------|-----------|------|
| 只读审计列表 | `gen-page-shell` → `GenCrudPage`（`readOnly`） | **必须** | `extensions` 挂详情积木（`allowOnReadOnly`）；勿在薄壳内联详情 DOM |
| 工具型表单/向导/对话 | `gen-page-shell` → `GenListLayout`（必要时 `plain`） | 可无 | 必须有独立 `i18n/gen/{pageKey}` 双端；禁止多菜单共用一个臃肿 pageKey |

枚举筛选项优先用 **字典**（`dictType` + `dict-tag`/`select`），不要在页面内硬编码多语言 map。

### AI 工具页 pageKey 清单（`apiModule=tool`，`backendModule=generator`）

| pageKey | 菜单 | 布局 |
|---------|------|------|
| `aiInvokeLog` | AI 调用记录 | 只读 GenCrud + 详情弹窗 |
| `aiDdlLog` | AI 建表记录 | 只读 GenCrud + 详情弹窗 |
| `aiChatLog` | AI 对话记录 | 只读 GenCrud + 会话详情 |
| `aiConfig` | AI 接口配置 | GenListLayout 自定义表单 |
| `toolAi` | 向导/对话共享文案 | 无 view.json（Chat / TableWizard / PageSpecWizard） |

i18n 双端：`frontend/src/i18n/gen/{pageKey}/` ↔ `backend/icoolkj-admin-generator/src/main/resources/i18n/gen/{pageKey}/`。

审计表（调用内容旁表等）**不强制**进 Velocity 一键 CRUD；手迁形态须与上表金样例一致，便于后续登记 gen_table 再编译。

## 5. 四道门禁（严格一致）

1. **本文档 + JSON Schema**（`gen-spec/schemas/`）
2. **金样例**（`gen-spec/golden/`）
3. **现有 Velocity 生成器**（`generateCode` 唯一编译器）
4. **`node gen-spec/validate.mjs`** — Schema + 交叉规则

AI 输出应为 **page-spec.yaml + 列元数据/DDL**，经编译后走现有生成器，而非自由格式 Vue/JSON。

## 6. 校验脚本

```bash
# 首次（gen-spec 目录安装依赖）
cd gen-spec && npm install && cd ..

node gen-spec/validate.mjs
node gen-spec/validate.mjs gen-spec/golden/user
```

校验项：

- page-spec 符合 `gen-page-spec.schema.json`
- expected/*.json 符合 view / i18n schema
- pageKey 与 view.meta.businessName 一致
- page-spec 声明的 javaField 集合 ⊆ expected view 中出现的 field
- i18n fields 的 key 与 view form/query/table field 对齐（warn 级别允许 page 级额外 key）

## 7. 金样例

| pageKey | layout | vueVariant | 说明 |
|---------|--------|------------|------|
| `user` | standard-crud | userExtended | 分配角色、部门树 |
| `dict` | master-detail | masterDetail | dictType + dictItem |

## 后续（部分已实现）

- **AI 助手**：能力目录配置 + 只读审计三页（`aiInvokeLog` / `aiDdlLog` / `aiChatLog`）+ 向导/对话共享 `toolAi`；见上文 pageKey 清单
- `compile-page-spec` CLI → 写入 gen_table + 触发 generateCode
- 配置中心 Git ↔ DB 草稿发布
