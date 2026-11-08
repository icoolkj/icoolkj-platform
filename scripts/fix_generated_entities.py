#!/usr/bin/env python3
"""Regenerate domain entities without duplicating BaseEntity/TenantBaseEntity fields."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SQL = ROOT / "backend" / "database" / "migration" / "V1__init_icoolkj_platform.sql"
SYSTEM_JAVA = ROOT / "backend" / "icoolkj-admin-system" / "src" / "main" / "java" / "com" / "icoolkj" / "platform" / "system"

BASE_COLS = {"id", "created_by", "created_at", "updated_by", "updated_at", "deleted"}
TENANT_COLS = {"tenant_id"}

TABLE_REGISTRY = {
    "sys_role": ("role", "RoleEntity"),
    "sys_dept": ("dept", "DeptEntity"),
    "sys_post": ("post", "PostEntity"),
    "sys_dict_type": ("dict", "DictTypeEntity"),
    "sys_dict_item": ("dict", "DictItemEntity"),
    "sys_notice": ("notice", "NoticeEntity"),
    "sys_operation_log": ("operationlog", "OperationLogEntity"),
}


def snake_to_camel(name: str) -> str:
    parts = name.split("_")
    return parts[0] + "".join(p.title() for p in parts[1:])


def java_type(sql_type: str) -> str:
    t = sql_type.lower()
    if t.startswith("varchar") or t.startswith("char") or t == "text":
        return "String"
    if t.startswith("datetime"):
        return "LocalDateTime"
    if t == "bigint":
        return "Long"
    if "int" in t or "tinyint" in t:
        return "Integer"
    return "String"


def parse_tables(sql: str) -> dict[str, list[tuple[str, str]]]:
    out: dict[str, list[tuple[str, str]]] = {}
    for m in re.finditer(r"create table if not exists (\w+)\s*\((.*?)\)\s*engine=", sql, re.I | re.S):
        table = m.group(1)
        cols = []
        for line in m.group(2).splitlines():
            line = line.strip().rstrip(",")
            if not line or line.lower().startswith(("primary key", "unique key", "key ")):
                continue
            cm = re.match(r"(\w+)\s+(\S+)", line)
            if cm:
                cols.append((cm.group(1), cm.group(2)))
        out[table] = cols
    return out


def entity_extends(table: str) -> tuple[str, bool, bool]:
    if table in ("sys_tenant", "sys_menu"):
        return "BaseEntity", False, True
    if table in ("sys_user_role", "sys_role_menu", "sys_user_post", "sys_role_dept"):
        return "", True, False
    if table in ("sys_login_log", "sys_operation_log"):
        return "", True, True
    if table in ("sys_config", "sys_notice", "sys_dict_type", "sys_dict_item", "sys_ip_access_rule"):
        return "BaseEntity", True, True
    return "TenantBaseEntity", True, True


def generate(module: str, entity: str, table: str, cols: list[tuple[str, str]]) -> str:
    extends, has_tenant, has_audit = entity_extends(table)
    skip = set()
    if extends == "BaseEntity":
        skip |= BASE_COLS
    elif extends == "TenantBaseEntity":
        skip |= BASE_COLS | TENANT_COLS
    elif not extends:
        skip.add("tenant_id") if not has_audit else None

    lines = [f"package com.icoolkj.platform.system.{module}.domain;", ""]
    imports = ["import com.baomidou.mybatisplus.annotation.TableField;", "import com.baomidou.mybatisplus.annotation.TableName;"]
    if extends:
        imports.append(f"import com.icoolkj.platform.framework.mybatis.{extends};")
    if not extends and any(c[0] == "id" for c in cols):
        imports.append("import com.baomidou.mybatisplus.annotation.IdType;")
        imports.append("import com.baomidou.mybatisplus.annotation.TableId;")
    if any(java_type(t) == "LocalDateTime" for _, t in cols):
        imports.append("import java.time.LocalDateTime;")
    for imp in sorted(set(imports)):
        lines.append(imp)
    lines += ["", "/**", f" * Maps to `{table}`.", " */", f'@TableName("{table}")']
    if extends:
        lines.append(f"public class {entity} extends {extends} {{")
    else:
        lines.append(f"public class {entity} {{")
    lines.append("")

    for col, sql_t in cols:
        if col in skip:
            continue
        prop = snake_to_camel(col)
        jt = java_type(sql_t)
        if col == "id" and not extends:
            lines.append("    @TableId(type = IdType.ASSIGN_ID)")
        elif col != prop:
            lines.append(f'    @TableField("{col}")')
        lines.append(f"    private {jt} {prop};")
        lines.append("")

    for col, sql_t in cols:
        if col in skip:
            continue
        prop = snake_to_camel(col)
        jt = java_type(sql_t)
        cap = prop[0].upper() + prop[1:]
        lines += [f"    public {jt} get{cap}() {{", f"        return {prop};", "    }", "",
                  f"    public void set{cap}({jt} {prop}) {{", f"        this.{prop} = {prop};", "    }", ""]

    lines.append("}")
    lines.append("")
    return "\n".join(lines)


def main() -> None:
    tables = parse_tables(SQL.read_text(encoding="utf-8"))
    for table, (module, entity) in TABLE_REGISTRY.items():
        path = SYSTEM_JAVA / module / "domain" / f"{entity}.java"
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(generate(module, entity, table, tables[table]), encoding="utf-8")
        print("fixed", path.relative_to(ROOT))


if __name__ == "__main__":
    main()
