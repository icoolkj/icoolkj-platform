# iCoolKJ 代码生成器

**独立代码生成工具**，按单表生成增删改查全套基础代码，后期可扩展前端页面生成。

## 目录结构

```
tools/icoolkj-codegen/
├── codegen.py              # CLI 入口
├── config/
│   └── tables.yaml         # 表清单（模块、base 类型、crud 模式、路由）
├── custom/                 # 自定义 Mapper SQL 片段
│   ├── UserMapper.java.frag
│   └── UserMapper.xml.frag
├── icoolkj_codegen/        # 生成引擎
│   ├── model.py            # 表元数据 / SQL 解析
│   ├── render.py           # 模板渲染
│   └── engine.py           # 编排与写文件
├── templates/frontend/     # 前端模板（预留）
└── requirements.txt
```

## 快速开始

```bash
pip install -r tools/icoolkj-codegen/requirements.txt

# 列出 22 张表及 crud 模式
python tools/icoolkj-codegen/codegen.py --list

# 生成全部（Entity + Mapper + XML + DTO + VO + Service + Controller）
python tools/icoolkj-codegen/codegen.py --all

# 仅生成有完整 CRUD 栈的表（跳过关联表、手写 Service 的日志表）
python tools/icoolkj-codegen/codegen.py --crud-only

# 单表验证
python tools/icoolkj-codegen/codegen.py --tables sys_post

# 含前端 API 占位（输出到 frontend/src/api/generated/）
python tools/icoolkj-codegen/codegen.py --tables sys_post --artifacts entity,mapper,xml,service,controller,frontend
```

## 生成产物（单表 CRUD）

| 层级 | 产物 | 说明 |
|------|------|------|
| Domain | `{Biz}Entity` | Lombok + `BaseEntity`/`TenantBaseEntity` |
| Mapper | `{Biz}Mapper` + XML | `selectById/selectList/selectPage/countByQuery/insert/updateById/deleteById` |
| DTO | `{Biz}PageQuery/CreateRequest/UpdateRequest` | 分页 + 入参 |
| VO | `{Biz}VO` | 出参 |
| Convert | `{Biz}Convert` | Entity ↔ DTO/VO |
| Service | `{Biz}Service` + `Impl` | 标准 CRUD，**主键 `IdGeneratorUtils.nextIdStr()`** |
| Controller | `{Biz}Controller` | REST + `ApiResponse` + `PageResult` |

## 通用字段约定

| 字段 | 来源 |
|------|------|
| `id` | 创建时 `IdGeneratorUtils.nextIdStr()`（雪花算法） |
| `createdBy/updatedBy/createdAt/updatedAt/deleted` | `MetaObjectHandlerImpl` 自动填充 |
| `tenantId` | 租户上下文自动填充（`TenantBaseEntity`） |

配置项（`application.yml`）：

```yaml
icoolkj:
  id-generator:
    worker-id: 1
    datacenter-id: 1
```

## 表配置 `crud` 模式

| 模式 | 说明 | 示例 |
|------|------|------|
| `full` | 分页 + 详情 + 增删改 | sys_post, sys_user |
| `readonly` | 分页 + 详情 | 日志类 |
| `none` | 仅 Entity/Mapper/XML | sys_user_role |

`gen_stack: false` — 保留手写 Service（如 `LoginLogServiceImpl`），仅更新 Mapper。

## 新增一张表

1. 在 `backend/database/migration/` 添加 DDL  
2. 在 `config/tables.yaml` 追加条目  
3. 如有自定义 SQL → `custom/{Mapper}.*.frag`  
4. `python tools/icoolkj-codegen/codegen.py --tables 新表名`  

## 后续扩展

- [ ] Vue 列表/表单页面（`templates/frontend/`）
- [ ] `--merge` 保留手写代码块
- [ ] 可视化 Web UI（代码生成菜单）

## 兼容入口

`scripts/codegen/generator.py` 已转发到本工具，旧命令仍可使用。
