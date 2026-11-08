#!/usr/bin/env bash
# 在服务器上构建并推送到私有镜像仓库（无需本地电脑）
# 前提：服务器上有项目源码目录，且已配置 insecure-registries（HTTP 仓库）
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

ENV_FILE="${ENV_FILE:-$ROOT/deploy/env/.env}"
if [[ -f "$ENV_FILE" ]]; then
  # shellcheck disable=SC1090
  source "$ENV_FILE"
fi

REGISTRY="${REGISTRY:-$IMAGE_REGISTRY}"
TAG="${TAG:-${IMAGE_TAG:-1.0.0}}"

if [[ -z "$REGISTRY" || "$REGISTRY" == "your-registry.example.com/icoolkj" ]]; then
  echo "[错误] 请设置 REGISTRY 或在 deploy/env/.env 中配置 IMAGE_REGISTRY"
  echo "  示例: export REGISTRY=registry.example.com:5000/icoolkj"
  exit 1
fi

API_IMAGE="$REGISTRY/icoolkj-api:$TAG"
WORKER_IMAGE="$REGISTRY/icoolkj-worker:$TAG"
WEB_IMAGE="$REGISTRY/icoolkj-web:$TAG"

echo "==> 构建 api / worker / web (tag=$TAG)"
docker build -f deploy/docker/backend.Dockerfile --target api -t "$API_IMAGE" .
docker build -f deploy/docker/backend.Dockerfile --target worker -t "$WORKER_IMAGE" .
docker build -f deploy/docker/frontend.Dockerfile -t "$WEB_IMAGE" .

echo "==> 推送到 $REGISTRY"
docker push "$API_IMAGE"
docker push "$WORKER_IMAGE"
docker push "$WEB_IMAGE"

echo ""
echo "完成。启动/更新服务："
echo "  ./scripts/deploy-docker-server.sh"
echo ""
echo "或手动："
echo "  docker compose -f deploy/compose/docker-compose.images.yml --env-file deploy/env/.env up -d"
