-- =============================================================================
-- 可配置 OA 审批模板（平台声明映射 + 租户物化映射版本 + 统一实例存储）
-- 设计说明见对话规范；落地时合并进 V1__schema.sql 或独立 migration（按项目节奏）
--
-- 业务分类：字典 oa_biz_category（dict_value = category_code），与 wf_model.biz_type 对齐
--
-- 表清单（9 张）：
--   平台：oa_platform_template / oa_platform_template_version / oa_platform_field_def
--   租户：oa_tenant_template / oa_tenant_template_version / oa_tenant_field_def
--   实例：oa_instance（瘦主表）/ oa_instance_data（JSON+长文）/ oa_instance_promote（100 字符串槽位）
--
-- 提升槽位规范（总计 idx_str_01 … idx_str_100）：
--   idx_str_01 … idx_str_10：可 list_slot_sync 冗余到 oa_instance 主表（列表免 JOIN，最多 10 列）
--   idx_str_11 … idx_str_100：仅 oa_instance_promote（检索/条件/报表，JOIN + 索引）
--   同一字段 promote_slot 全局唯一；list_slot_sync=1 时 promote_slot 必须在 01…10
--   长文/富文本 → oa_instance_data.content_text；全量 → form_data JSON
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 平台审批模板（头表，tenant_id 固定 0）
-- -----------------------------------------------------------------------------
create table oa_platform_template (
  id varchar(20) not null comment '平台模板ID',
  tenant_id varchar(20) not null default '0' comment '固定 0=平台',
  category_code varchar(64) not null comment '业务分类编码，对齐字典 oa_biz_category / wf_model.biz_type',
  template_code varchar(64) not null comment '模板编码',
  template_name varchar(100) not null comment '模板名称',
  wf_model_id varchar(20) null comment '默认关联 wf_model.id',
  latest_version_no int not null default 0 comment '当前已发布版本号，0=未发布',
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

-- -----------------------------------------------------------------------------
-- 2. 平台模板版本（不可变；改字段声明 = 新版本）
-- -----------------------------------------------------------------------------
create table oa_platform_template_version (
  id varchar(20) not null comment '平台模板版本ID',
  tenant_id varchar(20) not null default '0' comment '固定 0=平台',
  template_id varchar(20) not null comment 'oa_platform_template.id',
  version_no int not null comment '版本号，从 1 递增',
  version_status varchar(20) not null default 'draft' comment 'draft草稿 published已发布 archived归档',
  wf_model_id varchar(20) null comment '该版本绑定的 wf_model.id',
  wf_model_version_no int null comment '发布时锁定的 wf_model 版本号',
  published_at datetime(3) null comment '发布时间',
  remark varchar(500) null comment '版本说明',
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

-- -----------------------------------------------------------------------------
-- 3. 平台字段声明映射
-- -----------------------------------------------------------------------------
create table oa_platform_field_def (
  id varchar(20) not null comment '字段定义ID',
  tenant_id varchar(20) not null default '0' comment '固定 0=平台',
  template_version_id varchar(20) not null comment 'oa_platform_template_version.id',
  field_code varchar(64) not null comment '字段编码，JSON key / 流程变量名',
  field_name varchar(100) not null comment '字段名称',
  field_alias varchar(100) null comment '字段别名/简称',
  field_type varchar(20) not null comment '数据类型 string/int/decimal/date/datetime/bool/file/ref/richText',
  component_type varchar(32) null comment '组件类型 input/select/date/textarea/richText/fileUpload等',
  dict_type varchar(64) null comment '关联 sys_dict 字典类型',
  field_required char(1) not null default '0' comment '是否必填 1是0否',
  sort_no int not null default 0 comment '显示排序升序',
  default_value varchar(500) null comment '默认值',
  max_length int null comment '最大长度',
  platform_locked char(1) not null default '0' comment '平台锁定 1=租户不可删不可改类型',
  used_in_condition char(1) not null default '0' comment '是否用于流程条件 1是',
  used_in_list char(1) not null default '0' comment '是否用于列表检索 1是',
  promote_slot varchar(32) null comment '提升槽位 idx_str_01…idx_str_100；richText→content_text 不占槽位',
  list_slot_sync char(1) not null default '0' comment '冗余到主表 idx_str_01…10（须与 promote_slot 对应）1是',
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

-- -----------------------------------------------------------------------------
-- 4. 租户审批模板（头表；租户管理员启用/发布后产生）
-- -----------------------------------------------------------------------------
create table oa_tenant_template (
  id varchar(20) not null comment '租户模板ID',
  tenant_id varchar(20) not null comment '企业租户ID',
  category_code varchar(64) not null comment '业务分类',
  template_code varchar(64) not null comment '租户模板编码',
  template_name varchar(100) not null comment '租户侧显示名称',
  platform_template_id varchar(20) not null comment '来源 oa_platform_template.id',
  source_platform_tpl_ver_id varchar(20) null comment '首次启用时拷贝的平台版本ID',
  latest_version_no int not null default 0 comment '当前已发布租户版本号',
  published_tpl_ver_id varchar(20) null comment '当前生效的 oa_tenant_template_version.id',
  enable_status varchar(20) not null default 'disabled' comment 'disabled未启用 enabled已启用',
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

-- -----------------------------------------------------------------------------
-- 5. 租户模板版本（重新定制 = 新版本；已发布版本只读）
-- -----------------------------------------------------------------------------
create table oa_tenant_template_version (
  id varchar(20) not null comment '租户模板版本ID',
  tenant_id varchar(20) not null comment '企业租户ID',
  tenant_template_id varchar(20) not null comment 'oa_tenant_template.id',
  version_no int not null comment '租户版本号，从 1 递增',
  version_status varchar(20) not null default 'draft' comment 'draft草稿 published已发布 archived归档',
  platform_tpl_ver_id varchar(20) not null comment '拷贝源平台版本 oa_platform_template_version.id',
  wf_model_id varchar(20) null comment '该租户版本使用的流程模型',
  wf_model_version_no int null comment '发布时锁定的 wf_model 版本号',
  field_snapshot_json mediumtext null comment '发布时字段定义全量快照',
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

-- -----------------------------------------------------------------------------
-- 6. 租户字段映射（物化映射表）
-- -----------------------------------------------------------------------------
create table oa_tenant_field_def (
  id varchar(20) not null comment '租户字段映射ID',
  tenant_id varchar(20) not null comment '企业租户ID',
  template_version_id varchar(20) not null comment 'oa_tenant_template_version.id',
  source_platform_field_id varchar(20) null comment '来源 oa_platform_field_def.id',
  field_code varchar(64) not null comment '字段编码',
  field_name varchar(100) not null comment '字段名称',
  field_alias varchar(100) null comment '字段别名',
  field_type varchar(20) not null comment '数据类型',
  component_type varchar(32) null comment '组件类型',
  dict_type varchar(64) null comment '字典类型',
  field_required char(1) not null default '0' comment '是否必填',
  sort_no int not null default 0 comment '排序',
  default_value varchar(500) null comment '默认值',
  max_length int null comment '最大长度',
  platform_locked char(1) not null default '0' comment '平台锁定',
  used_in_condition char(1) not null default '0' comment '用于流程条件',
  used_in_list char(1) not null default '0' comment '用于列表检索',
  promote_slot varchar(32) null comment '提升槽位 idx_str_01…idx_str_100',
  list_slot_sync char(1) not null default '0' comment '冗余到主表 idx_str_01…10（须与 promote_slot 对应）',
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

-- -----------------------------------------------------------------------------
-- 7. OA 业务实例主表（瘦表：头信息 + 列表冗余槽位）
-- wf_biz_process.biz_id = id；biz_type 建议 = category_code
-- -----------------------------------------------------------------------------
create table oa_instance (
  id varchar(20) not null comment '实例ID',
  tenant_id varchar(20) not null comment '业务租户ID',
  category_code varchar(64) not null comment '业务分类',
  tenant_template_id varchar(20) not null comment 'oa_tenant_template.id',
  tenant_tpl_ver_id varchar(20) not null comment '发起时锁定的租户模板版本',
  platform_tpl_ver_id varchar(20) not null comment '发起时锁定的平台模板版本',
  instance_title varchar(200) null comment '单据标题',
  approve_status varchar(32) not null default 'draft' comment 'draft/running/approved/rejected/cancelled/closed',
  process_instance_id varchar(64) null comment 'Flowable流程实例ID',
  process_definition_key varchar(64) null comment '流程定义Key',
  starter_user_id varchar(20) not null comment '发起人',
  user_id varchar(20) not null comment '归属用户',
  dept_id varchar(20) null comment '归属部门',
  submitted_at datetime(3) null comment '提交审批时间',
  finished_at datetime(3) null comment '结束时间',
  -- 列表冗余：仅 idx_str_01…10；idx_str_11…100 仅存于 oa_instance_promote
  idx_str_01 varchar(128) null comment '列表冗余槽位1',
  idx_str_02 varchar(128) null comment '列表冗余槽位2',
  idx_str_03 varchar(128) null comment '列表冗余槽位3',
  idx_str_04 varchar(128) null comment '列表冗余槽位4',
  idx_str_05 varchar(128) null comment '列表冗余槽位5',
  idx_str_06 varchar(128) null comment '列表冗余槽位6',
  idx_str_07 varchar(128) null comment '列表冗余槽位7',
  idx_str_08 varchar(128) null comment '列表冗余槽位8',
  idx_str_09 varchar(128) null comment '列表冗余槽位9',
  idx_str_10 varchar(128) null comment '列表冗余槽位10',
  status char(1) not null default '0' comment '0正常 1停用',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
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

-- -----------------------------------------------------------------------------
-- 8. OA 实例表单数据（1:1；JSON 全貌 + 长文正文，列表默认不 JOIN）
-- -----------------------------------------------------------------------------
create table oa_instance_data (
  id varchar(20) not null comment '数据行ID',
  tenant_id varchar(20) not null comment '业务租户ID',
  instance_id varchar(20) not null comment 'oa_instance.id',
  form_data json not null comment '扩展字段值全貌，key=field_code',
  content_text mediumtext null comment '文章/富文本长正文（field_type=richText）',
  field_snapshot_json mediumtext null comment '发起时字段定义快照，保障在途展示',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_oa_instance_data_instance (instance_id, deleted),
  key idx_oa_instance_data_tenant (tenant_id)
) engine=innodb default charset=utf8mb4 comment='OA实例表单数据表';

-- -----------------------------------------------------------------------------
-- 9. OA 实例提升槽位（1:N；逻辑槽位 idx_str_01 … idx_str_100，值统一字符串）
-- idx_str_01…10 可与主表冗余列同步；idx_str_11…100 仅本表 + 索引检索
-- -----------------------------------------------------------------------------
create table oa_instance_promote (
  id varchar(20) not null comment '槽位值ID',
  tenant_id varchar(20) not null comment '业务租户ID',
  instance_id varchar(20) not null comment 'oa_instance.id',
  slot_code varchar(32) not null comment '槽位编码 idx_str_01…idx_str_100',
  str_value varchar(1000) null comment '槽位值（统一字符串，写入时按 field_type 格式化）',
  field_code varchar(64) null comment '来源字段编码，便于追溯',
  created_by varchar(20) null comment '创建人',
  created_at datetime(3) not null default current_timestamp(3) comment '创建时间',
  updated_by varchar(20) null comment '更新人',
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted tinyint(1) not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_oa_instance_promote_slot (instance_id, slot_code, deleted),
  key idx_oa_instance_promote_tenant (tenant_id, slot_code),
  key idx_oa_instance_promote_value (tenant_id, slot_code, str_value(128)),
  key idx_oa_instance_promote_instance (instance_id)
) engine=innodb default charset=utf8mb4 comment='OA实例提升槽位表';
