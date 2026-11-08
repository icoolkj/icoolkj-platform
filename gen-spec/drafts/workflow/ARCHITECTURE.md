# 工作流框架架构（Phase 1 → Phase 2）

> 模块：`icoolkj-admin-workflow`（引擎与通用能力） + 业务模块 SPI 实现（如 `icoolkj-admin-system` 公告）

## 1. 设计目标

| 目标 | 说明 |
|------|------|
| **与平台多租户一致** | 业务租户边界由 `wf_biz_process.tenant_id` + 流程变量 `tenantId` 保证；Flowable 实例带 `tenantId` |
| **业务解耦** | 业务模块只实现 SPI，不直接操作 Flowable API |
| **可配置扩展** | 新 `bizType` = 模型配置 + SPI + 业务审批页；**工作流模块不含任何具体业务** |
| **严谨可靠** | 统一启动编排、访问校验、已办过滤、回调异常类型 |

## 2. 模块边界（强制）

**`icoolkj-admin-workflow` 只允许：**

| 类别 | 内容 |
|------|------|
| 引擎能力 | 启动 / 审批 / 撤回 / 待办 / 已办 / 实例轨迹 |
| SPI 接口 | `WorkflowStartPreparer`、`WorkflowBizHandler`、`WorkflowUserDisplayNameResolver` |
| 通用常量 | `bizType`/`bizId`/`approved`/`endApproved` 等平台约定 |
| 通用 UI 组件 | `WorkflowApproveLayout`、`WorkflowProgressPanel` |
| 配置驱动 | `wf_model.form_route` → 待办跳转；`application.yml` 声明 BPMN classpath（由业务模块提供） |

**禁止出现在 workflow 模块：**

- 具体业务的 BPMN、i18n、VO、Controller
- 业务域流程变量名（如 `deptLeaderUserId`）
- 业务 `bizType` 常量、硬编码审批路由 fallback

**业务模块（如 system）负责：**

| 职责 | 公告示例 |
|------|----------|
| BPMN 文件 | `system/resources/processes/notice_publish.bpmn20.xml` |
| 启动/结束 SPI | `NoticeWorkflowStartPreparer`、`NoticeWorkflowBizHandler` |
| 业务变量常量 | `NoticeWorkflowVariables` |
| 审批详情 API | `GET /system/notice/{id}/approve-detail` |
| 审批页面 | `views/system/notice/approve/index.vue`（复用 `WorkflowApproveLayout`） |
| gen-spec | `gen-spec/drafts/system/noticeApprove/` |

```
┌─────────────────────────────────────────────────────────────┐
│  业务模块 (system/xxx)                                       │
│  BPMN · StartPreparer · BizHandler · 审批页 · approve-detail │
└───────────────────────────┬─────────────────────────────────┘
                            │ SPI + wf_model.form_route
┌───────────────────────────▼─────────────────────────────────┐
│  icoolkj-admin-workflow（零业务）                            │
│  Process/Task/Instance/Model · Timeline · AccessGuard        │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│  Flowable + wf_model / wf_biz_process                        │
└─────────────────────────────────────────────────────────────┘
```

## 3. 分层结构

```
┌─────────────────────────────────────────────────────────────┐
│  API (icoolkj-admin-api)                                     │
│  WorkflowTask/Instance/ModelController + 业务扩展 Controller │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│  业务模块 (system/xxx)                                       │
│  XxxWorkflowStartPreparer  ──► 解析审批人/业务变量           │
│  XxxWorkflowBizHandler     ──► 流程结束同步业务状态           │
│  GET /xxx/{id}/approve-detail ──► 业务数据 + instance 轨迹   │
└───────────────────────────┬─────────────────────────────────┘
                            │ SPI
┌───────────────────────────▼─────────────────────────────────┐
│  icoolkj-admin-workflow                                      │
│  WorkflowStartCoordinator   启动编排（模型 + preparer 变量）  │
│  WorkflowProcessService     start / complete / cancel         │
│  WorkflowTaskService        待办/已办 + formRoute 填充        │
│  WorkflowInstanceService    已发申请 / 实例详情 + 访问控制    │
│  WorkflowModelRegistry      模型解析、部署、formRoute          │
│  WorkflowAccessGuard        实例可读/可撤回校验               │
│  WorkflowTenantSupport      租户 ID 归一化                    │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│  Flowable Process Engine (ACT_*)                             │
└─────────────────────────────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│  应用表 wf_model / wf_model_version / wf_biz_process         │
└─────────────────────────────────────────────────────────────┘
```

## 4. SPI 扩展点

### 4.1 `WorkflowStartPreparer`（业务启动前）

业务模块在流程启动前注入变量（审批人、业务上下文等）。

```java
public interface WorkflowStartPreparer {
    boolean supports(String bizType);
    Map<String, Object> prepareVariables(WorkflowStartContext context);
}
```

- 由 `WorkflowStartCoordinator` 按 `bizType` 自动发现并合并变量
- 业务域变量名定义在**业务模块**（如 `NoticeWorkflowVariables`），不进入 workflow 常量

### 4.2 `WorkflowBizHandler`（流程结束后）

流程到达终态或撤回时，同步业务表状态。

### 4.3 新增业务接入清单

1. 业务模块：BPMN + `application.yml` bootstrap 路径
2. `wf_model` 种子：`biz_type`、`form_route`（如 `/system/order/approve`）
3. 实现 `WorkflowStartPreparer` + `WorkflowBizHandler`
4. 业务 Service 调用 `workflowProcessService.startProcess(WorkflowStartRequest)`
5. 业务模块：`GET /{resource}/{id}/approve-detail` + 审批页（复用 `WorkflowApproveLayout`）
6. 隐藏菜单路由（`visible=1`）注册 component_path

## 5. 多租户与模型策略

### 5.1 模型归属（`wf_model.tenant_id`）

| 值 | 含义 | 管理角色 |
|----|------|----------|
| `0` | 平台模板，全租户可回落使用 | 平台管理员 |
| `{tenantId}` | 租户自定义，覆盖同 `biz_type` 平台模板 | 租户管理员（Phase 2 菜单开放） |

### 5.2 表单路由（`wf_model.form_route`）

待办「审批」按钮**只读** `WorkflowTaskVO.formRoute`，不做业务 fallback。

| 字段 | 示例 |
|------|------|
| `biz_type` | `sys_notice` |
| `form_route` | `/system/notice/approve` |

前端 `router.push({ path: formRoute, query: { taskId, bizId } })`。

### 5.3 Flowable 租户

- **流程实例** `tenantId` = 业务实体所属租户
- **任务查询** 以 `taskAssignee = userId` 为主；「已发申请」走 `wf_biz_process`（starter）

## 6. 核心流程

### 6.1 启动

```
业务 submitApprove
  → WorkflowStartCoordinator.buildVariables(request, model)
       → 标准变量 bizType/bizId/tenantId/starter*
       → 合并 WorkflowStartPreparer(s)
  → ProcessInstanceBuilder.tenantId(bizTenant).variables(...).start()
  → insert wf_biz_process (RUNNING)
```

### 6.2 审批

```
待办 formRoute → 业务审批页
  → 业务 approve-detail API（完整业务数据 + instance 轨迹）
  → POST /workflow/task/{taskId}/complete（通用）
  → 若流程结束 → WorkflowBizHandler
```

### 6.3 已办过滤

- `HistoricTaskInstance.deleteReason` 为空
- 所属流程实例非撤回

### 6.4 访问控制（`WorkflowAccessGuard`）

| 操作 | 规则 |
|------|------|
| 查看实例详情 | 发起人 **或** 历史/当前任务 assignee |
| 撤回 | 仅发起人且 RUNNING |
| 审批 | 仅当前 task assignee |

## 7. 与 gen-spec 关系

| gen-spec | 所属 | 运行时 |
|----------|------|--------|
| `workflowModel` / `workflowTask*` | `drafts/workflow/` | 通用 CRUD + 待办（**新业务无需改**） |
| `noticeApprove` | `drafts/system/` | 公告审批页（**业务模板**，Phase 1 手工实现） |
| `workflow-api-spec.yaml` | workflow | 通用 `/workflow/*` 契约 |
| **[CONFIGURATION.md](./CONFIGURATION.md)** | workflow | **配置体系、接入检查表、生成器边界** |

## 8. Phase 2 预留

- 租户管理员模型 CRUD/部署
- 可视化 BPMN 设计器
- 审批人策略配置化（角色/部门/表达式）
- Flowable 任务 DB 分页

## 9. 审批页模式（业务实现 + 通用组件）

| 区域 | 通用/业务 | 说明 |
|------|-----------|------|
| 布局壳 | 通用 `WorkflowApproveLayout` | 左业务 slot + 进度 + 右表单 slot |
| 业务详情 | 业务模块 | 公告：标题/封面/富文本 |
| 进度 | 通用 `WorkflowProgressPanel` | `instance.progressNodes` + `tasks` |
| 审批提交 | 通用 `POST /workflow/task/{id}/complete` | |

公告参考：`views/system/notice/approve/index.vue` + `GET /system/notice/{id}/approve-detail`

## 10. 关键类索引

| 类 | 包 |
|----|-----|
| `WorkflowStartPreparer` | `workflow.spi` |
| `WorkflowStartCoordinator` | `workflow.support` |
| `WorkflowAccessGuard` | `workflow.support` |
| `WorkflowInstanceTimelineBuilder` | `workflow.support` |
| `WorkflowApproveLayout.vue` | `frontend/components/workflow` |
| `WorkflowProgressPanel.vue` | `frontend/components/workflow` |
| `NoticeWorkflowStartPreparer` | `system.workflow`（示例） |
| `NoticeWorkflowBizHandler` | `system.workflow`（示例） |
| `NoticeWorkflowVariables` | `system.constant`（示例） |
