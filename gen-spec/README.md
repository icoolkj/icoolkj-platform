# gen-spec — 页面生成规范包

> **AI / 工具读取入口**：本目录自包含规范、Schema、金样例、Prompt 与校验脚本。运行时产物仍在 `frontend/src/i18n/gen/{pageKey}/`。

## 目录结构

```
gen-spec/
├── README.md                 ← 你在这里（索引）
├── gen-page-spec.md          ← 规范正文
├── schemas/
│   ├── gen-page-spec.schema.json
│   ├── gen-view.schema.json
│   └── gen-i18n.schema.json
├── golden/                   ← 金样例（page-spec + expected JSON）
│   ├── user/                 ← 交互对话框 + handler
│   ├── dict/                 ← master-detail
│   ├── notice/               ← 详情积木 GenNoticeDetailDialog
│   └── aiInvokeLog/          ← 只读审计 + 详情积木
├── prompts/
│   └── gen-page-spec.prompt.md
├── validate.mjs              ← Schema + 交叉规则校验
└── package.json
```

## AI 推荐阅读顺序

1. `gen-page-spec.md` — 概念、pageKey、布局、Git 优先、`view.extensions` 积木
2. `schemas/*.schema.json` — 输出结构约束
3. `golden/{pageKey}/page-spec.yaml` — 可复制的意图示例
4. `golden/{pageKey}/expected/` — 生成器应对齐的 view / i18n 产物
5. `prompts/gen-page-spec.prompt.md` — 系统 Prompt 模板

## 运行时 vs 规范

| 类型 | 路径 | 用途 |
|------|------|------|
| **规范输入** | `gen-spec/golden/{pageKey}/page-spec.yaml` | AI / 人工意图 |
| **期望产物** | `gen-spec/golden/{pageKey}/expected/` | 校验基准 |
| **运行时 FE** | `frontend/src/i18n/gen/{pageKey}/` | Vue 唯一来源 |
| **运行时 BE** | `backend/*/src/main/resources/i18n/gen/{pageKey}/` | classpath 副本（须与 FE 同步） |

## 校验

```bash
cd gen-spec && npm install && cd ..
node gen-spec/validate.mjs
node gen-spec/validate.mjs gen-spec/golden/user
```

兼容旧命令：`node scripts/validate-gen-pages.mjs`（转发到本目录）。

## 运行时 view.json 校验

`node gen-spec/validate-runtime-views.mjs` 校验 `frontend/src/i18n/gen/**/view.json`：

- Schema 形状（`schemas/gen-view.schema.json`，含 rowAction `builtin: detail`）；
- 注册集交叉校验（与运行时同一发现源，非硬编码）：
  - `extensions.dialogs` / `rowActions[].dialog` → `domains/**/gen-blocks.ts` 的 `GEN_DIALOG_WIDGETS`（+ 内核 `GenDetailDialog`）
  - `rowActions[].handler` → `GEN_ROW_ACTION_HANDLERS` 或页面本地 `actionHandlers`
  - `treeSources.*.apiKey` → `GEN_TREE_SOURCE_APIS`
  - `query/form` 字段 `component` → `field-widgets/registerCore.ts` 已注册积木
- `meta.resourceKey` 与 `api/**` 的 `GEN_RESOURCE_KEY` 对齐（警告级）

新加业务页或积木后先跑本脚本，把"运行时才报 unregistered"提前到提交前。

## 金样例一览

| pageKey | layout | 说明 |
|---------|--------|------|
| `user` | standard-crud | `GenUserRoleDialog` + `user.resetPassword` |
| `dict` | master-detail | dictType + dictItem |
| `notice` | standard-crud | **详情积木** `GenNoticeDetailDialog` + 审批 handlers |
| `aiInvokeLog` | standard-crud | 只读审计 + **详情积木** `GenAiInvokeLogDetailDialog` |

## 管理端 UI

- 菜单：**AI**（能力目录 / 对话 / 文生图 / 助手等）
- **能力目录**：连接（Base URL / Key）+ 模型 + chat/image 等能力绑定（非固定厂商）
- **页面规格**：选表 → AI 生成 YAML → 校验 → **一键编译**（同步 gen_table + generateCode 写 Git）
- 一键编译暂支持 `standard-crud` / `tree-crud`；`master-detail` 请用 dict 金样例；详情积木见 notice / aiInvokeLog
