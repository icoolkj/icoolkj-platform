# 宿主机直部署（jar + dist，无 Docker）

适用于：本机 `mvn package` + `npm run build` 后，只把**产物**上传到服务器（如 `/data/icoolkj-platform`）。

**完整生产手册**（MySQL 8.4 二进制、Redis、JDK21、Nginx HTTPS、演示库日同步）：见 [docs/deployment-linux-host.md](../../docs/deployment-linux-host.md)。

**不是源码**：服务器上只有 jar 与静态文件，不含 `.java` / `.vue`。

## 目录示例

```text
/data/icoolkj-platform/
  dist/                                    # 前端 npm run build 产出
  icoolkj-admin-api-1.0.0-SNAPSHOT.jar     # 必传：HTTP 接口
  icoolkj-admin-worker-1.0.0-SNAPSHOT.jar  # 必传：定时任务
  env.prod                                 # 环境变量（从 env.prod.example 复制）
  logs/                                    # 运行后自动生成
  data/                                    # 上传文件、备份等（可选）
```

## 服务器还需具备

| 组件 | 说明 |
|------|------|
| **JDK 17+** | `java -version` |
| **Nginx** | 静态 `dist` + 反代 `/api/` |
| **MySQL 8** | 建库并允许连接 |
| **Redis** | 带密码 |
| **MinIO**（可选） | 文件存储；不配则 mainly local（需改 storage 配置） |

## 1. 本机打包

```bash
# 后端（在 backend 目录）
mvn clean package -DskipTests
# 产出：
#   icoolkj-admin-api/target/icoolkj-admin-api-1.0.0-SNAPSHOT.jar
#   icoolkj-admin-worker/target/icoolkj-admin-worker-1.0.0-SNAPSHOT.jar

# 前端（在 frontend 目录）
npm ci && npx vite build
# 产出：frontend/dist/
```

上传到服务器（示例）：

```bash
scp backend/icoolkj-admin-api/target/icoolkj-admin-api-1.0.0-SNAPSHOT.jar root@服务器:/data/icoolkj-platform/
scp backend/icoolkj-admin-worker/target/icoolkj-admin-worker-1.0.0-SNAPSHOT.jar root@服务器:/data/icoolkj-platform/
scp -r frontend/dist root@服务器:/data/icoolkj-platform/
```

## 2. 配置环境变量

```bash
cd /data/icoolkj-platform
cp deploy/host/env.prod.example env.prod
vim env.prod   # 改数据库、Redis、JWT 等
```

## 3. 启动后端

```bash
chmod +x deploy/host/start.sh
./deploy/host/start.sh
# 或指定目录：APP_HOME=/data/icoolkj-platform ./deploy/host/start.sh
```

首次启动 API 会跑 Flyway 迁移建表。

## 4. 配置 Nginx

```bash
cp deploy/host/nginx-icoolkj.conf /etc/nginx/conf.d/icoolkj.conf
# 修改 root 路径、server_name
nginx -t && systemctl reload nginx
```

参考配置见 `deploy/host/nginx-icoolkj.conf`（`root` 指向 `dist`，`/api/` 反代 `127.0.0.1:8080`）。

## 5. 访问

- `http://<服务器IP>/`
- 默认账号：`admin` / `StudyOnly@2026`

## 运维

```bash
./deploy/host/stop.sh
./deploy/host/start.sh
tail -f logs/icoolkj-admin-api.log
```

## 与 Docker 方案对比

| | jar + dist（本方案） | Docker 镜像 |
|--|---------------------|-------------|
| 服务器内容 | jar + dist | 仅镜像 |
| 需 JDK | 是 | 否（在镜像内） |
| 源码 | 无 | 无 |
| 适用 | 已有 Nginx/MySQL 的 VPS | 一键全栈 |
