# 区划与租户模型

> 版本：**V2.0（定稿）** | 模块：region-tenant | 上游：[13 多租户](13-multi-tenant.md)、[06 认证与权限](06-auth-permission.md)

## 概述

平台严格区分两类业务租户：**企业租户**与**单位租户**。区划（`sys_region`）为平台国标库，租户通过 `root_region_id` 绑定服务根区划；**部门（`sys_dept`）仅表示租户内编制**，不承载区划。

**定稿约束（2026-07-10）：**

| # | 决策 |
|---|------|
| 1 | 区划：国标 GB/T 2260 + **国家节点**（`region_level=0`），平台统一维护（`tenant_id=0`） |
| 2 | 租户类型：`tenant_type` = `ENTERPRISE`（企业）\| `ORGANIZATION`（单位），**创建时必选、不可混用规则** |
| 3 | 一租户一根区划（`root_region_id`）；企业仅可选省/市/区县；单位可选 **国家/省/市/区县** |
| 4 | 单位租户可建父子租户（`parent_tenant_id`），层级与区划对齐；**企业租户禁止父子** |
| 5 | 部门 = 租户内标准树（`parent_id`），**不在部门表存区划** |
| 6 | 业务表可带 `region_id`（事项发生地），默认取自租户 `root_region_id` |
| 7 | 区划数据权限（`data_scope` 6/7/8）锚点为 **租户 `root_region_id`**，P2 实现 |

---

## 概念模型

```text
平台 sys_region（国家 → 省 → 市 → 区县 → 街道 → 社区）
        │
        │ root_region_id
        ▼
sys_tenant（tenant_type + 可选 parent_tenant_id）
        │
        ├── ENTERPRISE   企业租户（扁平，无 parent）
        └── ORGANIZATION 单位租户（可 parent 链，对齐区划）
        │
        └── sys_dept（纯部门树，tenant 内编制）
                └── sys_user.dept_id
```

### 两类租户对比

| 维度 | 企业租户 `ENTERPRISE` | 单位租户 `ORGANIZATION` |
|------|----------------------|-------------------------|
| 现实对象 | 公司、SaaS 客户、子公司 | 政府机关、事业单位 |
| `parent_tenant_id` | **必须为空** | 可为空（顶级单位）或指向上级单位租户 |
| `root_region_id` | 省 / 市 / 区县（**必填**） | 国家 / 省 / 市 / 区县（**必填**） |
| 组织管理 | 部门树（`GenTreeCrudPage`） | 部门树（同企业） |
| 租户层级 UI | 无 | 平台可管理下级单位租户 |
| 典型示例 | iCoolKJ（合肥） | 省厅 → 市局 → 区县局 |

---

## 区划层级

| `region_level` | 名称 | 说明 |
|----------------|------|------|
| `0` | 国家 | 如「中国」，单位租户国家级 root 可选 |
| `1` | 省 | |
| `2` | 市 | |
| `3` | 区县 | |
| `4` | 街道 | 随子树可见，不作租户 root |
| `5` | 社区 | 随子树可见，不作租户 root |

**租户可见区划：** `root_region_id` 及其全部下级（含街道、社区）。

**树结构示例：**

```text
中国 (0)
 └── 安徽省 (1)
      └── 合肥市 (2)
           ├── 蜀山区 (3)
           └── 包河区 (3)
```

---

## 表结构

### 1. `sys_region`

| 字段 | 说明 |
|------|------|
| `tenant_id` | 固定 `'0'` |
| `parent_id` / `ancestors` | 区划树 |
| `region_code` | 国标码；国家可用 `100000` |
| `region_name` | 名称 |
| `region_level` | `0`~`5` 见上表 |

### 2. `sys_tenant`

| 字段 | 说明 |
|------|------|
| `tenant_type` | `ENTERPRISE` / `ORGANIZATION`，NOT NULL |
| `parent_tenant_id` | 上级单位租户 ID；企业必须为 NULL |
| `ancestors` | 上级租户 ID 链（单位租户树） |
| `root_region_id` | 服务根区划 ID，NOT NULL |

**校验规则：**

| 规则 | 说明 |
|------|------|
| R1 | `ENTERPRISE` ⇒ `parent_tenant_id` 为空 |
| R2 | `ORGANIZATION` 且 `parent_tenant_id` 非空 ⇒ 父租户必须为 `ORGANIZATION` |
| R3 | 子租户 `root_region_id` 必须在父租户 `root_region_id` 的区划子树内 |
| R4 | 企业 root：`region_level IN (1,2,3)` |
| R5 | 单位 root：`region_level IN (0,1,2,3)` |

### 3. `sys_dept`

标准租户内部门树：`tenant_id`、`parent_id`、`ancestors`、`dept_name`…  
**不含** `region_id`、`dept_type`、`competent_id`（业务层不依赖）。

### 4. `sys_role.data_scope` 扩展（P2）

| 值 | 含义 |
|----|------|
| 6 | 本区划及下级（相对租户 `root_region_id`） |
| 7 | 本区划 |
| 8 | 自定义区划（`sys_role_region`） |

用户有效区划：`sys_tenant.root_region_id`（非部门推导）。

---

## 典型场景

### 企业租户

```text
iCoolKJ（ENTERPRISE, root=合肥市）
  └── 部门：研发中心、销售部、人事部
```

### 单位租户（层级）

```text
安徽省生态环境厅（ORGANIZATION, root=安徽省）
  └── 合肥市生态环境局（ORGANIZATION, parent=省厅, root=合肥市）
        └── 部门：办公室、执法支队
```

### 全国型集团（企业）

```text
多个 ENTERPRISE 租户（一省/一市一个），无 parent_tenant_id
```

---

## 平台严格划分

### 平台管理员（`tenant_id=0`）

| 能力 | 说明 |
|------|------|
| 区划管理 | 维护国标库 CRUD（含国家节点）；租户侧只读 |
| 租户管理 | 创建/编辑 **企业** 或 **单位** 租户，校验 R1–R5 |
| 单位租户树 | 仅对 `ORGANIZATION` 展示 `parent_tenant_id` 选择器 |
| 菜单模板 | 不变；`assign_scope` 区分 PLATFORM / TENANT |

### 企业租户管理员

| 能力 | 说明 |
|------|------|
| 部门管理 | 标准部门树 |
| 用户/角色 | 租户内 |
| 区划 | 只读 `tenant-tree`（裁剪至 root），**无区划管理菜单** |
| 下级租户 | **不可创建** |

### 单位租户管理员

| 能力 | 说明 |
|------|------|
| 部门管理 | 标准部门树（与企业相同） |
| 用户/角色 | 租户内 |
| 区划 | 只读子树 |
| 下级单位租户 | P2：上级单位可代管或直接由平台创建子租户 |

---

## API 要点

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/system/region/tree` | 平台全量区划树 |
| POST | `/system/region` | 新增区划（平台） |
| PUT | `/system/region/{id}` | 修改区划（平台） |
| DELETE | `/system/region/{id}` | 删除区划（平台，无子节点/引用） |
| GET | `/system/region/selectable-roots?tenantType=` | 按租户类型返回可选 root 区划 |
| GET | `/system/region/tenant-tree` | 当前租户可见区划子树 |
| CRUD | `/tenant/tenant/*` | 含 `tenantType`、`parentTenantId`、`rootRegionId` |

---

## 研发路线

| 阶段 | 交付 |
|------|------|
| **P0** | ✅ 本文档 V2.0 + DDL（`tenant_type`、国家区划、部门回纯树） |
| **P1** | ✅ 租户 UI 区分两类；单位父子租户；部门/区划 Gen 树；平台区划 CRUD |
| **P2** | `data_scope` 6/7/8 + `sys_role_region` |
| **P3** | 业务表 `region_id` 规范 + Codegen |
| **P4** | 工作流按区划层级路由 |

---

## 相关文档

- [AI 速查](../AI-CONTEXT.md)
- [13 多租户设计](13-multi-tenant.md)
- [06 认证与权限](06-auth-permission.md)
- [09 核心模块](09-core-modules.md)
