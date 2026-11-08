-- V2: 平台种子数据
-- 租户(企业+单位) / 区划 / 平台+租户部门岗位 / 用户 / 角色 / 菜单 / 字典 / 参数 / IP / 公告 / 授权

set names utf8mb4;

-- ==========================================
-- 23. 初始化基础租户数据
-- 说明：tenant_id='0' 作为平台级语义，不落 sys_tenant
-- ==========================================
insert into sys_tenant (
  id, tenant_code, tenant_name, contact_name, contact_phone, contact_email,
  license_no, expire_at, account_limit, status, tenant_type, parent_tenant_id, ancestors,
  root_region_id, remark,
  created_by, created_at, updated_by, updated_at, deleted
) values
(
  '1000000000000000001',
  'default',
  '默认企业租户',
  'icoolkj',
  '13800000000',
  'icoolkj@163.com',
  null,
  '2099-12-31 23:59:59.000',
  1000,
  '0',
  'ENTERPRISE',
  null,
  '0',
  '1300000000000000002',
  '演示用企业租户（SaaS 客户）',
  '0', now(3), '0', now(3), 0
),
(
  '1000000000000000002',
  'demo_org',
  '演示单位租户',
  'icoolkj',
  '13800000003',
  'icoolkj@163.com',
  null,
  '2099-12-31 23:59:59.000',
  500,
  '0',
  'ORGANIZATION',
  null,
  '0',
  '1300000000000000001',
  '演示用单位租户（政务/事业单位，根区划=安徽省）',
  '0', now(3), '0', now(3), 0
);

-- ==========================================
-- 23b. 国标区划样例（中国 → 安徽省 → 合肥市 → 区县）
-- ==========================================
insert into sys_region (
  id, tenant_id, parent_id, ancestors, region_code, region_name, region_level,
  sort_no, status, remark, created_by, created_at, updated_by, updated_at, deleted
) values
('1300000000000000000', '0', '0', '0', '100000', '中国', '0', 1, '0', '国标样例-国家', '0', now(3), '0', now(3), 0),
('1300000000000000001', '0', '1300000000000000000', '0,1300000000000000000', '340000', '安徽省', '1', 1, '0', '国标样例-省', '0', now(3), '0', now(3), 0),
('1300000000000000002', '0', '1300000000000000001', '0,1300000000000000000,1300000000000000001', '340100', '合肥市', '2', 1, '0', '国标样例-市', '0', now(3), '0', now(3), 0),
('1300000000000000003', '0', '1300000000000000002', '0,1300000000000000000,1300000000000000001,1300000000000000002', '340104', '蜀山区', '3', 1, '0', '国标样例-区县', '0', now(3), '0', now(3), 0),
('1300000000000000004', '0', '1300000000000000002', '0,1300000000000000000,1300000000000000001,1300000000000000002', '340111', '包河区', '3', 2, '0', '国标样例-区县', '0', now(3), '0', now(3), 0);

-- ==========================================
-- 24. 初始化基础部门
-- 平台 tenant_id='0' 与默认业务租户均需根部门，供用户归属与部门管理演示
-- ==========================================
insert into sys_dept (
  id, tenant_id, parent_id, ancestors,
  dept_name, dept_code, leader_name, phone, email,
  sort_no, status, remark, created_by, created_at, updated_by, updated_at, deleted
) values
(
  '1100000000000000000',
  '0',
  '0',
  '0',
  '平台管理部',
  'platform_ops',
  '平台管理员',
  '13800000001',
  'icoolkj@163.com',
  0,
  '0',
  '平台级根部门（tenant_id=0）',
  '0', now(3), '0', now(3), 0
),
(
  '1100000000000000001',
  '1000000000000000001',
  '0',
  '0',
  '默认部门',
  'default_dept',
  '租户管理员',
  '13800000000',
  'icoolkj@163.com',
  0,
  '0',
  '默认企业租户根部门',
  '0', now(3), '0', now(3), 0
),
(
  '1100000000000000002',
  '1000000000000000002',
  '0',
  '0',
  '演示单位根部门',
  'demo_org_root',
  '单位负责人',
  '13800000003',
  'icoolkj@163.com',
  0,
  '0',
  '演示单位租户根部门',
  '0', now(3), '0', now(3), 0
);

-- ==========================================
-- 25. 初始化基础岗位
-- ==========================================
insert into sys_post (
  id, tenant_id, post_name, post_code, sort_no, status, remark,
  created_by, created_at, updated_by, updated_at, deleted
) values
(
  '1200000000000000000',
  '0',
  '平台管理员岗位',
  'platform_admin',
  0,
  '0',
  '平台级默认岗位',
  '0', now(3), '0', now(3), 0
),
(
  '1200000000000000001',
  '1000000000000000001',
  '租户管理员岗位',
  'tenant_admin',
  0,
  '0',
  '默认企业租户岗位',
  '0', now(3), '0', now(3), 0
),
(
  '1200000000000000002',
  '1000000000000000002',
  '单位管理员岗位',
  'org_admin',
  0,
  '0',
  '演示单位租户岗位',
  '0', now(3), '0', now(3), 0
);

-- ==========================================
-- 26. 初始化角色
-- 平台超级管理员角色 tenant_id='0'
-- 租户管理员角色 tenant_id=默认租户ID
-- ==========================================
insert into sys_role (
  id, tenant_id, role_name, role_code, data_scope, sort_no, status, is_super_role,
  remark, created_by, created_at, updated_by, updated_at, deleted
) values
(
  '2000000000000000001',
  '0',
  '平台超级管理员',
  'platform_super_admin',
  '1',
  1,
  '0',
  '1',
  '平台最高权限角色',
  '0', now(3), '0', now(3), 0
),
(
  '2000000000000000002',
  '1000000000000000001',
  '租户管理员',
  'tenant_admin',
  '1',
  2,
  '0',
  '1',
  '默认租户管理员：本租户内全部能力（不含平台专属菜单）',
  '0', now(3), '0', now(3), 0
),
(
  '2000000000000000004',
  '1000000000000000002',
  '单位管理员',
  'org_admin',
  '1',
  3,
  '0',
  '1',
  '演示单位租户管理员：本租户内全部能力（不含平台专属菜单）',
  '0', now(3), '0', now(3), 0
),
(
  '2000000000000000005',
  '1000000000000000001',
  '部门审批人',
  'dept_approver',
  '4',
  4,
  '0',
  '0',
  '公告等流程一级审批',
  '0', now(3), '0', now(3), 0
);

-- ==========================================
-- 27. 初始化用户
-- 初始明文：StudyOnly@2026（符合默认口令策略：≥10 位，大小写+数字+特殊字符）
-- BCrypt：$2b$10$s7ENUgEgYy6S32zM3PAAleV8KyzPRBuW1bLL/UJIWeDyXKLV4Vldy
-- ==========================================
insert into sys_user (
  id, tenant_id, dept_id, username, nickname, real_name, user_type, email, phone,
  gender, avatar_url, password_hash, status, is_super_admin, login_fail_count,
  locked_until, last_login_ip, last_login_at, pwd_updated_at, must_change_password,
  remark, created_by, created_at, updated_by, updated_at, deleted
) values
(
  '3000000000000000001',
  '0',
  '1100000000000000000',
  'admin',
  '平台管理员',
  '平台管理员',
  'platform',
  'icoolkj@163.com',
  '13800000001',
  '2',
  null,
  '$2b$10$s7ENUgEgYy6S32zM3PAAleV8KyzPRBuW1bLL/UJIWeDyXKLV4Vldy',
  '0',
  '1',
  0,
  null,
  null,
  null,
  now(3),
  '1',
  '平台超级管理员',
  '0', now(3), '0', now(3), 0
),
(
  '3000000000000000002',
  '1000000000000000001',
  '1100000000000000001',
  'tenant_admin',
  '租户管理员',
  '租户管理员',
  'system',
  'icoolkj@163.com',
  '13800000002',
  '2',
  null,
  '$2b$10$s7ENUgEgYy6S32zM3PAAleV8KyzPRBuW1bLL/UJIWeDyXKLV4Vldy',
  '0',
  '0',
  0,
  null,
  null,
  null,
  now(3),
  '1',
  '默认租户管理员',
  '0', now(3), '0', now(3), 0
),
(
  '3000000000000000003',
  '1000000000000000002',
  '1100000000000000002',
  'org_admin',
  '单位管理员',
  '单位管理员',
  'system',
  'icoolkj@163.com',
  '13800000004',
  '2',
  null,
  '$2b$10$s7ENUgEgYy6S32zM3PAAleV8KyzPRBuW1bLL/UJIWeDyXKLV4Vldy',
  '0',
  '0',
  0,
  null,
  null,
  null,
  now(3),
  '1',
  '演示单位租户管理员',
  '0', now(3), '0', now(3), 0
),
(
  '3000000000000000004',
  '1000000000000000001',
  '1100000000000000001',
  'dept_approver',
  '部门审批人',
  '部门审批人',
  'system',
  'icoolkj@163.com',
  '13800000003',
  '2',
  null,
  '$2b$10$s7ENUgEgYy6S32zM3PAAleV8KyzPRBuW1bLL/UJIWeDyXKLV4Vldy',
  '0',
  '0',
  0,
  null,
  null,
  null,
  now(3),
  '1',
  '默认租户部门审批人（公告一级审批）',
  '0', now(3), '0', now(3), 0
);

-- ==========================================
-- 28. 初始化用户角色关联
-- ==========================================
insert into sys_user_role (tenant_id, user_id, role_id) values
('0', '3000000000000000001', '2000000000000000001'),
('1000000000000000001', '3000000000000000002', '2000000000000000002'),
('1000000000000000001', '3000000000000000004', '2000000000000000005'),
('1000000000000000002', '3000000000000000003', '2000000000000000004');

-- ==========================================
-- 29. 初始化用户岗位关联
-- ==========================================
insert into sys_user_post (tenant_id, user_id, post_id) values
('0', '3000000000000000001', '1200000000000000000'),
('1000000000000000001', '3000000000000000002', '1200000000000000001'),
('1000000000000000002', '3000000000000000003', '1200000000000000002');

insert into sys_menu (
  id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path,
  permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope, product_code,
  created_by, created_at, updated_by, updated_at, deleted
) values

('4000010001000000000', '0', 'C', '平台概览', json_object('zh-CN', '平台概览', 'en-US', 'Platform Overview'), 'PlatformOverview', 'overview', '/product/platform/overview/index', 'platform:overview:view', 'DataBoard', 0, '0', '0', '0', '1', '平台产品首页', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020000000000000', '0', 'M', '系统管理', json_object('zh-CN', '系统管理', 'en-US', 'System Management'), 'System', '/system', null, null, 'Tools', 90, '0', '0', '0', '1', '系统管理目录', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

-- 系统管理二级目录（扁平 C 收纳；树构建会自动补祖先）
('4000020100000000000', '4000020000000000000', 'M', '组织权限', json_object('zh-CN', '组织权限', 'en-US', 'Organization'), 'SystemOrg', '/system/org', null, null, 'UserFilled', 1, '0', '0', '0', '1', '用户/角色/部门/岗位', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),
('4000020300000000000', '4000020000000000000', 'M', '平台元数据', json_object('zh-CN', '平台元数据', 'en-US', 'Platform Metadata'), 'SystemMeta', '/system/meta', null, null, 'Collection', 3, '0', '0', '0', '1', '菜单/区划/字典/参数/产品', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000020400000000000', '4000020000000000000', 'M', '国际化', json_object('zh-CN', '国际化', 'en-US', 'Internationalization'), 'SystemI18n', '/system/i18n', null, null, 'Globe', 4, '0', '0', '0', '1', '语言与文案', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000020200000000000', '4000020000000000000', 'M', '展示与策略', json_object('zh-CN', '展示与策略', 'en-US', 'Presentation & Policy'), 'SystemPresent', '/system/present', null, null, 'Postcard', 2, '0', '0', '0', '1', '公告/轮播/站内信/品牌/租户策略', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020101000000000', '4000020100000000000', 'C', '用户管理', json_object('zh-CN', '用户管理', 'en-US', 'Users'), 'SystemUser', 'user', '/system/user/index', 'system:user:view', 'User', 1, '0', '0', '0', '1', '用户管理页面', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020101010000000', '4000020101000000000', 'F', '用户查询', json_object('zh-CN', '用户查询', 'en-US', 'List'), null, null, null, 'system:user:list', null, 1, '0', '0', '0', '1', '用户列表权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020101020000000', '4000020101000000000', 'F', '用户新增', json_object('zh-CN', '用户新增', 'en-US', 'Add'), null, null, null, 'system:user:add', null, 2, '0', '0', '0', '1', '用户新增权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020101030000000', '4000020101000000000', 'F', '用户修改', json_object('zh-CN', '用户修改', 'en-US', 'Edit'), null, null, null, 'system:user:update', null, 3, '0', '0', '0', '1', '用户修改权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020101040000000', '4000020101000000000', 'F', '用户删除', json_object('zh-CN', '用户删除', 'en-US', 'Delete'), null, null, null, 'system:user:delete', null, 4, '0', '0', '0', '1', '用户删除权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020101050000000', '4000020101000000000', 'F', '用户导出', json_object('zh-CN', '用户导出', 'en-US', 'Export'), null, null, null, 'system:user:export', null, 5, '0', '0', '0', '1', '用户导出权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020101080000000', '4000020101000000000', 'F', '用户导入', json_object('zh-CN', '用户导入', 'en-US', 'Import'), null, null, null, 'system:user:import', null, 8, '0', '0', '0', '1', '用户导入权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020101060000000', '4000020101000000000', 'F', '用户重置密码', json_object('zh-CN', '用户重置密码', 'en-US', 'Reset Password'), null, null, null, 'system:user:resetPassword', null, 6, '0', '0', '0', '1', '用户重置密码权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020101070000000', '4000020101000000000', 'F', '用户解锁', json_object('zh-CN', '用户解锁', 'en-US', 'Unlock'), null, null, null, 'system:user:unlock', null, 7, '0', '0', '0', '1', '解除用户登录锁定', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020102000000000', '4000020100000000000', 'C', '角色管理', json_object('zh-CN', '角色管理', 'en-US', 'Roles'), 'SystemRole', 'role', '/system/role/index', 'system:role:view', 'Avatar', 2, '0', '0', '0', '1', '角色管理页面', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020102010000000', '4000020102000000000', 'F', '角色查询', json_object('zh-CN', '角色查询', 'en-US', 'List'), null, null, null, 'system:role:list', null, 1, '0', '0', '0', '1', '角色列表权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020102020000000', '4000020102000000000', 'F', '角色新增', json_object('zh-CN', '角色新增', 'en-US', 'Add'), null, null, null, 'system:role:add', null, 2, '0', '0', '0', '1', '角色新增权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020102030000000', '4000020102000000000', 'F', '角色修改', json_object('zh-CN', '角色修改', 'en-US', 'Edit'), null, null, null, 'system:role:update', null, 3, '0', '0', '0', '1', '角色修改权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020102040000000', '4000020102000000000', 'F', '角色删除', json_object('zh-CN', '角色删除', 'en-US', 'Delete'), null, null, null, 'system:role:delete', null, 4, '0', '0', '0', '1', '角色删除权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020102050000000', '4000020102000000000', 'F', '角色授权', json_object('zh-CN', '角色授权', 'en-US', 'Grant'), null, null, null, 'system:role:grant', null, 5, '0', '0', '0', '1', '角色授权权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020301000000000', '4000020300000000000', 'C', '菜单管理', json_object('zh-CN', '菜单管理', 'en-US', 'Menus'), 'SystemMenu', 'menu', '/system/menu/index', 'system:menu:view', 'Menu', 1, '0', '0', '0', '1', '菜单管理页面', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000020301010000000', '4000020301000000000', 'F', '菜单查询', json_object('zh-CN', '菜单查询', 'en-US', 'List'), null, null, null, 'system:menu:list', null, 1, '0', '0', '0', '1', '菜单查询权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000020301020000000', '4000020301000000000', 'F', '菜单新增', json_object('zh-CN', '菜单新增', 'en-US', 'Add'), null, null, null, 'system:menu:add', null, 2, '0', '0', '0', '1', '菜单新增权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000020301030000000', '4000020301000000000', 'F', '菜单修改', json_object('zh-CN', '菜单修改', 'en-US', 'Edit'), null, null, null, 'system:menu:update', null, 3, '0', '0', '0', '1', '菜单修改权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000020301040000000', '4000020301000000000', 'F', '菜单删除', json_object('zh-CN', '菜单删除', 'en-US', 'Delete'), null, null, null, 'system:menu:delete', null, 4, '0', '0', '0', '1', '菜单删除权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000020103000000000', '4000020100000000000', 'C', '部门管理', json_object('zh-CN', '部门管理', 'en-US', 'Departments'), 'SystemDept', 'dept', '/system/dept/index', 'system:dept:view', 'OfficeBuilding', 3, '0', '0', '0', '1', '部门管理页面', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020103010000000', '4000020103000000000', 'F', '部门查询', json_object('zh-CN', '部门查询', 'en-US', 'List'), null, null, null, 'system:dept:list', null, 1, '0', '0', '0', '1', '部门查询权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020103020000000', '4000020103000000000', 'F', '部门新增', json_object('zh-CN', '部门新增', 'en-US', 'Add'), null, null, null, 'system:dept:add', null, 2, '0', '0', '0', '1', '部门新增权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020103030000000', '4000020103000000000', 'F', '部门修改', json_object('zh-CN', '部门修改', 'en-US', 'Edit'), null, null, null, 'system:dept:update', null, 3, '0', '0', '0', '1', '部门修改权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020103040000000', '4000020103000000000', 'F', '部门删除', json_object('zh-CN', '部门删除', 'en-US', 'Delete'), null, null, null, 'system:dept:delete', null, 4, '0', '0', '0', '1', '部门删除权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020302000000000', '4000020300000000000', 'C', '区划管理', json_object('zh-CN', '区划管理', 'en-US', 'Regions'), 'SystemRegion', 'region', '/system/region/index', 'system:region:view', 'MapLocation', 2, '0', '0', '0', '1', '国标区划树（平台维护）', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000020302010000000', '4000020302000000000', 'F', '区划查询', json_object('zh-CN', '区划查询', 'en-US', 'List'), null, null, null, 'system:region:list', null, 1, '0', '0', '0', '1', '区划树查询', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000020302020000000', '4000020302000000000', 'F', '区划新增', json_object('zh-CN', '区划新增', 'en-US', 'Add'), null, null, null, 'system:region:add', null, 2, '0', '0', '0', '1', '区划新增权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000020302030000000', '4000020302000000000', 'F', '区划修改', json_object('zh-CN', '区划修改', 'en-US', 'Edit'), null, null, null, 'system:region:update', null, 3, '0', '0', '0', '1', '区划修改权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000020302040000000', '4000020302000000000', 'F', '区划删除', json_object('zh-CN', '区划删除', 'en-US', 'Delete'), null, null, null, 'system:region:delete', null, 4, '0', '0', '0', '1', '区划删除权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000020104000000000', '4000020100000000000', 'C', '岗位管理', json_object('zh-CN', '岗位管理', 'en-US', 'Posts'), 'SystemPost', 'post', '/system/post/index', 'system:post:view', 'Suitcase', 4, '0', '0', '0', '1', '岗位管理页面', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020104010000000', '4000020104000000000', 'F', '岗位查询', json_object('zh-CN', '岗位查询', 'en-US', 'List'), null, null, null, 'system:post:list', null, 1, '0', '0', '0', '1', '岗位查询权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020104020000000', '4000020104000000000', 'F', '岗位新增', json_object('zh-CN', '岗位新增', 'en-US', 'Add'), null, null, null, 'system:post:add', null, 2, '0', '0', '0', '1', '岗位新增权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020104030000000', '4000020104000000000', 'F', '岗位修改', json_object('zh-CN', '岗位修改', 'en-US', 'Edit'), null, null, null, 'system:post:update', null, 3, '0', '0', '0', '1', '岗位修改权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020104040000000', '4000020104000000000', 'F', '岗位删除', json_object('zh-CN', '岗位删除', 'en-US', 'Delete'), null, null, null, 'system:post:delete', null, 4, '0', '0', '0', '1', '岗位删除权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020303000000000', '4000020300000000000', 'C', '字典管理', json_object('zh-CN', '字典管理', 'en-US', 'Dictionaries'), 'SystemDict', 'dict', '/system/dict/index', 'system:dict:view', 'CollectionTag', 3, '0', '0', '0', '1', '字典管理页面', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000020303010000000', '4000020303000000000', 'F', '字典查询', json_object('zh-CN', '字典查询', 'en-US', 'List'), null, null, null, 'system:dict:list', null, 1, '0', '0', '0', '1', '字典查询权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000020303020000000', '4000020303000000000', 'F', '字典新增', json_object('zh-CN', '字典新增', 'en-US', 'Add'), null, null, null, 'system:dict:add', null, 2, '0', '0', '0', '1', '字典新增权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000020303030000000', '4000020303000000000', 'F', '字典修改', json_object('zh-CN', '字典修改', 'en-US', 'Edit'), null, null, null, 'system:dict:update', null, 3, '0', '0', '0', '1', '字典修改权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000020303040000000', '4000020303000000000', 'F', '字典删除', json_object('zh-CN', '字典删除', 'en-US', 'Delete'), null, null, null, 'system:dict:delete', null, 4, '0', '0', '0', '1', '字典删除权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000020304000000000', '4000020300000000000', 'C', '参数管理', json_object('zh-CN', '参数管理', 'en-US', 'Parameters'), 'SystemConfig', 'config', '/system/config/index', 'system:config:view', 'Tools', 4, '0', '0', '0', '1', '参数管理页面', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000020304010000000', '4000020304000000000', 'F', '参数查询', json_object('zh-CN', '参数查询', 'en-US', 'List'), null, null, null, 'system:config:list', null, 1, '0', '0', '0', '1', '参数查询权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000020304020000000', '4000020304000000000', 'F', '参数新增', json_object('zh-CN', '参数新增', 'en-US', 'Add'), null, null, null, 'system:config:add', null, 2, '0', '0', '0', '1', '参数新增权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000020304030000000', '4000020304000000000', 'F', '参数修改', json_object('zh-CN', '参数修改', 'en-US', 'Edit'), null, null, null, 'system:config:update', null, 3, '0', '0', '0', '1', '参数修改权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000020304040000000', '4000020304000000000', 'F', '参数删除', json_object('zh-CN', '参数删除', 'en-US', 'Delete'), null, null, null, 'system:config:delete', null, 4, '0', '0', '0', '1', '参数删除权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000020305000000000', '4000020300000000000', 'C', '产品管理', json_object('zh-CN', '产品管理', 'en-US', 'Products'), 'SystemProduct', 'product', '/system/product/index', 'system:product:view', 'Grid', 5, '0', '0', '0', '1', 'Console 顶栏产品宇宙', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000020305010000000', '4000020305000000000', 'F', '产品查询', json_object('zh-CN', '产品查询', 'en-US', 'List'), null, null, null, 'system:product:list', null, 1, '0', '0', '0', '1', '产品列表权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000020305020000000', '4000020305000000000', 'F', '产品新增', json_object('zh-CN', '产品新增', 'en-US', 'Add'), null, null, null, 'system:product:add', null, 2, '0', '0', '0', '1', '产品新增权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000020305030000000', '4000020305000000000', 'F', '产品修改', json_object('zh-CN', '产品修改', 'en-US', 'Edit'), null, null, null, 'system:product:update', null, 3, '0', '0', '0', '1', '产品修改权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000020305040000000', '4000020305000000000', 'F', '产品删除', json_object('zh-CN', '产品删除', 'en-US', 'Delete'), null, null, null, 'system:product:delete', null, 4, '0', '0', '0', '1', '产品删除权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000020205000000000', '4000020200000000000', 'C', '租户策略配置', json_object('zh-CN', '租户策略配置', 'en-US', 'Tenant Policies'), 'SystemTenantPolicy', 'tenantPolicy', '/system/tenantPolicy/index', 'system:tenantPolicy:view', 'SetUp', 5, '0', '0', '0', '1', '租户可覆盖的安全/策略参数', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020205010000000', '4000020205000000000', 'F', '策略查询', json_object('zh-CN', '策略查询', 'en-US', 'List'), null, null, null, 'system:tenantPolicy:query', null, 1, '0', '0', '0', '1', '策略查询权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020205020000000', '4000020205000000000', 'F', '策略保存', json_object('zh-CN', '策略保存', 'en-US', 'Edit'), null, null, null, 'system:tenantPolicy:update', null, 2, '0', '0', '0', '1', '策略保存权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020204000000000', '4000020200000000000', 'C', '站点品牌', json_object('zh-CN', '站点品牌', 'en-US', 'Site Brand'), 'SystemBrandPack', 'brandPack', '/system/brandPack/index', 'system:brandPack:view', 'PictureFilled', 4, '0', '0', '0', '1', 'OEM 品牌包：产品名/多规格 Logo/版权；平台默认，租户可覆盖', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020204010000000', '4000020204000000000', 'F', '品牌查询', json_object('zh-CN', '品牌查询', 'en-US', 'Query'), null, null, null, 'system:brandPack:query', null, 1, '0', '0', '0', '1', '读取 sys_brand_pack', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020204020000000', '4000020204000000000', 'F', '品牌保存', json_object('zh-CN', '品牌保存', 'en-US', 'Save'), null, null, null, 'system:brandPack:update', null, 2, '0', '0', '0', '1', '保存 / 重置租户覆盖', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020201000000000', '4000020200000000000', 'C', '公告管理', json_object('zh-CN', '公告管理', 'en-US', 'Notices'), 'SystemNotice', 'notice', '/system/notice/index', 'system:notice:view', 'Bell', 1, '0', '0', '0', '1', '公告管理页面', 'TENANT', 'oa', '0', now(3), '0', now(3), 0),

('4000020201010000000', '4000020201000000000', 'F', '公告查询', json_object('zh-CN', '公告查询', 'en-US', 'List'), null, null, null, 'system:notice:list', null, 1, '0', '0', '0', '1', '公告查询权限', 'TENANT', 'oa', '0', now(3), '0', now(3), 0),

('4000020201020000000', '4000020201000000000', 'F', '公告新增', json_object('zh-CN', '公告新增', 'en-US', 'Add'), null, null, null, 'system:notice:add', null, 2, '0', '0', '0', '1', '公告新增权限', 'TENANT', 'oa', '0', now(3), '0', now(3), 0),

('4000020201030000000', '4000020201000000000', 'F', '公告修改', json_object('zh-CN', '公告修改', 'en-US', 'Edit'), null, null, null, 'system:notice:update', null, 3, '0', '0', '0', '1', '公告修改权限', 'TENANT', 'oa', '0', now(3), '0', now(3), 0),

('4000020201040000000', '4000020201000000000', 'F', '公告删除', json_object('zh-CN', '公告删除', 'en-US', 'Delete'), null, null, null, 'system:notice:delete', null, 4, '0', '0', '0', '1', '公告删除权限', 'TENANT', 'oa', '0', now(3), '0', now(3), 0),

('4000020202000000000', '4000020200000000000', 'C', '轮播图管理', json_object('zh-CN', '轮播图管理', 'en-US', 'Banners'), 'SystemBanner', 'banner', '/system/banner/index', 'system:banner:view', 'Picture', 2, '0', '0', '0', '1', '轮播图管理页面', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020202010000000', '4000020202000000000', 'F', '轮播图查询', json_object('zh-CN', '轮播图查询', 'en-US', 'List'), null, null, null, 'system:banner:list', null, 1, '0', '0', '0', '1', '轮播图查询权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020202020000000', '4000020202000000000', 'F', '轮播图新增', json_object('zh-CN', '轮播图新增', 'en-US', 'Add'), null, null, null, 'system:banner:add', null, 2, '0', '0', '0', '1', '轮播图新增权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020202030000000', '4000020202000000000', 'F', '轮播图修改', json_object('zh-CN', '轮播图修改', 'en-US', 'Edit'), null, null, null, 'system:banner:update', null, 3, '0', '0', '0', '1', '轮播图修改权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020202040000000', '4000020202000000000', 'F', '轮播图删除', json_object('zh-CN', '轮播图删除', 'en-US', 'Delete'), null, null, null, 'system:banner:delete', null, 4, '0', '0', '0', '1', '轮播图删除权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000020203000000000', '4000020200000000000', 'C', '站内信息', json_object('zh-CN', '站内信息', 'en-US', 'Site Messages'), 'SystemSiteMessage', 'siteMessage', '/system/siteMessage/index', 'system:siteMessage:view', 'Message', 3, '0', '0', '0', '1', '站内信息管理页面', 'TENANT', 'oa', '0', now(3), '0', now(3), 0),

('4000020203010000000', '4000020203000000000', 'F', '站内信查询', json_object('zh-CN', '站内信查询', 'en-US', 'List'), null, null, null, 'system:siteMessage:list', null, 1, '0', '0', '0', '1', '站内信查询权限', 'TENANT', 'oa', '0', now(3), '0', now(3), 0),

('4000020203020000000', '4000020203000000000', 'F', '站内信发送', json_object('zh-CN', '站内信发送', 'en-US', 'Send'), null, null, null, 'system:siteMessage:send', null, 2, '0', '0', '0', '1', '站内信发送权限', 'TENANT', 'oa', '0', now(3), '0', now(3), 0),

('4000020203030000000', '4000020203000000000', 'F', '站内信删除', json_object('zh-CN', '站内信删除', 'en-US', 'Delete'), null, null, null, 'system:siteMessage:delete', null, 3, '0', '0', '0', '1', '站内信删除权限', 'TENANT', 'oa', '0', now(3), '0', now(3), 0),

('4000030000000000000', '0', 'M', '租户管理', json_object('zh-CN', '租户管理', 'en-US', 'Tenant Management'), 'Tenant', '/tenant', null, null, 'Grid', 50, '0', '0', '0', '1', '租户管理目录', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000030001000000000', '4000030000000000000', 'C', '租户列表', json_object('zh-CN', '租户列表', 'en-US', 'Tenants'), 'TenantList', 'list', '/tenant/list/index', 'tenant:tenant:view', 'OfficeBuilding', 1, '0', '0', '0', '1', '租户管理页面', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000030001010000000', '4000030001000000000', 'F', '租户查询', json_object('zh-CN', '租户查询', 'en-US', 'List'), null, null, null, 'tenant:tenant:list', null, 1, '0', '0', '0', '1', '租户查询权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000030001020000000', '4000030001000000000', 'F', '租户新增', json_object('zh-CN', '租户新增', 'en-US', 'Add'), null, null, null, 'tenant:tenant:add', null, 2, '0', '0', '0', '1', '租户新增权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000030001030000000', '4000030001000000000', 'F', '租户修改', json_object('zh-CN', '租户修改', 'en-US', 'Edit'), null, null, null, 'tenant:tenant:update', null, 3, '0', '0', '0', '1', '租户修改权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000030001040000000', '4000030001000000000', 'F', '租户删除', json_object('zh-CN', '租户删除', 'en-US', 'Delete'), null, null, null, 'tenant:tenant:delete', null, 4, '0', '0', '0', '1', '租户删除权限', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000030002000000000', '4000030000000000000', 'C', '租户管理员', json_object('zh-CN', '租户管理员', 'en-US', 'Tenant Admins'), 'TenantAdmin', 'admin', '/tenant/admin/index', 'tenant:admin:view', 'UserFilled', 2, '0', '0', '0', '1', '平台运维租户超级管理员；预留进入租户', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000030002010000000', '4000030002000000000', 'F', '管理员查询', json_object('zh-CN', '管理员查询', 'en-US', 'List'), null, null, null, 'tenant:admin:list', null, 1, '0', '0', '0', '1', '租户管理员列表', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000030002020000000', '4000030002000000000', 'F', '重置密码', json_object('zh-CN', '重置密码', 'en-US', 'Reset Password'), null, null, null, 'tenant:admin:resetPassword', null, 2, '0', '0', '0', '1', '重置租户管理员初始密码', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000030002030000000', '4000030002000000000', 'F', '解锁账号', json_object('zh-CN', '解锁账号', 'en-US', 'Unlock'), null, null, null, 'tenant:admin:unlock', null, 3, '0', '0', '0', '1', '解除租户管理员登录锁定', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000040000000000000', '0', 'M', '安全治理', json_object('zh-CN', '安全治理', 'en-US', 'Security Governance'), 'Security', '/security', null, null, 'Lock', 40, '0', '0', '0', '1', '安全治理目录', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000040001000000000', '4000040000000000000', 'C', 'IP规则管理', json_object('zh-CN', 'IP规则管理', 'en-US', 'IP Rules'), 'SecurityIpRule', 'ipAccessRule', '/system/ipAccessRule/index', 'system:ipAccessRule:view', 'Connection', 1, '0', '0', '0', '1', 'IP规则管理页面', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000040001010000000', '4000040001000000000', 'F', 'IP规则查询', json_object('zh-CN', 'IP规则查询', 'en-US', 'List'), null, null, null, 'system:ipAccessRule:list', null, 1, '0', '0', '0', '1', 'IP规则查询权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000040001020000000', '4000040001000000000', 'F', 'IP规则新增', json_object('zh-CN', 'IP规则新增', 'en-US', 'Add'), null, null, null, 'system:ipAccessRule:add', null, 2, '0', '0', '0', '1', 'IP规则新增权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000040001030000000', '4000040001000000000', 'F', 'IP规则修改', json_object('zh-CN', 'IP规则修改', 'en-US', 'Edit'), null, null, null, 'system:ipAccessRule:update', null, 3, '0', '0', '0', '1', 'IP规则修改权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000040001040000000', '4000040001000000000', 'F', 'IP规则删除', json_object('zh-CN', 'IP规则删除', 'en-US', 'Delete'), null, null, null, 'system:ipAccessRule:delete', null, 4, '0', '0', '0', '1', 'IP规则删除权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000050000000000000', '0', 'M', '监控审计', json_object('zh-CN', '监控审计', 'en-US', 'Monitoring & Audit'), 'Monitor', '/monitor', null, null, 'DataAnalysis', 30, '0', '0', '0', '1', '监控审计目录', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000050002000000000', '4000050000000000000', 'C', '登录日志', json_object('zh-CN', '登录日志', 'en-US', 'Login Logs'), 'MonitorLoginLog', 'loginLog', '/system/loginLog/index', 'monitor:loginLog:view', 'List', 1, '0', '0', '0', '1', '登录日志页面', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000050002010000000', '4000050002000000000', 'F', '登录日志查询', json_object('zh-CN', '登录日志查询', 'en-US', 'List'), null, null, null, 'monitor:loginLog:list', null, 1, '0', '0', '0', '1', '登录日志查询权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000050002020000000', '4000050002000000000', 'F', '登录日志清理', json_object('zh-CN', '登录日志清理', 'en-US', 'Clear'), null, null, null, 'monitor:loginLog:clear', null, 2, '0', '0', '0', '1', '登录日志清理权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000050002030000000', '4000050002000000000', 'F', '登录日志导出', json_object('zh-CN', '登录日志导出', 'en-US', 'Export'), null, null, null, 'monitor:loginLog:export', null, 3, '0', '0', '0', '1', '登录日志导出权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000050003000000000', '4000050000000000000', 'C', '操作日志', json_object('zh-CN', '操作日志', 'en-US', 'Operation Logs'), 'MonitorOperationLog', 'operationLog', '/system/operationLog/index', 'monitor:operationLog:view', 'Tickets', 2, '0', '0', '0', '1', '操作日志页面', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000050003010000000', '4000050003000000000', 'F', '操作日志查询', json_object('zh-CN', '操作日志查询', 'en-US', 'List'), null, null, null, 'monitor:operationLog:list', null, 1, '0', '0', '0', '1', '操作日志查询权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000050003020000000', '4000050003000000000', 'F', '操作日志清理', json_object('zh-CN', '操作日志清理', 'en-US', 'Clear'), null, null, null, 'monitor:operationLog:clear', null, 2, '0', '0', '0', '1', '操作日志清理权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000050003030000000', '4000050003000000000', 'F', '操作日志导出', json_object('zh-CN', '操作日志导出', 'en-US', 'Export'), null, null, null, 'monitor:operationLog:export', null, 3, '0', '0', '0', '1', '操作日志导出权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000050004000000000', '4000050000000000000', 'C', '接口日志', json_object('zh-CN', '接口日志', 'en-US', 'API Access Logs'), 'MonitorApiLog', 'apiAccessLog', '/infra/apiAccessLog/index', 'monitor:apiLog:view', 'Document', 3, '0', '0', '0', '1', '接口日志页面', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000050004010000000', '4000050004000000000', 'F', '接口日志查询', json_object('zh-CN', '接口日志查询', 'en-US', 'List'), null, null, null, 'monitor:apiLog:list', null, 1, '0', '0', '0', '1', '接口日志查询权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000050004020000000', '4000050004000000000', 'F', '接口日志清理', json_object('zh-CN', '接口日志清理', 'en-US', 'Clear'), null, null, null, 'monitor:apiLog:clear', null, 2, '0', '0', '0', '1', '接口日志清理权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000050001000000000', '4000050000000000000', 'C', '在线用户', json_object('zh-CN', '在线用户', 'en-US', 'Online Users'), 'MonitorOnline', 'online', '/system/online/index', 'monitor:online:view', 'User', 0, '0', '0', '0', '1', '在线会话监控', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),
('4000050001010000000', '4000050001000000000', 'F', '在线用户查询', json_object('zh-CN', '在线用户查询', 'en-US', 'List'), null, null, null, 'monitor:online:list', null, 1, '0', '0', '0', '1', '在线用户列表', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),
('4000050001020000000', '4000050001000000000', 'F', '强制下线', json_object('zh-CN', '强制下线', 'en-US', 'Force Logout'), null, null, null, 'monitor:online:forceLogout', null, 2, '0', '0', '0', '1', '强制踢出会话', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000050006000000000', '4000050000000000000', 'C', '定时任务', json_object('zh-CN', '定时任务', 'en-US', 'Scheduled Jobs'), 'MonitorJob', 'job', '/system/job/index', 'monitor:job:view', 'Timer', 5, '0', '0', '0', '1', '平台定时任务', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000050006010000000', '4000050006000000000', 'F', '任务查询', json_object('zh-CN', '任务查询', 'en-US', 'List'), null, null, null, 'monitor:job:list', null, 1, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000050006020000000', '4000050006000000000', 'F', '任务执行', json_object('zh-CN', '任务执行', 'en-US', 'Run'), null, null, null, 'monitor:job:run', null, 2, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000050006030000000', '4000050006000000000', 'F', '任务修改', json_object('zh-CN', '任务修改', 'en-US', 'Update'), null, null, null, 'monitor:job:update', null, 3, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000050007000000000', '4000050000000000000', 'C', '任务日志', json_object('zh-CN', '任务日志', 'en-US', 'Job Logs'), 'MonitorJobLog', 'jobLog', '/system/jobLog/index', 'monitor:jobLog:view', 'Document', 6, '0', '0', '0', '1', '平台定时任务执行日志', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000050007010000000', '4000050007000000000', 'F', '日志查询', json_object('zh-CN', '日志查询', 'en-US', 'List'), null, null, null, 'monitor:jobLog:list', null, 1, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000050005000000000', '4000050000000000000', 'C', '数据库备份', json_object('zh-CN', '数据库备份', 'en-US', 'Database Backups'), 'MonitorDatabaseBackup', 'databaseBackup', '/infra/databaseBackup/index', 'monitor:dbBackup:view', 'Coin', 4, '0', '0', '0', '1', '数据库备份运维', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000050005010000000', '4000050005000000000', 'F', '备份查询', json_object('zh-CN', '备份查询', 'en-US', 'List'), null, null, null, 'monitor:dbBackup:list', null, 1, '0', '0', '0', '1', '备份文件列表', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000050005020000000', '4000050005000000000', 'F', '立即备份', json_object('zh-CN', '立即备份', 'en-US', 'Backup Now'), null, null, null, 'monitor:dbBackup:run', null, 2, '0', '0', '0', '1', '手动触发备份', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000050005030000000', '4000050005000000000', 'F', '删除备份', json_object('zh-CN', '删除备份', 'en-US', 'Delete'), null, null, null, 'monitor:dbBackup:delete', null, 3, '0', '0', '0', '1', '删除备份文件', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000050005040000000', '4000050005000000000', 'F', '备份配置', json_object('zh-CN', '备份配置', 'en-US', 'Edit'), null, null, null, 'monitor:dbBackup:edit', null, 4, '0', '0', '0', '1', '修改备份策略', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),

('4000060000000000000', '0', 'M', '系统工具', json_object('zh-CN', '系统工具', 'en-US', 'System Tools'), 'Tool', '/tool', null, null, 'SetUp', 60, '0', '0', '0', '1', '系统工具目录', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000060001000000000', '4000060000000000000', 'C', '代码生成', json_object('zh-CN', '代码生成', 'en-US', 'Code Generator'), 'ToolGen', 'gen', '/tool/gen/index', 'tool:gen:view', 'DocumentCopy', 1, '0', '0', '0', '1', '代码生成页面', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000060001010000000', '4000060001000000000', 'F', '生成查询', json_object('zh-CN', '生成查询', 'en-US', 'List'), null, null, null, 'tool:gen:list', null, 1, '0', '0', '0', '1', '代码生成列表', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000060001020000000', '4000060001000000000', 'F', '生成详情', json_object('zh-CN', '生成详情', 'en-US', 'View'), null, null, null, 'tool:gen:query', null, 2, '0', '0', '0', '1', '代码生成详情', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000060001030000000', '4000060001000000000', 'F', '导入表', json_object('zh-CN', '导入表', 'en-US', 'Import'), null, null, null, 'tool:gen:import', null, 3, '0', '0', '0', '1', '导入数据库表', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000060001040000000', '4000060001000000000', 'F', '生成修改', json_object('zh-CN', '生成修改', 'en-US', 'Edit'), null, null, null, 'tool:gen:edit', null, 4, '0', '0', '0', '1', '修改生成配置/同步', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000060001050000000', '4000060001000000000', 'F', '生成删除', json_object('zh-CN', '生成删除', 'en-US', 'Delete'), null, null, null, 'tool:gen:remove', null, 5, '0', '0', '0', '1', '删除生成配置', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000060001060000000', '4000060001000000000', 'F', '预览代码', json_object('zh-CN', '预览代码', 'en-US', 'Preview'), null, null, null, 'tool:gen:preview', null, 6, '0', '0', '0', '1', '预览生成代码', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000060001070000000', '4000060001000000000', 'F', '生成代码', json_object('zh-CN', '生成代码', 'en-US', 'Generate Code'), null, null, null, 'tool:gen:code', null, 7, '0', '0', '0', '1', '下载/写入代码', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),
('4000060002000000000', '4000060000000000000', 'C', '表单设计器', json_object('zh-CN', '表单设计器', 'en-US', 'Form Designer'), 'ToolFormDesigner', 'form-designer', '/tool/form-designer/index', 'tool:formDesigner:view', 'EditPen', 3, '0', '0', '0', '1', '通用 Form Designer SDK 沙箱：设计与多端预览，产出 FormDocument JSON', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000070000000000000', '0', 'M', '运维监控', json_object('zh-CN', '基础设施', 'en-US', 'Ops & Monitoring'), 'Infra', '/infra', null, null, 'Operation', 20, '0', '0', '0', '1', '异步任务等运维入口（platform）', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000070001000000000', '4000070000000000000', 'C', '异步任务', json_object('zh-CN', '异步任务', 'en-US', 'Async Tasks'), 'InfraAsyncTask', 'asyncTask', '/infra/asyncTask/index', 'infra:asyncTask:view', 'Clock', 1, '0', '0', '0', '1', '异步任务页面（只读+终态删除；操作审计见操作日志）', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000070001010000000', '4000070001000000000', 'F', '异步任务查询', json_object('zh-CN', '异步任务查询', 'en-US', 'List'), null, null, null, 'infra:asyncTask:list', null, 1, '0', '0', '0', '1', '异步任务查询权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000070001040000000', '4000070001000000000', 'F', '异步任务删除', json_object('zh-CN', '异步任务删除', 'en-US', 'Delete'), null, null, null, 'infra:asyncTask:delete', null, 2, '0', '0', '0', '1', '仅终态（成功/失败/取消）可删', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000070003000000000', '4000210002000000000', 'C', '文件管理', json_object('zh-CN', '文件管理', 'en-US', 'Files'), 'InfraFile', 'file', '/infra/file/index', 'infra:file:view', 'FolderOpened', 2, '0', '0', '0', '1', '文件管理页面', 'TENANT', 'files', '0', now(3), '0', now(3), 0),

('4000070003010000000', '4000070003000000000', 'F', '文件查询', json_object('zh-CN', '文件查询', 'en-US', 'List'), null, null, null, 'infra:file:list', null, 1, '0', '0', '0', '1', '文件查询权限', 'TENANT', 'files', '0', now(3), '0', now(3), 0),

('4000070003020000000', '4000070003000000000', 'F', '文件上传', json_object('zh-CN', '文件上传', 'en-US', 'Upload'), null, null, null, 'infra:file:upload', null, 2, '0', '0', '0', '1', '文件上传权限', 'TENANT', 'files', '0', now(3), '0', now(3), 0),

('4000070003030000000', '4000070003000000000', 'F', '文件下载', json_object('zh-CN', '文件下载', 'en-US', 'Download'), null, null, null, 'infra:file:download', null, 3, '0', '0', '0', '1', '文件下载权限', 'TENANT', 'files', '0', now(3), '0', now(3), 0),

('4000070003040000000', '4000070003000000000', 'F', '文件删除', json_object('zh-CN', '文件删除', 'en-US', 'Delete'), null, null, null, 'infra:file:delete', null, 4, '0', '0', '0', '1', '文件删除权限', 'TENANT', 'files', '0', now(3), '0', now(3), 0),

('4000070003050000000', '4000070003000000000', 'F', '敏感文件下载', json_object('zh-CN', '敏感文件下载', 'en-US', 'Sensitive download'), null, null, null, 'infra:file:download:sensitive', null, 5, '0', '0', '0', '1', '机密密级文件下载权限', 'TENANT', 'files', '0', now(3), '0', now(3), 0),

-- 文件配置中心：文件目录 → 槽位配置 → 目录槽位 → 策略配置（策略按槽位 biz_type_pattern）
('4000070100000000000', '0', 'M', '文件配置中心', json_object('zh-CN', '文件配置中心', 'en-US', 'File Config'), 'InfraFileConfig', '/infra/fileConfig', null, null, 'Folder', 20, '0', '0', '0', '1', '材料目录、槽位绑定、上传约束与下载访问策略', 'TENANT', 'files', '0', now(3), '0', now(3), 0),

('4000070199010000000', '4000070100000000000', 'F', '配置查询', json_object('zh-CN', '配置查询', 'en-US', 'Query'), null, null, null, 'infra:fileProfile:query', null, 1, '0', '0', '0', '1', '查询目录/槽位配置', 'TENANT', 'files', '0', now(3), '0', now(3), 0),

('4000070199020000000', '4000070100000000000', 'F', '配置新增', json_object('zh-CN', '配置新增', 'en-US', 'Add'), null, null, null, 'infra:fileProfile:add', null, 2, '0', '0', '0', '1', '新增平台目录/槽位', 'TENANT', 'files', '0', now(3), '0', now(3), 0),

('4000070199030000000', '4000070100000000000', 'F', '配置修改', json_object('zh-CN', '配置修改', 'en-US', 'Update'), null, null, null, 'infra:fileProfile:update', null, 3, '0', '0', '0', '1', '保存规则/租户覆盖', 'TENANT', 'files', '0', now(3), '0', now(3), 0),

('4000070199040000000', '4000070100000000000', 'F', '配置删除', json_object('zh-CN', '配置删除', 'en-US', 'Delete'), null, null, null, 'infra:fileProfile:delete', null, 4, '0', '0', '0', '1', '删除平台目录/槽位', 'TENANT', 'files', '0', now(3), '0', now(3), 0),

('4000070101000000000', '4000070100000000000', 'C', '文件目录', json_object('zh-CN', '文件目录', 'en-US', 'File Catalogs'), 'InfraFileCatalog', 'catalog', '/infra/fileConfig/catalog/index', 'infra:fileCatalog:view', 'Files', 1, '0', '0', '0', '1', '场景材料目录（编排多槽位）', 'TENANT', 'files', '0', now(3), '0', now(3), 0),

('4000070102000000000', '4000070100000000000', 'C', '槽位配置', json_object('zh-CN', '槽位配置', 'en-US', 'Slot Profiles'), 'InfraFileProfile', 'profile', '/infra/fileConfig/profile/index', 'infra:fileProfile:view', 'SetUp', 2, '0', '0', '0', '1', '上传槽位：扩展名/大小/数量；平台基线，租户边界内可覆盖', 'TENANT', 'files', '0', now(3), '0', now(3), 0),

('4000070103000000000', '4000070100000000000', 'C', '目录槽位', json_object('zh-CN', '目录槽位', 'en-US', 'Catalog Slots'), 'InfraFileCatalogItem', 'catalogItem', '/infra/fileConfig/catalogItem/index', 'infra:fileCatalogItem:view', 'List', 3, '0', '0', '0', '1', '目录内槽位绑定与场景级数量覆盖', 'TENANT', 'files', '0', now(3), '0', now(3), 0),

('4000070104000000000', '4000070100000000000', 'C', '策略配置', json_object('zh-CN', '策略配置', 'en-US', 'Access Policies'), 'InfraFileAccessPolicy', 'accessPolicy', '/infra/fileConfig/accessPolicy/index', 'infra:fileAccessPolicy:view', 'Lock', 4, '0', '0', '0', '1', '下载访问策略：按槽位/biz_type 匹配（非目录）', 'PLATFORM', 'files', '0', now(3), '0', now(3), 0),

('4000070104010000000', '4000070104000000000', 'F', '策略查询', json_object('zh-CN', '策略查询', 'en-US', 'Query'), null, null, null, 'infra:fileAccessPolicy:query', null, 1, '0', '0', '0', '1', '查询访问策略', 'PLATFORM', 'files', '0', now(3), '0', now(3), 0),

('4000070104020000000', '4000070104000000000', 'F', '策略新增', json_object('zh-CN', '策略新增', 'en-US', 'Add'), null, null, null, 'infra:fileAccessPolicy:add', null, 2, '0', '0', '0', '1', '新增访问策略', 'PLATFORM', 'files', '0', now(3), '0', now(3), 0),

('4000070104030000000', '4000070104000000000', 'F', '策略修改', json_object('zh-CN', '策略修改', 'en-US', 'Update'), null, null, null, 'infra:fileAccessPolicy:update', null, 3, '0', '0', '0', '1', '修改访问策略', 'PLATFORM', 'files', '0', now(3), '0', now(3), 0),

('4000070104040000000', '4000070104000000000', 'F', '策略删除', json_object('zh-CN', '策略删除', 'en-US', 'Delete'), null, null, null, 'infra:fileAccessPolicy:delete', null, 4, '0', '0', '0', '1', '删除访问策略', 'PLATFORM', 'files', '0', now(3), '0', now(3), 0),

('4000070004000000000', '4000070000000000000', 'C', '第三方调用日志', json_object('zh-CN', '第三方调用日志', 'en-US', 'Third-Party Call Logs'), 'InfraThirdPartyCallLog', 'thirdPartyCallLog', '/infra/thirdPartyCallLog/index', 'infra:thirdPartyCallLog:view', 'Connection', 4, '0', '0', '0', '1', '第三方调用日志页面', 'TENANT', 'platform', '0', now(3), '0', now(3), 0),

('4000070004010000000', '4000070004000000000', 'F', '第三方调用日志查询', json_object('zh-CN', '第三方调用日志查询', 'en-US', 'List'), null, null, null, 'infra:thirdPartyCallLog:list', null, 1, '0', '0', '0', '1', '第三方调用日志查询权限', 'TENANT', 'platform', '0', now(3), '0', now(3), 0);

-- 平台定时任务种子（与 ManagedJobCatalog 对齐；database-backup 默认暂停）
insert into sys_job (
  id, tenant_id, job_code, job_name, job_group, cron_expression,
  misfire_policy, allow_concurrent, status, remark,
  created_by, created_at, updated_by, updated_at, deleted
) values
(
  '6200000000000000001', '0', 'audit-log-retention', '审计日志归档清理', 'PLATFORM',
  '0 0 3 * * ?', '3', '0', '0', null,
  '0', now(3), '0', now(3), 0
),
(
  '6200000000000000002', '0', 'database-backup', '数据库备份', 'PLATFORM',
  '0 0 2 * * ?', '3', '0', '1', null,
  '0', now(3), '0', now(3), 0
),
(
  '6200000000000000003', '0', 'infra-file-gc', '文件垃圾回收', 'PLATFORM',
  '0 30 4 * * ?', '3', '0', '0', null,
  '0', now(3), '0', now(3), 0
),
(
  '6200000000000000009', '0', 'async-task-pending-reconcile', '异步任务排队对账', 'PLATFORM',
  '0 */2 * * * ?', '3', '0', '0', 'PENDING 超时重投；RUNNING 超时置失败',
  '0', now(3), '0', now(3), 0
);

-- i18n 管理菜单

-- 4. 国际化管理菜单（系统管理下）
-- ==========================================
insert into sys_menu (id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path, permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope, created_by, created_at, updated_by, updated_at, deleted) select '4000020401000000000', '4000020400000000000', 'C', '语言配置', json_object('zh-CN', '语言配置', 'en-US', 'Locales'), 'SystemI18nLocale', 'i18n/locale', '/system/i18n/locale/index', 'system:i18nLocale:view', 'Globe', 1, '0', '0', '0', '1', '国际化语言配置', 'PLATFORM', '0', now(3), '0', now(3), 0
where not exists (select 1 from sys_menu where id = '4000020401000000000');

insert into sys_menu (id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path, permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope, created_by, created_at, updated_by, updated_at, deleted) select '4000020402000000000', '4000020400000000000', 'C', '文案管理', json_object('zh-CN', '文案管理', 'en-US', 'I18n Messages'), 'SystemI18nMessage', 'i18n/message', '/system/i18n/message/index', 'system:i18nMessage:view', 'Document', 2, '0', '0', '0', '1', '国际化文案管理', 'PLATFORM', '0', now(3), '0', now(3), 0
where not exists (select 1 from sys_menu where id = '4000020402000000000');

-- 按钮权限：语言配置
insert into sys_menu (id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path, permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope, created_by, created_at, updated_by, updated_at, deleted) select '4000020401010000000', '4000020401000000000', 'F', '语言查询', json_object('zh-CN', '语言查询', 'en-US', 'List'), null, null, null, 'system:i18nLocale:list', null, 1, '0', '0', '0', '1', null, 'PLATFORM', '0', now(3), '0', now(3), 0
where not exists (select 1 from sys_menu where id = '4000020401010000000');

insert into sys_menu (id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path, permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope, created_by, created_at, updated_by, updated_at, deleted) select '4000020401020000000', '4000020401000000000', 'F', '语言新增', json_object('zh-CN', '语言新增', 'en-US', 'Add'), null, null, null, 'system:i18nLocale:add', null, 2, '0', '0', '0', '1', null, 'PLATFORM', '0', now(3), '0', now(3), 0
where not exists (select 1 from sys_menu where id = '4000020401020000000');

insert into sys_menu (id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path, permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope, created_by, created_at, updated_by, updated_at, deleted) select '4000020401030000000', '4000020401000000000', 'F', '语言修改', json_object('zh-CN', '语言修改', 'en-US', 'Edit'), null, null, null, 'system:i18nLocale:update', null, 3, '0', '0', '0', '1', null, 'PLATFORM', '0', now(3), '0', now(3), 0
where not exists (select 1 from sys_menu where id = '4000020401030000000');

insert into sys_menu (id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path, permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope, created_by, created_at, updated_by, updated_at, deleted) select '4000020401040000000', '4000020401000000000', 'F', '语言删除', json_object('zh-CN', '语言删除', 'en-US', 'Delete'), null, null, null, 'system:i18nLocale:delete', null, 4, '0', '0', '0', '1', null, 'PLATFORM', '0', now(3), '0', now(3), 0
where not exists (select 1 from sys_menu where id = '4000020401040000000');

-- 按钮权限：文案管理
insert into sys_menu (id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path, permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope, created_by, created_at, updated_by, updated_at, deleted) select '4000020402010000000', '4000020402000000000', 'F', '文案查询', json_object('zh-CN', '文案查询', 'en-US', 'List'), null, null, null, 'system:i18nMessage:list', null, 1, '0', '0', '0', '1', null, 'PLATFORM', '0', now(3), '0', now(3), 0
where not exists (select 1 from sys_menu where id = '4000020402010000000');

insert into sys_menu (id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path, permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope, created_by, created_at, updated_by, updated_at, deleted) select '4000020402020000000', '4000020402000000000', 'F', '文案新增', json_object('zh-CN', '文案新增', 'en-US', 'Add'), null, null, null, 'system:i18nMessage:add', null, 2, '0', '0', '0', '1', null, 'PLATFORM', '0', now(3), '0', now(3), 0
where not exists (select 1 from sys_menu where id = '4000020402020000000');

insert into sys_menu (id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path, permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope, created_by, created_at, updated_by, updated_at, deleted) select '4000020402030000000', '4000020402000000000', 'F', '文案修改', json_object('zh-CN', '文案修改', 'en-US', 'Edit'), null, null, null, 'system:i18nMessage:update', null, 3, '0', '0', '0', '1', null, 'PLATFORM', '0', now(3), '0', now(3), 0
where not exists (select 1 from sys_menu where id = '4000020402030000000');

insert into sys_menu (id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path, permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope, created_by, created_at, updated_by, updated_at, deleted) select '4000020402040000000', '4000020402000000000', 'F', '文案删除', json_object('zh-CN', '文案删除', 'en-US', 'Delete'), null, null, null, 'system:i18nMessage:delete', null, 4, '0', '0', '0', '1', null, 'PLATFORM', '0', now(3), '0', now(3), 0
where not exists (select 1 from sys_menu where id = '4000020402040000000');

-- 刷新缓存复用 system:i18nMessage:update（见 I18nMessageController.refreshCache），不单独建 F 菜单以免 permission_code 唯一约束冲突

-- AI 顶级菜单与授权（能力目录为唯一配置入口；文生图独立页）

insert into sys_menu (
  id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path,
  permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope, product_code,
  created_by, created_at, updated_by, updated_at, deleted
) values
('4000060100000000000', '0', 'M', 'AI 能力', json_object('zh-CN', 'AI 能力', 'en-US', 'AI Capabilities'), 'AiCapabilities', 'capabilities', null, null, 'MagicStick', 10, '0', '0', '0', '0', 'AI：能力目录、会话、文生图、智能建表、代码生成', 'TENANT', 'ai', '0', now(3), '0', now(3), 0),
('4000060100800000000', '4000060100000000000', 'C', 'AI 能力目录', json_object('zh-CN', 'AI 能力目录', 'en-US', 'AI Catalog'), 'ToolAiCatalog', null, '/tool/ai/catalog/index', 'tool:aiCatalog:view', 'Collection', 1, '0', '0', '0', '0', 'Provider / Model / 能力默认启用', 'TENANT', 'ai', '0', now(3), '0', now(3), 0),
('4000060199100000000', '4000060100800000000', 'F', 'AI目录查询', json_object('zh-CN', 'AI目录查询', 'en-US', 'Query'), null, null, null, 'tool:aiCatalog:query', null, 1, '0', '0', '0', '0', null, 'TENANT', 'ai', '0', now(3), '0', now(3), 0),
('4000060199110000000', '4000060100800000000', 'F', 'AI目录新增', json_object('zh-CN', 'AI目录新增', 'en-US', 'Add'), null, null, null, 'tool:aiCatalog:add', null, 2, '0', '0', '0', '0', null, 'TENANT', 'ai', '0', now(3), '0', now(3), 0),
('4000060199120000000', '4000060100800000000', 'F', 'AI目录修改', json_object('zh-CN', 'AI目录修改', 'en-US', 'Edit'), null, null, null, 'tool:aiCatalog:update', null, 3, '0', '0', '0', '0', null, 'TENANT', 'ai', '0', now(3), '0', now(3), 0),
('4000060199130000000', '4000060100800000000', 'F', 'AI目录删除', json_object('zh-CN', 'AI目录删除', 'en-US', 'Delete'), null, null, null, 'tool:aiCatalog:delete', null, 4, '0', '0', '0', '0', null, 'TENANT', 'ai', '0', now(3), '0', now(3), 0),
('4000060199010000000', '4000060100800000000', 'F', 'AI 功能查询', json_object('zh-CN', 'AI 功能查询', 'en-US', 'Query'), null, null, null, 'tool:ai:query', null, 1, '0', '0', '0', '0', '就绪探测、审计日志、会话列表', 'TENANT', 'ai', '0', now(3), '0', now(3), 0),
('4000060199030000000', '4000060100800000000', 'F', '页面规格生成', json_object('zh-CN', '页面规格生成', 'en-US', 'Generate Page Spec'), null, null, null, 'tool:ai:generate', null, 3, '0', '0', '0', '0', 'AI 生成 page-spec.yaml', 'TENANT', 'ai', '0', now(3), '0', now(3), 0),
('4000060102000000000', '4000060100000000000', 'C', 'AI 智能建表', json_object('zh-CN', 'AI 智能建表', 'en-US', 'AI Table Design'), 'ToolAiTable', null, '/tool/ai/table/index', null, 'Grid', 2, '0', '0', '0', '0', '从数据库导入或 AI 设计并创建新表', 'TENANT', 'ai', '0', now(3), '0', now(3), 0),
('4000060103000000000', '4000060100000000000', 'C', 'AI 代码生成助手', json_object('zh-CN', 'AI 代码生成助手', 'en-US', 'AI Code Assistant'), 'ToolAiCodegen', null, '/tool/ai/codegen/index', null, 'DocumentCopy', 3, '0', '0', '0', '0', '分步向导生成管理页面代码', 'TENANT', 'ai', '0', now(3), '0', now(3), 0),
('4000060104000000000', '4000060100000000000', 'C', 'AI 会话交流', json_object('zh-CN', 'AI 会话交流', 'en-US', 'AI Chat'), 'ToolAiChat', null, '/tool/ai/chat/index', null, 'ChatDotRound', 4, '0', '0', '0', '0', 'AI 对话助手', 'TENANT', 'ai', '0', now(3), '0', now(3), 0),
('4000060108000000000', '4000060100000000000', 'C', 'AI 文生图', json_object('zh-CN', 'AI 文生图', 'en-US', 'AI Text to Image'), 'ToolAiImage', null, '/tool/ai/image/index', null, 'Picture', 5, '0', '0', '0', '0', '提示词生成图片（image 能力）', 'TENANT', 'ai', '0', now(3), '0', now(3), 0),
('4000060199040000000', '4000060102000000000', 'F', 'AI 智能建表', json_object('zh-CN', 'AI 智能建表', 'en-US', 'AI Table Design'), null, null, null, 'tool:ai:table', null, 4, '0', '0', '0', '0', '智能建表操作权限', 'TENANT', 'ai', '0', now(3), '0', now(3), 0),
('4000060199050000000', '4000060103000000000', 'F', 'AI 代码生成', json_object('zh-CN', 'AI 代码生成', 'en-US', 'Generate Code'), null, null, null, 'tool:ai:codegen', null, 5, '0', '0', '0', '0', '代码生成助手操作权限', 'TENANT', 'ai', '0', now(3), '0', now(3), 0),
('4000060199060000000', '4000060108000000000', 'F', 'AI 文生图', json_object('zh-CN', 'AI 文生图', 'en-US', 'Text to Image'), null, null, null, 'tool:ai:image', null, 6, '0', '0', '0', '0', '文生图操作权限', 'TENANT', 'ai', '0', now(3), '0', now(3), 0),
('4000060105000000000', '4000060100000000000', 'C', 'AI 调用记录', json_object('zh-CN', 'AI 调用记录', 'en-US', 'AI Invoke Logs'), 'ToolAiInvokeLog', null, '/tool/ai/log/invoke/index', null, 'Document', 6, '0', '0', '0', '0', 'AI 调用审计', 'TENANT', 'ai', '0', now(3), '0', now(3), 0),
('4000060106000000000', '4000060100000000000', 'C', 'AI 建表记录', json_object('zh-CN', 'AI 建表记录', 'en-US', 'AI DDL Logs'), 'ToolAiDdlLog', null, '/tool/ai/log/ddl/index', null, 'Tickets', 7, '0', '0', '0', '0', 'AI DDL 生成与执行记录', 'TENANT', 'ai', '0', now(3), '0', now(3), 0),
('4000060107000000000', '4000060100000000000', 'C', 'AI 对话记录', json_object('zh-CN', 'AI 对话记录', 'en-US', 'AI Chat Logs'), 'ToolAiChatLog', null, '/tool/ai/log/chat/index', null, 'ChatLineSquare', 8, '0', '0', '0', '0', 'AI 对话审计', 'TENANT', 'ai', '0', now(3), '0', now(3), 0);

-- 平台超级管理员：AI 菜单授权
insert into sys_role_menu (tenant_id, role_id, menu_id)
select '0', '2000000000000000001', m.id
from sys_menu m
where m.id in (
  '4000060100000000000',
  '4000060100800000000', '4000060199100000000', '4000060199110000000', '4000060199120000000', '4000060199130000000',
  '4000060199010000000', '4000060199030000000',
  '4000060102000000000', '4000060103000000000', '4000060104000000000', '4000060108000000000',
  '4000060199040000000', '4000060199050000000', '4000060199060000000',
  '4000060105000000000', '4000060106000000000', '4000060107000000000'
)
  and m.deleted = 0
  and not exists (
    select 1 from sys_role_menu rm
    where rm.tenant_id = '0' and rm.role_id = '2000000000000000001' and rm.menu_id = m.id
  );

-- 租户管理员：AI 使用菜单（含能力目录，可选用平台模型或自建连接）
insert into sys_role_menu (tenant_id, role_id, menu_id)
select r.tenant_id, r.id, m.id
from sys_role r
cross join sys_menu m
where r.role_code in ('tenant_admin', 'org_admin')
  and r.deleted = 0
  and r.tenant_id <> '0'
  and m.deleted = 0
  and m.id in (
    '4000060100000000000',
    '4000060100800000000', '4000060199100000000', '4000060199110000000', '4000060199120000000', '4000060199130000000',
    '4000060199010000000', '4000060199030000000',
    '4000060102000000000', '4000060103000000000', '4000060104000000000', '4000060108000000000',
    '4000060199040000000', '4000060199050000000', '4000060199060000000',
    '4000060105000000000', '4000060106000000000', '4000060107000000000'
  )
  and not exists (
    select 1 from sys_role_menu rm
    where rm.tenant_id = r.tenant_id and rm.role_id = r.id and rm.menu_id = m.id
  );

-- 平台默认 AI 连接示例（Catalog seed；可在能力目录增删改，非代码写死厂商）
-- 示例：DeepSeek openai_compatible；密钥在能力目录录入
insert into tool_ai_provider (
  id, tenant_id, provider_code, provider_name, protocol, base_url, api_key_cipher, timeout_seconds,
  status, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select
    '7000000000000000001' as id, '0' as tenant_id, 'deepseek' as provider_code, 'DeepSeek' as provider_name,
    'openai_compatible' as protocol, 'https://api.deepseek.com' as base_url, null as api_key_cipher,
    60 as timeout_seconds, '0' as status,
    'DeepSeek OpenAI-compatible API (https://api-docs.deepseek.com/zh-cn/)' as remark,
    '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
) t
where not exists (select 1 from tool_ai_provider p where p.id = t.id and p.deleted = 0);

insert into tool_ai_model (
  id, tenant_id, provider_id, model_code, model_name, capabilities, default_params_json,
  status, sort_no, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select
    '7000000000000000011' as id, '0' as tenant_id, '7000000000000000001' as provider_id,
    'deepseek-v4-flash' as model_code, 'DeepSeek V4 Flash' as model_name, 'chat' as capabilities,
    null as default_params_json, '0' as status, 10 as sort_no,
    'DeepSeek 官方模型（默认 chat）' as remark,
    '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select
    '7000000000000000012', '0', '7000000000000000001',
    'deepseek-v4-pro', 'DeepSeek V4 Pro', 'chat',
    null, '0', 20, 'DeepSeek 官方模型',
    '0', now(3), '0', now(3), 0
) t
where not exists (select 1 from tool_ai_model m where m.id = t.id and m.deleted = 0);

insert into tool_ai_capability_binding (
  id, tenant_id, capability, model_id, enabled, remark,
  created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select
    '7000000000000000021' as id, '0' as tenant_id, 'chat' as capability,
    '7000000000000000011' as model_id, 1 as enabled,
    'Platform default chat → deepseek-v4-flash' as remark,
    '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
) t
where not exists (select 1 from tool_ai_capability_binding b where b.id = t.id and b.deleted = 0);

-- ==========================================
-- 31. 初始化角色资源授权
-- 平台超级管理员授予全部资源
-- 默认租户管理员先授予通用租户内资源，不授予租户管理目录
-- ==========================================
insert into sys_role_menu (tenant_id, role_id, menu_id)
select '0', '2000000000000000001', m.id
from sys_menu m
where m.deleted = 0
  and not exists (
    select 1 from sys_role_menu rm
    where rm.tenant_id = '0' and rm.role_id = '2000000000000000001' and rm.menu_id = m.id
  );

insert into sys_role_menu (tenant_id, role_id, menu_id)
select '1000000000000000001', '2000000000000000002', m.id
from sys_menu m
where m.deleted = 0
  and m.assign_scope <> 'PLATFORM'
  and not exists (
    select 1 from sys_role_menu rm
    where rm.tenant_id = '1000000000000000001' and rm.role_id = '2000000000000000002' and rm.menu_id = m.id
  );

insert into sys_role_menu (tenant_id, role_id, menu_id)
select '1000000000000000002', '2000000000000000004', m.id
from sys_menu m
where m.deleted = 0
  and m.assign_scope <> 'PLATFORM'
  and not exists (
    select 1 from sys_role_menu rm
    where rm.tenant_id = '1000000000000000002' and rm.role_id = '2000000000000000004' and rm.menu_id = m.id
  );

-- ==========================================
-- 32. 初始化基础字典
-- ==========================================
insert into sys_dict_type (
  id, tenant_id, dict_name, dict_type, status, remark,
  created_by, created_at, updated_by, updated_at, deleted
) values
('5000000000000000001', '0', '通用状态', 'sys_common_status', '0', '系统通用状态字典', '0', now(3), '0', now(3), 0),
('5000000000000000002', '0', '显示状态', 'sys_visible_status', '0', '显示隐藏状态字典', '0', now(3), '0', now(3), 0),
('5000000000000000003', '0', '性别', 'sys_gender', '0', '性别字典', '0', now(3), '0', now(3), 0),
('5000000000000000004', '0', '公告类型', 'sys_notice_type', '0', '公告类型字典', '0', now(3), '0', now(3), 0),
('5000000000000000005', '0', '登录状态', 'sys_login_status', '0', '登录状态字典', '0', now(3), '0', now(3), 0),
('5000000000000000006', '0', '是否', 'sys_yes_no', '0', 'Y/N 是否字典', '0', now(3), '0', now(3), 0),
('5000000000000000007', '0', '参数类型', 'sys_config_type', '0', '系统参数内置/自定义', '0', now(3), '0', now(3), 0),
('5000000000000000008', '0', '菜单类型', 'sys_menu_type', '0', 'M/C/F 菜单类型', '0', now(3), '0', now(3), 0),
('5000000000000000009', '0', '数据范围', 'sys_data_scope', '0', 'sys_role.data_scope 角色数据权限范围', '0', now(3), '0', now(3), 0),
('5000000000000000014', '0', '用户类型', 'sys_user_type', '0', 'sys_user.user_type 用户类型', '0', now(3), '0', now(3), 0),
('5000000000000000015', '0', '公告状态', 'sys_notice_status', '0', 'sys_notice.status 公告状态', '0', now(3), '0', now(3), 0),
('5000000000000000016', '0', '操作状态', 'sys_operation_status', '0', 'sys_operation_log.operation_status 操作状态', '0', now(3), '0', now(3), 0),
('5000000000000000017', '0', 'IP匹配类型', 'sys_ip_match_type', '0', 'sys_ip_access_rule.match_type 匹配类型', '0', now(3), '0', now(3), 0),
('5000000000000000018', '0', 'IP规则类型', 'sys_ip_rule_type', '0', 'sys_ip_access_rule.rule_type 规则类型', '0', now(3), '0', now(3), 0),
('5000000000000000019', '0', 'IP应用范围', 'sys_ip_apply_scope', '0', 'sys_ip_access_rule.apply_scope 应用范围', '0', now(3), '0', now(3), 0),
('5000000000000000010', '0', '异步任务状态', 'infra_async_task_status', '0', 'infra_async_task.status 异步任务状态', '0', now(3), '0', now(3), 0),
('5000000000000000011', '0', '日志级别', 'infra_log_level', '0', '基础设施日志级别', '0', now(3), '0', now(3), 0),
('5000000000000000012', '0', '存储类型', 'infra_storage_type', '0', 'infra_file.storage_type 文件存储类型', '0', now(3), '0', now(3), 0),
('5000000000000000013', '0', '调用状态', 'infra_call_status', '0', 'infra_thirdparty_call_log.call_status 第三方调用状态', '0', now(3), '0', now(3), 0),
('5000000000000000022', '0', '区划层级', 'sys_region_level', '0', 'sys_region.region_level 0国家1省2市3区县4街道5社区', '0', now(3), '0', now(3), 0),
('5000000000000000023', '0', '部门类型', 'sys_dept_type', '0', 'sys_dept.dept_type 主管部门/内设机构', '0', now(3), '0', now(3), 0),
('5000000000000000024', '0', '部门行政层级', 'sys_admin_level', '0', 'sys_dept.admin_level 部门行政层级', '0', now(3), '0', now(3), 0),
('5000000000000000025', '0', '租户类型', 'sys_tenant_type', '0', 'sys_tenant.tenant_type 企业/单位', '0', now(3), '0', now(3), 0),
('5000000000000000026', '0', '站内信分类', 'sys_site_message_category', '0', 'sys_site_message.message_category 系统/业务', '0', now(3), '0', now(3), 0),
('5000000000000000027', '0', '站内信已读状态', 'sys_site_message_read_status', '0', '站内信收件已读/未读', '0', now(3), '0', now(3), 0),
('5000000000000000028', '0', '公告发布位置', 'sys_notice_publish_location', '0', '通知公告发布位置', '0', now(3), '0', now(3), 0),
('5000000000000000029', '0', '轮播展示位置', 'sys_banner_display_position', '0', '轮播图展示位置', '0', now(3), '0', now(3), 0),
('5000000000000000030', '0', '轮播跳转方式', 'sys_banner_link_type', '0', '轮播图点击跳转方式', '0', now(3), '0', now(3), 0),
('5000000000000000031', '0', '公告审批状态', 'sys_notice_approve_status', '0', 'sys_notice.approve_status 与 WorkflowBizApproveStatus 码值一致', '0', now(3), '0', now(3), 0),
('5000000000000000032', '0', '流程表单类型', 'wf_form_type', '0', 'wf_model.form_type 流程表单类型', '0', now(3), '0', now(3), 0),
('5000000000000000033', '0', '流程业务类型', 'wf_biz_type', '0', 'wf_model.biz_type 流程业务类型', '0', now(3), '0', now(3), 0),
('5000000000000000034', '0', '流程实例状态', 'wf_instance_status', '0', 'wf_biz_process.status 流程实例状态', '0', now(3), '0', now(3), 0),
('5000000000000000035', '0', '审批结果', 'wf_approve_result', '0', '工作流任务审批结果', '0', now(3), '0', now(3), 0),
('5000000000000000036', '0', '审批操作', 'wf_approve_action', '0', '工作流审批表单操作', '0', now(3), '0', now(3), 0),
('5000000000000000037', '0', '审批人策略', 'wf_approver_strategy', '0', 'wf_approver_rule.strategy_type', '0', now(3), '0', now(3), 0),
('5000000000000000038', '0', '审批人解析范围', 'wf_approver_resolve_scope', '0', 'wf_approver_rule.resolve_scope', '0', now(3), '0', now(3), 0),
('5000000000000000039', '0', '流程绑定模式', 'wf_binding_mode', '0', 'wf_biz_binding_policy.binding_mode', '0', now(3), '0', now(3), 0),
('5000000000000000040', '0', 'OA租户启用状态', 'oa_enable_status', '0', '[OA运行态] oa_tenant_template.enable_status，不进动态表单字段字典池', '0', now(3), '0', now(3), 0),
('5000000000000000041', '0', '字典值类型', 'sys_dict_value_type', '0', 'sys_dict_item.value_type', '0', now(3), '0', now(3), 0),
('5000000000000000042', '0', '字典回显样式', 'sys_dict_list_class', '0', 'sys_dict_item.list_class / Element Plus Tag', '0', now(3), '0', now(3), 0),
('5000000000000000043', '0', '请假类型', 'oa_leave_type', '0', '[OA字段选项] oa_leave.leaveType，scope=oa 字典池', '0', now(3), '0', now(3), 0),
('5000000000000000044', '0', 'OA审批状态', 'oa_approve_status', '0', '[OA运行态] oa_instance.approve_status，不进动态表单字段字典池', '0', now(3), '0', now(3), 0),
('5000000000000000050', '0', 'AI调用业务类型', 'tool_ai_invoke_biz_type', '0', 'tool_ai_invoke_log.biz_type', '0', now(3), '0', now(3), 0),
('5000000000000000051', '0', 'AI调用状态', 'tool_ai_invoke_status', '0', 'tool_ai_invoke_log.response_status 0成功1失败', '0', now(3), '0', now(3), 0),
('5000000000000000052', '0', 'AI建表记录类型', 'tool_ai_ddl_record_type', '0', 'tool_ai_ddl_record.record_type', '0', now(3), '0', now(3), 0),
('5000000000000000053', '0', 'AI建表记录状态', 'tool_ai_ddl_status', '0', 'tool_ai_ddl_record.status', '0', now(3), '0', now(3), 0),
('5000000000000000054', '0', '公告上架状态', 'sys_notice_release_status', '0', 'sys_notice.release_status 与审批 approve_status 分离', '0', now(3), '0', now(3), 0),
('5000000000000000055', '0', '业务审批状态', 'wf_biz_approve_status', '0', '[工作流] 独立业务表 approve_status（与 WorkflowBizApproveStatus 对齐）；已发申请等跨业务列表用；不进 OA 动态表单字段字典池', '0', now(3), '0', now(3), 0),
('5000000000000000056', '0', '定时任务启停', 'sys_job_status', '0', 'sys_job.status 0正常 1暂停', '0', now(3), '0', now(3), 0),
('5000000000000000057', '0', '定时任务运行状态', 'sys_job_run_status', '0', 'sys_job Redis/日志 QUEUED|RUNNING|SUCCESS|FAILED', '0', now(3), '0', now(3), 0),
('5000000000000000058', '0', '定时任务触发类型', 'sys_job_trigger_type', '0', 'sys_job_log.trigger_type CRON|MANUAL', '0', now(3), '0', now(3), 0);

insert into sys_dict_item (
  id, tenant_id, dict_type_id, dict_label, dict_value, value_type,
  css_class, icon, list_class, is_default, sort_no, status, remark,
  created_by, created_at, updated_by, updated_at, deleted
) values
('5100000000000000001', '0', '5000000000000000001', '正常', '0', 'string', null, null, 'success', 1, 1, '0', '通用状态-正常', '0', now(3), '0', now(3), 0),
('5100000000000000002', '0', '5000000000000000001', '停用', '1', 'string', null, null, 'danger', 0, 2, '0', '通用状态-停用', '0', now(3), '0', now(3), 0),

('5100000000000000003', '0', '5000000000000000002', '显示', '0', 'string', null, null, 'success', 1, 1, '0', '显示状态-显示', '0', now(3), '0', now(3), 0),
('5100000000000000004', '0', '5000000000000000002', '隐藏', '1', 'string', null, null, 'info', 0, 2, '0', '显示状态-隐藏', '0', now(3), '0', now(3), 0),

('5100000000000000005', '0', '5000000000000000003', '男', '0', 'string', null, null, 'primary', 0, 1, '0', '性别-男', '0', now(3), '0', now(3), 0),
('5100000000000000006', '0', '5000000000000000003', '女', '1', 'string', null, null, 'danger', 0, 2, '0', '性别-女', '0', now(3), '0', now(3), 0),
('5100000000000000007', '0', '5000000000000000003', '未知', '2', 'string', null, null, 'info', 1, 3, '0', '性别-未知', '0', now(3), '0', now(3), 0),

('5100000000000000008', '0', '5000000000000000004', '通知', '1', 'string', null, null, 'primary', 1, 1, '0', '公告类型-通知', '0', now(3), '0', now(3), 0),
('5100000000000000009', '0', '5000000000000000004', '公告', '2', 'string', null, null, 'warning', 0, 2, '0', '公告类型-公告', '0', now(3), '0', now(3), 0),
('5100000000000000290', '0', '5000000000000000004', '警告', '3', 'string', null, null, 'danger', 0, 3, '0', '公告类型-警告', '0', now(3), '0', now(3), 0),

('5100000000000000010', '0', '5000000000000000005', '成功', '0', 'string', null, null, 'success', 1, 1, '0', '登录状态-成功', '0', now(3), '0', now(3), 0),
('5100000000000000011', '0', '5000000000000000005', '失败', '1', 'string', null, null, 'danger', 0, 2, '0', '登录状态-失败', '0', now(3), '0', now(3), 0),

('5100000000000000012', '0', '5000000000000000006', '是', 'Y', 'string', null, null, 'success', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000013', '0', '5000000000000000006', '否', 'N', 'string', null, null, 'info', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000014', '0', '5000000000000000007', '内置', 'Y', 'string', null, null, 'warning', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000015', '0', '5000000000000000007', '自定义', 'N', 'string', null, null, 'info', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000016', '0', '5000000000000000008', '目录', 'M', 'string', null, null, 'primary', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000017', '0', '5000000000000000008', '菜单', 'C', 'string', null, null, 'success', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000018', '0', '5000000000000000008', '按钮', 'F', 'string', null, null, 'info', 0, 3, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000019', '0', '5000000000000000009', '全部', '1', 'string', null, null, 'primary', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000020', '0', '5000000000000000009', '本部门及以下', '2', 'string', null, null, 'info', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000021', '0', '5000000000000000009', '本部门', '3', 'string', null, null, 'success', 0, 3, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000022', '0', '5000000000000000009', '本人', '4', 'string', null, null, 'warning', 0, 4, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000023', '0', '5000000000000000009', '自定义', '5', 'string', null, null, 'danger', 0, 5, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000240', '0', '5000000000000000009', '本区划及下级', '6', 'string', null, null, 'info', 0, 6, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000241', '0', '5000000000000000009', '本区划', '7', 'string', null, null, 'success', 0, 7, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000242', '0', '5000000000000000009', '自定义区划', '8', 'string', null, null, 'warning', 0, 8, '0', null, '0', now(3), '0', now(3), 0),

('5100000000000000249', '0', '5000000000000000022', '国家', '0', 'string', null, null, 'primary', 1, 0, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000250', '0', '5000000000000000022', '省', '1', 'string', null, null, 'primary', 0, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000251', '0', '5000000000000000022', '市', '2', 'string', null, null, 'primary', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000252', '0', '5000000000000000022', '区县', '3', 'string', null, null, 'primary', 0, 3, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000253', '0', '5000000000000000022', '街道', '4', 'string', null, null, 'info', 0, 4, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000254', '0', '5000000000000000022', '社区', '5', 'string', null, null, 'info', 0, 5, '0', null, '0', now(3), '0', now(3), 0),

('5100000000000000260', '0', '5000000000000000023', '主管部门', 'COMPETENT', 'string', null, null, 'primary', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000261', '0', '5000000000000000023', '内设机构', 'INTERNAL', 'string', null, null, 'info', 0, 2, '0', null, '0', now(3), '0', now(3), 0),

('5100000000000000270', '0', '5000000000000000024', '省级', 'PROVINCE', 'string', null, null, 'primary', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000271', '0', '5000000000000000024', '市级', 'CITY', 'string', null, null, 'primary', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000272', '0', '5000000000000000024', '县级', 'COUNTY', 'string', null, null, 'info', 0, 3, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000273', '0', '5000000000000000024', '区县级', 'DISTRICT', 'string', null, null, 'info', 0, 4, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000274', '0', '5000000000000000024', '街道', 'STREET', 'string', null, null, 'info', 0, 5, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000275', '0', '5000000000000000024', '无', 'NONE', 'string', null, null, 'default', 0, 6, '0', null, '0', now(3), '0', now(3), 0),

('5100000000000000280', '0', '5000000000000000025', '企业租户', 'ENTERPRISE', 'string', null, null, 'primary', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000281', '0', '5000000000000000025', '单位租户', 'ORGANIZATION', 'string', null, null, 'success', 0, 2, '0', null, '0', now(3), '0', now(3), 0),

('5100000000000000282', '0', '5000000000000000026', '系统', 'system', 'string', null, null, 'primary', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000283', '0', '5000000000000000026', '业务', 'business', 'string', null, null, 'success', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000284', '0', '5000000000000000027', '未读', '0', 'string', null, null, 'danger', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000285', '0', '5000000000000000027', '已读', '1', 'string', null, null, 'info', 0, 2, '0', null, '0', now(3), '0', now(3), 0),

('5100000000000000291', '0', '5000000000000000028', '后台移动端首页', 'admin_mobile', 'string', null, null, 'primary', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000292', '0', '5000000000000000028', '后台业务首页', 'admin_biz', 'string', null, null, 'success', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000293', '0', '5000000000000000028', '用户移动端首页', 'user_mobile', 'string', null, null, 'info', 0, 3, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000294', '0', '5000000000000000029', '后台移动端首页', 'admin_mobile', 'string', null, null, 'primary', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000295', '0', '5000000000000000029', '后台业务首页', 'admin_biz', 'string', null, null, 'success', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000296', '0', '5000000000000000029', '用户移动端首页', 'user_mobile', 'string', null, null, 'info', 0, 3, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000297', '0', '5000000000000000029', '后台系统首页', 'admin_system', 'string', null, null, 'warning', 0, 4, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000298', '0', '5000000000000000030', '无跳转', '0', 'string', null, null, 'info', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000299', '0', '5000000000000000030', '跳转至URL', '1', 'string', null, null, 'primary', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000300', '0', '5000000000000000030', '跳转至路由', '2', 'string', null, null, 'success', 0, 3, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000310', '0', '5000000000000000031', '草稿', 'draft', 'string', null, null, 'info', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000311', '0', '5000000000000000031', '审批中', 'running', 'string', null, null, 'warning', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000312', '0', '5000000000000000031', '已通过', 'approved', 'string', null, null, 'success', 0, 3, '0', '通过后写 published_at，门户按 approved 展示', '0', now(3), '0', now(3), 0),
('5100000000000000313', '0', '5000000000000000031', '已驳回', 'rejected', 'string', null, null, 'danger', 0, 4, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000314', '0', '5000000000000000031', '已撤回', 'cancelled', 'string', null, null, 'info', 0, 5, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000315', '0', '5000000000000000031', '已关闭', 'closed', 'string', null, null, 'info', 0, 6, '0', '有审批历史后不可删除，仅可关闭', '0', now(3), '0', now(3), 0),
('5100000000000000316', '0', '5000000000000000031', '退回修改', 'returned', 'string', null, null, 'warning', 0, 7, '0', '审批人退回发起人修改（starterEdit）', '0', now(3), '0', now(3), 0),

('5100000000000000320', '0', '5000000000000000032', '系统表单', 'system', 'string', null, null, 'primary', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000321', '0', '5000000000000000032', '路由表单', 'route', 'string', null, null, 'success', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000322', '0', '5000000000000000032', '自定义表单', 'custom', 'string', null, null, 'info', 0, 3, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000323', '0', '5000000000000000033', '通知公告', 'sys_notice', 'string', null, 'Bell', 'primary', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000342', '0', '5000000000000000033', '请假', 'oa_leave', 'string', null, 'Calendar', 'success', 0, 2, '0', '租户内请假审批', '0', now(3), '0', now(3), 0),
('5100000000000000324', '0', '5000000000000000034', '进行中', 'running', 'string', null, null, 'warning', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000325', '0', '5000000000000000034', '已通过', 'approved', 'string', null, null, 'success', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000326', '0', '5000000000000000034', '已驳回', 'rejected', 'string', null, null, 'danger', 0, 3, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000327', '0', '5000000000000000034', '已撤回', 'cancelled', 'string', null, null, 'info', 0, 4, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000328', '0', '5000000000000000035', '通过', 'pass', 'string', null, null, 'success', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000329', '0', '5000000000000000035', '驳回', 'reject', 'string', null, null, 'danger', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000330', '0', '5000000000000000036', '通过', 'pass', 'string', null, null, 'success', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000331', '0', '5000000000000000036', '驳回', 'reject', 'string', null, null, 'danger', 0, 2, '0', null, '0', now(3), '0', now(3), 0),

('5100000000000000332', '0', '5000000000000000037', '角色', 'role', 'string', null, null, 'primary', 1, 1, '0', '按角色解析首个用户', '0', now(3), '0', now(3), 0),
('5100000000000000333', '0', '5000000000000000037', '固定用户', 'fixed_user', 'string', null, null, 'info', 0, 2, '0', '指定 userId', '0', now(3), '0', now(3), 0),
('5100000000000000334', '0', '5000000000000000037', '发起人部门角色', 'starter_dept_role', 'string', null, null, 'success', 0, 3, '0', '发起人同部门内角色', '0', now(3), '0', now(3), 0),
('5100000000000000335', '0', '5000000000000000037', '表达式', 'expression', 'string', null, null, 'warning', 0, 4, '0', 'Phase1 仅 starter', '0', now(3), '0', now(3), 0),

('5100000000000000336', '0', '5000000000000000038', '业务租户', 'biz_tenant', 'string', null, null, 'primary', 1, 1, '0', '在业务所属租户解析', '0', now(3), '0', now(3), 0),
('5100000000000000337', '0', '5000000000000000038', '平台租户', 'platform_tenant', 'string', null, null, 'warning', 0, 2, '0', '在平台租户解析', '0', now(3), '0', now(3), 0),

('5100000000000000338', '0', '5000000000000000039', '强制平台流程', 'platform_mandatory', 'string', null, null, 'warning', 0, 1, '0', '租户不可自建同 biz_type 模型', '0', now(3), '0', now(3), 0),
('5100000000000000339', '0', '5000000000000000039', '租户模型优先', 'tenant_preferred', 'string', null, null, 'primary', 1, 2, '0', '租户模型优先，无则回落平台（兼容）', '0', now(3), '0', now(3), 0),
('5100000000000000343', '0', '5000000000000000039', '租户必配流程', 'tenant_required', 'string', null, null, 'success', 0, 3, '0', '提交仅用本租户已部署模型，不回落平台', '0', now(3), '0', now(3), 0),

('5100000000000000340', '0', '5000000000000000040', '未启用', 'disabled', 'string', null, null, 'info', 0, 1, '0', 'OA租户模板未发布', '0', now(3), '0', now(3), 0),
('5100000000000000341', '0', '5000000000000000040', '已启用', 'enabled', 'string', null, null, 'success', 1, 2, '0', 'OA审批模板已发布', '0', now(3), '0', now(3), 0),
('5100000000000000037', '0', '5000000000000000014', '系统用户', 'system', 'string', null, null, 'primary', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000038', '0', '5000000000000000014', '平台用户', 'platform', 'string', null, null, 'success', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000039', '0', '5000000000000000015', '正常', '0', 'string', null, null, 'success', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000040', '0', '5000000000000000015', '关闭', '1', 'string', null, null, 'info', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000041', '0', '5000000000000000016', '成功', '0', 'string', null, null, 'success', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000042', '0', '5000000000000000016', '失败', '1', 'string', null, null, 'danger', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000043', '0', '5000000000000000017', '单IP', '1', 'string', null, null, 'primary', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000044', '0', '5000000000000000017', 'IP段', '2', 'string', null, null, 'success', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000045', '0', '5000000000000000017', 'CIDR', '3', 'string', null, null, 'warning', 0, 3, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000046', '0', '5000000000000000018', '允许', '1', 'string', null, null, 'success', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000047', '0', '5000000000000000018', '禁止', '2', 'string', null, null, 'danger', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000048', '0', '5000000000000000019', '登录', '1', 'string', null, null, 'primary', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000049', '0', '5000000000000000019', '后台访问', '2', 'string', null, null, 'success', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000050', '0', '5000000000000000019', '开放接口', '3', 'string', null, null, 'warning', 0, 3, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000051', '0', '5000000000000000019', '全部', '4', 'string', null, null, 'info', 0, 4, '0', null, '0', now(3), '0', now(3), 0),

('5100000000000000024', '0', '5000000000000000010', '待处理', '0', 'string', null, null, 'info', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000025', '0', '5000000000000000010', '处理中', '1', 'string', null, null, 'warning', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000026', '0', '5000000000000000010', '成功', '2', 'string', null, null, 'success', 0, 3, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000027', '0', '5000000000000000010', '失败', '3', 'string', null, null, 'danger', 0, 4, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000028', '0', '5000000000000000010', '取消', '4', 'string', null, null, 'info', 0, 5, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000029', '0', '5000000000000000011', 'INFO', 'INFO', 'string', null, null, 'primary', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000030', '0', '5000000000000000011', 'WARN', 'WARN', 'string', null, null, 'warning', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000031', '0', '5000000000000000011', 'ERROR', 'ERROR', 'string', null, null, 'danger', 0, 3, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000032', '0', '5000000000000000011', 'DEBUG', 'DEBUG', 'string', null, null, 'info', 0, 4, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000033', '0', '5000000000000000012', 'MinIO', 'minio', 'string', null, null, 'success', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000034', '0', '5000000000000000012', '本地', 'local', 'string', null, null, 'info', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000035', '0', '5000000000000000013', '成功', '0', 'string', null, null, 'success', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000036', '0', '5000000000000000013', '失败', '1', 'string', null, null, 'danger', 0, 2, '0', null, '0', now(3), '0', now(3), 0),

('5100000000000000401', '0', '5000000000000000041', '字符串', 'string', 'string', null, null, 'primary', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000402', '0', '5000000000000000041', '数字', 'number', 'string', null, null, 'info', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000403', '0', '5000000000000000041', '布尔', 'boolean', 'string', null, null, 'warning', 0, 3, '0', null, '0', now(3), '0', now(3), 0),

('5100000000000000411', '0', '5000000000000000042', '默认(primary)', 'primary', 'string', null, null, 'primary', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000412', '0', '5000000000000000042', '成功(success)', 'success', 'string', null, null, 'success', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000413', '0', '5000000000000000042', '信息(info)', 'info', 'string', null, null, 'info', 0, 3, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000414', '0', '5000000000000000042', '警告(warning)', 'warning', 'string', null, null, 'warning', 0, 4, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000415', '0', '5000000000000000042', '危险(danger)', 'danger', 'string', null, null, 'danger', 0, 5, '0', null, '0', now(3), '0', now(3), 0),

('5100000000000000420', '0', '5000000000000000043', '年假', 'annual', 'string', null, null, 'primary', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000421', '0', '5000000000000000043', '事假', 'personal', 'string', null, null, 'info', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000422', '0', '5000000000000000043', '病假', 'sick', 'string', null, null, 'warning', 0, 3, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000423', '0', '5000000000000000043', '调休', 'comp', 'string', null, null, 'success', 0, 4, '0', null, '0', now(3), '0', now(3), 0),

('5100000000000000430', '0', '5000000000000000044', '草稿', 'draft', 'string', null, null, 'info', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000431', '0', '5000000000000000044', '审批中', 'running', 'string', null, null, 'warning', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000432', '0', '5000000000000000044', '已通过', 'approved', 'string', null, null, 'success', 0, 3, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000433', '0', '5000000000000000044', '已驳回', 'rejected', 'string', null, null, 'danger', 0, 4, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000434', '0', '5000000000000000044', '已撤回', 'cancelled', 'string', null, null, 'info', 0, 5, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000435', '0', '5000000000000000044', '已关闭', 'closed', 'string', null, null, 'info', 0, 6, '0', '有审批记录后不可删除，仅可关闭保留痕迹', '0', now(3), '0', now(3), 0),
('5100000000000000436', '0', '5000000000000000044', '退回修改', 'returned', 'string', null, null, 'warning', 0, 7, '0', '审批人退回发起人修改（starterEdit）', '0', now(3), '0', now(3), 0),

('5100000000000000440', '0', '5000000000000000055', '草稿', 'draft', 'string', null, null, 'info', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000441', '0', '5000000000000000055', '审批中', 'running', 'string', null, null, 'warning', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000442', '0', '5000000000000000055', '已通过', 'approved', 'string', null, null, 'success', 0, 3, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000443', '0', '5000000000000000055', '已驳回', 'rejected', 'string', null, null, 'danger', 0, 4, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000444', '0', '5000000000000000055', '已撤回', 'cancelled', 'string', null, null, 'info', 0, 5, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000445', '0', '5000000000000000055', '已关闭', 'closed', 'string', null, null, 'info', 0, 6, '0', '有审批记录后不可删除，仅可关闭保留痕迹', '0', now(3), '0', now(3), 0),
('5100000000000000446', '0', '5000000000000000055', '退回修改', 'returned', 'string', null, null, 'warning', 0, 7, '0', '审批人退回发起人修改（starterEdit）', '0', now(3), '0', now(3), 0),

('5100000000000000447', '0', '5000000000000000056', '正常', '0', 'string', null, null, 'success', 1, 1, '0', '定时任务-正常', '0', now(3), '0', now(3), 0),
('5100000000000000448', '0', '5000000000000000056', '暂停', '1', 'string', null, null, 'warning', 0, 2, '0', '定时任务-暂停', '0', now(3), '0', now(3), 0),

('5100000000000000449', '0', '5000000000000000057', '排队中', 'QUEUED', 'string', null, null, 'info', 0, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000450', '0', '5000000000000000057', '执行中', 'RUNNING', 'string', null, null, 'warning', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000451', '0', '5000000000000000057', '成功', 'SUCCESS', 'string', null, null, 'success', 1, 3, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000452', '0', '5000000000000000057', '失败', 'FAILED', 'string', null, null, 'danger', 0, 4, '0', null, '0', now(3), '0', now(3), 0),

('5100000000000000453', '0', '5000000000000000058', '定时', 'CRON', 'string', null, null, 'primary', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000454', '0', '5000000000000000058', '手动', 'MANUAL', 'string', null, null, 'success', 0, 2, '0', null, '0', now(3), '0', now(3), 0),

('5100000000000000501', '0', '5000000000000000050', '配置测试', 'CONFIG_TEST', 'string', null, null, 'info', 0, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000502', '0', '5000000000000000050', '新建表 DDL', 'DDL_CREATE', 'string', null, null, 'primary', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000503', '0', '5000000000000000050', '改表 DDL', 'DDL_ALTER', 'string', null, null, 'warning', 0, 3, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000504', '0', '5000000000000000050', '表结构设计', 'TABLE_DESIGN', 'string', null, null, 'success', 0, 4, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000505', '0', '5000000000000000050', '表结构变更设计', 'TABLE_ALTER_DESIGN', 'string', null, null, 'warning', 0, 5, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000506', '0', '5000000000000000050', '页面方案分析', 'PAGE_SPEC_ANALYZE', 'string', null, null, 'info', 0, 6, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000507', '0', '5000000000000000050', '页面方案生成', 'PAGE_SPEC_GENERATE', 'string', null, null, 'primary', 0, 7, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000508', '0', '5000000000000000050', 'AI 对话', 'CHAT', 'string', null, null, 'success', 0, 8, '0', null, '0', now(3), '0', now(3), 0),

('5100000000000000511', '0', '5000000000000000051', '成功', '0', 'string', null, null, 'success', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000512', '0', '5000000000000000051', '失败', '1', 'string', null, null, 'danger', 0, 2, '0', null, '0', now(3), '0', now(3), 0),

('5100000000000000521', '0', '5000000000000000052', '新建表 DDL', 'CREATE_DDL', 'string', null, null, 'primary', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000522', '0', '5000000000000000052', '改表 DDL', 'ALTER_DDL', 'string', null, null, 'warning', 0, 2, '0', null, '0', now(3), '0', now(3), 0),

('5100000000000000531', '0', '5000000000000000053', '已生成', 'GENERATED', 'string', null, null, 'info', 1, 1, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000532', '0', '5000000000000000053', '已复制', 'COPIED', 'string', null, null, 'success', 0, 2, '0', null, '0', now(3), '0', now(3), 0),
('5100000000000000533', '0', '5000000000000000053', '已执行', 'EXECUTED', 'string', null, null, 'warning', 0, 3, '0', null, '0', now(3), '0', now(3), 0),

('5100000000000000540', '0', '5000000000000000054', '未发布', 'unpublished', 'string', null, null, 'info', 1, 1, '0', '审批通过后待上架', '0', now(3), '0', now(3), 0),
('5100000000000000541', '0', '5000000000000000054', '已发布', 'published', 'string', null, null, 'success', 0, 2, '0', '门户可见', '0', now(3), '0', now(3), 0);

-- ==========================================
-- 33. 初始化系统参数
-- ==========================================
insert into sys_config (
  id, tenant_id, config_name, config_key, config_value, config_type,
  remark, created_by, created_at, updated_by, updated_at, deleted
) values
('6000000000000000001', '0', '登录失败最大次数', 'security.login.max-fail-count', '5', 'Y', '连续失败达到该次数后锁定', '0', now(3), '0', now(3), 0),
('6000000000000000002', '0', '登录锁定分钟数', 'security.login.lock-minutes', '30', 'Y', '超过失败阈值后锁定分钟数', '0', now(3), '0', now(3), 0),
('6000000000000000003', '0', '密码有效天数', 'security.password.expire-days', '90', 'Y', '密码有效期', '0', now(3), '0', now(3), 0),
('6000000000000000004', '0', '是否强制修改初始密码', 'security.password.force-change-initial', 'false', 'Y', '首次登录后强制修改密码', '0', now(3), '0', now(3), 0),
('6000000000000000005', '0', '是否启用验证码', 'security.login.require-captcha', 'false', 'Y', '登录验证码开关', '0', now(3), '0', now(3), 0),
('6000000000000000006', '0', '密码最小长度', 'security.password.min-length', '10', 'Y', '密码最小长度（等保口令基线）', '0', now(3), '0', now(3), 0),
('6000000000000000007', '0', '密码要求大写字母', 'security.password.require-upper', 'true', 'Y', '密码复杂度规则', '0', now(3), '0', now(3), 0),
('6000000000000000008', '0', '密码要求数字', 'security.password.require-number', 'true', 'Y', '密码复杂度规则', '0', now(3), '0', now(3), 0),
('6000000000000000009', '0', '密码要求特殊字符', 'security.password.require-special', 'true', 'Y', '密码复杂度规则', '0', now(3), '0', now(3), 0),
('6000000000000000010', '0', 'IP默认策略', 'security.ip.default-policy', 'allow', 'Y', '无规则命中时的默认策略', '0', now(3), '0', now(3), 0),
('6000000000000000011', '0', '同IP登录失败最大次数', 'security.login.max-fail-count-per-ip', '20', 'Y', '同一客户端 IP 连续登录失败上限，0 表示不限制', '0', now(3), '0', now(3), 0),
('6000000000000000012', '0', '同IP登录锁定分钟数', 'security.login.ip-lock-minutes', '15', 'Y', 'IP 登录失败达阈值后的锁定分钟数', '0', now(3), '0', now(3), 0),
('6000000000000000013', '0', '密码历史禁止重用次数', 'security.password.history-count', '5', 'Y', '新密码不能与最近 N 次密码相同，0 表示不限制', '0', now(3), '0', now(3), 0),
('6000000000000000014', '0', '是否允许多端登录', 'security.login.allow-multi-login', 'false', 'Y', 'false 时同一账号仅允许一个有效会话（租户可覆盖）', '0', now(3), '0', now(3), 0),
('6000000000000000015', '0', '单端登录时踢出旧会话', 'security.login.single-login-kickout-oldest', 'true', 'Y', '不允许多端时：true=新登录踢旧会话，false=拒绝新登录', '0', now(3), '0', now(3), 0),
('6000000000000000016', '0', '密码要求小写字母', 'security.password.require-lower', 'true', 'Y', '密码复杂度规则', '0', now(3), '0', now(3), 0),
('6000000000000000017', '0', '标准初始密码', 'security.password.initial', 'StudyOnly@2026', 'Y', '新建用户留空或管理员重置密码时使用；须符合当前密码复杂度策略', '0', now(3), '0', now(3), 0),
('6000000000000000018', '0', '是否启用 MFA', 'security.mfa.enabled', 'true', 'Y', '本租户是否启用 TOTP 双因素（登录二步/绑定）', '0', now(3), '0', now(3), 0),
('6000000000000000019', '0', '管理员强制绑定 MFA', 'security.mfa.force-admin', 'true', 'Y', '对超管/超级角色/三权角色强制绑定 MFA', '0', now(3), '0', now(3), 0);

-- ==========================================
-- 34. 初始化默认IP规则
-- 先放一个平台级允许全部登录/后台访问的基础规则
-- 后续由平台管理员按环境收紧
-- ==========================================
insert into sys_ip_access_rule (
  id, tenant_id, rule_name, ip_address, match_type, rule_type, apply_scope,
  priority_no, status, start_at, end_at, remark,
  created_by, created_at, updated_by, updated_at, deleted
) values
(
  '7000000000000000001',
  '0',
  '平台默认允许全部IP',
  '0.0.0.0/0',
  '3',
  '1',
  '4',
  0,
  '0',
  null,
  null,
  '初始化默认放行规则，生产环境建议按需收紧',
  '0', now(3), '0', now(3), 0
);

-- ==========================================
-- 35. 初始化平台公告
-- ==========================================
insert into sys_notice (
  id, tenant_id, notice_title, notice_type, cover_file_id, notice_content, notice_summary,
  sort_no, publish_locations, approve_status, release_status, published_at, finished_at, status, remark,
  created_by, created_at, updated_by, updated_at, deleted
) values
(
  '8000000000000000001',
  '0',
  '平台初始化完成',
  '2',
  null,
  '<p>iCoolKJ Platform 初始化完成。含：平台/企业/单位三类租户样例，平台与租户部门岗位，全量菜单与授权。</p>',
  '平台初始化完成，含租户样例、部门岗位与全量菜单授权。',
  99,
  'admin_biz',
  'approved',
  'published',
  now(3),
  now(3),
  '0',
  '默认平台公告',
  '0', now(3), '0', now(3), 0
);

-- ==========================================
-- 35.1 初始化流程模型（公告发布审批）Design SoT + Emitter 契约 BPMN
-- ==========================================
insert into wf_model (
  id, tenant_id, model_key, model_name, category_name, biz_type, form_type, form_route,
  latest_version, status, process_config, remark, created_by, created_at, updated_by, updated_at, deleted
) values (
  '9000000000000000001',
  '0',
  'notice_publish',
  '公告发布审批',
  '公告',
  'sys_notice',
  'system',
  '/system/notice/approve',
  1,
  '0',
  '{"startAcl":{"userIds":[],"deptIds":[],"roleCodes":[],"postIds":[],"tenantIds":[]},"behavior":{"allowWithdraw":true,"commentRequired":false},"notify":{"onTodo":true,"onReject":true,"onComplete":true},"design":{"schemaVersion":1,"nodes":[{"nodeType":"approve","taskDefinitionKey":"deptLeaderApprove","name":"部门负责人审批","variableName":"deptLeaderUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"dept_approver\\",\\"tenant_admin\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential","assignee":{"strategyType":"role","roleCodes":["dept_approver","tenant_admin"],"resolveScope":"biz_tenant"}},{"nodeType":"approve","taskDefinitionKey":"platformAdminApprove","name":"平台管理员审批","variableName":"platformAdminUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"platform_super_admin\\"]}","resolveScope":"platform_tenant","sortNo":2,"passMode":"sequential","assignee":{"strategyType":"role","roleCodes":["platform_super_admin"],"resolveScope":"platform_tenant"}}]}}',
  '公告两级审批：部门负责人 → 平台管理员（design_json SoT）',
  '0', now(3), '0', now(3), 0
);

-- 35.2 公告绑定策略：强制平台流程，租户不可自建 sys_notice 模型；审批人规则可租户覆盖
insert into wf_biz_binding_policy (
  id, biz_type, binding_mode, allow_tenant_approver_override, remark,
  created_by, created_at, updated_by, updated_at, deleted
) values (
  '9000000000000000101',
  'sys_notice',
  'platform_mandatory',
  '1',
  '公告发布必须走平台 notice_publish 流程；租户可覆盖节点审批人规则',
  '0', now(3), '0', now(3), 0
);

insert into wf_approver_rule (
  id, tenant_id, model_id, task_definition_key, variable_name, strategy_type, strategy_config,
  resolve_scope, sort_no, status, remark, created_by, created_at, updated_by, updated_at, deleted
) values
(
  '9000000000000000201', '0', '9000000000000000001', 'deptLeaderApprove', 'deptLeaderUserId', 'role',
  '{"roleCodes":["dept_approver","tenant_admin"]}',
  'biz_tenant', 1, '0', '部门负责人：优先 dept_approver，回落 tenant_admin',
  '0', now(3), '0', now(3), 0
),
(
  '9000000000000000202', '0', '9000000000000000001', 'platformAdminApprove', 'platformAdminUserId', 'role',
  '{"roleCodes":["platform_super_admin"]}',
  'platform_tenant', 1, '0', '平台管理员终审',
  '0', now(3), '0', now(3), 0
);

-- Flowable UEL：种子写 UEL{...}，再 REPLACE 成 dollar+brace 表达式（勿在本文件字面量写 dollar-brace，Flyway 会当占位符）
set @uel_open := concat('$', '{');
set @bpmn_notice_publish := replace(
  '<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://www.icoolkj.com/system/notice">
  <process id="notice_publish" name="notice_publish" isExecutable="true">
    <startEvent id="startEvent" name="start"/>
    <userTask id="deptLeaderApprove" name="deptLeaderApprove">
      <extensionElements>
        <flowable:taskListener event="create" delegateExpression="UEL{workflowAssigneeTaskListener}"/>
      </extensionElements>
    </userTask>
    <exclusiveGateway id="gw_deptLeaderApprove" name="deptLeaderApproveResult" default="flowReject_deptLeaderApprove"/>
    <userTask id="platformAdminApprove" name="platformAdminApprove">
      <extensionElements>
        <flowable:taskListener event="create" delegateExpression="UEL{workflowAssigneeTaskListener}"/>
      </extensionElements>
    </userTask>
    <exclusiveGateway id="gw_platformAdminApprove" name="platformAdminApproveResult" default="flowReject_platformAdminApprove"/>
    <endEvent id="endApproved" name="endApproved"/>
    <endEvent id="endRejected" name="endRejected"/>
    <sequenceFlow id="flowStart" sourceRef="startEvent" targetRef="deptLeaderApprove"/>
    <sequenceFlow id="flowTask_deptLeaderApprove" sourceRef="deptLeaderApprove" targetRef="gw_deptLeaderApprove"/>
    <sequenceFlow id="flowApprove_deptLeaderApprove" sourceRef="gw_deptLeaderApprove" targetRef="platformAdminApprove">
      <conditionExpression xsi:type="tFormalExpression"><![CDATA[UEL{approved}]]></conditionExpression>
    </sequenceFlow>
    <sequenceFlow id="flowReject_deptLeaderApprove" sourceRef="gw_deptLeaderApprove" targetRef="endRejected"/>
    <sequenceFlow id="flowTask_platformAdminApprove" sourceRef="platformAdminApprove" targetRef="gw_platformAdminApprove"/>
    <sequenceFlow id="flowApprove_platformAdminApprove" sourceRef="gw_platformAdminApprove" targetRef="endApproved">
      <conditionExpression xsi:type="tFormalExpression"><![CDATA[UEL{approved}]]></conditionExpression>
    </sequenceFlow>
    <sequenceFlow id="flowReject_platformAdminApprove" sourceRef="gw_platformAdminApprove" targetRef="endRejected"/>
  </process>
</definitions>',
  'UEL{', @uel_open
);

insert into wf_model_version (
  id, tenant_id, model_id, version_no, bpmn_xml, design_json, deployment_id, process_definition_id, process_definition_key,
  published, remark, created_by, created_at, updated_by, updated_at, deleted
) values (
  '9000000000000000300',
  '0',
  '9000000000000000001',
  1,
  @bpmn_notice_publish,
  '{"schemaVersion":1,"nodes":[{"nodeType":"approve","taskDefinitionKey":"deptLeaderApprove","name":"部门负责人审批","variableName":"deptLeaderUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"dept_approver\\",\\"tenant_admin\\"]}","resolveScope":"biz_tenant","sortNo":1,"passMode":"sequential"},{"nodeType":"approve","taskDefinitionKey":"platformAdminApprove","name":"平台管理员审批","variableName":"platformAdminUserId","strategyType":"role","strategyConfig":"{\\"roleCodes\\":[\\"platform_super_admin\\"]}","resolveScope":"platform_tenant","sortNo":2,"passMode":"sequential"}]}',
  null,
  null,
  'notice_publish',
  1,
  'seed design SoT',
  '0', now(3), '0', now(3), 0
);


-- i18n 配置

insert into sys_config (
  id, tenant_id, config_name, config_key, config_value, config_type, remark,
  created_by, created_at, updated_by, updated_at, deleted
)
select t.id, t.tenant_id, t.config_name, t.config_key, t.config_value, t.config_type, t.remark,
       t.created_by, t.created_at, t.updated_by, t.updated_at, t.deleted
from (
  select
    '6000000000000000011' as id,
    '0' as tenant_id,
    '默认语言' as config_name,
    'system.i18n.default-locale' as config_key,
    'zh-CN' as config_value,
    'Y' as config_type,
    '管理端默认语言，可选 zh-CN / en-US' as remark,
    '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all
  select
    '6000000000000000012',
    '0',
    '支持的语言列表',
    'system.i18n.supported-locales',
    'zh-CN,en-US',
    'Y',
    '逗号分隔的语言代码列表',
    '0', now(3), '0', now(3), 0
) t
where not exists (select 1 from sys_config c where c.id = t.id and c.deleted = 0)
  and not exists (
    select 1 from sys_config c
    where c.config_key = t.config_key and c.tenant_id = t.tenant_id and c.deleted = 0
  );

-- 语言列表

insert into sys_i18n_locale (id, locale_code, locale_name, native_name, sort_no, is_default, status, remark)
select '5000000000000000001', 'zh-CN', '简体中文', '简体中文', 1, 'Y', '0', '默认语言'
where not exists (select 1 from sys_i18n_locale where locale_code = 'zh-CN');

insert into sys_i18n_locale (id, locale_code, locale_name, native_name, sort_no, is_default, status, remark)
select '5000000000000000002', 'en-US', 'English', 'English', 2, 'N', '0', 'English'
where not exists (select 1 from sys_i18n_locale where locale_code = 'en-US');

-- sys_bool 字典

-- V7: sys_bool 字典（char(1) 字段 '0'/'1'，与 sys_dict_item.dict_value 一致）

insert into sys_dict_type (id, tenant_id, dict_name, dict_type, status, remark, created_by, created_at, updated_by, updated_at, deleted)
select '5000000000000000020', '0', '是否(0/1)', 'sys_bool', '0', 'char(1) 0否 1是', '0', now(3), '0', now(3), 0
where not exists (select 1 from sys_dict_type t where t.dict_type = 'sys_bool' and t.deleted = 0);

insert into sys_dict_item (id, tenant_id, dict_type_id, dict_label, dict_value, value_type, css_class, list_class, is_default, sort_no, status, remark, created_by, created_at, updated_by, updated_at, deleted)
select '5100000000000000052', '0', '5000000000000000020', '否', '0', 'string', null, 'info', 1, 1, '0', null, '0', now(3), '0', now(3), 0
where not exists (select 1 from sys_dict_item i where i.dict_type_id = '5000000000000000020' and i.dict_value = '0' and i.deleted = 0);

insert into sys_dict_item (id, tenant_id, dict_type_id, dict_label, dict_value, value_type, css_class, list_class, is_default, sort_no, status, remark, created_by, created_at, updated_by, updated_at, deleted)
select '5100000000000000053', '0', '5000000000000000020', '是', '1', 'string', null, 'success', 0, 2, '0', null, '0', now(3), '0', now(3), 0
where not exists (select 1 from sys_dict_item i where i.dict_type_id = '5000000000000000020' and i.dict_value = '1' and i.deleted = 0);

-- 菜单授权范围字典

insert into sys_dict_type (id, tenant_id, dict_name, dict_type, status, remark, created_by, created_at, updated_by, updated_at, deleted)
select '5000000000000000021', '0', '菜单授权范围', 'sys_menu_assign_scope', '0', 'PLATFORM/TENANT', '0', now(3), '0', now(3), 0
where not exists (select 1 from sys_dict_type t where t.dict_type = 'sys_menu_assign_scope' and t.deleted = 0);

insert into sys_dict_item (id, tenant_id, dict_type_id, dict_label, dict_value, value_type, css_class, list_class, is_default, sort_no, status, remark, created_by, created_at, updated_by, updated_at, deleted)
select '5100000000000000054', '0', '5000000000000000021', '租户可授权', 'TENANT', 'string', null, 'success', 1, 1, '0', null, '0', now(3), '0', now(3), 0
where not exists (select 1 from sys_dict_item i where i.dict_type_id = '5000000000000000021' and i.dict_value = 'TENANT' and i.deleted = 0);

insert into sys_dict_item (id, tenant_id, dict_type_id, dict_label, dict_value, value_type, css_class, list_class, is_default, sort_no, status, remark, created_by, created_at, updated_by, updated_at, deleted)
select '5100000000000000055', '0', '5000000000000000021', '平台专属', 'PLATFORM', 'string', null, 'warning', 0, 2, '0', null, '0', now(3), '0', now(3), 0
where not exists (select 1 from sys_dict_item i where i.dict_type_id = '5000000000000000021' and i.dict_value = 'PLATFORM' and i.deleted = 0);


-- 备份 / 存储 / 头像 / 工作流等平台参数（不含 AI：AI 走 tool_ai_* 能力目录）

insert into sys_config (
  id, tenant_id, config_name, config_key, config_value, config_type, remark,
  created_by, created_at, updated_by, updated_at, deleted
)
select t.id, t.tenant_id, t.config_name, t.config_key, t.config_value, t.config_type, t.remark,
       t.created_by, t.created_at, t.updated_by, t.updated_at, t.deleted
from (
  select
    '6000000000000000020' as id, '0' as tenant_id,
    '备份输出目录' as config_name, 'infra.backup.output-dir' as config_key,
    './data/db-backup' as config_value, 'Y' as config_type,
    '备份文件目录（.sql/.sql.gz；API 手动 + worker 定时）' as remark,
    '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select
    '6000000000000000021', '0', '备份保留天数', 'infra.backup.retention-days',
    '14', 'Y', '每次成功后删除超过 N 天的备份文件',
    '0', now(3), '0', now(3), 0
  union all select
    '6000000000000000022', '0', '备份 Gzip 压缩', 'infra.backup.compress',
    'true', 'Y', 'mysqldump 成功后是否 Gzip 压缩',
    '0', now(3), '0', now(3), 0
  union all select
    '6000000000000000023', '0', '默认存储提供方', 'infra.storage.default-provider',
    'local', 'Y', '默认上传提供方：local / minio',
    '0', now(3), '0', now(3), 0
  union all select
    '6000000000000000024', '0', '启用的存储提供方', 'infra.storage.enabled-providers',
    'local,minio', 'Y', '已启用提供方，逗号分隔',
    '0', now(3), '0', now(3), 0
  union all select
    '6000000000000000025', '0', '单文件大小上限(MB)', 'infra.storage.max-file-size-mb',
    '100', 'Y', '上传单文件最大体积',
    '0', now(3), '0', now(3), 0
  union all select
    '6000000000000000026', '0', '允许上传后缀', 'infra.storage.allowed-extensions',
    '', 'Y', '白名单，逗号分隔；空表示不限制',
    '0', now(3), '0', now(3), 0
  union all select
    '6000000000000000027', '0', '禁止上传后缀', 'infra.storage.blocked-extensions',
    'exe,bat,sh,cmd,com,msi', 'Y', '黑名单，逗号分隔；优先于白名单',
    '0', now(3), '0', now(3), 0
  union all select
    '6000000000000000028', '0', '头像大小上限(MB)', 'infra.avatar.max-size-mb',
    '2', 'Y', '用户头像单文件最大体积',
    '0', now(3), '0', now(3), 0
  union all select
    '6000000000000000029', '0', '头像允许后缀', 'infra.avatar.allowed-extensions',
    'jpg,jpeg,png,webp,gif', 'Y', '头像白名单，逗号分隔',
    '0', now(3), '0', now(3), 0
  union all select
    '6000000000000000030', '0', '头像输出边长', 'infra.avatar.output-size',
    '200', 'Y', '裁剪后主图正方形边长(像素)',
    '0', now(3), '0', now(3), 0
  union all select
    '6000000000000000031', '0', '头像小图边长', 'infra.avatar.thumb-sm-size',
    '32', 'Y', '导航栏等小尺寸头像边长',
    '0', now(3), '0', now(3), 0
  union all select
    '6000000000000000032', '0', '头像中图边长', 'infra.avatar.thumb-md-size',
    '88', 'Y', '个人中心卡片等中尺寸头像边长',
    '0', now(3), '0', now(3), 0
  union all select
    '6000000000000000033', '0', '头像内容校验', 'infra.avatar.content-scan-enabled',
    'true', 'Y', '是否校验图片魔数与可读性',
    '0', now(3), '0', now(3), 0
  union all select
    '6000000000000000034', '0', '审批常用意见', 'workflow.approve.quick-comments',
    '{"pass":[{"id":"pass_agree","labels":{"zh-CN":"同意","en-US":"Agree"}},{"id":"pass_noted","labels":{"zh-CN":"同意，请知悉","en-US":"Agreed. Noted."}}],"reject":[{"id":"reject_docs","labels":{"zh-CN":"请补充证明材料","en-US":"Please provide supporting documents"}},{"id":"reject_date","labels":{"zh-CN":"日期有误，请修改后重提","en-US":"Dates look incorrect. Please revise and resubmit."}},{"id":"reject_reason","labels":{"zh-CN":"事由不清晰，请完善说明","en-US":"Please clarify the reason."}}]}',
    'Y', '审批通过/驳回快捷短语（多语言 labels）；租户可整包覆盖',
    '0', now(3), '0', now(3), 0
  union all select
    '6000000000000000035', '0', '流程通知模板', 'workflow.notify.templates',
    '{"URGE":{"title":"{eventLabel} · {bizTitle}","content":"{title}（实例 {instanceId}）"},"TIMEOUT":{"title":"{eventLabel} · {bizTitle}","content":"{title}（实例 {instanceId}）"}}',
    'Y', '事件→title/content 模式；占位 {eventLabel}{bizTitle}{instanceId}{title}；空则走 notify.workflow.* i18n',
    '0', now(3), '0', now(3), 0
) t
where not exists (select 1 from sys_config c where c.id = t.id and c.deleted = 0)
  and not exists (
    select 1 from sys_config c
    where c.tenant_id = t.tenant_id and c.config_key = t.config_key and c.deleted = 0
  );

-- 平台默认站点品牌 / OEM（独立表；租户可另插覆盖行；素材进 assets_json / labels_json）
insert into sys_brand_pack (
  id, tenant_id, product_name, product_name_i18n,
  copyright_brand, icp_no, icp_url, domain, site_url, email, contact_name, contact_phone,
  assets_json, labels_json,
  remark, created_by, created_at, updated_by, updated_at, deleted
)
select
  '6100000000000000001',
  '0',
  'iCoolKJ 平台',
  json_object('zh-CN', 'iCoolKJ 平台', 'en-US', 'iCoolKJ Platform'),
  'icoolkj',
  '请填写备案号',
  'https://beian.miit.gov.cn/',
  'icoolkj.com',
  'https://icoolkj.com',
  'icoolkj@163.com',
  null,
  null,
  json_object(),
  json_object(),
  '平台默认 OEM；业务租户另插行整包覆盖',
  '0', now(3), '0', now(3), 0
from dual
where not exists (
  select 1 from sys_brand_pack b where b.tenant_id = '0' and b.deleted = 0
);

-- 文件访问策略（对齐系统真实附件 biz_type；与 FileAccessPolicyBuiltin 一致，表可覆盖）
insert into infra_file_access_policy (
  id, biz_type_pattern, auth_mode, tenant_scope, visibility, anon_purposes, perm_any_of,
  sort_no, enabled, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select
    '6200000000000000001' as id,
    'brand_*' as biz_type_pattern,
    'SESSION_OR_ANON' as auth_mode,
    'OWN_OR_PLATFORM' as tenant_scope,
    'ALWAYS' as visibility,
    'LOGIN_BRAND' as anon_purposes,
    null as perm_any_of,
    10 as sort_no,
    '1' as enabled,
    null as remark,
    '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all
  select
    '6200000000000000002',
    'notice_*',
    'SESSION',
    'OWN_OR_PLATFORM',
    'CUSTOM',
    null,
    null,
    20,
    '1',
    null,
    '0', now(3), '0', now(3), 0
  union all
  select
    '6200000000000000005',
    'banner_*',
    'SESSION',
    'OWN_OR_PLATFORM',
    'ALWAYS',
    null,
    null,
    30,
    '1',
    null,
    '0', now(3), '0', now(3), 0
  union all
  select
    '6200000000000000007',
    'user-avatar',
    'SESSION',
    'OWN',
    'ALWAYS',
    null,
    null,
    40,
    '1',
    null,
    '0', now(3), '0', now(3), 0
  union all
  select
    '6200000000000000008',
    'oa_instance',
    'SESSION',
    'OWN',
    'CUSTOM',
    null,
    null,
    50,
    '1',
    null,
    '0', now(3), '0', now(3), 0
  union all
  select
    '6200000000000000011',
    'excel-import',
    'SESSION',
    'OWN',
    'CUSTOM',
    null,
    null,
    5,
    '1',
    null,
    '0', now(3), '0', now(3), 0
  union all
  select
    '6200000000000000012',
    'excel-export',
    'SESSION',
    'OWN',
    'CUSTOM',
    null,
    null,
    5,
    '1',
    null,
    '0', now(3), '0', now(3), 0
  union all
  select
    '6200000000000000013',
    'attachment_demo_*',
    'SESSION',
    'OWN',
    'ALWAYS',
    null,
    null,
    60,
    '1',
    null,
    '0', now(3), '0', now(3), 0
  union all
  select
    '6200000000000000004',
    '*',
    'SESSION',
    'OWN',
    'ALWAYS',
    null,
    null,
    9999,
    '1',
    null,
    '0', now(3), '0', now(3), 0
) t
where not exists (
  select 1 from infra_file_access_policy p where p.biz_type_pattern = t.biz_type_pattern and p.deleted = 0
);

-- 文件上传槽位 + 规则（严格对齐现有附件 biz_type，不含业务示例）
insert into infra_file_profile (
  id, tenant_id, profile_code, profile_name, profile_name_i18n, category_tag, status, remark,
  created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '6300000000000000001' as id, '0' as tenant_id, 'brand_logo_sm' as profile_code, '品牌小标 Logo' as profile_name,
    json_object('zh-CN', '品牌小标 Logo', 'en-US', 'Brand logo (sm)') as profile_name_i18n, 'brand' as category_tag, '0' as status,
    '侧栏折叠、列表缩略' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '6300000000000000002', '0', 'brand_logo_md', '品牌中标 Logo',
    json_object('zh-CN', '品牌中标 Logo', 'en-US', 'Brand logo (md)'), 'brand', '0', '侧栏展开、通用导航', '0', now(3), '0', now(3), 0
  union all select '6300000000000000003', '0', 'brand_logo_lg', '品牌大标 Logo',
    json_object('zh-CN', '品牌大标 Logo', 'en-US', 'Brand logo (lg)'), 'brand', '0', '登录页品牌区', '0', now(3), '0', now(3), 0
  union all select '6300000000000000004', '0', 'brand_favicon', '站点 Favicon',
    json_object('zh-CN', '站点 Favicon', 'en-US', 'Favicon'), 'brand', '0', '浏览器标签图标', '0', now(3), '0', now(3), 0
  union all select '6300000000000000005', '0', 'brand_login_bg', '登录页背景',
    json_object('zh-CN', '登录页背景', 'en-US', 'Login background'), 'brand', '0', '登录页背景图', '0', now(3), '0', now(3), 0
  union all select '6300000000000000006', '0', 'brand_qr_wechat', '微信二维码',
    json_object('zh-CN', '微信二维码', 'en-US', 'WeChat QR'), 'brand', '0', '登录页或门户客服/微信码', '0', now(3), '0', now(3), 0
  union all select '6300000000000000007', '0', 'brand_qr_official', '公众号二维码',
    json_object('zh-CN', '公众号二维码', 'en-US', 'Official account QR'), 'brand', '0', '公众号关注二维码', '0', now(3), '0', now(3), 0
  union all select '6300000000000000008', '0', 'notice_cover', '公告封面',
    json_object('zh-CN', '公告封面', 'en-US', 'Notice cover'), 'notice', '0', 'CoverImagePicker / NoticeFileConstants.BIZ_TYPE_COVER', '0', now(3), '0', now(3), 0
  union all select '6300000000000000009', '0', 'notice_richtext', '公告富文本图片',
    json_object('zh-CN', '公告富文本图片', 'en-US', 'Notice richtext image'), 'notice', '0', 'RichTextEditor / NoticeFileConstants.BIZ_TYPE_RICHTEXT', '0', now(3), '0', now(3), 0
  union all select '6300000000000000010', '0', 'banner_image', '轮播图图片',
    json_object('zh-CN', '轮播图图片', 'en-US', 'Banner image'), 'banner', '0', 'BannerFileConstants.BIZ_TYPE_IMAGE', '0', now(3), '0', now(3), 0
  union all select '6300000000000000011', '0', 'user-avatar', '用户头像',
    json_object('zh-CN', '用户头像', 'en-US', 'User avatar'), 'user', '0', 'UserAvatarConstants.BIZ_TYPE；对齐 infra.avatar.*', '0', now(3), '0', now(3), 0
  union all select '6300000000000000012', '0', 'oa_instance', 'OA 实例附件',
    json_object('zh-CN', 'OA 实例附件', 'en-US', 'OA instance attachment'), 'oa', '0', 'OaFileUpload / OaInstanceFileConstants.BIZ_TYPE', '0', now(3), '0', now(3), 0
) t
where not exists (
  select 1 from infra_file_profile p where p.tenant_id = t.tenant_id and p.profile_code = t.profile_code and p.deleted = 0
);

insert into infra_file_profile_rule (
  id, tenant_id, profile_code, allowed_exts, allowed_content_types, required, min_num, max_num,
  min_size_bytes, max_size_bytes, sort_no, status, remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  -- CoverImagePicker accept: image/jpeg,png,webp,gif；品牌槽位单文件
  select '6310000000000000001' as id, '0' as tenant_id, 'brand_logo_sm' as profile_code,
    'png,jpg,jpeg,webp,gif' as allowed_exts, 'image/png,image/jpeg,image/webp,image/gif' as allowed_content_types,
    '0' as required, 0 as min_num, 1 as max_num, 0 as min_size_bytes, 5242880 as max_size_bytes, 10 as sort_no,
    '0' as status, null as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '6310000000000000002', '0', 'brand_logo_md',
    'png,jpg,jpeg,webp,gif', 'image/png,image/jpeg,image/webp,image/gif', '0', 0, 1, 0, 5242880, 20, '0', null, '0', now(3), '0', now(3), 0
  union all select '6310000000000000003', '0', 'brand_logo_lg',
    'png,jpg,jpeg,webp,gif', 'image/png,image/jpeg,image/webp,image/gif', '0', 0, 1, 0, 5242880, 30, '0', null, '0', now(3), '0', now(3), 0
  union all select '6310000000000000004', '0', 'brand_favicon',
    'png,jpg,jpeg,webp,gif,ico', 'image/png,image/jpeg,image/webp,image/gif,image/x-icon,image/vnd.microsoft.icon', '0', 0, 1, 0, 1048576, 40, '0', null, '0', now(3), '0', now(3), 0
  union all select '6310000000000000005', '0', 'brand_login_bg',
    'png,jpg,jpeg,webp,gif', 'image/png,image/jpeg,image/webp,image/gif', '0', 0, 1, 0, 10485760, 50, '0', null, '0', now(3), '0', now(3), 0
  union all select '6310000000000000006', '0', 'brand_qr_wechat',
    'png,jpg,jpeg,webp,gif', 'image/png,image/jpeg,image/webp,image/gif', '0', 0, 1, 0, 5242880, 60, '0', null, '0', now(3), '0', now(3), 0
  union all select '6310000000000000007', '0', 'brand_qr_official',
    'png,jpg,jpeg,webp,gif', 'image/png,image/jpeg,image/webp,image/gif', '0', 0, 1, 0, 5242880, 70, '0', null, '0', now(3), '0', now(3), 0
  union all select '6310000000000000008', '0', 'notice_cover',
    'png,jpg,jpeg,webp,gif', 'image/png,image/jpeg,image/webp,image/gif', '0', 0, 1, 0, 5242880, 10, '0', null, '0', now(3), '0', now(3), 0
  union all select '6310000000000000009', '0', 'notice_richtext',
    'png,jpg,jpeg,webp,gif', 'image/png,image/jpeg,image/webp,image/gif', '0', 0, 50, 0, 5242880, 20, '0', null, '0', now(3), '0', now(3), 0
  union all select '6310000000000000010', '0', 'banner_image',
    'png,jpg,jpeg,webp,gif', 'image/png,image/jpeg,image/webp,image/gif', '0', 0, 1, 0, 5242880, 10, '0', null, '0', now(3), '0', now(3), 0
  -- 对齐 infra.avatar.max-size-mb=2、allowed-extensions
  union all select '6310000000000000011', '0', 'user-avatar',
    'jpg,jpeg,png,webp,gif', 'image/jpeg,image/png,image/webp,image/gif', '0', 0, 1, 0, 2097152, 10, '0', null, '0', now(3), '0', now(3), 0
  -- OaFileUpload 默认 maxFiles=5；类型走全局存储策略
  union all select '6310000000000000012', '0', 'oa_instance',
    null, null, '0', 0, 5, 0, 0, 10, '0', '扩展名/大小受 infra.storage.* 全局天花板约束', '0', now(3), '0', now(3), 0
) t
where not exists (
  select 1 from infra_file_profile_rule r where r.tenant_id = t.tenant_id and r.profile_code = t.profile_code and r.deleted = 0
);

-- 材料目录：按现有功能场景编排（站点品牌 / 公告 / 轮播 / 头像 / OA）
insert into infra_file_catalog (
  id, tenant_id, catalog_code, catalog_name, catalog_name_i18n, status, remark,
  created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '6320000000000000001' as id, '0' as tenant_id, 'brand_logo' as catalog_code, '站点品牌 Logo' as catalog_name,
    json_object('zh-CN', '站点品牌 Logo', 'en-US', 'Site brand logos') as catalog_name_i18n, '0' as status,
    'sys_brand_pack 管理页 Logo/Favicon/登录背景槽位' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '6320000000000000006', '0', 'brand_qr', '站点品牌二维码',
    json_object('zh-CN', '站点品牌二维码', 'en-US', 'Site brand QR codes'), '0',
    'sys_brand_pack 管理页二维码槽位', '0', now(3), '0', now(3), 0
  union all select '6320000000000000002', '0', 'sys_notice', '通知公告',
    json_object('zh-CN', '通知公告', 'en-US', 'Notice'), '0', '封面 + 富文本内嵌图', '0', now(3), '0', now(3), 0
  union all select '6320000000000000003', '0', 'sys_banner', '轮播图',
    json_object('zh-CN', '轮播图', 'en-US', 'Banner'), '0', 'Banner 图片上传', '0', now(3), '0', now(3), 0
  union all select '6320000000000000004', '0', 'user_account', '用户账号',
    json_object('zh-CN', '用户账号', 'en-US', 'User account'), '0', '用户头像', '0', now(3), '0', now(3), 0
  union all select '6320000000000000005', '0', 'oa_instance', 'OA 实例附件',
    json_object('zh-CN', 'OA 实例附件', 'en-US', 'OA instance files'), '0', 'OaFileUpload 多附件', '0', now(3), '0', now(3), 0
) t
where not exists (
  select 1 from infra_file_catalog c where c.tenant_id = t.tenant_id and c.catalog_code = t.catalog_code and c.deleted = 0
);

insert into infra_file_catalog_item (
  id, tenant_id, catalog_code, profile_code, required, min_num, max_num, sort_no, status, chrome_roles, remark,
  created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '6330000000000000001' as id, '0' as tenant_id, 'brand_logo' as catalog_code, 'brand_logo_sm' as profile_code,
    null as required, null as min_num, null as max_num, 10 as sort_no, '0' as status,
    json_object('sidebar_logo', 10, 'login_logo', 30) as chrome_roles, null as remark,
    '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all select '6330000000000000002', '0', 'brand_logo', 'brand_logo_md', null, null, null, 20, '0',
    json_object('sidebar_logo', 20, 'login_logo', 20), null, '0', now(3), '0', now(3), 0
  union all select '6330000000000000003', '0', 'brand_logo', 'brand_logo_lg', null, null, null, 30, '0',
    json_object('login_logo', 10), null, '0', now(3), '0', now(3), 0
  union all select '6330000000000000004', '0', 'brand_logo', 'brand_favicon', null, null, null, 40, '0',
    json_object('favicon', 10), null, '0', now(3), '0', now(3), 0
  union all select '6330000000000000005', '0', 'brand_logo', 'brand_login_bg', null, null, null, 50, '0',
    json_object('login_bg', 10), null, '0', now(3), '0', now(3), 0
  union all select '6330000000000000006', '0', 'brand_qr', 'brand_qr_wechat', null, null, null, 10, '0',
    json_object('qr_wechat', 10), null, '0', now(3), '0', now(3), 0
  union all select '6330000000000000007', '0', 'brand_qr', 'brand_qr_official', null, null, null, 20, '0',
    json_object('qr_official', 10), null, '0', now(3), '0', now(3), 0
  union all select '6330000000000000008', '0', 'sys_notice', 'notice_cover', null, null, null, 10, '0',
    null, null, '0', now(3), '0', now(3), 0
  union all select '6330000000000000009', '0', 'sys_notice', 'notice_richtext', null, null, null, 20, '0',
    null, null, '0', now(3), '0', now(3), 0
  union all select '6330000000000000010', '0', 'sys_banner', 'banner_image', null, null, null, 10, '0',
    null, null, '0', now(3), '0', now(3), 0
  union all select '6330000000000000011', '0', 'user_account', 'user-avatar', null, null, null, 10, '0',
    null, null, '0', now(3), '0', now(3), 0
  union all select '6330000000000000012', '0', 'oa_instance', 'oa_instance', null, null, 5, 10, '0',
    null, null, '0', now(3), '0', now(3), 0
) t
where not exists (
  select 1 from infra_file_catalog_item i
  where i.tenant_id = t.tenant_id and i.catalog_code = t.catalog_code and i.profile_code = t.profile_code and i.deleted = 0
);

-- 租户业务开发者角色

-- ==========================================
-- 4. 租户业务开发者角色（模板，供后续业务模块参考）
-- ==========================================
insert into sys_role (
  id, tenant_id, role_name, role_code, data_scope, sort_no, status, is_super_role,
  remark, created_by, created_at, updated_by, updated_at, deleted
)
select
  '2000000000000000003',
  '1000000000000000001',
  '业务开发者',
  'tenant_developer',
  '1',
  3,
  '0',
  '0',
  '租户内业务开发：代码生成 + AI 使用，不含系统管理',
  '0', now(3), '0', now(3), 0
where not exists (
  select 1 from sys_role r where r.tenant_id = '1000000000000000001' and r.role_code = 'tenant_developer' and r.deleted = 0
);

-- 工作台 + 代码生成 + AI 使用
insert into sys_role_menu (tenant_id, role_id, menu_id)
select '1000000000000000001', '2000000000000000003', m.id
from sys_menu m
where m.deleted = 0
  and m.id in (
    '4000010001000000000',
    '4000060000000000000',
    '4000060001000000000',
    '4000060001010000000',
    '4000060001020000000',
    '4000060001030000000',
    '4000060001040000000',
    '4000060001050000000',
    '4000060001060000000',
    '4000060001070000000',
    '4000060002000000000',
    '4000060100000000000',
    '4000060199010000000',
    '4000060199030000000',
    '4000060102000000000',
    '4000060103000000000',
    '4000060104000000000',
    '4000060108000000000',
    '4000060199040000000',
    '4000060199050000000',
    '4000060199060000000',
    '4000060105000000000',
    '4000060106000000000',
    '4000060107000000000'
  )
  and not exists (
    select 1 from sys_role_menu rm
    where rm.tenant_id = '1000000000000000001'
      and rm.role_id = '2000000000000000003'
      and rm.menu_id = m.id
  );

-- ==========================================
-- 工作流 / 在线办公 / OA配置 菜单（新库种子；ID 分块连续）
-- 规范：可见 C 给租户侧栏；F 挂在对应 C 下；仅「审批跳转页」允许 hidden C
-- ID：400–424 工作流(PLATFORM)｜500–535 OA配置｜550–592 在线办公
-- ==========================================

-- ---------- 工作流（仅平台） ----------
insert into sys_menu (
  id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path,
  permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope, product_code,
  created_by, created_at, updated_by, updated_at, deleted
) values
('4000080000000000000', '0', 'M', '工作流', json_object('zh-CN', '工作流', 'en-US', 'Workflow'), 'Workflow', '/workflow', null, null, 'Share', 10, '0', '0', '0', '1', '平台引擎配置', 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000080001000000000', '4000080000000000000', 'C', '流程模型', json_object('zh-CN', '流程模型', 'en-US', 'Process Models'), 'WorkflowModel', 'model', '/workflow/model/index', 'workflow:model:view', 'SetUp', 1, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000080001010000000', '4000080001000000000', 'F', '模型查询', json_object('zh-CN', '模型查询', 'en-US', 'List'), null, null, null, 'workflow:model:list', null, 1, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000080001020000000', '4000080001000000000', 'F', '模型详情', json_object('zh-CN', '模型详情', 'en-US', 'View'), null, null, null, 'workflow:model:query', null, 2, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000080001030000000', '4000080001000000000', 'F', '模型部署', json_object('zh-CN', '模型部署', 'en-US', 'Deploy'), null, null, null, 'workflow:model:deploy', null, 3, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000080001040000000', '4000080001000000000', 'F', '模型修改', json_object('zh-CN', '模型修改', 'en-US', 'Edit'), null, null, null, 'workflow:model:update', null, 4, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000080002000000000', '4000080000000000000', 'C', '审批人规则', json_object('zh-CN', '审批人规则', 'en-US', 'Approver Rules'), 'WorkflowApproverRule', 'approver-rule', '/workflow/approver-rule/index', 'workflow:approverRule:view', 'User', 2, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000080002010000000', '4000080002000000000', 'F', '规则查询', json_object('zh-CN', '规则查询', 'en-US', 'List'), null, null, null, 'workflow:approverRule:list', null, 1, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000080002020000000', '4000080002000000000', 'F', '规则详情', json_object('zh-CN', '规则详情', 'en-US', 'View'), null, null, null, 'workflow:approverRule:query', null, 2, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000080002030000000', '4000080002000000000', 'F', '规则维护', json_object('zh-CN', '规则维护', 'en-US', 'Edit'), null, null, null, 'workflow:approverRule:update', null, 3, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000080002040000000', '4000080002000000000', 'F', '规则删除', json_object('zh-CN', '规则删除', 'en-US', 'Delete'), null, null, null, 'workflow:approverRule:delete', null, 4, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000080003000000000', '4000080000000000000', 'C', '绑定策略', json_object('zh-CN', '绑定策略', 'en-US', 'Binding Policies'), 'WorkflowBizBindingPolicy', 'biz-binding-policy', '/workflow/biz-binding-policy/index', 'workflow:bizBindingPolicy:view', 'Connection', 3, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000080003010000000', '4000080003000000000', 'F', '策略查询', json_object('zh-CN', '策略查询', 'en-US', 'List'), null, null, null, 'workflow:bizBindingPolicy:list', null, 1, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000080003020000000', '4000080003000000000', 'F', '策略详情', json_object('zh-CN', '策略详情', 'en-US', 'View'), null, null, null, 'workflow:bizBindingPolicy:query', null, 2, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000080003030000000', '4000080003000000000', 'F', '策略维护', json_object('zh-CN', '策略维护', 'en-US', 'Edit'), null, null, null, 'workflow:bizBindingPolicy:update', null, 3, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0),
('4000080003040000000', '4000080003000000000', 'F', '策略删除', json_object('zh-CN', '策略删除', 'en-US', 'Delete'), null, null, null, 'workflow:bizBindingPolicy:delete', null, 4, '0', '0', '0', '1', null, 'PLATFORM', 'platform', '0', now(3), '0', now(3), 0);

-- ---------- 在线办公（租户可见办理入口；F 挂可见页下） ----------
insert into sys_menu (
  id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path,
  permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope, product_code,
  created_by, created_at, updated_by, updated_at, deleted
) values
('4000100000000000000', '0', 'M', '在线办公', json_object('zh-CN', '在线办公', 'en-US', 'Online Office'), 'OnlineOffice', '/office', null, null, 'Briefcase', 10, '0', '0', '0', '1', '发起、办理与流程运营', 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100001000000000', '4000100000000000000', 'C', '发起申请', json_object('zh-CN', '发起申请', 'en-US', 'New Application'), 'OfficeApply', 'apply', '/office/apply/index', 'oa:office:apply:view', 'Grid', 1, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100001010000000', '4000100001000000000', 'F', '发起目录', json_object('zh-CN', '发起目录', 'en-US', 'List'), null, null, null, 'oa:office:apply:list', null, 1, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100002000000000', '4000100000000000000', 'C', '我的草稿', json_object('zh-CN', '我的草稿', 'en-US', 'My Drafts'), 'OfficeDraft', 'draft', '/office/draft/index', 'oa:office:draft:view', 'Document', 2, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100002010000000', '4000100002000000000', 'F', '草稿查询', json_object('zh-CN', '草稿查询', 'en-US', 'List'), null, null, null, 'oa:office:draft:list', null, 1, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100003000000000', '4000100000000000000', 'C', '已发申请', json_object('zh-CN', '已发申请', 'en-US', 'Submitted Applications'), 'OfficeSubmitted', 'submitted', '/office/submitted/index', 'oa:office:submitted:view', 'CircleCheck', 3, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100003020000000', '4000100003000000000', 'F', '已发页权限', json_object('zh-CN', '已发页权限', 'en-US', 'View'), null, null, null, 'workflow:submitted:view', null, 1, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100003010000000', '4000100003000000000', 'F', '已发查询', json_object('zh-CN', '已发查询', 'en-US', 'List'), null, null, null, 'workflow:submitted:list', null, 2, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100004000000000', '4000100000000000000', 'C', '待我处理', json_object('zh-CN', '待我处理', 'en-US', 'Pending Tasks'), 'OfficeTodo', 'todo', '/office/todo/index', 'oa:office:todo:view', 'Bell', 4, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100004010000000', '4000100004000000000', 'F', '待办页权限', json_object('zh-CN', '待办页权限', 'en-US', 'View'), null, null, null, 'workflow:todo:view', null, 1, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100004020000000', '4000100004000000000', 'F', '待办查询', json_object('zh-CN', '待办查询', 'en-US', 'List'), null, null, null, 'workflow:todo:list', null, 2, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100004030000000', '4000100004000000000', 'F', '任务详情', json_object('zh-CN', '任务详情', 'en-US', 'View'), null, null, null, 'workflow:task:query', null, 3, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100004040000000', '4000100004000000000', 'F', '任务审批', json_object('zh-CN', '任务审批', 'en-US', 'Approve'), null, null, null, 'workflow:task:complete', null, 4, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100004050000000', '4000100004000000000', 'F', '任务转办', json_object('zh-CN', '任务转办', 'en-US', 'Transfer'), null, null, null, 'workflow:task:transfer', null, 5, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100004060000000', '4000100004000000000', 'F', '任务催办', json_object('zh-CN', '任务催办', 'en-US', 'Urge'), null, null, null, 'workflow:task:urge', null, 6, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100004070000000', '4000100004000000000', 'F', '任务加签', json_object('zh-CN', '任务加签', 'en-US', 'Add Sign'), null, null, null, 'workflow:task:addSign', null, 7, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100004080000000', '4000100004000000000', 'F', '任务减签', json_object('zh-CN', '任务减签', 'en-US', 'Remove Sign'), null, null, null, 'workflow:task:removeSign', null, 8, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100004090000000', '4000100004000000000', 'F', '任务委派', json_object('zh-CN', '任务委派', 'en-US', 'Delegate'), null, null, null, 'workflow:task:delegate', null, 9, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100004100000000', '4000100004000000000', 'F', '任务跳转', json_object('zh-CN', '任务跳转', 'en-US', 'Jump'), null, null, null, 'workflow:task:jump', null, 10, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100005000000000', '4000100000000000000', 'C', '我已处理', json_object('zh-CN', '我已处理', 'en-US', 'Completed Tasks'), 'OfficeDone', 'done', '/office/done/index', 'oa:office:done:view', 'Finished', 5, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100005010000000', '4000100005000000000', 'F', '已办页权限', json_object('zh-CN', '已办页权限', 'en-US', 'View'), null, null, null, 'workflow:done:view', null, 1, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100005020000000', '4000100005000000000', 'F', '已办查询', json_object('zh-CN', '已办查询', 'en-US', 'List'), null, null, null, 'workflow:done:list', null, 2, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100006000000000', '4000100000000000000', 'C', '抄送给我的', json_object('zh-CN', '抄送给我的', 'en-US', 'CC''d to Me'), 'OfficeCc', 'cc', '/office/cc/index', 'oa:office:cc:view', 'Promotion', 6, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100006010000000', '4000100006000000000', 'F', '抄送页权限', json_object('zh-CN', '抄送页权限', 'en-US', 'View'), null, null, null, 'workflow:cc:view', null, 1, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100006020000000', '4000100006000000000', 'F', '抄送查询', json_object('zh-CN', '抄送查询', 'en-US', 'List'), null, null, null, 'workflow:cc:list', null, 2, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100010000000000', '4000100000000000000', 'C', '办公助手', json_object('zh-CN', '办公助手', 'en-US', 'Office Assistant'), 'OfficeAi', 'ai', '/office/ai/index', 'oa:office:ai:view', 'ChatDotRound', 7, '0', '0', '0', '1', '用户侧 AI Skills：查待办、查我发起的（数据按登录态隔离）', 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100010020000000', '4000100010000000000', 'F', '助手对话', json_object('zh-CN', '助手对话', 'en-US', 'Chat'), null, null, null, 'oa:office:ai:chat', null, 1, '0', '0', '0', '1', '办公助手对话权限', 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100003030000000', '4000100003000000000', 'F', '流程撤回', json_object('zh-CN', '流程撤回', 'en-US', 'Cancel'), null, null, null, 'workflow:instance:cancel', null, 3, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100007000000000', '4000100000000000000', 'C', '流程监控', json_object('zh-CN', '流程监控', 'en-US', 'Process Monitor'), 'WorkflowMonitor', 'monitor', '/workflow/monitor/index', 'workflow:monitor:view', 'DataAnalysis', 8, '0', '0', '0', '1', '租户管理员排查', 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100007020000000', '4000100007000000000', 'F', '监控查询', json_object('zh-CN', '监控查询', 'en-US', 'List'), null, null, null, 'workflow:monitor:list', null, 1, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100007010000000', '4000100007000000000', 'F', '流程终止', json_object('zh-CN', '流程终止', 'en-US', 'Terminate'), null, null, null, 'workflow:instance:terminate', null, 2, '0', '0', '0', '1', '管理员终止运行中实例', 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100099010000000', '4000100000000000000', 'F', 'OA单据新增', json_object('zh-CN', 'OA单据新增', 'en-US', 'Add'), null, null, null, 'oa:instance:add', null, 11, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100099020000000', '4000100000000000000', 'F', 'OA单据修改', json_object('zh-CN', 'OA单据修改', 'en-US', 'Edit'), null, null, null, 'oa:instance:update', null, 12, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100099030000000', '4000100000000000000', 'F', 'OA单据删除', json_object('zh-CN', 'OA单据删除', 'en-US', 'Delete'), null, null, null, 'oa:instance:delete', null, 13, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100099040000000', '4000100000000000000', 'F', 'OA单据提交', json_object('zh-CN', 'OA单据提交', 'en-US', 'Submit'), null, null, null, 'oa:instance:submit', null, 14, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100099050000000', '4000100000000000000', 'F', 'OA单据详情', json_object('zh-CN', 'OA单据详情', 'en-US', 'View'), null, null, null, 'oa:instance:query', null, 16, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100099060000000', '4000100000000000000', 'F', 'OA单据列表', json_object('zh-CN', 'OA单据列表', 'en-US', 'List'), null, null, null, 'oa:instance:list', null, 17, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100099070000000', '4000100000000000000', 'F', 'OA单据关闭', json_object('zh-CN', 'OA单据关闭', 'en-US', 'Close Document'), null, null, null, 'oa:instance:close', null, 18, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
-- 仅审批跳转真页面可 hidden（待办打开，不进侧栏）
('4000100008000000000', '4000100000000000000', 'C', '公告审批', json_object('zh-CN', '公告审批', 'en-US', 'Notice Approval'), 'NoticeApprove', 'notice-approve', '/system/notice/approve/index', 'workflow:approve:notice:view', 'Document', 98, '1', '0', '0', '0', '待办跳转页，侧栏隐藏', 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100008010000000', '4000100008000000000', 'F', '实例查询', json_object('zh-CN', '实例查询', 'en-US', 'List'), null, null, null, 'workflow:instance:query', null, 1, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000100009000000000', '4000100000000000000', 'C', 'OA审批', json_object('zh-CN', 'OA审批', 'en-US', 'OA Approval'), 'OfficeApprove', 'oa-approve', '/office/approve/index', 'workflow:approve:oa:view', 'Document', 99, '1', '0', '0', '0', '待办跳转页，侧栏隐藏', 'TENANT', 'oa', '0', now(3), '0', now(3), 0);

-- ---------- OA配置 ----------
insert into sys_menu (
  id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path,
  permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope, product_code,
  created_by, created_at, updated_by, updated_at, deleted
) values
('4000090000000000000', '0', 'M', 'OA配置', json_object('zh-CN', 'OA配置', 'en-US', 'OA Settings'), 'OaConfig', '/oa', null, null, 'SetUp', 20, '0', '0', '0', '1', '平台审批模板与租户审批模板', 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000090001000000000', '4000090000000000000', 'C', '平台审批模板', json_object('zh-CN', '平台审批模板', 'en-US', 'Platform Templates'), 'OaPlatformTemplate', 'platform-template', '/oa/platform-template/index', 'oa:platformTemplate:view', 'Document', 1, '0', '0', '0', '1', null, 'PLATFORM', 'oa', '0', now(3), '0', now(3), 0),
('4000090001010000000', '4000090001000000000', 'F', '模板查询', json_object('zh-CN', '模板查询', 'en-US', 'List'), null, null, null, 'oa:platformTemplate:list', null, 1, '0', '0', '0', '1', null, 'PLATFORM', 'oa', '0', now(3), '0', now(3), 0),
('4000090001020000000', '4000090001000000000', 'F', '模板详情', json_object('zh-CN', '模板详情', 'en-US', 'View'), null, null, null, 'oa:platformTemplate:query', null, 2, '0', '0', '0', '1', null, 'PLATFORM', 'oa', '0', now(3), '0', now(3), 0),
('4000090001030000000', '4000090001000000000', 'F', '模板维护', json_object('zh-CN', '模板维护', 'en-US', 'Edit'), null, null, null, 'oa:platformTemplate:update', null, 3, '0', '0', '0', '1', null, 'PLATFORM', 'oa', '0', now(3), '0', now(3), 0),
('4000090001040000000', '4000090001000000000', 'F', '模板发布', json_object('zh-CN', '模板发布', 'en-US', 'Publish Template'), null, null, null, 'oa:platformTemplate:publish', null, 4, '0', '0', '0', '1', null, 'PLATFORM', 'oa', '0', now(3), '0', now(3), 0),
('4000090002000000000', '4000090000000000000', 'C', '审批模板', json_object('zh-CN', '审批模板', 'en-US', 'Approval Templates'), 'OaTenantTemplate', 'tenant-template', '/oa/tenant-template/index', 'oa:tenantTemplate:view', 'Edit', 2, '0', '0', '0', '1', '本租户启用与配置；含向导内模型/审批人权限', 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000090002010000000', '4000090002000000000', 'F', '映射查询', json_object('zh-CN', '映射查询', 'en-US', 'List'), null, null, null, 'oa:tenantTemplate:list', null, 1, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000090002020000000', '4000090002000000000', 'F', '映射详情', json_object('zh-CN', '映射详情', 'en-US', 'View'), null, null, null, 'oa:tenantTemplate:query', null, 2, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000090002030000000', '4000090002000000000', 'F', '启用模板', json_object('zh-CN', '启用模板', 'en-US', 'Enable Template'), null, null, null, 'oa:tenantTemplate:enable', null, 3, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000090002040000000', '4000090002000000000', 'F', '映射维护', json_object('zh-CN', '映射维护', 'en-US', 'Edit'), null, null, null, 'oa:tenantTemplate:update', null, 4, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000090002050000000', '4000090002000000000', 'F', '映射发布', json_object('zh-CN', '映射发布', 'en-US', 'Publish Mapping'), null, null, null, 'oa:tenantTemplate:publish', null, 5, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000090002060000000', '4000090002000000000', 'F', '租户模型查看', json_object('zh-CN', '租户模型查看', 'en-US', 'View'), null, null, null, 'workflow:model:view', null, 6, '0', '0', '0', '1', '向导配置流程模型', 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000090002070000000', '4000090002000000000', 'F', '租户模型查询', json_object('zh-CN', '租户模型查询', 'en-US', 'List'), null, null, null, 'workflow:model:list', null, 7, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000090002080000000', '4000090002000000000', 'F', '租户模型详情', json_object('zh-CN', '租户模型详情', 'en-US', 'View'), null, null, null, 'workflow:model:query', null, 8, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000090002090000000', '4000090002000000000', 'F', '租户模型部署', json_object('zh-CN', '租户模型部署', 'en-US', 'Deploy'), null, null, null, 'workflow:model:deploy', null, 9, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000090002100000000', '4000090002000000000', 'F', '租户模型修改', json_object('zh-CN', '租户模型修改', 'en-US', 'Edit'), null, null, null, 'workflow:model:update', null, 10, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000090002110000000', '4000090002000000000', 'F', '审批规则查看', json_object('zh-CN', '审批规则查看', 'en-US', 'View'), null, null, null, 'workflow:approverRule:view', null, 11, '0', '0', '0', '1', '向导配置审批人', 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000090002120000000', '4000090002000000000', 'F', '审批规则查询', json_object('zh-CN', '审批规则查询', 'en-US', 'List'), null, null, null, 'workflow:approverRule:list', null, 12, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000090002130000000', '4000090002000000000', 'F', '审批规则详情', json_object('zh-CN', '审批规则详情', 'en-US', 'View'), null, null, null, 'workflow:approverRule:query', null, 13, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000090002140000000', '4000090002000000000', 'F', '审批规则维护', json_object('zh-CN', '审批规则维护', 'en-US', 'Edit'), null, null, null, 'workflow:approverRule:update', null, 14, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000090002150000000', '4000090002000000000', 'F', '审批规则删除', json_object('zh-CN', '审批规则删除', 'en-US', 'Delete'), null, null, null, 'workflow:approverRule:delete', null, 15, '0', '0', '0', '1', null, 'TENANT', 'oa', '0', now(3), '0', now(3), 0);

insert into sys_menu (
  id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path,
  permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope,
  created_by, created_at, updated_by, updated_at, deleted
) select '4000020201050000000', '4000020201000000000', 'F', '公告提交审批', json_object('zh-CN', '公告提交审批', 'en-US', 'Submit'), null, null, null, 'system:notice:submit', null, 5, '0', '0', '0', '1', '草稿/驳回后提交 Flowable 审批', 'TENANT', '0', now(3), '0', now(3), 0
where not exists (select 1 from sys_menu where id = '4000020201050000000');

insert into sys_menu (
  id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path,
  permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope,
  created_by, created_at, updated_by, updated_at, deleted
) select '4000020201060000000', '4000020201000000000', 'F', '公告撤回', json_object('zh-CN', '公告撤回', 'en-US', 'Withdraw'), null, null, null, 'system:notice:withdraw', null, 6, '0', '0', '0', '1', '审批中撤回（可选）', 'TENANT', '0', now(3), '0', now(3), 0
where not exists (select 1 from sys_menu where id = '4000020201060000000');

insert into sys_menu (
  id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path,
  permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope,
  created_by, created_at, updated_by, updated_at, deleted
) select '4000020201070000000', '4000020201000000000', 'F', '公告关闭', json_object('zh-CN', '公告关闭', 'en-US', 'Close'), null, null, null, 'system:notice:close', null, 7, '0', '0', '0', '1', '有审批历史后归档关闭（不可删除）；入口在公告列表', 'TENANT', '0', now(3), '0', now(3), 0
where not exists (select 1 from sys_menu where id = '4000020201070000000');

insert into sys_menu (
  id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path,
  permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope,
  created_by, created_at, updated_by, updated_at, deleted
) select '4000020201080000000', '4000020201000000000', 'F', '公告发布下架', json_object('zh-CN', '公告发布下架', 'en-US', 'Publish'), null, null, null, 'system:notice:publish', null, 8, '0', '0', '0', '1', '审批通过后上架/下架（门户可见性）', 'TENANT', '0', now(3), '0', now(3), 0
where not exists (select 1 from sys_menu where id = '4000020201080000000');

-- 平台超管：工作流引擎 + 平台审批模板 + 在线办公（含审批跳转页）
insert into sys_role_menu (tenant_id, role_id, menu_id)
select '0', '2000000000000000001', m.id
from sys_menu m
where m.deleted = 0
  and m.id in (
    '4000080000000000000','4000080001000000000','4000080001010000000','4000080001020000000','4000080001030000000','4000080001040000000',
    '4000080002000000000','4000080002010000000','4000080002020000000','4000080002030000000','4000080002040000000',
    '4000080003000000000','4000080003010000000','4000080003020000000','4000080003030000000','4000080003040000000',
    '4000090000000000000','4000090001000000000','4000090001010000000','4000090001020000000','4000090001030000000','4000090001040000000',
    '4000100000000000000','4000100001000000000','4000100001010000000','4000100002000000000','4000100002010000000',
    '4000100003000000000','4000100003020000000','4000100003010000000','4000100004000000000',
    '4000100004010000000','4000100004020000000','4000100004030000000','4000100004040000000','4000100004050000000',
    '4000100004060000000','4000100004070000000','4000100004080000000','4000100004090000000','4000100004100000000',
    '4000100005000000000','4000100005010000000','4000100005020000000','4000100006000000000','4000100006010000000','4000100006020000000',
    '4000100010000000000','4000100010020000000',
    '4000100003030000000','4000100007000000000','4000100007020000000','4000100007010000000',
    '4000100099010000000','4000100099020000000','4000100099030000000','4000100099040000000',
    '4000100099050000000','4000100099060000000','4000100099070000000',
    '4000100008000000000','4000100008010000000','4000100009000000000',
    '4000020201050000000','4000020201060000000','4000020201070000000','4000020201080000000'
  )
  and not exists (
    select 1 from sys_role_menu rm
    where rm.tenant_id = '0' and rm.role_id = '2000000000000000001' and rm.menu_id = m.id
  );

-- 租户管理员：在线办公全量 + 审批模板（含向导权限）；不含工作流目录
insert into sys_role_menu (tenant_id, role_id, menu_id)
select r.tenant_id, r.id, m.id
from sys_role r
cross join sys_menu m
where r.role_code in ('tenant_admin', 'org_admin')
  and r.deleted = 0
  and r.tenant_id <> '0'
  and m.deleted = 0
  and m.id in (
    '4000100000000000000','4000100001000000000','4000100001010000000','4000100002000000000','4000100002010000000',
    '4000100003000000000','4000100003020000000','4000100003010000000','4000100004000000000',
    '4000100004010000000','4000100004020000000','4000100004030000000','4000100004040000000','4000100004050000000',
    '4000100004060000000','4000100004070000000','4000100004080000000','4000100004090000000','4000100004100000000',
    '4000100005000000000','4000100005010000000','4000100005020000000','4000100006000000000','4000100006010000000','4000100006020000000',
    '4000100010000000000','4000100010020000000',
    '4000100003030000000','4000100007000000000','4000100007020000000','4000100007010000000',
    '4000100099010000000','4000100099020000000','4000100099030000000','4000100099040000000',
    '4000100099050000000','4000100099060000000','4000100099070000000',
    '4000100008000000000','4000100008010000000','4000100009000000000',
    '4000090000000000000','4000090002000000000','4000090002010000000','4000090002020000000','4000090002030000000',
    '4000090002040000000','4000090002050000000','4000090002060000000','4000090002070000000','4000090002080000000',
    '4000090002090000000','4000090002100000000','4000090002110000000','4000090002120000000','4000090002130000000',
    '4000090002140000000','4000090002150000000',
    '4000020201000000000','4000020201010000000','4000020201020000000','4000020201030000000','4000020201040000000',
    '4000020201050000000','4000020201060000000','4000020201070000000','4000020201080000000'
  )
  and not exists (
    select 1 from sys_role_menu rm
    where rm.tenant_id = r.tenant_id and rm.role_id = r.id and rm.menu_id = m.id
  );

-- 演示租户普通员工：在线办公办理（已发申请），不含流程监控 / OA配置
insert into sys_role_menu (tenant_id, role_id, menu_id)
select '1000000000000000001', '2000000000000000005', m.id
from sys_menu m
where m.id in (
  '4000010001000000000',
  '4000100000000000000','4000100001000000000','4000100001010000000','4000100002000000000','4000100002010000000',
  '4000100003000000000','4000100003020000000','4000100003010000000','4000100004000000000',
  '4000100004010000000','4000100004020000000','4000100004030000000','4000100004040000000','4000100004050000000',
  '4000100004060000000','4000100004070000000','4000100004080000000','4000100004090000000','4000100004100000000',
  '4000100005000000000','4000100005010000000','4000100005020000000','4000100006000000000','4000100006010000000','4000100006020000000',
  '4000100010000000000','4000100010020000000',
  '4000100003030000000',
  '4000100099010000000','4000100099020000000','4000100099030000000','4000100099040000000',
  '4000100099050000000','4000100099060000000','4000100099070000000',
  '4000100008000000000','4000100008010000000','4000100009000000000',
  -- OA 附件：list/upload/download（不含文件管理页 0320）
  '4000070003010000000','4000070003020000000','4000070003030000000'
)
and not exists (
  select 1 from sys_role_menu rm
  where rm.tenant_id = '1000000000000000001' and rm.role_id = '2000000000000000005' and rm.menu_id = m.id
);

-- ==========================================
-- 三权角色模板（default + demo_org；is_super_role=0）
-- ==========================================
insert into sys_role (
  id, tenant_id, role_name, role_code, data_scope, sort_no, status, is_super_role,
  remark, created_by, created_at, updated_by, updated_at, deleted
)
select * from (
  select '2000000000000000010' as id, '1000000000000000001' as tenant_id, '系统管理员' as role_name, 'tenant_sys_admin' as role_code, '1' as data_scope, 10 as sort_no, '0' as status, '0' as is_super_role, '三权·系统：组织/业务，不含安全与审计' as remark, '0' as created_by, now(3) as created_at, '0' as updated_by, now(3) as updated_at, 0 as deleted
  union all
  select '2000000000000000011', '1000000000000000001', '安全管理员', 'tenant_sec_admin', '1', 11, '0', '0', '三权·安全：用户/角色/IP/租户策略/在线踢人', '0', now(3), '0', now(3), 0
  union all
  select '2000000000000000012', '1000000000000000001', '审计管理员', 'tenant_audit_admin', '1', 12, '0', '0', '三权·审计：登录/操作/接口日志只读', '0', now(3), '0', now(3), 0
  union all
  select '2000000000000000013', '1000000000000000002', '系统管理员', 'tenant_sys_admin', '1', 10, '0', '0', '三权·系统：组织/业务，不含安全与审计', '0', now(3), '0', now(3), 0
  union all
  select '2000000000000000014', '1000000000000000002', '安全管理员', 'tenant_sec_admin', '1', 11, '0', '0', '三权·安全：用户/角色/IP/租户策略/在线踢人', '0', now(3), '0', now(3), 0
  union all
  select '2000000000000000015', '1000000000000000002', '审计管理员', 'tenant_audit_admin', '1', 12, '0', '0', '三权·审计：登录/操作/接口日志只读', '0', now(3), '0', now(3), 0
) t
where not exists (
  select 1 from sys_role r where r.tenant_id = t.tenant_id and r.role_code = t.role_code and r.deleted = 0
);

-- 系统管理员菜单：工作台 + 系统组织/业务（排除 user/role/tenantPolicy）+ 安全/审计目录外的租户菜单按权限前缀
insert into sys_role_menu (tenant_id, role_id, menu_id)
select r.tenant_id, r.id, m.id
from sys_role r
cross join sys_menu m
where r.role_code = 'tenant_sys_admin' and r.deleted = 0
  and m.deleted = 0 and m.assign_scope = 'TENANT'
  and (
    m.id in ('4000020000000000000', '4000020100000000000', '4000020200000000000')
    or m.permission_code like 'dashboard:%'
    or m.permission_code like 'system:dept:%'
    or m.permission_code like 'system:post:%'
    or m.permission_code like 'system:notice:%'
    or m.permission_code like 'system:banner:%'
    or m.permission_code like 'system:siteMessage:%'
    or m.permission_code like 'system:brandPack:%'
    or m.permission_code like 'oa:%'
    or m.permission_code like 'workflow:submitted:%'
    or m.permission_code like 'workflow:todo:%'
    or m.permission_code like 'workflow:done:%'
    or m.permission_code like 'workflow:cc:%'
    or m.permission_code like 'workflow:task:%'
    or m.permission_code like 'workflow:approve:%'
    or m.permission_code in ('workflow:instance:cancel', 'workflow:instance:query')
    or m.permission_code like 'infra:file:%'
    or m.permission_code like 'infra:fileProfile:%'
    or m.permission_code like 'infra:fileCatalog:%'
    or m.permission_code like 'infra:fileCatalogItem:%'
    or m.permission_code like 'infra:asyncTask:%'
    or m.permission_code like 'tool:formDesigner:%'
  )
  and not exists (
    select 1 from sys_role_menu rm
    where rm.tenant_id = r.tenant_id and rm.role_id = r.id and rm.menu_id = m.id
  );

-- 安全管理员
insert into sys_role_menu (tenant_id, role_id, menu_id)
select r.tenant_id, r.id, m.id
from sys_role r
cross join sys_menu m
where r.role_code = 'tenant_sec_admin' and r.deleted = 0
  and m.deleted = 0 and m.assign_scope = 'TENANT'
  and (
    m.id in ('4000020000000000000', '4000020100000000000', '4000020200000000000', '4000040000000000000', '4000050000000000000')
    or m.permission_code like 'dashboard:%'
    or m.permission_code like 'system:user:%'
    or m.permission_code like 'system:role:%'
    or m.permission_code like 'system:ipAccessRule:%'
    or m.permission_code like 'system:tenantPolicy:%'
    or m.permission_code like 'monitor:online:%'
  )
  and not exists (
    select 1 from sys_role_menu rm
    where rm.tenant_id = r.tenant_id and rm.role_id = r.id and rm.menu_id = m.id
  );

-- 审计管理员（不含 clear）
insert into sys_role_menu (tenant_id, role_id, menu_id)
select r.tenant_id, r.id, m.id
from sys_role r
cross join sys_menu m
where r.role_code = 'tenant_audit_admin' and r.deleted = 0
  and m.deleted = 0 and m.assign_scope = 'TENANT'
  and (
    m.id in ('4000050000000000000')
    or m.permission_code like 'dashboard:%'
    or m.permission_code in (
      'monitor:loginLog:view', 'monitor:loginLog:list', 'monitor:loginLog:export',
      'monitor:operationLog:view', 'monitor:operationLog:list', 'monitor:operationLog:export',
      'monitor:apiLog:view', 'monitor:apiLog:list'
    )
  )
  and not exists (
    select 1 from sys_role_menu rm
    where rm.tenant_id = r.tenant_id and rm.role_id = r.id and rm.menu_id = m.id
  );

-- ==========================================
-- 回填无部门用户至各租户根部门
-- ==========================================
update sys_user u
inner join sys_dept d on d.tenant_id = u.tenant_id and d.parent_id = '0' and d.deleted = 0
set u.dept_id = d.id
where (u.dept_id is null or u.dept_id = '')
  and u.deleted = 0;

-- ==========================================
-- 平台 AI Skill 种子（用户侧办公：查待办 / 查我发起的）
-- ==========================================
insert into sys_ai_skill (
  id, skill_code, tool_name, category, skill_name, name_i18n, description, description_i18n,
  required_permission, tool_description, parameters_schema_json, sort_no, status, remark,
  created_by, created_at, updated_by, updated_at, deleted
) values
(
  '4100000001000000001',
  'list_my_todos',
  'list_my_todos',
  'OFFICE',
  '查待办',
  json_object('zh-CN', '查待办', 'en-US', 'List my todos'),
  '查询当前登录用户待处理的审批任务（与「待我处理」同一数据源与权限）。',
  json_object(
    'zh-CN', '查询当前登录用户待处理的审批任务（与「待我处理」同一数据源与权限）。',
    'en-US', 'List pending approval tasks for the current user (same data/permission as Todo page).'
  ),
  'workflow:todo:list',
  'List the current user''s pending workflow todos. Never accept another user id.',
  '{"type":"object","properties":{"limit":{"type":"integer","description":"Max rows to return, default 10, max 20"}},"additionalProperties":false}',
  1,
  '0',
  '用户侧办公 Skill：待办列表',
  '0', now(3), '0', now(3), 0
),
(
  '4100000001000000002',
  'list_my_submitted',
  'list_my_submitted',
  'OFFICE',
  '查我发起的',
  json_object('zh-CN', '查我发起的', 'en-US', 'List my submitted applications'),
  '查询当前登录用户已发起的申请（与「已发申请」同一数据源与权限）。',
  json_object(
    'zh-CN', '查询当前登录用户已发起的申请（与「已发申请」同一数据源与权限）。',
    'en-US', 'List applications started by the current user (same data/permission as Submitted page).'
  ),
  'workflow:submitted:list',
  'List workflow applications started by the current user. Never accept another user id.',
  '{"type":"object","properties":{"limit":{"type":"integer","description":"Max rows to return, default 10, max 20"}},"additionalProperties":false}',
  2,
  '0',
  '用户侧办公 Skill：已发申请列表',
  '0', now(3), '0', now(3), 0
);

-- ==========================================
-- 产品宇宙 Console Shell（sort_no：业务 1–49，内置能力 50–89，platform 90+）
-- ==========================================
insert into sys_product (
  id, code, name, name_i18n, icon, sort_no, status, remark,
  created_by, created_at, updated_by, updated_at, deleted
) values
('4400000000000000001', 'oa', '办公', json_object('zh-CN', '办公', 'en-US', 'Office'), 'Briefcase', 50, '0', '内置：在线办公/OA/公告站内信管理', '0', now(3), '0', now(3), 0),
('4400000000000000002', 'files', '文件', json_object('zh-CN', '文件', 'en-US', 'Files'), 'FolderOpened', 60, '0', '内置：文件管理与配置中心', '0', now(3), '0', now(3), 0),
('4400000000000000003', 'ai', 'AI', json_object('zh-CN', 'AI', 'en-US', 'AI'), 'MagicStick', 70, '0', '内置：AI Catalog 与试跑', '0', now(3), '0', now(3), 0),
('4400000000000000004', 'platform', '平台', json_object('zh-CN', '平台', 'en-US', 'Platform'), 'Monitor', 90, '0', '内置：组织权限/运维监控/系统管理（靠后）', '0', now(3), '0', now(3), 0);



-- 产品宇宙：概览与文件分组（无产品包装 M；产品由 Tab 表达）
insert into sys_menu (
  id, parent_id, menu_type, menu_name, name_i18n, route_name, route_path, component_path,
  permission_code, icon, sort_no, visible, status, is_external, is_cache, remark, assign_scope, product_code,
  created_by, created_at, updated_by, updated_at, deleted
) values
('4000210001000000000', '0', 'C', '文件概览', json_object('zh-CN', '文件概览', 'en-US', 'Files Overview'), 'FilesOverview', 'overview', '/product/files/overview/index', 'files:overview:view', 'DataBoard', 0, '0', '0', '0', '1', '文件产品首页', 'TENANT', 'files', '0', now(3), '0', now(3), 0),
('4000210002000000000', '0', 'M', '文件管理', json_object('zh-CN', '文件管理', 'en-US', 'File Management'), 'FilesMgmt', 'management', null, null, 'FolderOpened', 10, '0', '0', '0', '1', '文件存储与管理', 'TENANT', 'files', '0', now(3), '0', now(3), 0),
('4000220000000000000', '0', 'C', '办公概览', json_object('zh-CN', '办公概览', 'en-US', 'Office Overview'), 'OaOverview', 'overview', '/product/oa/overview/index', 'oa:overview:view', 'DataBoard', 0, '0', '0', '0', '1', '办公产品首页', 'TENANT', 'oa', '0', now(3), '0', now(3), 0),
('4000230000000000000', '0', 'C', 'AI 概览', json_object('zh-CN', 'AI 概览', 'en-US', 'AI Overview'), 'AiOverview', 'overview', '/product/ai/overview/index', 'ai:overview:view', 'DataBoard', 0, '0', '0', '0', '1', 'AI 产品首页', 'TENANT', 'ai', '0', now(3), '0', now(3), 0);

-- 授权：超管角色补新菜单（租户可授权范围）
insert into sys_role_menu (tenant_id, role_id, menu_id)
select r.tenant_id, r.id, m.id
from sys_role r
cross join sys_menu m
where r.deleted = 0 and r.is_super_role = '1'
  and m.deleted = 0 and m.assign_scope = 'TENANT'
  and m.id in (
    '4000210001000000000','4000210002000000000',
    '4000220000000000000',
    '4000230000000000000',
    '4000010001000000000'
  )
  and not exists (
    select 1 from sys_role_menu rm
    where rm.tenant_id = r.tenant_id and rm.role_id = r.id and rm.menu_id = m.id
  );

-- 平台超管角色（platform 租户）同样补授权
insert into sys_role_menu (tenant_id, role_id, menu_id)
select r.tenant_id, r.id, m.id
from sys_role r
cross join sys_menu m
where r.deleted = 0 and r.tenant_id = '0'
  and m.deleted = 0
  and m.id in (
    '4000210001000000000','4000210002000000000',
    '4000220000000000000',
    '4000230000000000000'
  )
  and not exists (
    select 1 from sys_role_menu rm
    where rm.tenant_id = r.tenant_id and rm.role_id = r.id and rm.menu_id = m.id
  );
