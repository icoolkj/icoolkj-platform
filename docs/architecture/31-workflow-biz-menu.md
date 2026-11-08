# 工作流业务接入与菜单分层

> 版本：V1.1 | 2026-07-16 | 配套：[15 菜单权限](15-menu-permission.md) · [24 跨租户](24-workflow-cross-tenant.md)

本文约定：**租户侧栏怎么组织**，以及 **OA 可配置审批 vs 系统内置审批（公告型）** 如何选型与接入。

---

## 1. 菜单规范（先看这个）

> **可见菜单给人用；F 按钮挂在可见页下；禁止「整页隐藏只为挂权限」。**

| 类型 | 侧栏 | 用途 |
|------|------|------|
| **M / 可见 C** | 显示 | 租户真正点的入口 |
| **F** | 不显示（按钮） | 挂在对应 **可见 C** 下，供接口鉴权 |
| **hidden C** | 隐藏 | **仅**待办跳转的真页面（公告审批 / OA 审批） |

反例（已废弃）：再建一个 `visible=1` 的「我的待办」C，只为挂 `workflow:todo:*`——与「待我处理」重复。

正例：`workflow:todo:list` 等 F **直接挂在「待我处理」下**；租户侧栏看到的是「待我处理」。

唯一键：`(permission_code, assign_scope, deleted)`，故平台/租户可同码分 scope（如 `workflow:model:list`）。

### 1.1 ID 分块（V2 种子）

| 区间 | 目录 | scope |
|------|------|-------|
| `400–424` | 工作流 | PLATFORM |
| `500–535` | OA配置 | 混合 |
| `550–592` | 在线办公 | TENANT |

### 1.2 谁看见什么

| 角色 | 侧栏 |
|------|------|
| 员工 | 在线办公：发起…抄送、**已发申请** |
| 租户管理员 | 同上 + **流程监控**；OA配置 → 审批模板 |
| 平台超管 | 工作流（模型/规则/策略）+ 平台审批模板 |

租户**不出现**「工作流」目录。

---

## 2. 两条业务接入线

| 维度 | **OA 可配置审批** | **系统内置（公告型）** |
|------|-------------------|------------------------|
| 绑定 | `tenant_required` / `tenant_preferred` | **`platform_mandatory`** |
| 业务表 | `oa_instance` | 专用表（如 `sys_notice`） |
| 发起 | 在线办公 → 发起申请 | 业务模块 → 提交审批 |
| 审批页 | `/office/approve`（hidden C） | `/system/notice/approve`（hidden C） |
| OA 目录 | 平台审批模板 + 审批模板 | **不进** |
| 模型 | 租户向导可配（F 挂「审批模板」） | 平台种子 `tenant_id=0` |

### 2.1 独立业务审批字段包（公告型）

业务表挂审批流时，**审批列与码值固定**；业务结果另开列（如公告 `published_at`），勿用业务结果码顶替审批状态。

| 落库字段 | 码值 / 说明 |
|----------|-------------|
| `approve_status` | `draft \| running \| approved \| rejected \| cancelled \| returned \| closed`（常量见 `WorkflowBizApproveStatus`） |
| `process_instance_id` / `process_definition_key` | 当前或最近流程 |
| `starter_user_id` / `submitted_at` / `finished_at` | 发起人与时间 |
| 列表 VO | `hasApprovalHistory`、`allowWithdraw`（不落库） |
| 审批状态字典 | 跨业务：`wf_biz_approve_status`；OA：`oa_approve_status`；公告：`sys_notice_approve_status`（码值同 `WorkflowBizApproveStatus`） |
| 公告业务上架（非审批包） | `release_status`：`unpublished \| published`；仅 `approved` 可发布/下架；门户 = approved + published；`published_at` 为最近上架时间 |

生命周期：

- 无审批历史 → 可删
- 有历史 → **禁删，仅 close** → `closed`（关闭同时下架）
- `rejected` / `cancelled` / `returned` → 可编辑再发起；`returned` 保留 `processInstanceId`
- Handler：`approved` / `rejected` / `cancelled` / `returned` 按上表写状态；撤回保留 `processInstanceId`（勿清成 draft）；**审批通过不自动上架**
- 公告：`publish` / `unpublish` 管门户可见性

接入清单（公告型；OA 运行中撤回走已发 `workflow:instance:cancel`，无业务 withdraw F）：

```
□ bizType + platform_mandatory（或其它绑定策略）
□ 上表审批字段包 + WorkflowBizApproveStatus
□ submitApprove / withdraw / close（tenantId=业务行；withdraw 仅公告等业务列表入口）
□ AbstractWorkflowBizHandler + CrossTenantSupport（Handler **只写审批字段包**）
□ WorkflowBizHistorySupport 驱动删/关
□ hidden C 审批页 + workflow:approve:xxx:view
□ 业务 F：submit / withdraw / close（挂业务菜单；OA 无 withdraw）
□ 公告等需上架的业务：release_status + publish/unpublish F
□ 不进 oa_*_template（公告型）
□ cancel 原因码放业务常量（勿写入 workflow 模块词表）
```

---

## 3. 「已发申请」与业务处分工

| 入口 | 职责 | 权限 / API |
|------|------|------------|
| **已发申请** | **工作流视角**：每业务最新一轮；查看、审批中撤回 | `workflow:submitted:view/list` · `workflow:instance:cancel` · `GET /workflow/instance/submitted/page` · `POST .../cancel` |
| **我的草稿**（OA） | OA 单据：修改、提交、**关闭**、删除 | `oa:instance:*` · `/oa/instance/**` |
| **公告列表** | 公告单据：修改、提交、撤回、发布、**关闭** | `system:notice:*` · `/system/notice/**` |

### 3.1 边界（强制）

```
工作流模块（在线办公·已发 / 待办 / 已办）
  · 只碰 wf_biz_process + Flowable
  · 能力码：workflow:submitted:* / workflow:todo:* / workflow:done:* / workflow:instance:*
  · 侧栏壳：oa:office:{submitted|todo|done}:view（挂在「在线办公」）
  · 撤回 = cancelProcess → Handler.onProcessCancelled；已发 cancelInstance 固定 starter_withdraw
  · 不调用业务 close API
  · 已发 bizApproveStatus 字典：wf_biz_approve_status

业务模块（OA 草稿、公告列表）
  · 关闭 / 发布 / 删草稿；公告列表可业务侧撤回
  · OA 运行中撤回仅已发入口（无 oa:instance:withdraw）
  · 提交审批 = startProcess
  · 原因码：OaWorkflowCancelReasons / NoticeWorkflowCancelReasons

WorkflowBizHandler
  · 只写：approve_status / process_instance_id / finished_at（returned → finished_at=null）
```

### 3.2 列表粒度

- **已发单位** = `(bizType, bizId)` 最新一轮主流程（`parent_instance_id` 空）；历史轮次只在详情 `processCycles`
- 关闭写业务 `approve_status=closed`，**不改** `wf_biz_process.status`；已发可展示 `bizApproveStatus` 供对照

### 3.3 种子数据

- 初始化脚本**仅 INSERT**（新库），禁止用 UPDATE 迁菜单
- 公告关闭 F（`system:notice:close`）挂**公告管理**；OA 关闭 F（`oa:instance:close`）挂**在线办公**

---

## 4. 相关文档

- [15](15-menu-permission.md) · [24](24-workflow-cross-tenant.md) · [27](27-workflow-foundation-contracts.md)
