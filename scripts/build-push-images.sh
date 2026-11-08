#!/usr/bin/env bash
# 在本机（有源码）构建并推送 Docker 镜像；服务器无需源码
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

REGISTRY="${REGISTRY:?请设置 REGISTRY，例如 registry.cn-hangzhou.aliyuncs.com/你的命名空间}"
TAG="${TAG:-1.0.0}"

API_IMAGE="$REGISTRY/icoolkj-api:$TAG"
WORKER_IMAGE="$REGISTRY/icoolkj-worker:$TAG"
WEB_IMAGE="$REGISTRY/icoolkj-web:$TAG"

echo "构建镜像 tag=$TAG"
docker build -f deploy/docker/backend.Dockerfile --target api -t "$API_IMAGE" .
docker build -f deploy/docker/backend.Dockerfile --target worker -t "$WORKER_IMAGE" .
docker build -f deploy/docker/frontend.Dockerfile -t "$WEB_IMAGE" .

echo "推送镜像..."
docker push "$API_IMAGE"
docker push "$WORKER_IMAGE"
docker push "$WEB_IMAGE"

echo ""
echo "完成。请在 deploy/env/.env 中设置："
echo "  IMAGE_REGISTRY=$REGISTRY"
echo "  IMAGE_TAG=$TAG"
echo ""
echo "服务器部署："
echo "  scp deploy/compose/docker-compose.images.yml deploy/env/.env root@服务器:/opt/icoolkj/"
echo "  ssh root@服务器 'cd /opt/icoolkj && docker compose -f docker-compose.images.yml --env-file .env pull && docker compose -f docker-compose.images.yml --env-file .env up -d'"
