# 第三方组件与许可证声明

本文件列出 iCoolKJ Platform 直接依赖的主要第三方组件及其常见许可证，用于归属声明与合规提示。

> **重要免责声明**
> 1. 本文件**仅列出直接依赖**，不包含传递依赖。完整的依赖树与许可证元数据请以 `backend/pom.xml`、`frontend/package.json`、`site/package.json`、`portal/package.json` 及各 lock 文件为准，并使用 `mvn dependency:tree`、`npm ls --all`、`license-checker` 等工具自行复核。
> 2. 下表的许可证信息依据各组件公开发布的常见版本整理，**不保证准确或最新**。任何分发、集成或商业使用前，请以组件官方仓库中的 LICENSE 文件为准，必要时咨询法律顾问。
> 3. **第三方组件不受 iCoolKJ 非商业学习研究许可证约束**，各自适用其原始许可证。若其条款与本仓库许可存在冲突，以第三方组件原始条款为准，并需由使用方自行解决兼容性问题。
> 4. 本仓库 `LICENSE.txt` 是**非 OSI 开源许可证**。部分依赖的许可证例外条款（license exception）以「使用 OSI 认可的开源许可证」为适用前提，本项目可能不满足该前提，见下方「需重点复核」章节。

---

## 后端（Java 17 / Maven）

| 组件 | 版本 | 常见许可证 |
|---|---|---|
| Spring Boot / Spring Framework / Spring Security / Spring Cloud | 3.2.5 / 2023.0.1 | Apache-2.0 |
| MyBatis-Plus | 3.5.5 | Apache-2.0 |
| Flowable Process Engine | 7.0.1 | Apache-2.0 |
| Redisson | 3.27.2 | Apache-2.0 |
| JJWT (api / impl / jackson) | 0.12.5 | Apache-2.0 |
| MapStruct | 1.5.5.Final | Apache-2.0 |
| Alibaba EasyExcel | 3.3.4 | Apache-2.0 |
| MinIO Java SDK | 8.5.10 | Apache-2.0 |
| Apache Commons IO | 2.15.1 | Apache-2.0 |
| Apache Velocity Engine | — | Apache-2.0 |
| Jackson Databind | — | Apache-2.0 |
| **MySQL Connector/J** | **8.3.0** | **GPL-2.0-only WITH FOSS License Exception**（见下方重点复核） |
| Bouncy Castle (bcprov-jdk18on) | 1.78.1 | Bouncy Castle Licence（MIT 风格） |
| Hutool | 5.8.27 | MulanPSL-2.0（木兰宽松许可证 第 2 版） |
| OSHI Core | 6.6.0 | MIT |
| jsoup | 1.17.2 | MIT |
| ip2region | 2.7.0 | Apache-2.0（请复核） |
| java-otp | 0.4.0 | MIT（请复核） |
| Project Lombok | — | MIT |
| JUnit Jupiter | — | EPL-2.0 |

## 前端与管理端（Node / Vue 3）

| 组件 | 常见许可证 |
|---|---|
| Vue 3、Vue Router、Pinia、vue-i18n | MIT |
| Element Plus、@element-plus/icons-vue | MIT |
| Axios | MIT |
| WangEditor (`@wangeditor/editor`, `editor-for-vue`) | MIT |
| cropperjs | MIT |
| sm-crypto（国密算法实现） | MIT |
| highlight.js | BSD-3-Clause |
| Vite、Vitest、ESLint、Prettier、Sass | MIT |
| TypeScript | Apache-2.0 |
| jsdom | MIT |
| ajv、ajv-formats | MIT |
| yaml (eemeli/yaml) | ISC |
| PyYAML（tools/icoolkj-codegen） | MIT |

---

## 需重点复核的合规风险

### 1. MySQL Connector/J — GPL-2.0 + FOSS License Exception

`mysql-connector-j` 采用 **GPL-2.0 附通用 FOSS 许可证例外**。该例外通常要求下游项目使用 **OSI 认可的开源许可证**（FOSS License）分发，才能在不触发 GPL 整体传染的前提下使用连接器。

本仓库使用的是**自定义非商用许可证，不是 OSI 认可的开源许可证**。因此：

- 存在「FOSS License Exception 是否适用」的实质不确定性；
- 若例外不适用，分发包含该连接器的整体作品可能需要按 GPL-2.0 提供相应源代码与权利，而 GPL-2.0 与「禁止商用」条款直接冲突。

**建议**（请自行与法律顾问确认后择一）：
1. 将 JDBC 驱动改为在运行时由使用者自行提供（不在发行物中捆绑），并在文档中说明；
2. 替换为许可证更宽松的驱动（如 MariaDB Connector/J，LGPL-2.1，需同样复核兼容性）；
3. 向 Oracle 获取书面确认或单独的商业授权。

### 2. 强著佐权 / 弱著佐权组件

Hutool 的 MulanPSL-2.0、JUnit 的 EPL-2.0、TypeScript 的 Apache-2.0 等条款各有归属与声明要求。若未来把本项目打包为发行物，需随附对应许可证全文与版权声明。

### 3. 素材与代码来源

- `frontend/src/assets/brand/platform-logo.png` 为唯一二进制品牌素材，其创作来源与授权状态未在仓库中说明，权利人应补充归属说明或替换为明确自有素材。
- `scripts/` 中若干脚本命名提及 RuoYi（若依）分层风格。经初步检查未发现复制粘贴的第三方源码或第三方版权头；若有实质性代码搬运，须按 RuoYi 的 MIT 许可证保留版权声明并在此登记。

---

## 维护方式

新增或升级直接依赖时，请同步更新本文件，并在 PR 中说明许可证变化。建议在 CI 中接入许可证扫描（如 `license-checker`、`mvn license:aggregate-add-third-party`、FOSSA / ScanCode）与密钥扫描（gitleaks / trufflehog）。
