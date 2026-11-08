# 贡献指南

感谢你对 iCoolKJ Platform 的关注。请先读完本文件再提交贡献。

## 0. 先确认你的使用目的

本仓库是**源码公开 / Source Available**，采用 [iCoolKJ 非商业学习研究许可证 v1.0](LICENSE.txt)，**不是 OSI 开源软件**，**禁止任何商业使用**。

- 你可以在个人学习、研究、教学、非营利评估的范围内阅读、修改、实验并提交贡献。
- 如果你的目的是为企业构建内部系统、交付客户项目、提供付费服务或任何商业用途，请先按 [LICENSE.txt](LICENSE.txt) 第 8 条联系权利人取得书面商业授权，再讨论技术合作。

## 1. 提交贡献即表示你同意

向本仓库提交 issue、PR、补丁、代码、文档、翻译或任何素材，即表示你：

1. 保证对该贡献拥有完整合法的权利，且未侵犯任何第三方权益；
2. 同意按 [LICENSE.txt](LICENSE.txt) 第 7 条的相同条款，无偿授权权利人及后续接收方使用、复制、修改、分发该贡献；
3. 理解你保留对自己原创部分的著作权，但其行使不得突破本协议对本项目整体的非商用限制；
4. 理解权利人没有义务接受、合并、回应贡献，也没有义务提供维护或支持。

## 2. 严禁提交的内容

提交前请自查，以下内容一律不得出现在 diff 中：

- 真实口令、JWT secret、API key、AccessKey/SecretKey、私钥、证书、token；
- 生产或内网 IP、真实域名、真实 ICP 备案号、第三方统计/追踪 ID；
- 真实客户数据、个人信息、身份证号、手机号、业务数据导出；
- 未获授权的第三方代码、字体、图标、图片、文档；
- 任何会绕过或削弱 `LICENSE.txt` 条款的改动。

仓库 `.gitignore` 已覆盖 `*.pem`、`*.key`、`.env*`、`env.prod` 等常见密钥路径，但**忽略规则不是保险**。请在提交前运行 `git diff --cached` 逐行确认。

## 3. 测试数据规范

- 演示账号、手机号、邮箱、身份证号等**必须虚构**，使用保留域（`.test`、`.example`）与连号示例值；
- 不得使用真实口令作为默认初始密码。代码与种子中的口令应来自环境变量或配置，缺省值留空或强制首次启动生成；
- 新增种子数据时，请在 SQL 头部注明「以下均为虚构测试数据」。

## 4. 代码与文档规范

- Java、Vue、TypeScript 的命名与目录约定见 [docs/architecture/05-naming-conventions.md](docs/architecture/05-naming-conventions.md)；
- 数据库约定见 [docs/architecture/07-database-conventions.md](docs/architecture/07-database-conventions.md)，API 约定见 [docs/architecture/08-api-conventions.md](docs/architecture/08-api-conventions.md)；
- 数据库变更一律新增 Flyway 迁移脚本，不要修改已发布的历史迁移；
- i18n 文案遵循 [docs/i18n-standard.md](docs/i18n-standard.md)，中英双语需同步；
- 不要删除或改写 `LICENSE.txt`、`NOTICE.txt` 中的版权与许可声明；新增源文件建议添加 SPDX 标记：
  `// SPDX-License-Identifier: LicenseRef-iCoolKJ-NonCommercial-1.0`

## 5. 提交流程

1. Fork 或创建分支，保持改动聚焦、原子化；
2. 本地通过构建与测试（`mvn test`、`pnpm test`、`pnpm build`）；
3. PR 描述中说明「改了什么 / 为什么 / 如何验证」，并确认已通过第 2 节自查；
4. 涉及许可证、品牌、作者信息、协议文案的改动，请先与权利人沟通，不要自行调整。

## 6. 行为准则

保持友善、就事论事。禁止人身攻击、歧视、骚扰、恶意灌水与未经许可公开他人隐私。权利人有权删除违规内容、关闭讨论或限制参与。

## 7. 联系

- 许可与商业授权：icoolkj <icoolkj@163.com> · QQ 594047667 · <https://icoolkj.com>
- 安全漏洞：见 [SECURITY.md](SECURITY.md)，请勿公开提交
