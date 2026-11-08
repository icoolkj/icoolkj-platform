-- =============================================================================
-- V4 · 通用办事 10 类（平台基础模板 + icoolkj 完整工作流样例）
-- =============================================================================
-- A. 平台：10 类基础模板（字典/绑定策略/wf 壳+stub BPMN/OA 表单字段）；无 design / 审批规则
-- B. icoolkj：完整组织矩阵（部门/角色/人员）
-- C. icoolkj：10 套完整工作流配置（design + approver_rule + 已启用模板）
-- 其它租户：拉取平台 OA 基础模板后，自行配置工作流
-- =============================================================================

set names utf8mb4;

-- =============================================================================
-- A. 平台侧（基础模板：无流程设计/审批规则）
-- =============================================================================
insert into sys_dict_type (
  id, tenant_id, dict_name, dict_type, status, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '5000000000000000091' as id, '0' as tenant_id, '报销类型' as dict_name, 'oa_expense_type' as dict_type, '0' as status,
         '[OA] oa_expense.expenseType' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '5000000000000000092', '0', '用印文件类型', 'oa_seal_file_type', '0', '[OA] oa_seal.fileType', '0', now(3), '0', now(3), 0
  union all select '5000000000000000093', '0', 'IT权限等级', 'oa_it_privilege', '0', '[OA] oa_it_access.privilegeLevel', '0', now(3), '0', now(3), 0
) x
where not exists (
  select 1 from sys_dict_type t where t.id = x.id or (t.tenant_id = x.tenant_id and t.dict_type = x.dict_type and t.deleted = 0)
);

insert into sys_dict_item (
  id, tenant_id, dict_type_id, dict_label, dict_value, value_type, css_class, icon, list_class, is_default, sort_no, status, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '5100000000000000905' as id, '0' as tenant_id, '5000000000000000033' as dict_type_id, '费用报销' as dict_label, 'oa_expense' as dict_value, 'string' as value_type, null as css_class, 'Wallet' as icon, 'success' as list_class, '0' as is_default, 3 as sort_no, '0' as status, '基础模板' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '5100000000000000906', '0', '5000000000000000033', '出差申请', 'oa_travel', 'string', null, 'Suitcase', 'success', '0', 4, '0', '基础模板', '0', now(3), '0', now(3), 0
  union all select '5100000000000000907', '0', '5000000000000000033', '合同评审', 'oa_contract', 'string', null, 'Notebook', 'primary', '0', 10, '0', '基础模板', '0', now(3), '0', now(3), 0
  union all select '5100000000000000908', '0', '5000000000000000033', '员工入职', 'oa_onboard', 'string', null, 'Avatar', 'primary', '0', 11, '0', '基础模板', '0', now(3), '0', now(3), 0
  union all select '5100000000000000909', '0', '5000000000000000033', '员工离职', 'oa_offboard', 'string', null, 'SwitchButton', 'warning', '0', 12, '0', '基础模板', '0', now(3), '0', now(3), 0
  union all select '5100000000000000910', '0', '5000000000000000033', '采购申请', 'oa_purchase', 'string', null, 'ShoppingCart', 'success', '0', 13, '0', '基础模板', '0', now(3), '0', now(3), 0
  union all select '5100000000000000911', '0', '5000000000000000033', '用印申请', 'oa_seal', 'string', null, 'Stamp', 'warning', '0', 14, '0', '基础模板', '0', now(3), '0', now(3), 0
  union all select '5100000000000000912', '0', '5000000000000000033', 'IT权限开通', 'oa_it_access', 'string', null, 'Key', 'info', '0', 15, '0', '基础模板', '0', now(3), '0', now(3), 0
  union all select '5100000000000000913', '0', '5000000000000000033', '销售报价审批', 'oa_quote', 'string', null, 'Ticket', 'success', '0', 16, '0', '基础模板', '0', now(3), '0', now(3), 0
  union all select '5100000000000000914', '0', '5000000000000000043', '婚假', 'marriage', 'string', null, null, 'success', 0, 4, '0', null, '0', now(3), '0', now(3), 0
  union all select '5100000000000000915', '0', '5000000000000000091', '差旅费', 'travel', 'string', null, null, 'primary', 1, 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '5100000000000000916', '0', '5000000000000000091', '招待费', 'entertain', 'string', null, null, 'info', 0, 2, '0', null, '0', now(3), '0', now(3), 0
  union all select '5100000000000000917', '0', '5000000000000000091', '办公费', 'office', 'string', null, null, 'success', 0, 3, '0', null, '0', now(3), '0', now(3), 0
  union all select '5100000000000000918', '0', '5000000000000000092', '普通通知', 'normal', 'string', null, null, 'info', 1, 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '5100000000000000919', '0', '5000000000000000092', '合同/对外承诺', 'contract', 'string', null, null, 'warning', 0, 2, '0', null, '0', now(3), '0', now(3), 0
  union all select '5100000000000000920', '0', '5000000000000000093', '普通功能权限', 'normal', 'string', null, null, 'info', 1, 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '5100000000000000921', '0', '5000000000000000093', '核心数据/管理员', 'high', 'string', null, null, 'danger', 0, 2, '0', null, '0', now(3), '0', now(3), 0
) x
where not exists (
  select 1 from sys_dict_item t where t.id = x.id or (t.tenant_id = x.tenant_id and t.dict_type_id = x.dict_type_id and t.dict_value = x.dict_value and t.deleted = 0)
);


insert into wf_biz_binding_policy (
  id, biz_type, binding_mode, allow_tenant_approver_override, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '9090000000000000101' as id, 'oa_expense' as biz_type, 'tenant_required' as binding_mode, '1' as allow_tenant_approver_override, '费用报销：须部署本租户模型' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '9090000000000000103', 'oa_travel', 'tenant_required', '1', '出差申请：须部署本租户模型', '0', now(3), '0', now(3), 0
  union all select '9090000000000000104', 'oa_contract', 'tenant_required', '1', '合同评审：须部署本租户模型', '0', now(3), '0', now(3), 0
  union all select '9090000000000000105', 'oa_onboard', 'tenant_required', '1', '员工入职：须部署本租户模型', '0', now(3), '0', now(3), 0
  union all select '9090000000000000106', 'oa_offboard', 'tenant_required', '1', '员工离职：须部署本租户模型', '0', now(3), '0', now(3), 0
  union all select '9090000000000000107', 'oa_purchase', 'tenant_required', '1', '采购申请：须部署本租户模型', '0', now(3), '0', now(3), 0
  union all select '9090000000000000108', 'oa_seal', 'tenant_required', '1', '用印申请：须部署本租户模型', '0', now(3), '0', now(3), 0
  union all select '9090000000000000109', 'oa_it_access', 'tenant_required', '1', 'IT权限开通：须部署本租户模型', '0', now(3), '0', now(3), 0
  union all select '9090000000000000110', 'oa_quote', 'tenant_required', '1', '销售报价审批：须部署本租户模型', '0', now(3), '0', now(3), 0
) x
where not exists (
  select 1 from wf_biz_binding_policy t where t.id = x.id or (t.biz_type = x.biz_type and t.deleted = 0)
);

set @cfg_global := '{"startAcl":{"userIds":[],"deptIds":[],"roleCodes":["employee"],"postIds":[],"tenantIds":[]},"behavior":{"allowWithdraw":true,"commentRequired":false},"notify":{"onTodo":true,"onReject":true,"onComplete":true,"onCc":true}}';
set @bpmn_stub := concat(
  '<?xml version="1.0" encoding="UTF-8"?>',
  '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" ',
  'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ',
  'xmlns:flowable="http://flowable.org/bpmn" ',
  'targetNamespace="http://www.icoolkj.com/office-lab">',
  '<process id="__KEY__" name="__KEY__" isExecutable="true">',
  '<startEvent id="startEvent"/><endEvent id="endApproved"/>',
  '<sequenceFlow id="flowStart" sourceRef="startEvent" targetRef="endApproved"/>',
  '</process></definitions>'
);

insert into wf_model (
  id, tenant_id, model_key, model_name, category_name, biz_type, form_type, form_route, latest_version, status, process_config, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '9090000000000000011' as id, '0' as tenant_id, 'oa_expense' as model_key, '费用报销' as model_name, '通用办事' as category_name, 'oa_expense' as biz_type, 'system' as form_type, '/office/approve' as form_route, 1 as latest_version, '0' as status, @cfg_global as process_config, '平台基础模板：费用报销' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '9090000000000000013', '0', 'oa_travel', '出差申请', '通用办事', 'oa_travel', 'system', '/office/approve', 1, '0', @cfg_global, '平台基础模板：出差申请', '0', now(3), '0', now(3), 0
  union all select '9090000000000000001', '0', 'oa_contract', '合同评审', '通用办事', 'oa_contract', 'system', '/office/approve', 1, '0', @cfg_global, '平台基础模板：合同评审', '0', now(3), '0', now(3), 0
  union all select '9090000000000000002', '0', 'oa_onboard', '员工入职', '通用办事', 'oa_onboard', 'system', '/office/approve', 1, '0', @cfg_global, '平台基础模板：员工入职', '0', now(3), '0', now(3), 0
  union all select '9090000000000000003', '0', 'oa_offboard', '员工离职', '通用办事', 'oa_offboard', 'system', '/office/approve', 1, '0', @cfg_global, '平台基础模板：员工离职', '0', now(3), '0', now(3), 0
  union all select '9090000000000000004', '0', 'oa_purchase', '采购申请', '通用办事', 'oa_purchase', 'system', '/office/approve', 1, '0', @cfg_global, '平台基础模板：采购申请', '0', now(3), '0', now(3), 0
  union all select '9090000000000000015', '0', 'oa_seal', '用印申请', '通用办事', 'oa_seal', 'system', '/office/approve', 1, '0', @cfg_global, '平台基础模板：用印申请', '0', now(3), '0', now(3), 0
  union all select '9090000000000000016', '0', 'oa_it_access', 'IT权限开通', '通用办事', 'oa_it_access', 'system', '/office/approve', 1, '0', @cfg_global, '平台基础模板：IT权限开通', '0', now(3), '0', now(3), 0
  union all select '9090000000000000017', '0', 'oa_quote', '销售报价审批', '通用办事', 'oa_quote', 'system', '/office/approve', 1, '0', @cfg_global, '平台基础模板：销售报价审批', '0', now(3), '0', now(3), 0
) x
where not exists (
  select 1 from wf_model t where t.id = x.id or (t.tenant_id = x.tenant_id and t.model_key = x.model_key)
);



insert into wf_model_version (
  id, tenant_id, model_id, version_no, bpmn_xml, design_json, process_config_json, deployment_id, process_definition_id, process_definition_key, published, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '9090000000000000311' as id, '0' as tenant_id, '9090000000000000011' as model_id, 1 as version_no, replace(@bpmn_stub, '__KEY__', 'oa_expense') as bpmn_xml, null as design_json, @cfg_global as process_config_json, null as deployment_id, null as process_definition_id, 'oa_expense' as process_definition_key, 1 as published, '平台基础模板' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '9090000000000000313', '0', '9090000000000000013', 1, replace(@bpmn_stub, '__KEY__', 'oa_travel'), null, @cfg_global, null, null, 'oa_travel', 1, '平台基础模板', '0', now(3), '0', now(3), 0
  union all select '9090000000000000301', '0', '9090000000000000001', 1, replace(@bpmn_stub, '__KEY__', 'oa_contract'), null, @cfg_global, null, null, 'oa_contract', 1, '平台基础模板', '0', now(3), '0', now(3), 0
  union all select '9090000000000000302', '0', '9090000000000000002', 1, replace(@bpmn_stub, '__KEY__', 'oa_onboard'), null, @cfg_global, null, null, 'oa_onboard', 1, '平台基础模板', '0', now(3), '0', now(3), 0
  union all select '9090000000000000303', '0', '9090000000000000003', 1, replace(@bpmn_stub, '__KEY__', 'oa_offboard'), null, @cfg_global, null, null, 'oa_offboard', 1, '平台基础模板', '0', now(3), '0', now(3), 0
  union all select '9090000000000000304', '0', '9090000000000000004', 1, replace(@bpmn_stub, '__KEY__', 'oa_purchase'), null, @cfg_global, null, null, 'oa_purchase', 1, '平台基础模板', '0', now(3), '0', now(3), 0
  union all select '9090000000000000315', '0', '9090000000000000015', 1, replace(@bpmn_stub, '__KEY__', 'oa_seal'), null, @cfg_global, null, null, 'oa_seal', 1, '平台基础模板', '0', now(3), '0', now(3), 0
  union all select '9090000000000000316', '0', '9090000000000000016', 1, replace(@bpmn_stub, '__KEY__', 'oa_it_access'), null, @cfg_global, null, null, 'oa_it_access', 1, '平台基础模板', '0', now(3), '0', now(3), 0
  union all select '9090000000000000317', '0', '9090000000000000017', 1, replace(@bpmn_stub, '__KEY__', 'oa_quote'), null, @cfg_global, null, null, 'oa_quote', 1, '平台基础模板', '0', now(3), '0', now(3), 0
) x
where not exists (
  select 1 from wf_model_version t where t.id = x.id or (t.model_id = x.model_id and t.version_no = x.version_no)
);




insert into oa_platform_template (
  id, tenant_id, category_code, template_code, template_name, wf_model_id, latest_version_no, status, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '9290000000000000400' as id, '0' as tenant_id, 'oa_expense' as category_code, 'oa_expense' as template_code, '费用报销' as template_name, '9090000000000000011' as wf_model_id, 1 as latest_version_no, '0' as status, '基础模板' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '9290000000000000410', '0', 'oa_travel', 'oa_travel', '出差申请', '9090000000000000013', 1, '0', '基础模板', '0', now(3), '0', now(3), 0
  union all select '9290000000000000420', '0', 'oa_contract', 'oa_contract', '合同评审', '9090000000000000001', 1, '0', '基础模板', '0', now(3), '0', now(3), 0
  union all select '9290000000000000430', '0', 'oa_onboard', 'oa_onboard', '员工入职', '9090000000000000002', 1, '0', '基础模板', '0', now(3), '0', now(3), 0
  union all select '9290000000000000440', '0', 'oa_offboard', 'oa_offboard', '员工离职', '9090000000000000003', 1, '0', '基础模板', '0', now(3), '0', now(3), 0
  union all select '9290000000000000450', '0', 'oa_purchase', 'oa_purchase', '采购申请', '9090000000000000004', 1, '0', '基础模板', '0', now(3), '0', now(3), 0
  union all select '9290000000000000460', '0', 'oa_seal', 'oa_seal', '用印申请', '9090000000000000015', 1, '0', '基础模板', '0', now(3), '0', now(3), 0
  union all select '9290000000000000470', '0', 'oa_it_access', 'oa_it_access', 'IT权限开通', '9090000000000000016', 1, '0', '基础模板', '0', now(3), '0', now(3), 0
  union all select '9290000000000000480', '0', 'oa_quote', 'oa_quote', '销售报价审批', '9090000000000000017', 1, '0', '基础模板', '0', now(3), '0', now(3), 0
) x
where not exists (
  select 1 from oa_platform_template t where t.id = x.id or (t.tenant_id = x.tenant_id and t.template_code = x.template_code and t.deleted = 0)
);

insert into oa_platform_template_version (
  id, tenant_id, template_id, version_no, version_status, wf_model_id, wf_model_version_no, published_at, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '9290000000000000401' as id, '0' as tenant_id, '9290000000000000400' as template_id, 1 as version_no, 'published' as version_status, '9090000000000000011' as wf_model_id, 1 as wf_model_version_no, now(3) as published_at, '基础模板' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '9290000000000000411', '0', '9290000000000000410', 1, 'published', '9090000000000000013', 1, now(3), '基础模板', '0', now(3), '0', now(3), 0
  union all select '9290000000000000421', '0', '9290000000000000420', 1, 'published', '9090000000000000001', 1, now(3), '基础模板', '0', now(3), '0', now(3), 0
  union all select '9290000000000000431', '0', '9290000000000000430', 1, 'published', '9090000000000000002', 1, now(3), '基础模板', '0', now(3), '0', now(3), 0
  union all select '9290000000000000441', '0', '9290000000000000440', 1, 'published', '9090000000000000003', 1, now(3), '基础模板', '0', now(3), '0', now(3), 0
  union all select '9290000000000000451', '0', '9290000000000000450', 1, 'published', '9090000000000000004', 1, now(3), '基础模板', '0', now(3), '0', now(3), 0
  union all select '9290000000000000461', '0', '9290000000000000460', 1, 'published', '9090000000000000015', 1, now(3), '基础模板', '0', now(3), '0', now(3), 0
  union all select '9290000000000000471', '0', '9290000000000000470', 1, 'published', '9090000000000000016', 1, now(3), '基础模板', '0', now(3), '0', now(3), 0
  union all select '9290000000000000481', '0', '9290000000000000480', 1, 'published', '9090000000000000017', 1, now(3), '基础模板', '0', now(3), '0', now(3), 0
) x
where not exists (
  select 1 from oa_platform_template_version t where t.id = x.id or (t.template_id = x.template_id and t.version_no = x.version_no and t.deleted = 0)
);

insert into oa_platform_field_def (
  id, tenant_id, template_version_id, field_code, field_name, field_alias, field_type, component_type, dict_type, field_required, sort_no, default_value, max_length, platform_locked, used_in_condition, used_in_list, promote_slot, list_slot_sync, status, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '9290000000000004002' as id, '0' as tenant_id, '9290000000000000401' as template_version_id, 'title' as field_code, '标题' as field_name, null as field_alias, 'string' as field_type, 'input' as component_type, null as dict_type, '1' as field_required, 1 as sort_no, null as default_value, 100 as max_length, '1' as platform_locked, '0' as used_in_condition, '1' as used_in_list, 'idx_str_01' as promote_slot, '1' as list_slot_sync, '0' as status, null as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '9290000000000004003', '0', '9290000000000000401', 'expenseType', '报销类型', null, 'string', 'select', 'oa_expense_type', '1', 2, null, null, '1', '0', '1', 'idx_str_02', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004004', '0', '9290000000000000401', 'amount', '报销金额', null, 'decimal', 'input', null, '1', 3, null, null, '1', '1', '1', 'idx_str_03', '1', '0', '条件变量', '0', now(3), '0', now(3), 0
  union all select '9290000000000004005', '0', '9290000000000000401', 'reason', '报销事由', null, 'string', 'textarea', null, '1', 4, null, 500, '0', '0', '0', null, '0', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004006', '0', '9290000000000000401', 'attachments', '发票附件', null, 'file', 'file', null, '1', 5, null, null, '0', '0', '0', null, '0', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004102', '0', '9290000000000000411', 'title', '标题', null, 'string', 'input', null, '1', 1, null, 100, '1', '0', '1', 'idx_str_01', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004103', '0', '9290000000000000411', 'destination', '出差地点', null, 'string', 'input', null, '1', 2, null, 100, '1', '0', '1', 'idx_str_02', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004104', '0', '9290000000000000411', 'amount', '出差预算', null, 'decimal', 'input', null, '1', 3, null, null, '1', '1', '1', 'idx_str_03', '1', '0', '条件变量', '0', now(3), '0', now(3), 0
  union all select '9290000000000004105', '0', '9290000000000000411', 'startDate', '开始日期', null, 'date', 'date', null, '1', 4, null, null, '0', '0', '1', 'idx_str_04', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004106', '0', '9290000000000000411', 'endDate', '结束日期', null, 'date', 'date', null, '1', 5, null, null, '0', '0', '1', 'idx_str_05', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004107', '0', '9290000000000000411', 'reason', '出差事由', null, 'string', 'textarea', null, '1', 6, null, 500, '0', '0', '0', null, '0', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004202', '0', '9290000000000000421', 'title', '合同标题', null, 'string', 'input', null, '1', 1, null, 100, '1', '0', '1', 'idx_str_01', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004203', '0', '9290000000000000421', 'contractNo', '合同编号', null, 'string', 'input', null, '0', 2, null, 64, '1', '0', '1', 'idx_str_02', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004204', '0', '9290000000000000421', 'amount', '合同金额', null, 'decimal', 'input', null, '1', 3, null, null, '0', '0', '1', 'idx_str_03', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004205', '0', '9290000000000000421', 'reason', '合同摘要', null, 'string', 'textarea', null, '1', 4, null, 500, '0', '0', '0', null, '0', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004206', '0', '9290000000000000421', 'attachments', '合同附件', null, 'file', 'file', null, '1', 5, null, null, '0', '0', '0', null, '0', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004302', '0', '9290000000000000431', 'title', '入职标题', null, 'string', 'input', null, '1', 1, null, 100, '1', '0', '1', 'idx_str_01', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004303', '0', '9290000000000000431', 'employeeName', '入职人姓名', null, 'string', 'input', null, '1', 2, null, 64, '1', '0', '1', 'idx_str_02', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004304', '0', '9290000000000000431', 'entryDate', '入职日期', null, 'date', 'date', null, '1', 3, null, null, '0', '0', '1', 'idx_str_03', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004305', '0', '9290000000000000431', 'deptName', '入职部门', null, 'string', 'input', null, '1', 4, null, 64, '0', '0', '1', 'idx_str_04', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004306', '0', '9290000000000000431', 'reason', '备注', null, 'string', 'textarea', null, '0', 5, null, 500, '0', '0', '0', null, '0', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004402', '0', '9290000000000000441', 'title', '离职标题', null, 'string', 'input', null, '1', 1, null, 100, '1', '0', '1', 'idx_str_01', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004403', '0', '9290000000000000441', 'leaveDate', '预计离职日', null, 'date', 'date', null, '1', 2, null, null, '1', '0', '1', 'idx_str_02', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004404', '0', '9290000000000000441', 'handover', '工作交接人', null, 'string', 'input', null, '1', 3, null, 64, '0', '0', '1', 'idx_str_03', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004405', '0', '9290000000000000441', 'reason', '离职原因', null, 'string', 'textarea', null, '1', 4, null, 500, '0', '0', '0', null, '0', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004502', '0', '9290000000000000451', 'title', '采购标题', null, 'string', 'input', null, '1', 1, null, 100, '1', '0', '1', 'idx_str_01', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004503', '0', '9290000000000000451', 'amount', '采购金额', null, 'decimal', 'input', null, '1', 2, null, null, '1', '1', '1', 'idx_str_02', '1', '0', '条件变量', '0', now(3), '0', now(3), 0
  union all select '9290000000000004504', '0', '9290000000000000451', 'itemName', '物资名称', null, 'string', 'input', null, '1', 3, null, 100, '0', '0', '1', 'idx_str_03', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004505', '0', '9290000000000000451', 'reason', '采购事由', null, 'string', 'textarea', null, '1', 4, null, 500, '0', '0', '0', null, '0', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004602', '0', '9290000000000000461', 'title', '用印标题', null, 'string', 'input', null, '1', 1, null, 100, '1', '0', '1', 'idx_str_01', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004603', '0', '9290000000000000461', 'fileType', '文件类型', null, 'string', 'select', 'oa_seal_file_type', '1', 2, null, null, '1', '1', '1', 'idx_str_02', '1', '0', '条件变量', '0', now(3), '0', now(3), 0
  union all select '9290000000000004604', '0', '9290000000000000461', 'sealPurpose', '用印用途', null, 'string', 'textarea', null, '1', 3, null, 500, '0', '0', '0', null, '0', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004605', '0', '9290000000000000461', 'attachments', '待盖章文件', null, 'file', 'file', null, '1', 4, null, null, '0', '0', '0', null, '0', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004702', '0', '9290000000000000471', 'title', '权限申请标题', null, 'string', 'input', null, '1', 1, null, 100, '1', '0', '1', 'idx_str_01', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004703', '0', '9290000000000000471', 'privilegeLevel', '权限等级', null, 'string', 'select', 'oa_it_privilege', '1', 2, null, null, '1', '1', '1', 'idx_str_02', '1', '0', '条件变量', '0', now(3), '0', now(3), 0
  union all select '9290000000000004704', '0', '9290000000000000471', 'systemName', '系统/模块', null, 'string', 'input', null, '1', 3, null, 100, '0', '0', '1', 'idx_str_03', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004705', '0', '9290000000000000471', 'reason', '申请事由', null, 'string', 'textarea', null, '1', 4, null, 500, '0', '0', '0', null, '0', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004802', '0', '9290000000000000481', 'title', '报价标题', null, 'string', 'input', null, '1', 1, null, 100, '1', '0', '1', 'idx_str_01', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004803', '0', '9290000000000000481', 'customerName', '客户名称', null, 'string', 'input', null, '1', 2, null, 100, '1', '0', '1', 'idx_str_02', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004804', '0', '9290000000000000481', 'amount', '报价金额', null, 'decimal', 'input', null, '1', 3, null, null, '0', '0', '1', 'idx_str_03', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000004805', '0', '9290000000000000481', 'discount', '折扣(折)', null, 'decimal', 'input', null, '1', 4, null, null, '1', '1', '1', 'idx_str_04', '1', '0', '条件变量，如95表示95折', '0', now(3), '0', now(3), 0
  union all select '9290000000000004806', '0', '9290000000000000481', 'reason', '报价说明', null, 'string', 'textarea', null, '0', 5, null, 500, '0', '0', '0', null, '0', '0', null, '0', now(3), '0', now(3), 0
) x
where not exists (
  select 1 from oa_platform_field_def t where t.id = x.id
);


-- -----------------------------------------------------------------------------
-- 通用办事 · 请假（第 10 类平台基础模板）
-- -----------------------------------------------------------------------------
insert into wf_model (
  id, tenant_id, model_key, model_name, category_name, biz_type, form_type, form_route,
  latest_version, status, process_config, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '9000000000000000002' as id, '0' as tenant_id, 'oa_leave' as model_key, '请假申请' as model_name,
         '通用办事' as category_name, 'oa_leave' as biz_type, 'system' as form_type, '/office/approve' as form_route,
         1 as latest_version, '0' as status, @cfg_global as process_config,
         '通用办事：请假申请（平台基础模板）' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
) x
where not exists (
  select 1 from wf_model t where t.id = x.id or (t.tenant_id = x.tenant_id and t.model_key = x.model_key)
);

insert into wf_biz_binding_policy (
  id, biz_type, binding_mode, allow_tenant_approver_override, remark,
  created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '9000000000000000102' as id, 'oa_leave' as biz_type, 'tenant_required' as binding_mode, '1' as allow_tenant_approver_override,
         '请假申请：须同步并部署本租户模型后发起' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
) x
where not exists (
  select 1 from wf_biz_binding_policy t where t.id = x.id or (t.biz_type = x.biz_type and t.deleted = 0)
);


insert into wf_model_version (
  id, tenant_id, model_id, version_no, bpmn_xml, design_json, process_config_json, deployment_id, process_definition_id, process_definition_key,
  published, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '9000000000000000302' as id, '0' as tenant_id, '9000000000000000002' as model_id, 1 as version_no,
         replace(@bpmn_stub, '__KEY__', 'oa_leave') as bpmn_xml, null as design_json, @cfg_global as process_config_json,
         null as deployment_id, null as process_definition_id, 'oa_leave' as process_definition_key, 1 as published,
         '平台基础模板：请假' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
) x
where not exists (
  select 1 from wf_model_version t where t.id = x.id or (t.model_id = x.model_id and t.version_no = x.version_no)
);

insert into oa_platform_template (
  id, tenant_id, category_code, template_code, template_name, wf_model_id, latest_version_no,
  status, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '9200000000000000001' as id, '0' as tenant_id, 'oa_leave' as category_code, 'oa_leave' as template_code, '请假申请' as template_name,
         '9000000000000000002' as wf_model_id, 1 as latest_version_no, '0' as status, '通用办事：请假申请' as remark,
         '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
) x
where not exists (
  select 1 from oa_platform_template t where t.id = x.id or (t.tenant_id = x.tenant_id and t.template_code = x.template_code and t.deleted = 0)
);

insert into oa_platform_template_version (
  id, tenant_id, template_id, version_no, version_status, wf_model_id, wf_model_version_no,
  published_at, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '9200000000000000002' as id, '0' as tenant_id, '9200000000000000001' as template_id, 1 as version_no, 'published' as version_status,
         '9000000000000000002' as wf_model_id, 1 as wf_model_version_no, now(3) as published_at, '初始发布' as remark,
         '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
) x
where not exists (
  select 1 from oa_platform_template_version t where t.id = x.id or (t.template_id = x.template_id and t.version_no = x.version_no and t.deleted = 0)
);

insert into oa_platform_field_def (
  id, tenant_id, template_version_id, field_code, field_name, field_alias, field_type, component_type,
  dict_type, field_required, sort_no, default_value, max_length, platform_locked,
  used_in_condition, used_in_list, promote_slot, list_slot_sync, status, remark,
  created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '9200000000000000011' as id, '0' as tenant_id, '9200000000000000002' as template_version_id, 'leaveType' as field_code, '请假类型' as field_name, null as field_alias, 'string' as field_type, 'select' as component_type,
         'oa_leave_type' as dict_type, '1' as field_required, 1 as sort_no, null as default_value, 32 as max_length, '1' as platform_locked, '1' as used_in_condition, '1' as used_in_list, 'idx_str_01' as promote_slot, '1' as list_slot_sync, '0' as status, '条件变量' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '9200000000000000012', '0', '9200000000000000002', 'startDate', '开始日期', null, 'date', 'date', null, '1', 2, null, null, '1', '0', '1', 'idx_str_02', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9200000000000000013', '0', '9200000000000000002', 'endDate', '结束日期', null, 'date', 'date', null, '1', 3, null, null, '0', '0', '1', 'idx_str_03', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9200000000000000014', '0', '9200000000000000002', 'leaveDays', '请假天数', null, 'decimal', 'input', null, '1', 4, null, null, '0', '0', '1', 'idx_str_04', '1', '0', null, '0', now(3), '0', now(3), 0
  union all select '9200000000000000017', '0', '9200000000000000002', 'handover', '工作交接人', null, 'string', 'input', null, '0', 5, null, 64, '0', '0', '0', null, '0', '0', null, '0', now(3), '0', now(3), 0
  union all select '9200000000000000015', '0', '9200000000000000002', 'reason', '请假事由', null, 'string', 'textarea', null, '1', 6, null, 500, '0', '0', '0', null, '0', '0', null, '0', now(3), '0', now(3), 0
  union all select '9200000000000000016', '0', '9200000000000000002', 'attachments', '证明材料', null, 'file', 'file', null, '0', 7, null, null, '0', '0', '0', null, '0', '0', null, '0', now(3), '0', now(3), 0
) x
where not exists (
  select 1 from oa_platform_field_def t where t.id = x.id
);

-- =============================================================================
-- B. icoolkj 测试租户组织
-- =============================================================================
-- 登录 tenantCode=icoolkj / 密码 StudyOnly@2026（联调账号）
-- ID 段：租户 100…099 / 部门 119… / 岗位 129… / 角色 209… / 用户 309…

-- ------------------------------------------
-- 1. 租户
-- ------------------------------------------
insert into sys_tenant (
  id, tenant_code, tenant_name, contact_name, contact_phone, contact_email, license_no, expire_at, account_limit, status, tenant_type, parent_tenant_id, ancestors, root_region_id, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '1000000000000000099' as id, 'icoolkj' as tenant_code, 'icoolkj有限公司' as tenant_name, 'icoolkj管理员' as contact_name, '13900000990' as contact_phone, 'admin@icoolkj.test' as contact_email, null as license_no, '2099-12-31 23:59:59.000' as expire_at, 200 as account_limit, '0' as status, 'ENTERPRISE' as tenant_type, null as parent_tenant_id, '0' as ancestors, '1300000000000000002' as root_region_id, 'icoolkj有限公司工作流/OA 测试租户：多部门多角色人员矩阵' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
) x
where not exists (
  select 1 from sys_tenant t where t.id = x.id
);

-- ------------------------------------------
-- 2. 部门树
--    icoolkj有限公司
--    ├── 总经办
--    ├── 研发中心
--    │   ├── 前端组
--    │   └── 后端组
--    ├── 产品部
--    ├── 人力资源部
--    ├── 财务部
--    ├── 行政部
--    ├── 法务部
--    ├── IT部
--    ├── 采购部
--    ├── 仓储部
--    └── 销售部
-- ------------------------------------------
insert into sys_dept (
  id, tenant_id, parent_id, ancestors, dept_name, dept_code, leader_name, phone, email, sort_no, status, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '1190000000000000001' as id, '1000000000000000099' as tenant_id, '0' as parent_id, '0' as ancestors, 'icoolkj有限公司' as dept_name, 'icoolkj_root' as dept_code, '张伟' as leader_name, '13900000990' as phone, 'ceo@icoolkj.test' as email, 0 as sort_no, '0' as status, '根部门' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '1190000000000000002', '1000000000000000099', '1190000000000000001', '0,1190000000000000001', '总经办', 'icoolkj_ceo_office', '张伟', '13900000991', 'ceo-office@icoolkj.test', 1, '0', '总经理办', '0', now(3), '0', now(3), 0
  union all select '1190000000000000003', '1000000000000000099', '1190000000000000001', '0,1190000000000000001', '研发中心', 'icoolkj_rd', '陈浩然', '13900000992', 'rd@icoolkj.test', 2, '0', '研发一级部门', '0', now(3), '0', now(3), 0
  union all select '1190000000000000004', '1000000000000000099', '1190000000000000003', '0,1190000000000000001,1190000000000000003', '前端组', 'icoolkj_rd_fe', '刘晓彤', '13900000993', 'fe@icoolkj.test', 1, '0', '研发-前端', '0', now(3), '0', now(3), 0
  union all select '1190000000000000005', '1000000000000000099', '1190000000000000003', '0,1190000000000000001,1190000000000000003', '后端组', 'icoolkj_rd_be', '王俊杰', '13900000994', 'be@icoolkj.test', 2, '0', '研发-后端', '0', now(3), '0', now(3), 0
  union all select '1190000000000000006', '1000000000000000099', '1190000000000000001', '0,1190000000000000001', '产品部', 'icoolkj_pd', '赵思远', '13900000995', 'pd@icoolkj.test', 3, '0', null, '0', now(3), '0', now(3), 0
  union all select '1190000000000000007', '1000000000000000099', '1190000000000000001', '0,1190000000000000001', '人力资源部', 'icoolkj_hr', '周雅琴', '13900000996', 'hr@icoolkj.test', 4, '0', '入职/离职/请假', '0', now(3), '0', now(3), 0
  union all select '1190000000000000008', '1000000000000000099', '1190000000000000001', '0,1190000000000000001', '财务部', 'icoolkj_finance', '吴嘉慧', '13900000997', 'fin@icoolkj.test', 5, '0', '报销/预算/采购付款', '0', now(3), '0', now(3), 0
  union all select '1190000000000000009', '1000000000000000099', '1190000000000000001', '0,1190000000000000001', '行政部', 'icoolkj_admin', '郑立群', '13900000998', 'admin@icoolkj.test', 6, '0', '用印/入职物资/离职资产', '0', now(3), '0', now(3), 0
  union all select '1190000000000000010', '1000000000000000099', '1190000000000000001', '0,1190000000000000001', '法务部', 'icoolkj_legal', '马文博', '13900000999', 'legal@icoolkj.test', 7, '0', '合同评审/用印复核', '0', now(3), '0', now(3), 0
  union all select '1190000000000000011', '1000000000000000099', '1190000000000000001', '0,1190000000000000001', 'IT部', 'icoolkj_it', '何志强', '13900001000', 'it@icoolkj.test', 8, '0', '权限开通/入职账号/离职回收', '0', now(3), '0', now(3), 0
  union all select '1190000000000000012', '1000000000000000099', '1190000000000000001', '0,1190000000000000001', '采购部', 'icoolkj_purchase', '冯晓薇', '13900001001', 'purchase@icoolkj.test', 9, '0', '物资采购', '0', now(3), '0', now(3), 0
  union all select '1190000000000000013', '1000000000000000099', '1190000000000000001', '0,1190000000000000001', '仓储部', 'icoolkj_warehouse', '曹国栋', '13900001002', 'warehouse@icoolkj.test', 10, '0', '收货验收', '0', now(3), '0', now(3), 0
  union all select '1190000000000000014', '1000000000000000099', '1190000000000000001', '0,1190000000000000001', '销售部', 'icoolkj_sales', '沈悦', '13900001003', 'sales@icoolkj.test', 11, '0', '销售报价审批', '0', now(3), '0', now(3), 0
) x
where not exists (
  select 1 from sys_dept t where t.id = x.id
);

-- ------------------------------------------
-- 3. 岗位
-- ------------------------------------------
insert into sys_post (
  id, tenant_id, post_name, post_code, sort_no, status, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '1290000000000000001' as id, '1000000000000000099' as tenant_id, '租户管理员岗' as post_name, 'icoolkj_post_admin' as post_code, 1 as sort_no, '0' as status, null as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '1290000000000000002', '1000000000000000099', '部门负责人岗', 'icoolkj_post_leader', 2, '0', null, '0', now(3), '0', now(3), 0
  union all select '1290000000000000003', '1000000000000000099', '员工岗', 'icoolkj_post_staff', 3, '0', null, '0', now(3), '0', now(3), 0
  union all select '1290000000000000004', '1000000000000000099', 'HR审批岗', 'icoolkj_post_hr', 4, '0', null, '0', now(3), '0', now(3), 0
  union all select '1290000000000000005', '1000000000000000099', '财务审批岗', 'icoolkj_post_fin', 5, '0', null, '0', now(3), '0', now(3), 0
  union all select '1290000000000000006', '1000000000000000099', '法务审批岗', 'icoolkj_post_legal', 6, '0', null, '0', now(3), '0', now(3), 0
  union all select '1290000000000000007', '1000000000000000099', 'IT审批岗', 'icoolkj_post_it', 7, '0', null, '0', now(3), '0', now(3), 0
  union all select '1290000000000000008', '1000000000000000099', '采购审批岗', 'icoolkj_post_pur', 8, '0', null, '0', now(3), '0', now(3), 0
  union all select '1290000000000000009', '1000000000000000099', '仓储审批岗', 'icoolkj_post_wh', 9, '0', null, '0', now(3), '0', now(3), 0
  union all select '1290000000000000010', '1000000000000000099', '销售审批岗', 'icoolkj_post_sales', 10, '0', null, '0', now(3), '0', now(3), 0
) x
where not exists (
  select 1 from sys_post t where t.id = x.id
);

-- ------------------------------------------
-- 4. 角色（role_code 对齐工作流策略配置）
--    tenant_admin / dept_approver / hr_approver / finance_approver /
--    general_manager / cc_receiver / employee /
--    legal_approver / it_approver / purchase_approver / warehouse_approver /
--    sales_approver / sales_manager / tech_director
-- ------------------------------------------
insert into sys_role (
  id, tenant_id, role_name, role_code, data_scope, sort_no, status, is_super_role, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '2090000000000000001' as id, '1000000000000000099' as tenant_id, '租户管理员' as role_name, 'tenant_admin' as role_code, '1' as data_scope, 1 as sort_no, '0' as status, '1' as is_super_role, '本租户全能力（不含 PLATFORM 菜单）；审批人回落常用' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '2090000000000000002', '1000000000000000099', '部门审批人', 'dept_approver', '3', 2, '0', '0', '一级审批；starter_dept_role / role 策略；数据范围=本部门', '0', now(3), '0', now(3), 0
  union all select '2090000000000000003', '1000000000000000099', '人事审批人', 'hr_approver', '1', 3, '0', '0', 'HR 节点：请假/入职/离职', '0', now(3), '0', now(3), 0
  union all select '2090000000000000004', '1000000000000000099', '财务审批人', 'finance_approver', '1', 4, '0', '0', '财务/报销/预算节点', '0', now(3), '0', now(3), 0
  union all select '2090000000000000005', '1000000000000000099', '总经理', 'general_manager', '1', 5, '0', '0', '终审 / 金额超阈值', '0', now(3), '0', now(3), 0
  union all select '2090000000000000006', '1000000000000000099', '抄送接收人', 'cc_receiver', '4', 6, '0', '0', '抄送节点办理人；数据范围=本人', '0', now(3), '0', now(3), 0
  union all select '2090000000000000016', '1000000000000000099', '行政办事员', 'admin_officer', '1', 16, '0', '0', '平台通用办事行政节点（用印/入职物资等）；拉取平台目录时用', '0', now(3), '0', now(3), 0
  union all select '2090000000000000007', '1000000000000000099', '普通员工', 'employee', '4', 7, '0', '0', '发起人角色；数据范围=本人', '0', now(3), '0', now(3), 0
  union all select '2090000000000000008', '1000000000000000099', '法务审批人', 'legal_approver', '1', 8, '0', '0', '合同评审 / 用印复核', '0', now(3), '0', now(3), 0
  union all select '2090000000000000009', '1000000000000000099', 'IT审批人', 'it_approver', '1', 9, '0', '0', '权限开通 / 入职账号 / 离职回收', '0', now(3), '0', now(3), 0
  union all select '2090000000000000010', '1000000000000000099', '采购审批人', 'purchase_approver', '1', 10, '0', '0', '采购比价与确认', '0', now(3), '0', now(3), 0
  union all select '2090000000000000011', '1000000000000000099', '仓储审批人', 'warehouse_approver', '1', 11, '0', '0', '收货验收', '0', now(3), '0', now(3), 0
  union all select '2090000000000000012', '1000000000000000099', '销售主管', 'sales_approver', '3', 12, '0', '0', '报价初审；数据范围=本部门', '0', now(3), '0', now(3), 0
  union all select '2090000000000000013', '1000000000000000099', '销售经理', 'sales_manager', '1', 13, '0', '0', '中档折扣审批', '0', now(3), '0', now(3), 0
  union all select '2090000000000000014', '1000000000000000099', '技术总监', 'tech_director', '1', 14, '0', '0', '高权限开通终审', '0', now(3), '0', now(3), 0
) x
where not exists (
  select 1 from sys_role t where t.id = x.id
);

-- ------------------------------------------
-- 5. 用户（26 人）密码 = StudyOnly@2026
-- ------------------------------------------
-- hash 与平台种子一致
set @pwd := '$2b$10$s7ENUgEgYy6S32zM3PAAleV8KyzPRBuW1bLL/UJIWeDyXKLV4Vldy';

insert into sys_user (
  id, tenant_id, dept_id, username, nickname, real_name, user_type, email, phone, gender, avatar_url, password_hash, status, is_super_admin, login_fail_count, locked_until, last_login_ip, last_login_at, pwd_updated_at, must_change_password, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '3090000000000000001' as id, '1000000000000000099' as tenant_id, '1190000000000000002' as dept_id, 'icoolkj_admin' as username, '系统管理员' as nickname, '林志远' as real_name, 'system' as user_type, 'icoolkj_admin@icoolkj.test' as email, '13900010001' as phone, '1' as gender, null as avatar_url, @pwd as password_hash, '0' as status, '0' as is_super_admin, 0 as login_fail_count, null as locked_until, null as last_login_ip, null as last_login_at, now(3) as pwd_updated_at, '0' as must_change_password, '租户管理员：配置映射/流程设计' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '3090000000000000002', '1000000000000000099', '1190000000000000002', 'icoolkj_ceo', '张伟', '张伟', 'system', 'icoolkj_ceo@icoolkj.test', '13900010002', '1', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '总经理终审', '0', now(3), '0', now(3), 0
  union all select '3090000000000000003', '1000000000000000099', '1190000000000000002', 'icoolkj_cc01', '孙雨欣', '孙雨欣', 'system', 'icoolkj_cc01@icoolkj.test', '13900010003', '2', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '企划助理·抄送办理人', '0', now(3), '0', now(3), 0
  union all select '3090000000000000004', '1000000000000000099', '1190000000000000003', 'icoolkj_rd_lead', '陈浩然', '陈浩然', 'system', 'icoolkj_rd_lead@icoolkj.test', '13900010004', '1', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '研发中心负责人(dept_approver)', '0', now(3), '0', now(3), 0
  union all select '3090000000000000005', '1000000000000000099', '1190000000000000004', 'icoolkj_fe_lead', '刘晓彤', '刘晓彤', 'system', 'icoolkj_fe_lead@icoolkj.test', '13900010005', '2', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '前端组长：同部门 starter_dept_role', '0', now(3), '0', now(3), 0
  union all select '3090000000000000006', '1000000000000000099', '1190000000000000004', 'icoolkj_fe01', '李明轩', '李明轩', 'system', 'icoolkj_fe01@icoolkj.test', '13900010006', '1', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '前端工程师·常用发起人', '0', now(3), '0', now(3), 0
  union all select '3090000000000000007', '1000000000000000099', '1190000000000000004', 'icoolkj_fe02', '李佳宁', '李佳宁', 'system', 'icoolkj_fe02@icoolkj.test', '13900010007', '2', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '前端工程师', '0', now(3), '0', now(3), 0
  union all select '3090000000000000008', '1000000000000000099', '1190000000000000005', 'icoolkj_be_lead', '王俊杰', '王俊杰', 'system', 'icoolkj_be_lead@icoolkj.test', '13900010008', '1', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '后端组长(dept_approver)', '0', now(3), '0', now(3), 0
  union all select '3090000000000000009', '1000000000000000099', '1190000000000000005', 'icoolkj_be01', '周子墨', '周子墨', 'system', 'icoolkj_be01@icoolkj.test', '13900010009', '1', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '后端工程师', '0', now(3), '0', now(3), 0
  union all select '3090000000000000010', '1000000000000000099', '1190000000000000006', 'icoolkj_pd_lead', '赵思远', '赵思远', 'system', 'icoolkj_pd_lead@icoolkj.test', '13900010010', '1', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '产品负责人(dept_approver)', '0', now(3), '0', now(3), 0
  union all select '3090000000000000011', '1000000000000000099', '1190000000000000006', 'icoolkj_pd01', '钱雨桐', '钱雨桐', 'system', 'icoolkj_pd01@icoolkj.test', '13900010011', '2', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '产品经理', '0', now(3), '0', now(3), 0
  union all select '3090000000000000012', '1000000000000000099', '1190000000000000007', 'icoolkj_hr_lead', '周雅琴', '周雅琴', 'system', 'icoolkj_hr_lead@icoolkj.test', '13900010012', '2', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', 'HR经理(hr_approver+dept_approver)', '0', now(3), '0', now(3), 0
  union all select '3090000000000000013', '1000000000000000099', '1190000000000000007', 'icoolkj_hr01', '吴倩', '吴倩', 'system', 'icoolkj_hr01@icoolkj.test', '13900010013', '2', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '人事专员', '0', now(3), '0', now(3), 0
  union all select '3090000000000000014', '1000000000000000099', '1190000000000000008', 'icoolkj_fin_lead', '吴嘉慧', '吴嘉慧', 'system', 'icoolkj_fin_lead@icoolkj.test', '13900010014', '2', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '财务经理(finance_approver+dept_approver)', '0', now(3), '0', now(3), 0
  union all select '3090000000000000015', '1000000000000000099', '1190000000000000008', 'icoolkj_fin01', '郑晨曦', '郑晨曦', 'system', 'icoolkj_fin01@icoolkj.test', '13900010015', '1', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '出纳', '0', now(3), '0', now(3), 0
  union all select '3090000000000000016', '1000000000000000099', '1190000000000000009', 'icoolkj_adm_lead', '郑立群', '郑立群', 'system', 'icoolkj_adm_lead@icoolkj.test', '13900010016', '1', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '行政经理(dept_approver)·用印/物资', '0', now(3), '0', now(3), 0
  union all select '3090000000000000026', '1000000000000000099', '1190000000000000009', 'icoolkj_adm01', '蒋婉清', '蒋婉清', 'system', 'icoolkj_adm01@icoolkj.test', '13900010026', '2', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '行政专员', '0', now(3), '0', now(3), 0
  union all select '3090000000000000017', '1000000000000000099', '1190000000000000010', 'icoolkj_legal_lead', '马文博', '马文博', 'system', 'icoolkj_legal_lead@icoolkj.test', '13900010017', '1', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '法务经理(legal_approver+dept_approver)', '0', now(3), '0', now(3), 0
  union all select '3090000000000000018', '1000000000000000099', '1190000000000000011', 'icoolkj_it_lead', '何志强', '何志强', 'system', 'icoolkj_it_lead@icoolkj.test', '13900010018', '1', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', 'IT经理(it_approver+dept_approver)', '0', now(3), '0', now(3), 0
  union all select '3090000000000000019', '1000000000000000099', '1190000000000000011', 'icoolkj_it01', '徐浩然', '徐浩然', 'system', 'icoolkj_it01@icoolkj.test', '13900010019', '1', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '运维工程师', '0', now(3), '0', now(3), 0
  union all select '3090000000000000025', '1000000000000000099', '1190000000000000011', 'icoolkj_tech_dir', '杨启明', '杨启明', 'system', 'icoolkj_tech_dir@icoolkj.test', '13900010025', '1', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '技术总监(tech_director)', '0', now(3), '0', now(3), 0
  union all select '3090000000000000020', '1000000000000000099', '1190000000000000012', 'icoolkj_pur_lead', '冯晓薇', '冯晓薇', 'system', 'icoolkj_pur_lead@icoolkj.test', '13900010020', '2', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '采购经理(purchase_approver+dept_approver)', '0', now(3), '0', now(3), 0
  union all select '3090000000000000021', '1000000000000000099', '1190000000000000013', 'icoolkj_wh_lead', '曹国栋', '曹国栋', 'system', 'icoolkj_wh_lead@icoolkj.test', '13900010021', '1', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '仓储主管(warehouse_approver+dept_approver)', '0', now(3), '0', now(3), 0
  union all select '3090000000000000022', '1000000000000000099', '1190000000000000014', 'icoolkj_sales_lead', '沈悦', '沈悦', 'system', 'icoolkj_sales_lead@icoolkj.test', '13900010022', '2', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '销售主管(sales_approver+dept_approver)', '0', now(3), '0', now(3), 0
  union all select '3090000000000000023', '1000000000000000099', '1190000000000000014', 'icoolkj_sales_mgr', '韩立成', '韩立成', 'system', 'icoolkj_sales_mgr@icoolkj.test', '13900010023', '1', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '销售经理(sales_manager)', '0', now(3), '0', now(3), 0
  union all select '3090000000000000024', '1000000000000000099', '1190000000000000014', 'icoolkj_sales01', '朱明辉', '朱明辉', 'system', 'icoolkj_sales01@icoolkj.test', '13900010024', '1', null, @pwd, '0', '0', 0, null, null, null, now(3), '0', '销售代表·报价发起', '0', now(3), '0', now(3), 0
) x
where not exists (
  select 1 from sys_user t where t.id = x.id
);

-- ------------------------------------------
-- 6. 用户 ↔ 角色
-- ------------------------------------------
insert into sys_user_role (
  tenant_id, user_id, role_id
)
select * from (
  select '1000000000000000099' as tenant_id, '3090000000000000001' as user_id, '2090000000000000001' as role_id
  union all select '1000000000000000099', '3090000000000000002', '2090000000000000005'
  union all select '1000000000000000099', '3090000000000000003', '2090000000000000006'
  union all select '1000000000000000099', '3090000000000000004', '2090000000000000002'
  union all select '1000000000000000099', '3090000000000000005', '2090000000000000002'
  union all select '1000000000000000099', '3090000000000000006', '2090000000000000007'
  union all select '1000000000000000099', '3090000000000000007', '2090000000000000007'
  union all select '1000000000000000099', '3090000000000000008', '2090000000000000002'
  union all select '1000000000000000099', '3090000000000000009', '2090000000000000007'
  union all select '1000000000000000099', '3090000000000000010', '2090000000000000002'
  union all select '1000000000000000099', '3090000000000000011', '2090000000000000007'
  union all select '1000000000000000099', '3090000000000000012', '2090000000000000003'
  union all select '1000000000000000099', '3090000000000000012', '2090000000000000002'
  union all select '1000000000000000099', '3090000000000000013', '2090000000000000007'
  union all select '1000000000000000099', '3090000000000000013', '2090000000000000006'
  union all select '1000000000000000099', '3090000000000000014', '2090000000000000004'
  union all select '1000000000000000099', '3090000000000000014', '2090000000000000002'
  union all select '1000000000000000099', '3090000000000000015', '2090000000000000007'
  union all select '1000000000000000099', '3090000000000000016', '2090000000000000002'
  union all select '1000000000000000099', '3090000000000000016', '2090000000000000016'
  union all select '1000000000000000099', '3090000000000000026', '2090000000000000007'
  union all select '1000000000000000099', '3090000000000000017', '2090000000000000008'
  union all select '1000000000000000099', '3090000000000000017', '2090000000000000002'
  union all select '1000000000000000099', '3090000000000000018', '2090000000000000009'
  union all select '1000000000000000099', '3090000000000000018', '2090000000000000002'
  union all select '1000000000000000099', '3090000000000000019', '2090000000000000007'
  union all select '1000000000000000099', '3090000000000000025', '2090000000000000014'
  union all select '1000000000000000099', '3090000000000000020', '2090000000000000010'
  union all select '1000000000000000099', '3090000000000000020', '2090000000000000002'
  union all select '1000000000000000099', '3090000000000000021', '2090000000000000011'
  union all select '1000000000000000099', '3090000000000000021', '2090000000000000002'
  union all select '1000000000000000099', '3090000000000000022', '2090000000000000012'
  union all select '1000000000000000099', '3090000000000000022', '2090000000000000002'
  union all select '1000000000000000099', '3090000000000000023', '2090000000000000013'
  union all select '1000000000000000099', '3090000000000000024', '2090000000000000007'
) x
where not exists (
  select 1 from sys_user_role t where t.tenant_id = x.tenant_id and t.user_id = x.user_id and t.role_id = x.role_id
);

-- ------------------------------------------
-- 7. 用户 ↔ 岗位
-- ------------------------------------------
insert into sys_user_post (
  tenant_id, user_id, post_id
)
select * from (
  select '1000000000000000099' as tenant_id, '3090000000000000001' as user_id, '1290000000000000001' as post_id
  union all select '1000000000000000099', '3090000000000000002', '1290000000000000002'
  union all select '1000000000000000099', '3090000000000000003', '1290000000000000003'
  union all select '1000000000000000099', '3090000000000000004', '1290000000000000002'
  union all select '1000000000000000099', '3090000000000000005', '1290000000000000002'
  union all select '1000000000000000099', '3090000000000000006', '1290000000000000003'
  union all select '1000000000000000099', '3090000000000000007', '1290000000000000003'
  union all select '1000000000000000099', '3090000000000000008', '1290000000000000002'
  union all select '1000000000000000099', '3090000000000000009', '1290000000000000003'
  union all select '1000000000000000099', '3090000000000000010', '1290000000000000002'
  union all select '1000000000000000099', '3090000000000000011', '1290000000000000003'
  union all select '1000000000000000099', '3090000000000000012', '1290000000000000004'
  union all select '1000000000000000099', '3090000000000000013', '1290000000000000003'
  union all select '1000000000000000099', '3090000000000000014', '1290000000000000005'
  union all select '1000000000000000099', '3090000000000000015', '1290000000000000003'
  union all select '1000000000000000099', '3090000000000000016', '1290000000000000002'
  union all select '1000000000000000099', '3090000000000000026', '1290000000000000003'
  union all select '1000000000000000099', '3090000000000000017', '1290000000000000006'
  union all select '1000000000000000099', '3090000000000000018', '1290000000000000007'
  union all select '1000000000000000099', '3090000000000000019', '1290000000000000003'
  union all select '1000000000000000099', '3090000000000000025', '1290000000000000002'
  union all select '1000000000000000099', '3090000000000000020', '1290000000000000008'
  union all select '1000000000000000099', '3090000000000000021', '1290000000000000009'
  union all select '1000000000000000099', '3090000000000000022', '1290000000000000010'
  union all select '1000000000000000099', '3090000000000000023', '1290000000000000010'
  union all select '1000000000000000099', '3090000000000000024', '1290000000000000003'
) x
where not exists (
  select 1 from sys_user_post t where t.tenant_id = x.tenant_id and t.user_id = x.user_id and t.post_id = x.post_id
);

-- ------------------------------------------
-- 8. 角色菜单
--    tenant_admin：全部非 PLATFORM 菜单
--    其余业务角色：仪表盘 + 在线办公 + 工作流通用待办权限（够发起/审批/抄送）
-- ------------------------------------------
insert into sys_role_menu (tenant_id, role_id, menu_id)
select '1000000000000000099', '2090000000000000001', m.id
from sys_menu m
where m.deleted = 0
  and m.assign_scope <> 'PLATFORM'
  and not exists (
    select 1 from sys_role_menu rm
    where rm.tenant_id = '1000000000000000099'
      and rm.role_id = '2090000000000000001'
      and rm.menu_id = m.id
  );

-- 业务角色共用菜单：工作台 + 在线办公树 + 隐藏待办/已办/OA审批页
insert into sys_role_menu (tenant_id, role_id, menu_id)
select r.tenant_id, r.id, m.id
from sys_role r
cross join sys_menu m
where r.tenant_id = '1000000000000000099'
  and r.role_code in (
    'dept_approver', 'hr_approver', 'finance_approver',
    'general_manager', 'cc_receiver', 'employee',
    'legal_approver', 'it_approver', 'purchase_approver', 'warehouse_approver',
    'sales_approver', 'sales_manager', 'tech_director'
  )
  and r.deleted = 0
  and m.deleted = 0
  and m.id in (
    -- 仪表盘
    '4000010001000000000',
    -- 在线办公可见页 + 办理 F + 已发申请 + 审批跳转页（不含流程监控）
    '4000100000000000000', '4000100001000000000', '4000100001010000000',
    '4000100002000000000', '4000100002010000000', '4000100003000000000',
    '4000100003020000000', '4000100003010000000', '4000100004000000000',
    '4000100004010000000', '4000100004020000000', '4000100004030000000',
    '4000100004040000000', '4000100004050000000', '4000100004060000000',
    '4000100004070000000', '4000100004080000000', '4000100004090000000',
    '4000100004100000000',
    '4000100005000000000', '4000100005010000000', '4000100005020000000',
    '4000100006000000000', '4000100006010000000', '4000100006020000000',
    '4000100010000000000', '4000100010020000000',
    '4000100003030000000',
    '4000100099010000000', '4000100099020000000', '4000100099030000000',
    '4000100099040000000', '4000100099050000000',
    '4000100099060000000', '4000100099070000000',
    '4000100008000000000', '4000100008010000000', '4000100009000000000',
    -- OA 表单附件能力（无「文件管理」菜单页）：list=storage-options / upload / download
    '4000070003010000000', '4000070003020000000', '4000070003030000000'
  )
  and not exists (
    select 1 from sys_role_menu rm
    where rm.tenant_id = r.tenant_id and rm.role_id = r.id and rm.menu_id = m.id
  );

-- =============================================================================
-- 账号速查（tenantCode=icoolkj / 密码 StudyOnly@2026）
-- =============================================================================
-- icoolkj_admin       林志远     总经办         tenant_admin
-- icoolkj_ceo         张伟       总经办         general_manager          userId=3090000000000000002
-- icoolkj_cc01        孙雨欣     总经办         cc_receiver              userId=3090000000000000003
-- icoolkj_rd_lead     陈浩然     研发中心       dept_approver
-- icoolkj_fe_lead     刘晓彤     前端组         dept_approver
-- icoolkj_fe01/02     李明轩等   前端组         employee（常发起）
-- icoolkj_be_lead     王俊杰     后端组         dept_approver
-- icoolkj_be01        周子墨     后端组         employee
-- icoolkj_pd_lead     赵思远     产品部         dept_approver
-- icoolkj_pd01        钱雨桐     产品部         employee
-- icoolkj_hr_lead     周雅琴     人力资源部     hr_approver + dept_approver
-- icoolkj_hr01        吴倩       人力资源部     employee + cc_receiver
-- icoolkj_fin_lead    吴嘉慧     财务部         finance_approver + dept_approver
-- icoolkj_fin01       郑晨曦     财务部         employee
-- icoolkj_adm_lead    郑立群     行政部         dept_approver + admin_officer  userId=3090000000000000016
-- icoolkj_adm01       蒋婉清     行政部         employee
-- icoolkj_legal_lead  马文博     法务部         legal_approver
-- icoolkj_it_lead     何志强     IT部           it_approver
-- icoolkj_it01        徐浩然     IT部           employee
-- icoolkj_tech_dir    杨启明     IT部           tech_director
-- icoolkj_pur_lead    冯晓薇     采购部         purchase_approver
-- icoolkj_wh_lead     曹国栋     仓储部         warehouse_approver
-- icoolkj_sales_lead  沈悦       销售部         sales_approver
-- icoolkj_sales_mgr   韩立成     销售部         sales_manager
-- icoolkj_sales01     朱明辉     销售部         employee（报价发起）
--
-- 策略配置示例：
--   starter_dept_role → {"roleCodes":["dept_approver"]}
--   role(HR)          → {"roleCodes":["hr_approver"]}
--   role(财务)        → {"roleCodes":["finance_approver"]}
--   role(法务)        → {"roleCodes":["legal_approver"]}
--   role(IT)          → {"roleCodes":["it_approver"]}
--   role(采购)        → {"roleCodes":["purchase_approver"]}
--   role(仓储)        → {"roleCodes":["warehouse_approver"]}
--   role(销售主管)    → {"roleCodes":["sales_approver"]}
--   role(销售经理)    → {"roleCodes":["sales_manager"]}
--   role(技术总监)    → {"roleCodes":["tech_director"]}
--   role(总经理)      → {"roleCodes":["general_manager"]}
--   fixed_user(张伟)  → {"userId":"3090000000000000002"}
--   fixed_user(抄送)  → {"userId":"3090000000000000003"}
--
-- 【外网测试】密码均为 StudyOnly@2026，仅用于隔离测试环境；正式交付前请改密或轮换。
-- =============================================================================

-- =============================================================================
-- C. icoolkj 完整工作流配置（design + 审批规则 + 已启用模板）
-- =============================================================================
set @icoolkj_tid := '1000000000000000099';

set @cfg_prefix := '{"startAcl":{"userIds":[],"deptIds":[],"roleCodes":["employee"],"postIds":[],"tenantIds":[]},"behavior":{"allowWithdraw":true,"commentRequired":false},"notify":{"onTodo":true,"onReject":true,"onComplete":true,"onCc":true},"design":';
set @cfg_global := '{"startAcl":{"userIds":[],"deptIds":[],"roleCodes":["employee"],"postIds":[],"tenantIds":[]},"behavior":{"allowWithdraw":true,"commentRequired":false},"notify":{"onTodo":true,"onReject":true,"onComplete":true,"onCc":true}}';
set @bpmn_stub := concat(
  '<?xml version="1.0" encoding="UTF-8"?>',
  '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" ',
  'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ',
  'xmlns:flowable="http://flowable.org/bpmn" ',
  'targetNamespace="http://www.icoolkj.com/office-lab">',
  '<process id="__KEY__" name="__KEY__" isExecutable="true">',
  '<startEvent id="startEvent"/><endEvent id="endApproved"/>',
  '<sequenceFlow id="flowStart" sourceRef="startEvent" targetRef="endApproved"/>',
  '</process></definitions>'
);

set @design_oa_expense := '{"schemaVersion":1,"nodes":[{"nodeType":"approve","taskDefinitionKey":"deptLeaderApprove","name":"直属部门主管审批","variableName":"deptLeaderUserId","strategyType":"starter_dept_role","strategyConfig":"{\\"roleCodes\\":[\\"dept_approver\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"},{"nodeType":"condition","taskDefinitionKey":"amountGate","name":"按报销金额分流","variableName":"amountGate","strategyType":"expression","strategyConfig":"{}","resolveScope":"biz_tenant","sortNo":2,"branches":[{"id":"lte2k","name":"金额≤2000","expression":"UEL{amount <= 2000}","isDefault":false,"nodes":[]},{"id":"default","name":"金额＞2000","expression":"","isDefault":true,"nodes":[{"nodeType":"approve","taskDefinitionKey":"gmApprove","name":"总经理终审","variableName":"gmUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"general_manager\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"}]}]},{"nodeType":"approve","taskDefinitionKey":"financeApprove","name":"财务核验发票与预算","variableName":"financeUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"finance_approver\\"]}","resolveScope":"biz_tenant","sortNo":3,"passMode":"sequential"},{"nodeType":"service","taskDefinitionKey":"payService","name":"财务系统打款并生成凭证","variableName":"payServiceVar","strategyType":"expression","strategyConfig":"{\\"delegateExpression\\":\\"UEL{workflowNoOpService.execute(execution)}\\"}","resolveScope":"biz_tenant","sortNo":4},{"nodeType":"cc","taskDefinitionKey":"finalCc","name":"抄送企划","variableName":"finalCcUserId","strategyType":"fixed_user","strategyConfig":"{\\"userId\\":\\"3090000000000000003\\"}","resolveScope":"biz_tenant","sortNo":5}]}';

set @design_oa_leave := '{"schemaVersion":1,"nodes":[{"nodeType":"approve","taskDefinitionKey":"deptLeaderApprove","name":"直属领导审批","variableName":"deptLeaderUserId","strategyType":"starter_dept_role","strategyConfig":"{\\"roleCodes\\":[\\"dept_approver\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"},{"nodeType":"condition","taskDefinitionKey":"leaveTypeGate","name":"按假期类型分流","variableName":"leaveTypeGate","strategyType":"expression","strategyConfig":"{}","resolveScope":"biz_tenant","sortNo":2,"branches":[{"id":"special","name":"病假/婚假","expression":"UEL{leaveType == ''sick'' || leaveType == ''marriage''}","isDefault":false,"nodes":[{"nodeType":"approve","taskDefinitionKey":"hrReview","name":"HR复核并校验证明材料","variableName":"hrReviewUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"hr_approver\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"}]},{"id":"default","name":"事假/年假","expression":"","isDefault":true,"nodes":[{"nodeType":"approve","taskDefinitionKey":"hrRecord","name":"HR人事备案","variableName":"hrRecordUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"hr_approver\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"}]}]},{"nodeType":"service","taskDefinitionKey":"attendanceSync","name":"同步考勤系统","variableName":"attendanceSyncVar","strategyType":"expression","strategyConfig":"{\\"delegateExpression\\":\\"UEL{workflowNoOpService.execute(execution)}\\"}","resolveScope":"biz_tenant","sortNo":3},{"nodeType":"cc","taskDefinitionKey":"finalCc","name":"抄送企划","variableName":"finalCcUserId","strategyType":"fixed_user","strategyConfig":"{\\"userId\\":\\"3090000000000000003\\"}","resolveScope":"biz_tenant","sortNo":4}]}';

set @design_oa_travel := '{"schemaVersion":1,"nodes":[{"nodeType":"approve","taskDefinitionKey":"deptLeaderApprove","name":"部门负责人审批","variableName":"deptLeaderUserId","strategyType":"starter_dept_role","strategyConfig":"{\\"roleCodes\\":[\\"dept_approver\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"},{"nodeType":"condition","taskDefinitionKey":"budgetGate","name":"按出差预算分流","variableName":"budgetGate","strategyType":"expression","strategyConfig":"{}","resolveScope":"biz_tenant","sortNo":2,"branches":[{"id":"lte5k","name":"预算≤5000","expression":"UEL{amount <= 5000}","isDefault":false,"nodes":[]},{"id":"default","name":"预算＞5000","expression":"","isDefault":true,"nodes":[{"nodeType":"approve","taskDefinitionKey":"gmApprove","name":"总经理审批","variableName":"gmUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"general_manager\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"}]}]},{"nodeType":"approve","taskDefinitionKey":"financeBudget","name":"财务预算占用登记","variableName":"financeBudgetUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"finance_approver\\"]}","resolveScope":"biz_tenant","sortNo":3,"passMode":"sequential"},{"nodeType":"cc","taskDefinitionKey":"finalCc","name":"抄送企划","variableName":"finalCcUserId","strategyType":"fixed_user","strategyConfig":"{\\"userId\\":\\"3090000000000000003\\"}","resolveScope":"biz_tenant","sortNo":4}]}';

set @design_oa_contract := '{"schemaVersion":1,"nodes":[{"nodeType":"approve","taskDefinitionKey":"bizLeadApprove","name":"业务负责人初审","variableName":"bizLeadUserId","strategyType":"starter_dept_role","strategyConfig":"{\\"roleCodes\\":[\\"dept_approver\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"},{"nodeType":"parallel","taskDefinitionKey":"parGate","name":"财务法务并行审核","variableName":"parGate","strategyType":"expression","strategyConfig":"{}","resolveScope":"biz_tenant","sortNo":2,"branches":[{"id":"p_fin","name":"财务","nodes":[{"nodeType":"approve","taskDefinitionKey":"financeApprove","name":"财务审核价格账期回款风险","variableName":"financeUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"finance_approver\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"}]},{"id":"p_legal","name":"法务","nodes":[{"nodeType":"approve","taskDefinitionKey":"legalApprove","name":"法务审核合同条款合规风险","variableName":"legalUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"legal_approver\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"}]}]},{"nodeType":"approve","taskDefinitionKey":"gmApprove","name":"总经理终审","variableName":"gmUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"general_manager\\"]}","resolveScope":"biz_tenant","sortNo":3,"passMode":"sequential"},{"nodeType":"approve","taskDefinitionKey":"adminSeal","name":"行政用印登记与档案归档","variableName":"adminSealUserId","strategyType":"fixed_user","strategyConfig":"{\\"userId\\":\\"3090000000000000016\\"}","resolveScope":"biz_tenant","sortNo":4,"passMode":"sequential"},{"nodeType":"cc","taskDefinitionKey":"finalCc","name":"抄送企划","variableName":"finalCcUserId","strategyType":"fixed_user","strategyConfig":"{\\"userId\\":\\"3090000000000000003\\"}","resolveScope":"biz_tenant","sortNo":5}]}';

set @design_oa_onboard := '{"schemaVersion":1,"nodes":[{"nodeType":"approve","taskDefinitionKey":"hrContract","name":"HR录入档案签订劳动合同","variableName":"hrContractUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"hr_approver\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"},{"nodeType":"parallel","taskDefinitionKey":"parGate","name":"三部门协同入职","variableName":"parGate","strategyType":"expression","strategyConfig":"{}","resolveScope":"biz_tenant","sortNo":2,"branches":[{"id":"p_it","name":"IT部","nodes":[{"nodeType":"approve","taskDefinitionKey":"itProvision","name":"开通邮箱账号与权限","variableName":"itUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"it_approver\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"}]},{"id":"p_adm","name":"行政部","nodes":[{"nodeType":"approve","taskDefinitionKey":"adminAsset","name":"发放工牌工位办公物资","variableName":"adminUserId","strategyType":"fixed_user","strategyConfig":"{\\"userId\\":\\"3090000000000000016\\"}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"}]},{"id":"p_fin","name":"财务部","nodes":[{"nodeType":"approve","taskDefinitionKey":"financeOnboard","name":"录入薪资卡与社保参保","variableName":"financeUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"finance_approver\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"}]}]},{"nodeType":"approve","taskDefinitionKey":"trainingConfirm","name":"新人入职培训确认","variableName":"trainingUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"hr_approver\\"]}","resolveScope":"biz_tenant","sortNo":3,"passMode":"sequential"},{"nodeType":"cc","taskDefinitionKey":"finalCc","name":"抄送企划","variableName":"finalCcUserId","strategyType":"fixed_user","strategyConfig":"{\\"userId\\":\\"3090000000000000003\\"}","resolveScope":"biz_tenant","sortNo":4}]}';

set @design_oa_offboard := '{"schemaVersion":1,"nodes":[{"nodeType":"approve","taskDefinitionKey":"deptHandover","name":"直属领导审批与工作交接确认","variableName":"deptHandoverUserId","strategyType":"starter_dept_role","strategyConfig":"{\\"roleCodes\\":[\\"dept_approver\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"},{"nodeType":"parallel","taskDefinitionKey":"parGate","name":"并行回收资产与权限","variableName":"parGate","strategyType":"expression","strategyConfig":"{}","resolveScope":"biz_tenant","sortNo":2,"branches":[{"id":"p_it","name":"IT","nodes":[{"nodeType":"approve","taskDefinitionKey":"itRevoke","name":"回收账号注销系统权限","variableName":"itUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"it_approver\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"}]},{"id":"p_adm","name":"行政","nodes":[{"nodeType":"approve","taskDefinitionKey":"adminRevoke","name":"回收电脑工牌办公设备","variableName":"adminUserId","strategyType":"fixed_user","strategyConfig":"{\\"userId\\":\\"3090000000000000016\\"}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"}]},{"id":"p_fin","name":"财务","nodes":[{"nodeType":"approve","taskDefinitionKey":"financeSettle","name":"核算未发薪资与借款清算","variableName":"financeUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"finance_approver\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"}]}]},{"nodeType":"approve","taskDefinitionKey":"hrExit","name":"HR办理离职备案与社保停缴","variableName":"hrExitUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"hr_approver\\"]}","resolveScope":"biz_tenant","sortNo":3,"passMode":"sequential"},{"nodeType":"cc","taskDefinitionKey":"finalCc","name":"抄送企划","variableName":"finalCcUserId","strategyType":"fixed_user","strategyConfig":"{\\"userId\\":\\"3090000000000000003\\"}","resolveScope":"biz_tenant","sortNo":4}]}';

set @design_oa_purchase := '{"schemaVersion":1,"nodes":[{"nodeType":"approve","taskDefinitionKey":"deptLeaderApprove","name":"部门负责人审批需求合理性","variableName":"deptLeaderUserId","strategyType":"starter_dept_role","strategyConfig":"{\\"roleCodes\\":[\\"dept_approver\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"},{"nodeType":"approve","taskDefinitionKey":"purchaseQuote","name":"采购部比价出具报价单","variableName":"purchaseQuoteUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"purchase_approver\\"]}","resolveScope":"biz_tenant","sortNo":2,"passMode":"sequential"},{"nodeType":"condition","taskDefinitionKey":"amountGate","name":"按采购金额分流","variableName":"amountGate","strategyType":"expression","strategyConfig":"{}","resolveScope":"biz_tenant","sortNo":3,"branches":[{"id":"small","name":"小额采购≤10000","expression":"UEL{amount <= 10000}","isDefault":false,"nodes":[{"nodeType":"approve","taskDefinitionKey":"purchaseConfirm","name":"采购负责人确认","variableName":"purchaseConfirmUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"purchase_approver\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"}]},{"id":"default","name":"大额采购","expression":"","isDefault":true,"nodes":[{"nodeType":"approve","taskDefinitionKey":"gmApprove","name":"总经理审批","variableName":"gmUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"general_manager\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"}]}]},{"nodeType":"approve","taskDefinitionKey":"financeLock","name":"财务锁定采购预算","variableName":"financeLockUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"finance_approver\\"]}","resolveScope":"biz_tenant","sortNo":4,"passMode":"sequential"},{"nodeType":"service","taskDefinitionKey":"orderService","name":"生成采购订单","variableName":"orderServiceVar","strategyType":"expression","strategyConfig":"{\\"delegateExpression\\":\\"UEL{workflowNoOpService.execute(execution)}\\"}","resolveScope":"biz_tenant","sortNo":5},{"nodeType":"approve","taskDefinitionKey":"warehouseRecv","name":"仓库收货验收","variableName":"warehouseUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"warehouse_approver\\"]}","resolveScope":"biz_tenant","sortNo":6,"passMode":"sequential"},{"nodeType":"cc","taskDefinitionKey":"finalCc","name":"抄送企划","variableName":"finalCcUserId","strategyType":"fixed_user","strategyConfig":"{\\"userId\\":\\"3090000000000000003\\"}","resolveScope":"biz_tenant","sortNo":7}]}';

set @design_oa_seal := '{"schemaVersion":1,"nodes":[{"nodeType":"approve","taskDefinitionKey":"deptLeaderApprove","name":"部门负责人审核文件内容","variableName":"deptLeaderUserId","strategyType":"starter_dept_role","strategyConfig":"{\\"roleCodes\\":[\\"dept_approver\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"},{"nodeType":"condition","taskDefinitionKey":"fileTypeGate","name":"按文件类型分流","variableName":"fileTypeGate","strategyType":"expression","strategyConfig":"{}","resolveScope":"biz_tenant","sortNo":2,"branches":[{"id":"normal","name":"普通通知文件","expression":"UEL{fileType == ''normal''}","isDefault":false,"nodes":[]},{"id":"default","name":"合同/对外承诺","expression":"","isDefault":true,"nodes":[{"nodeType":"approve","taskDefinitionKey":"legalApprove","name":"法务复核","variableName":"legalUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"legal_approver\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"},{"nodeType":"approve","taskDefinitionKey":"gmApprove","name":"总经理审批","variableName":"gmUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"general_manager\\"]}","resolveScope":"biz_tenant","sortNo":2,"passMode":"sequential"}]}]},{"nodeType":"approve","taskDefinitionKey":"adminSeal","name":"行政印章登记盖章","variableName":"adminSealUserId","strategyType":"fixed_user","strategyConfig":"{\\"userId\\":\\"3090000000000000016\\"}","resolveScope":"biz_tenant","sortNo":3,"passMode":"sequential"},{"nodeType":"cc","taskDefinitionKey":"finalCc","name":"抄送企划","variableName":"finalCcUserId","strategyType":"fixed_user","strategyConfig":"{\\"userId\\":\\"3090000000000000003\\"}","resolveScope":"biz_tenant","sortNo":4}]}';

set @design_oa_it_access := '{"schemaVersion":1,"nodes":[{"nodeType":"approve","taskDefinitionKey":"deptLeaderApprove","name":"直属领导确认业务所需权限","variableName":"deptLeaderUserId","strategyType":"starter_dept_role","strategyConfig":"{\\"roleCodes\\":[\\"dept_approver\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"},{"nodeType":"approve","taskDefinitionKey":"itCheck","name":"IT管理员校验权限范围","variableName":"itCheckUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"it_approver\\"]}","resolveScope":"biz_tenant","sortNo":2,"passMode":"sequential"},{"nodeType":"condition","taskDefinitionKey":"privGate","name":"按权限等级分流","variableName":"privGate","strategyType":"expression","strategyConfig":"{}","resolveScope":"biz_tenant","sortNo":3,"branches":[{"id":"normal","name":"普通功能权限","expression":"UEL{privilegeLevel == ''normal''}","isDefault":false,"nodes":[]},{"id":"default","name":"核心数据/管理员权限","expression":"","isDefault":true,"nodes":[{"nodeType":"approve","taskDefinitionKey":"techDirApprove","name":"技术总监审批","variableName":"techDirUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"tech_director\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"}]}]},{"nodeType":"service","taskDefinitionKey":"openAccess","name":"后台自动开通账号权限","variableName":"openAccessVar","strategyType":"expression","strategyConfig":"{\\"delegateExpression\\":\\"UEL{workflowNoOpService.execute(execution)}\\"}","resolveScope":"biz_tenant","sortNo":4},{"nodeType":"approve","taskDefinitionKey":"userConfirm","name":"申请人确认权限可用","variableName":"userConfirmUserId","strategyType":"expression","strategyConfig":"{\\"expr\\":\\"starter\\"}","resolveScope":"biz_tenant","sortNo":5,"passMode":"sequential"},{"nodeType":"cc","taskDefinitionKey":"finalCc","name":"抄送企划","variableName":"finalCcUserId","strategyType":"fixed_user","strategyConfig":"{\\"userId\\":\\"3090000000000000003\\"}","resolveScope":"biz_tenant","sortNo":6}]}';

set @design_oa_quote := '{"schemaVersion":1,"nodes":[{"nodeType":"approve","taskDefinitionKey":"salesLeadApprove","name":"销售主管初审","variableName":"salesLeadUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"sales_approver\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"},{"nodeType":"condition","taskDefinitionKey":"discountGate","name":"按折扣比例分流","variableName":"discountGate","strategyType":"expression","strategyConfig":"{}","resolveScope":"biz_tenant","sortNo":2,"branches":[{"id":"gte95","name":"折扣≥95折","expression":"UEL{discount >= 95}","isDefault":false,"nodes":[]},{"id":"mid","name":"90折＜折扣＜95折","expression":"UEL{discount > 90 && discount < 95}","isDefault":false,"nodes":[{"nodeType":"approve","taskDefinitionKey":"salesMgrApprove","name":"销售经理审批","variableName":"salesMgrUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"sales_manager\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"}]},{"id":"default","name":"折扣≤90折","expression":"","isDefault":true,"nodes":[{"nodeType":"approve","taskDefinitionKey":"gmApprove","name":"总经理终审","variableName":"gmUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"general_manager\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"}]}]},{"nodeType":"approve","taskDefinitionKey":"financeMargin","name":"财务核对毛利底价校验","variableName":"financeMarginUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"finance_approver\\"]}","resolveScope":"biz_tenant","sortNo":3,"passMode":"sequential"},{"nodeType":"service","taskDefinitionKey":"quoteArchive","name":"生成正式报价单归档","variableName":"quoteArchiveVar","strategyType":"expression","strategyConfig":"{\\"delegateExpression\\":\\"UEL{workflowNoOpService.execute(execution)}\\"}","resolveScope":"biz_tenant","sortNo":4},{"nodeType":"cc","taskDefinitionKey":"finalCc","name":"抄送企划","variableName":"finalCcUserId","strategyType":"fixed_user","strategyConfig":"{\\"userId\\":\\"3090000000000000003\\"}","resolveScope":"biz_tenant","sortNo":5}]}';

-- Flowable UEL：种子写 UEL{...}，再 REPLACE 成 dollar+brace 表达式（勿在本文件字面量写 dollar-brace，Flyway 会当占位符）
set @uel_open := concat('$', '{');
set @design_oa_expense := replace(@design_oa_expense, 'UEL{', @uel_open);
set @design_oa_leave := replace(@design_oa_leave, 'UEL{', @uel_open);
set @design_oa_travel := replace(@design_oa_travel, 'UEL{', @uel_open);
set @design_oa_purchase := replace(@design_oa_purchase, 'UEL{', @uel_open);
set @design_oa_seal := replace(@design_oa_seal, 'UEL{', @uel_open);
set @design_oa_it_access := replace(@design_oa_it_access, 'UEL{', @uel_open);
set @design_oa_quote := replace(@design_oa_quote, 'UEL{', @uel_open);

insert into wf_model (
  id, tenant_id, model_key, model_name, category_name, biz_type, form_type, form_route, latest_version, status, process_config, source_model_id, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '9090000000000000009' as id, @icoolkj_tid as tenant_id, 'oa_leave' as model_key, '请假申请' as model_name, '通用办事' as category_name, 'oa_leave' as biz_type, 'system' as form_type, '/office/approve' as form_route, 1 as latest_version, '0' as status, concat(@cfg_prefix, @design_oa_leave, '}') as process_config, '9000000000000000002' as source_model_id, 'icoolkj 标准请假' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '9090000000000000012', @icoolkj_tid, 'oa_expense', '费用报销', '通用办事', 'oa_expense', 'system', '/office/approve', 1, '0', concat(@cfg_prefix, @design_oa_expense, '}'), '9090000000000000011', 'icoolkj 标准费用报销', '0', now(3), '0', now(3), 0
  union all select '9090000000000000014', @icoolkj_tid, 'oa_travel', '出差申请', '通用办事', 'oa_travel', 'system', '/office/approve', 1, '0', concat(@cfg_prefix, @design_oa_travel, '}'), '9090000000000000013', 'icoolkj 标准出差申请', '0', now(3), '0', now(3), 0
  union all select '9090000000000000005', @icoolkj_tid, 'oa_contract', '合同评审', '通用办事', 'oa_contract', 'system', '/office/approve', 1, '0', concat(@cfg_prefix, @design_oa_contract, '}'), '9090000000000000001', 'icoolkj 标准合同评审', '0', now(3), '0', now(3), 0
  union all select '9090000000000000006', @icoolkj_tid, 'oa_onboard', '员工入职', '通用办事', 'oa_onboard', 'system', '/office/approve', 1, '0', concat(@cfg_prefix, @design_oa_onboard, '}'), '9090000000000000002', 'icoolkj 标准员工入职', '0', now(3), '0', now(3), 0
  union all select '9090000000000000007', @icoolkj_tid, 'oa_offboard', '员工离职', '通用办事', 'oa_offboard', 'system', '/office/approve', 1, '0', concat(@cfg_prefix, @design_oa_offboard, '}'), '9090000000000000003', 'icoolkj 标准员工离职', '0', now(3), '0', now(3), 0
  union all select '9090000000000000008', @icoolkj_tid, 'oa_purchase', '采购申请', '通用办事', 'oa_purchase', 'system', '/office/approve', 1, '0', concat(@cfg_prefix, @design_oa_purchase, '}'), '9090000000000000004', 'icoolkj 标准采购申请', '0', now(3), '0', now(3), 0
  union all select '9090000000000000018', @icoolkj_tid, 'oa_seal', '用印申请', '通用办事', 'oa_seal', 'system', '/office/approve', 1, '0', concat(@cfg_prefix, @design_oa_seal, '}'), '9090000000000000015', 'icoolkj 标准用印申请', '0', now(3), '0', now(3), 0
  union all select '9090000000000000019', @icoolkj_tid, 'oa_it_access', 'IT权限开通', '通用办事', 'oa_it_access', 'system', '/office/approve', 1, '0', concat(@cfg_prefix, @design_oa_it_access, '}'), '9090000000000000016', 'icoolkj 标准IT权限开通', '0', now(3), '0', now(3), 0
  union all select '9090000000000000020', @icoolkj_tid, 'oa_quote', '销售报价审批', '通用办事', 'oa_quote', 'system', '/office/approve', 1, '0', concat(@cfg_prefix, @design_oa_quote, '}'), '9090000000000000017', 'icoolkj 标准销售报价审批', '0', now(3), '0', now(3), 0
) x
where not exists (
  select 1 from wf_model t where t.id = x.id or (t.tenant_id = x.tenant_id and t.model_key = x.model_key)
);



insert into wf_model_version (
  id, tenant_id, model_id, version_no, bpmn_xml, design_json, process_config_json, deployment_id, process_definition_id, process_definition_key, published, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '9090000000000000309' as id, @icoolkj_tid as tenant_id, '9090000000000000009' as model_id, 1 as version_no, replace(@bpmn_stub, '__KEY__', 'oa_leave') as bpmn_xml, @design_oa_leave as design_json, @cfg_global as process_config_json, null as deployment_id, null as process_definition_id, 'oa_leave' as process_definition_key, 1 as published, 'office-lab icoolkj leave' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '9090000000000000312', @icoolkj_tid, '9090000000000000012', 1, replace(@bpmn_stub, '__KEY__', 'oa_expense'), @design_oa_expense, @cfg_global, null, null, 'oa_expense', 1, 'office-lab icoolkj', '0', now(3), '0', now(3), 0
  union all select '9090000000000000314', @icoolkj_tid, '9090000000000000014', 1, replace(@bpmn_stub, '__KEY__', 'oa_travel'), @design_oa_travel, @cfg_global, null, null, 'oa_travel', 1, 'office-lab icoolkj', '0', now(3), '0', now(3), 0
  union all select '9090000000000000305', @icoolkj_tid, '9090000000000000005', 1, replace(@bpmn_stub, '__KEY__', 'oa_contract'), @design_oa_contract, @cfg_global, null, null, 'oa_contract', 1, 'office-lab icoolkj', '0', now(3), '0', now(3), 0
  union all select '9090000000000000306', @icoolkj_tid, '9090000000000000006', 1, replace(@bpmn_stub, '__KEY__', 'oa_onboard'), @design_oa_onboard, @cfg_global, null, null, 'oa_onboard', 1, 'office-lab icoolkj', '0', now(3), '0', now(3), 0
  union all select '9090000000000000307', @icoolkj_tid, '9090000000000000007', 1, replace(@bpmn_stub, '__KEY__', 'oa_offboard'), @design_oa_offboard, @cfg_global, null, null, 'oa_offboard', 1, 'office-lab icoolkj', '0', now(3), '0', now(3), 0
  union all select '9090000000000000308', @icoolkj_tid, '9090000000000000008', 1, replace(@bpmn_stub, '__KEY__', 'oa_purchase'), @design_oa_purchase, @cfg_global, null, null, 'oa_purchase', 1, 'office-lab icoolkj', '0', now(3), '0', now(3), 0
  union all select '9090000000000000318', @icoolkj_tid, '9090000000000000018', 1, replace(@bpmn_stub, '__KEY__', 'oa_seal'), @design_oa_seal, @cfg_global, null, null, 'oa_seal', 1, 'office-lab icoolkj', '0', now(3), '0', now(3), 0
  union all select '9090000000000000319', @icoolkj_tid, '9090000000000000019', 1, replace(@bpmn_stub, '__KEY__', 'oa_it_access'), @design_oa_it_access, @cfg_global, null, null, 'oa_it_access', 1, 'office-lab icoolkj', '0', now(3), '0', now(3), 0
  union all select '9090000000000000320', @icoolkj_tid, '9090000000000000020', 1, replace(@bpmn_stub, '__KEY__', 'oa_quote'), @design_oa_quote, @cfg_global, null, null, 'oa_quote', 1, 'office-lab icoolkj', '0', now(3), '0', now(3), 0
) x
where not exists (
  select 1 from wf_model_version t where t.id = x.id or (t.model_id = x.model_id and t.version_no = x.version_no)
);



insert into wf_approver_rule (
  id, tenant_id, model_id, task_definition_key, variable_name, strategy_type, strategy_config, resolve_scope, sort_no, status, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '9090000000000000249' as id, '1000000000000000099' as tenant_id, '9090000000000000009' as model_id, 'deptLeaderApprove' as task_definition_key, 'deptLeaderUserId' as variable_name, 'starter_dept_role' as strategy_type, '{"roleCodes":["dept_approver"]}' as strategy_config, 'biz_tenant' as resolve_scope, 1 as sort_no, '0' as status, null as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '9090000000000000250', '1000000000000000099', '9090000000000000009', 'hrReview', 'hrReviewUserId', 'role', '{"roleCodes":["hr_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000251', '1000000000000000099', '9090000000000000009', 'hrRecord', 'hrRecordUserId', 'role', '{"roleCodes":["hr_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000252', '1000000000000000099', '9090000000000000009', 'finalCc', 'finalCcUserId', 'fixed_user', '{"userId":"3090000000000000003"}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000253', '1000000000000000099', '9090000000000000012', 'deptLeaderApprove', 'deptLeaderUserId', 'starter_dept_role', '{"roleCodes":["dept_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000254', '1000000000000000099', '9090000000000000012', 'gmApprove', 'gmUserId', 'role', '{"roleCodes":["general_manager"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000255', '1000000000000000099', '9090000000000000012', 'financeApprove', 'financeUserId', 'role', '{"roleCodes":["finance_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000256', '1000000000000000099', '9090000000000000012', 'finalCc', 'finalCcUserId', 'fixed_user', '{"userId":"3090000000000000003"}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000257', '1000000000000000099', '9090000000000000014', 'deptLeaderApprove', 'deptLeaderUserId', 'starter_dept_role', '{"roleCodes":["dept_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000258', '1000000000000000099', '9090000000000000014', 'gmApprove', 'gmUserId', 'role', '{"roleCodes":["general_manager"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000259', '1000000000000000099', '9090000000000000014', 'financeBudget', 'financeBudgetUserId', 'role', '{"roleCodes":["finance_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000260', '1000000000000000099', '9090000000000000014', 'finalCc', 'finalCcUserId', 'fixed_user', '{"userId":"3090000000000000003"}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000261', '1000000000000000099', '9090000000000000005', 'bizLeadApprove', 'bizLeadUserId', 'starter_dept_role', '{"roleCodes":["dept_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000262', '1000000000000000099', '9090000000000000005', 'financeApprove', 'financeUserId', 'role', '{"roleCodes":["finance_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000263', '1000000000000000099', '9090000000000000005', 'legalApprove', 'legalUserId', 'role', '{"roleCodes":["legal_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000264', '1000000000000000099', '9090000000000000005', 'gmApprove', 'gmUserId', 'role', '{"roleCodes":["general_manager"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000265', '1000000000000000099', '9090000000000000005', 'adminSeal', 'adminSealUserId', 'fixed_user', '{"userId":"3090000000000000016"}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000266', '1000000000000000099', '9090000000000000005', 'finalCc', 'finalCcUserId', 'fixed_user', '{"userId":"3090000000000000003"}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000267', '1000000000000000099', '9090000000000000006', 'hrContract', 'hrContractUserId', 'role', '{"roleCodes":["hr_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000268', '1000000000000000099', '9090000000000000006', 'itProvision', 'itUserId', 'role', '{"roleCodes":["it_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000269', '1000000000000000099', '9090000000000000006', 'adminAsset', 'adminUserId', 'fixed_user', '{"userId":"3090000000000000016"}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000270', '1000000000000000099', '9090000000000000006', 'financeOnboard', 'financeUserId', 'role', '{"roleCodes":["finance_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000271', '1000000000000000099', '9090000000000000006', 'trainingConfirm', 'trainingUserId', 'role', '{"roleCodes":["hr_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000272', '1000000000000000099', '9090000000000000006', 'finalCc', 'finalCcUserId', 'fixed_user', '{"userId":"3090000000000000003"}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000273', '1000000000000000099', '9090000000000000007', 'deptHandover', 'deptHandoverUserId', 'starter_dept_role', '{"roleCodes":["dept_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000274', '1000000000000000099', '9090000000000000007', 'itRevoke', 'itUserId', 'role', '{"roleCodes":["it_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000275', '1000000000000000099', '9090000000000000007', 'adminRevoke', 'adminUserId', 'fixed_user', '{"userId":"3090000000000000016"}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000276', '1000000000000000099', '9090000000000000007', 'financeSettle', 'financeUserId', 'role', '{"roleCodes":["finance_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000277', '1000000000000000099', '9090000000000000007', 'hrExit', 'hrExitUserId', 'role', '{"roleCodes":["hr_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000278', '1000000000000000099', '9090000000000000007', 'finalCc', 'finalCcUserId', 'fixed_user', '{"userId":"3090000000000000003"}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000279', '1000000000000000099', '9090000000000000008', 'deptLeaderApprove', 'deptLeaderUserId', 'starter_dept_role', '{"roleCodes":["dept_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000280', '1000000000000000099', '9090000000000000008', 'purchaseQuote', 'purchaseQuoteUserId', 'role', '{"roleCodes":["purchase_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000281', '1000000000000000099', '9090000000000000008', 'purchaseConfirm', 'purchaseConfirmUserId', 'role', '{"roleCodes":["purchase_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000282', '1000000000000000099', '9090000000000000008', 'gmApprove', 'gmUserId', 'role', '{"roleCodes":["general_manager"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000283', '1000000000000000099', '9090000000000000008', 'financeLock', 'financeLockUserId', 'role', '{"roleCodes":["finance_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000284', '1000000000000000099', '9090000000000000008', 'warehouseRecv', 'warehouseUserId', 'role', '{"roleCodes":["warehouse_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000285', '1000000000000000099', '9090000000000000008', 'finalCc', 'finalCcUserId', 'fixed_user', '{"userId":"3090000000000000003"}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000286', '1000000000000000099', '9090000000000000018', 'deptLeaderApprove', 'deptLeaderUserId', 'starter_dept_role', '{"roleCodes":["dept_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000287', '1000000000000000099', '9090000000000000018', 'legalApprove', 'legalUserId', 'role', '{"roleCodes":["legal_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000288', '1000000000000000099', '9090000000000000018', 'gmApprove', 'gmUserId', 'role', '{"roleCodes":["general_manager"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000289', '1000000000000000099', '9090000000000000018', 'adminSeal', 'adminSealUserId', 'fixed_user', '{"userId":"3090000000000000016"}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000290', '1000000000000000099', '9090000000000000018', 'finalCc', 'finalCcUserId', 'fixed_user', '{"userId":"3090000000000000003"}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000291', '1000000000000000099', '9090000000000000019', 'deptLeaderApprove', 'deptLeaderUserId', 'starter_dept_role', '{"roleCodes":["dept_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000292', '1000000000000000099', '9090000000000000019', 'itCheck', 'itCheckUserId', 'role', '{"roleCodes":["it_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000293', '1000000000000000099', '9090000000000000019', 'techDirApprove', 'techDirUserId', 'role', '{"roleCodes":["tech_director"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000294', '1000000000000000099', '9090000000000000019', 'userConfirm', 'userConfirmUserId', 'expression', '{"expr":"starter"}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000295', '1000000000000000099', '9090000000000000019', 'finalCc', 'finalCcUserId', 'fixed_user', '{"userId":"3090000000000000003"}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000296', '1000000000000000099', '9090000000000000020', 'salesLeadApprove', 'salesLeadUserId', 'role', '{"roleCodes":["sales_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000297', '1000000000000000099', '9090000000000000020', 'salesMgrApprove', 'salesMgrUserId', 'role', '{"roleCodes":["sales_manager"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000298', '1000000000000000099', '9090000000000000020', 'gmApprove', 'gmUserId', 'role', '{"roleCodes":["general_manager"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000299', '1000000000000000099', '9090000000000000020', 'financeMargin', 'financeMarginUserId', 'role', '{"roleCodes":["finance_approver"]}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
  union all select '9090000000000000300', '1000000000000000099', '9090000000000000020', 'finalCc', 'finalCcUserId', 'fixed_user', '{"userId":"3090000000000000003"}', 'biz_tenant', 1, '0', null, '0', now(3), '0', now(3), 0
) x
where not exists (
  select 1 from wf_approver_rule t where t.id = x.id or (t.model_id = x.model_id and t.task_definition_key = x.task_definition_key and t.tenant_id = x.tenant_id)
);

set @snap_oa_expense := '[{"fieldCode":"title","fieldName":"标题","fieldType":"string","componentType":"input","fieldRequired":"1","sortNo":1,"platformLocked":"1","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","maxLength":100,"promoteSlot":"idx_str_01"},{"fieldCode":"expenseType","fieldName":"报销类型","fieldType":"string","componentType":"select","fieldRequired":"1","sortNo":2,"platformLocked":"1","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","dictType":"oa_expense_type","promoteSlot":"idx_str_02"},{"fieldCode":"amount","fieldName":"报销金额","fieldType":"decimal","componentType":"input","fieldRequired":"1","sortNo":3,"platformLocked":"1","usedInCondition":"1","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","promoteSlot":"idx_str_03"},{"fieldCode":"reason","fieldName":"报销事由","fieldType":"string","componentType":"textarea","fieldRequired":"1","sortNo":4,"platformLocked":"0","usedInCondition":"0","usedInList":"0","listSlotSync":"0","originType":"platform_copy","status":"0","maxLength":500},{"fieldCode":"attachments","fieldName":"发票附件","fieldType":"file","componentType":"file","fieldRequired":"1","sortNo":5,"platformLocked":"0","usedInCondition":"0","usedInList":"0","listSlotSync":"0","originType":"platform_copy","status":"0"}]';
set @snap_oa_leave := '[{"fieldCode":"leaveType","fieldName":"请假类型","fieldType":"string","componentType":"select","fieldRequired":"1","sortNo":1,"platformLocked":"1","usedInCondition":"1","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","dictType":"oa_leave_type","promoteSlot":"idx_str_01"},{"fieldCode":"startDate","fieldName":"开始日期","fieldType":"date","componentType":"date","fieldRequired":"1","sortNo":2,"platformLocked":"1","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","promoteSlot":"idx_str_02"},{"fieldCode":"endDate","fieldName":"结束日期","fieldType":"date","componentType":"date","fieldRequired":"1","sortNo":3,"platformLocked":"0","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","promoteSlot":"idx_str_03"},{"fieldCode":"leaveDays","fieldName":"请假天数","fieldType":"decimal","componentType":"input","fieldRequired":"1","sortNo":4,"platformLocked":"0","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","promoteSlot":"idx_str_04"},{"fieldCode":"handover","fieldName":"工作交接人","fieldType":"string","componentType":"input","fieldRequired":"0","sortNo":5,"platformLocked":"0","usedInCondition":"0","usedInList":"0","listSlotSync":"0","originType":"platform_copy","status":"0","maxLength":64},{"fieldCode":"reason","fieldName":"请假事由","fieldType":"string","componentType":"textarea","fieldRequired":"1","sortNo":6,"platformLocked":"0","usedInCondition":"0","usedInList":"0","listSlotSync":"0","originType":"platform_copy","status":"0","maxLength":500},{"fieldCode":"attachments","fieldName":"证明材料","fieldType":"file","componentType":"file","fieldRequired":"0","sortNo":7,"platformLocked":"0","usedInCondition":"0","usedInList":"0","listSlotSync":"0","originType":"platform_copy","status":"0"}]';
set @snap_oa_travel := '[{"fieldCode":"title","fieldName":"标题","fieldType":"string","componentType":"input","fieldRequired":"1","sortNo":1,"platformLocked":"1","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","maxLength":100,"promoteSlot":"idx_str_01"},{"fieldCode":"destination","fieldName":"出差地点","fieldType":"string","componentType":"input","fieldRequired":"1","sortNo":2,"platformLocked":"1","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","maxLength":100,"promoteSlot":"idx_str_02"},{"fieldCode":"amount","fieldName":"出差预算","fieldType":"decimal","componentType":"input","fieldRequired":"1","sortNo":3,"platformLocked":"1","usedInCondition":"1","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","promoteSlot":"idx_str_03"},{"fieldCode":"startDate","fieldName":"开始日期","fieldType":"date","componentType":"date","fieldRequired":"1","sortNo":4,"platformLocked":"0","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","promoteSlot":"idx_str_04"},{"fieldCode":"endDate","fieldName":"结束日期","fieldType":"date","componentType":"date","fieldRequired":"1","sortNo":5,"platformLocked":"0","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","promoteSlot":"idx_str_05"},{"fieldCode":"reason","fieldName":"出差事由","fieldType":"string","componentType":"textarea","fieldRequired":"1","sortNo":6,"platformLocked":"0","usedInCondition":"0","usedInList":"0","listSlotSync":"0","originType":"platform_copy","status":"0","maxLength":500}]';
set @snap_oa_contract := '[{"fieldCode":"title","fieldName":"合同标题","fieldType":"string","componentType":"input","fieldRequired":"1","sortNo":1,"platformLocked":"1","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","maxLength":100,"promoteSlot":"idx_str_01"},{"fieldCode":"contractNo","fieldName":"合同编号","fieldType":"string","componentType":"input","fieldRequired":"0","sortNo":2,"platformLocked":"1","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","maxLength":64,"promoteSlot":"idx_str_02"},{"fieldCode":"amount","fieldName":"合同金额","fieldType":"decimal","componentType":"input","fieldRequired":"1","sortNo":3,"platformLocked":"0","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","promoteSlot":"idx_str_03"},{"fieldCode":"reason","fieldName":"合同摘要","fieldType":"string","componentType":"textarea","fieldRequired":"1","sortNo":4,"platformLocked":"0","usedInCondition":"0","usedInList":"0","listSlotSync":"0","originType":"platform_copy","status":"0","maxLength":500},{"fieldCode":"attachments","fieldName":"合同附件","fieldType":"file","componentType":"file","fieldRequired":"1","sortNo":5,"platformLocked":"0","usedInCondition":"0","usedInList":"0","listSlotSync":"0","originType":"platform_copy","status":"0"}]';
set @snap_oa_onboard := '[{"fieldCode":"title","fieldName":"入职标题","fieldType":"string","componentType":"input","fieldRequired":"1","sortNo":1,"platformLocked":"1","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","maxLength":100,"promoteSlot":"idx_str_01"},{"fieldCode":"employeeName","fieldName":"入职人姓名","fieldType":"string","componentType":"input","fieldRequired":"1","sortNo":2,"platformLocked":"1","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","maxLength":64,"promoteSlot":"idx_str_02"},{"fieldCode":"entryDate","fieldName":"入职日期","fieldType":"date","componentType":"date","fieldRequired":"1","sortNo":3,"platformLocked":"0","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","promoteSlot":"idx_str_03"},{"fieldCode":"deptName","fieldName":"入职部门","fieldType":"string","componentType":"input","fieldRequired":"1","sortNo":4,"platformLocked":"0","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","maxLength":64,"promoteSlot":"idx_str_04"},{"fieldCode":"reason","fieldName":"备注","fieldType":"string","componentType":"textarea","fieldRequired":"0","sortNo":5,"platformLocked":"0","usedInCondition":"0","usedInList":"0","listSlotSync":"0","originType":"platform_copy","status":"0","maxLength":500}]';
set @snap_oa_offboard := '[{"fieldCode":"title","fieldName":"离职标题","fieldType":"string","componentType":"input","fieldRequired":"1","sortNo":1,"platformLocked":"1","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","maxLength":100,"promoteSlot":"idx_str_01"},{"fieldCode":"leaveDate","fieldName":"预计离职日","fieldType":"date","componentType":"date","fieldRequired":"1","sortNo":2,"platformLocked":"1","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","promoteSlot":"idx_str_02"},{"fieldCode":"handover","fieldName":"工作交接人","fieldType":"string","componentType":"input","fieldRequired":"1","sortNo":3,"platformLocked":"0","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","maxLength":64,"promoteSlot":"idx_str_03"},{"fieldCode":"reason","fieldName":"离职原因","fieldType":"string","componentType":"textarea","fieldRequired":"1","sortNo":4,"platformLocked":"0","usedInCondition":"0","usedInList":"0","listSlotSync":"0","originType":"platform_copy","status":"0","maxLength":500}]';
set @snap_oa_purchase := '[{"fieldCode":"title","fieldName":"采购标题","fieldType":"string","componentType":"input","fieldRequired":"1","sortNo":1,"platformLocked":"1","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","maxLength":100,"promoteSlot":"idx_str_01"},{"fieldCode":"amount","fieldName":"采购金额","fieldType":"decimal","componentType":"input","fieldRequired":"1","sortNo":2,"platformLocked":"1","usedInCondition":"1","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","promoteSlot":"idx_str_02"},{"fieldCode":"itemName","fieldName":"物资名称","fieldType":"string","componentType":"input","fieldRequired":"1","sortNo":3,"platformLocked":"0","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","maxLength":100,"promoteSlot":"idx_str_03"},{"fieldCode":"reason","fieldName":"采购事由","fieldType":"string","componentType":"textarea","fieldRequired":"1","sortNo":4,"platformLocked":"0","usedInCondition":"0","usedInList":"0","listSlotSync":"0","originType":"platform_copy","status":"0","maxLength":500}]';
set @snap_oa_seal := '[{"fieldCode":"title","fieldName":"用印标题","fieldType":"string","componentType":"input","fieldRequired":"1","sortNo":1,"platformLocked":"1","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","maxLength":100,"promoteSlot":"idx_str_01"},{"fieldCode":"fileType","fieldName":"文件类型","fieldType":"string","componentType":"select","fieldRequired":"1","sortNo":2,"platformLocked":"1","usedInCondition":"1","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","dictType":"oa_seal_file_type","promoteSlot":"idx_str_02"},{"fieldCode":"sealPurpose","fieldName":"用印用途","fieldType":"string","componentType":"textarea","fieldRequired":"1","sortNo":3,"platformLocked":"0","usedInCondition":"0","usedInList":"0","listSlotSync":"0","originType":"platform_copy","status":"0","maxLength":500},{"fieldCode":"attachments","fieldName":"待盖章文件","fieldType":"file","componentType":"file","fieldRequired":"1","sortNo":4,"platformLocked":"0","usedInCondition":"0","usedInList":"0","listSlotSync":"0","originType":"platform_copy","status":"0"}]';
set @snap_oa_it_access := '[{"fieldCode":"title","fieldName":"权限申请标题","fieldType":"string","componentType":"input","fieldRequired":"1","sortNo":1,"platformLocked":"1","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","maxLength":100,"promoteSlot":"idx_str_01"},{"fieldCode":"privilegeLevel","fieldName":"权限等级","fieldType":"string","componentType":"select","fieldRequired":"1","sortNo":2,"platformLocked":"1","usedInCondition":"1","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","dictType":"oa_it_privilege","promoteSlot":"idx_str_02"},{"fieldCode":"systemName","fieldName":"系统/模块","fieldType":"string","componentType":"input","fieldRequired":"1","sortNo":3,"platformLocked":"0","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","maxLength":100,"promoteSlot":"idx_str_03"},{"fieldCode":"reason","fieldName":"申请事由","fieldType":"string","componentType":"textarea","fieldRequired":"1","sortNo":4,"platformLocked":"0","usedInCondition":"0","usedInList":"0","listSlotSync":"0","originType":"platform_copy","status":"0","maxLength":500}]';
set @snap_oa_quote := '[{"fieldCode":"title","fieldName":"报价标题","fieldType":"string","componentType":"input","fieldRequired":"1","sortNo":1,"platformLocked":"1","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","maxLength":100,"promoteSlot":"idx_str_01"},{"fieldCode":"customerName","fieldName":"客户名称","fieldType":"string","componentType":"input","fieldRequired":"1","sortNo":2,"platformLocked":"1","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","maxLength":100,"promoteSlot":"idx_str_02"},{"fieldCode":"amount","fieldName":"报价金额","fieldType":"decimal","componentType":"input","fieldRequired":"1","sortNo":3,"platformLocked":"0","usedInCondition":"0","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","promoteSlot":"idx_str_03"},{"fieldCode":"discount","fieldName":"折扣(折)","fieldType":"decimal","componentType":"input","fieldRequired":"1","sortNo":4,"platformLocked":"1","usedInCondition":"1","usedInList":"1","listSlotSync":"1","originType":"platform_copy","status":"0","promoteSlot":"idx_str_04"},{"fieldCode":"reason","fieldName":"报价说明","fieldType":"string","componentType":"textarea","fieldRequired":"0","sortNo":5,"platformLocked":"0","usedInCondition":"0","usedInList":"0","listSlotSync":"0","originType":"platform_copy","status":"0","maxLength":500}]';

insert into oa_tenant_template (
  id, tenant_id, category_code, template_code, template_name, platform_template_id, source_platform_tpl_ver_id, latest_version_no, published_tpl_ver_id, enable_status, status, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '9290000000000000140' as id, @icoolkj_tid as tenant_id, 'oa_leave' as category_code, 'oa_leave' as template_code, '请假申请' as template_name, '9200000000000000001' as platform_template_id, '9200000000000000002' as source_platform_tpl_ver_id, 1 as latest_version_no, '9290000000000000141' as published_tpl_ver_id, 'enabled' as enable_status, '0' as status, 'icoolkj 标准请假' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '9290000000000000600', @icoolkj_tid, 'oa_expense', 'oa_expense', '费用报销', '9290000000000000400', '9290000000000000401', 1, '9290000000000000601', 'enabled', '0', 'icoolkj 标准费用报销', '0', now(3), '0', now(3), 0
  union all select '9290000000000000610', @icoolkj_tid, 'oa_travel', 'oa_travel', '出差申请', '9290000000000000410', '9290000000000000411', 1, '9290000000000000611', 'enabled', '0', 'icoolkj 标准出差申请', '0', now(3), '0', now(3), 0
  union all select '9290000000000000620', @icoolkj_tid, 'oa_contract', 'oa_contract', '合同评审', '9290000000000000420', '9290000000000000421', 1, '9290000000000000621', 'enabled', '0', 'icoolkj 标准合同评审', '0', now(3), '0', now(3), 0
  union all select '9290000000000000630', @icoolkj_tid, 'oa_onboard', 'oa_onboard', '员工入职', '9290000000000000430', '9290000000000000431', 1, '9290000000000000631', 'enabled', '0', 'icoolkj 标准员工入职', '0', now(3), '0', now(3), 0
  union all select '9290000000000000640', @icoolkj_tid, 'oa_offboard', 'oa_offboard', '员工离职', '9290000000000000440', '9290000000000000441', 1, '9290000000000000641', 'enabled', '0', 'icoolkj 标准员工离职', '0', now(3), '0', now(3), 0
  union all select '9290000000000000650', @icoolkj_tid, 'oa_purchase', 'oa_purchase', '采购申请', '9290000000000000450', '9290000000000000451', 1, '9290000000000000651', 'enabled', '0', 'icoolkj 标准采购申请', '0', now(3), '0', now(3), 0
  union all select '9290000000000000660', @icoolkj_tid, 'oa_seal', 'oa_seal', '用印申请', '9290000000000000460', '9290000000000000461', 1, '9290000000000000661', 'enabled', '0', 'icoolkj 标准用印申请', '0', now(3), '0', now(3), 0
  union all select '9290000000000000670', @icoolkj_tid, 'oa_it_access', 'oa_it_access', 'IT权限开通', '9290000000000000470', '9290000000000000471', 1, '9290000000000000671', 'enabled', '0', 'icoolkj 标准IT权限开通', '0', now(3), '0', now(3), 0
  union all select '9290000000000000680', @icoolkj_tid, 'oa_quote', 'oa_quote', '销售报价审批', '9290000000000000480', '9290000000000000481', 1, '9290000000000000681', 'enabled', '0', 'icoolkj 标准销售报价审批', '0', now(3), '0', now(3), 0
) x
where not exists (
  select 1 from oa_tenant_template t where t.id = x.id or (t.tenant_id = x.tenant_id and t.template_code = x.template_code and t.deleted = 0)
);

insert into oa_tenant_template_version (
  id, tenant_id, tenant_template_id, version_no, version_status, platform_tpl_ver_id, wf_model_id, wf_model_version_no, field_snapshot_json, published_at, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '9290000000000000141' as id, @icoolkj_tid as tenant_id, '9290000000000000140' as tenant_template_id, 1 as version_no, 'published' as version_status, '9200000000000000002' as platform_tpl_ver_id, '9090000000000000009' as wf_model_id, 1 as wf_model_version_no, @snap_oa_leave as field_snapshot_json, now(3) as published_at, 'office-lab' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '9290000000000000601', @icoolkj_tid, '9290000000000000600', 1, 'published', '9290000000000000401', '9090000000000000012', 1, @snap_oa_expense, now(3), 'office-lab', '0', now(3), '0', now(3), 0
  union all select '9290000000000000611', @icoolkj_tid, '9290000000000000610', 1, 'published', '9290000000000000411', '9090000000000000014', 1, @snap_oa_travel, now(3), 'office-lab', '0', now(3), '0', now(3), 0
  union all select '9290000000000000621', @icoolkj_tid, '9290000000000000620', 1, 'published', '9290000000000000421', '9090000000000000005', 1, @snap_oa_contract, now(3), 'office-lab', '0', now(3), '0', now(3), 0
  union all select '9290000000000000631', @icoolkj_tid, '9290000000000000630', 1, 'published', '9290000000000000431', '9090000000000000006', 1, @snap_oa_onboard, now(3), 'office-lab', '0', now(3), '0', now(3), 0
  union all select '9290000000000000641', @icoolkj_tid, '9290000000000000640', 1, 'published', '9290000000000000441', '9090000000000000007', 1, @snap_oa_offboard, now(3), 'office-lab', '0', now(3), '0', now(3), 0
  union all select '9290000000000000651', @icoolkj_tid, '9290000000000000650', 1, 'published', '9290000000000000451', '9090000000000000008', 1, @snap_oa_purchase, now(3), 'office-lab', '0', now(3), '0', now(3), 0
  union all select '9290000000000000661', @icoolkj_tid, '9290000000000000660', 1, 'published', '9290000000000000461', '9090000000000000018', 1, @snap_oa_seal, now(3), 'office-lab', '0', now(3), '0', now(3), 0
  union all select '9290000000000000671', @icoolkj_tid, '9290000000000000670', 1, 'published', '9290000000000000471', '9090000000000000019', 1, @snap_oa_it_access, now(3), 'office-lab', '0', now(3), '0', now(3), 0
  union all select '9290000000000000681', @icoolkj_tid, '9290000000000000680', 1, 'published', '9290000000000000481', '9090000000000000020', 1, @snap_oa_quote, now(3), 'office-lab', '0', now(3), '0', now(3), 0
) x
where not exists (
  select 1 from oa_tenant_template_version t where t.id = x.id or (t.tenant_template_id = x.tenant_template_id and t.version_no = x.version_no and t.deleted = 0)
);

insert into oa_tenant_field_def (
  id, tenant_id, template_version_id, source_platform_field_id, field_code, field_name, field_alias, field_type, component_type, dict_type, field_required, sort_no, default_value, max_length, platform_locked, used_in_condition, used_in_list, promote_slot, list_slot_sync, origin_type, status, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '9290000000000000142' as id, @icoolkj_tid as tenant_id, '9290000000000000141' as template_version_id, '9200000000000000011' as source_platform_field_id, 'leaveType' as field_code, '请假类型' as field_name, null as field_alias, 'string' as field_type, 'select' as component_type, 'oa_leave_type' as dict_type, '1' as field_required, 1 as sort_no, null as default_value, null as max_length, '1' as platform_locked, '1' as used_in_condition, '1' as used_in_list, 'idx_str_01' as promote_slot, '1' as list_slot_sync, 'platform_copy' as origin_type, '0' as status, null as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '9290000000000000143', @icoolkj_tid, '9290000000000000141', '9200000000000000012', 'startDate', '开始日期', null, 'date', 'date', null, '1', 2, null, null, '1', '0', '1', 'idx_str_02', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000000144', @icoolkj_tid, '9290000000000000141', '9200000000000000013', 'endDate', '结束日期', null, 'date', 'date', null, '1', 3, null, null, '0', '0', '1', 'idx_str_03', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000000145', @icoolkj_tid, '9290000000000000141', '9200000000000000014', 'leaveDays', '请假天数', null, 'decimal', 'input', null, '1', 4, null, null, '0', '0', '1', 'idx_str_04', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000000146', @icoolkj_tid, '9290000000000000141', null, 'handover', '工作交接人', null, 'string', 'input', null, '0', 5, null, 64, '0', '0', '0', null, '0', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000000147', @icoolkj_tid, '9290000000000000141', '9200000000000000015', 'reason', '请假事由', null, 'string', 'textarea', null, '1', 6, null, 500, '0', '0', '0', null, '0', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000000148', @icoolkj_tid, '9290000000000000141', '9200000000000000016', 'attachments', '证明材料', null, 'file', 'file', null, '0', 7, null, null, '0', '0', '0', null, '0', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006002', @icoolkj_tid, '9290000000000000601', '9290000000000004002', 'title', '标题', null, 'string', 'input', null, '1', 1, null, 100, '1', '0', '1', 'idx_str_01', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006003', @icoolkj_tid, '9290000000000000601', '9290000000000004003', 'expenseType', '报销类型', null, 'string', 'select', 'oa_expense_type', '1', 2, null, null, '1', '0', '1', 'idx_str_02', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006004', @icoolkj_tid, '9290000000000000601', '9290000000000004004', 'amount', '报销金额', null, 'decimal', 'input', null, '1', 3, null, null, '1', '1', '1', 'idx_str_03', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006005', @icoolkj_tid, '9290000000000000601', '9290000000000004005', 'reason', '报销事由', null, 'string', 'textarea', null, '1', 4, null, 500, '0', '0', '0', null, '0', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006006', @icoolkj_tid, '9290000000000000601', '9290000000000004006', 'attachments', '发票附件', null, 'file', 'file', null, '1', 5, null, null, '0', '0', '0', null, '0', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006102', @icoolkj_tid, '9290000000000000611', '9290000000000004102', 'title', '标题', null, 'string', 'input', null, '1', 1, null, 100, '1', '0', '1', 'idx_str_01', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006103', @icoolkj_tid, '9290000000000000611', '9290000000000004103', 'destination', '出差地点', null, 'string', 'input', null, '1', 2, null, 100, '1', '0', '1', 'idx_str_02', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006104', @icoolkj_tid, '9290000000000000611', '9290000000000004104', 'amount', '出差预算', null, 'decimal', 'input', null, '1', 3, null, null, '1', '1', '1', 'idx_str_03', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006105', @icoolkj_tid, '9290000000000000611', '9290000000000004105', 'startDate', '开始日期', null, 'date', 'date', null, '1', 4, null, null, '0', '0', '1', 'idx_str_04', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006106', @icoolkj_tid, '9290000000000000611', '9290000000000004106', 'endDate', '结束日期', null, 'date', 'date', null, '1', 5, null, null, '0', '0', '1', 'idx_str_05', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006107', @icoolkj_tid, '9290000000000000611', '9290000000000004107', 'reason', '出差事由', null, 'string', 'textarea', null, '1', 6, null, 500, '0', '0', '0', null, '0', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006202', @icoolkj_tid, '9290000000000000621', '9290000000000004202', 'title', '合同标题', null, 'string', 'input', null, '1', 1, null, 100, '1', '0', '1', 'idx_str_01', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006203', @icoolkj_tid, '9290000000000000621', '9290000000000004203', 'contractNo', '合同编号', null, 'string', 'input', null, '0', 2, null, 64, '1', '0', '1', 'idx_str_02', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006204', @icoolkj_tid, '9290000000000000621', '9290000000000004204', 'amount', '合同金额', null, 'decimal', 'input', null, '1', 3, null, null, '0', '0', '1', 'idx_str_03', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006205', @icoolkj_tid, '9290000000000000621', '9290000000000004205', 'reason', '合同摘要', null, 'string', 'textarea', null, '1', 4, null, 500, '0', '0', '0', null, '0', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006206', @icoolkj_tid, '9290000000000000621', '9290000000000004206', 'attachments', '合同附件', null, 'file', 'file', null, '1', 5, null, null, '0', '0', '0', null, '0', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006302', @icoolkj_tid, '9290000000000000631', '9290000000000004302', 'title', '入职标题', null, 'string', 'input', null, '1', 1, null, 100, '1', '0', '1', 'idx_str_01', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006303', @icoolkj_tid, '9290000000000000631', '9290000000000004303', 'employeeName', '入职人姓名', null, 'string', 'input', null, '1', 2, null, 64, '1', '0', '1', 'idx_str_02', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006304', @icoolkj_tid, '9290000000000000631', '9290000000000004304', 'entryDate', '入职日期', null, 'date', 'date', null, '1', 3, null, null, '0', '0', '1', 'idx_str_03', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006305', @icoolkj_tid, '9290000000000000631', '9290000000000004305', 'deptName', '入职部门', null, 'string', 'input', null, '1', 4, null, 64, '0', '0', '1', 'idx_str_04', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006306', @icoolkj_tid, '9290000000000000631', '9290000000000004306', 'reason', '备注', null, 'string', 'textarea', null, '0', 5, null, 500, '0', '0', '0', null, '0', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006402', @icoolkj_tid, '9290000000000000641', '9290000000000004402', 'title', '离职标题', null, 'string', 'input', null, '1', 1, null, 100, '1', '0', '1', 'idx_str_01', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006403', @icoolkj_tid, '9290000000000000641', '9290000000000004403', 'leaveDate', '预计离职日', null, 'date', 'date', null, '1', 2, null, null, '1', '0', '1', 'idx_str_02', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006404', @icoolkj_tid, '9290000000000000641', '9290000000000004404', 'handover', '工作交接人', null, 'string', 'input', null, '1', 3, null, 64, '0', '0', '1', 'idx_str_03', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006405', @icoolkj_tid, '9290000000000000641', '9290000000000004405', 'reason', '离职原因', null, 'string', 'textarea', null, '1', 4, null, 500, '0', '0', '0', null, '0', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006502', @icoolkj_tid, '9290000000000000651', '9290000000000004502', 'title', '采购标题', null, 'string', 'input', null, '1', 1, null, 100, '1', '0', '1', 'idx_str_01', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006503', @icoolkj_tid, '9290000000000000651', '9290000000000004503', 'amount', '采购金额', null, 'decimal', 'input', null, '1', 2, null, null, '1', '1', '1', 'idx_str_02', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006504', @icoolkj_tid, '9290000000000000651', '9290000000000004504', 'itemName', '物资名称', null, 'string', 'input', null, '1', 3, null, 100, '0', '0', '1', 'idx_str_03', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006505', @icoolkj_tid, '9290000000000000651', '9290000000000004505', 'reason', '采购事由', null, 'string', 'textarea', null, '1', 4, null, 500, '0', '0', '0', null, '0', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006602', @icoolkj_tid, '9290000000000000661', '9290000000000004602', 'title', '用印标题', null, 'string', 'input', null, '1', 1, null, 100, '1', '0', '1', 'idx_str_01', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006603', @icoolkj_tid, '9290000000000000661', '9290000000000004603', 'fileType', '文件类型', null, 'string', 'select', 'oa_seal_file_type', '1', 2, null, null, '1', '1', '1', 'idx_str_02', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006604', @icoolkj_tid, '9290000000000000661', '9290000000000004604', 'sealPurpose', '用印用途', null, 'string', 'textarea', null, '1', 3, null, 500, '0', '0', '0', null, '0', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006605', @icoolkj_tid, '9290000000000000661', '9290000000000004605', 'attachments', '待盖章文件', null, 'file', 'file', null, '1', 4, null, null, '0', '0', '0', null, '0', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006702', @icoolkj_tid, '9290000000000000671', '9290000000000004702', 'title', '权限申请标题', null, 'string', 'input', null, '1', 1, null, 100, '1', '0', '1', 'idx_str_01', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006703', @icoolkj_tid, '9290000000000000671', '9290000000000004703', 'privilegeLevel', '权限等级', null, 'string', 'select', 'oa_it_privilege', '1', 2, null, null, '1', '1', '1', 'idx_str_02', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006704', @icoolkj_tid, '9290000000000000671', '9290000000000004704', 'systemName', '系统/模块', null, 'string', 'input', null, '1', 3, null, 100, '0', '0', '1', 'idx_str_03', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006705', @icoolkj_tid, '9290000000000000671', '9290000000000004705', 'reason', '申请事由', null, 'string', 'textarea', null, '1', 4, null, 500, '0', '0', '0', null, '0', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006802', @icoolkj_tid, '9290000000000000681', '9290000000000004802', 'title', '报价标题', null, 'string', 'input', null, '1', 1, null, 100, '1', '0', '1', 'idx_str_01', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006803', @icoolkj_tid, '9290000000000000681', '9290000000000004803', 'customerName', '客户名称', null, 'string', 'input', null, '1', 2, null, 100, '1', '0', '1', 'idx_str_02', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006804', @icoolkj_tid, '9290000000000000681', '9290000000000004804', 'amount', '报价金额', null, 'decimal', 'input', null, '1', 3, null, null, '0', '0', '1', 'idx_str_03', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006805', @icoolkj_tid, '9290000000000000681', '9290000000000004805', 'discount', '折扣(折)', null, 'decimal', 'input', null, '1', 4, null, null, '1', '1', '1', 'idx_str_04', '1', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
  union all select '9290000000000006806', @icoolkj_tid, '9290000000000000681', '9290000000000004806', 'reason', '报价说明', null, 'string', 'textarea', null, '0', 5, null, 500, '0', '0', '0', null, '0', 'platform_copy', '0', null, '0', now(3), '0', now(3), 0
) x
where not exists (
  select 1 from oa_tenant_field_def t where t.id = x.id
);

update wf_model
   set process_config = replace(
         process_config,
         '"roleCodes":["employee","tenant_admin"]',
         '"roleCodes":["employee"]'
       ),
       updated_at = now(3)
 where tenant_id = @icoolkj_tid
   and process_config like '%"roleCodes":["employee","tenant_admin"]%';

update wf_model_version
   set process_config_json = replace(
         process_config_json,
         '"roleCodes":["employee","tenant_admin"]',
         '"roleCodes":["employee"]'
       ),
       updated_at = now(3)
 where tenant_id = @icoolkj_tid
   and process_config_json like '%"roleCodes":["employee","tenant_admin"]%';
