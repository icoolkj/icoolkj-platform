# iCoolKJ 速查与 AI 上下文

> **单页真值摘要**（2026-07-18）。AI 助手与开发者应优先读本页；细节见链接分册，勿沿用已废弃的「主管部门/内设机构」「Flyway V5/V6」等描述。

| 口径 | 值 |
|------|-----|
| 架构规范 | V2.2 → `docs/architecture/` |
| 数据库 | **Flyway V1–V4** → `backend/database/migration/`（V1 schema含 `sys_ai_skill`/OA layout；V2 平台种子含表单设计器/办公助手/AI Skill；V3 附件样例；V4 **通用办事平台基础模板** + icoolkj 完整工作流；改后删库重建） |
| 租户/区划模型 | **V2.0 定稿** → [23-region-org.md](architecture/23-region-org.md) |
| 前台 Web | `frontend/` Admin；`site/` 平台官网；`portal/` 租户站 → [32-site-portal.md](architecture/32-site-portal.md) |

---

## 核心不变量

### 租户 `sys_tenant`

| 类型 | 代码 | 规则 |
|------|------|------|
| 企业租户 | `ENTERPRISE` | `parent_tenant_id` **必须为空**；`root_region_id` 仅可选省/市/区县（level 1–3） |
| 单位租户 | `ORGANIZATION` | 可选 `parent_tenant_id`（父须为 ORGANIZATION）；`root_region_id` 可选国家/省/市/区县（level 0–3） |

- 一账号一租户；`tenant_id='0'` = 平台级语义（非业务租户行）。
- 校验实现：`TenantServiceImpl`、`TenantBootstrapService`（开户）、`RegionServiceImpl.assertRegionSelectableForTenantRoot`。
- 开户模型 A：平台创建租户 → 自动根部门 + 管理员 → 弹窗交接登录信息 → 租户自管组织（不做平台代管用户/部门/岗位）。
- 开户可指定：管理员账号 / 根部门编码 / 根部门名称（可空走默认）；平台菜单「租户管理员」支持重置密码与解锁（预留进入租户）。

### 区划 `sys_region`

- 平台国标库，`tenant_id='0'`，level 0（国家）~ 5（街道/社区）。
- **平台管理员**：区划 CRUD（`system:region:add/update/delete`）。
- **租户**：只读 `GET /system/region/tenant-tree`（裁剪至 `root_region_id` 子树），无区划管理菜单。

### 部门 `sys_dept`

- **纯租户内组织树**（`parent_id` / `ancestors`）。
- **业务不依赖** `region_id`、`dept_type`、`competent_id`（若 DDL 仍留列，视为遗留，勿写业务逻辑）。

### 平台 vs 租户菜单

- `assign_scope=PLATFORM`：仅平台管理员（如区划管理、租户管理）。
- 租户管理员（`tenant_admin` / `org_admin`）不含 PLATFORM 菜单。

### 产品宇宙（Console Shell）

- 菜单归属 `sys_menu.product_code`；产品目录 `sys_product`（`sort_no`：业务 1–49，oa/files/ai 50–89，platform 90+）。
- 顶栏切换产品 → 侧栏 `routers?productCode=`；动态路由仍注册全量。授权 v1 = 已授菜单上的 distinct 产品码。
- 详见 [38-product-universe-console-shell.md](architecture/38-product-universe-console-shell.md)。

### 工作流跨租户

三层租户：**操作者**（登录 `tenant_id`）/ **业务**（业务表与 `wf_biz_process`）/ **Flowable**（`wf_model.tenant_id`）。`platform_mandatory` 须用 `WorkflowCrossTenantSupport` + `AbstractWorkflowBizHandler`。详见 [24-workflow-cross-tenant.md](architecture/24-workflow-cross-tenant.md)。

工作流地基：设计器 Design SoT；`notice_publish` 平台强制（V2）；**通用办事 10 类**平台侧仅为基础模板（无 design/审批规则），租户自行配置工作流；**icoolkj** 在 V4 已内置完整 10 套样例；其它租户 `enable` 拉表单后自行设计/部署（`syncFromPlatform` 仅得壳+stub BPMN）。

菜单：租户侧无「工作流」目录（PLATFORM）；可见入口在「在线办公 / OA配置」；`workflow:*` 等 F 挂在可见页下；仅审批跳转页为 hidden C。见 [31-workflow-biz-menu.md](architecture/31-workflow-biz-menu.md)。  
配置驱动（强制）：产品行为 JSON / `sys_config`；无 Service 硬编码话术与散落前缀，见 [03 架构原则](architecture/03-principles.md)。
审批人策略：`ApproverStrategyHandler` 注册表；OA 字段文案：`category_code`→`i18n/gen/{camel}`；配置键：`ConfigKeyRegistry`。  
品牌：`sys_brand_pack`→bootstrap→`useBrandStore`；通知模板：`workflow.notify.templates`（空则 `notify.workflow.*` i18n）；`sys_config.config_value`=mediumtext。  
附件：统一 `AttachmentManager` / `CatalogAttachmentManager`；业务声明 `bizType`（勿写入 GenFormField）；目录资产用 `BizCatalogAssetsSupport` + `CatalogRequiredAssetsValidator`；闭环样例见附件测试业务与 [33-file-attachment.md](architecture/33-file-attachment.md)。  
数据权限：`DataScopePredicateHandler` 注册表；代码生成模块：`generator/modules.json`；工作流 classpath BPMN 默认关闭（`classpath-deploy-enabled`），主路径 DB 种子 + `model-sync`。  
菜单多语言：`sys_menu.name_i18n` JSON（`zh-CN`/`en-US`）；表单中英文写入该字段；侧栏按 `Accept-Language` 解析；**无** `sys_menu_locale`。

Entity Lombok：继承 `BaseEntity`/`TenantBaseEntity` 用 `@Getter/@Setter` + `callSuper`；禁止 `@Data`、禁止重复声明 `id`（见 codegen.mdc）。

### OA 表单配置（L0）

- **定稿**：表格「配置字段」= 字段元数据编辑器（非可视化表单设计器）；运行时 `OaDynamicForm` 纵向渲染。
- 通用发起/审批：`/oa/instance` + `oa:instance:*`；`form_route` → `/office/approve`（勿带 `/index`，与菜单 normalize 一致）。
- L1（排序拖拽/分组双列）及画布设计器 **暂缓**；勿用可视化设计器替换表格主入口。
- 详见 [25-oa-form-field-editor.md](architecture/25-oa-form-field-editor.md)。

### i18n

| 键前缀 | 存储 | 说明 |
|--------|------|------|
| `common.*` / `layout.*` / `login.*` | 双端 `i18n/shell/`（FE `src/` + BE `icoolkj-admin-system/.../resources/`） | 壳层公用；启动注入 `t()`；**不进** DB / `gen` |
| `error.*` / `entity.name.*` / `validation.*` / `notify.*` / `oa.instance.*` | 双端 `i18n/platform/`；**DB 可覆盖** | Git 基线；`PlatformI18nExtendedMessageResolver`；文案管理同键优先；无 Flyway seed |
| `gen.{module}.*` | 双端 `i18n/gen/`（FE + 对应模块 `resources/`） | 业务页 `gp()`；禁止写入 `sys_i18n_message` |
| 菜单名 | `sys_menu.name_i18n` | 运行时 `/system/menu/routers` 按语言解析 |
| 布局偏好 | `sys_user_preference.ui.layout`（JSON） | 侧栏风格、标签页、水印、表单模式等 |

详见 [i18n-standard.md](i18n-standard.md)。

---

## 空库初始化

```bash
mysql -u root -p < backend/database/init/00_reset_database.sql
cd backend && mvn -pl icoolkj-admin-api -am clean compile -DskipTests
# 启动 icoolkj-admin-api
python scripts/verify_flyway_sql_source.py
```

| 版本 | 文件 | 职责 |
|------|------|------|
| V1 | `V1__schema.sql` | 全量 DDL（含 `sys_menu.name_i18n`；无 `sys_menu_locale`） |
| V2 | `V2__platform_seed.sql` | 演示租户、区划、用户/角色/菜单（含 name_i18n 回填）/字典/参数；含 `notice_publish` |
| V3 | `V3__attachment_demo.sql` | 附件样例 |
| V4 | `V4__general_office.sql` | **通用办事 10 类**平台基础模板 + icoolkj 组织与完整工作流 |

**约定**：schema/平台种子改 **V1–V2**；附件样例改 **V3**；办事/工作流模板改 **V4**。改 migration 后 **删库重建**，勿新增 **V5+**。平台固定文案在 Git `i18n/platform/`。

安全（租户策略）：租户自助覆盖 `ConfigKeyRegistry` 中 `tenantOverridable` 且非敏感键；API `/system/tenant-policy/*`（`system:tenantPolicy:*`，菜单在 V2，`assign_scope=TENANT`）；页面文案 Git **双端** `i18n/gen/tenantPolicy/`（FE `frontend/src/` + BE `icoolkj-admin-system/.../resources/`，不进 `sys_i18n_message`）；仅清理本租户 Redis 配置缓存。平台「参数管理」仍为 PLATFORM。

---

## 默认账号（V2 seed）

| 账号 | tenantCode | 角色 | 密码 |
|------|------------|------|------|
| `admin` | `platform` | 平台超管 | `StudyOnly@2026` |
| `tenant_admin` | `default` | 企业租户管理员 | `StudyOnly@2026` |
| `org_admin` | `demo_org` | 单位租户管理员 | `StudyOnly@2026` |

首次登录 `must_change_password=1`。

安全（P0）：密码复杂度按租户 `security.password.*`（`PasswordPolicyService`）在创建用户/改密时校验；改密、赋角色、角色赋菜单后 `removeAllUserSessions` 吊销 Redis 会话；空客户端 IP 在 `security.ip.default-policy=deny` 时拒绝登录。新错误键 `error.user.password_require_*` 在 `i18n/platform/`（可由 `messages_*.properties` 经 `generate-i18n-seed.mjs` 补缺）。存量库若仍有旧 error 基线行，执行 `scripts/clear-platform-i18n-db-baseline.sql` 后刷新缓存。

安全（P1）：登录按 IP 限流（`security.login.max-fail-count-per-ip` 默认 20、`ip-lock-minutes` 默认 15，无库种子时用代码默认）；登出将 access JWT 写入 Redis 黑名单；`IpAccessFilter` 对已认证请求按 ADMIN/ALL 作用域校验 IP。

安全（验证码）：`GET /auth/captcha?tenantCode=` 按租户配置返回是否需要验证码及图片；登录校验 Redis 一次性码。V2 种子与 prod 默认 `security.login.require-captcha=true`（本地 YAML fallback 可为 false）。验证码 UI 文案在壳层 `frontend/src/i18n/shell/login/`（`t('login.captcha*')`）。

安全（等保三级 · 2026-07-18 重对齐）：应用面约 **76%**（部分计半）。多租户 **平台定基线、租户策略可覆盖**；**Opaque Refresh**、**sys_config enc:v1 AES-GCM**、**三类审计哈希链+库内归档**（归档后 live 链 `reanchor`；登录/操作/接口均有 `integrity/verify`）、**CORS 白名单**、**用户解锁清 Redis**、MFA、三权；prod **Gen 禁写盘**。审计留存由 **worker** RetentionJob 执行。完整对照 **[security-mlps-l3-alignment.md](security-mlps-l3-alignment.md)** §2.1。

交付刚需（2026-07-18）：**Excel 导出/导入**为 Gen 标准积木（契约见 [35-excel-export-import.md](architecture/35-excel-export-import.md)：`ExportRow`/`ImportRow` 白名单 + `isExport`/`isImport`；`view.extensions.toolbarActions` `builtin: export|import`；表头/文件名走 gen i18n；导入全有或全无 + `ExcelImportResult`）；**定时任务**（见 [34-platform-job.md](architecture/34-platform-job.md)：`sys_job*` + `JobHandler`/`JobRunner`，与用户异步 `infra_async_task*` 分离）；认证相关单测基线；**OA 表单 L1**（拖拽排序 + `field_group`/`col_span`）。

---

## 关键 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/system/region/tree` | 平台全量区划树 |
| POST/PUT/DELETE | `/system/region` | 平台区划 CRUD |
| GET | `/system/region/selectable-roots?tenantType=` | 按租户类型可选 root |
| GET | `/system/region/tenant-tree` | 当前租户可见子树 |
| CRUD | `/tenant/tenant/*` | `tenantType`、`parentTenantId`、`rootRegionId` |

---

## 关键代码路径

| 领域 | 路径 |
|------|------|
| 租户校验 | `backend/.../tenant/service/impl/TenantServiceImpl.java` |
| 租户开户 | `backend/.../tenant/service/impl/TenantBootstrapServiceImpl.java`（根部门/岗位/超级角色/首个管理员；返回 `TenantProvisionVO`） |
| 区划 | `backend/.../region/service/impl/RegionServiceImpl.java`, `RegionController.java` |
| Gen CRUD / 列表布局 | `GenCrudPage.vue`, `GenTreeCrudPage.vue`, `GenListLayout.vue`（封闭布局 slot） |
| 区划/租户 Gen | `frontend/src/i18n/gen/region/`, `frontend/src/i18n/gen/tenant/` |
| 工作流跨租户 | `backend/.../workflow/support/WorkflowCrossTenantSupport.java`, [24-workflow-cross-tenant.md](architecture/24-workflow-cross-tenant.md) |
| Flyway 源 | `backend/database/migration/V1–V4` |

---

## 禁止事项（常见 AI 幻觉）

1. 勿写 Flyway **V5+** 或 `V1__init_icoolkj_platform.sql`（当前基线为 **V1–V4**；改后删库重建）。
2. 勿把部门当「主管部门/内设机构」或绑区划业务。
3. 勿再建 `sys_menu_locale`；菜单多语言只用 `sys_menu.name_i18n`。勿把 `gen.*` / 平台固定前缀 seed 进 DB（基线在 Git `i18n/platform/` / `i18n/gen/`）。
4. 勿假设企业租户可有 `parent_tenant_id` 或单位可选街道/社区作 root。
5. `schema/` 为 DDL 参考，**冲突时以 `migration/` 为准**。
6. 存量库平台文案覆盖清理用 `scripts/clear-platform-i18n-db-baseline.sql`（与菜单无关）。

---

## 文档阅读顺序

1. 本页 → [23](architecture/23-region-org.md) → [13](architecture/13-multi-tenant.md)
2. [11 初始化](architecture/11-initialization.md) + [database/README](../backend/database/README.md)
3. [i18n-standard.md](i18n-standard.md)
4. 其余见 [docs/README.md](README.md)（已下线过程/审计分册勿再引用）
