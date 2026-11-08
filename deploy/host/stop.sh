#!/usr/bin/env bash
set -euo pipefail

APP_HOME="${APP_HOME:-/data/icoolkj-platform}"
PID_DIR="$APP_HOME/run"

stop_one() {
  local name=$1
  local pid_file="$PID_DIR/$name.pid"
  if [[ ! -f "$pid_file" ]]; then
    echo "[$name] 未运行"
    return
  fi
  local pid
  pid=$(cat "$pid_file")
  if kill -0 "$pid" 2>/dev/null; then
    kill "$pid"
    echo "[$name] 已停止 pid=$pid"
  fi
  rm -f "$pid_file"
}

stop_one icoolkj-admin-worker
stop_one icoolkj-admin-api
