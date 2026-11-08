-- =============================================================================
-- iCoolKJ Platform - Infrastructure Tables DDL (V2.2)
-- Copyright (c) icoolkj. See LICENSE.txt.
-- Author: icoolkj | Email: icoolkj@163.com
-- Version: V2.2
-- Tables: 6
-- Changes: log fields annotated as desensitized content
-- =============================================================================

-- 14.18 infra_file 文件表
create table infra_file (
  id varchar(20) not null comment '文件ID',
  tenant_id varchar(20) not null comment '租户ID',
  biz_type varchar(50) null comment '业务类型',
  biz_id varchar(20) null comment '业务ID',
  original_name varchar(255) not null comment '原始文件名',
  file_name varchar(255) not null comment '存储文件名',
  file_ext varchar(20) null comment '文件扩展名',
  content_type varchar(100) null comment '内容类型',
  file_size bigint not null default 0 comment '文件大小(字节)',
  storage_type varchar(20) not null default 'local' comment '存储类型',
  bucket_name varchar(100) not null comment '桶名称',
  object_key varchar(255) not null comment '对象Key',
  file_url varchar(500) null comment '访问地址(MinIO公网URL或/infra/file/{id}/download?inline=1)',
  md5 varchar(64) null comment '文件MD5',
  scan_status char(1) not null default '3' comment '扫描状态 0待扫 1清洁 2感染 3跳过',
  scan_engine varchar(50) null comment '扫描引擎',
  scan_at datetime(3) null comment '扫描时间',
  encrypted char(1) not null default '0' comment '应用层加密 0否 1是',
  sensitivity_level char(1) not null default '1' comment '密级 0公开 1内部 2秘密 3机密',
  user_id varchar(20) null comment '归属用户(上传人)',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint not null default 0 comment '逻辑删除 0否 1是',
  deleted_at datetime(3) null comment '逻辑删除时间',
  primary key (id),
  key idx_infra_file_tenant_id (tenant_id),
  key idx_infra_file_biz_type_biz_id (biz_type, biz_id),
  key idx_infra_file_user_id (user_id),
  key idx_infra_file_created_at (created_at),
  key idx_infra_file_deleted (deleted),
  key idx_infra_file_deleted_at (deleted, deleted_at)
) engine=innodb default charset=utf8mb4 comment='文件表';

-- 14.18.1 infra_file_ref 文件业务引用表
create table infra_file_ref (
  id varchar(20) not null comment '引用ID',
  tenant_id varchar(20) not null comment '租户ID',
  file_id varchar(20) not null comment '文件ID',
  biz_type varchar(50) not null comment '业务类型',
  biz_id varchar(20) not null comment '业务ID',
  ref_field varchar(100) null comment '业务字段/用途',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  deleted tinyint not null default 0 comment '逻辑删除 0否 1是',
  primary key (id),
  key idx_infra_file_ref_tenant_id (tenant_id),
  key idx_infra_file_ref_file_id (file_id),
  key idx_infra_file_ref_biz (biz_type, biz_id),
  key idx_infra_file_ref_deleted (deleted)
) engine=innodb default charset=utf8mb4 comment='文件业务引用表';

-- 14.19 infra_async_task 异步任务表
create table infra_async_task (
  id varchar(20) not null comment '任务ID',
  tenant_id varchar(20) not null comment '租户ID',
  task_type varchar(50) not null comment '任务类型',
  task_name varchar(100) not null comment '任务名称',
  biz_type varchar(50) null comment '业务类型',
  biz_id varchar(20) null comment '业务ID',
  task_status char(1) not null default '0' comment '任务状态 0待处理 1处理中 2成功 3失败 4取消',
  request_param text null comment '请求参数',
  result_data text null comment '结果数据',
  error_message text null comment '错误信息',
  retry_count int not null default 0 comment '重试次数',
  max_retry_count int not null default 3 comment '最大重试次数',
  started_at datetime(3) null comment '开始时间',
  finished_at datetime(3) null comment '完成时间',
  user_id varchar(20) null comment '归属用户',
  locale varchar(16) null comment '提交者语言标签（异步导出 i18n）',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  key idx_infra_async_task_tenant_id (tenant_id),
  key idx_infra_async_task_task_type (task_type),
  key idx_infra_async_task_task_status (task_status),
  key idx_infra_async_task_owner (tenant_id, user_id),
  key idx_infra_async_task_created_at (created_at)
) engine=innodb default charset=utf8mb4 comment='异步任务表';

-- 14.20 infra_async_task_log 异步任务日志表
create table infra_async_task_log (
  id varchar(20) not null comment '日志ID',
  tenant_id varchar(20) not null comment '租户ID',
  task_id varchar(20) not null comment '任务ID',
  log_level varchar(20) not null default 'INFO' comment '日志级别',
  log_content text not null comment '日志内容',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  primary key (id),
  key idx_infra_async_task_log_tenant_id (tenant_id),
  key idx_infra_async_task_log_task_id (task_id),
  key idx_infra_async_task_log_created_at (created_at)
) engine=innodb default charset=utf8mb4 comment='异步任务日志表';

-- 14.21 infra_api_access_log 接口访问日志表
create table infra_api_access_log (
  id varchar(20) not null comment '日志ID',
  tenant_id varchar(20) not null comment '租户ID',
  trace_id varchar(64) null comment '链路ID',
  request_uri varchar(255) not null comment '请求URI',
  request_method varchar(20) not null comment '请求方法',
  client_ip varchar(64) null comment '客户端IP',
  user_id varchar(20) null comment '用户ID',
  username varchar(50) null comment '用户名',
  request_param text null comment '脱敏后请求参数',
  response_code int null comment '响应码',
  response_message varchar(255) null comment '响应消息',
  cost_time int null comment '耗时毫秒',
  accessed_at datetime(3) not null default current_timestamp(3) comment '访问时间',
  primary key (id),
  key idx_infra_api_access_log_tenant_id (tenant_id),
  key idx_infra_api_access_log_user_id (user_id),
  key idx_infra_api_access_log_request_uri (request_uri),
  key idx_infra_api_access_log_accessed_at (accessed_at)
) engine=innodb default charset=utf8mb4 comment='接口访问日志表';

-- 14.22 infra_thirdparty_call_log 第三方调用日志表
create table infra_thirdparty_call_log (
  id varchar(20) not null comment '日志ID',
  tenant_id varchar(20) not null comment '租户ID',
  provider_name varchar(100) not null comment '服务提供方',
  biz_type varchar(50) null comment '业务类型',
  request_url varchar(500) null comment '请求地址',
  request_method varchar(20) null comment '请求方法',
  request_data text null comment '脱敏后请求数据',
  response_data text null comment '脱敏后响应数据',
  response_status char(1) not null default '0' comment '调用状态 0成功 1失败',
  error_message text null comment '错误信息',
  cost_time int null comment '耗时毫秒',
  called_at datetime(3) not null default current_timestamp(3) comment '调用时间',
  primary key (id),
  key idx_infra_thirdparty_call_log_tenant_id (tenant_id),
  key idx_infra_thirdparty_call_log_provider_name (provider_name),
  key idx_infra_thirdparty_call_log_response_status (response_status),
  key idx_infra_thirdparty_call_log_called_at (called_at)
) engine=innodb default charset=utf8mb4 comment='第三方调用日志表';
