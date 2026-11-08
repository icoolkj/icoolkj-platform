-- 工作流应用层表（与 V1__schema.sql 同步）

-- wf_model 流程模型表
create table wf_model (
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

-- wf_model_version 流程模型版本表
create table wf_model_version (
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

-- wf_biz_process 业务流程实例关联表
create table wf_biz_process (
  id varchar(20) not null comment '记录ID',
  tenant_id varchar(20) not null default '0' comment '租户ID',
  model_id varchar(20) not null comment '模型ID',
  model_version_no int null comment '发起时锁定的 wf_model_version.version_no',
  biz_type varchar(64) not null comment '业务类型',
  biz_id varchar(20) not null comment '业务主键',
  biz_title varchar(200) null comment '业务标题',
  process_definition_key varchar(64) not null comment '流程定义Key',
  process_instance_id varchar(64) not null comment 'Flowable流程实例ID',
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

-- wf_biz_binding_policy 业务类型与流程绑定策略
create table wf_biz_binding_policy (
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

-- wf_approver_rule 审批人规则
create table wf_approver_rule (
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

-- wf_task_action 任务动作审计（转办/催办/驳回跳转/超时/加签）
create table wf_task_action (
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
