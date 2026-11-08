# 工作流 — 从零建库与分角色联调

> 架构说明见 [ARCHITECTURE.md](./ARCHITECTURE.md) · 配置与接入标准见 [CONFIGURATION.md](./CONFIGURATION.md)

适用环境：MySQL 8.x、JDK 21、Redis（可选）、dev 库名默认 `icoolkj_platform_dev`。

初始密码（V2 种子）：**StudyOnly@2026**

## 1. 从零建库

```sql
DROP DATABASE IF EXISTS icoolkj_platform_dev;
CREATE DATABASE icoolkj_platform_dev DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

启动 `icoolkj-admin-api`（Flyway 自动执行 V1–V2）。日志中应出现：

- `Flowable process deployed ... key=notice_publish`
- `Synced wf_model_version for key=notice_publish, version=1`

验证 SQL：

```sql
USE icoolkj_platform_dev;
SELECT model_key, latest_version FROM wf_model WHERE model_key = 'notice_publish';
SELECT version_no, published FROM wf_model_version WHERE model_id = '9000000000000000001';
SELECT COUNT(*) FROM act_re_procdef WHERE KEY_ = 'notice_publish';
```

## 2. 测试账号与角色

| 账号 | 密码 | 租户 | 角色 | 用途 |
|------|------|------|------|------|
| admin | StudyOnly@2026 | 平台 | 平台超级管理员 | 二级审批、流程模型管理 |
| tenant_admin | StudyOnly@2026 | 默认企业租户 | 租户管理员 | 创建/提交公告 |
| dept_approver | StudyOnly@2026 | 默认企业租户（tenantCode=`default`） | 部门审批人 | 一级审批 |
| org_admin | StudyOnly@2026 | 演示单位 | 单位管理员 | 非本流程主路径 |

## 3. 分角色联调步骤

### 3.1 平台管理员 — 确认流程可配置

1. 登录 **admin**，进入 **工作流 → 流程模型**
2. 应看到 **公告发布审批**（`notice_publish`），当前版本 ≥ 1
3. （可选）打开 **部署**，粘贴 `notice_publish.bpmn20.xml` 内容并部署新版本

### 3.2 租户管理员 — 发起公告审批

1. 登录 **tenant_admin**
2. **系统管理 → 通知公告** → 新增公告（草稿）
3. 列表点击 **提交审批** → `approve_status` 变为 **running**
4. **在线办公 → 已发申请** 应出现 **一条** **running** 记录（按业务单据去重；含公告等全业务）
5. 撤回：已发申请统一 `POST /workflow/instance/{id}/cancel`（业务态由 Handler 回写）
6. 关闭：在**公告列表** / **OA 草稿**操作，不在已发申请
7. 撤回 / 驳回后再提交：已发仍只显示 **最新一轮**；历史轮次在审批详情时间轴（processCycles）

### 3.3 部门审批人 — 一级审批

1. 登录 **dept_approver**（租户编码 **default**，勿用 platform）
2. **工作流 → 我的待办** → 点击 **审批**
3. 选择 **通过** 或 **驳回**，填写意见后提交

### 3.4 平台管理员 — 二级审批（通过路径）

1. 一级通过后，登录 **admin**
2. **我的待办** → 审批 → **通过**
3. 公告 `approve_status` → **approved**（并写 `finished_at`）；**不自动上架**，需列表 **发布** 后 `release_status=published` 门户才可见

### 3.5 驳回路径

1. 任一级选择 **驳回**
2. 公告 `approve_status` → **rejected**；**tenant_admin** 可修改后再次提交；有历史不可删，仅可关闭

### 3.6 撤回

1. **tenant_admin** 在 **审批中** 时进入列表 → **撤回**
2. 公告 `approve_status` → **cancelled**（保留 `process_instance_id`），Flowable 实例结束为 **cancelled**
3. **dept_approver** 的 **我的待办** 应清空；**我已处理** 仍展示本人已点过通过/驳回的记录

## 4. 数据核对

```sql
-- 公告状态
SELECT id, notice_title, approve_status, process_instance_id FROM sys_notice ORDER BY created_at DESC LIMIT 5;

-- 业务流程关联
SELECT biz_title, status, process_instance_id, starter_user_id FROM wf_biz_process ORDER BY start_time DESC LIMIT 5;
```

## 5. 前端页面（代码生成规范）

工作流相关页面均按 **GenCrudPage + `i18n/gen/{businessName}/view.json`** 实现：

| 菜单 | 页面 | businessName |
|------|------|----------------|
| 我的待办 | `frontend/src/views/workflow/task/todo/index.vue` | `workflowTaskTodo` |
| 我的已办 | `frontend/src/views/workflow/task/done/index.vue` | `workflowTaskDone` |
| 已发申请 | `frontend/src/views/office/submitted/index.vue` | `officeSubmitted`（`apiModule=workflow`；全 bizType） |
| 流程模型 | `frontend/src/views/workflow/model/index.vue` | `workflowModel` |
| OA 审批 | `frontend/src/views/office/approve/index.vue` | `officeApprove`（`apiModule/backendModule=oa`） |
| 公告审批 | `frontend/src/views/system/notice/approve/index.vue` | `noticeApprove`（system 模块，复用 WorkflowApproveLayout） |

i18n 与 view：`frontend/src/i18n/gen/{businessName}/`；后端镜像按 `backendModule`：
- workflow 页 → `icoolkj-admin-workflow/.../i18n/gen/`
- OA 页（含 `officeApprove`）→ `icoolkj-admin-oa/.../i18n/gen/`
- 公告审批 → `icoolkj-admin-system/.../i18n/gen/`

## 6. 常见问题

| 现象 | 处理 |
|------|------|
| 提交审批报「流程模型尚未部署」 | 确认 `wf_model.latest_version > 0` 且 Flowable 有 `notice_publish` 定义；重启触发 bootstrap sync |
| 待办为空 | 确认当前登录用户是否为任务 assignee（部门审批人 / admin） |
| Flyable 表在错误库 | 确认 `FlowableEngineConfiguration` 的 `databaseCatalog` 与 JDBC URL 一致 |
