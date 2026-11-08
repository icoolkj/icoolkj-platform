# 平台定时任务与异步任务

> 模块：`system` + `infra` + `worker`

## 1. 两套能力，禁止混用

| 能力 | 表 | 模块 | 触发 | 归属 |
|------|-----|------|------|------|
| 平台定时任务 | `sys_job` / `sys_job_log` | system + worker | Cron / 立即执行 | 手动有 `trigger_user_id` |
| 业务异步任务 | `infra_async_task` | infra | 用户 API → `AsyncTaskRuntime` / `AsyncTaskBus` | L3：`user_id` = 创建者；管理端只读+终态删除；操作审计走 `sys_operation_log` |

平台跑批只写 `sys_job_log`，禁止写入 `infra_async_task`。  
用户异步走 **独立 Redis Stream**（`icoolkj:async:task:stream`），禁止复用 `sys_job` 的 Redis List。

## 2. 结构

```
framework/job/
  JobHandler / JobRunner / ManagedJobCatalog / ManagedJobTriggerService

system/
  SysJob* / SysJobLog* / JobRunnerImpl
  API: /monitor/job、/monitor/jobLog

worker/job/
  ManagedJobCronDispatcher
  ManagedJobCommandConsumer
  handler/*JobHandler

worker/async/
  AsyncTaskStreamPoller   # Redis Stream 消费轮询

infra/async/
  AsyncTaskRuntime / AsyncTaskBus / AsyncTaskHandler / AsyncTaskRegistry
  LocalAsyncTaskBus（provider=local）
  redis/RedisStreamAsyncTaskBusImpl + Publisher + Consumer（provider=redis-stream）
  DefaultAsyncTaskExecutor
```

### 2.1 业务异步任务

| 项 | 说明 |
|----|------|
| 提交 | `AsyncTaskRuntime.submit` → insert PENDING → `bus.publish`；**publish 失败** → 标 FAILED（`error.async.publish_failed`）+ 抛出，避免孤儿 PENDING |
| local | `LocalAsyncTaskBus` 有界线程池 + 复制 Security/Tenant |
| redis-stream | `XADD`（taskId/tenantId/userId）；worker 消费 → 重建上下文 → Executor → `XACK`；超限进 DLQ |
| 查询 | 提交者轮询 `GET /infra/asyncTask/mine/{id}`；管理端列表/详情/删除亦**仅本人**（与 Excel 暂存对称；删任务会 `releaseBizFiles`） |
| 金样例 | User Excel：`POST /system/user/import-async`，`taskType=excel.import.user`；AI 文生图：`POST /tool/ai-image/generate-async`，见 [37-ai-async-task.md](37-ai-async-task.md) |

**YAML 只露决策（禁止抄队列/重试/DLQ；默认值在 `AsyncProperties`）：**

| 文件 | 写什么 |
|------|--------|
| `application-icoolkj-shared-*.yml` | `icoolkj.async.provider: redis-stream` 或 `local` |
| Worker `application.yml` | `icoolkj.async.consume: true` |
| API | **不写** async（不消费；provider 继承 shared） |

| provider | 含义 |
|----------|------|
| redis-stream | API 只投递，须起 Worker；**Handler 必须同时在 API（提交校验）与 Worker（执行）注册** |
| `local` | API 进程内执行（单机/单测） |

**禁止**：租户 / `sys_config` 配置 `provider` 或 `consume`（通道是部署拓扑，不是业务策略）。

开发环境 shared-dev 默认 `redis-stream`。未配时 Java 默认 `provider=local`、`consume=false`。

禁止把用户导入接到 `sys_job`。

### 同步 vs 异步（产品 UX）

- **交互试跑 / 生成**：默认同步，页面显式可选异步（金样例：文生图），见 [37-ai-async-task.md](37-ai-async-task.md) §1.1。
- **批处理 Excel**：导出点击后弹框选同步｜异步（默认异步）；导入在对话框内选择；禁止静默把交互能力改走后台异步。

### 失败与重试（统一）

Local 与 Redis Stream 均经 `AsyncTaskRetrySupport` + `AsyncTaskRetryPolicy` 决策：

| 情况 | 行为 |
|------|------|
| `BusinessException`（`error.*` key） | FAILED + ACK，**不重试** |
| `Error` / classpath（如 `NoClassDefFoundError`） | FAILED + ACK + DLQ，**不重试** |
| 其它瞬时技术失败 | 回 PENDING 再投递，最多 `retry.maxTimes`（默认 5） |
| Worker 未启动 | 任务一直 PENDING；前端轮询约 90s 后停止空转；后台对账任务有限重投后置 FAILED |

DB 是状态 SoT；Stream / Local 队列只负责投递。`error_message` 优先存 i18n key（可带 args），展示侧按 locale 翻译。

### PENDING / RUNNING 对账（`async-task-pending-reconcile`）

Worker 托管任务（`ManagedJobCatalog`，默认每 2 分钟）：

| 状态 | 条件 | 动作 |
|------|------|------|
| PENDING | `updated_at` 超过 `reconciliation.pendingTimeoutSeconds`（默认 120s） | `requeue_count++` 后 `AsyncTaskBus.publish`；达到 `maxRequeueAttempts`（默认 3）→ FAILED `error.async.pending_timeout` + DLQ + error 日志 |
| RUNNING | `updated_at` 超过 `runningTimeoutSeconds`（默认 600s） | **不重投**，FAILED `error.async.running_timeout` + DLQ（防双写） |
| 终态 excel-import / excel-export | `finished_at` 超过 `excelImportStagingRetentionDays`（默认 7） | `ExcelImportStagingCleanupService.releaseExpiredStaging`（任务行保留） |
| 孤儿 excel staging | 无匹配任务且超过 `excelImportOrphanStagingHours`（默认 6h） | `ExcelImportStagingCleanupService.releaseOrphanStaging` |

默认值在 `AsyncProperties.Reconciliation`，勿抄进 YAML。与前端 90s UX 正交：FE 停转圈，对账保正确性。

## 3. 约束

- `JobHandler` 代码注册，`job_code` 白名单
- 禁止库内配置任意类/方法名执行
- Cron 执行不写归属用户；手动执行记 `trigger_user_id`
- `sys_job*`：L1 + L2，无业务 SELF L3
- `infra_async_task*`：归属创建者；管理端 page/get/delete **强制本人**（非 DataScope ALL 可看全租户）；`AsyncTaskHandler` 代码注册
- worker 依赖 `system` + `infra` + `biz`，以便扫到各模块 Handler

## 4. 前端 / 菜单

| 页面 | 路径 | 权限 |
|------|------|------|
| 定时任务 | `/system/job` | `monitor:job:view` / `list` / `run` / `update` |
| 任务日志 | `/system/jobLog` | `monitor:jobLog:view` / `list` |
| 异步任务 | `/infra/asyncTask` | `infra:asyncTask:view` / `list` / `delete`（只读+终态删除；详情弹窗；删除 `excel-import` 任务时 `releaseBizFiles`） |
| 备份策略 | `/infra/databaseBackup` | `monitor:dbBackup:*` |

用户操作审计统一看 **操作日志**（`/system/operationLog`，`sys_operation_log`）。`infra_async_task_log` 表保留为空壳，**不挂菜单**（请求/结果/错误在任务详情中查看）。

菜单挂在「监控审计」下（`PLATFORM`）：`定时任务` → 查询/执行/修改；`任务日志` → 查询。
