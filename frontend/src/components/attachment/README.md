# 附件组件选型

业务页**优先**用本目录组件 + `@/constants/file` 常量。  
配置目录上传必须用 **`view.json` → `fileCatalogs`** 驱动目录编码。

> 完整约定见 [docs/architecture/33-file-attachment.md](../../../../docs/architecture/33-file-attachment.md)。

## 分层提醒

- **公共组件**不内置 `notice_cover` / `oa_instance` 等业务码。
- **GenFormField** 只透传 `view.json` 的 `bizType` / `downloadScope`；缺 `bizType` 抛错。
- **业务**在 view / 页面 / OA mapper 中声明。

## 规范闭环样例（必看）

菜单：**基础设施 → 文件配置中心 → 附件测试业务**

| 层 | 路径 |
|----|------|
| 表 | `infra_attachment_demo`（Flyway `V3__attachment_demo.sql`） |
| 目录 | `attachment_demo`（封面/合同/材料包槽位） |
| 后端 | `AttachmentDemo*` + `BizCatalogAssetsSupport` / `CatalogRequiredAssetsValidator` + `syncBizFileRefs` |
| 前端 | `views/infra/attachmentDemo` + `i18n/gen/attachmentDemo/{view,zh-CN,en-US}.json` |

对照步骤：`view.json` 声明 `fileCatalogs` → 表单 `CatalogAttachmentManager` → 保存 `assets` → 后端写 `assets_json` 并 `syncBizFileRefs` → 删除 `releaseBizFiles`。

组件级 Tab 试用（非业务表）：`/dev/attachment-showcase`

| 场景 | 组件 |
|------|------|
| 单图封面 | `AttachmentManager` single + image |
| 多文件列表 | `AttachmentManager` multiple + list |
| 配置目录多槽位表 | `CatalogAttachmentManager` |
| 头像 / 富文本 / 管理端上传 | 特殊，勿替换 |

管理端「文件管理 → 文件上传」：`FileUploadDialog` 必须选择上传配置，`FormData.bizType` = `profileCode`；列表类型图标用 `FileThumb`（非真实缩略图）。

废弃壳：`CoverImagePicker`、`OaFileUpload` — 新代码请直接用 `AttachmentManager`。
