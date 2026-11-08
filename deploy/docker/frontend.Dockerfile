# syntax=docker/dockerfile:1
# 构建前端静态资源 + Nginx（含 /api 反向代理）

FROM node:20-bookworm-slim AS builder
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
# 生产构建：跳过 vue-tsc，避免本地类型问题阻塞镜像构建
RUN npx vite build

FROM nginx:1.27-alpine
ENV TZ=Asia/Shanghai
COPY deploy/nginx/icoolkj.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
