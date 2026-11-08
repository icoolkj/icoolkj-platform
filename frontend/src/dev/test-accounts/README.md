# 联调测试账号（临时）

- 页面：`/dev/test-accounts`
- 登录页仅保留「测试账号一览」链接（DEV / 演示模式）

## 后期去除

1. 删除本目录 `frontend/src/dev/test-accounts`
2. 删除 `frontend/src/router/index.ts` 中 `registerDevTestAccountRoutes` 的 import 与调用
3. 删除登录页中的 `showTestAccountsLink` / `login-test-link` 相关代码与样式
