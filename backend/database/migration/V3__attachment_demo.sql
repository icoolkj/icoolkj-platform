-- 附件测试业务：表 + 目录槽位 + 菜单（规范闭环样例）
-- Flyway V3

create table if not exists infra_attachment_demo (
  id varchar(20) not null comment '主键',
  tenant_id varchar(20) not null comment '租户ID',
  title varchar(200) not null comment '标题',
  assets_json json null comment '目录槽位 profile_code → fileId 或 fileId[]',
  status char(1) not null default '0' comment '状态（0正常 1停用）',
  remark varchar(500) null comment '备注',
  created_by varchar(20) null,
  created_at datetime(3) not null default current_timestamp(3),
  updated_by varchar(20) null,
  updated_at datetime(3) not null default current_timestamp(3) on update current_timestamp(3),
  deleted tinyint(1) not null default 0,
  primary key (id),
  key idx_infra_attachment_demo_tenant (tenant_id),
  key idx_infra_attachment_demo_title (tenant_id, title)
) comment='附件测试业务（CatalogAttachmentManager + syncBizFileRefs 样例）';

-- 槽位：封面(单图) / 合同(单文件) / 材料包(多文件)
insert into infra_file_profile (
  id, tenant_id, profile_code, profile_name, profile_name_i18n, category_tag, status, remark,
  created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '6300000000000000020' as id, '0' as tenant_id, 'attachment_demo_cover' as profile_code,
    '测试封面' as profile_name,
    json_object('zh-CN', '测试封面', 'en-US', 'Demo cover') as profile_name_i18n,
    'attachment_demo' as category_tag, '0' as status,
    'AttachmentDemo 单图封面槽位' as remark,
    '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '6300000000000000021', '0', 'attachment_demo_contract', '测试合同',
    json_object('zh-CN', '测试合同', 'en-US', 'Demo contract'), 'attachment_demo', '0',
    'AttachmentDemo 单文件合同槽位', '0', now(3), '0', now(3), 0
  union all select '6300000000000000022', '0', 'attachment_demo_docs', '测试材料包',
    json_object('zh-CN', '测试材料包', 'en-US', 'Demo document pack'), 'attachment_demo', '0',
    'AttachmentDemo 多文件材料槽位', '0', now(3), '0', now(3), 0
) t
where not exists (
  select 1 from infra_file_profile p where p.tenant_id = t.tenant_id and p.profile_code = t.profile_code and p.deleted = 0
);

insert into infra_file_profile_rule (
  id, tenant_id, profile_code, allowed_exts, allowed_content_types, required, min_num, max_num,
  min_size_bytes, max_size_bytes, sort_no, status, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '6310000000000000020' as id, '0' as tenant_id, 'attachment_demo_cover' as profile_code,
    'png,jpg,jpeg,webp' as allowed_exts,
    'image/png,image/jpeg,image/webp' as allowed_content_types,
    '1' as required, 1 as min_num, 1 as max_num, 0 as min_size_bytes, 5242880 as max_size_bytes,
    10 as sort_no, '0' as status, null as remark,
    '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '6310000000000000021', '0', 'attachment_demo_contract',
    'pdf,doc,docx', 'application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    '0', 0, 1, 0, 20971520, 20, '0', null, '0', now(3), '0', now(3), 0
  union all select '6310000000000000022', '0', 'attachment_demo_docs',
    'pdf,doc,docx,xls,xlsx,zip,png,jpg,jpeg',
    null,
    '0', 0, 5, 0, 20971520, 30, '0', '最多 5 个材料文件', '0', now(3), '0', now(3), 0
) t
where not exists (
  select 1 from infra_file_profile_rule r where r.tenant_id = t.tenant_id and r.profile_code = t.profile_code and r.deleted = 0
);

insert into infra_file_catalog (
  id, tenant_id, catalog_code, catalog_name, catalog_name_i18n, status, remark,
  created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '6320000000000000010' as id, '0' as tenant_id, 'attachment_demo' as catalog_code,
    '附件测试业务目录' as catalog_name,
    json_object('zh-CN', '附件测试业务目录', 'en-US', 'Attachment demo catalog') as catalog_name_i18n,
    '0' as status, 'view.json fileCatalogs 驱动样例',
    '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
) t
where not exists (
  select 1 from infra_file_catalog c where c.tenant_id = t.tenant_id and c.catalog_code = t.catalog_code and c.deleted = 0
);

insert into infra_file_catalog_item (
  id, tenant_id, catalog_code, profile_code, required, min_num, max_num, sort_no, status,
  created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '6330000000000000020' as id, '0' as tenant_id, 'attachment_demo' as catalog_code,
    'attachment_demo_cover' as profile_code, '1' as required, 1 as min_num, 1 as max_num, 10 as sort_no, '0' as status,
    '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '6330000000000000021', '0', 'attachment_demo', 'attachment_demo_contract',
    '0', null, null, 20, '0', '0', now(3), '0', now(3), 0
  union all select '6330000000000000022', '0', 'attachment_demo', 'attachment_demo_docs',
    '0', null, null, 30, '0', '0', now(3), '0', now(3), 0
) t
where not exists (
  select 1 from infra_file_catalog_item i
  where i.tenant_id = t.tenant_id and i.catalog_code = t.catalog_code and i.profile_code = t.profile_code and i.deleted = 0
);

-- 菜单：文件配置中心 → 附件测试业务
insert into sys_menu (
  id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path, permission_code,
  icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope, product_code,
  created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '4000070105000000000' as id, '4000070100000000000' as parent_id, 'C' as menu_type,
    '附件测试业务' as menu_name,
    json_object('zh-CN', '附件测试业务', 'en-US', 'Attachment Demo') as name_i18n,
    'InfraAttachmentDemo' as route_name, 'attachmentDemo' as route_path,
    '/infra/attachmentDemo/index' as component_path, 'infra:attachmentDemo:view' as permission_code,
    'Upload' as icon, 5 as sort_no, '0' as visible, '0' as status, '0' as is_external, '1' as is_cache,
    '规范样例：业务表 + view.json fileCatalogs + CatalogAttachmentManager + syncBizFileRefs' as remark,
    'TENANT' as assign_scope, 'files' as product_code, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '4000070105010000000', '4000070105000000000', 'F', '查询',
    json_object('zh-CN', '查询', 'en-US', 'List'), null, null, null, 'infra:attachmentDemo:list',
    null, 1, '0', '0', '0', '1', null, 'TENANT', 'files', '0', now(3), '0', now(3), 0
  union all select '4000070105020000000', '4000070105000000000', 'F', '新增',
    json_object('zh-CN', '新增', 'en-US', 'Add'), null, null, null, 'infra:attachmentDemo:add',
    null, 2, '0', '0', '0', '1', null, 'TENANT', 'files', '0', now(3), '0', now(3), 0
  union all select '4000070105030000000', '4000070105000000000', 'F', '修改',
    json_object('zh-CN', '修改', 'en-US', 'Update'), null, null, null, 'infra:attachmentDemo:update',
    null, 3, '0', '0', '0', '1', null, 'TENANT', 'files', '0', now(3), '0', now(3), 0
  union all select '4000070105040000000', '4000070105000000000', 'F', '删除',
    json_object('zh-CN', '删除', 'en-US', 'Delete'), null, null, null, 'infra:attachmentDemo:delete',
    null, 4, '0', '0', '0', '1', null, 'TENANT', 'files', '0', now(3), '0', now(3), 0
) t
where not exists (select 1 from sys_menu m where m.id = t.id and m.deleted = 0);

-- 平台超管 + 租户管理员补授权
insert into sys_role_menu (tenant_id, role_id, menu_id)
select '0', '2000000000000000001', m.id
from sys_menu m
where m.id in ('4000070105000000000', '4000070105010000000', '4000070105020000000', '4000070105030000000', '4000070105040000000')
  and m.deleted = 0
  and not exists (
    select 1 from sys_role_menu rm
    where rm.tenant_id = '0' and rm.role_id = '2000000000000000001' and rm.menu_id = m.id
  );

insert into sys_role_menu (tenant_id, role_id, menu_id)
select r.tenant_id, r.id, m.id
from sys_role r
cross join sys_menu m
where r.role_code in ('tenant_admin', 'org_admin', 'tenant_sys_admin')
  and r.deleted = 0
  and m.deleted = 0
  and m.id in ('4000070105000000000', '4000070105010000000', '4000070105020000000', '4000070105030000000', '4000070105040000000')
  and not exists (
    select 1 from sys_role_menu rm
    where rm.tenant_id = r.tenant_id and rm.role_id = r.id and rm.menu_id = m.id
  );

insert into sys_role_menu (tenant_id, role_id, menu_id)
select r.tenant_id, r.id, m.id
from sys_role r
cross join sys_menu m
where r.role_code = 'tenant_sys_admin' and r.deleted = 0
  and m.deleted = 0 and m.assign_scope = 'TENANT'
  and m.permission_code like 'infra:attachmentDemo:%'
  and not exists (
    select 1 from sys_role_menu rm
    where rm.tenant_id = r.tenant_id and rm.role_id = r.id and rm.menu_id = m.id
  );
