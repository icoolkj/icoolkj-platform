# 33 · 文件附件约定

业务附件统一走 **上传配置（profile）+ 业务引用（file_ref）**；前端优先复用附件组件，后端复用 assets / 必填校验支持类。

## 分层（禁止把业务塞进公共层）

| 层 | 职责 | 不要做 |
|----|------|--------|
| `AttachmentManager` | 通用单/多附件；**必填** `bizType`；`downloadScope` 默认 `infra`（技术默认，非业务绑定） | 不内置 `notice_cover` / `oa_instance` 等业务码 |
| `CatalogAttachmentManager` | 按目录编码拉槽位；`validate()` 前端必填校验 | 不写品牌/公告等业务文案逻辑 |
| `GenFormField` | **只透传** `view.json` 的 `bizType` / `downloadScope`；缺 `bizType` 直接抛错 | 不写 notice/OA 默认值 |
| 业务页 / `view.json` / OA mapper | 声明业务 `bizType`、scope、目录编码 | 不手写槽位循环（目录场景用 CAM） |
| 后端 `BizCatalogAssetsSupport` | `assets_json` ↔ Map / `InfraFileRefBinding` | — |
| 后端 `CatalogRequiredAssetsValidator` | 按目录槽位必传/最小数量 | 业务 Service 内复制校验循环 |

## 双 biz_type

| 用途 | 含义 | 示例 |
|------|------|------|
| 上传 `biz_type` | **profileCode**（走上传配置中心规则） | `notice_cover`、目录槽位码 |
| 引用 `biz_type` | **业务常量**（`infra_file_ref`） | `infra_attachment_demo`、公告/OA 常量 |

## 标准闭环（对照样例）

菜单：**基础设施 → 文件配置中心 → 附件测试业务**

1. `view.json` 声明 `fileCatalogs: [{ code: "attachment_demo", ... }]`
2. 表单 `CatalogAttachmentManager` + 保存前 `validate()`
3. 后端写 `assets_json`，`syncBizFileRefs`；删除 `releaseBizFiles`
4. Flyway：`V3__attachment_demo.sql`（表 + 目录槽位种子）

组件级试用（非业务表）：`/dev/attachment-showcase`

## 前端选型

| 场景 | 组件 |
|------|------|
| 单图 / 多文件列表 | `AttachmentManager` |
| 配置目录多槽位 | `CatalogAttachmentManager` |
| 头像裁剪 / 富文本内嵌 / 管理端存储对话框 / OA 附件面板 | 特殊场景，勿强行替换 |

**管理端文件上传**（`FileUploadDialog`）：仍用存储引擎 Tab，**不**换成 `AttachmentManager`；但必须传 **`bizType` = 已启用上传配置的 `profileCode`**（与业务附件同一套校验）。列表类型列用统一 `FileThumb`（按扩展名/类型图标，不做真实缩略图）。

废弃壳（仅兼容）：`CoverImagePicker`、`OaFileUpload` — **新代码直接用 `AttachmentManager`**。

## Gen 字段约定

```json
{
  "field": "coverFileId",
  "component": "image-upload",
  "bizType": "notice_cover",
  "downloadScope": "notice"
}
```

- `bizType`：**必填**（业务声明）
- `downloadScope`：可选；未声明时组件用技术默认 `infra`
- 上传按钮文案键：`upload`（回落 `common.upload`）；勿再依赖 `coverUpload`

## 后端复用

```java
// 保存前
requiredAssetsValidator.validateRequired(assets, catalogCode);
// 持久化
entity.setAssetsJson(assetsSupport.writeAssets(assets));
// 引用同步
infraFileService.syncBizFileRefs(tenantId, BIZ_TYPE, bizId, assetsSupport.toBindings(assets));
```

错误码：`error.file.attachment_required`（platform 双端 i18n）。

## 相关路径

- FE：`frontend/src/components/attachment/`
- 常量：`frontend/src/constants/file.ts`
- BE：`infra/support/BizCatalogAssetsSupport`、`CatalogRequiredAssetsValidator`
- 样例：`infra/attachmentDemo`、`system/brandPack`（CAM + `fileCatalogs`）
