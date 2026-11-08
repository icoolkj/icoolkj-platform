#!/usr/bin/env python3
"""Cross-check enum-like columns, view.json dictType, and V1 dict seeds."""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
V1 = ROOT / "backend/database/migration/V1__init_icoolkj_platform.sql"
SCHEMA = ROOT / "backend/database/schema"
GEN_DIRS = [
    ROOT / "frontend/src/i18n/gen",
    ROOT / "backend/icoolkj-admin-system/src/main/resources/i18n/gen",
    ROOT / "backend/icoolkj-admin-infra/src/main/resources/i18n/gen",
]

# table.column -> expected dict_type (from DDL comments + platform convention)
EXPECTED = {
    ("sys_tenant", "status"): "sys_common_status",
    ("sys_user", "status"): "sys_common_status",
    ("sys_user", "gender"): "sys_gender",
    ("sys_user", "user_type"): "sys_user_type",
    ("sys_role", "status"): "sys_common_status",
    ("sys_role", "data_scope"): "sys_data_scope",
    ("sys_menu", "menu_type"): "sys_menu_type",
    ("sys_menu", "visible"): "sys_visible_status",
    ("sys_menu", "status"): "sys_common_status",
    ("sys_dept", "status"): "sys_common_status",
    ("sys_post", "status"): "sys_common_status",
    ("sys_dict_type", "status"): "sys_common_status",
    ("sys_dict_item", "status"): "sys_common_status",
    ("sys_config", "config_type"): "sys_config_type",
    ("sys_notice", "notice_type"): "sys_notice_type",
    ("sys_notice", "status"): "sys_notice_status",
    ("sys_ip_access_rule", "match_type"): "sys_ip_match_type",
    ("sys_ip_access_rule", "rule_type"): "sys_ip_rule_type",
    ("sys_ip_access_rule", "apply_scope"): "sys_ip_apply_scope",
    ("sys_ip_access_rule", "status"): "sys_common_status",
    ("sys_login_log", "login_status"): "sys_login_status",
    ("sys_operation_log", "operation_status"): "sys_operation_status",
    ("sys_i18n_locale", "status"): "sys_common_status",
    ("sys_i18n_locale", "is_default"): "sys_yes_no",
    ("infra_file", "storage_type"): "infra_storage_type",
    ("infra_async_task", "task_status"): "infra_async_task_status",
    ("infra_async_task_log", "log_level"): "infra_log_level",
    ("infra_thirdparty_call_log", "response_status"): "infra_call_status",
}


def parse_dict_types(sql: str) -> set[str]:
    return set(re.findall(r"'([^']+)',\s*'0',\s*'[^']*字典", sql)) | set(
        re.findall(r", '([a-z_]+)',\s*'0',\s*'sys_", sql)
    )


def collect_view_dict_types() -> set[str]:
    found: set[str] = set()
    for base in GEN_DIRS:
        if not base.is_dir():
            continue
        for path in base.rglob("view.json"):
            data = json.loads(path.read_text(encoding="utf-8"))
            text = json.dumps(data)
            found.update(re.findall(r'"dictType"\s*:\s*"([^"]+)"', text))
    return found


def main() -> None:
    v1 = V1.read_text(encoding="utf-8")
    seeded = set(re.findall(r", '([a-z][a-z0-9_]+)', '0', '[^']*'", v1.split("insert into sys_dict_type")[1].split("insert into sys_dict_item")[0]))
    # cleaner parse from known block
    seeded = set(re.findall(r"'([a-z][a-z0-9_]+)', '0', '[^']*'", v1))
    seeded = {x for x in seeded if x.startswith(("sys_", "infra_")) and x.count("_") >= 2}

    # manual authoritative list from V1 section 32
    seeded = {
        "sys_common_status", "sys_visible_status", "sys_gender", "sys_notice_type",
        "sys_login_status", "sys_yes_no", "sys_config_type", "sys_menu_type",
        "sys_data_scope", "sys_user_type", "sys_notice_status", "sys_operation_status",
        "sys_ip_match_type", "sys_ip_rule_type", "sys_ip_apply_scope",
        "infra_async_task_status", "infra_log_level", "infra_storage_type", "infra_call_status",
    }

    view_dicts = collect_view_dict_types()
    missing_seed = sorted(view_dicts - seeded)
    unused_seed = sorted(seeded - view_dicts)

    print("=== view.json dictType 引用 (%d) ===" % len(view_dicts))
    for d in sorted(view_dicts):
        print(f"  {d}")

    print("\n=== 已初始化但未在 view.json 使用的字典 ===")
    for d in unused_seed:
        print(f"  {d}")

    if missing_seed:
        print("\n=== view.json 引用但 V1 未初始化的字典 [缺失] ===")
        for d in missing_seed:
            print(f"  {d}")
    else:
        print("\n=== view.json 引用的字典均在 V1 已初始化 ===")

    print("\n=== 表字段 vs view.json 绑定（预期字典） ===")
    bound_fields: set[tuple[str, str]] = set()
    for base in GEN_DIRS:
        if not base.is_dir():
            continue
        for path in base.rglob("view.json"):
            data = json.loads(path.read_text(encoding="utf-8"))
            text = json.dumps(data)
            for field, dt in re.findall(r'"field"\s*:\s*"([^"]+)"[^}]{0,200}?"dictType"\s*:\s*"([^"]+)"', text):
                bound_fields.add((field, dt))

    for (table, column), expected in sorted(EXPECTED.items()):
        field_names = {column, column.replace("_", "")}  # loose
        matched = any(dt == expected for f, dt in bound_fields if f in field_names or f.replace("Hash", "") in field_names)
        col_short = column
        if column == "user_type":
            field_names.add("userType")
        if column == "is_default":
            field_names.add("isDefault")
        matched = any(
            dt == expected
            for f, dt in bound_fields
            if f in field_names or f.lower() == col_short.replace("_", "").lower()
        )
        status = "OK" if matched else "未绑定 UI"
        if status != "OK" and expected in seeded:
            status = "字典已建 / UI 未绑定"
        print(f"  {table}.{column:18} -> {expected:28} [{status}]")


if __name__ == "__main__":
    main()
