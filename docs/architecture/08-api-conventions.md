# API 设计规范

> 版本：V2.2 | 模块：api-conventions | 上游：[docs/README.md](../README.md)

## 概述

本篇定义 API 路径前缀、统一响应体、分页格式、错误码规范，以及前后端类型对照。

---

## API 前缀规范

| 前缀 | 用途 |
|------|------|
| `/api/auth/*` | 认证相关（登录、退出、刷新令牌） |
| `/api/system/*` | 系统管理（用户、角色、菜单等） |
| `/api/tenant/*` | **租户管理**（V2 新增） |
| `/api/infra/*` | 基础设施（文件、异步任务等） |
| `/api/biz/*` | 业务模块 |

---

## 响应体规范

### 成功响应

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

### 分页响应

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "list": [],
    "total": 0,
    "pageNo": 1,
    "pageSize": 20
  }
}
```

---

## 错误码规范

| 范围 | 含义 |
|------|------|
| `0` | 成功 |
| `40000-49999` | 业务错误 |
| `50000-59999` | 系统错误 |

---

## 前后端类型对照

### 统一响应包装

**Java**

```java
public class R<T> {
    private int code;
    private String message;
    private T data;
}
```

**TypeScript**

```typescript
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNo: number
  pageSize: number
}
```

### 登录请求（V2 含租户）

**TypeScript**

```typescript
export interface LoginRequest {
  username: string
  password: string
  tenantCode: string
}
```

**Java**

```java
public class LoginRequest {
    private String username;
    private String password;
    private String tenantCode;
}
```

### 实体 ID 与租户 ID

| 层级 | 类型 | 示例 |
|------|------|------|
| 数据库 | `varchar(20)` | `'1912345678901234567'` |
| Java | `String` | `"1912345678901234567"` |
| TypeScript | `string` | `'1912345678901234567'` |

> 前后端传递 ID 一律使用字符串，避免 JavaScript 大整数精度丢失。

---

## 相关文档

- 上一篇：[07 数据库规范](07-database-conventions.md)
- 下一篇：[09 核心模块](09-core-modules.md)
- 认证接口：[06 认证与权限](06-auth-permission.md)
- 多租户：[13 多租户设计](13-multi-tenant.md)

---

## 落地检查清单（Checklist）

- [ ] 所有 Controller 路径以 `/api/{模块}/` 开头
- [ ] 租户管理接口使用 `/api/tenant/*` 前缀
- [ ] 登录接口接收 `tenantCode` 参数
- [ ] 所有接口返回 `R<T>` 统一包装
- [ ] 分页接口 `data` 包含 `list`、`total`、`pageNo`、`pageSize`
- [ ] 所有 ID 字段前后端均使用 `string` 类型
