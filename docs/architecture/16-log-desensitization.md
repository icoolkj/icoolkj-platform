# 日志脱敏规则

> 版本：V2.2 | 模块：log-desensitization | 上游：[docs/README.md](../README.md)

## 概述

本篇定义平台日志分类及操作日志、访问日志、第三方调用日志的脱敏规则与处理原则。

---

## 日志分类

| 表名 | 类型 |
|------|------|
| `sys_login_log` | 登录日志 |
| `sys_operation_log` | 操作日志 |
| `infra_api_access_log` | 接口访问日志 |
| `infra_thirdparty_call_log` | 第三方调用日志 |

V2.1 约定：`request_param`、`response_result`、`request_data`、`response_data` 字段存储**脱敏后**内容。

---

## 必须脱敏的字段

- password / oldPassword / newPassword / confirmPassword（字段名含 password/passwd 均脱敏）
- token / accessToken / refreshToken
- secret / appSecret
- Authorization
- 手机号
- 身份证号
- 银行卡号
- 邮箱（可部分脱敏）

---

## 脱敏处理原则

1. 请求参数记录前先脱敏
2. 响应结果记录前先脱敏
3. Header 中敏感字段先脱敏
4. 超长文本按长度截断
5. 文件上传接口不记录二进制内容
6. 导入类接口不记录原始文件流
7. 可通过注解或配置指定忽略字段

---

## 脱敏示例

```json
{
  "username": "admin",
  "password": "******",
  "phone": "138****1234",
  "token": "******"
}
```

---

## 实现建议

- `icoolkj-admin-framework` 提供 `LogDesensitizeUtil` 或 AOP
- `@LogDesensitize` 注解标记需脱敏字段
- 操作日志 AOP 在落库前统一脱敏
- API 访问日志 Filter/Interceptor 脱敏 query/body

---

## 相关文档

- [09 核心模块](09-core-modules.md)
- [06 认证与权限](06-auth-permission.md)
- [backend/database/schema/sys_tables.sql](../../backend/database/schema/sys_tables.sql)

---

## 落地检查清单（Checklist）

- [ ] 操作日志 `request_param`/`response_result` 脱敏后落库
- [ ] 接口访问日志 `request_param` 脱敏后落库
- [ ] 第三方调用日志 `request_data`/`response_data` 脱敏后落库
- [ ] 密码、token 类字段永不明文落库
- [ ] 上传/导入接口不记录文件二进制
- [ ] 超长日志文本有截断上限
