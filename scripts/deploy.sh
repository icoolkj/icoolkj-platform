#!/usr/bin/env bash
# 一键部署到 Linux 服务器（需已安装 Docker 与 Docker Compose v2）
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ENV_EXAMPLE="$ROOT/deploy/env/.env.example"
ENV_FILE="$ROOT/deploy/env/.env"
COMPOSE_FILE="$ROOT/deploy/compose/docker-compose.yml"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "[错误] 未找到命令: $1"
    exit 1
  fi
}

require_cmd docker
if ! docker compose version >/dev/null 2>&1; then
  echo "[错误] 需要 Docker Compose v2（docker compose）"
  exit 1
fi

mkdir -p "$ROOT/deploy/env"

if [[ ! -f "$ENV_FILE" ]]; then
  cp "$ENV_EXAMPLE" "$ENV_FILE"
  echo "[提示] 已生成 $ENV_FILE"
  echo "[提示] 请先编辑其中的密码与 JWT_SECRET，然后重新运行本脚本"
  exit 0
fi

if grep -q 'ChangeMe_' "$ENV_FILE"; then
  echo "[警告] deploy/env/.env 仍含默认占位密码，生产环境请务必修改后再部署"
fi

echo "[1/3] 构建并启动容器..."
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d --build

echo "[2/3] 等待 API 健康检查..."
for i in $(seq 1 60); do
  if docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps api 2>/dev/null | grep -q '(healthy)'; then
    break
  fi
  sleep 5
done

HTTP_PORT="$(grep -E '^HTTP_PORT=' "$ENV_FILE" | cut -d= -f2- || true)"
HTTP_PORT="${HTTP_PORT:-80}"

echo "[3/3] 部署完成"
echo "  访问地址: http://<服务器IP>:${HTTP_PORT}"
echo "  默认账号: admin / StudyOnly@2026 （首次登录需改密）"
echo ""
echo "常用命令:"
echo "  查看状态: docker compose -f deploy/compose/docker-compose.yml --env-file deploy/env/.env ps"
echo "  查看日志: docker compose -f deploy/compose/docker-compose.yml --env-file deploy/env/.env logs -f api"
echo "  停止服务: docker compose -f deploy/compose/docker-compose.yml --env-file deploy/env/.env down"
