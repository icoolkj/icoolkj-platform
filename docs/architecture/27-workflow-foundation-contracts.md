# 工作流地基契约：Design SoT + 启动变量

> 版本：V1.0 | 状态：**强制执行** | 上游：[29 JSON vs BPMN](29-workflow-design-json-vs-bpmn.md) · [24 跨租户](24-workflow-cross-tenant.md)

本文冻结 Phase A 两条契约，作为后续能力扩展的**不可绕过地基**。实现必须与本文一致；新增特判须先改本文。

---

## 1. Design SoT（设计唯一真源）

### 1.1 定义

| 层级 | 存储 | 职责 |
|------|------|------|
| **已发布版本权威** | `wf_model_version.design_json` | 随部署不可变；回放/字段权限/审计按版本读取 |
| **当前草稿** | `wf_model.process_config.design` + startAcl/behavior/notify | 设计器打开与全局属性编辑；成功 design-deploy 后与最新版对齐 |
| **BPMN XML** | `wf_model_version.bpmn_xml` | 仅由 Design → BPMN Emitter 生成，供 Flowable |

- **高级 XML 逃生舱**：允许运维覆盖 BPMN（`design_json` 可空），视为破坏 SoT；再次设计器「保存并部署」必须以 nodes 为准写回 `design_json` + BPMN。

### 1.2 `process_config` / `design_json` 形状

```json
{
  "startAcl": {
    "userIds": [],
    "deptIds": [],
    "roleCodes": [],
    "postIds": [],
    "tenantIds": []
  },
  "behavior": { "allowWithdraw": true, "commentRequired": false },
  "notify": { "onTodo": true, "onReject": true, "onComplete": true },
  "design": {
    "schemaVersion": 1,
    "nodes": [ /* 与 design-deploy 请求 nodes 同构 */ ]
  }
}
```

`design_json` 存的是上表中的 **`design` 文档本身**（含 schemaVersion + nodes），不含 startAcl。

`startAcl` 活动维：`userIds` / `deptIds` / `roleCodes` / `postIds`（任一命中即可；全空放行）。  
`tenantIds`：平台跨租户预留，**本版不参与** `isStartAllowed`。

规则：

1. `POST /workflow/model/design-deploy` **同一事务**：Strategy 校验 nodes → Emitter 生成 BPMN → `publishVersion(bpmn, designJson)` → 同步 `wf_approver_rule` → 写回 `process_config`（design + 全局属性）。
2. 设计器打开优先序：`version.design_json` → `process_config.design` → **规则线性回退 / 空画布**（**禁止**产品路径 BPMN 反解析）。
3. 节点类型扩展：新增 `DesignNodeHandler` + `BpmnNodeEmitter` Bean，**禁止**在 ServiceImpl 用 if 判断 nodeType。

### 1.3 与发布门禁

租户 OA `publishVersion` 仍校验：本租户模型已同步、已部署、`userTask` 审批人覆盖。部署成功后 version.design_json 与 BPMN/`wf_approver_rule` 必须一致。

---

## 2. 启动变量契约（StartVariableContract）

### 2.1 分层与合并顺序

`WorkflowStartCoordinator.buildStartVariables` **固定顺序**：

1. **系统保留变量**（不可被业务覆盖）  
2. **SPI `WorkflowStartPreparer`**（按 `bizType`；OA 注入条件字段）  
3. **请求 `variables`**：仅允许非保留键；与保留键冲突则忽略请求值并打 warn（或拒绝启动——本版 **忽略并保留系统值**）

### 2.2 系统保留键（`WorkflowVariables`）

| Key | 含义 |
|-----|------|
| `bizType` | 业务类型 |
| `bizId` | 业务主键 |
| `bizTitle` | 标题 |
| `tenantId` | 业务租户 |
| `starterUserId` / `starterUserName` | 发起人 |
| `modelId` | 生效模型 id |
| `approved` | 审批结果（运行期写入；启动时勿设） |

保留键集合以代码 `WorkflowVariables.reservedKeys()` 为准。

### 2.3 OA 条件字段注入（系统表单）

对 `bizType` 以 `oa_` 开头：

1. 读 `oa_instance` / `oa_instance_data.form_data`。  
2. 读实例绑定的 `tenant_tpl_ver_id` 字段定义中 `used_in_condition='1'` 的 `fieldCode`。  
3. 将对应表单值写入同名流程变量（键 = `fieldCode`）。  
4. 类型：数字尽量转 `Number`；布尔识别；其余 `String`；空值可省略键。

未标记 `used_in_condition` 的字段 **不** 注入，避免变量膨胀。

### 2.4 模型解析 SoT（运行）

- 发起：**始终** `requireActiveByBizType(tenantId, bizType)`（服从 `wf_biz_binding_policy`）。  
- `WorkflowStartRequest.modelId` 若传入，**仅校验必须与解析结果一致**，不一致抛业务错（防止锁与运行漂移）。  
- OA 模板版本上的 `wf_model_id`：发布时写入为审计/门禁；发起以 policy 为准，并可选与锁一致性校验。

---

## 3. 工程落点（代码）

| 契约 | 主要类 |
|------|--------|
| Design 编译 | `WorkflowDesignCompiler` + `DesignNodeHandler*` + `BpmnNodeEmitter*` |
| Design SoT 写入 | `WorkflowDesignSoTSupport` + `WorkflowVersionPublishCommand` |
| 配置解析 | `WorkflowProcessConfigSupport.ProcessConfig` |
| 启动合并 | `WorkflowStartCoordinator` |
| OA 注入 | `OaFormConditionStartPreparer` |
| 保留键 | `WorkflowVariables` |

---

## 4. 验收（Phase A + B.1）

1. 设计器保存后，`wf_model_version.design_json` 与请求 nodes 一致，且 `process_config.design` 对齐。  
2. 条件字段标记 `used_in_condition=1` 后发起 OA，Flowable 变量可见对应 fieldCode。  
3. 请求 variables 无法覆盖 `starterUserId` 等保留键。  
4. 传入错误 `modelId` 时启动失败（与 policy 解析不一致）。  
5. `behavior.allowWithdraw=false` 时撤回失败。  
6. 新增节点类型只需新增 Handler/Emitter Bean，不必改 ServiceImpl 分支。

---

## 5. 明确不做（本契约范围外）

- 钉钉级转交/加签/催办 — **Phase C 已落地**（`wf_task_action` + Task API；节点字段权限仍不做）  
- 新设计表（继续 version.design_json）  

---

## 6. 变更记录

| 日期 | 说明 |
|------|------|
| 2026-07-15 | V1.0 强制生效：Design SoT + StartVariableContract |
| 2026-07-15 | V1.1：`version.design_json` + Handler/Emitter 策略扩展；全局属性 UI；撤回 enforcement |
| 2026-07-15 | V1.2：设计器兜底改为规则/空画布；禁止产品 BPMN 反解析；种子 SoT + Listener 契约 |
| 2026-07-16 | V1.3：Phase C 转办/催办/驳回策略/超时 boundary/加签 + `wf_task_action` |
