# Gen 运行时

## 四层

```text
i18n/gen/{pageKey}/view.json     声明
api/{module}/{entity}.ts         HTTP + GEN_RESOURCE_KEY + genCrudResource
GenRuntimePage → GenPage → patterns/GenPattern* → panels
registry/                        crud-resources | extension-blocks | tree-sources
```

## 加载策略

- `view.json`（布局声明）**按需懒加载**：`useGenPageBundle` 在 pageKey 打开时动态
  import，未访问页面的声明不进首屏主包；同一 bundle 带缓存。
- `zh-CN / en-US` 文案同样
  `useGenPageShellI18n` 异步加载后经响应式缓存更新标签；`gp/gf` 在加载前回退为
  原始 key，加载完成自动刷新。需要同步等待的场景先 `await loadGenPageBundle(pageKey)`。
- api 资源注册表（`registerAll`）与域积木（`domains/**/gen-blocks.ts`）保持 eager：
  模块轻量（合计约 150KB），换取 `resolveGenCrudResource` 等运行时同步解析。

## 目录

```text
runtime/GenRuntimePage.vue    框架统一入口（业务页唯一挂载点）
pages/GenPage.vue             layout 路由
patterns/GenPattern*.vue      形态积木 + expose.ts
panels/ layout/ fields/ table/ excel/
dialogs/GenDetailDialog.vue   内核通用详情（仅此）
field-widgets/                平台字段 Widget 组件
registry/                     注册表 + page-patterns.ts
domains/{domain}/             业务插件（dialogs + gen-blocks.ts）
lib/gen-crud/                 api 层 CRUD 定义（无 UI 依赖）
```

## 形态（layout → 积木）

| layout | 积木 | 示例 pageKey |
|--------|------|--------------|
| `standard-crud` / `custom-form` | GenPatternStandardCrud | post, role, banner |
| `tree-crud` | GenPatternTreeCrud | dept, menu |
| `master-detail` | GenPatternMasterDetail | dict |
| `tree-list` | GenPatternTreeList | （规划：user） |

完整形态表：`registry/page-patterns.ts`（P01–P12）。

## 业务页规范

### L0 标准页

```vue
<template>
  <GenRuntimePage page-key="post" />
</template>

<script setup lang="ts">
import { GenRuntimePage } from '@/components/gen'
</script>
```

### L4 扩展页（Tab / handler / 自定义表单）

```vue
<GenRuntimePage
  page-key="config"
  :api-override="apiOverride"
  :action-handlers="actionHandlers"
  @open-form="handleOpenForm"
>
  <template #table-top>...</template>
  <template #toolbar-extra>...</template>
</GenRuntimePage>
```

壳层 i18n（Runtime 外部的 Tab/按钮文案）：

```typescript
import { useGenPageShellI18n } from '@/composables/gen'
const { gp, gf } = useGenPageShellI18n('config')
```

### L5 复合页（P09）

`GenListLayout` + `#table-top` 外壳控件 + 内嵌 `<GenRuntimePage hide-header />`。  
金样例：`databaseBackup`（Tab）、`menu`（产品 Tab + tree-crud）、`attachmentDemo`（说明条 + custom-form）。

### 类型与 ref

```typescript
import type { GenRuntimePageProps, GenPageExpose } from '@/components/gen'

const pageRef = ref<InstanceType<typeof GenRuntimePage>>()
pageRef.value?.loadPage()
pageRef.value?.loadMasterPage() // 主从页
```

## 统一契约

| 项 | 类型 / 位置 |
|----|-------------|
| 入口 props | `GenRuntimePageProps` |
| 路由 props | `GenPagePatternProps` |
| 形态 props | `GenPatternStandardCrudProps` 等 |
| 事件 | `GenPageEmits`：`selection-change` / `open-form` |
| expose | `GenPageExpose` + `createGenPageExpose()` |
| 平台注册 | **仅** `main.ts` 调用 `registerGenPlatform()` |

## api/*.ts

```typescript
import { defineGenCrudResource } from '@/lib/gen-crud'

export const GEN_RESOURCE_KEY = 'system.post' as const
export const genCrudResource = defineGenCrudResource(PERM_Post, {
  page, get, create, update, delete,
})
```

- 只读：`defineReadOnlyGenCrudResource`
- 主从：`defineMasterDetailGenCrudResource`
- 多 pageKey 共文件：`GEN_CRUD_RESOURCES` map（workflow task、ai log 等）
- `registerAll` glob `@/api/**/*.ts`（排除 `*.test.ts`）

## 插件（view.extensions）

| 类型 | 注册 | 声明位置 | view 引用 |
|------|------|----------|-----------|
| 标准详情 | `GenDetailDialog`（内核） | `registerPlatformGenBlocks` | `builtin:detail` |
| Dialog | `GEN_DIALOG_WIDGETS` | `domains/**/gen-blocks.ts` | `extensions.dialogs` |
| Handler | `GEN_ROW_ACTION_HANDLERS` | `domains/**/gen-blocks.ts` | `rowActions[].handler` |
| TreeSource | `GEN_TREE_SOURCE_APIS` | `domains/**/gen-blocks.ts` | `treeSources.*.apiKey` |
| Field | `registerFieldWidget` | `registry/field-widgets/registerCore` + domains | `field.component` |

`registerAllDomainGenBlocks()` glob 发现 `domains/**/gen-blocks.ts`，与 crud-resources glob 风格一致。

### 新增业务 Dialog / Handler

1. `domains/{domain}/dialogs/MyDialog.vue`
2. `domains/{domain}/gen-blocks.ts` 导出 `GEN_DIALOG_WIDGETS` / `GEN_ROW_ACTION_HANDLERS`
3. `view.json` extensions 引用 id（无需改 registry 中心文件）

## 新增页

1. `i18n/gen/{pageKey}/` + `api/*.ts` + `views/.../index.vue`
2. `view.json` 设对 `page.layout`
3. `node gen-spec/validate-runtime-views.mjs`

## 常见错误

| 现象 | 处理 |
|------|------|
| `Failed to resolve extends base type`（Vite / vue SFC） | `defineProps<>` 引用的类型须为**显式 interface**，勿用 `Omit`/`Pick` 作 extends 基类；见 `GenRuntimePageExtensionProps` |
| `unregistered resourceKey` | 补 api 尾部 `GEN_RESOURCE_KEY` + `genCrudResource` |
| `no page bundle` | 补 `i18n/gen/{pageKey}/` |
| Vitest in browser | api glob 勿包含 `*.test.ts` |
| 一直 loading | 看 Network；树源失败已 fail-open |

生成器规范：`gen-spec/gen-page-spec.md`。
