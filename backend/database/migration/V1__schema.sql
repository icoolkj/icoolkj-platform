-- V1: 全量表结构（含 i18n / 代码生成 / AI 审计 / 国标区划 / wf_task_action）

-- 空库基线；种子数据见 V2

set names utf8mb4;

-- ==========================================
-- iCoolKJ Platform V2.2 初始化脚本（空库基线）
-- Author: icoolkj
-- Domain: icoolkj.com
-- Version: V2.2 / Flyway V1
-- 含：全表 DDL、用户/角色/菜单（system+tool+infra+monitor）、字典、参数、IP 规则、公告
-- ==========================================
-- Initial password: StudyOnly@2026 (BCrypt; force-change gated by security.password.force-change-initial)

set names utf8mb4;

-- ==========================================
-- 1. 租户表
-- ==========================================
create table if not exists sys_tenant (
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
  tenant_type varchar(20) not null default 'ENTERPRISE' comment 'ENTERPRISE企业租户 ORGANIZATION单位租户',
  parent_tenant_id varchar(20) null comment '上级单位租户ID，企业租户必须为空',
  ancestors varchar(1000) not null default '0' comment '上级租户ID链',
  root_region_id varchar(20) null comment '租户服务根区划ID',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_sys_tenant_tenant_code (tenant_code, deleted),
  key idx_sys_tenant_status (status),
  key idx_sys_tenant_expire_at (expire_at),
  key idx_sys_tenant_root_region (root_region_id),
  key idx_sys_tenant_type (tenant_type),
  key idx_sys_tenant_parent (parent_tenant_id)
) engine=innodb default charset=utf8mb4 comment='租户表';

-- ==========================================
-- 1b. 行政区划表（国标 GB/T 2260，平台维护 tenant_id=0）
-- ==========================================
create table if not exists sys_region (
  id varchar(20) not null comment '区划ID',
  tenant_id varchar(20) not null default '0' comment '固定0=平台国标库',
  parent_id varchar(20) not null default '0' comment '父区划ID',
  ancestors varchar(1000) not null default '0' comment '祖级区划ID链，逗号分隔',
  region_code varchar(12) not null comment '国标行政区划代码',
  region_name varchar(100) not null comment '区划名称',
  region_level char(1) not null comment '0国家 1省 2市 3区县 4街道 5社区',
  sort_no int not null default 0 comment '显示顺序',
  status char(1) not null default '0' comment '状态 0正常 1停用',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_sys_region_code (region_code, deleted),
  key idx_sys_region_parent (parent_id),
  key idx_sys_region_level (region_level),
  key idx_sys_region_ancestors (ancestors(255))
) engine=innodb default charset=utf8mb4 comment='行政区划表';

-- ==========================================
-- 2. 用户表
-- ==========================================
create table if not exists sys_user (
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
  is_super_admin char(1) not null default '0' comment '是否超级管理员 0否 1是',
  login_fail_count int not null default 0 comment '连续登录失败次数',
  locked_until datetime(3) null comment '锁定截止时间',
  last_login_ip varchar(64) null comment '最后登录IP',
  last_login_at datetime(3) null comment '最后登录时间',
  pwd_updated_at datetime(3) null comment '密码更新时间',
  must_change_password char(1) not null default '0' comment '是否必须修改密码 0否 1是',
  mfa_enabled char(1) not null default '0' comment '是否启用 MFA 0否 1是',
  mfa_secret_enc varchar(512) null comment 'TOTP 密钥密文(AES-GCM)',
  mfa_bound_at datetime(3) null comment 'MFA 绑定时间',
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

create table if not exists sys_user_password_history (
  id varchar(20) not null comment '主键',
  tenant_id varchar(20) not null comment '租户ID',
  user_id varchar(20) not null comment '用户ID',
  password_hash varchar(100) not null comment '历史密码哈希',
  created_at datetime(3) not null default current_timestamp(3) comment '写入时间',
  primary key (id),
  key idx_sys_user_pwd_hist_user (tenant_id, user_id, created_at)
) engine=innodb default charset=utf8mb4 comment='用户密码历史（防重用）';

-- ==========================================
-- 3. 角色表
-- ==========================================
create table if not exists sys_role (
  id varchar(20) not null comment '角色ID',
  tenant_id varchar(20) not null comment '租户ID',
  role_name varchar(50) not null comment '角色名称',
  role_code varchar(50) not null comment '角色编码',
  data_scope char(1) not null default '1' comment '数据范围 1全部 2本部门及以下 3本部门 4本人 5自定义部门 6本区划及下级 7本区划 8自定义区划',
  sort_no int not null default 0 comment '显示顺序',
  status char(1) not null default '0' comment '状态 0正常 1停用',
  is_super_role char(1) not null default '0' comment '是否超级管理员角色 0否 1是',
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

-- ==========================================
-- 4. 用户角色关联表
-- ==========================================
create table if not exists sys_user_role (
  tenant_id varchar(20) not null comment '租户ID',
  user_id varchar(20) not null comment '用户ID',
  role_id varchar(20) not null comment '角色ID',
  primary key (tenant_id, user_id, role_id),
  key idx_sys_user_role_user_id (user_id),
  key idx_sys_user_role_role_id (role_id)
) engine=innodb default charset=utf8mb4 comment='用户角色关联表';

-- ==========================================
-- 5. 菜单权限资源表
-- ==========================================
create table if not exists sys_menu (
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
  is_external char(1) not null default '0' comment '是否外链 0否 1是',
  is_cache char(1) not null default '1' comment '是否缓存 0否 1是',
  remark varchar(500) null comment '备注',
  assign_scope varchar(20) not null default 'TENANT' comment '授权范围 PLATFORM/TENANT',
  product_code varchar(32) not null default 'platform' comment '产品宇宙编码 platform/oa/files/ai/业务码',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_sys_menu_permission_scope (permission_code, assign_scope, deleted),
  key idx_sys_menu_parent_id (parent_id),
  key idx_sys_menu_menu_type (menu_type),
  key idx_sys_menu_status (status),
  key idx_sys_menu_product_code (product_code)
) engine=innodb default charset=utf8mb4 comment='菜单权限资源表';

-- ==========================================
-- 5.1 产品宇宙目录（Console Shell 顶栏切换；业务宇宙 sort_no 小于平台底座）
-- ==========================================
create table if not exists sys_product (
  id varchar(20) not null comment '产品ID',
  code varchar(32) not null comment '产品编码 platform/oa/files/ai/geo…',
  name varchar(50) not null comment '产品名称',
  name_i18n json null comment '多语言名称 {"zh-CN":"...","en-US":"..."}',
  icon varchar(100) null comment '图标',
  sort_no int not null default 100 comment '显示顺序：业务宇宙宜 1–49，内置能力 50–89，platform 建议 90+',
  status char(1) not null default '0' comment '状态 0正常 1停用',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_sys_product_code (code, deleted),
  key idx_sys_product_sort (sort_no, status)
) engine=innodb default charset=utf8mb4 comment='产品宇宙目录';

-- ==========================================
-- 6. 角色资源关联表
-- ==========================================
create table if not exists sys_role_menu (
  tenant_id varchar(20) not null comment '租户ID',
  role_id varchar(20) not null comment '角色ID',
  menu_id varchar(20) not null comment '资源ID',
  primary key (tenant_id, role_id, menu_id),
  key idx_sys_role_menu_role_id (role_id),
  key idx_sys_role_menu_menu_id (menu_id)
) engine=innodb default charset=utf8mb4 comment='角色资源关联表';

-- ==========================================
-- 7. 部门表
-- ==========================================
create table if not exists sys_dept (
  id varchar(20) not null comment '部门ID',
  tenant_id varchar(20) not null comment '租户ID',
  parent_id varchar(20) not null default '0' comment '父部门ID',
  ancestors varchar(1000) not null default '0' comment '祖级部门ID链，逗号分隔',
  region_id varchar(20) null comment '归属区划ID',
  dept_type varchar(20) not null default 'INTERNAL' comment 'COMPETENT主管部门 INTERNAL内设机构',
  admin_level varchar(20) null comment 'PROVINCE/CITY/COUNTY/DISTRICT/STREET/NONE',
  competent_id varchar(20) null comment '内设机构所属主管部门ID',
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
  key idx_sys_dept_tenant_region_type (tenant_id, region_id, dept_type),
  key idx_sys_dept_tenant_competent (tenant_id, competent_id),
  key idx_sys_dept_status (status)
) engine=innodb default charset=utf8mb4 comment='部门表';

-- ==========================================
-- 8. 岗位表
-- ==========================================
create table if not exists sys_post (
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

-- ==========================================
-- 9. 用户岗位关联表
-- ==========================================
create table if not exists sys_user_post (
  tenant_id varchar(20) not null comment '租户ID',
  user_id varchar(20) not null comment '用户ID',
  post_id varchar(20) not null comment '岗位ID',
  primary key (tenant_id, user_id, post_id),
  key idx_sys_user_post_user_id (user_id),
  key idx_sys_user_post_post_id (post_id)
) engine=innodb default charset=utf8mb4 comment='用户岗位关联表';

-- ==========================================
-- 10. 角色部门数据权限表
-- ==========================================
create table if not exists sys_role_dept (
  tenant_id varchar(20) not null comment '租户ID',
  role_id varchar(20) not null comment '角色ID',
  dept_id varchar(20) not null comment '部门ID',
  primary key (tenant_id, role_id, dept_id),
  key idx_sys_role_dept_role_id (role_id),
  key idx_sys_role_dept_dept_id (dept_id)
) engine=innodb default charset=utf8mb4 comment='角色部门数据权限表';

-- ==========================================
-- 10b. 角色区划数据权限表
-- ==========================================
create table if not exists sys_role_region (
  tenant_id varchar(20) not null comment '租户ID',
  role_id varchar(20) not null comment '角色ID',
  region_id varchar(20) not null comment '区划ID',
  primary key (tenant_id, role_id, region_id),
  key idx_sys_role_region_role_id (role_id),
  key idx_sys_role_region_region_id (region_id)
) engine=innodb default charset=utf8mb4 comment='角色区划数据权限表';

-- ==========================================
-- 11. 字典类型表
-- ==========================================
create table if not exists sys_dict_type (
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

-- ==========================================
-- 12. 字典数据项表
-- ==========================================
create table if not exists sys_dict_item (
  id varchar(20) not null comment '字典项ID',
  tenant_id varchar(20) not null default '0' comment '租户ID，0表示平台级',
  dict_type_id varchar(20) not null comment '字典类型ID',
  dict_label varchar(100) not null comment '字典标签',
  dict_value varchar(100) not null comment '字典值',
  value_type varchar(20) not null default 'string' comment '值类型',
  css_class varchar(100) null comment '可选 CSS 类名',
  icon varchar(64) null comment 'Element Plus 图标 PascalCase 名（与 sys_menu.icon 约定一致）',
  list_class varchar(100) null comment '表格回显样式（Tag 色调）',
  is_default char(1) not null default '0' comment '是否默认 0否 1是',
  sort_no int not null default 0 comment '显示顺序',
  status char(1) not null default '0' comment '状态 0正常 1停用',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_sys_dict_item_tenant_type_value (tenant_id, dict_type_id, dict_value, deleted),
  key idx_sys_dict_item_tenant_id (tenant_id),
  key idx_sys_dict_item_dict_type_id (dict_type_id),
  key idx_sys_dict_item_status (status)
) engine=innodb default charset=utf8mb4 comment='字典数据项表';

-- ==========================================
-- 13. 系统参数表
-- ==========================================
create table if not exists sys_config (
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

-- ==========================================
-- 13.1 站点品牌 / OEM（一行一租户；0=平台默认）
-- ==========================================
create table if not exists sys_brand_pack (
  id varchar(20) not null comment '品牌包ID',
  tenant_id varchar(20) not null default '0' comment '租户ID，0表示平台默认；业务租户有行即整包覆盖',
  product_name varchar(64) not null default '' comment '产品名称（默认）',
  product_name_i18n json null comment 'locale→产品名',
  copyright_brand varchar(64) null comment '版权品牌',
  icp_no varchar(64) null comment '备案号',
  icp_url varchar(256) null comment '备案链接',
  domain varchar(128) null comment '站点域名',
  site_url varchar(256) null comment '站点 URL',
  email varchar(128) null comment '联系邮箱',
  contact_name varchar(64) null comment '联系人',
  contact_phone varchar(32) null comment '联系人电话',
  assets_json json null comment '品牌素材 profile_code→fileId（brand_logo_sm/md/lg、favicon、login_bg、qr_*）',
  labels_json json null comment '素材说明文案 profile_code→label（如二维码 caption）',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_sys_brand_pack_tenant (tenant_id, deleted),
  key idx_sys_brand_pack_tenant_id (tenant_id)
) engine=innodb default charset=utf8mb4 comment='站点品牌/OEM 表';

-- ==========================================
-- 14. 通知公告表
-- ==========================================
create table if not exists sys_notice (
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

-- ==========================================
-- 14.0 工作流应用层表
-- ==========================================
create table if not exists wf_model (
  id varchar(20) not null comment '模型ID',
  tenant_id varchar(20) not null default '0' comment '租户ID，0表示平台流程',
  model_key varchar(64) not null comment '模型标识(对应Flowable processDefinitionKey)',
  model_name varchar(100) not null comment '模型名称',
  category_name varchar(50) null comment '分类名称',
  biz_type varchar(64) not null comment '业务类型，如 sys_notice',
  form_type varchar(20) not null default 'system' comment '表单类型 system/route/custom',
  form_route varchar(200) null comment '审批表单路由',
  latest_version int not null default 0 comment '当前已发布版本号',
  status char(1) not null default '0' comment '状态 0正常 1停用',
  remark varchar(500) null comment '备注',
  process_config mediumtext null comment '流程全局配置JSON',
  source_model_id varchar(20) null comment '同步来源平台模型ID（租户副本）',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_wf_model_tenant_key (tenant_id, model_key, deleted),
  key idx_wf_model_biz_type (biz_type),
  key idx_wf_model_status (status),
  key idx_wf_model_source (source_model_id)
) engine=innodb default charset=utf8mb4 comment='流程模型表';

create table if not exists wf_model_version (
  id varchar(20) not null comment '版本ID',
  tenant_id varchar(20) not null default '0' comment '租户ID',
  model_id varchar(20) not null comment '模型ID',
  version_no int not null comment '版本号',
  bpmn_xml mediumtext not null comment 'BPMN XML',
  design_json mediumtext null comment '设计快照JSON(process_config.design 同构，版本不可变)',
  process_config_json mediumtext null comment '发布时全局配置快照(startAcl/behavior/notify，运行只读此列)',
  deployment_id varchar(64) null comment 'Flowable deploymentId',
  process_definition_id varchar(64) null comment 'Flowable processDefinitionId',
  process_definition_key varchar(64) null comment 'Flowable processDefinitionKey',
  published tinyint(1) not null default 0 comment '是否已发布到Flowable 1是0否',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_wf_model_version_no (model_id, version_no, deleted),
  key idx_wf_model_version_model (model_id),
  key idx_wf_model_version_published (published)
) engine=innodb default charset=utf8mb4 comment='流程模型版本表';

create table if not exists wf_biz_process (
  id varchar(20) not null comment '记录ID',
  tenant_id varchar(20) not null default '0' comment '租户ID',
  model_id varchar(20) not null comment '模型ID',
  model_version_no int null comment '发起时锁定的 wf_model_version.version_no',
  biz_type varchar(64) not null comment '业务类型',
  biz_id varchar(20) not null comment '业务主键',
  biz_title varchar(200) null comment '业务标题',
  process_definition_key varchar(64) not null comment '流程定义Key',
  process_instance_id varchar(64) not null comment 'Flowable流程实例ID',
  parent_instance_id varchar(64) null comment '父流程实例ID（callActivity 子流程）',
  status varchar(20) not null default 'running' comment 'running/approved/rejected/cancelled',
  starter_user_id varchar(20) not null comment '发起人',
  user_id varchar(20) not null comment '归属用户',
  dept_id varchar(20) null comment '归属部门',
  start_time datetime(3) not null comment '发起时间',
  end_time datetime(3) null comment '结束时间',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_wf_biz_process_instance (process_instance_id),
  key idx_wf_biz_process_biz (biz_type, biz_id),
  key idx_wf_biz_process_starter (starter_user_id, status),
  key idx_wf_biz_process_owner (tenant_id, user_id, status),
  key idx_wf_biz_process_dept (tenant_id, dept_id, status),
  key idx_wf_biz_process_tenant (tenant_id)
) engine=innodb default charset=utf8mb4 comment='业务流程实例关联表';

-- wf_biz_binding_policy 业务类型与流程绑定策略（平台级，按 biz_type 唯一）
create table if not exists wf_biz_binding_policy (
  id varchar(20) not null comment '策略ID',
  biz_type varchar(64) not null comment '业务类型，如 sys_notice',
  binding_mode varchar(32) not null default 'tenant_preferred' comment 'platform_mandatory=强制平台流程; tenant_preferred=租户模型优先',
  allow_tenant_approver_override char(1) not null default '1' comment 'platform_mandatory 时租户是否可覆盖审批人规则 1是0否',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_wf_biz_binding_biz_type (biz_type, deleted),
  key idx_wf_biz_binding_mode (binding_mode)
) engine=innodb default charset=utf8mb4 comment='工作流业务绑定策略';

-- wf_approver_rule 审批人规则（平台默认 tenant_id=0；租户可覆盖审批人，不可覆盖 platform_mandatory 流程定义）
create table if not exists wf_approver_rule (
  id varchar(20) not null comment '规则ID',
  tenant_id varchar(20) not null default '0' comment '0=平台默认规则；业务租户ID=租户覆盖',
  model_id varchar(20) not null comment '流程模型ID',
  task_definition_key varchar(64) not null comment 'BPMN 用户任务节点 id',
  variable_name varchar(64) not null comment '注入 Flowable 的流程变量名',
  strategy_type varchar(32) not null comment 'role / fixed_user',
  strategy_config varchar(1000) not null comment 'JSON，如 {"roleCodes":["dept_approver"]}',
  resolve_scope varchar(32) not null default 'biz_tenant' comment 'biz_tenant=业务租户内解析; platform_tenant=平台租户内解析',
  sort_no int not null default 0 comment '同节点多规则时的顺序',
  status char(1) not null default '0' comment '0正常 1停用',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_wf_approver_rule_scope (model_id, task_definition_key, tenant_id, deleted),
  key idx_wf_approver_rule_model (model_id),
  key idx_wf_approver_rule_tenant (tenant_id)
) engine=innodb default charset=utf8mb4 comment='工作流审批人规则';

create table if not exists wf_task_action (
  id varchar(20) not null comment '记录ID',
  tenant_id varchar(20) not null default '0' comment '租户ID',
  process_instance_id varchar(64) not null comment '流程实例ID',
  task_id varchar(64) null comment '任务ID',
  task_definition_key varchar(64) null comment '节点定义Key',
  action_type varchar(32) not null comment 'transfer/urge/reject_jump/timeout/add_sign_before/add_sign_after',
  from_user_id varchar(20) null comment '操作人/原办理人',
  to_user_id varchar(20) null comment '目标用户',
  comment varchar(500) null comment '意见',
  payload_json varchar(2000) null comment '扩展JSON',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  key idx_wf_task_action_pi (process_instance_id),
  key idx_wf_task_action_task (task_id),
  key idx_wf_task_action_tenant (tenant_id),
  key idx_wf_task_action_type (action_type)
) engine=innodb default charset=utf8mb4 comment='工作流任务动作审计';

create table if not exists wf_cc_receipt (
  id varchar(20) not null comment '记录ID',
  tenant_id varchar(20) not null default '0' comment '租户ID',
  process_instance_id varchar(64) not null comment '流程实例ID',
  task_id varchar(64) not null comment '抄送任务ID',
  user_id varchar(20) not null comment '抄送接收人',
  read_at datetime(3) null comment '已读时间',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_wf_cc_receipt_task_user (task_id, user_id, deleted),
  key idx_wf_cc_receipt_pi (process_instance_id),
  key idx_wf_cc_receipt_user (tenant_id, user_id)
) engine=innodb default charset=utf8mb4 comment='工作流抄送已读回执';

-- ==========================================
-- 14.0.1 轮播图表
-- ==========================================
create table if not exists sys_banner (
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

-- ==========================================
-- 14.1 站内信主表
-- ==========================================
create table if not exists sys_site_message (
  id varchar(20) not null comment '消息ID',
  tenant_id varchar(20) not null comment '租户ID',
  message_subject varchar(200) not null comment '主题',
  message_category varchar(20) not null comment '分类 system/business',
  message_content text not null comment '正文',
  sender_id varchar(20) not null comment '发送人ID',
  user_id varchar(20) not null comment '归属用户',
  sender_name varchar(50) null comment '发送人姓名',
  sent_at datetime(3) not null comment '发送时间',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  key idx_sys_site_message_tenant_id (tenant_id),
  key idx_sys_site_message_category (message_category),
  key idx_sys_site_message_sent_at (sent_at),
  key idx_sys_site_message_owner (tenant_id, user_id),
  key idx_sys_site_message_deleted (deleted)
) engine=innodb default charset=utf8mb4 comment='站内信主表';

-- ==========================================
-- 14.2 站内信收件表
-- ==========================================
create table if not exists sys_site_message_recipient (
  id varchar(20) not null comment '收件记录ID',
  tenant_id varchar(20) not null comment '租户ID',
  message_id varchar(20) not null comment '消息ID',
  user_id varchar(20) not null comment '收件人ID',
  read_at datetime(3) null comment '已读时间',
  deleted tinyint(1) not null default 0 comment '用户侧删除',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  primary key (id),
  unique key uk_sys_site_message_recipient_msg_user (tenant_id, message_id, user_id, deleted),
  key idx_sys_site_message_recipient_tenant_id (tenant_id),
  key idx_sys_site_message_recipient_message_id (message_id),
  key idx_sys_site_message_recipient_user_id (user_id),
  key idx_sys_site_message_recipient_read_at (read_at),
  key idx_sys_site_message_recipient_deleted (deleted)
) engine=innodb default charset=utf8mb4 comment='站内信收件表';

-- ==========================================
-- 15. IP访问规则表
-- ==========================================
create table if not exists sys_ip_access_rule (
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

-- ==========================================
-- 16. 登录日志表
-- ==========================================
create table if not exists sys_login_log (
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
  prev_hash varchar(64) null comment '审计链上一行哈希',
  row_hash varchar(64) null comment '本行完整性哈希',
  primary key (id),
  key idx_sys_login_log_tenant_id (tenant_id),
  key idx_sys_login_log_username (username),
  key idx_sys_login_log_status (login_status),
  key idx_sys_login_log_login_at (login_at)
) engine=innodb default charset=utf8mb4 comment='登录日志表';

-- ==========================================
-- 17. 操作日志表
-- ==========================================
create table if not exists sys_operation_log (
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
  prev_hash varchar(64) null comment '审计链上一行哈希',
  row_hash varchar(64) null comment '本行完整性哈希',
  primary key (id),
  key idx_sys_operation_log_tenant_id (tenant_id),
  key idx_sys_operation_log_operator_id (operator_id),
  key idx_sys_operation_log_operation_status (operation_status),
  key idx_sys_operation_log_operation_at (operation_at)
) engine=innodb default charset=utf8mb4 comment='操作日志表';

-- ==========================================
-- 17b. 平台定时任务
-- ==========================================
create table if not exists sys_job (
  id varchar(20) not null comment '主键',
  tenant_id varchar(20) not null default '0' comment '租户ID（平台运维=0）',
  job_code varchar(64) not null comment '任务编码（绑定 JobHandler）',
  job_name varchar(100) not null comment '任务名称',
  job_group varchar(64) not null default 'PLATFORM' comment '任务分组',
  cron_expression varchar(100) not null comment 'Cron 表达式',
  misfire_policy char(1) not null default '3' comment 'misfire：1立即 2一次 3放弃',
  allow_concurrent char(1) not null default '0' comment '是否允许并发：0禁止 1允许',
  status char(1) not null default '0' comment '状态：0正常 1暂停',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_sys_job_code (job_code, deleted),
  key idx_sys_job_tenant_status (tenant_id, status, deleted)
) engine=innodb default charset=utf8mb4 comment='平台定时任务定义';

create table if not exists sys_job_log (
  id varchar(20) not null comment '主键',
  tenant_id varchar(20) not null default '0' comment '租户ID',
  job_id varchar(20) not null comment '任务定义ID',
  job_code varchar(64) not null comment '任务编码',
  trigger_type varchar(16) not null comment '触发类型 CRON|MANUAL',
  trigger_user_id varchar(20) null comment '手动触发人（Cron为空）',
  status varchar(16) not null comment 'RUNNING|SUCCESS|FAILED',
  job_message varchar(500) null comment '摘要消息',
  exception_info text null comment '异常信息',
  request_param text null comment '请求/上下文 JSON',
  result_summary text null comment '结果摘要 JSON',
  started_at datetime(3) not null comment '开始时间',
  finished_at datetime(3) null comment '结束时间',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  primary key (id),
  key idx_sys_job_log_job_code (job_code, started_at),
  key idx_sys_job_log_job_id (job_id, started_at),
  key idx_sys_job_log_status (status, started_at),
  key idx_sys_job_log_trigger_user (trigger_user_id, started_at)
) engine=innodb default charset=utf8mb4 comment='平台定时任务执行日志';

-- ==========================================
-- 18. 文件表
-- ==========================================
create table if not exists infra_file (
  id varchar(20) not null comment '文件ID',
  tenant_id varchar(20) not null comment '租户ID',
  biz_type varchar(50) null comment '业务类型',
  biz_id varchar(20) null comment '业务ID',
  original_name varchar(255) not null comment '原始文件名',
  file_name varchar(255) not null comment '存储文件名',
  file_ext varchar(20) null comment '文件扩展名',
  content_type varchar(100) null comment '内容类型',
  file_size bigint not null default 0 comment '文件大小(字节)',
  storage_type varchar(20) not null default 'minio' comment '存储类型',
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

-- ==========================================
-- 18.1 文件引用表
-- ==========================================
create table if not exists infra_file_ref (
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

-- ==========================================
-- 18.2 文件访问策略（按 biz_type 模式；代码内置 + 表覆盖）
-- ==========================================
create table if not exists infra_file_access_policy (
  id varchar(20) not null comment '策略ID',
  biz_type_pattern varchar(64) not null comment '精确 biz_type 或前缀（如 brand_*）；* 为默认兜底',
  auth_mode varchar(32) not null comment 'SESSION|ANON|SESSION_OR_ANON',
  tenant_scope varchar(32) not null comment 'OWN|OWN_OR_PLATFORM（无隐式平台运营跨租户旁路）',
  visibility varchar(32) not null comment 'ALWAYS|CUSTOM（CUSTOM 走 FileAccessVisibilityChecker）',
  anon_purposes varchar(128) null comment '允许匿名访问的 purpose，逗号分隔（如 LOGIN_BRAND）',
  perm_any_of varchar(256) null comment '会话侧附加权限（任一即可），空表示不校验',
  sort_no int not null default 0 comment '同分时越小越优先；匹配主序仍为 pattern 最长优先',
  enabled char(1) not null default '1' comment '1启用 0停用',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_infra_file_access_policy_pattern (biz_type_pattern, deleted),
  key idx_infra_file_access_policy_enabled (enabled, deleted)
) engine=innodb default charset=utf8mb4 comment='文件访问策略表';

-- ==========================================
-- 18.3 文件上传配置（Profile = 槽位；Rule = 约束；Catalog = 场景编排）
-- ==========================================
create table if not exists infra_file_profile (
  id varchar(20) not null comment '配置ID',
  tenant_id varchar(20) not null default '0' comment '租户ID，0=平台基线',
  profile_code varchar(64) not null comment '槽位编码（对齐 biz_type，如 brand_logo_md / notice_cover）',
  profile_name varchar(80) not null comment '槽位名称',
  profile_name_i18n json null comment 'locale→名称',
  category_tag varchar(64) null comment '分类标签（如 brand / notice / enterprise）',
  status char(1) not null default '0' comment '0正常 1停用',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_infra_file_profile_tenant_code (tenant_id, profile_code, deleted),
  key idx_infra_file_profile_tenant (tenant_id, deleted),
  key idx_infra_file_profile_category (category_tag, deleted)
) engine=innodb default charset=utf8mb4 comment='文件上传槽位配置';

create table if not exists infra_file_profile_rule (
  id varchar(20) not null comment '规则ID',
  tenant_id varchar(20) not null default '0' comment '租户ID，0=平台基线；租户行覆盖平台',
  profile_code varchar(64) not null comment '槽位编码',
  allowed_exts varchar(256) null comment '允许扩展名 csv（空=不额外限制，仍受全局天花板）',
  allowed_content_types varchar(512) null comment '允许 MIME csv（空=不额外限制）',
  required char(1) not null default '0' comment '是否必传 0否 1是（定稿校验用）',
  min_num int not null default 0 comment '最小文件数',
  max_num int not null default 1 comment '最大文件数',
  min_size_bytes bigint not null default 0 comment '最小字节',
  max_size_bytes bigint not null default 0 comment '最大字节，0=不额外限制',
  sort_no int not null default 0 comment '排序',
  status char(1) not null default '0' comment '0正常 1停用',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_infra_file_profile_rule_tenant_code (tenant_id, profile_code, deleted),
  key idx_infra_file_profile_rule_tenant (tenant_id, deleted)
) engine=innodb default charset=utf8mb4 comment='文件上传槽位规则（平台边界内租户可覆盖）';

create table if not exists infra_file_catalog (
  id varchar(20) not null comment '目录ID',
  tenant_id varchar(20) not null default '0' comment '租户ID，0=平台基线',
  catalog_code varchar(64) not null comment '目录编码（如 COMP_BASIC）',
  catalog_name varchar(80) not null comment '目录名称',
  catalog_name_i18n json null comment 'locale→名称',
  status char(1) not null default '0' comment '0正常 1停用',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_infra_file_catalog_tenant_code (tenant_id, catalog_code, deleted),
  key idx_infra_file_catalog_tenant (tenant_id, deleted)
) engine=innodb default charset=utf8mb4 comment='文件材料目录（多槽位场景编排）';

create table if not exists infra_file_catalog_item (
  id varchar(20) not null comment '目录项ID',
  tenant_id varchar(20) not null default '0' comment '租户ID，0=平台基线',
  catalog_code varchar(64) not null comment '目录编码',
  profile_code varchar(64) not null comment '槽位编码',
  required char(1) null comment '场景级必传覆盖，空则用 profile 规则',
  min_num int null comment '场景级最小数量覆盖',
  max_num int null comment '场景级最大数量覆盖',
  sort_no int not null default 0 comment '排序',
  status char(1) not null default '0' comment '0正常 1停用',
  chrome_roles json null comment '运行时用途→优先级（越小越优先），如 {"sidebar_logo":10}；停用槽位不参与解析',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_infra_file_catalog_item (tenant_id, catalog_code, profile_code, deleted),
  key idx_infra_file_catalog_item_catalog (tenant_id, catalog_code, deleted)
) engine=innodb default charset=utf8mb4 comment='文件材料目录项';

-- ==========================================
-- 19. 异步任务表
-- ==========================================
create table if not exists infra_async_task (
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
  requeue_count int not null default 0 comment '投递重投次数（排队对账）',
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
  key idx_infra_async_task_created_at (created_at),
  key idx_infra_async_task_status_updated (task_status, updated_at)
) engine=innodb default charset=utf8mb4 comment='异步任务表';

-- ==========================================
-- 20. 异步任务日志表
-- ==========================================
create table if not exists infra_async_task_log (
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

-- ==========================================
-- 21. 接口访问日志表
-- ==========================================
create table if not exists infra_api_access_log (
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
  prev_hash varchar(64) null comment '审计链上一行哈希',
  row_hash varchar(64) null comment '本行完整性哈希',
  primary key (id),
  key idx_infra_api_access_log_tenant_id (tenant_id),
  key idx_infra_api_access_log_user_id (user_id),
  key idx_infra_api_access_log_request_uri (request_uri),
  key idx_infra_api_access_log_accessed_at (accessed_at)
) engine=innodb default charset=utf8mb4 comment='接口访问日志表';

-- 审计归档表（live 到期后先归档再删除）
create table if not exists sys_login_log_archive (
  id varchar(20) not null,
  tenant_id varchar(20) not null,
  username varchar(50) not null,
  login_type varchar(20) not null default 'password',
  login_status char(1) not null,
  message varchar(500) null,
  ip varchar(64) null,
  location varchar(255) null,
  user_agent varchar(500) null,
  browser varchar(100) null,
  os varchar(100) null,
  login_at datetime(3) not null,
  prev_hash varchar(64) null,
  row_hash varchar(64) null,
  archived_at datetime(3) not null default current_timestamp(3),
  primary key (id),
  key idx_sys_login_log_archive_tenant_login_at (tenant_id, login_at),
  key idx_sys_login_log_archive_login_at (login_at),
  key idx_sys_login_log_archive_archived_at (archived_at)
) engine=innodb default charset=utf8mb4 comment='登录日志归档';

create table if not exists sys_operation_log_archive (
  id varchar(20) not null,
  tenant_id varchar(20) not null,
  trace_id varchar(64) null,
  module_name varchar(100) null,
  business_type varchar(50) null,
  method varchar(255) null,
  request_method varchar(20) null,
  operator_id varchar(20) null,
  operator_name varchar(50) null,
  operation_url varchar(255) null,
  operation_ip varchar(64) null,
  operation_location varchar(255) null,
  request_param text null,
  response_result text null,
  operation_status char(1) not null default '0',
  error_message text null,
  cost_time int null,
  operation_at datetime(3) not null,
  prev_hash varchar(64) null,
  row_hash varchar(64) null,
  archived_at datetime(3) not null default current_timestamp(3),
  primary key (id),
  key idx_sys_operation_log_archive_tenant_op_at (tenant_id, operation_at),
  key idx_sys_operation_log_archive_operation_at (operation_at),
  key idx_sys_operation_log_archive_archived_at (archived_at)
) engine=innodb default charset=utf8mb4 comment='操作日志归档';

create table if not exists infra_api_access_log_archive (
  id varchar(20) not null,
  tenant_id varchar(20) not null,
  trace_id varchar(64) null,
  request_uri varchar(255) not null,
  request_method varchar(20) not null,
  client_ip varchar(64) null,
  user_id varchar(20) null,
  username varchar(50) null,
  request_param text null,
  response_code int null,
  response_message varchar(255) null,
  cost_time int null,
  accessed_at datetime(3) not null,
  prev_hash varchar(64) null,
  row_hash varchar(64) null,
  archived_at datetime(3) not null default current_timestamp(3),
  primary key (id),
  key idx_infra_api_access_log_archive_tenant_at (tenant_id, accessed_at),
  key idx_infra_api_access_log_archive_accessed_at (accessed_at),
  key idx_infra_api_access_log_archive_archived_at (archived_at)
) engine=innodb default charset=utf8mb4 comment='接口访问日志归档';

-- ==========================================
-- 22. 第三方调用日志表
-- ==========================================
create table if not exists infra_thirdparty_call_log (
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



-- i18n / 代码生成 / AI 审计表
-- 菜单多语言：sys_menu.name_i18n（已无独立 sys_menu_locale 表）

create table if not exists sys_user_preference (
  tenant_id varchar(20) not null comment '租户ID',
  user_id varchar(20) not null comment '用户ID',
  pref_key varchar(64) not null comment '偏好键',
  pref_value varchar(256) not null comment '偏好值',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  primary key (tenant_id, user_id, pref_key)
) engine=innodb default charset=utf8mb4 comment='用户偏好表';

create table if not exists sys_i18n_locale (
  id varchar(20) not null comment '主键',
  locale_code varchar(16) not null comment '语言代码 zh-CN/en-US',
  locale_name varchar(64) not null comment '语言名称（管理端展示）',
  native_name varchar(64) not null comment '原生语言名称',
  sort_no int not null default 0 comment '排序',
  is_default char(1) not null default 'N' comment '是否默认 Y/N',
  status char(1) not null default '0' comment '状态 0正常 1停用',
  remark varchar(256) default null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_sys_i18n_locale_code_deleted (locale_code, deleted),
  key idx_sys_i18n_locale_deleted (deleted)
) engine=innodb default charset=utf8mb4 comment='国际化语言配置表';

create table if not exists sys_i18n_message (
  id varchar(20) not null comment '主键',
  message_key varchar(128) not null comment '消息键',
  locale varchar(16) not null comment '语言代码',
  message_value text not null comment '消息内容',
  module varchar(64) not null default 'common' comment '模块分组',
  scope varchar(16) not null default 'frontend' comment '作用域 backend/frontend/all',
  remark varchar(256) default null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_sys_i18n_message_key_locale_deleted (message_key, locale, deleted),
  key idx_sys_i18n_message_locale (locale),
  key idx_sys_i18n_message_module (module),
  key idx_sys_i18n_message_scope (scope),
  key idx_sys_i18n_message_deleted (deleted)
) engine=innodb default charset=utf8mb4 comment='平台国际化文案表';

create table if not exists gen_table (
  id varchar(20) not null comment '编号',
  table_name varchar(200) not null default '' comment '表名称',
  table_comment varchar(500) default '' comment '表描述',
  class_name varchar(100) not null default '' comment '实体类名(Post)',
  tpl_category varchar(50) not null default 'crud' comment '模板 crud/tree',
  tpl_web_type varchar(30) default 'element-plus-ts' comment '前端模板',
  backend_module varchar(30) not null default 'system' comment '后端模块 system/infra/biz',
  api_module varchar(30) not null default 'system' comment 'API包 system/tenant/infra/biz',
  base_type varchar(30) not null default 'tenant' comment '实体基类 tenant/platform/base/plain',
  business_name varchar(50) not null default '' comment '业务名(路由段)',
  function_name varchar(100) not null default '' comment '功能名',
  function_author varchar(50) default 'icoolkj' comment '作者',
  route_prefix varchar(100) default '' comment '路由前缀 /system',
  permission_prefix varchar(100) default '' comment '权限前缀',
  gen_type char(1) not null default '0' comment '0zip 1自定义路径',
  gen_path varchar(255) default '' comment '生成根路径',
  options varchar(2000) default null comment '扩展JSON',
  remark varchar(500) default null,
  created_by varchar(20) null,
  created_at datetime(3) not null default current_timestamp(3),
  updated_by varchar(20) null,
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3),
  primary key (id)
) engine=innodb default charset=utf8mb4 comment='代码生成业务表';

create table if not exists gen_table_column (
  id varchar(20) not null comment '编号',
  table_id varchar(20) not null comment '归属表',
  column_name varchar(200) not null comment '列名',
  column_comment varchar(500) default '' comment '列描述',
  column_type varchar(100) default '' comment '列类型',
  java_type varchar(50) default 'String' comment 'Java类型',
  java_field varchar(100) default '' comment 'Java字段',
  is_pk char(1) default '0' comment '是否主键',
  is_insert char(1) default '1' comment '插入',
  is_edit char(1) default '1' comment '编辑',
  is_list char(1) default '1' comment '列表',
  is_export char(1) default '1' comment '导出(Excel 白名单列)',
  is_import char(1) default '0' comment '导入(Excel 分通道列)',
  is_query char(1) default '0' comment '查询',
  query_type varchar(20) default 'EQ' comment 'EQ/LIKE',
  html_type varchar(50) default 'input' comment '控件类型',
  dict_type varchar(100) default '' comment '字典',
  is_required char(1) not null default '0' comment '是否必填(非空)',
  column_length int default null comment '字符串最大长度/数值精度',
  sort int default 0 comment '排序',
  created_by varchar(20) null,
  created_at datetime(3) not null default current_timestamp(3),
  updated_by varchar(20) null,
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3),
  primary key (id),
  key idx_gen_table_column_table_id (table_id)
) engine=innodb default charset=utf8mb4 comment='代码生成业务表字段';

create table if not exists gen_i18n_message (
  id            varchar(20)  not null comment '主键',
  table_id      varchar(20)  not null comment 'gen_table.id',
  locale        varchar(16)  not null comment 'zh-CN / en-US',
  messages_json json         not null comment 'CRUD 页文案 JSON（page/fields/meta）',
  remark        varchar(256) default null,
  created_at    datetime(3)  not null default current_timestamp(3),
  updated_at    datetime(3)  not null default current_timestamp(3) on update current_timestamp(3),
  primary key (id),
  unique key uk_gen_i18n_table_locale (table_id, locale),
  key idx_gen_i18n_table_id (table_id)
) engine=innodb default charset=utf8mb4 comment='低代码生成表 UI 国际化（JSON）';

create table if not exists gen_view_config (
  id         varchar(20)  not null comment '主键',
  table_id   varchar(20)  not null comment 'gen_table.id',
  view_json  json         not null comment 'CRUD 布局 JSON（query/table/form）',
  remark     varchar(256) default null,
  created_at datetime(3)  not null default current_timestamp(3),
  updated_at datetime(3)  not null default current_timestamp(3) on update current_timestamp(3),
  primary key (id),
  unique key uk_gen_view_table (table_id),
  key idx_gen_view_table_id (table_id)
) engine=innodb default charset=utf8mb4 comment='低代码生成表 UI 布局（view.json）';

create table if not exists tool_ai_invoke_log (
  id varchar(20) not null comment '调用ID',
  tenant_id varchar(20) not null comment '租户ID',
  trace_id varchar(64) null comment '链路ID',
  provider_name varchar(50) not null default 'DeepSeek' comment '提供方',
  model varchar(100) null comment '模型',
  biz_type varchar(50) not null comment '业务类型',
  biz_ref_id varchar(64) null comment '业务关联ID',
  request_url varchar(500) null comment '请求地址',
  response_status char(1) not null default '0' comment '0成功 1失败',
  error_message varchar(1000) null comment '错误摘要',
  cost_time int null comment '耗时毫秒',
  user_prompt_preview varchar(500) null comment '用户输入摘要',
  response_preview varchar(500) null comment '响应摘要',
  operator_id varchar(20) null comment '操作人ID',
  operator_name varchar(100) null comment '操作人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  primary key (id),
  key idx_tool_ai_invoke_log_tenant_id (tenant_id),
  key idx_tool_ai_invoke_log_biz_type (biz_type),
  key idx_tool_ai_invoke_log_trace_id (trace_id),
  key idx_tool_ai_invoke_log_created_at (created_at)
) engine=innodb default charset=utf8mb4 comment='AI 调用记录';

create table if not exists tool_ai_provider (
  id varchar(20) not null comment '主键',
  tenant_id varchar(20) not null comment '0=平台；租户自建连接',
  provider_code varchar(64) not null comment '连接编码',
  provider_name varchar(128) not null comment '显示名',
  protocol varchar(32) not null default 'openai_compatible' comment '协议：openai_compatible',
  base_url varchar(512) null comment 'API Base URL',
  api_key_cipher varchar(1024) null comment '加密后的 API Key',
  timeout_seconds int not null default 60 comment '超时秒',
  status char(1) not null default '0' comment '0正常 1停用',
  remark varchar(500) null,
  created_by varchar(20) null,
  created_at datetime(3) not null default current_timestamp(3),
  updated_by varchar(20) null,
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3),
  deleted tinyint(1) not null default 0,
  primary key (id),
  unique key uk_tool_ai_provider_code (tenant_id, provider_code, deleted),
  key idx_tool_ai_provider_tenant (tenant_id, deleted, status)
) engine=innodb default charset=utf8mb4 comment='AI 连接（OpenAI 兼容）';

create table if not exists tool_ai_model (
  id varchar(20) not null comment '主键',
  tenant_id varchar(20) not null comment '与 provider 同属',
  provider_id varchar(20) not null comment '所属连接',
  model_code varchar(128) not null comment '请求 model 字段',
  model_name varchar(128) not null comment '显示名',
  capabilities varchar(128) not null comment '逗号分隔：chat,image,embedding',
  default_params_json text null comment '默认参数 JSON',
  status char(1) not null default '0' comment '0正常 1停用',
  sort_no int not null default 0,
  remark varchar(500) null,
  created_by varchar(20) null,
  created_at datetime(3) not null default current_timestamp(3),
  updated_by varchar(20) null,
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3),
  deleted tinyint(1) not null default 0,
  primary key (id),
  unique key uk_tool_ai_model_code (tenant_id, provider_id, model_code, deleted),
  key idx_tool_ai_model_provider (provider_id, deleted),
  key idx_tool_ai_model_tenant (tenant_id, deleted, status)
) engine=innodb default charset=utf8mb4 comment='AI 模型（挂连接下，带能力标签）';

create table if not exists tool_ai_capability_binding (
  id varchar(20) not null comment '主键',
  tenant_id varchar(20) not null comment '0=平台兜底；租户=当前启用选择',
  capability varchar(32) not null comment 'chat/image/embedding',
  model_id varchar(20) not null comment '选用的模型（可为平台或本租户）',
  enabled tinyint(1) not null default 1,
  remark varchar(500) null,
  created_by varchar(20) null,
  created_at datetime(3) not null default current_timestamp(3),
  updated_by varchar(20) null,
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3),
  deleted tinyint(1) not null default 0,
  primary key (id),
  unique key uk_tool_ai_cap_bind (tenant_id, capability, deleted),
  key idx_tool_ai_cap_model (model_id, deleted)
) engine=innodb default charset=utf8mb4 comment='能力当前启用模型绑定';

create table if not exists tool_ai_invoke_content (
  invoke_id varchar(20) not null comment '调用ID',
  system_prompt mediumtext null comment '系统提示词',
  user_message mediumtext null comment '用户消息',
  response_content mediumtext null comment 'AI 响应',
  primary key (invoke_id)
) engine=innodb default charset=utf8mb4 comment='AI 调用详情（大文本）';

create table if not exists tool_ai_ddl_record (
  id varchar(20) not null comment '记录ID',
  tenant_id varchar(20) not null comment '租户ID',
  trace_id varchar(64) null comment '链路ID',
  invoke_id varchar(20) null comment '关联调用ID',
  record_type varchar(30) not null comment 'CREATE_DDL/ALTER_DDL',
  api_module varchar(30) null comment '模块',
  target_table_name varchar(100) null comment '目标表',
  generated_summary varchar(500) null comment '摘要',
  user_prompt_preview varchar(500) null comment '需求摘要',
  status varchar(20) not null default 'GENERATED' comment 'GENERATED/COPIED/EXECUTED',
  operator_id varchar(20) null comment '操作人ID',
  user_id varchar(20) null comment '归属用户',
  operator_name varchar(100) null comment '操作人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  primary key (id),
  key idx_tool_ai_ddl_record_tenant_id (tenant_id),
  key idx_tool_ai_ddl_record_invoke_id (invoke_id),
  key idx_tool_ai_ddl_record_record_type (record_type),
  key idx_tool_ai_ddl_record_owner (tenant_id, user_id),
  key idx_tool_ai_ddl_record_created_at (created_at)
) engine=innodb default charset=utf8mb4 comment='AI DDL 生成记录';

create table if not exists tool_ai_ddl_content (
  ddl_record_id varchar(20) not null comment 'DDL记录ID',
  user_prompt mediumtext null comment '用户需求',
  generated_ddl mediumtext null comment 'AI 生成 DDL',
  final_ddl mediumtext null comment '最终 DDL（用户编辑后）',
  primary key (ddl_record_id)
) engine=innodb default charset=utf8mb4 comment='AI DDL 详情（大文本）';

create table if not exists tool_ai_chat_session (
  id varchar(20) not null comment '会话ID',
  tenant_id varchar(20) not null comment '租户ID',
  title varchar(200) null comment '标题',
  status varchar(20) not null default 'ACTIVE' comment 'ACTIVE/ARCHIVED',
  operator_id varchar(20) null comment '操作人ID',
  user_id varchar(20) null comment '归属用户',
  operator_name varchar(100) null comment '操作人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  primary key (id),
  key idx_tool_ai_chat_session_tenant_id (tenant_id),
  key idx_tool_ai_chat_session_owner (tenant_id, user_id),
  key idx_tool_ai_chat_session_created_at (created_at)
) engine=innodb default charset=utf8mb4 comment='AI 对话会话（预留）';

create table if not exists tool_ai_chat_message (
  id varchar(20) not null comment '消息ID',
  session_id varchar(20) not null comment '会话ID',
  invoke_id varchar(20) null comment '关联调用ID',
  role varchar(20) not null comment 'user/assistant/system',
  content_preview varchar(500) null comment '内容摘要',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  primary key (id),
  key idx_tool_ai_chat_message_session_id (session_id),
  key idx_tool_ai_chat_message_invoke_id (invoke_id)
) engine=innodb default charset=utf8mb4 comment='AI 对话消息';

create table if not exists tool_ai_chat_message_content (
  message_id varchar(20) not null comment '消息ID',
  content mediumtext null comment '消息全文',
  primary key (message_id)
) engine=innodb default charset=utf8mb4 comment='AI 对话消息详情';

-- =============================================================================
-- 可配置 OA 审批模板（平台声明 + 租户物化映射 + 实例存储）
-- =============================================================================

create table if not exists oa_platform_template (
  id varchar(20) not null comment '平台模板ID',
  tenant_id varchar(20) not null default '0' comment '固定 0=平台',
  category_code varchar(64) not null comment '业务分类编码',
  template_code varchar(64) not null comment '模板编码',
  template_name varchar(100) not null comment '模板名称',
  wf_model_id varchar(20) null comment '默认关联 wf_model.id',
  latest_version_no int not null default 0 comment '当前已发布版本号',
  status char(1) not null default '0' comment '0正常 1停用',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_oa_platform_tpl_code (tenant_id, template_code, deleted),
  unique key uk_oa_platform_tpl_category (tenant_id, category_code, deleted),
  key idx_oa_platform_tpl_wf_model (wf_model_id),
  key idx_oa_platform_tpl_status (status)
) engine=innodb default charset=utf8mb4 comment='OA平台审批模板头表';

create table if not exists oa_platform_template_version (
  id varchar(20) not null comment '平台模板版本ID',
  tenant_id varchar(20) not null default '0' comment '固定 0=平台',
  template_id varchar(20) not null comment 'oa_platform_template.id',
  version_no int not null comment '版本号',
  version_status varchar(20) not null default 'draft' comment 'draft/published/archived',
  wf_model_id varchar(20) null comment 'wf_model.id',
  wf_model_version_no int null comment '发布时锁定的 wf_model 版本号',
  published_at datetime(3) null comment '发布时间',
  remark varchar(500) null comment '版本说明',
  layout_json mediumtext null comment '表单布局 Layout Persistence 引用形 JSON（草稿）',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_oa_platform_tpl_ver (template_id, version_no, deleted),
  key idx_oa_platform_tpl_ver_status (template_id, version_status),
  key idx_oa_platform_tpl_ver_wf (wf_model_id)
) engine=innodb default charset=utf8mb4 comment='OA平台模板版本表';

create table if not exists oa_platform_field_def (
  id varchar(20) not null comment '字段定义ID',
  tenant_id varchar(20) not null default '0' comment '固定 0=平台',
  template_version_id varchar(20) not null comment 'oa_platform_template_version.id',
  field_code varchar(64) not null comment '字段编码',
  field_name varchar(100) not null comment '字段名称',
  field_alias varchar(100) null comment '字段别名',
  field_type varchar(20) not null comment '数据类型',
  component_type varchar(32) null comment '组件类型',
  dict_type varchar(64) null comment '字典类型',
  field_required char(1) not null default '0' comment '是否必填',
  sort_no int not null default 0 comment '排序',
  field_group varchar(64) null comment '字段分组',
  col_span tinyint not null default 1 comment '栅格跨度1或2',
  default_value varchar(500) null comment '默认值',
  max_length int null comment '最大长度',
  platform_locked char(1) not null default '0' comment '平台锁定',
  used_in_condition char(1) not null default '0' comment '用于流程条件',
  used_in_list char(1) not null default '0' comment '用于列表检索',
  promote_slot varchar(32) null comment '提升槽位 idx_str_01…idx_str_100',
  list_slot_sync char(1) not null default '0' comment '冗余主表 idx_str_01…10',
  status char(1) not null default '0' comment '0正常 1停用',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_oa_platform_field (template_version_id, field_code, deleted),
  key idx_oa_platform_field_tpl_ver (template_version_id),
  key idx_oa_platform_field_promote (template_version_id, promote_slot)
) engine=innodb default charset=utf8mb4 comment='OA平台字段声明映射表';

create table if not exists oa_tenant_template (
  id varchar(20) not null comment '租户模板ID',
  tenant_id varchar(20) not null comment '企业租户ID',
  category_code varchar(64) not null comment '业务分类',
  template_code varchar(64) not null comment '租户模板编码',
  template_name varchar(100) not null comment '租户侧显示名称',
  platform_template_id varchar(20) not null comment 'oa_platform_template.id',
  source_platform_tpl_ver_id varchar(20) null comment '首次启用拷贝的平台版本ID',
  latest_version_no int not null default 0 comment '当前已发布租户版本号',
  published_tpl_ver_id varchar(20) null comment '当前生效租户版本ID',
  enable_status varchar(20) not null default 'disabled' comment 'disabled/enabled',
  status char(1) not null default '0' comment '0正常 1停用',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_oa_tenant_tpl_category (tenant_id, category_code, deleted),
  unique key uk_oa_tenant_tpl_code (tenant_id, template_code, deleted),
  key idx_oa_tenant_tpl_platform (platform_template_id),
  key idx_oa_tenant_tpl_enable (tenant_id, enable_status)
) engine=innodb default charset=utf8mb4 comment='OA租户审批模板头表';

create table if not exists oa_tenant_template_version (
  id varchar(20) not null comment '租户模板版本ID',
  tenant_id varchar(20) not null comment '企业租户ID',
  tenant_template_id varchar(20) not null comment 'oa_tenant_template.id',
  version_no int not null comment '租户版本号',
  version_status varchar(20) not null default 'draft' comment 'draft/published/archived',
  platform_tpl_ver_id varchar(20) not null comment '拷贝源平台版本ID',
  wf_model_id varchar(20) null comment 'wf_model.id',
  wf_model_version_no int null comment 'wf_model 版本号',
  field_snapshot_json mediumtext null comment '发布时字段快照',
  layout_json mediumtext null comment '表单布局 Layout Persistence 引用形 JSON（草稿）',
  layout_snapshot_json mediumtext null comment '发布时布局快照',
  published_at datetime(3) null comment '发布时间',
  remark varchar(500) null comment '版本说明',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_oa_tenant_tpl_ver (tenant_template_id, version_no, deleted),
  key idx_oa_tenant_tpl_ver_status (tenant_template_id, version_status),
  key idx_oa_tenant_tpl_ver_platform_src (platform_tpl_ver_id)
) engine=innodb default charset=utf8mb4 comment='OA租户模板版本表';

create table if not exists oa_tenant_field_def (
  id varchar(20) not null comment '租户字段映射ID',
  tenant_id varchar(20) not null comment '企业租户ID',
  template_version_id varchar(20) not null comment 'oa_tenant_template_version.id',
  source_platform_field_id varchar(20) null comment '来源平台字段ID',
  field_code varchar(64) not null comment '字段编码',
  field_name varchar(100) not null comment '字段名称',
  field_alias varchar(100) null comment '字段别名',
  field_type varchar(20) not null comment '数据类型',
  component_type varchar(32) null comment '组件类型',
  dict_type varchar(64) null comment '字典类型',
  field_required char(1) not null default '0' comment '是否必填',
  sort_no int not null default 0 comment '排序',
  field_group varchar(64) null comment '字段分组',
  col_span tinyint not null default 1 comment '栅格跨度1或2',
  default_value varchar(500) null comment '默认值',
  max_length int null comment '最大长度',
  platform_locked char(1) not null default '0' comment '平台锁定',
  used_in_condition char(1) not null default '0' comment '用于流程条件',
  used_in_list char(1) not null default '0' comment '用于列表检索',
  promote_slot varchar(32) null comment '提升槽位',
  list_slot_sync char(1) not null default '0' comment '冗余主表',
  origin_type varchar(20) not null default 'platform_copy' comment 'platform_copy/tenant_custom/tenant_override',
  status char(1) not null default '0' comment '0正常 1停用',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_oa_tenant_field (template_version_id, field_code, deleted),
  key idx_oa_tenant_field_tpl_ver (template_version_id),
  key idx_oa_tenant_field_origin (origin_type),
  key idx_oa_tenant_field_source (source_platform_field_id)
) engine=innodb default charset=utf8mb4 comment='OA租户字段映射表';

create table if not exists oa_instance (
  id varchar(20) not null comment '实例ID',
  tenant_id varchar(20) not null comment '业务租户ID',
  category_code varchar(64) not null comment '业务分类',
  tenant_template_id varchar(20) not null comment 'oa_tenant_template.id',
  tenant_tpl_ver_id varchar(20) not null comment '租户模板版本',
  platform_tpl_ver_id varchar(20) not null comment '平台模板版本',
  instance_title varchar(200) null comment '单据标题',
  approve_status varchar(32) not null default 'draft' comment 'draft/running/approved/rejected/cancelled/closed',
  process_instance_id varchar(64) null comment 'Flowable流程实例ID',
  process_definition_key varchar(64) null comment '流程定义Key',
  starter_user_id varchar(20) not null comment '发起人',
  user_id varchar(20) not null comment '归属用户',
  dept_id varchar(20) null comment '归属部门',
  submitted_at datetime(3) null comment '提交时间',
  finished_at datetime(3) null comment '结束时间',
  idx_str_01 varchar(128) null, idx_str_02 varchar(128) null, idx_str_03 varchar(128) null,
  idx_str_04 varchar(128) null, idx_str_05 varchar(128) null, idx_str_06 varchar(128) null,
  idx_str_07 varchar(128) null, idx_str_08 varchar(128) null, idx_str_09 varchar(128) null,
  idx_str_10 varchar(128) null,
  status char(1) not null default '0' comment '0正常 1停用',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null, created_at datetime(3) not null default current_timestamp(3),
  updated_by varchar(20) null, updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3),
  deleted tinyint(1) not null default 0,
  primary key (id),
  key idx_oa_instance_tenant_category (tenant_id, category_code, approve_status),
  key idx_oa_instance_tpl_ver (tenant_tpl_ver_id),
  key idx_oa_instance_starter (tenant_id, starter_user_id, created_at),
  key idx_oa_instance_owner (tenant_id, user_id, approve_status),
  key idx_oa_instance_dept (tenant_id, dept_id, approve_status),
  key idx_oa_instance_process (process_instance_id),
  key idx_oa_instance_list_01 (tenant_id, category_code, idx_str_01),
  key idx_oa_instance_list_02 (tenant_id, category_code, idx_str_02),
  key idx_oa_instance_list_03 (tenant_id, category_code, idx_str_03)
) engine=innodb default charset=utf8mb4 comment='OA业务实例主表';

create table if not exists oa_instance_data (
  id varchar(20) not null comment '数据行ID',
  tenant_id varchar(20) not null comment '业务租户ID',
  instance_id varchar(20) not null comment 'oa_instance.id',
  form_data json not null comment '表单JSON',
  content_text mediumtext null comment '富文本正文',
  field_snapshot_json mediumtext null comment '字段快照',
  layout_snapshot_json mediumtext null comment '发起时布局快照',
  created_by varchar(20) null, created_at datetime(3) not null default current_timestamp(3),
  updated_by varchar(20) null, updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3),
  deleted tinyint(1) not null default 0,
  primary key (id),
  unique key uk_oa_instance_data_instance (instance_id, deleted),
  key idx_oa_instance_data_tenant (tenant_id)
) engine=innodb default charset=utf8mb4 comment='OA实例表单数据表';

create table if not exists oa_instance_promote (
  id varchar(20) not null comment '槽位值ID',
  tenant_id varchar(20) not null comment '业务租户ID',
  instance_id varchar(20) not null comment 'oa_instance.id',
  slot_code varchar(32) not null comment '槽位编码',
  str_value varchar(1000) null comment '槽位值',
  field_code varchar(64) null comment '来源字段',
  created_by varchar(20) null, created_at datetime(3) not null default current_timestamp(3),
  updated_by varchar(20) null, updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3),
  deleted tinyint(1) not null default 0,
  primary key (id),
  unique key uk_oa_instance_promote_slot (instance_id, slot_code, deleted),
  key idx_oa_instance_promote_tenant (tenant_id, slot_code),
  key idx_oa_instance_promote_value (tenant_id, slot_code, str_value(128)),
  key idx_oa_instance_promote_instance (instance_id)
) engine=innodb default charset=utf8mb4 comment='OA实例提升槽位表';

-- ==========================================
-- 平台 AI Skill 能力目录（全局表，无 tenant_id；须加入 ignore-tables）
-- ==========================================
create table if not exists sys_ai_skill (
  id                     varchar(20)  not null comment '主键',
  skill_code             varchar(64)  not null comment '稳定编码，与 Handler 绑定',
  tool_name              varchar(64)  not null comment 'OpenAI tool function name',
  category               varchar(32)  not null default 'OFFICE' comment 'OFFICE/TOOL/OPS',
  skill_name             varchar(100) not null comment '默认显示名（中文兜底）',
  name_i18n              json         null comment 'locale -> 显示名',
  description            varchar(500) null comment '默认描述（中文兜底）',
  description_i18n       json         null comment 'locale -> 描述',
  required_permission    varchar(128) not null comment '调用所需业务权限码',
  tool_description       varchar(1000) not null comment '模型侧工具说明（英文）',
  parameters_schema_json mediumtext   not null comment 'OpenAI parameters JSON Schema',
  sort_no                int          not null default 0 comment '排序，越小越前',
  status                 char(1)      not null default '0' comment '0正常 1停用',
  remark                 varchar(500) null comment '备注',
  created_by             varchar(20)  not null default '0' comment '创建人',
  created_at             datetime(3)  not null default current_timestamp(3) comment '创建时间',
  updated_by             varchar(20)  not null default '0' comment '更新人',
  updated_at             datetime(3)  not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted                tinyint(1)   not null default 0 comment '逻辑删除 0否 1是',
  primary key (id),
  unique key uk_sys_ai_skill_code (skill_code),
  unique key uk_sys_ai_skill_tool_name (tool_name),
  key idx_sys_ai_skill_category_status (category, status, sort_no)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='平台 AI Skill 能力目录';

-- ==========================================
-- 办公助手会话（租户隔离 + 仅本人可见）
-- ==========================================
create table if not exists oa_ai_chat_session (
  id               varchar(20)  not null comment '会话ID',
  tenant_id        varchar(20)  not null comment '租户ID',
  user_id          varchar(20)  not null comment '归属用户（仅本人可读写）',
  title            varchar(200) null comment '会话标题（首条用户问题截断）',
  status           char(1)      not null default '0' comment '0正常 1归档',
  last_message_at  datetime(3)  null comment '最近消息时间',
  message_count    int          not null default 0 comment '消息条数',
  created_by       varchar(20)  not null default '0' comment '创建人',
  created_at       datetime(3)  not null default current_timestamp(3) comment '创建时间',
  updated_by       varchar(20)  not null default '0' comment '更新人',
  updated_at       datetime(3)  not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted          tinyint(1)   not null default 0 comment '逻辑删除',
  primary key (id),
  key idx_oa_ai_chat_session_owner (tenant_id, user_id, deleted, last_message_at)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='办公助手 AI 会话';

create table if not exists oa_ai_chat_message (
  id                varchar(20)  not null comment '消息ID',
  tenant_id         varchar(20)  not null comment '租户ID',
  session_id        varchar(20)  not null comment '会话ID',
  role              varchar(20)  not null comment 'user/assistant',
  content           mediumtext   not null comment '消息全文',
  used_skills_json  varchar(500) null comment '助手消息已调用 skill_code JSON 数组',
  created_by        varchar(20)  not null default '0' comment '创建人',
  created_at        datetime(3)  not null default current_timestamp(3) comment '创建时间',
  updated_by        varchar(20)  not null default '0' comment '更新人',
  updated_at        datetime(3)  not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted           tinyint(1)   not null default 0 comment '逻辑删除',
  primary key (id),
  key idx_oa_ai_chat_message_session (tenant_id, session_id, deleted, created_at)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci comment='办公助手 AI 消息';
