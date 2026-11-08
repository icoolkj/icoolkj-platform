-- 产品管理（平台元数据）
insert into sys_menu (
  id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path,
  permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope, product_code,
  created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '4000020305000000000' as id, '4000020300000000000' as parent_id, 'C' as menu_type,
    '产品管理' as menu_name,
    json_object('zh-CN', '产品管理', 'en-US', 'Products') as name_i18n,
    'SystemProduct' as route_name, 'product' as route_path, '/system/product/index' as component_path,
    'system:product:view' as permission_code, 'Grid' as icon, 5 as sort_no,
    '0' as visible, '0' as status, '0' as is_external, '1' as is_cache,
    'Console 顶栏产品宇宙：编码/名称/图标/排序' as remark,
    'PLATFORM' as assign_scope, 'platform' as product_code,
    '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '4000020305010000000', '4000020305000000000', 'F', '产品查询',
    json_object('zh-CN', '产品查询', 'en-US', 'List'), null, null, null, 'system:product:list',
    null, 1, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0
  union all select '4000020305020000000', '4000020305000000000', 'F', '产品新增',
    json_object('zh-CN', '产品新增', 'en-US', 'Add'), null, null, null, 'system:product:add',
    null, 2, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0
  union all select '4000020305030000000', '4000020305000000000', 'F', '产品修改',
    json_object('zh-CN', '产品修改', 'en-US', 'Edit'), null, null, null, 'system:product:update',
    null, 3, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0
  union all select '4000020305040000000', '4000020305000000000', 'F', '产品删除',
    json_object('zh-CN', '产品删除', 'en-US', 'Delete'), null, null, null, 'system:product:delete',
    null, 4, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0
) t
where not exists (select 1 from sys_menu where id = '4000020305000000000');

insert into sys_role_menu (tenant_id, role_id, menu_id)
select '0', '2000000000000000001', m.id
from sys_menu m
where m.deleted = 0
  and m.id in (
    '4000020305000000000',
    '4000020305010000000', '4000020305020000000',
    '4000020305030000000', '4000020305040000000'
  )
  and not exists (
    select 1 from sys_role_menu rm
    where rm.tenant_id = '0' and rm.role_id = '2000000000000000001' and rm.menu_id = m.id
  );
