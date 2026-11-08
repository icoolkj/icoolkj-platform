-- =============================================================================
-- 区划与组织模型 — P1 DDL + 字典种子（定稿草案）
-- 状态：已合并入 migration/V1 + V2 + V3 + V4（P1 2026-07-09）
-- 设计文档：docs/architecture/23-region-org.md
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 国标区划表（平台共享，tenant_id 固定 '0'）
-- -----------------------------------------------------------------------------
create table if not exists sys_region (
  id              varchar(20)  not null comment '区划ID',
  tenant_id       varchar(20)  not null default '0' comment '固定0=平台国标库',
  parent_id       varchar(20)  not null default '0' comment '父区划ID',
  ancestors       varchar(1000) not null default '0' comment '祖级区划ID链，逗号分隔',
  region_code     varchar(12)  not null comment '国标行政区划代码 GB/T 2260',
  region_name     varchar(100) not null comment '区划名称',
  region_level    char(1)      not null comment '1省 2市 3区县 4街道 5社区',
  sort_no         int          not null default 0 comment '显示顺序',
  status          char(1)      not null default '0' comment '状态 0正常 1停用',
  remark          varchar(500) null comment '备注',
  created_by      varchar(20)  null comment '创建人',
  created_at      datetime(3)  not null default current_timestamp(3) comment '创建时间',
  updated_by      varchar(20)  null comment '更新人',
  updated_at      datetime(3)  not null default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
  deleted         tinyint(1)   not null default 0 comment '是否删除',
  primary key (id),
  unique key uk_sys_region_code (region_code, deleted),
  key idx_sys_region_parent (parent_id),
  key idx_sys_region_level (region_level),
  key idx_sys_region_ancestors (ancestors(255))
) engine=innodb default charset=utf8mb4 comment='行政区划表（国标，平台维护）';

-- -----------------------------------------------------------------------------
-- 2. 租户服务根区划
-- -----------------------------------------------------------------------------
alter table sys_tenant
  add column root_region_id varchar(20) null comment '租户服务根区划ID，仅可选省/市/区县' after status;

alter table sys_tenant
  add key idx_sys_tenant_root_region (root_region_id);

-- -----------------------------------------------------------------------------
-- 3. 部门扩展：主管部门 / 内设机构
-- -----------------------------------------------------------------------------
alter table sys_dept
  add column region_id varchar(20) null comment '归属区划ID' after ancestors,
  add column dept_type varchar(20) not null default 'INTERNAL' comment 'COMPETENT主管部门 INTERNAL内设机构' after region_id,
  add column admin_level varchar(20) null comment 'PROVINCE/CITY/COUNTY/DISTRICT/STREET/NONE' after dept_type,
  add column competent_id varchar(20) null comment '内设机构所属主管部门ID；主管部门=自身id' after admin_level;

alter table sys_dept
  add key idx_sys_dept_tenant_region_type (tenant_id, region_id, dept_type),
  add key idx_sys_dept_tenant_competent (tenant_id, competent_id);

-- 历史数据迁移：现有部门视为租户根下主管部门（P1 脚本按租户补 region_id）
-- update sys_dept set dept_type='COMPETENT', competent_id=id where parent_id='0' and dept_type='INTERNAL';
-- update sys_dept d join sys_dept p on d.parent_id=p.id set d.competent_id=coalesce(p.competent_id,p.id), d.region_id=p.region_id where d.dept_type='INTERNAL';

-- -----------------------------------------------------------------------------
-- 4. 角色自定义区划（data_scope=8）
-- -----------------------------------------------------------------------------
create table if not exists sys_role_region (
  tenant_id  varchar(20) not null comment '租户ID',
  role_id    varchar(20) not null comment '角色ID',
  region_id  varchar(20) not null comment '区划ID',
  primary key (tenant_id, role_id, region_id),
  key idx_sys_role_region_role (role_id),
  key idx_sys_role_region_region (region_id)
) engine=innodb default charset=utf8mb4 comment='角色区划数据权限表';

-- -----------------------------------------------------------------------------
-- 5. 扩展 data_scope 字段注释
-- -----------------------------------------------------------------------------
alter table sys_role
  modify column data_scope char(1) not null default '1'
  comment '数据范围 1全部 2本部门及以下 3本部门 4本人 5自定义部门 6本区划及下级 7本区划 8自定义区划';

-- =============================================================================
-- 字典类型（合并 V2 时使用新 ID，避免冲突）
-- =============================================================================
-- sys_region_level   区划层级
-- sys_dept_type      部门类型
-- sys_admin_level    部门行政层级
-- sys_data_scope     追加 6/7/8

-- insert into sys_dict_type (id, tenant_id, dict_name, dict_type, ...) values
-- ('5000000000000000022', '0', '区划层级', 'sys_region_level', ...),
-- ('5000000000000000023', '0', '部门类型', 'sys_dept_type', ...),
-- ('5000000000000000024', '0', '部门行政层级', 'sys_admin_level', ...);

-- sys_region_level
-- 1=省 2=市 3=区县 4=街道 5=社区

-- sys_dept_type
-- COMPETENT=主管部门 INTERNAL=内设机构

-- sys_admin_level
-- PROVINCE=省级 CITY=市级 COUNTY=县级 DISTRICT=区县级 STREET=街道 NONE=无

-- sys_data_scope 追加
-- ('5100000000000000240', ..., '本区划及下级', '6', ...),
-- ('5100000000000000241', ..., '本区划', '7', ...),
-- ('5100000000000000242', ..., '自定义区划', '8', ...);

-- =============================================================================
-- 6. 开发样例区划（安徽省 / 合肥市 / 蜀山区）— 正式环境由国标全量导入替换
-- =============================================================================
insert into sys_region (
  id, tenant_id, parent_id, ancestors, region_code, region_name, region_level,
  sort_no, status, created_by, created_at, updated_by, updated_at, deleted
) values
('R3400000000000000001', '0', '0', '0', '340000', '安徽省', '1', 1, '0', '0', now(3), '0', now(3), 0),
('R3401000000000000001', '0', 'R3400000000000000001', '0,R3400000000000000001', '340100', '合肥市', '2', 1, '0', '0', now(3), '0', now(3), 0),
('R3401040000000000001', '0', 'R3401000000000000001', '0,R3400000000000000001,R3401000000000000001', '340104', '蜀山区', '3', 1, '0', '0', now(3), '0', now(3), 0),
('R3401110000000000001', '0', 'R3401000000000000001', '0,R3400000000000000001,R3401000000000000001', '340111', '包河区', '3', 2, '0', '0', now(3), '0', now(3), 0)
on duplicate key update updated_at = now(3);

-- 样例：默认租户绑定合肥市（可按实际调整）
-- update sys_tenant set root_region_id = 'R3401000000000000001' where tenant_code = 'default';
