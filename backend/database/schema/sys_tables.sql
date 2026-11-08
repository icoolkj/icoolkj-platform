-- =============================================================================
-- iCoolKJ Platform - System Tables DDL (V2.2)
-- Copyright (c) icoolkj. See LICENSE.txt.
-- Author: icoolkj | Email: icoolkj@163.com
-- Version: V2.2
-- Tables: 17
-- Changes: unique keys include deleted; user security fields; menu permission_code uk
-- =============================================================================

-- 14.1 sys_tenant 租户表
create table sys_tenant (
  id varchar(20) not null comment '租户ID',
  tenant_code varchar(50) not null comment '租户编码',
  tenant_name varchar(100) not null comment '租户名称',
  contact_name varchar(50) null comment '联系人',
  contact_phone varchar(20) null comment '联系电话',
  contact_email varchar(100) null comment '联系邮箱',
  license_no varchar(100) null comment '营业执照号',
  expire_at datetime(3) null comment '到期时间',
  account_limit int null comment '账号上限',
  status char(1) not null default '0' comment '状态 0正常 1停用',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_sys_tenant_tenant_code (tenant_code, deleted),
  key idx_sys_tenant_status (status),
  key idx_sys_tenant_expire_at (expire_at)
) engine=innodb default charset=utf8mb4 comment='租户表';

-- 14.2 sys_user 用户表
create table sys_user (
  id varchar(20) not null comment '用户ID',
  tenant_id varchar(20) not null comment '租户ID',
  dept_id varchar(20) null comment '部门ID',
  username varchar(50) not null comment '登录账号',
  nickname varchar(50) not null comment '用户昵称',
  real_name varchar(50) null comment '真实姓名',
  user_type varchar(20) not null default 'system' comment '用户类型 system/platform',
  email varchar(100) null comment '邮箱',
  phone varchar(20) null comment '手机号',
  gender char(1) not null default '2' comment '性别 0男 1女 2未知',
  avatar_url varchar(255) null comment '头像地址(中图)',
  avatar_url_sm varchar(255) null comment '头像小图地址',
  avatar_url_lg varchar(255) null comment '头像大图地址',
  avatar_file_id varchar(20) null comment '头像主文件ID(infra_file)',
  password_hash varchar(100) not null comment '密码哈希',
  status char(1) not null default '0' comment '状态 0正常 1停用',
  is_super_admin tinyint(1) not null default 0 comment '是否超级管理员',
  login_fail_count int not null default 0 comment '连续登录失败次数',
  locked_until datetime(3) null comment '锁定截止时间',
  last_login_ip varchar(64) null comment '最后登录IP',
  last_login_at datetime(3) null comment '最后登录时间',
  pwd_updated_at datetime(3) null comment '密码更新时间',
  must_change_password tinyint(1) not null default 0 comment '是否必须修改密码',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_sys_user_tenant_username (tenant_id, username, deleted),
  key idx_sys_user_tenant_id (tenant_id),
  key idx_sys_user_dept_id (dept_id),
  key idx_sys_user_status (status),
  key idx_sys_user_locked_until (locked_until),
  key idx_sys_user_created_at (created_at)
) engine=innodb default charset=utf8mb4 comment='用户表';

-- 14.3 sys_role 角色表
create table sys_role (
  id varchar(20) not null comment '角色ID',
  tenant_id varchar(20) not null comment '租户ID',
  role_name varchar(50) not null comment '角色名称',
  role_code varchar(50) not null comment '角色编码',
  data_scope char(1) not null default '1' comment '数据范围 1全部 2本部门及以下 3本部门 4本人 5自定义',
  sort_no int not null default 0 comment '显示顺序',
  status char(1) not null default '0' comment '状态 0正常 1停用',
  is_super_role tinyint(1) not null default 0 comment '是否超级管理员角色',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_sys_role_tenant_role_code (tenant_id, role_code, deleted),
  key idx_sys_role_tenant_id (tenant_id),
  key idx_sys_role_status (status)
) engine=innodb default charset=utf8mb4 comment='角色表';

-- 14.4 sys_user_role 用户角色关联表
create table sys_user_role (
  tenant_id varchar(20) not null comment '租户ID',
  user_id varchar(20) not null comment '用户ID',
  role_id varchar(20) not null comment '角色ID',
  primary key (tenant_id, user_id, role_id),
  key idx_sys_user_role_user_id (user_id),
  key idx_sys_user_role_role_id (role_id)
) engine=innodb default charset=utf8mb4 comment='用户角色关联表';

-- 14.5 sys_menu 菜单权限资源表
create table sys_menu (
  id varchar(20) not null comment '资源ID',
  parent_id varchar(20) not null default '0' comment '父资源ID',
  menu_type char(1) not null comment '资源类型 M目录 C菜单 F按钮',
  menu_name varchar(50) not null comment '资源名称',
  name_i18n json null comment '菜单多语言名称 {"zh-CN":"...","en-US":"..."}',
  route_name varchar(100) null comment '路由名称',
  route_path varchar(200) null comment '路由地址',
  component_path varchar(200) null comment '组件路径',
  permission_code varchar(100) null comment '权限标识',
  icon varchar(100) null comment '菜单图标',
  sort_no int not null default 0 comment '显示顺序',
  visible char(1) not null default '0' comment '显示状态 0显示 1隐藏',
  status char(1) not null default '0' comment '状态 0正常 1停用',
  is_external tinyint(1) not null default 0 comment '是否外链',
  is_cache tinyint(1) not null default 1 comment '是否缓存',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_sys_menu_permission_scope (permission_code, assign_scope, deleted),
  key idx_sys_menu_parent_id (parent_id),
  key idx_sys_menu_menu_type (menu_type),
  key idx_sys_menu_status (status)
) engine=innodb default charset=utf8mb4 comment='菜单权限资源表';

-- 14.6 sys_role_menu 角色资源关联表
create table sys_role_menu (
  tenant_id varchar(20) not null comment '租户ID',
  role_id varchar(20) not null comment '角色ID',
  menu_id varchar(20) not null comment '资源ID',
  primary key (tenant_id, role_id, menu_id),
  key idx_sys_role_menu_role_id (role_id),
  key idx_sys_role_menu_menu_id (menu_id)
) engine=innodb default charset=utf8mb4 comment='角色资源关联表';

-- 14.7 sys_dept 部门表
create table sys_dept (
  id varchar(20) not null comment '部门ID',
  tenant_id varchar(20) not null comment '租户ID',
  parent_id varchar(20) not null default '0' comment '父部门ID',
  ancestors varchar(1000) not null default '0' comment '祖级部门ID链，逗号分隔',
  dept_name varchar(100) not null comment '部门名称',
  dept_code varchar(50) null comment '部门编码',
  leader_name varchar(50) null comment '负责人',
  phone varchar(20) null comment '联系电话',
  email varchar(100) null comment '邮箱',
  sort_no int not null default 0 comment '显示顺序',
  status char(1) not null default '0' comment '状态 0正常 1停用',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_sys_dept_tenant_dept_code (tenant_id, dept_code, deleted),
  key idx_sys_dept_tenant_id (tenant_id),
  key idx_sys_dept_tenant_parent_id (tenant_id, parent_id),
  key idx_sys_dept_tenant_ancestors (tenant_id, ancestors(255)),
  key idx_sys_dept_status (status)
) engine=innodb default charset=utf8mb4 comment='部门表';

-- 14.8 sys_post 岗位表
create table sys_post (
  id varchar(20) not null comment '岗位ID',
  tenant_id varchar(20) not null comment '租户ID',
  post_name varchar(50) not null comment '岗位名称',
  post_code varchar(50) not null comment '岗位编码',
  sort_no int not null default 0 comment '显示顺序',
  status char(1) not null default '0' comment '状态 0正常 1停用',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_sys_post_tenant_post_code (tenant_id, post_code, deleted),
  key idx_sys_post_tenant_id (tenant_id),
  key idx_sys_post_status (status)
) engine=innodb default charset=utf8mb4 comment='岗位表';

-- 14.9 sys_user_post 用户岗位关联表
create table sys_user_post (
  tenant_id varchar(20) not null comment '租户ID',
  user_id varchar(20) not null comment '用户ID',
  post_id varchar(20) not null comment '岗位ID',
  primary key (tenant_id, user_id, post_id),
  key idx_sys_user_post_user_id (user_id),
  key idx_sys_user_post_post_id (post_id)
) engine=innodb default charset=utf8mb4 comment='用户岗位关联表';

-- 14.10 sys_role_dept 角色部门数据权限表
create table sys_role_dept (
  tenant_id varchar(20) not null comment '租户ID',
  role_id varchar(20) not null comment '角色ID',
  dept_id varchar(20) not null comment '部门ID',
  primary key (tenant_id, role_id, dept_id),
  key idx_sys_role_dept_role_id (role_id),
  key idx_sys_role_dept_dept_id (dept_id)
) engine=innodb default charset=utf8mb4 comment='角色部门数据权限表';

-- 14.11 sys_dict_type 字典类型表
create table sys_dict_type (
  id varchar(20) not null comment '字典类型ID',
  tenant_id varchar(20) not null default '0' comment '租户ID，0表示平台级',
  dict_name varchar(100) not null comment '字典名称',
  dict_type varchar(100) not null comment '字典类型',
  status char(1) not null default '0' comment '状态 0正常 1停用',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_sys_dict_type_tenant_dict_type (tenant_id, dict_type, deleted),
  key idx_sys_dict_type_tenant_id (tenant_id),
  key idx_sys_dict_type_status (status)
) engine=innodb default charset=utf8mb4 comment='字典类型表';

-- 14.12 sys_dict_item 字典数据项表
create table sys_dict_item (
  id varchar(20) not null comment '字典项ID',
  tenant_id varchar(20) not null default '0' comment '租户ID，0表示平台级',
  dict_type_id varchar(20) not null comment '字典类型ID',
  dict_label varchar(100) not null comment '字典标签',
  dict_value varchar(100) not null comment '字典值',
  value_type varchar(20) not null default 'string' comment '值类型',
  css_class varchar(100) null comment '可选 CSS 类名',
  icon varchar(64) null comment 'Element Plus 图标 PascalCase 名（与 sys_menu.icon 约定一致）',
  list_class varchar(100) null comment '表格回显样式（Tag 色调）',
  is_default tinyint(1) not null default 0 comment '是否默认',
  sort_no int not null default 0 comment '显示顺序',
  status char(1) not null default '0' comment '状态 0正常 1停用',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  key idx_sys_dict_item_tenant_id (tenant_id),
  key idx_sys_dict_item_dict_type_id (dict_type_id),
  key idx_sys_dict_item_status (status)
) engine=innodb default charset=utf8mb4 comment='字典数据项表';

-- 14.13 sys_config 系统参数表
create table sys_config (
  id varchar(20) not null comment '参数ID',
  tenant_id varchar(20) not null default '0' comment '租户ID，0表示平台级',
  config_name varchar(100) not null comment '参数名称',
  config_key varchar(100) not null comment '参数键名',
  config_value mediumtext not null comment '参数键值（支持 JSON 大包）',
  config_type char(1) not null default 'N' comment '是否系统内置 Y是 N否',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_sys_config_tenant_config_key (tenant_id, config_key, deleted),
  key idx_sys_config_tenant_id (tenant_id)
) engine=innodb default charset=utf8mb4 comment='系统参数表';

-- 14.14 sys_notice 通知公告表
create table sys_notice (
  id varchar(20) not null comment '公告ID',
  tenant_id varchar(20) not null default '0' comment '租户ID，0表示平台公告',
  notice_title varchar(200) not null comment '公告标题',
  notice_type char(1) not null default '1' comment '公告类型 1通知 2公告 3警告',
  cover_file_id varchar(20) null comment '封面图文件ID(infra_file)',
  notice_content text null comment '公告内容(富文本HTML)',
  notice_summary varchar(60) not null comment '摘要',
  sort_no int not null default 99 comment '排序号(越小越靠前)',
  publish_locations varchar(100) null comment '发布位置(逗号分隔字典值)',
  approve_status varchar(20) not null default 'draft' comment '审批状态 draft/running/approved/rejected/cancelled/returned/closed',
  process_instance_id varchar(64) null comment 'Flowable流程实例ID',
  process_definition_key varchar(64) null comment '流程定义Key',
  starter_user_id varchar(20) null comment '发起人用户ID',
  submitted_at datetime(3) null comment '提交审批时间',
  finished_at datetime(3) null comment '审批结束时间',
  release_status varchar(20) not null default 'unpublished' comment '上架状态 unpublished/published（仅 approved 后可发布/下架）',
  published_at datetime(3) null comment '最近上架时间（发布时写入）',
  status char(1) not null default '0' comment '状态 0正常 1关闭',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  key idx_sys_notice_tenant_id (tenant_id),
  key idx_sys_notice_status (status),
  key idx_sys_notice_approve_status (approve_status),
  key idx_sys_notice_release_status (release_status),
  key idx_sys_notice_process_instance (process_instance_id),
  key idx_sys_notice_sort_no (sort_no),
  key idx_sys_notice_created_at (created_at)
) engine=innodb default charset=utf8mb4 comment='通知公告表';

-- 14.14.1 sys_banner 轮播图表
create table sys_banner (
  id varchar(20) not null comment '轮播图ID',
  tenant_id varchar(20) not null default '0' comment '租户ID，0表示平台轮播',
  banner_title varchar(200) not null comment '标题',
  image_file_id varchar(20) not null comment '图片文件ID(infra_file)',
  display_positions varchar(100) not null comment '展示位置(逗号分隔字典值)',
  position_config text null comment '各位置点击配置(JSON数组)',
  sort_no int not null default 99 comment '排序号(越小越靠前)',
  status char(1) not null default '0' comment '状态 0启用 1停用',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  key idx_sys_banner_tenant_id (tenant_id),
  key idx_sys_banner_status (status),
  key idx_sys_banner_sort_no (sort_no),
  key idx_sys_banner_created_at (created_at)
) engine=innodb default charset=utf8mb4 comment='轮播图表';

-- 14.15 sys_ip_access_rule IP访问规则表
create table sys_ip_access_rule (
  id varchar(20) not null comment '规则ID',
  tenant_id varchar(20) not null default '0' comment '租户ID，0表示平台级',
  rule_name varchar(100) not null comment '规则名称',
  ip_address varchar(255) not null comment 'IP地址或CIDR网段',
  match_type char(1) not null default '1' comment '匹配类型 1单IP 2IP段 3CIDR',
  rule_type char(1) not null comment '规则类型 1允许 2禁止',
  apply_scope char(1) not null default '1' comment '应用范围 1登录 2后台访问 3开放接口 4全部',
  priority_no int not null default 0 comment '优先级，值越大越优先',
  status char(1) not null default '0' comment '状态 0正常 1停用',
  start_at datetime(3) null comment '生效开始时间',
  end_at datetime(3) null comment '生效结束时间',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  key idx_sys_ip_access_rule_tenant_id (tenant_id),
  key idx_sys_ip_access_rule_status (status),
  key idx_sys_ip_access_rule_rule_type (rule_type),
  key idx_sys_ip_access_rule_apply_scope (apply_scope),
  key idx_sys_ip_access_rule_priority_no (priority_no)
) engine=innodb default charset=utf8mb4 comment='IP访问规则表';

-- 14.16 sys_login_log 登录日志表
create table sys_login_log (
  id varchar(20) not null comment '日志ID',
  tenant_id varchar(20) not null comment '租户ID',
  username varchar(50) not null comment '登录账号',
  login_type varchar(20) not null default 'password' comment '登录方式',
  login_status char(1) not null comment '登录状态 0成功 1失败',
  message varchar(500) null comment '提示消息',
  ip varchar(64) null comment '登录IP',
  location varchar(255) null comment '登录地点',
  user_agent varchar(500) null comment '用户代理',
  browser varchar(100) null comment '浏览器',
  os varchar(100) null comment '操作系统',
  login_at datetime(3) not null default current_timestamp(3) comment '登录时间',
  primary key (id),
  key idx_sys_login_log_tenant_id (tenant_id),
  key idx_sys_login_log_username (username),
  key idx_sys_login_log_status (login_status),
  key idx_sys_login_log_login_at (login_at)
) engine=innodb default charset=utf8mb4 comment='登录日志表';

-- 14.17 sys_operation_log 操作日志表
create table sys_operation_log (
  id varchar(20) not null comment '日志ID',
  tenant_id varchar(20) not null comment '租户ID',
  trace_id varchar(64) null comment '链路ID',
  module_name varchar(100) null comment '模块名称',
  business_type varchar(50) null comment '业务类型',
  method varchar(255) null comment '方法名(类名.方法)',
  request_method varchar(20) null comment '请求方式',
  operator_id varchar(20) null comment '操作人ID',
  operator_name varchar(50) null comment '操作人',
  operation_url varchar(255) null comment '请求URL',
  operation_ip varchar(64) null comment '请求IP',
  operation_location varchar(255) null comment '操作地点',
  request_param text null comment '脱敏后请求参数',
  response_result text null comment '脱敏后返回结果',
  operation_status char(1) not null default '0' comment '操作状态 0成功 1失败',
  error_message text null comment '错误消息',
  cost_time int null comment '耗时毫秒',
  operation_at datetime(3) not null default current_timestamp(3) comment '操作时间',
  primary key (id),
  key idx_sys_operation_log_tenant_id (tenant_id),
  key idx_sys_operation_log_operator_id (operator_id),
  key idx_sys_operation_log_operation_status (operation_status),
  key idx_sys_operation_log_operation_at (operation_at)
) engine=innodb default charset=utf8mb4 comment='操作日志表';
