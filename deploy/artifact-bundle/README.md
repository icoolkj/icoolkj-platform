# 服务器已有 jar + dist 时，直接 Docker 部署

适用于目录里**已经有**：

- `icoolkj-admin-api-1.0.0-SNAPSHOT.jar`
- `icoolkj-admin-worker-1.0.0-SNAPSHOT.jar`
- `dist/`

**不需要** Git 源码、不需要 Maven/npm 构建。

## 1. 把本目录文件拷到服务器同一文件夹

例如 `/data/icoolkj-platform/`：

```text
/data/icoolkj-platform/
  dist/
  icoolkj-admin-api-1.0.0-SNAPSHOT.jar
  icoolkj-admin-worker-1.0.0-SNAPSHOT.jar
  artifact-bundle/          # compose、Dockerfile、deploy.sh、.env
    docker-compose.yml
    Dockerfile.api
    ...
```

## 2. 在服务器执行

```bash
cd /data/icoolkj-platform/artifact-bundle

# 若从 Windows 上传，先去掉 CRLF（只需一次）
sed -i 's/\r$//' deploy.sh

cp .env.example .env
vim .env    # 改 MYSQL / REDIS / JWT 等密码

chmod +x deploy.sh
./deploy.sh
```

首次若只生成 `.env` 会提示你改密码，改完再执行一次 `./deploy.sh`。

## 3. 访问

- `http://your-server.example.com/`（`.env` 里 `HTTP_PORT=80`）
- 账号 `admin` / `StudyOnly@2026`

## 说明

- `env.prod` 是给**宿主机直跑 jar** 用的；Docker 全栈请用本目录 **`.env`**（含 MySQL/Redis 容器密码）。
- 不需要镜像仓库，Docker 在本目录直接 `build` 三个小镜像。
- 与 jar/dist 直部署相比：MySQL/Redis/MinIO 也一并容器化，无需单独安装。

## 常用命令

```bash
docker compose ps
docker compose logs -f api
docker compose down      # 停服务，数据卷保留
docker compose up -d --build   # 更新 jar/dist 后重建
```
