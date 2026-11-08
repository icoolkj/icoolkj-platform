-- 清空库并重建（按需修改库名）
-- 用法: mysql -u root -p < backend/database/init/00_reset_database.sql

drop database if exists icoolkj_platform_dev;
create database icoolkj_platform_dev
  default character set utf8mb4
  collate utf8mb4_unicode_ci;

-- 若 application.yml 使用 icoolkj_platform，请同步修改库名：
-- drop database if exists icoolkj_platform;
-- create database icoolkj_platform ...
