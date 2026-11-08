# 命名与元数据规范

> 版本：V2.2 | 模块：naming-conventions | 上游：[docs/README.md](../README.md)

## 概述

本篇定义 Java 包命名、代码头注释、Maven 坐标、OpenAPI 信息及前端品牌元数据，确保项目标识一致。

---

## 包命名规范

统一包名根：`com.icoolkj.platform`

推荐结构：

```text
com.icoolkj.platform.api
com.icoolkj.platform.common
com.icoolkj.platform.framework
com.icoolkj.platform.system
com.icoolkj.platform.infra
com.icoolkj.platform.biz
```

---

## 代码头注释

新增 Java 源文件建议使用 SPDX 标记（见下方「许可与版权文件」）：

```java
// SPDX-License-Identifier: LicenseRef-iCoolKJ-NonCommercial-1.0
// Copyright (c) icoolkj - https://icoolkj.com
```

---

## Maven 坐标

| 属性 | 值 |
|------|-----|
| groupId | `com.icoolkj` |
| artifactId | `icoolkj-platform`（父 POM） |
| 子模块 artifactId | `icoolkj-admin-api`、`icoolkj-admin-system` 等 |

---

## OpenAPI 信息

| 属性 | 值 |
|------|-----|
| Title | iCoolKJ Platform API |
| Contact | icoolkj |
| Email | icoolkj@163.com |

---

## 前端品牌信息

| 属性 | 值 |
|------|-----|
| 品牌名称 | iCoolKJ |
| 域名 | icoolkj.com |

---

## 许可与版权文件

仓库应包含：

- `LICENSE.txt` — iCoolKJ 非商业学习研究许可证 v1.0 全文（中英双语）
- `NOTICE.txt` — 版权、署名与许可性质说明
- `THIRD-PARTY-NOTICES.md` — 第三方依赖许可证清单
- `CONTRIBUTING.md` / `SECURITY.md` — 贡献与安全政策

本项目是**源码公开 / Source Available**，**不是 OSI 开源软件**：允许非商业学习研究，禁止任何商业使用，商业用途须另行取得书面授权。请勿在源码中加入 MIT / Apache / GPL 等 OSI 许可证头，也不要把项目描述为「开源」。

---

## 相关文档

- 上一篇：[04 目录结构](04-directory-structure.md)
- 下一篇：[06 认证与权限](06-auth-permission.md)
- 根目录：[LICENSE.txt](../../LICENSE.txt)、[NOTICE.txt](../../NOTICE.txt)

---

## 落地检查清单（Checklist）

- [ ] 所有 Java 文件包名以 `com.icoolkj.platform` 开头
- [ ] 新增 Java 源文件包含 SPDX 标记
- [ ] Maven 父 POM `groupId` 为 `com.icoolkj`
- [ ] OpenAPI 配置 Title 为 `iCoolKJ Platform API`
- [ ] 前端登录页、页脚显示 `iCoolKJ` 品牌
- [ ] 仓库根目录存在 `LICENSE.txt`、`NOTICE.txt`、`THIRD-PARTY-NOTICES.md`
