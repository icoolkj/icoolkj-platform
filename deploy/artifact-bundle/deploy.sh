#!/usr/bin/env bash
# 在 artifact-bundle 目录执行；jar/dist 在上级 /data/icoolkj-platform/
set -euo pipefail

BUNDLE_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$BUNDLE_DIR/.." && pwd)"
COMPOSE_FILE="$BUNDLE_DIR/docker-compose.yml"
ENV_FILE="$BUNDLE_DIR/.env"

for f in "$ROOT/icoolkj-admin-api-1.0.0-SNAPSHOT.jar" "$ROOT/icoolkj-admin-worker-1.0.0-SNAPSHOT.jar" "$ROOT/dist"; do
  if [[ ! -e "$f" ]]; then
    echo "[错误] 缺少: $f"
    echo "jar 与 dist 应放在: $ROOT"
    exit 1
  fi
done

if [[ ! -f "$ENV_FILE" ]]; then
  cp "$BUNDLE_DIR/.env.example" "$ENV_FILE"
  echo "[提示] 已生成 $ENV_FILE"
  echo "[提示] 请先 vim $ENV_FILE 修改密码后再运行"
  exit 0
fi

cd "$ROOT"
echo "==> 构建并启动（MySQL + Redis + MinIO + API + Worker + Web）..."
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d --build

HTTP_PORT="$(grep -E '^HTTP_PORT=' "$ENV_FILE" | cut -d= -f2- || true)"
HTTP_PORT="${HTTP_PORT:-80}"
IP="$(hostname -I 2>/dev/null | awk '{print $1}' || echo '<服务器IP>')"

echo ""
echo "部署完成: http://${IP}:${HTTP_PORT}"
echo "默认账号: admin / StudyOnly@2026"
echo "查看日志: docker compose -f $COMPOSE_FILE --env-file $ENV_FILE logs -f api"
