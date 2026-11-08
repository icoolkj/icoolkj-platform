# i18n 标准（icoolkj-platform）

> 速查：[AI-CONTEXT.md](AI-CONTEXT.md) · 数据库 Flyway **V1–V4**

## 原则

- **平台固定文案**：Git **前后端双份** `i18n/platform/{zh-CN,en-US}.json`（`error.*` / `entity.name.*` / 平台级 `validation.*` / `notify.*` / `oa.instance.*`）。运行时 **DB 可覆盖**（文案管理插入同键优先）；**无 Flyway seed**。
- **前端壳层公用**：Git **前后端双份** `i18n/shell/{common,layout,login}/`；启动 merge 进 `vue-i18n`；**不进** `sys_i18n_message`，**不进** `i18n/gen/`。
- **壳层构建常量**：`shell.config.json`（FE 根目录 + BE `resources/shell/` 镜像）；含 defaultTitle、analytics、DevTools、storageKeys、copyright 值、boot 门文案；**TS 只做加载**。
- **业务页文案**：Git **前后端双份** `i18n/gen/{businessName}/`；前端 `useGenI18n` / `gp()`。
- **菜单名称**：`sys_menu.name_i18n` JSON（`{"zh-CN":"...","en-US":"..."}`）+ `menu_name` 主名称；**无** `sys_menu_locale`。
- **按语言切换**：请求头 `Accept-Language` + 用户偏好 `ui.locale`；后端 `LocaleContextHolder`，前端 `vue-i18n`。
- **键名规范**：`模块.语义`，如 `error.auth.invalid_credentials`、`common.search`、`layout.pageTitle`。

---

## 分层

| 层 | 存储 | 加载方式 |
|----|------|----------|
| 前端壳层 | 双端 `i18n/shell/{common,layout,login}/` | 前端 `loadRemoteI18n()` → `t()`；后端 classpath 镜像 |
| 平台固定文案 | 双端 `i18n/platform/` | 后端：`DB` → `PlatformI18nExtendedMessageResolver`；前端：注入 `vue-i18n` |
| 前端业务页 | 双端 `i18n/gen/{page}/` | `import` + `useGenI18n` / `gp()` |
| DB 覆盖 | `sys_i18n_message` scope=`backend` | 同键优先于 classpath 基线（默认无 seed） |
| 菜单名称 | `sys_menu.name_i18n` | `/system/menu/routers` 按 `Accept-Language` 解析 |
| Gen 校验文案 | 后端 classpath `i18n/gen/` | `GenI18nExtendedMessageResolver`（`{gen.*}`） |
| 语言目录 | `sys_i18n_locale` | `GET /system/i18n/bootstrap` |
| 首屏 HTML / 构建开关 | `frontend/shell.config.json`（镜像 `backend/.../resources/shell/shell.config.json`） | Vite 注入 `index.html`；TS 薄加载器 `shell.config.ts` |
| Element Plus | 框架 locale 包 | `stores/locale.ts` |

**平台固定键解析顺序：** `sys_i18n_message`（覆盖）→ classpath `i18n/platform` → 原 key

**例外 / 禁止：**

- **禁止**再建 `sys_menu_locale`
- **禁止**把壳层 `common`/`layout`/`login` 放进 `i18n/gen/`
- **禁止**自建 `frontend/src/i18n/local/` 或页面内硬编码多语言 map（含 `if (locale === 'zh')` 拼 ZH/EN）
- **禁止**组件 `withDefaults` 写中文/英文文案默认值；缺省用 `t()` / `gp()` 回退
- 生成器（`GenI18nBuilder`）写死基线文案可以；运行时只能 key
- **代码生成 i18n 基线**：文案模板在双端 `i18n/gen/_codegen/{zh-CN,en-US}.json`（占位 `{functionName}` / `{label}` / `{maxLength}` …）；`GenI18nBuilder` 只做填充，禁止再在 Java 里写中英文案
- shell / platform / gen 文案改动须 **前后端 JSON 同步**（可用 `scripts/sync-gen-i18n-fe-to-be.cjs`）
- **壳层构建配置** `shell.config.json` 改动须同步 `backend/icoolkj-admin-system/src/main/resources/shell/shell.config.json`（OEM 单一事实源）
- `generate-i18n-seed.mjs`：把 `messages_*.properties` **增量补进** `i18n/platform`（不覆盖已有键）

---

## 后端约定

### 抛异常

```java
throw new BusinessException("error.entity.not_found", "entity.name.user");
throw new UnauthorizedException("error.auth.invalid_credentials");
@NotBlank(message = "{validation.tenant_code.required}")
@NotBlank(message = "{gen.tenantPolicy.configValue.required}")
```

### 新增文案 / 菜单

| 类型 | 做法 |
|------|------|
| 壳层 / platform / gen | 改前后端对应 JSON |
| 菜单名 | 写 `menu_name` + `name_i18n`（管理端中英文栏） |
| 热修平台文案 | 文案管理插入同键 |

---

## 前端约定

```vue
{{ t('common.save') }}
{{ t('error.auth.invalid_credentials') }}
const { gp } = useGenI18n('menu', 'system', GEN_I18N_FILES)
```

- 启动：`loadRemoteI18n()` 注入 shell + platform
- 菜单标题来自 routers API（已按语言解析），勿在前端再 t() 菜单名

---

## 键名前缀参考

| 前缀 | 存储 |
|------|------|
| `error.*` / `entity.name.*` / `validation.*` / `notify.*` / `oa.instance.*` | 双端 `i18n/platform/` |
| `common.*` / `layout.*` / `login.*` | 双端 `i18n/shell/` |
| 业务页 | 双端 `i18n/gen/` |
| 菜单显示名 | `sys_menu.name_i18n` |

---

## 自检命令

```bash
rg 'BusinessException\("[\u4e00-\u9fff]' backend/
test -f frontend/src/i18n/platform/zh-CN.json
test -f backend/icoolkj-admin-system/src/main/resources/i18n/platform/zh-CN.json
# 基线为 V1–V4；勿新增 V5+；sys_menu_locale 不应存在
ls backend/database/migration/V*.sql
rg 'sys_menu_locale' backend/database/migration/ || true
# sys_i18n_locale（语言目录）仍合法
```
