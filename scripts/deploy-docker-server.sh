#!/usr/bin/env bash
# 在服务器上运行：只拉镜像并启动（目录内只需 docker-compose.images.yml + .env）
set -euo pipefail

DIR="${APP_DIR:-$(cd "$(dirname "$0")/.." && pwd)}"
COMPOSE_FILE="${COMPOSE_FILE:-$DIR/deploy/compose/docker-compose.images.yml}"
ENV_FILE="${ENV_FILE:-$DIR/deploy/env/.env}"

if [[ -f "$DIR/docker-compose.images.yml" && -f "$DIR/.env" ]]; then
  COMPOSE_FILE="$DIR/docker-compose.images.yml"
  ENV_FILE="$DIR/.env"
fi

if [[ ! -f "$ENV_FILE" ]]; then
  echo "[错误] 缺少 $ENV_FILE"
  echo "请复制 deploy/env/.env.example 并配置 IMAGE_REGISTRY / IMAGE_TAG / 密码"
  exit 1
fi

if grep -q 'your-registry.example.com' "$ENV_FILE" 2>/dev/null; then
  echo "[错误] 请先配置 .env 中的 IMAGE_REGISTRY 与 IMAGE_TAG"
  exit 1
fi

echo "[1/2] 拉取镜像..."
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" pull

echo "[2/2] 启动服务..."
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d

HTTP_PORT="$(grep -E '^HTTP_PORT=' "$ENV_FILE" | cut -d= -f2- || true)"
HTTP_PORT="${HTTP_PORT:-80}"

echo ""
echo "部署完成: http://<服务器IP>:${HTTP_PORT}"
echo "默认账号: admin / StudyOnly@2026"
echo "查看状态: docker compose -f $COMPOSE_FILE --env-file $ENV_FILE ps"
echo "查看日志: docker compose -f $COMPOSE_FILE --env-file $ENV_FILE logs -f api"
