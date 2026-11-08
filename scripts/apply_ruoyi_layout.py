#!/usr/bin/env python3
"""
Restructure icoolkj-admin-system / icoolkj-admin-infra to platform package layout:
  {module}/domain | mapper | service | service/impl | dto | vo | convert | support

Also scaffolds missing Entity/Mapper/XML for all V1 tables and fixes imports/namespaces.
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

MODULES = [
    "auth", "tenant", "user", "role", "menu", "dept", "post", "dict",
    "config", "notice", "iprule", "loginlog", "operationlog",
]

INFRA_DB_MODULES = {
    "file": ("storage", "InfraFile", "infra_file"),
    "task": ("async", "AsyncTask", "infra_async_task"),
    "tasklog": ("async", "AsyncTaskLog", "infra_async_task_log"),
    "accesslog": ("log", "ApiAccessLog", "infra_api_access_log"),
    "thirdpartylog": ("log", "ThirdPartyCallLog", "infra_thirdparty_call_log"),
}

# table -> (module, entity_name, resource_folder)
TABLE_REGISTRY = {
    "sys_tenant": ("tenant", "TenantEntity", "tenant"),
    "sys_user": ("user", "UserEntity", "user"),
    "sys_role": ("role", "RoleEntity", "role"),
    "sys_user_role": ("user", "UserRoleEntity", "user"),
    "sys_menu": ("menu", "MenuEntity", "menu"),
    "sys_role_menu": ("role", "RoleMenuEntity", "role"),
    "sys_dept": ("dept", "DeptEntity", "dept"),
    "sys_post": ("post", "PostEntity", "post"),
    "sys_user_post": ("user", "UserPostEntity", "user"),
    "sys_role_dept": ("role", "RoleDeptEntity", "role"),
    "sys_dict_type": ("dict", "DictTypeEntity", "dict"),
    "sys_dict_item": ("dict", "DictItemEntity", "dict"),
    "sys_config": ("config", "ConfigEntity", "config"),
    "sys_notice": ("notice", "NoticeEntity", "notice"),
    "sys_ip_access_rule": ("iprule", "IpAccessRuleEntity", "iprule"),
    "sys_login_log": ("loginlog", "LoginLogEntity", "loginlog"),
    "sys_operation_log": ("operationlog", "OperationLogEntity", "operationlog"),
}


def classify_file(filename: str, content: str) -> str | None:
    if filename.endswith("Entity.java"):
        return "domain"
    if filename.endswith("Mapper.java"):
        return "mapper"
    impl_suffixes = (
        "ServiceImpl.java", "QueryServiceImpl.java", "CacheServiceImpl.java",
        "PolicyServiceImpl.java", "MatchServiceImpl.java",
    )
    if any(filename.endswith(s) for s in impl_suffixes):
        return "service/impl"
    if filename in ("LoginSecurityConfigService.java", "TenantConfigResolver.java"):
        return "service/impl" if "class " in content else "service"
    if filename.endswith(("Service.java", "QueryService.java", "CacheService.java",
                          "PolicyService.java", "MatchService.java")):
        return "service"
    if filename.endswith(("Request.java", "PageQuery.java")):
        return "dto"
    if filename.endswith("VO.java") or filename == "RouteVO.java":
        return "vo"
    if filename.endswith("Convert.java"):
        return "convert"
    if filename.endswith("TreeBuilder.java"):
        return "support"
    return None


def restructure_system_module() -> dict[str, str]:
    """Move flat files into subpackages. Returns old_fqcn -> new_fqcn map."""
    mapping: dict[str, str] = {}

    for module in MODULES:
        module_dir = SYSTEM_JAVA / module
        if not module_dir.exists():
            continue

        for java_file in list(module_dir.glob("*.java")):
            content = java_file.read_text(encoding="utf-8")
            sub = classify_file(java_file.name, content)
            if sub is None:
                continue

            old_pkg = f"com.icoolkj.platform.system.{module}"
            new_pkg = f"{old_pkg}.{sub.replace('/', '.')}"
            class_name = java_file.stem
            old_fqcn = f"{old_pkg}.{class_name}"
            new_fqcn = f"{new_pkg}.{class_name}"
            mapping[old_fqcn] = new_fqcn

            target_dir = module_dir / sub
            target_dir.mkdir(parents=True, exist_ok=True)
            target_file = target_dir / java_file.name

            new_content = re.sub(
                rf"^package {re.escape(old_pkg)};",
                f"package {new_pkg};",
                content,
                count=1,
                flags=re.MULTILINE,
            )
            target_file.write_text(new_content, encoding="utf-8")
            if target_file.resolve() != java_file.resolve():
                java_file.unlink()

    return mapping


def restructure_infra_db() -> dict[str, str]:
    mapping: dict[str, str] = {}
    moves = [
        ("storage", "InfraFileEntity.java", "domain"),
        ("storage", "InfraFileMapper.java", "mapper"),
        ("async", "AsyncTaskEntity.java", "domain"),
        ("async", "AsyncTaskMapper.java", "mapper"),
        ("async", "AsyncTaskLogEntity.java", "domain"),
        ("async", "AsyncTaskLogMapper.java", "mapper"),
        ("log", "ApiAccessLogEntity.java", "domain"),
        ("log", "ApiAccessLogMapper.java", "mapper"),
        ("log", "ThirdPartyCallLogEntity.java", "domain"),
        ("log", "ThirdPartyCallLogMapper.java", "mapper"),
    ]
    for folder, filename, sub in moves:
        src = INFRA_JAVA / folder / filename
        if not src.exists():
            continue
        content = src.read_text(encoding="utf-8")
        old_pkg = f"com.icoolkj.platform.infra.{folder}"
        new_pkg = f"{old_pkg}.{sub.replace('/', '.')}"
        class_name = src.stem
        mapping[f"{old_pkg}.{class_name}"] = f"{new_pkg}.{class_name}"

        target_dir = INFRA_JAVA / folder / sub
        target_dir.mkdir(parents=True, exist_ok=True)
        target = target_dir / filename
        new_content = re.sub(
            rf"^package {re.escape(old_pkg)};",
            f"package {new_pkg};",
            content,
            count=1,
            flags=re.MULTILINE,
        )
        target.write_text(new_content, encoding="utf-8")
        if target.resolve() != src.resolve():
            src.unlink()
    return mapping


def update_mapper_xml_namespaces(module: str, mapper_class: str, resource_folder: str) -> None:
    xml_path = SYSTEM_RES / resource_folder / f"{mapper_class}.xml"
    if not xml_path.exists():
        return
    content = xml_path.read_text(encoding="utf-8")
    old_ns = f"com.icoolkj.platform.system.{module}.{mapper_class}"
    new_ns = f"com.icoolkj.platform.system.{module}.mapper.{mapper_class}"
    content = content.replace(old_ns, new_ns)
    # domain entity types
    for entity in re.findall(rf"com\.icoolkj\.platform\.system\.{module}\.(\w+Entity)", content):
        content = content.replace(
            f"com.icoolkj.platform.system.{module}.{entity}",
            f"com.icoolkj.platform.system.{module}.domain.{entity}",
        )
    xml_path.write_text(content, encoding="utf-8")


def apply_import_mapping(mapping: dict[str, str]) -> None:
    roots = [
        BACKEND / "icoolkj-admin-system" / "src",
        BACKEND / "icoolkj-admin-infra" / "src",
        BACKEND / "icoolkj-admin-api" / "src",
        BACKEND / "icoolkj-admin-framework" / "src",
        SYSTEM_RES,
        INFRA_RES,
    ]
    # longest keys first to avoid partial replacements
    for old, new in sorted(mapping.items(), key=lambda x: len(x[0]), reverse=True):
        for root in roots:
            if not root.exists():
                continue
            for path in root.rglob("*"):
                if not path.is_file() or path.suffix not in (".java", ".xml"):
                    continue
                text = path.read_text(encoding="utf-8")
                if old not in text:
                    continue
                path.write_text(text.replace(old, new), encoding="utf-8")


def snake_to_camel(name: str) -> str:
    parts = name.split("_")
    return parts[0] + "".join(p.title() for p in parts[1:])


def parse_create_tables(sql: str) -> dict[str, list[tuple[str, str]]]:
    tables: dict[str, list[tuple[str, str]]] = {}
    for m in re.finditer(
        r"create table if not exists (\w+)\s*\((.*?)\)\s*engine=",
        sql,
        re.IGNORECASE | re.DOTALL,
    ):
        table = m.group(1)
        body = m.group(2)
        cols: list[tuple[str, str]] = []
        for line in body.splitlines():
            line = line.strip().rstrip(",")
            if not line or line.startswith(("primary key", "unique key", "key ")):
                continue
            cm = re.match(r"(\w+)\s+(\w+(?:\(\d+(?:,\d+)?\))?)", line)
            if cm:
                cols.append((cm.group(1), cm.group(2)))
        tables[table] = cols
    return tables


def java_type(sql_type: str) -> str:
    t = sql_type.lower()
    if t.startswith("varchar") or t.startswith("char") or t == "text":
        return "String"
    if t.startswith("datetime"):
        return "LocalDateTime"
    if t in ("int", "tinyint(1)"):
        return "Integer"
    if t == "bigint":
        return "Long"
    if "tinyint" in t:
        return "Integer"
    return "String"


def entity_extends(table: str) -> tuple[str, str]:
    if table in ("sys_tenant", "sys_menu"):
        return "BaseEntity", "com.icoolkj.platform.framework.mybatis.BaseEntity"
    if table in ("sys_user_role", "sys_role_menu", "sys_user_post", "sys_role_dept"):
        return "", ""
    if table in ("sys_login_log", "sys_operation_log", "infra_async_task_log",
                 "infra_api_access_log", "infra_thirdparty_call_log"):
        return "", ""
    if table.startswith("infra_file"):
        return "", ""
    if table in ("sys_config", "sys_notice", "sys_dict_type", "sys_dict_item", "sys_ip_access_rule"):
        return "BaseEntity", "com.icoolkj.platform.framework.mybatis.BaseEntity"
    return "TenantBaseEntity", "com.icoolkj.platform.framework.mybatis.TenantBaseEntity"


def generate_entity(module: str, entity_name: str, table: str, cols: list[tuple[str, str]]) -> str:
    extends, extends_import = entity_extends(table)
    lines = [f"package com.icoolkj.platform.system.{module}.domain;", "",]
    imports = set()
    if extends_import:
        imports.add(f"import {extends_import};")
    imports.add("import com.baomidou.mybatisplus.annotation.TableField;")
    imports.add("import com.baomidou.mybatisplus.annotation.TableName;")
    if table in ("sys_login_log", "sys_operation_log", "infra_async_task_log",
                 "infra_api_access_log", "infra_thirdparty_call_log"):
        imports.add("import com.baomidou.mybatisplus.annotation.IdType;")
        imports.add("import com.baomidou.mybatisplus.annotation.TableId;")
    if any(java_type(t) == "LocalDateTime" for _, t in cols):
        imports.add("import java.time.LocalDateTime;")
    for imp in sorted(imports):
        lines.append(imp)
    lines.append("")
    lines.append("/**")
    lines.append(f" * Maps to `{table}`.")
    lines.append(" */")
    lines.append(f'@TableName("{table}")')
    if extends:
        lines.append(f"public class {entity_name} extends {extends} {{")
    else:
        lines.append(f"public class {entity_name} {{")
    lines.append("")

    pk_handled = extends == "BaseEntity" or extends == "TenantBaseEntity"
    for col, sql_t in cols:
        prop = snake_to_camel(col)
        jt = java_type(sql_t)
        if not pk_handled and col == "id":
            lines.append("    @TableId(type = IdType.ASSIGN_ID)")
            pk_handled = True
        elif col != "id" or not extends:
            if snake_to_camel(col) != prop or col != prop:
                lines.append(f'    @TableField("{col}")')
        lines.append(f"    private {jt} {prop};")
        lines.append("")

    for col, _ in cols:
        prop = snake_to_camel(col)
        jt = java_type(cols[[c[0] for c in cols].index(col)][1])
        cap = prop[0].upper() + prop[1:]
        lines.append(f"    public {jt} get{cap}() {{")
        lines.append(f"        return {prop};")
        lines.append("    }")
        lines.append("")
        lines.append(f"    public void set{cap}({jt} {prop}) {{")
        lines.append(f"        this.{prop} = {prop};")
        lines.append("    }")
        lines.append("")

    lines.append("}")
    lines.append("")
    return "\n".join(lines)


def generate_mapper_java(module: str, mapper_name: str, entity_name: str, table: str) -> str:
    fq_entity = f"com.icoolkj.platform.system.{module}.domain.{entity_name}"
    lines = [
        f"package com.icoolkj.platform.system.{module}.mapper;",
        "",
        f"import {fq_entity};",
        "import org.apache.ibatis.annotations.Mapper;",
        "import org.apache.ibatis.annotations.Param;",
        "",
        "@Mapper",
        f"public interface {mapper_name} {{",
        "",
        f"    {entity_name} selectById(@Param(\"id\") String id);",
        "",
        f"    int insert({entity_name} entity);",
        "",
        f"    int updateById({entity_name} entity);",
        "",
        f"    int deleteById(@Param(\"id\") String id);",
        "",
        "}",
        "",
    ]
    return "\n".join(lines)


def generate_mapper_xml(module: str, mapper_name: str, entity_name: str, table: str,
                        cols: list[tuple[str, str]], extra: str = "") -> str:
    fq_mapper = f"com.icoolkj.platform.system.{module}.mapper.{mapper_name}"
    fq_entity = f"com.icoolkj.platform.system.{module}.domain.{entity_name}"
    col_list = ", ".join(c[0] for c in cols)
    has_deleted = any(c[0] == "deleted" for c in cols)

    result_lines = []
    for col, _ in cols:
        prop = snake_to_camel(col)
        if col == "id":
            result_lines.append(f'        <id column="{col}" property="{prop}"/>')
        else:
            result_lines.append(f'        <result column="{col}" property="{prop}"/>')

    insert_cols = ", ".join(c[0] for c in cols)
    insert_vals = ", ".join(f"#{{{snake_to_camel(c[0])}}}" for c in cols)
    update_sets = ",\n            ".join(
        f"{c[0]} = #{{{snake_to_camel(c[0])}}}"
        for c in cols if c[0] not in ("id", "created_at", "created_by")
    )
    where_deleted = " AND deleted = 0" if has_deleted else ""
    logic_delete = f"UPDATE {table} SET deleted = 1 WHERE id = #{{id}}" if has_deleted else f"DELETE FROM {table} WHERE id = #{{id}}"

    return f"""<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="{fq_mapper}">

    <resultMap id="BaseResultMap" type="{fq_entity}">
{chr(10).join(result_lines)}
    </resultMap>

    <sql id="Base_Column_List">
        {col_list}
    </sql>
{extra}
    <select id="selectById" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM {table}
        WHERE id = #{{id}}{where_deleted}
    </select>

    <insert id="insert">
        INSERT INTO {table} ({insert_cols})
        VALUES ({insert_vals})
    </insert>

    <update id="updateById">
        UPDATE {table}
        SET {update_sets}
        WHERE id = #{{id}}{where_deleted}
    </update>

    <update id="deleteById">
        {logic_delete}
    </update>

</mapper>
"""


def scaffold_missing(sql_path: Path) -> None:
    sql = sql_path.read_text(encoding="utf-8")
    tables = parse_create_tables(sql)

    extras = {
        "UserMapper": """
    <select id="selectByTenantAndUsername" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM sys_user
        WHERE tenant_id = #{tenantId}
          AND username = #{username}
          AND deleted = 0
        LIMIT 1
    </select>

    <update id="resetLoginSuccess">
        UPDATE sys_user
        SET login_fail_count = 0,
            locked_until = NULL,
            last_login_ip = #{lastLoginIp},
            last_login_at = #{lastLoginAt},
            updated_at = NOW(3)
        WHERE id = #{id}
          AND deleted = 0
    </update>

    <update id="updateLoginFailCount">
        UPDATE sys_user
        SET login_fail_count = #{loginFailCount},
            updated_at = NOW(3)
        WHERE id = #{id}
          AND deleted = 0
    </update>

    <update id="updateLockState">
        UPDATE sys_user
        SET login_fail_count = #{loginFailCount},
            locked_until = #{lockedUntil},
            updated_at = NOW(3)
        WHERE id = #{id}
          AND deleted = 0
    </update>
""",
        "TenantMapper": """
    <select id="selectByTenantCode" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM sys_tenant
        WHERE tenant_code = #{tenantCode}
          AND deleted = 0
        LIMIT 1
    </select>
""",
        "ConfigMapper": """
    <select id="selectByTenantAndKey" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM sys_config
        WHERE tenant_id = #{tenantId}
          AND config_key = #{configKey}
          AND deleted = 0
        LIMIT 1
    </select>
""",
        "MenuMapper": """
    <select id="selectPermissionCodesByMenuIds" resultType="java.lang.String">
        SELECT DISTINCT permission_code
        FROM sys_menu
        WHERE deleted = 0
          AND status = '0'
          AND permission_code IS NOT NULL
          AND permission_code != ''
          AND id IN
        <foreach collection="menuIds" item="menuId" open="(" separator="," close=")">
            #{menuId}
        </foreach>
        ORDER BY permission_code
    </select>

    <select id="selectAllActivePermissionCodes" resultType="java.lang.String">
        SELECT DISTINCT permission_code
        FROM sys_menu
        WHERE deleted = 0
          AND status = '0'
          AND permission_code IS NOT NULL
          AND permission_code != ''
        ORDER BY permission_code
    </select>
""",
        "UserRoleMapper": """
    <select id="selectRoleIdsByTenantAndUser" resultType="java.lang.String">
        SELECT DISTINCT role_id
        FROM sys_user_role
        WHERE tenant_id = #{tenantId}
          AND user_id = #{userId}
    </select>
""",
        "RoleMenuMapper": """
    <select id="selectMenuIdsByTenantAndRoleIds" resultType="java.lang.String">
        SELECT DISTINCT menu_id
        FROM sys_role_menu
        WHERE tenant_id = #{tenantId}
          AND role_id IN
        <foreach collection="roleIds" item="roleId" open="(" separator="," close=")">
            #{roleId}
        </foreach>
    </select>
""",
        "IpAccessRuleMapper": """
    <select id="selectActiveRules" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM sys_ip_access_rule
        WHERE deleted = 0
          AND status = '0'
          AND tenant_id IN (#{platformTenantId}, #{tenantId})
          AND (start_at IS NULL OR start_at &lt;= #{now})
          AND (end_at IS NULL OR end_at &gt;= #{now})
        ORDER BY priority_no DESC
    </select>
""",
        "LoginLogMapper": """
    <insert id="insert">
        INSERT INTO sys_login_log (
            id, tenant_id, username, login_type, login_status, message, ip, location, user_agent,
            browser, os, login_at
        ) VALUES (
            #{id}, #{tenantId}, #{username}, #{loginType}, #{loginStatus}, #{message}, #{ip},
            #{location}, #{userAgent}, #{browser}, #{os}, #{loginAt}
        )
    </insert>
""",
    }

    custom_mappers = {
        "UserMapper": ["selectByTenantAndUsername", "resetLoginSuccess", "updateLoginFailCount", "updateLockState"],
        "TenantMapper": ["selectByTenantCode"],
        "ConfigMapper": ["selectByTenantAndKey"],
        "MenuMapper": ["selectPermissionCodesByMenuIds", "selectAllActivePermissionCodes"],
        "UserRoleMapper": ["selectRoleIdsByTenantAndUser"],
        "RoleMenuMapper": ["selectMenuIdsByTenantAndRoleIds"],
        "IpAccessRuleMapper": ["selectActiveRules"],
        "LoginLogMapper": ["insert"],
    }

    for table, (module, entity_name, folder) in TABLE_REGISTRY.items():
        if table not in tables:
            continue
        cols = tables[table]
        domain_dir = SYSTEM_JAVA / module / "domain"
        mapper_dir = SYSTEM_JAVA / module / "mapper"
        domain_dir.mkdir(parents=True, exist_ok=True)
        mapper_dir.mkdir(parents=True, exist_ok=True)

        entity_path = domain_dir / f"{entity_name}.java"
        entity_content = entity_path.read_text(encoding="utf-8") if entity_path.exists() else ""
        if not entity_path.exists() or "@TableName" not in entity_content:
            entity_path.write_text(generate_entity(module, entity_name, table, cols), encoding="utf-8")

        mapper_name = entity_name.replace("Entity", "Mapper")
        mapper_path = mapper_dir / f"{mapper_name}.java"
        extra_methods = extras.get(mapper_name, "")
        skip_crud = mapper_name in custom_mappers

        if not mapper_path.exists() or len(mapper_path.read_text(encoding="utf-8")) < 80:
            if skip_crud:
                # write custom interface
                custom_iface = [f"package com.icoolkj.platform.system.{module}.mapper;", "",
                                f"import com.icoolkj.platform.system.{module}.domain.{entity_name};",
                                "import org.apache.ibatis.annotations.Mapper;",
                                "import org.apache.ibatis.annotations.Param;", "", "@Mapper",
                                f"public interface {mapper_name} {{", ""]
                if mapper_name == "UserMapper":
                    custom_iface += [
                        "    UserEntity selectByTenantAndUsername(@Param(\"tenantId\") String tenantId, @Param(\"username\") String username);",
                        "    int resetLoginSuccess(@Param(\"id\") String id, @Param(\"lastLoginIp\") String lastLoginIp, @Param(\"lastLoginAt\") java.time.LocalDateTime lastLoginAt);",
                        "    int updateLoginFailCount(@Param(\"id\") String id, @Param(\"loginFailCount\") int loginFailCount);",
                        "    int updateLockState(@Param(\"id\") String id, @Param(\"loginFailCount\") int loginFailCount, @Param(\"lockedUntil\") java.time.LocalDateTime lockedUntil);",
                    ]
                elif mapper_name == "TenantMapper":
                    custom_iface.append("    TenantEntity selectByTenantCode(@Param(\"tenantCode\") String tenantCode);")
                elif mapper_name == "ConfigMapper":
                    custom_iface.append("    ConfigEntity selectByTenantAndKey(@Param(\"tenantId\") String tenantId, @Param(\"configKey\") String configKey);")
                elif mapper_name == "MenuMapper":
                    custom_iface += [
                        "    java.util.List<String> selectPermissionCodesByMenuIds(@Param(\"menuIds\") java.util.List<String> menuIds);",
                        "    java.util.List<String> selectAllActivePermissionCodes();",
                    ]
                elif mapper_name == "UserRoleMapper":
                    custom_iface.append("    java.util.List<String> selectRoleIdsByTenantAndUser(@Param(\"tenantId\") String tenantId, @Param(\"userId\") String userId);")
                elif mapper_name == "RoleMenuMapper":
                    custom_iface.append("    java.util.List<String> selectMenuIdsByTenantAndRoleIds(@Param(\"tenantId\") String tenantId, @Param(\"roleIds\") java.util.List<String> roleIds);")
                elif mapper_name == "IpAccessRuleMapper":
                    custom_iface.append("    java.util.List<IpAccessRuleEntity> selectActiveRules(@Param(\"platformTenantId\") String platformTenantId, @Param(\"tenantId\") String tenantId, @Param(\"now\") java.time.LocalDateTime now);")
                elif mapper_name == "LoginLogMapper":
                    custom_iface.append(f"    int insert({entity_name} entity);")
                custom_iface += ["", "}", ""]
                mapper_path.write_text("\n".join(custom_iface), encoding="utf-8")
            else:
                mapper_path.write_text(generate_mapper_java(module, mapper_name, entity_name, table), encoding="utf-8")

        xml_dir = SYSTEM_RES / folder
        xml_dir.mkdir(parents=True, exist_ok=True)
        xml_path = xml_dir / f"{mapper_name}.xml"
        if skip_crud and mapper_name in extras:
            xml_path.write_text(
                generate_mapper_xml(module, mapper_name, entity_name, table, cols, extras[mapper_name]).replace(
                    '<select id="selectById"', '<!-- custom queries only -->\n    <select id="selectById"'
                ) if False else build_custom_xml(module, mapper_name, entity_name, table, cols, extras[mapper_name]),
                encoding="utf-8",
            )
        elif not xml_path.exists():
            xml_path.write_text(generate_mapper_xml(module, mapper_name, entity_name, table, cols), encoding="utf-8")


def build_custom_xml(module, mapper_name, entity_name, table, cols, extra: str) -> str:
    fq_mapper = f"com.icoolkj.platform.system.{module}.mapper.{mapper_name}"
    fq_entity = f"com.icoolkj.platform.system.{module}.domain.{entity_name}"
    col_list = ", ".join(c[0] for c in cols)
    result_lines = []
    for col, _ in cols:
        prop = snake_to_camel(col)
        tag = "id" if col == "id" else "result"
        result_lines.append(f'        <{tag} column="{col}" property="{prop}"/>')
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="{fq_mapper}">

    <resultMap id="BaseResultMap" type="{fq_entity}">
{chr(10).join(result_lines)}
    </resultMap>

    <sql id="Base_Column_List">
        {col_list}
    </sql>
{extra}
</mapper>
"""


def update_application_mapper_scan() -> None:
    app = BACKEND / "icoolkj-admin-api" / "src" / "main" / "java" / "com" / "icoolkj" / "platform" / "api" / "ICoolKJAdminApiApplication.java"
    text = app.read_text(encoding="utf-8")
    text = re.sub(
        r'@MapperScan\("[^"]+"\)',
        '@MapperScan({"com.icoolkj.platform.system", "com.icoolkj.platform.infra", "com.icoolkj.platform.biz"})',
        text,
    )
    app.write_text(text, encoding="utf-8")


def main() -> None:
    print("Restructuring system module packages...")
    sys_map = restructure_system_module()
    print(f"  moved {len(sys_map)} system classes")

    print("Restructuring infra DB packages...")
    infra_map = restructure_infra_db()
    print(f"  moved {len(infra_map)} infra classes")

    all_map = {**sys_map, **infra_map}

    print("Updating mapper XML namespaces...")
    for table, (module, entity_name, folder) in TABLE_REGISTRY.items():
        mapper_name = entity_name.replace("Entity", "Mapper")
        update_mapper_xml_namespaces(module, mapper_name, folder)

    print("Scaffolding entities/mappers/xml from V1 SQL...")
    scaffold_missing(BACKEND / "database" / "migration" / "V1__init_icoolkj_platform.sql")

    print("Applying import/package mapping...")
    apply_import_mapping(all_map)

    print("Updating @MapperScan...")
    update_application_mapper_scan()
    print("Done.")


if __name__ == "__main__":
    main()
