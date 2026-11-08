#!/usr/bin/env python3
"""
Flatten icoolkj-admin-system to platform module layout:

  com.icoolkj.platform.system.{domain,mapper,service,service.impl,dto,vo,convert,support}
  resources/mapper/system/*.xml

Regenerate mapper XML with selectById + selectList + insert + updateById + deleteById.
"""
from __future__ import annotations

import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BACKEND = ROOT / "backend"
SYSTEM_JAVA = BACKEND / "icoolkj-admin-system" / "src" / "main" / "java" / "com" / "icoolkj" / "platform" / "system"
SYSTEM_RES = BACKEND / "icoolkj-admin-system" / "src" / "main" / "resources" / "mapper"
INFRA_JAVA = BACKEND / "icoolkj-admin-infra" / "src" / "main" / "java" / "com" / "icoolkj" / "platform" / "infra"
INFRA_RES = BACKEND / "icoolkj-admin-infra" / "src" / "main" / "resources" / "mapper"
SQL_FILE = BACKEND / "database" / "migration" / "V1__init_icoolkj_platform.sql"

PKG = "com.icoolkj.platform.system"
INFRA_PKG = "com.icoolkj.platform.infra"

LAYERS = ("domain", "mapper", "dto", "vo", "convert", "support")

MAPPER_DEFS = {
    "TenantMapper": {
        "table": "sys_tenant", "entity": "TenantEntity", "has_deleted": True,
        "extra_java": ["    TenantEntity selectByTenantCode(@Param(\"tenantCode\") String tenantCode);"],
        "extra_xml": """
    <select id="selectByTenantCode" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM sys_tenant
        WHERE tenant_code = #{tenantCode} AND deleted = 0
        LIMIT 1
    </select>""",
        "list_filters": ["tenantCode", "tenantName", "status"],
    },
    "UserMapper": {
        "table": "sys_user", "entity": "UserEntity", "has_deleted": True,
        "extra_java": [
            "    UserEntity selectByTenantAndUsername(@Param(\"tenantId\") String tenantId, @Param(\"username\") String username);",
            "    int resetLoginSuccess(@Param(\"id\") String id, @Param(\"lastLoginIp\") String lastLoginIp, @Param(\"lastLoginAt\") java.time.LocalDateTime lastLoginAt);",
            "    int updateLoginFailCount(@Param(\"id\") String id, @Param(\"loginFailCount\") int loginFailCount);",
            "    int updateLockState(@Param(\"id\") String id, @Param(\"loginFailCount\") int loginFailCount, @Param(\"lockedUntil\") java.time.LocalDateTime lockedUntil);",
        ],
        "extra_xml": """
    <select id="selectByTenantAndUsername" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM sys_user
        WHERE tenant_id = #{tenantId} AND username = #{username} AND deleted = 0
        LIMIT 1
    </select>
    <update id="resetLoginSuccess">
        UPDATE sys_user SET login_fail_count = 0, locked_until = NULL,
            last_login_ip = #{lastLoginIp}, last_login_at = #{lastLoginAt}, updated_at = NOW(3)
        WHERE id = #{id} AND deleted = 0
    </update>
    <update id="updateLoginFailCount">
        UPDATE sys_user SET login_fail_count = #{loginFailCount}, updated_at = NOW(3)
        WHERE id = #{id} AND deleted = 0
    </update>
    <update id="updateLockState">
        UPDATE sys_user SET login_fail_count = #{loginFailCount}, locked_until = #{lockedUntil}, updated_at = NOW(3)
        WHERE id = #{id} AND deleted = 0
    </update>""",
        "list_filters": ["tenantId", "username", "nickname", "phone", "status", "deptId"],
    },
    "RoleMapper": {"table": "sys_role", "entity": "RoleEntity", "has_deleted": True,
                   "list_filters": ["tenantId", "roleName", "roleCode", "status"]},
    "UserRoleMapper": {
        "table": "sys_user_role", "entity": "UserRoleEntity", "has_deleted": False, "has_id": False,
        "extra_java": ["    List<String> selectRoleIdsByTenantAndUser(@Param(\"tenantId\") String tenantId, @Param(\"userId\") String userId);"],
        "extra_xml": """
    <select id="selectRoleIdsByTenantAndUser" resultType="java.lang.String">
        SELECT DISTINCT role_id FROM sys_user_role
        WHERE tenant_id = #{tenantId} AND user_id = #{userId}
    </select>""",
        "list_filters": ["tenantId", "userId", "roleId"],
    },
    "MenuMapper": {
        "table": "sys_menu", "entity": "MenuEntity", "has_deleted": True,
        "extra_java": [
            "    List<String> selectPermissionCodesByMenuIds(@Param(\"menuIds\") List<String> menuIds);",
            "    List<String> selectAllActivePermissionCodes();",
        ],
        "extra_xml": """
    <select id="selectPermissionCodesByMenuIds" resultType="java.lang.String">
        SELECT DISTINCT permission_code FROM sys_menu
        WHERE deleted = 0 AND status = '0' AND permission_code IS NOT NULL AND permission_code != ''
          AND id IN <foreach collection="menuIds" item="menuId" open="(" separator="," close=")">#{menuId}</foreach>
        ORDER BY permission_code
    </select>
    <select id="selectAllActivePermissionCodes" resultType="java.lang.String">
        SELECT DISTINCT permission_code FROM sys_menu
        WHERE deleted = 0 AND status = '0' AND permission_code IS NOT NULL AND permission_code != ''
        ORDER BY permission_code
    </select>""",
        "list_filters": ["menuName", "menuType", "status", "parentId"],
    },
    "RoleMenuMapper": {
        "table": "sys_role_menu", "entity": "RoleMenuEntity", "has_deleted": False, "has_id": False,
        "extra_java": ["    List<String> selectMenuIdsByTenantAndRoleIds(@Param(\"tenantId\") String tenantId, @Param(\"roleIds\") List<String> roleIds);"],
        "extra_xml": """
    <select id="selectMenuIdsByTenantAndRoleIds" resultType="java.lang.String">
        SELECT DISTINCT menu_id FROM sys_role_menu
        WHERE tenant_id = #{tenantId}
          AND role_id IN <foreach collection="roleIds" item="roleId" open="(" separator="," close=")">#{roleId}</foreach>
    </select>""",
        "list_filters": ["tenantId", "roleId", "menuId"],
    },
    "DeptMapper": {"table": "sys_dept", "entity": "DeptEntity", "has_deleted": True,
                   "list_filters": ["tenantId", "deptName", "deptCode", "status", "parentId"]},
    "PostMapper": {"table": "sys_post", "entity": "PostEntity", "has_deleted": True,
                   "list_filters": ["tenantId", "postName", "postCode", "status"]},
    "UserPostMapper": {"table": "sys_user_post", "entity": "UserPostEntity", "has_deleted": False, "has_id": False,
                       "list_filters": ["tenantId", "userId", "postId"]},
    "RoleDeptMapper": {"table": "sys_role_dept", "entity": "RoleDeptEntity", "has_deleted": False, "has_id": False,
                       "list_filters": ["tenantId", "roleId", "deptId"]},
    "DictTypeMapper": {"table": "sys_dict_type", "entity": "DictTypeEntity", "has_deleted": True,
                       "list_filters": ["tenantId", "dictName", "dictType", "status"]},
    "DictItemMapper": {"table": "sys_dict_item", "entity": "DictItemEntity", "has_deleted": True,
                        "list_filters": ["tenantId", "dictTypeId", "dictLabel", "dictValue", "status"]},
    "ConfigMapper": {
        "table": "sys_config", "entity": "ConfigEntity", "has_deleted": True,
        "extra_java": ["    ConfigEntity selectByTenantAndKey(@Param(\"tenantId\") String tenantId, @Param(\"configKey\") String configKey);"],
        "extra_xml": """
    <select id="selectByTenantAndKey" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM sys_config
        WHERE tenant_id = #{tenantId} AND config_key = #{configKey} AND deleted = 0
        LIMIT 1
    </select>""",
        "list_filters": ["tenantId", "configName", "configKey", "configType"],
    },
    "NoticeMapper": {"table": "sys_notice", "entity": "NoticeEntity", "has_deleted": True,
                     "list_filters": ["tenantId", "noticeTitle", "noticeType", "status"]},
    "IpAccessRuleMapper": {
        "table": "sys_ip_access_rule", "entity": "IpAccessRuleEntity", "has_deleted": True,
        "extra_java": [
            "    List<IpAccessRuleEntity> selectActiveRules(@Param(\"platformTenantId\") String platformTenantId,",
            "            @Param(\"tenantId\") String tenantId, @Param(\"now\") java.time.LocalDateTime now);",
        ],
        "extra_xml": """
    <select id="selectActiveRules" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM sys_ip_access_rule
        WHERE deleted = 0 AND status = '0'
          AND tenant_id IN (#{platformTenantId}, #{tenantId})
          AND (start_at IS NULL OR start_at &lt;= #{now})
          AND (end_at IS NULL OR end_at &gt;= #{now})
        ORDER BY priority_no DESC
    </select>""",
        "list_filters": ["tenantId", "ruleName", "ruleType", "applyScope", "status"],
    },
    "LoginLogMapper": {
        "table": "sys_login_log", "entity": "LoginLogEntity", "has_deleted": False,
        "list_filters": ["tenantId", "username", "loginStatus"],
        "order_by": "login_at DESC",
    },
    "OperationLogMapper": {
        "table": "sys_operation_log", "entity": "OperationLogEntity", "has_deleted": False,
        "list_filters": ["tenantId", "operatorId", "operationStatus", "moduleName"],
        "order_by": "operation_at DESC",
    },
}

INFRA_MAPPERS = {
    "InfraFileMapper": {"table": "infra_file", "entity": "InfraFileEntity", "has_deleted": False,
                        "list_filters": ["tenantId", "bizType", "bizId"]},
    "AsyncTaskMapper": {"table": "infra_async_task", "entity": "AsyncTaskEntity", "has_deleted": True,
                        "list_filters": ["tenantId", "taskType", "taskStatus"]},
    "AsyncTaskLogMapper": {"table": "infra_async_task_log", "entity": "AsyncTaskLogEntity", "has_deleted": False,
                           "list_filters": ["tenantId", "taskId"], "order_by": "created_at DESC"},
    "ApiAccessLogMapper": {"table": "infra_api_access_log", "entity": "ApiAccessLogEntity", "has_deleted": False,
                           "list_filters": ["tenantId", "userId", "requestUri"], "order_by": "accessed_at DESC"},
    "ThirdPartyCallLogMapper": {"table": "infra_thirdparty_call_log", "entity": "ThirdPartyCallLogEntity", "has_deleted": False,
                                "list_filters": ["tenantId", "providerName", "responseStatus"], "order_by": "called_at DESC"},
}


def snake_to_camel(name: str) -> str:
    p = name.split("_")
    return p[0] + "".join(x.title() for x in p[1:])


def parse_tables(sql: str) -> dict[str, list[str]]:
    out: dict[str, list[str]] = {}
    for m in re.finditer(r"create table if not exists (\w+)\s*\((.*?)\)\s*engine=", sql, re.I | re.S):
        cols = []
        for line in m.group(2).splitlines():
            line = line.strip().rstrip(",")
            if not line or line.lower().startswith(("primary key", "unique key", "key ")):
                continue
            cm = re.match(r"(\w+)\s+", line)
            if cm:
                cols.append(cm.group(1))
        out[m.group(1)] = cols
    return out


def flatten_system_java() -> None:
    modules = [p for p in SYSTEM_JAVA.iterdir() if p.is_dir() and p.name not in LAYERS and p.name != "service"]
    for module in modules:
        # domain, mapper, dto, ...
        for layer in LAYERS:
            src_dir = module / layer
            if not src_dir.exists():
                continue
            dst_dir = SYSTEM_JAVA / layer
            dst_dir.mkdir(parents=True, exist_ok=True)
            for f in src_dir.glob("*.java"):
                target = dst_dir / f.name
                content = f.read_text(encoding="utf-8")
                old_pkg = f"com.icoolkj.platform.system.{module.name}.{layer}"
                new_pkg = f"{PKG}.{layer}"
                content = re.sub(rf"^package {re.escape(old_pkg)};", f"package {new_pkg};", content, count=1, flags=re.M)
                content = re.sub(rf"com\.icoolkj\.platform\.system\.\w+\.domain\.", f"{PKG}.domain.", content)
                content = re.sub(rf"com\.icoolkj\.platform\.system\.\w+\.mapper\.", f"{PKG}.mapper.", content)
                target.write_text(content, encoding="utf-8")
                if f.resolve() != target.resolve():
                    f.unlink()
        # service
        for sub in ("service",):
            svc = module / sub
            if not svc.exists():
                continue
            for f in svc.glob("*.java"):
                dst = SYSTEM_JAVA / "service" / f.name
                SYSTEM_JAVA.joinpath("service").mkdir(parents=True, exist_ok=True)
                content = f.read_text(encoding="utf-8")
                old_pkg = f"com.icoolkj.platform.system.{module.name}.service"
                content = re.sub(rf"^package {re.escape(old_pkg)};", f"package {PKG}.service;", content, 1, flags=re.M)
                dst.write_text(content, encoding="utf-8")
                f.unlink()
            impl = svc / "impl"
            if impl.exists():
                dst_impl = SYSTEM_JAVA / "service" / "impl"
                dst_impl.mkdir(parents=True, exist_ok=True)
                for f in impl.glob("*.java"):
                    dst = dst_impl / f.name
                    content = f.read_text(encoding="utf-8")
                    old_pkg = f"com.icoolkj.platform.system.{module.name}.service.impl"
                    content = re.sub(rf"^package {re.escape(old_pkg)};", f"package {PKG}.service.impl;", content, 1, flags=re.M)
                    dst.write_text(content, encoding="utf-8")
                    f.unlink()


def flatten_infra_java() -> None:
    for folder in ("storage", "async", "log"):
        base = INFRA_JAVA / folder
        if not base.exists():
            continue
        for layer in ("domain", "mapper"):
            src = base / layer
            if not src.exists():
                continue
            dst = INFRA_JAVA / layer
            dst.mkdir(parents=True, exist_ok=True)
            for f in src.glob("*.java"):
                target = dst / f.name
                content = f.read_text(encoding="utf-8")
                old_pkg = f"{INFRA_PKG}.{folder}.{layer}"
                new_pkg = f"{INFRA_PKG}.{layer}"
                content = re.sub(rf"^package {re.escape(old_pkg)};", f"package {new_pkg};", content, 1, flags=re.M)
                target.write_text(content, encoding="utf-8")
                if f.resolve() != target.resolve():
                    f.unlink()


def global_replace_packages() -> None:
    roots = list(BACKEND.rglob("*"))
    patterns = [
        (r"com\.icoolkj\.platform\.system\.\w+\.service\.impl", f"{PKG}.service.impl"),
        (r"com\.icoolkj\.platform\.system\.\w+\.service", f"{PKG}.service"),
        (r"com\.icoolkj\.platform\.system\.\w+\.domain", f"{PKG}.domain"),
        (r"com\.icoolkj\.platform\.system\.\w+\.mapper", f"{PKG}.mapper"),
        (r"com\.icoolkj\.platform\.system\.\w+\.dto", f"{PKG}.dto"),
        (r"com\.icoolkj\.platform\.system\.\w+\.vo", f"{PKG}.vo"),
        (r"com\.icoolkj\.platform\.system\.\w+\.convert", f"{PKG}.convert"),
        (r"com\.icoolkj\.platform\.system\.\w+\.support", f"{PKG}.support"),
        (r"com\.icoolkj\.platform\.infra\.(storage|async|log)\.domain", f"{INFRA_PKG}.domain"),
        (r"com\.icoolkj\.platform\.infra\.(storage|async|log)\.mapper", f"{INFRA_PKG}.mapper"),
    ]
    for path in BACKEND.rglob("*"):
        if not path.is_file() or path.suffix not in (".java", ".xml", ".mdc"):
            continue
        text = path.read_text(encoding="utf-8")
        orig = text
        for pat, repl in patterns:
            text = re.sub(pat, repl, text)
        if text != orig:
            path.write_text(text, encoding="utf-8")


def build_result_map(cols: list[str]) -> str:
    lines = []
    for col in cols:
        prop = snake_to_camel(col)
        tag = "id" if col == "id" else "result"
        lines.append(f'        <{tag} column="{col}" property="{prop}"/>')
    return "\n".join(lines)


def build_list_where(filters: list[str], has_deleted: bool) -> str:
    parts = []
    if has_deleted:
        parts.append("            deleted = 0")
    for f in filters:
        parts.append(f'            <if test="{f} != null and {f} != \'\'">AND {camel_to_snake(f)} = #{{{f}}}</if>')
    return "\n".join(parts)


def camel_to_snake(name: str) -> str:
    return re.sub(r"(?<!^)(?=[A-Z])", "_", name).lower()


def build_mapper_xml(mapper_name: str, meta: dict, cols: list[str], pkg: str) -> str:
    entity = meta["entity"]
    table = meta["table"]
    has_deleted = meta.get("has_deleted", True)
    has_id = meta.get("has_id", True)
    fq_entity = f"{pkg}.domain.{entity}"
    fq_mapper = f"{pkg}.mapper.{mapper_name}"
    col_list = ", ".join(cols)
    extra = meta.get("extra_xml", "")

    blocks = [
        '<?xml version="1.0" encoding="UTF-8"?>',
        '<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">',
        f'<mapper namespace="{fq_mapper}">',
        "",
        f'    <resultMap id="BaseResultMap" type="{fq_entity}">',
        build_result_map(cols),
        "    </resultMap>",
        "",
        "    <sql id=\"Base_Column_List\">",
        f"        {col_list}",
        "    </sql>",
        "",
    ]

    if has_id:
        where_del = " AND deleted = 0" if has_deleted else ""
        blocks += [
            '    <select id="selectById" resultMap="BaseResultMap">',
            "        SELECT <include refid=\"Base_Column_List\"/>",
            f"        FROM {table}",
            f"        WHERE id = #{{id}}{where_del}",
            "    </select>",
            "",
        ]

    filters = meta.get("list_filters", [])
    order = meta.get("order_by", "created_at DESC" if "created_at" in cols else "id DESC")
    blocks += [
        '    <select id="selectList" resultMap="BaseResultMap">',
        "        SELECT <include refid=\"Base_Column_List\"/>",
        f"        FROM {table}",
        "        <where>",
        build_list_where(filters, has_deleted),
        "        </where>",
        f"        ORDER BY {order}",
        "    </select>",
        "",
    ]

    insert_cols = ", ".join(cols)
    insert_vals = ", ".join(f"#{{{snake_to_camel(c)}}}" for c in cols)
    blocks += [
        '    <insert id="insert">',
        f"        INSERT INTO {table} ({insert_cols})",
        f"        VALUES ({insert_vals})",
        "    </insert>",
        "",
    ]

    if has_id:
        update_cols = [c for c in cols if c not in ("id", "created_at", "created_by")]
        set_clause = ",\n            ".join(f"{c} = #{{{snake_to_camel(c)}}}" for c in update_cols)
        where_del = " AND deleted = 0" if has_deleted else ""
        blocks += [
            '    <update id="updateById">',
            f"        UPDATE {table}",
            f"        SET {set_clause}",
            f"        WHERE id = #{{id}}{where_del}",
            "    </update>",
            "",
        ]
        if has_deleted:
            blocks += [
                '    <update id="deleteById">',
                f"        UPDATE {table} SET deleted = 1 WHERE id = #{{id}}",
                "    </update>",
                "",
            ]
        else:
            blocks += [
                '    <delete id="deleteById">',
                f"        DELETE FROM {table} WHERE id = #{{id}}",
                "    </delete>",
                "",
            ]
    else:
        blocks += [
            '    <delete id="delete">',
            f"        DELETE FROM {table}",
            "        <where>",
            build_list_where(filters, False),
            "        </where>",
            "    </delete>",
            "",
        ]

    if extra.strip():
        blocks.append(extra)
        blocks.append("")

    blocks.append("</mapper>")
    blocks.append("")
    return "\n".join(blocks)


def build_mapper_java(mapper_name: str, meta: dict, pkg: str) -> str:
    entity = meta["entity"]
    has_id = meta.get("has_id", True)
    lines = [
        f"package {pkg}.mapper;",
        "",
        f"import {pkg}.domain.{entity};",
        "import org.apache.ibatis.annotations.Mapper;",
        "import org.apache.ibatis.annotations.Param;",
        "",
        "import java.util.List;",
        "",
        "@Mapper",
        f"public interface {mapper_name} {{",
        "",
    ]
    if has_id:
        lines += [
            f"    {entity} selectById(@Param(\"id\") String id);",
            "",
        ]
    lines += [
        f"    List<{entity}> selectList({entity} query);",
        "",
        f"    int insert({entity} entity);",
        "",
    ]
    if has_id:
        lines += [
            f"    int updateById({entity} entity);",
            "",
            "    int deleteById(@Param(\"id\") String id);",
            "",
        ]
    else:
        lines += [
            f"    int delete({entity} entity);",
            "",
        ]
    for line in meta.get("extra_java", []):
        lines.append(line)
    lines += ["}", ""]
    return "\n".join(lines)


def regenerate_mappers() -> None:
    tables = parse_tables(SQL_FILE.read_text(encoding="utf-8"))
    system_xml_dir = SYSTEM_RES / "system"
    system_xml_dir.mkdir(parents=True, exist_ok=True)
    # remove old nested xml dirs
    for d in SYSTEM_RES.iterdir():
        if d.is_dir() and d.name != "system":
            shutil.rmtree(d, ignore_errors=True)

    mapper_dir = SYSTEM_JAVA / "mapper"
    mapper_dir.mkdir(parents=True, exist_ok=True)

    for mapper_name, meta in MAPPER_DEFS.items():
        table = meta["table"]
        cols = tables.get(table, [])
        if not cols:
            continue
        (mapper_dir / f"{mapper_name}.java").write_text(build_mapper_java(mapper_name, meta, PKG), encoding="utf-8")
        (system_xml_dir / f"{mapper_name}.xml").write_text(build_mapper_xml(mapper_name, meta, cols, PKG), encoding="utf-8")

    infra_xml_dir = INFRA_RES / "infra"
    infra_xml_dir.mkdir(parents=True, exist_ok=True)
    infra_mapper_dir = INFRA_JAVA / "mapper"
    infra_mapper_dir.mkdir(parents=True, exist_ok=True)
    for mapper_name, meta in INFRA_MAPPERS.items():
        table = meta["table"]
        cols = tables.get(table, [])
        if not cols:
            continue
        (infra_mapper_dir / f"{mapper_name}.java").write_text(build_mapper_java(mapper_name, meta, INFRA_PKG), encoding="utf-8")
        (infra_xml_dir / f"{mapper_name}.xml").write_text(build_mapper_xml(mapper_name, meta, cols, INFRA_PKG), encoding="utf-8")


def cleanup_empty_dirs() -> None:
    for base in (SYSTEM_JAVA, INFRA_JAVA):
        if not base.exists():
            continue
        for path in sorted(base.rglob("*"), reverse=True):
            if path.is_dir() and not any(path.iterdir()):
                path.rmdir()


def update_cursor_rule() -> None:
    rule = ROOT / ".cursor" / "rules" / "mybatis-module-layout.mdc"
    rule.write_text("""---
description: MyBatis XML + iCoolKJ flat module layout (system/infra)
globs: backend/icoolkj-admin-*/**/*
alwaysApply: false
---

# 扁平模块规范

## Java 包结构（按 Maven 模块划分，不再按表名分子包）

```
com.icoolkj.platform.system/
  domain/           # 全部 Entity
  mapper/           # 全部 Mapper 接口
  service/          # Service 接口
  service/impl/       # Service 实现
  dto/              # Request / PageQuery
  vo/               # 出参 VO
  convert/          # MapStruct
  support/          # TreeBuilder 等

com.icoolkj.platform.infra/
  domain/
  mapper/
  ...
```

## XML 路径

- system: `resources/mapper/system/{XxxMapper}.xml`
- infra:  `resources/mapper/infra/{XxxMapper}.xml`

namespace = `com.icoolkj.platform.{module}.mapper.{XxxMapper}`

## 每个 Mapper 必须包含

| 方法 | 说明 |
|------|------|
| `selectById` | 单条（有 id 主键表） |
| `selectList` | 列表，Entity 作查询条件 |
| `insert` | 新增 |
| `updateById` | 更新 |
| `deleteById` | 逻辑删或物理删 |

禁止 Service 层 `Wrappers` / `BaseMapper`。
""", encoding="utf-8")


def main() -> None:
    print("Flatten system Java...")
    flatten_system_java()
    print("Flatten infra Java...")
    flatten_infra_java()
    print("Global package replace...")
    global_replace_packages()
    print("Regenerate mappers + XML...")
    regenerate_mappers()
    print("Cleanup empty dirs...")
    cleanup_empty_dirs()
    print("Update cursor rule...")
    update_cursor_rule()
    print("Done. Run: python scripts/fix_java_imports_after_layout.py && mvn compile")


if __name__ == "__main__":
    main()
