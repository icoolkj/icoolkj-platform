# AI 异步任务与业务对接约定

> 更新：2026-07-30 · master 平台收口：`infra_async_task` + AI Catalog/协议；业务只写场景 Handler / API

## 1. 职责边界（禁止自建第二套异步）

| 归属 | 负责 |
|------|------|
| **master / 平台** | `infra_async_task` 生命周期、投递（local / redis-stream）、轮询 `GET /infra/asyncTask/mine/{id}`、`AsyncTaskHandler` SPI、租户/Ownership、提交者 RBAC 重建、重试策略、`result_data` 约定、AI Catalog 解析、OpenAI Compatible 协议客户端 |
| **业务平台** | 场景 Handler（`@Component` + `taskType()`，**API 与 Worker  classpath 都要有**）、业务提交 Facade/API、前端 `pollAsyncTask`、消费 `result_data` |

短任务（通常 &lt;15–30s）可继续同步 API；长任务走平台异步。**流式输出 ≠ 异步任务替代**。

## 1.1 同步 vs 异步 UX（强制）

| 类型 | 默认 | 用户选择 | 禁止 |
|------|------|----------|------|
| **交互试跑 / 生成**（文生图等） | **同步**主按钮 | 双按钮：「生成图片」/「异步生成」 | 前端静默改走 async；选异步却静默回落同步 |
| **批处理 / 大文件**（Excel 导入导出） | **异步** | 导出：点击后弹框选通道；导入：对话框内选通道 | 把异步伪装成瞬时同步下载；用户选异步却静默回落 |
| **流式对话** | 同步 / SSE | — | 用 `infra_async_task` 冒充流式 |

选异步且遇到 `error.async.disabled`：**交互类**明确提示失败；**Excel 类**允许回落同步（用户意图是导入/导出，不是选通道）。

运行时通道详见 [34-platform-job.md](34-platform-job.md)；Excel 金样例见 [35-excel-export-import.md](35-excel-export-import.md)。

## 2. 生命周期（业务只需对接这几步）

```
业务 API ──submitWithId──► AsyncTaskRuntime
                              │ insert PENDING + Ownership
                              ▼
                         AsyncTaskBus.publish
                              │
                    ┌─────────┴─────────┐
                 local               redis-stream
                    │                   │
                    └─────────┬─────────┘
                              ▼
                    AsyncTaskHandler.handle
                              │
                         result_data JSON
                              ▼
            前端 poll GET /infra/asyncTask/mine/{taskId}
```

| 状态 | 码 | 说明 |
|------|----|------|
| PENDING | `0` | 已入队未执行 |
| RUNNING | `1` | Worker 执行中 |
| SUCCESS | `2` | 成功，读 `resultData` |
| FAILED | `3` | 失败，读 `errorMessage`（i18n key） |
| CANCELLED | `4` | 已取消 |

**轮询**：只用 `GET /infra/asyncTask/mine/{id}`（登录即可，勿挂列表权限）。前端统一用 `utils/asyncTaskPoll.ts` 的 `pollAsyncTask`。

## 3. Handler SPI

```java
@Component
public class XxxAsyncHandler implements AsyncTaskHandler {
    @Override
    public String taskType() { return "biz.scenario.name"; } // 全局唯一

    @Override
    public String handle(AsyncTaskContext ctx) throws Exception {
        // 1. 反序列化 ctx.requestParam()
        // 2. 调业务 Service（租户/权限已由平台重建）
        // 3. return result_data JSON（可为 null）
    }
}
```

- Handler 必须是 Spring Bean，由 `AsyncTaskRegistry` 按 `taskType` 收集。
- **禁止**在 Handler 内自建线程池 / 自建轮询表。
- `BusinessException`（`error.*`）→ FAILED **不重试**；瞬时技术异常 → 回 PENDING 重试（见 34）。

## 4. `result_data` 与 `fileId` 约定

凡产出可下载/可预览文件的异步任务，顶层必须带平台文件结果字段（与 Excel 导出对齐）：

```json
{
  "fileId": "…",
  "fileName": "…"
}
```

对应 VO：`com.icoolkj.platform.infra.vo.AsyncTaskFileResultVO`（Excel 导出 `ExcelExportAsyncResultVO` 继承同一约定）。

业务可扩展字段，但 **不得去掉** `fileId` / `fileName`。文生图金样例扩展为 `AiAsyncImageResultVO`：

```json
{
  "fileId": "首图",
  "fileName": "ai-image-{fileId}.png",
  "invokeId": "…",
  "prompt": "…",
  "model": "…",
  "providerName": "…",
  "size": "1024x1024",
  "n": 1,
  "items": [{ "fileId": "…", "url": "…" }]
}
```

前端成功后：预览用 `items[].url`（或 `GET /infra/file/{fileId}/…`）；下载用 `fileId`。

## 5. 租户 / 权限 / 重试（业务侧注意点）

| 项 | 约定 |
|----|------|
| 租户 | 提交时写入 Ownership；Worker 经 `AsyncTaskPrincipalResolver` 重建，Handler 内 Service 照常走租户线 |
| 权限 | **提交 API** 上 `@PreAuthorize`；轮询 `/mine/{id}` 仅本人任务 |
| 重试 | 业务可预期失败请抛 `BusinessException("error.xxx")`，避免无意义重试 |
| `error_message` | 存 i18n key（可带 args），前端用 `resolveAsyncTaskErrorMessage` / `resolveI18nPayloadMessage` |
| 异步关闭 | `error.async.disabled` → 前端回落同步 API（与 Excel 一致） |

## 6. AI Catalog / 协议（master 唯一入口）

| 能力 | 约定 |
|------|------|
| 解析 | 只经 `AiCapabilityRuntimeService.resolve(AiCapability.*)`，禁止读遗留 DeepSeek `sys_config` |
| 协议 | `openai_compatible`；Base URL 必须是 API Root（禁止资源后缀） |
| 客户端 | `OpenAiCompatible*`（chat / image）；参数合并走白名单 |
| 绑定 | Catalog：`tool_ai_provider` / `tool_ai_model` / `tool_ai_capability_binding` |

异步 Handler 内调用业务 Service 即可；**不要**在 Handler 里再解析 Catalog。

## 7. 平台保留 taskType / bizType

| 常量 | 值 | 说明 |
|------|-----|------|
| `AiAsyncTaskTypes.IMAGE_GENERATE` | `ai.image.generate` | 文生图（已实现 Handler） |
| `AiAsyncTaskTypes.CONTENT_GENERATE` | `ai.content.generate` | 内容生成（预留，业务实现 Handler） |
| `AiAsyncConstants.BIZ_TYPE_IMAGE` | `ai-image` | 任务 bizType |
| `AiAsyncConstants.BIZ_TYPE_CONTENT` | `ai-content` | 预留 |

业务自定义 `taskType` 使用反向域名风格：`{product}.{domain}.{action}`，避免与 `excel.*` / `ai.*` 冲突。

## 8. 文生图金样例（业务照抄）

| 层 | 路径 / 接口 |
|----|-------------|
| 提交 Facade | `AiImageAsyncSubmitFacade` |
| Handler | `AiImageGenerateAsyncHandler`（`ai.image.generate`） |
| 同步 | `POST /tool/ai-image/generate` |
| 异步 | `POST /tool/ai-image/generate-async` → `{ taskId }` |
| 轮询 | `GET /infra/asyncTask/mine/{taskId}` |
| FE | **双按钮**：「生成图片」同步 /「异步生成」异步；选异步且 `error.async.disabled` 时明确提示，**不**静默回落 |

```text
POST /tool/ai-image/generate-async
  → AsyncTaskSubmitVO.taskId
  → pollAsyncTask(taskId, { timeoutMs: 10 * 60 * 1000 })
  → JSON.parse(task.resultData)  // AiAsyncImageResultVO
```

## 9. 业务平台接入清单

1. 依赖已含 `infra`（经 system / generator 传递即可）。
2. 定义 `TASK_TYPE` 常量；实现 `AsyncTaskHandler` + `@Component`。
3. Facade：`IdGeneratorUtils.nextIdStr()` → `asyncTaskRuntime.submitWithId(...)` → `AsyncTaskSubmitVO`。
4. Controller：业务权限 + 返回 `taskId`；**不要**再暴露第二套任务查询 API。
5. 前端：`pollAsyncTask` + 解析 `resultData`；文件类结果遵守 `fileId`/`fileName`。
6. 需要文件暂存绑定时：以 **taskId** 为 `bizId`（参考 Excel import）；纯产出文件可绑业务主键（文生图绑 `invokeId`）。
7. **redis-stream**：Handler 所在模块须加入 **Worker** 依赖（文生图：`icoolkj-admin-generator` → `icoolkj-admin-worker`），否则 Worker 报 `error.async.handler_not_found` /「未注册的异步任务类型」。

## 10. 禁止事项

- 禁止新建 `xxx_async_task` 表或自建轮询通道。
- 禁止把用户异步接到 `sys_job`。
- 禁止绕过 Catalog 硬编码厂商 URL / Key。
- 禁止在 `result_data` 中只返回临时外链而不落 `infra_file`（平台侧无法统一权限与生命周期）。
