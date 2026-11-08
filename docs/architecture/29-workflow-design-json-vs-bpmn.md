# 工作流双轨模型：Design JSON（人机） vs BPMN（引擎）

> 版本：V1.0 | 2026-07-15 | 强制 | 上游：[27 地基契约](27-workflow-foundation-contracts.md)

## 1. 结论（先钉死）

| 用途 | 权威数据 | 禁止 |
|------|----------|------|
| 设计器 / 审批页流程图 / 当前步骤高亮 | **`design_json`（+ runtime 状态叠加）** | 反解析 BPMN XML 当产品 UI |
| Flowable 部署与执行 | **BPMN**（由 Design 编译生成） | 手写拼接 XML 字符串扩张业务 |

> 界面是 JSON 渲染的。流程图必须能用同一套 `nodes` 树 + `nodeKey → status` 画出来；BPMN 只服务引擎，不是给人看的中间态。

```
Design JSON (SoT for humans)
        │
        ├─► 前端竖向/网关图渲染（设计器 + 审批进度）
        │
        └─► DesignNodeHandler 校验
                └─► BpmnNodeEmitter（BpmnModel API）
                        └─► BpmnXMLConverter → bpmn_xml → Flowable deploy
```

## 2. 业界规范对照

1. **产品态 DSL**：自有 JSON（钉钉审批同样不是拿 BPMN XML 直接画业务图）。  
2. **引擎态模型**：用引擎官方 Model API 构图，再序列化为 XML；**禁止**业务代码 `xml.append("<userTask...")`。  
3. **运行时投影**：用历史/活动任务把状态投影到 DSL 节点 key 上，而不是用引擎图形坐标。

本仓库对应：

- DSL：`wf_model_version.design_json` / `process_config.design`  
- Model API：`org.flowable.bpmn.model.*` + `BpmnXMLConverter`  
- Emitter：`BpmnNodeEmitter`（Strategy，按 nodeType 扩展）

## 3. 审批流程图契约

实例详情（`WorkflowInstanceDetailVO`）须携带：

| 字段 | 含义 |
|------|------|
| `designJson` | 本实例对应版本的设计快照（可空：旧数据兜底 progressNodes） |
| `progressNodes` | `nodeKey` + `nodeStatus`（pending/active/completed/rejected）等运行投影 |

前端：

1. **有 `designJson`**：按设计器同构 JSON 渲染拓扑，用 `progressNodes` 染色当前/已过/驳回。  
2. **无 `designJson`**：退化为线性 Steps（现有 `progressNodes` / timeline）。

## 4. BPMN 生成规范（代码层）

```text
BpmnNodeEmitter.appendElements(Process, DesignStep, ctx)
BpmnNodeEmitter.appendFlows(Process, DesignStep, nextTarget, ctx)
BpmnModelCompiler.compile(modelKey, name, steps) → BpmnModel → XML
```

禁止：

- Emitter 内 `StringBuilder` 拼标签  
- ServiceImpl / Compiler 按 nodeType 写大段 if  
- **在 Emitter / BPMN 引擎侧写死中文或某一语种文案**

允许：

- `LinearBpmnBuilder`：DesignStep / 分支补齐  
- `BpmnElementIds`：**元素 id、连线 id、变量名、Listener、EL** 唯一入口  
- `BpmnTechnicalNames`：引擎侧 `name` 属性（语言中性，非产品文案）

## 4.1 国际化怎么做（钉死）

| 文案类型 | 放哪 | 国际化方式 |
|----------|------|------------|
| 用户看见的节点名（经理审批、财务…） | `design_json.nodes[].name` | 设计器按当前 locale 录入；后期可升级为 `nameI18n:{zh-CN,en-US}` 或 gen/`error` 键 |
| 审批流程图标签 | 同上 + 前端 i18n | **渲染只吃 JSON**，切换语言不重部署流程 |
| 引擎结构名（start/merge/resultGateway） | `BpmnTechnicalNames` | **永不本地化**；仅调试/Flowable 控制台可见 |
| 按钮/提示（通过、驳回） | 前端 `gen.*` / `common.*` | 平台现有 i18n，与 BPMN 无关 |

原则：**BPMN 部署产物不绑语言**。部署时用 MessageSource 把中文写进 XML = 锁死语种，切英文还得重新部署，错误。

## 4.2 BPMN 元素 ID 规约（钉死）

前缀不是随便写的，而是**进程内稳定约定**（编译 Emitter 与运行时 Listener / 变量读写必须同一套）。代码入口：`BpmnElementIds`。

| 类别 | 格式 | 示例（taskKey=`mgrApprove`） |
|------|------|------------------------------|
| userTask / 抄送 | `{taskKey}` = design `taskDefinitionKey` | `mgrApprove` |
| 审批结果网关 | `gw_{taskKey}` | `gw_mgrApprove` |
| 任务→结果网关 | `flowTask_{taskKey}` | `flowTask_mgrApprove` |
| 通过线 | `flowApprove_{taskKey}` | → 下一节点；条件 `${approved}` |
| 驳回线（默认流） | `flowReject_{taskKey}` | → `endRejected`（terminateAll，杀并发令牌） |
| 条件分裂 / 汇合 | `cgw_` / `mgw_` | `cgw_cond1` / `mgw_cond1` |
| 条件分支线 | `flowCond_{key}_{branchId}` / `flowCondDef_{key}` | |
| 并行 fork / join | `pgw_fork_` / `pgw_join_` | |
| 固定骨架 | `startEvent` / `endApproved` / `endRejected` / `flowStart` | 见 `WorkflowEndEvents` |
| 会签列表变量 | `{taskKey}AssigneeList` | 与 Listener 读写对齐 |
| 办理人 Listener | `workflowAssigneeTaskListener` / `…ExecutionListener` | Bean 名与 BPMN 委托一致 |

约束：

1. **Emitter 内禁止** `"gw_" + key` 这类字面量拼接；只调 `BpmnElementIds.*`。  
2. **改前缀 = 破坏契约**：须同步改本文档、Emitter、运行时读变量处，并评估已部署定义（旧实例仍按旧 BPMN）。  
3. **产品流程图不认这些 id**；只认 `design_json.nodes[].taskDefinitionKey` + `progressNodes`。

## 5. 扩展新节点

同时增加：

1. `DesignNodeHandler`（校验 + toStep）  
2. `BpmnNodeEmitter`（向 `Process` 加元素/线）  
3. 前端设计器节点组件（JSON 渲染）

## 6. 变更记录

| 日期 | 说明 |
|------|------|
| 2026-07-15 | V1.0：双轨职责 + 审批图 JSON 契约 + BpmnModel 生成规范 |
| 2026-07-15 | V1.1：禁止 BPMN 硬编码语种；`BpmnTechnicalNames` + 产品文案走 design JSON / 前端 i18n |
| 2026-07-15 | V1.2：`BpmnElementIds` 统一元素/连线/变量/Listener 规约；Emitter 禁止散落前缀 |
