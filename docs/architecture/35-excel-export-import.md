# Excel 导出 / 导入契约

> 更新：2026-07-24 · 与 Gen CRUD / 等保脱敏对齐（积木化 + 异步导入/导出收口）

## 原则

1. **导出列 = 服务端白名单**：`*ExportRow` + `@ExcelI18nProperty` 是运行时唯一列定义。
2. **不要**用运行时 `view.json` / 前端列配置驱动 Excel 列（UI 布局 ≠ 导出契约；等保不允许客户端决定导出字段）。
3. **列表与导出同源**：Gen `isList` / `isExport` 决定 ExportRow；**导入列独立**用 `isImport`（**默认关**，不跟 `isInsert` 同步）。
4. **查询权限**：`GET /export` 与 `POST /export-async` 必须走 `service.page`（继承租户线 + `@DataScope`），分页写出；超过安全上限（5 万）明确失败，**禁止静默截断**。
5. **导入与导出分通道**：仅当字段勾选 `isImport` 时生成导入；工具栏只出 `builtin: import`，对话框内「下载模板（i18n 表头）→ 按模板填写 → 上传」；绑定靠 `@ExcelProperty(index)`。
6. **导入默认全有或全无**：`readRows`（先校验 .xlsx 魔数/10MB）+ `Service.importRows` → `ExcelImportResult`（含 `errorTotal`）；任一行失败则 `success=0` 且不落库。
7. **导出先预检再下发**：`writeLocalizedPaged` / `writeLocalizedPagedTo` 先查 `total`，超限在写响应头/文件之前失败，避免 HTTP 200 + 残缺文件。
8. **大导出用异步**：同步 `GET /export` 在写出响应头后若分页中途失败可能留下残缺 xlsx；Gen 点击「导出」→ **弹框选择执行方式（同步｜异步）**，默认异步。交互型 AI 双按钮见 [37-ai-async-task.md](37-ai-async-task.md) §1.1。

## 布局支持矩阵

| 布局组件 | 导出 | 导入对话框 | 实现 |
|----------|------|------------|------|
| `GenCrudPage` | ✅ | ✅ | `GenExcelToolbarActions` + `useGenExcelToolbar` |
| `GenTreeCrudPage` | ✅ | ✅ | 同上；导出 query **剥离** `pageNo/pageSize` |
| `GenMasterDetailPage` | ✅ 主表 / ✅ 明细 | ✅ 主表 / ⚠️ 明细不生成导入 | 明细工具栏仅 `export`（无法向 Excel 行注入主表外键）；需先选中主表 |

## 前端积木

| 模块 | 职责 |
|------|------|
| `types/gen/crudApi.ts` | `GenCrudApi` / `GenCrudPerm`（Dialog/布局共享，勿挂在页面组件上） |
| `useGenExcelToolbar` | 工具栏可见性/权限/样式/导出/打开导入 |
| `GenExcelToolbarActions` | 按钮 + `GenExcelImportDialog` 挂载 |
| `GenExcelImportDialog` | 模板下载、上传预检、错误列表、`importFile` / `importFileAsync`；步骤提示优先 `page.importStep*Hint`，回退 `common.*` |
| `utils/excelImport.ts` | `unwrapExcelImportResult` / `validateExcelImportFile` |
| `utils/asyncTaskPoll.ts` | 轮询 `GET /infra/asyncTask/mine/{id}` 至终态 |
| `utils/download.ts` | 统一 blob 下载（`X-Download-Filename` 优先） |

## 运行时组件（后端）

| 类 | 职责 |
|----|------|
| `ExcelExportSupport` | `loadRows`、`writeLocalizedPaged`（HTTP）、`writeLocalizedPagedTo`（异步 OutputStream）、`writeLocalized`、`@ExcelMask` |
| `AnnotatedColumnWidthHandler` | 自定义 head 路径下应用字段 `@ColumnWidth` |
| `ExcelFileNames` | 文件名/Sheet 消毒；`applyAttachmentHeaders`（`X-Download-Filename` + 旧头） |
| `ExcelValueFormatters` | status / datetime / page 文案 / audit 文案 |
| `ExcelImportSupport` | 空模板、`assertXlsxUpload`、字典/部门/岗位/角色下拉、`readRows`、index 连续校验 |
| `ExcelImportLookup` | 字典/部门/岗位/角色：下拉 + 名称→id 解析 |
| `NoopExcelImportLookup` | 无 system 实现时 **失败关闭**（禁止透传脏码） |
| `@ExcelImportDict` / `@ExcelImportRef` | 字典；`DEPT_NAME` / `POST_NAME` / `ROLE_NAME` |
| `ExcelImportResult` | `{total,success,skipped,errorTotal,errors[]}`；`errors` 可截断，以 `errorTotal` 为准 |
| `ExcelImportAsyncSubmitFacade` | 预分配 `taskId` → `uploadExcelImportStaging` → `submitWithId`；失败 `releaseBizFiles` |
| `ExcelExportAsyncSubmitFacade` | 预分配 `taskId` → `submitWithId`（query JSON）；Worker 写出后 `storeExcelExportStaging` |
| `AbstractExcelImportAsyncHandler` | 读 `fileId` → **file 行锚定 + refRole=import** → `assertXlsxResource` → `readRows` → `importRows` |
| `AbstractExcelExportAsyncHandler` | 反序列化 query → `writeLocalizedPagedTo` → staging → `result_data={fileId,fileName}` |
| `ExcelImportAsyncConstants` | `BIZ_TYPE=excel-import` / `REF_ROLE=import` |
| `@ExcelI18nProperty` | 列头 i18n key |
| `@ColumnWidth` | 列宽（字符）；由 Handler 写出 |
| `@ExcelMask(PHONE\|EMAIL\|NAME\|IP\|ID_CARD\|BANK_CARD)` | 写出时掩码 |
| `@ExcelProperty(index)` | 导入列位置（必填，从 0 连续） |

## Gen 生成物

| 开关 | 生成 |
|------|------|
| `isExport=1` | `ExportRow`、`GET /export`、`POST /export-async`、Export Handler、菜单 `:export`、toolbar `builtin: export`、vue `exportFileAsync` |
| `isImport=1` | `ImportRow`、模板/`POST /import`/`POST /import-async`、Handler、`Service.importRows`（BV + **文件内唯一键** → `create()` + 事务回滚）、vue `importFileAsync` 接线 |

i18n：`page.exportFileName` / `exportSheetName` / `exportAsyncTaskName` / `importTemplateFileName` / `importSheetName` / `importAsyncTaskName` / `importStep*Hint`（`_codegen` 模板已含）

默认 `importRows`：Bean Validation → 文件内唯一键（required String：`username`/`code`/`account`/`*Code`/`*No`/`*Key`）→ `toCreateRequest` → `create()`（含 Ownership）；复杂唯一性对照 User 金样例扩展，**不要**绕过 `create` 直接 mapper insert。行错误用 `ExcelImportI18n.encode` / **`encodeViolation`**（优先 `{gen.*.*.*}` messageTemplate）存 key，勿 `i18n.resolve` bake Worker 语言。前端 Dialog 经 `gv` 解析 `gen.{business}.{field}.{rule}`。

## 参考样板

**用户管理**（`system:user`）为 Gen 导入/导出金样例；登录/操作日志为「仅导出」异步样板：

| 产物 | 路径 |
|------|------|
| ExportRow | `system/excel/UserExportRow.java`（PHONE/EMAIL/NAME 掩码，含 nickname） |
| ImportRow | `system/excel/UserImportRow.java`（`@ExcelProperty(index)` + i18n 表头） |
| API | `GET /export`、`POST /export-async`、`GET /import-template`、`POST /import`、`POST /import-async` |
| Service | `importRows`：BV + 文件内去重（`import_duplicate_in_file`）+ 部门 DataScope + 落库异常回滚映射行错误 |
| 异步导入 | `UserExcelImportAsyncHandler`（`excel.import.user`） |
| 异步导出 | `UserExcelExportAsyncHandler`（`excel.export.user`）；日志：`LoginLog`/`OperationLog` `*ExcelExportAsyncHandler` |
| 测试 | `UserServiceImplImportRowsTest` / Facade·Handler 单测 / `AsyncTaskRuntimeLocalTest` |
| view | `toolbarActions`: `export` + `import`；日志页仅 `export` + `exportFileAsync` |
| 权限 | `system:user:export` / `system:user:import`；日志 `:export` |

## 异步导入

- **同步** `POST /import` 仍保留（Gen 与金样例均保留）。
- **异步**：`POST .../import-async` → `ExcelImportAsyncSubmitFacade`（预分配 `taskId` → 上传并绑引用 `bizId=taskId` → `submitWithId`；失败则 `releaseBizFiles`）→ 轮询 **`GET /infra/asyncTask/mine/{id}`**；`result_data` = JSON(`ExcelImportResult`)。
- **投递失败**：`AsyncTaskRuntimeImpl` 在 insert 后 `publish` 失败时将任务标为 **FAILED**（`error.async.publish_failed`）；mark 失败则硬删任务行；Facade 仍 `releaseBizFiles`。
- **保留字**：客户端 `upload`/`addRef`/`syncBizFileRefs` **禁止** `bizType=excel-import`；仅 Facade 的 `uploadExcelImportStaging` 可绑定。
- **孤儿 staging**：对账任务额外释放「无匹配异步任务」且超过 `excelImportOrphanStagingHours`（默认 6h）的 `excel-import` 文件。
- 任务行与文件引用统一以 **`taskId` 为业务锚**；`fileId` 仅在 `request_param`；Handler 要求 file 行锚定 + `refField=import`。
- **FE 降级**：`importFileAsync` 遇 `error.async.disabled` 时回落 `importFile` / `POST /import`。
- **Gen**：勾选导入时生成 Handler（继承 `AbstractExcelImportAsyncHandler`）、`/import-async`、前端 `importFileAsync`；`importRows` 含 Bean Validation。同步 `POST /import` **保留**供脚本/联调，Gen 页面默认只接 async。
- 前端：`GenCrudApi.importFileAsync` + `pollAsyncTask`（`/mine/{id}`）；工具栏 builtin 仅 `export`|`import`（`importTemplate` 已废弃，模板在对话框内）。
- **重试**：`AsyncTaskRetrySupport` — Local 与 Redis Stream 共用 FAILED→PENDING 决策（`AsyncTaskRetryPolicy`）。
- 运行时通道见 [34-platform-job.md](34-platform-job.md)；Worker 经 `AsyncTaskPrincipalResolver` 重建提交者 RBAC。
- **失败文案**：DB `error_message` 存 i18n key（可带 `\u001f` 分隔的 args）；Dialog/`resolveAsyncTaskErrorMessage` 按当前 locale 翻译。
- **排队空转**：PENDING 约 90s 后 toast 并停转圈；任务仍在 DB。
- **管理端**：只读+终态删除；删除 `excel-import` 任务时 `releaseBizFiles`。
- **暂存文件**：提交时引用绑定到 `taskId`；**任务存在期间保留**；用户**删除终态任务**时释放；另对账任务对终态超过 `excelImportStagingRetentionDays`（默认 7 天）的任务自动 `releaseBizFiles`（任务行仍保留审计）。
- 行错误 `ExcelImportResult.errors[].message` 存 **i18n key（可带 args）**，前端按当前语言翻译；勿在 Worker 侧 bake 文案。
- 行数/体积上限不变（5 万 / 10MB）。
- **导入提示文案**：壳层 `common.importStep*Hint` 兜底；业务可用 `page.importStepTemplateHint` / `importStepUploadHint` 覆盖。
- 异步任务名：`page.importAsyncTaskName`（经 `ExcelValueFormatters.pageText`）。

导入列对操作者用 **标签/名称**（模板下拉）；落库前解析为 **码值/ID**。字典：`@ExcelImportDict`；部门：`@ExcelImportRef(DEPT_NAME)`。密码不导入，走系统初始密码。

## 导入模板规范（字典 / 部门 / 岗位 / 角色）

1. **人对标签，系统对码**：模板下拉展示字典 label、部门/岗位/角色名称；导入接受 label 或 code / 名称或在范围内的 id。
2. **绑定仍靠 index**：禁止用本地化表头或下拉文案做 EasyExcel 字段映射。
3. **服务端必校验**：字典未命中、引用不存在/重名/越权 → `ExcelImportRowError`。
4. **Gen**：`dictType` → `@ExcelImportDict`；`dept*` / `post*` / `role*` → 对应 `@ExcelImportRef` + 模板下拉。

## MasterDetail 明细 Excel

主表可声明 `export`/`import`。明细工具栏**运行时仅渲染 `export`**（导出 query 自动带主表外键）；明细导入需 Excel 自带外键列，当前 Gen 壳不注入，故不启用。需先选中主表才显示明细 Excel 工具栏。

## 开发者用法（组件化）

页面侧只接线，不复制导入导出流程：

```
GenCrudPage / GenTreeCrudPage / GenMasterDetailPage
  └─ GenExcelToolbarActions（builtin export|import + 权限/只读）
       ├─ useGenExcelToolbar（可见性 / perm / 导出 / 打开对话框）
       └─ GenExcelImportDialog（模板 → 上传 → async 轮询 → 行错误表）
```

1. 字段勾选 **列表 + 导出**；需要导入再勾 **导入**（与导出解耦；默认不导入）。
2. page-spec：`isExport` / `isImport`；`extensions.toolbarActions: ["export"]` 或 `["import"]`（模板下载在导入对话框内）。
3. `GenCrudApi` 提供 `exportFileAsync`（首选）/ 可选 `exportFile`；`importFileAsync`（首选）/ 可选 `importFile`；`GenCrudPerm.export|import` 缺失则**不渲染**按钮。view.json 未声明 toolbar 时，布局会经 `useExcelImportExport` 按 API/权限自动发现。
4. 导入落库：生成代码默认调用 `create()`；领域预校验扩展 `validateImportDomain`（对照 User 金样例），**不要**绕过 `create` 直接 mapper insert。
5. 自定义业务页复用：挂载 `GenExcelToolbarActions`，或用 `useExcelImportExport` 生成 `actions`，传入 `api` / `perm` / `labelResolver=gp` / `resolveFieldValidation=gv` 即可。

## 明确禁止

- 请求参数传入「导出列清单」决定列集合
- 解析 classpath `view.json` 作为导出 SoT
- 用本地化表头字符串做 EasyExcel 字段绑定
- 超限静默截断导出/导入结果
- 分批落库后半失败导致半提交（默认路径必须全有或全无）
- 将雪花 `deptId` 作为唯一人机导入接口（应下拉部门名称）
- 将 OA `field_def` / `promote_slot` 与 Gen 导出混为一谈
- 无 `ExcelImportLookup` 实现时透传任意 dict/dept/post/role 文本

## 异步导出

- **同步** `GET /export` 仍保留（脚本/联调）；**异步** `POST /export-async` → `ExcelExportAsyncSubmitFacade`（预分配 `taskId` → `submitWithId`，`request_param`=query JSON，任务行记 `locale`）→ Worker `AbstractExcelExportAsyncHandler`：按提交者 locale 解析表头 → `writeLocalizedPagedTo` → `storeExcelExportStaging`（`bizType=excel-export`，`bizId=taskId`）→ `result_data={fileId,fileName}`。
- FE：点击「导出」→ 弹框选择同步/异步（默认异步）→ `exportFileAsync` + `pollAsyncTask` 或同步下载；用户选异步且 `error.async.disabled` 时明确提示。
- **下载鉴权**：`excel-import` / `excel-export` / `oa_instance` 为代码**钉死** CUSTOM（表行不可削弱）；Excel 仅提交者；OA 为发起人/流程参与者/DataScope 可读。
- **文件管理对称**：列表 SQL 对 excel staging 按当前用户过滤；VO `downloadable` 由 `FileAccessGuard` 计算；前端仅在 `downloadable !== false` 时显示下载按钮。
- **策略配置**：管理端拒绝改删钉死 pattern；「当前生效」与列表 `pinned` 标记对齐。
- **保留字**：客户端 `upload`/`addRef`/`syncBizFileRefs` **禁止** `bizType=excel-export`；仅 `storeExcelExportStaging` 可绑定。
- **孤儿 / 保留**：与导入共用对账与 `excelImportOrphanStagingHours` / `excelImportStagingRetentionDays`（覆盖 `excel-import` 与 `excel-export`）。
- 异步任务名：`page.exportAsyncTaskName`（经 `ExcelValueFormatters.pageText`）。
- 行数上限与同步一致（5 万）；超限在 Worker 写文件前失败，任务 FAILED。

## 已知限制（后续）

- 同步导出已改为「先写完临时文件再下发响应头」，避免中途失败留下残缺 HTTP body；超大导出仍优先 `exportFileAsync`
- `@ExcelImportRef` 可继续扩展业务自定义 Kind（客户等）
- 同步/异步导入/导出行数与体积上限仍为 5 万 / 10MB
- worker 侧经 `SystemAsyncTaskPrincipalResolver` 重建提交者权限；无解析器时回退最小 LoginUser（仅单测）
- MasterDetail **明细导入**未启用（无法向 Excel 行注入主表外键）；明细导出文件名回退 `detail.businessName`（勿用主表 `exportFileName`）；明细可 API 自动发现 export
- Gen 文件内 + 库内唯一键覆盖 required String 的 username/code/account/*Code/*No/*Key；DataScope/可分配引用等仍对照 User 金样例扩展 `validateImportDomain`
