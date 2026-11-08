# syntax=docker/dockerfile:1
# 构建 API / Worker JAR（多阶段）
# 用法：docker build --target api -f deploy/docker/backend.Dockerfile .
#       docker build --target worker -f deploy/docker/backend.Dockerfile .

FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY backend/pom.xml backend/pom.xml
COPY backend/icoolkj-admin-common backend/icoolkj-admin-common
COPY backend/icoolkj-admin-framework backend/icoolkj-admin-framework
COPY backend/icoolkj-admin-system backend/icoolkj-admin-system
COPY backend/icoolkj-admin-infra backend/icoolkj-admin-infra
COPY backend/icoolkj-admin-biz backend/icoolkj-admin-biz
COPY backend/icoolkj-admin-api backend/icoolkj-admin-api
COPY backend/icoolkj-admin-worker backend/icoolkj-admin-worker
COPY backend/icoolkj-admin-generator backend/icoolkj-admin-generator
COPY backend/database backend/database
RUN mvn -f backend/pom.xml clean package -DskipTests -pl icoolkj-admin-api,icoolkj-admin-worker -am

FROM eclipse-temurin:17-jre-jammy AS api
ENV TZ=Asia/Shanghai
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=builder /build/backend/icoolkj-admin-api/target/icoolkj-admin-api-*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "/app/app.jar"]

FROM eclipse-temurin:17-jre-jammy AS worker
ENV TZ=Asia/Shanghai
WORKDIR /app
COPY --from=builder /build/backend/icoolkj-admin-worker/target/icoolkj-admin-worker-*.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "/app/app.jar"]
