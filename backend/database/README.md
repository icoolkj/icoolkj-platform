# 数据库目录

```
database/
├── migration/          # Flyway 权威来源（V1–V4）
│   ├── V1__schema.sql
│   ├── V2__platform_seed.sql   # 平台种子（含 notice_publish、文件访问策略、表单设计器/办公助手、AI Skill）
│   ├── V3__attachment_demo.sql
│   └── V4__general_office.sql  # 通用办事：平台基础模板 + icoolkj 完整工作流
├── seed/               # 可选说明（业务已进 V4）
│   └── README.md
└── init/               # 删库重建脚本
    └── 00_reset_database.sql
```

**Flyway 维护 `migration/` 下 V1–V4**；应用启动时从 `classpath:db/migration` 加载。  
`icoolkj` 测试租户与 10 套完整通用办事工作流已在 V4；平台侧仅为可拉取基础模板（无流程设计）。无需再手工导 seed。  
策略对齐（OA CUSTOM、`attachment_demo_*`、无孤儿 `sys_notice`/`sys_banner` 下载策略）已写入 **V2**；升级按空库重建。

## 快速开始

见 [migration/README.md](migration/README.md) 与 [docs/AI-CONTEXT.md](../../docs/AI-CONTEXT.md)。
