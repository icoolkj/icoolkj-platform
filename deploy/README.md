# Docker 部署指南（推荐）

全栈 Docker：**MySQL + Redis + MinIO + API + Worker + Nginx 前端**，一条 compose 拉起。

服务器**不需要**上传 jar / dist / 源码（你之前传到 `/data/icoolkj-platform` 的 jar 和 dist **可以删掉**，改走 Docker）。

---

## 推荐流程（本机构镜像 → 服务器只拉镜像）

```text
Windows/Mac 开发机（有源码）
  ① docker build  →  ② docker push 到私有仓库

Linux 服务器（只有 2 个文件）
  docker-compose.images.yml + .env
  ③ docker compose pull  →  ④ docker compose up -d
```

**无源码泄露**：服务器上没有 `.java` / `.vue`，只有镜像（jar + 编译后的 dist 已在镜像内）。

---

## 在服务器上构建并推送（不用本地电脑）

你已自建镜像库（如 `http://your-server.example.com:5000/`）时，可在**服务器上**完成 build + push + 运行。

### 0. HTTP 私有仓库必配（仅首次）

Docker 默认不允许推送到 **HTTP** 仓库，需在本机和服务器的 `/etc/docker/daemon.json` 增加：

```json
{
  "insecure-registries": ["your-server.example.com:5000"]
}
```

然后：

```bash
systemctl restart docker
```

HTTPS 仓库可跳过此步。

### 1. 服务器上准备源码（仅构建时需要）

构建阶段需要源码；**运行阶段**只需镜像，不需要源码。

```bash
cd /opt
git clone <你的仓库> icoolkj-platform
cd icoolkj-platform
```

或用 zip 上传后解压，构建完可删除源码目录，只保留 compose + `.env`。

### 2. 配置 .env

```bash
cp deploy/env/.env.example deploy/env/.env
vim deploy/env/.env
```

```env
IMAGE_REGISTRY=registry.example.com:5000/icoolkj
IMAGE_TAG=1.0.0
# ... 密码、JWT 等同上
```

### 3. 服务器构建并推送镜像

```bash
chmod +x scripts/build-push-on-server.sh scripts/deploy-docker-server.sh

export REGISTRY=registry.example.com:5000/icoolkj
export TAG=1.0.0
./scripts/build-push-on-server.sh
```

### 4. 启动（从本仓库 pull 再 up）

```bash
./scripts/deploy-docker-server.sh
```

同一台机器上若刚 push 完，也可直接：

```bash
docker compose -f deploy/compose/docker-compose.images.yml --env-file deploy/env/.env up -d
```

### 5. 访问

`http://your-server.example.com/`（若 `HTTP_PORT=80`）或你映射的端口。

> **说明**：构建时要有源码；镜像进库后，可把 `/opt/icoolkj-platform` 源码删掉，只留 `/opt/icoolkj/` 下的 `docker-compose.images.yml` + `.env` 做日常运维。

---

## 第一步：本机构建并推送镜像（可选，有开发机时用）

在项目根目录（Git Bash / WSL / Linux）：

```bash
docker login registry.cn-hangzhou.aliyuncs.com   # 或你的仓库

export REGISTRY=registry.cn-hangzhou.aliyuncs.com/你的命名空间
export TAG=1.0.0

chmod +x scripts/build-push-images.sh
./scripts/build-push-images.sh
```

会推送三个镜像：

- `{REGISTRY}/icoolkj-api:{TAG}`
- `{REGISTRY}/icoolkj-worker:{TAG}`
- `{REGISTRY}/icoolkj-web:{TAG}`

> Windows 需安装 Docker Desktop，在 WSL2 或 Git Bash 里执行上述命令。

---

## 第二步：服务器准备

```bash
# 安装 Docker（若未装）
curl -fsSL https://get.docker.com | sh

mkdir -p /opt/icoolkj
cd /opt/icoolkj
```

只上传 **两个文件**（不要上传整个源码仓库）：

```bash
# 在本机执行
scp deploy/compose/docker-compose.images.yml root@服务器:/opt/icoolkj/
scp deploy/env/.env.example root@服务器:/opt/icoolkj/.env
```

在服务器编辑 `.env`：

```bash
vim /opt/icoolkj/.env
```

必改项：

```env
IMAGE_REGISTRY=registry.cn-hangzhou.aliyuncs.com/你的命名空间
IMAGE_TAG=1.0.0
MYSQL_ROOT_PASSWORD=...
MYSQL_PASSWORD=...
REDIS_PASSWORD=...
MINIO_ROOT_PASSWORD=...
JWT_SECRET=...
HTTP_PORT=80
```

若仓库需登录：

```bash
docker login registry.cn-hangzhou.aliyuncs.com
```

---

## 第三步：服务器一键启动

```bash
cd /opt/icoolkj
docker compose -f docker-compose.images.yml --env-file .env pull
docker compose -f docker-compose.images.yml --env-file .env up -d
```

或使用脚本（若把整个仓库放在服务器上）：

```bash
chmod +x scripts/deploy-docker-server.sh
./scripts/deploy-docker-server.sh
```

访问：`http://服务器IP/`  
账号：`admin` / `StudyOnly@2026`

---

## 常用命令

```bash
cd /opt/icoolkj
COMPOSE="docker compose -f docker-compose.images.yml --env-file .env"

$COMPOSE ps
$COMPOSE logs -f api
$COMPOSE restart web
$COMPOSE down          # 停容器，数据卷保留
```

---

## 更新版本

本机重新 build-push 新 TAG 后，服务器改 `.env` 里 `IMAGE_TAG`，再：

```bash
docker compose -f docker-compose.images.yml --env-file .env pull
docker compose -f docker-compose.images.yml --env-file .env up -d
```

---

## 备选：服务器 clone 源码现编（不推荐公网）

适合内网、不介意服务器上有源码：

```bash
git clone ... && cd icoolkj-platform
cp deploy/env/.env.example deploy/env/.env
./scripts/deploy.sh
```

使用 `deploy/compose/docker-compose.yml`（含 `build:`），**会**在服务器编译源码。

---

## 与已上传 jar/dist 的关系

| 文件 | Docker 部署 |
|------|-------------|
| `icoolkj-admin-*.jar` | ❌ 不需要，已在 api/worker 镜像里 |
| `dist/` | ❌ 不需要，已在 web 镜像里 |
| `docker-compose.images.yml` + `.env` | ✅ 足够 |

可清理：`rm -rf /data/icoolkj-platform/dist *.jar`（确认改用 `/opt/icoolkj` Docker 后再删）。

---

## 架构

```text
浏览器 → web:80 (Nginx + 前端)
              └→ /api/ → api:8080
api / worker → mysql, redis, minio（Docker 内网）
```

详细配置见 [configuration.md](../docs/configuration.md)。
