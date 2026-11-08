# ICOOLKJ 前后端分离 · Linux 宿主机完整部署

> 适用：Anolis / Alibaba Cloud Linux x86_64 · MySQL 8.4 + Redis + JDK 21 + Nginx HTTPS  
> 正式域名示例：`https://platform.example.com`  
> 配套：每日 02:00 正式库全量覆盖演示库  
> 对照代码配置：[`application-icoolkj-shared*.yml`](../backend/icoolkj-admin-common/src/main/resources/) · [`deploy/host/`](../deploy/host/)

本文是**手工二进制/源码部署**运维手册。Docker 方案见 [deploy/README.md](../deploy/README.md)；目录约定见 [architecture/10-deployment.md](architecture/10-deployment.md)。

---

## 一、环境总览

| 项 | 选型 |
|----|------|
| OS | Linux x86_64（Anolis / Alibaba Cloud Linux） |
| 数据库 | MySQL **8.4.10** 官方二进制（`lower_case_table_names=1`） |
| 缓存 | Redis 稳定版（二进制/源码，systemd） |
| JDK | **21** LTS（Eclipse Temurin） |
| Web | Nginx 源码编译（SSL / 反代 / 静态） |
| 架构 | Vue 前端 + Spring Boot 后端（`context-path=/api`） |
| 正式域名 | `https://platform.example.com` |
| 自动化 | 每日 02:00 `icoolkj_platform` → 覆盖 `icoolkj_platform_dev` |

**与仓库一致的关键约定：**

| 项 | 约定 |
|----|------|
| 正式库 | `icoolkj_platform` · profile `prod` |
| 演示库 | `icoolkj_platform_dev` · profile `demo`（`icoolkj.demo.enabled=true`） |
| Redis 默认密码（共享 yml） | `test`（小写 `redis`；生产请改并用环境变量覆盖） |
| API 端口 | `8080`，路径前缀 `/api` |
| 前端 baseURL | `/api`（Nginx 反代，无需写死域名） |
| 必启进程 | **api + worker**（定时/异步任务在 worker） |

---

## 二、MySQL 8.4.10

### 2.1 目录

| 用途 | 路径 |
|------|------|
| 安装 | `/opt/software/mysql-8.4.10-linux-glibc2.28-x86_64` |
| 数据 | `/data/mysql` |
| 配置 | `/etc/my.cnf` |

### 2.2 `my.cnf`（须在初始化前配置，`lower_case_table_names` 不可事后改）

```ini
[mysqld]
lower_case_table_names=1
user=mysql
port=3306
basedir=/opt/software/mysql-8.4.10-linux-glibc2.28-x86_64
datadir=/data/mysql
socket=/tmp/mysql.sock
pid-file=/data/mysql/ICOOLKJ.pid
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
log_error=/data/mysql/mysql-error.log
slow_query_log=1
long_query_time=2

[client]
socket=/tmp/mysql.sock
default-character-set=utf8mb4
```

### 2.3 初始化与库

1. 初始化数据目录后启动 mysqld。  
2. 用 `skip-grant-tables` 等方式重置 `root` 密码（自定强密码）。  
3. 建库：

```sql
CREATE DATABASE icoolkj_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE icoolkj_platform_dev DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

表结构由 API 首次启动 **Flyway V1–V4** 自动执行，勿手搓 DDL（见 [backend/database/README.md](../backend/database/README.md)）。

---

## 三、Redis

### 3.1 依赖与目录

```bash
yum install -y gcc gcc-c++ make
```

| 用途 | 路径 |
|------|------|
| 安装 | `/usr/local/redis` |
| 配置 | `/usr/local/redis/redis.conf` |
| 数据 | `/data/redis` |

### 3.2 生产向核心配置

```conf
daemonize yes
bind 127.0.0.1
protected-mode yes
port 6379
requirepass test
appendonly yes
dir /data/redis
```

> 生产务必改密，并与下文 `REDIS_PASSWORD` / `env.prod` 一致。仓库默认示例为 `test`。

### 3.3 systemd：`/etc/systemd/system/redis.service`

```ini
[Unit]
Description=Redis Service
After=network.target

[Service]
Type=forking
ExecStart=/usr/local/redis/redis-server /usr/local/redis/redis.conf
ExecStop=/bin/kill -s TERM $MAINPID
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

```bash
systemctl daemon-reload
systemctl enable --now redis
redis-cli -a 'test' ping
```

勿把密码写进 `ExecStop` 命令行（进程列表可见）。

---

## 四、JDK 21（Temurin）

```bash
# 示例：清华镜像（版本号以镜像站为准）
wget https://mirrors.tuna.tsinghua.edu.cn/Adoptium/21/jdk/x64/linux/OpenJDK21U-jdk_x64_linux_hotspot_21.0.11_9.tar.gz
mkdir -p /usr/local/jdk
tar -zxf OpenJDK21U-jdk_x64_linux_hotspot_21.0.11_9.tar.gz -C /usr/local/jdk
ln -sfn /usr/local/jdk/jdk-21.0.11+9 /usr/local/jdk/jdk21
```

`/etc/profile`：

```bash
export JAVA_HOME=/usr/local/jdk/jdk21
export PATH=$JAVA_HOME/bin:$PATH
```

```bash
source /etc/profile
java -version
```

---

## 五、Nginx（源码编译 + SSL）

建议模块：`http_ssl_module`、`http_gzip_static_module`、`http_stub_status_module`。

| 用途 | 路径 |
|------|------|
| 安装 | `/usr/local/nginx` |
| 站点 | `/usr/local/nginx/conf/vhost/` |
| 证书 | `/usr/local/nginx/conf/ssl/` |

`nginx.conf` 的 `http {}` 内：

```nginx
include vhost/*.conf;
```

证书可用 [acme.sh](https://github.com/acmesh-official/acme.sh)：

```bash
curl https://get.acme.sh | sh
```

---

## 六、项目目录与打包产物

### 6.1 服务器目录（与本文一致）

```text
/opt/project/
├── frontend/          # Vite dist 静态资源
├── backend/           # api / worker jar + env
│   ├── icoolkj-admin-api-1.0.0-SNAPSHOT.jar
│   ├── icoolkj-admin-worker-1.0.0-SNAPSHOT.jar
│   └── env.prod       # 环境变量（勿提交 Git）
└── logs/
```

也可沿用仓库脚本默认目录 `/data/icoolkj-platform/`（见 [deploy/host/README.md](../deploy/host/README.md)），路径统一即可。

### 6.2 本机打包

```bash
# 后端
cd backend
mvn clean package -DskipTests
# 产出：
#   icoolkj-admin-api/target/icoolkj-admin-api-1.0.0-SNAPSHOT.jar
#   icoolkj-admin-worker/target/icoolkj-admin-worker-1.0.0-SNAPSHOT.jar

# 前端
cd frontend
npm ci && npx vite build
# 产出：frontend/dist/  → 上传到 /opt/project/frontend/
```

### 6.3 正式环境变量：`/opt/project/backend/env.prod`

参考 [deploy/host/env.prod.example](../deploy/host/env.prod.example)：

```bash
export SPRING_PROFILES_ACTIVE=prod

export DB_URL="jdbc:mysql://127.0.0.1:3306/icoolkj_platform?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
export DB_USERNAME=root
export DB_PASSWORD='你的MySQL密码'

export REDIS_HOST=127.0.0.1
export REDIS_PORT=6379
export REDIS_PASSWORD='你的Redis密码'

export JWT_SECRET='至少32字节的随机串'

# 文件存储按实际选型（本地或 MinIO）
# export ICOOLKJ_STORAGE_PROVIDER=minio
# ...
```

演示实例另用 `SPRING_PROFILES_ACTIVE=demo`，并把 `DB_URL` 指到 `icoolkj_platform_dev`。

### 6.4 systemd：API

`/etc/systemd/system/icoolkj-admin-api.service`：

```ini
[Unit]
Description=icoolkj-admin-api
After=network.target mysqld.service redis.service

[Service]
Type=simple
User=root
EnvironmentFile=/opt/project/backend/env.prod
WorkingDirectory=/opt/project/backend
ExecStart=/usr/local/jdk/jdk21/bin/java -Xms512m -Xmx1024m -jar /opt/project/backend/icoolkj-admin-api-1.0.0-SNAPSHOT.jar
Restart=on-failure
RestartSec=5
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

### 6.5 systemd：Worker（必配）

`/etc/systemd/system/icoolkj-admin-worker.service`：

```ini
[Unit]
Description=icoolkj-admin-worker
After=network.target mysqld.service redis.service icoolkj-admin-api.service

[Service]
Type=simple
User=root
EnvironmentFile=/opt/project/backend/env.prod
WorkingDirectory=/opt/project/backend
ExecStart=/usr/local/jdk/jdk21/bin/java -Xms256m -Xmx512m -jar /opt/project/backend/icoolkj-admin-worker-1.0.0-SNAPSHOT.jar
Restart=on-failure
RestartSec=5
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

```bash
systemctl daemon-reload
systemctl enable --now icoolkj-admin-api icoolkj-admin-worker
journalctl -u icoolkj-admin-api -f
```

---

## 七、Nginx HTTPS（`platform.example.com`）

文件：`/usr/local/nginx/conf/vhost/icoolkj.conf`

```nginx
# HTTP → HTTPS
server {
    listen 80 default_server;
    server_name platform.example.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl default_server;
    server_name platform.example.com;

    # 证书文件名以实际签发为准（可与域名不一致，但须匹配证书 CN/SAN）
    ssl_certificate     /usr/local/nginx/conf/ssl/platform.example.com.crt;
    ssl_certificate_key /usr/local/nginx/conf/ssl/platform.example.com.key;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:HIGH:!aNULL:!MD5:!RC4:!DHE;
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    root /opt/project/frontend;
    index index.html;
    charset utf-8;

    client_max_body_size 100m;

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }

    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff2?)$ {
        expires 7d;
        access_log off;
    }

    access_log /opt/project/logs/icoolkj-access.log;
    error_log  /opt/project/logs/icoolkj-error.log;
}
```

```bash
nginx -t && nginx -s reload
```

仓库参考配置：[deploy/host/nginx-icoolkj.conf](../deploy/host/nginx-icoolkj.conf)（可据此扩展 HTTPS）。

---

## 八、演示库每日覆盖同步

目标：每天 **02:00** 用正式库 `icoolkj_platform` **全量覆盖** `icoolkj_platform_dev`。

### 8.1 脚本 `/opt/script/sync_platform_dev.sh`

```bash
#!/bin/bash
set -euo pipefail

DB_USER="root"
DB_PASS='你的MySQL密码'
DB_HOST="127.0.0.1"
DB_PROD="icoolkj_platform"
DB_DEV="icoolkj_platform_dev"

MYSQL_BIN="/opt/software/mysql-8.4.10-linux-glibc2.28-x86_64/bin"
TMP_SQL="/opt/script/platform_prod_dump.sql"
LOG_FILE="/opt/script/sync_dev_log.log"

echo "==================== $(date '+%Y-%m-%d %H:%M:%S') 开始同步 ====================" >> "$LOG_FILE"

"$MYSQL_BIN/mysqldump" -u"$DB_USER" -p"$DB_PASS" -h"$DB_HOST" \
  --single-transaction --routines --triggers --set-gtid-purged=OFF \
  "$DB_PROD" > "$TMP_SQL"

"$MYSQL_BIN/mysql" -u"$DB_USER" -p"$DB_PASS" -h"$DB_HOST" <<EOF
DROP DATABASE IF EXISTS ${DB_DEV};
CREATE DATABASE ${DB_DEV} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOF

"$MYSQL_BIN/mysql" -u"$DB_USER" -p"$DB_PASS" -h"$DB_HOST" "$DB_DEV" < "$TMP_SQL"
echo "✅ 同步完成：${DB_PROD} → ${DB_DEV}" >> "$LOG_FILE"

# 可选：同步后清演示 Redis（避免会话/缓存串库）
# redis-cli -a "$REDIS_PASS" FLUSHDB
```

```bash
chmod +x /opt/script/sync_platform_dev.sh
# 若从 Windows 拷贝：dos2unix /opt/script/sync_platform_dev.sh
bash /opt/script/sync_platform_dev.sh
```

### 8.2 crontab

```cron
0 2 * * * /opt/script/sync_platform_dev.sh
```

> 覆盖同步会清空演示库全部数据。演示实例若正在写库，同步瞬间会断连，属预期。

---

## 九、防火墙 / 安全组

```bash
firewall-cmd --add-port=80/tcp --permanent
firewall-cmd --add-port=443/tcp --permanent
firewall-cmd --reload
```

云安全组放行 **80 / 443**。`3306`、`6379`、`8080` **仅本机**，勿对公网开放。

---

## 十、访问与维护

| 项 | 地址 / 命令 |
|----|-------------|
| 正式站 | `https://platform.example.com` |
| API | Nginx `/api/` → `127.0.0.1:8080/api/` |
| 后端 | `systemctl start\|stop\|restart\|status icoolkj-admin-api` |
| Worker | `systemctl … icoolkj-admin-worker` |
| 日志 | `journalctl -u icoolkj-admin-api -f` |
| Nginx | `nginx -t && nginx -s reload` |
| Redis | `systemctl status redis` · `redis-cli -a '…' ping` |
| 同步日志 | `tail -f /opt/script/sync_dev_log.log` |

---

## 十一、常见问题

| 现象 | 排查 |
|------|------|
| Nginx 默认页 | 未 `include vhost`、无 `default_server`、`server_name` 不匹配 |
| 接口 404 | `proxy_pass` 尾斜杠；后端 `context-path` 必须是 `/api` |
| 502 | jar 未起、8080 未监听、`env.prod` 未加载 |
| 登录/缓存异常 | Redis 未起或 `REDIS_PASSWORD` 与 `requirepass` 不一致 |
| Flyway 失败 | 库字符集、账号权限、是否误用手搓表 |
| 脚本 `\r` 错误 | `dos2unix` 处理 shell |
| 定时任务不跑 | **未部署 worker** |

---

## 十二、软件下载（版本按需核对）

| 组件 | 地址 |
|------|------|
| Nginx | https://nginx.org/download/nginx-1.26.2.tar.gz |
| JDK 21 Temurin | https://mirrors.tuna.tsinghua.edu.cn/Adoptium/21/ |
| MySQL 8.4 | https://dev.mysql.com/downloads/mysql/8.4.html |
| Redis | https://download.redis.io/releases/ |
| acme.sh | https://github.com/acmesh-official/acme.sh |

```bash
wget https://nginx.org/download/nginx-1.26.2.tar.gz
wget https://mirrors.tuna.tsinghua.edu.cn/Adoptium/21/jdk/x64/linux/OpenJDK21U-jdk_x64_linux_hotspot_21.0.11_9.tar.gz
curl https://get.acme.sh | sh
```

---

## 十三、与仓库其它部署文档关系

| 文档 | 场景 |
|------|------|
| **本文** | 生产机手工装 MySQL/Redis/JDK/Nginx + jar 直跑 + 演示库同步 |
| [deploy/host/README.md](../deploy/host/README.md) | 精简 jar+dist 启动脚本 |
| [deploy/README.md](../deploy/README.md) | Docker / 私有镜像仓库 |
| [architecture/10-deployment.md](architecture/10-deployment.md) | 组件与端口约定 |
| [configuration.md](configuration.md) | Profile 与环境变量 |

---

## 十四、测试账号

登录页需填 **租户编码（tenantCode）+ 用户名 + 密码**。

### 14.1 基线账号（应用首次启动自动就绪）

统一初始密码：`StudyOnly@2026`。  
默认首次登录会要求改密。

| 用户名 | tenantCode | 说明 |
|--------|------------|------|
| `admin` | `platform` | 平台超级管理员（全菜单，含 PLATFORM） |
| `tenant_admin` | `default` | 默认**企业**租户管理员 |
| `org_admin` | `demo_org` | 演示**单位**租户管理员 |

验证建议：

1. `platform` / `admin` → 能进「工作流」「平台审批模板」等平台菜单  
2. `default` / `tenant_admin` → 在线办公、审批模板、流程监控；无平台引擎目录  
3. `demo_org` / `org_admin` → 单位租户视角

### 14.2 icoolkj有限公司 · 工作流联调账号

Flyway **V4** 已内置平台通用办事基础模板，以及 `tenantCode=icoolkj` 组织矩阵与 10 套完整工作流（enabled）。删库重建并启动 api 后即可登录，无需再导 seed。

账号一览见 V4 文末注释或前端 `/dev/test-accounts`；密码均为 `StudyOnly@2026`（仅隔离测试环境）。
| 项 | 值 |
|----|-----|
| tenantCode | `icoolkj` |
| 企业名称 | icoolkj有限公司 |
| 密码 | 均为 `StudyOnly@2026`（联调种子 `must_change_password=0`） |

组织含：总经办、研发（前/后端）、产品、人力、财务、行政、法务、IT、采购、仓储、销售。

| 用户名 | 姓名 | 角色/用途 | 部门 |
|--------|------|-----------|------|
| `icoolkj_admin` | 林志远 | 租户管理员（配置专用，不发起 starter_dept_role 类单） | 总经办 |
| `icoolkj_ceo` | 张伟 | 总经理 | 总经办 |
| `icoolkj_cc01` | 孙雨欣 | 抄送接收 | 总经办 |
| `icoolkj_fe_lead` | 刘晓彤 | 部门审批 | 前端组 |
| `icoolkj_fe01` | 李明轩 | 员工（常发起） | 前端组 |
| `icoolkj_hr_lead` | 周雅琴 | 人事审批 | 人力资源部 |
| `icoolkj_fin_lead` | 吴嘉慧 | 财务审批 | 财务部 |
| `icoolkj_adm_lead` | 郑立群 | 行政/用印 | 行政部 |
| `icoolkj_legal_lead` | 马文博 | 法务审批 | 法务部 |
| `icoolkj_it_lead` | 何志强 | IT 审批 | IT部 |
| `icoolkj_tech_dir` | 杨启明 | 技术总监 | IT部 |
| `icoolkj_pur_lead` | 冯晓薇 | 采购审批 | 采购部 |
| `icoolkj_wh_lead` | 曹国栋 | 仓储验收 | 仓储部 |
| `icoolkj_sales_lead` | 沈悦 | 销售主管 | 销售部 |
| `icoolkj_sales_mgr` | 韩立成 | 销售经理 | 销售部 |
| `icoolkj_sales01` | 朱明辉 | 销售（报价发起） | 销售部 |
| `icoolkj_pd01` | 钱雨桐 | 产品（合同发起） | 产品部 |

**10 套标准 BPMN**（发起目录）：费用报销、请假、出差、合同评审、入职、离职、采购、用印、IT 权限开通、销售报价。

**联调提示**：业务开单用 `icoolkj_fe01`（→ 部门 `icoolkj_fe_lead`），勿用 `icoolkj_admin`；报价用 `icoolkj_sales01`；抄送看 `icoolkj_cc01`。业务角色含 OA 附件能力码 `infra:file:list/upload/download`（无文件管理菜单页）。  
前端临时页（DEV/演示）：`/dev/test-accounts`（目录 `frontend/src/dev/test-accounts`，后期可整夹删除）。
### 14.3 安全提醒

- 上述账号**仅用于隔离测试 / 演示环境**；公网演示库每日被正式库覆盖时，账号状态会随正式库走。  
- **正式交付或公网暴露前必须改密**，并下线或轮换 `icoolkj` 等测试租户。  
- 勿把真实生产密码写进文档或提交到 Git。

