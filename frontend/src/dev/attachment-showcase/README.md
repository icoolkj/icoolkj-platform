# 附件组件场景测试页

规范与业务页一致：**`view.json` 驱动结构，`zh-CN.json` / `en-US.json` 驱动文案**。

| 文件 | 职责 |
|------|------|
| `src/i18n/gen/attachmentShowcase/view.json` | scenarios / fileCatalogs / pickGuide / specialCases |
| `src/i18n/gen/attachmentShowcase/zh-CN.json` | 中文 page.* / fields.* |
| `src/i18n/gen/attachmentShowcase/en-US.json` | 英文 page.* / fields.* |

打开：登录后 `/dev/attachment-showcase`

- 目录槽位 Tab 的 `catalogCode` **只来自** `view.json` → `fileCatalogs`（按 `catalogBindTab` 过滤）
- 场景 Tab / 选型指引 / 代码片段 / 特殊说明全部 `gp(key)`，禁止页面内写死中英双语
- 公告封面示例：`bizType=notice_cover` + `downloadScope=notice`；轮播示例只换 `FILE_BIZ_BANNER_IMAGE`
- 不再演示 `CoverImagePicker` / `OaFileUpload`（兼容壳，新代码勿用）

## 移除

删除本目录，并去掉 `router/index.ts` 中的 register；同时可删 `src/i18n/gen/attachmentShowcase/`。
