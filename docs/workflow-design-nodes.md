# 工作流设计节点能力地图

JSON（`process_config.design`）为权威设计模型；BPMN 由 `BpmnNodeEmitterRegistry` + Flowable Model API 生成。  
元素 id / 变量 / Listener 规约见 [`BpmnElementIds`](../backend/icoolkj-admin-workflow/src/main/java/com/icoolkj/platform/workflow/design/bpmn/BpmnElementIds.java) 与 [architecture/29 §4.2](architecture/29-workflow-design-json-vs-bpmn.md)。  
完整契约：见 [architecture/27-workflow-foundation-contracts.md](architecture/27-workflow-foundation-contracts.md) · [29](architecture/29-workflow-design-json-vs-bpmn.md)。

## 产品节点 ↔ Flowable

| 档 | 产品 `nodeType` / 字段 | Flowable | 状态 |
|----|------------------------|----------|------|
| 已有 | `approve` | `userTask` + 通过/驳回 `exclusiveGateway` | 已实现 |
| 已有 | `cc` | `userTask`（直通下一节点） | 已实现 |
| 已有 | `condition` | `exclusiveGateway` 多出口 + `mgw_` 汇合 | 已实现 |
| A | `parallel` | `parallelGateway` fork + join | 已实现 |
| A | `approve.passMode` = `any` / `all` / `sequentialAll` | `multiInstanceLoopCharacteristics` | 已实现 |
| A | `inclusive` | `inclusiveGateway` | 已实现 |
| P1 | `approve.allowDelegate` | 任务委派（local 变量记录原办理人） | 已实现 |
| P2 | `delay` | `intermediateCatchEvent` + timer | 已实现 |
| P2 | `service` | `serviceTask` + `delegateExpression` | 已实现 |
| P2 | `subprocess` | `callActivity`（`inheritVariables=true`） | 已实现 |
| 延期 | message / signal / compensation | — | 不做 |

## JSON 要点

- 网关类（`condition` / `parallel` / `inclusive`）统一使用 `branches[]`；子节点 v1 仅 `approve` / `cc`，禁止再嵌套网关 / delay / service / subprocess。
- `condition` / `inclusive`：多条条件 + 一条 `isDefault`；非默认须填 `expression`。`inclusive` 可同时走多条为真的分支后 OR 汇合。
- **驳回语义**：任一审批节点驳回（含包容/并行分支内）→ `endRejected`（terminateAll），整单终止为驳回，不会再经汇合/抄送走到通过。
- 排他条件部署前校验（前端硬拦）：表达式非空、不可完全重复、范围下限须小于上限；同字段两条开口「> / >=」且前序阈值更小时视为后条不可达并拦截「保存并部署」。
- `parallel`：至少两路；无表达式、无 default。
- `passMode`：`sequential`（默认，单人）| `any`（或签）| `all`（会签）| `sequentialAll`（顺序会签）。会签集合变量约定：`{taskDefinitionKey}AssigneeList`，元素变量仍为 `variableName`。
- `allowTransfer` / `allowDelegate` / `allowAddSign`：缺省 `true`；节点缺失 design 快照时后端 fail-closed。
- `delay`：`dueDuration` 必填（ISO-8601，如 `PT24H`）；BPMN 元素 id 为 `delay_{taskDefinitionKey}`。
- `service`：`strategyConfig` 必填 JSON `{"delegateExpression":"${myBean.execute(execution)}"}`。
- `subprocess`：`strategyConfig` 必填 JSON `{"calledElement":"modelKey"}`；子流程变量继承；callActivity 启动时由 `WorkflowSubprocessStartedListener` 写入 `wf_biz_process.parent_instance_id`（无子行则按父单补桥接行）。
- 业务态：`running` / `returned`（starterEdit 退回修改）/ `approved` / `rejected` / `cancelled`。
- 委派：被委派人通过后归还原办理人再办结；可选 `delegateUntil` 过期后禁止提交。

## 示例：并行 + 会签

```json
{
  "schemaVersion": 1,
  "nodes": [
    {
      "nodeType": "parallel",
      "taskDefinitionKey": "par1",
      "name": "并行审批",
      "variableName": "par1",
      "strategyType": "expression",
      "strategyConfig": "{}",
      "resolveScope": "biz_tenant",
      "sortNo": 1,
      "branches": [
        {
          "id": "p1",
          "name": "并行1",
          "nodes": [
            {
              "nodeType": "approve",
              "taskDefinitionKey": "financeApprove",
              "name": "财务",
              "variableName": "financeApproveUserId",
              "passMode": "all",
              "strategyType": "role",
              "strategyConfig": "{\"roleCodes\":[\"finance\"]}",
              "resolveScope": "biz_tenant",
              "sortNo": 1
            }
          ]
        },
        { "id": "p2", "name": "并行2", "nodes": [] }
      ]
    }
  ]
}
```
