#!/usr/bin/env bash
set -euo pipefail

APP_HOME="${APP_HOME:-/data/icoolkj-platform}"
ENV_FILE="${ENV_FILE:-$APP_HOME/env.prod}"
API_JAR="$APP_HOME/icoolkj-admin-api-1.0.0-SNAPSHOT.jar"
WORKER_JAR="$APP_HOME/icoolkj-admin-worker-1.0.0-SNAPSHOT.jar"
PID_DIR="$APP_HOME/run"
LOG_DIR="$APP_HOME/logs"

mkdir -p "$PID_DIR" "$LOG_DIR" "$APP_HOME/data/db-backup"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "[错误] 缺少 $ENV_FILE，请从 deploy/host/env.prod.example 复制并修改"
  exit 1
fi
# shellcheck disable=SC1090
source "$ENV_FILE"

if [[ ! -f "$API_JAR" ]]; then
  echo "[错误] 缺少 API 包: $API_JAR"
  exit 1
fi
if [[ ! -f "$WORKER_JAR" ]]; then
  echo "[错误] 缺少 Worker 包: $WORKER_JAR"
  exit 1
fi

start_one() {
  local name=$1 jar=$2 extra_java_opts=${3:-}
  local pid_file="$PID_DIR/$name.pid"
  if [[ -f "$pid_file" ]] && kill -0 "$(cat "$pid_file")" 2>/dev/null; then
    echo "[$name] 已在运行 pid=$(cat "$pid_file")"
    return
  fi
  echo "[$name] 启动中..."
  nohup java $extra_java_opts -jar "$jar" >> "$LOG_DIR/$name.log" 2>&1 &
  echo $! > "$pid_file"
  echo "[$name] pid=$(cat "$pid_file") 日志: $LOG_DIR/$name.log"
}

start_one icoolkj-admin-api "$API_JAR" "-Xms512m -Xmx1024m"
sleep 3
start_one icoolkj-admin-worker "$WORKER_JAR" "-Xms256m -Xmx512m"

echo "完成。API 健康检查: curl -s http://127.0.0.1:8080/api/actuator/health"
