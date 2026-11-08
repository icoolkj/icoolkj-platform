#!/usr/bin/env python3
"""Merge desktop codegen output into platform, preserving Gen layout shells and system routes."""
from __future__ import annotations

import json
import re
import shutil
import subprocess
import sys
from copy import deepcopy
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CODEGEN = Path(r"C:\Users\admin\Desktop\icoolkj_code")

FE = ROOT / "frontend" / "src"
FE_GEN = FE / "i18n" / "gen"
BE_SYSTEM = ROOT / "backend" / "icoolkj-admin-system" / "src" / "main"
BE_INFRA = ROOT / "backend" / "icoolkj-admin-infra" / "src" / "main"
BE_API = ROOT / "backend" / "icoolkj-admin-api" / "src" / "main"
BE_SYSTEM_GEN = BE_SYSTEM / "resources" / "i18n" / "gen"
BE_INFRA_GEN = BE_INFRA / "resources" / "i18n" / "gen"

SKIP_RELATION_RE = re.compile(
    r"(RoleDept|RoleMenu|UserPost|UserRole|UserPreference|MenuLocale)",
    re.I,
)

SKIP_SERVICE_IMPL = {
    "UserRoleServiceImpl.java",
    "RoleMenuServiceImpl.java",
    "RoleDeptServiceImpl.java",
    "UserPreferenceServiceImpl.java",
    "MenuLocaleServiceImpl.java",
    "ConfigServiceImpl.java",
    "ConfigAdminServiceImpl.java",
    "ConfigCacheServiceImpl.java",
    "ConfigQueryServiceImpl.java",
    "I18nLocaleAdminServiceImpl.java",
    "I18nMessageAdminServiceImpl.java",
    "I18nLocaleServiceImpl.java",
    "I18nMessageServiceImpl.java",
    "DictDataServiceImpl.java",
    "DictCacheServiceImpl.java",
    "DictQueryServiceImpl.java",
    "DictServiceImpl.java",
    "UserQueryServiceImpl.java",
    "MenuQueryServiceImpl.java",
    "TenantQueryServiceImpl.java",
    "AuthServiceImpl.java",
    "PermissionServiceImpl.java",
    "LoginSecurityConfigService.java",
    "LoginSecurityPolicyServiceImpl.java",
    "IpRuleServiceImpl.java",
    "IpRuleMatchServiceImpl.java",
    "IpRuleCacheServiceImpl.java",
    "I18nCacheServiceImpl.java",
    "I18nQueryServiceImpl.java",
    "SystemDataPermissionServiceImpl.java",
    "TenantConfigResolver.java",
}

SKIP_JAVA_NAMES = {
    "I18nLocaleConvert.java",
    "I18nMessageConvert.java",
    "I18nLocaleService.java",
    "I18nMessageService.java",
    "LoginLogConvert.java",
    "OperationLogConvert.java",
}

# Codegen File* duplicates platform InfraFile* stack.
SKIP_JAVA_RE = re.compile(
    r"^(File(?:Entity|Mapper|Service(?:Impl)?|Convert|VO|CreateRequest|UpdateRequest|PageQuery)|"
    r"LoginLog(?:Create|Update)Request|OperationLog(?:Create|Update)Request)\.java$"
)

SKIP_FRONTEND_REL = re.compile(
    r"(?:^|[\\/])api[\\/]system[\\/]tenant\.ts$"
    r"|(?:^|[\\/])types[\\/]system[\\/]tenant\.ts$"
    r"|(?:^|[\\/])views[\\/]monitor[\\/]"
    r"|(?:^|[\\/])api[\\/]monitor[\\/]",
)

# Never overwrite: mappers/XML (custom SQL), controllers (routes + extensions),
# hand-written service impls. Re-run restores these from git if compile breaks.
SKIP_MAPPER = True
SKIP_CONTROLLERS = True

# codegen folder -> system business folder
FRONTEND_MODULE_MAP = {
    "thirdpartyCallLog": "thirdPartyCallLog",
}

SYSTEM_MODULES = {
    "post", "notice", "config", "i18nLocale", "i18nMessage", "tenant",
    "loginLog", "operationLog", "apiAccessLog", "menu", "dept", "dict",
    "user", "role", "ipAccessRule",
}

INFRA_MODULES = {
    "asyncTask", "asyncTaskLog", "file", "thirdPartyCallLog", "apiAccessLog",
}


def should_skip_java(path: Path) -> bool:
    name = path.name
    if SKIP_RELATION_RE.search(name):
        return True
    if name in SKIP_JAVA_NAMES:
        return True
    if SKIP_JAVA_RE.match(name):
        return True
    if "controller" in path.parts and SKIP_CONTROLLERS and name.endswith("Controller.java"):
        return True
    if SKIP_MAPPER and ("mapper" in path.parts or name.endswith("Mapper.java") or name.endswith("Mapper.xml")):
        return True
    if name in SKIP_SERVICE_IMPL:
        return True
    if name.startswith("Thirdparty") or name.startswith("Thirdparty"):
        return True
    return False


def copy_backend_java() -> list[str]:
    copied: list[str] = []
    src_root = CODEGEN / "backend"
    for src in src_root.rglob("*.java"):
        if should_skip_java(src):
            continue
        rel = src.relative_to(src_root)
        dst = ROOT / "backend" / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
        copied.append(str(rel))
    return copied


def copy_backend_xml() -> list[str]:
    copied: list[str] = []
    for module in ("icoolkj-admin-system", "icoolkj-admin-infra"):
        src_dir = CODEGEN / "backend" / module / "src" / "main" / "resources" / "mapper"
        if not src_dir.is_dir():
            continue
        dst_dir = ROOT / "backend" / module / "src" / "main" / "resources" / "mapper"
        for src in src_dir.glob("*.xml"):
            if SKIP_RELATION_RE.search(src.name):
                continue
            if src.name.startswith("Thirdparty"):
                continue
            dst_dir.mkdir(parents=True, exist_ok=True)
            shutil.copy2(src, dst_dir / src.name)
            copied.append(f"{module}/mapper/{src.name}")
    return copied


def read_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def write_json(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def merge_page_section(existing: dict | None, generated: dict) -> dict:
    out = deepcopy(generated)
    if existing and isinstance(existing.get("page"), dict):
        for k, v in existing["page"].items():
            if k not in out.get("page", {}):
                out.setdefault("page", {})[k] = v
    return out


def build_dict_view(type_view: dict, item_view: dict, existing: dict | None) -> dict:
    meta = (existing or {}).get("meta") or {
        "businessName": "dict",
        "apiModule": "system",
        "backendModule": "system",
        "permissionPrefix": "system:dict",
        "className": "Dict",
    }
    return {
        "meta": meta,
        "page": {"layout": "master-detail"},
        "masterDetail": {
            "masterSpan": 10,
            "detailSpan": 14,
            "detailForeignKey": "dictTypeId",
            "master": {
                "query": type_view.get("query", {}),
                "table": type_view.get("table", {}),
                "form": type_view.get("form", {}),
            },
            "detail": {
                "table": item_view.get("table", {}),
                "form": item_view.get("form", {}),
            },
        },
    }


def preserve_layout(view: dict, business: str, existing: dict | None) -> dict:
    layout_map = {
        "menu": "tree-crud",
        "dept": "tree-crud",
        "dict": "master-detail",
    }
    if business in layout_map:
        if business == "dict":
            return view
        view = deepcopy(view)
        view.setdefault("page", {})["layout"] = layout_map[business]
    if existing:
        meta = existing.get("meta")
        if meta and business in ("loginLog", "operationLog", "apiAccessLog", "tenant", "dict"):
            view["meta"] = deepcopy(meta)
    return view


def copy_frontend_types_and_api() -> None:
    cg_fe = CODEGEN / "frontend" / "src"
    for sub in ("api", "types"):
        for src in (cg_fe / sub).rglob("*"):
            if not src.is_file():
                continue
            rel = src.relative_to(cg_fe / sub)
            rel_posix = rel.as_posix()
            if rel.name.startswith("dictType") or rel.name.startswith("dictItem"):
                continue
            if SKIP_RELATION_RE.search(str(rel)):
                continue
            if SKIP_FRONTEND_REL.search(rel_posix):
                continue
            if "thirdpartyCallLog" in str(rel).lower() and "thirdPartyCallLog" not in str(rel):
                # map to system camelCase file names
                new_parts = [p.replace("thirdpartyCallLog", "thirdPartyCallLog")
                             .replace("ThirdpartyCallLog", "ThirdPartyCallLog") for p in rel.parts]
                rel = Path(*new_parts)
            dst = FE / sub / rel
            dst.parent.mkdir(parents=True, exist_ok=True)
            if dst.exists() and sub == "api":
                merge_api_file(src, dst)
            else:
                shutil.copy2(src, dst)

    merge_dict_types_and_api(cg_fe)


def merge_api_file(src: Path, dst: Path) -> None:
    """Keep destination BASE path and PERM; copy rest from generated when safe."""
    src_text = src.read_text(encoding="utf-8")
    dst_text = dst.read_text(encoding="utf-8")
    base_match = re.search(r"const BASE = '([^']+)'", dst_text)
    if not base_match:
        shutil.copy2(src, dst)
        return
    base = base_match.group(1)
    src_text = re.sub(r"const BASE = '[^']+'", f"const BASE = '{base}'", src_text, count=1)
    perm_match = re.search(r"export const PERM_\w+ = \{[^}]+\}", dst_text, re.S)
    if perm_match:
        src_text = re.sub(r"export const PERM_\w+ = \{[^}]+\}", perm_match.group(0), src_text, count=1, flags=re.S)
    # preserve clear/refresh helpers from destination
    for fn in re.findall(r"export function (\w+)\(", dst_text):
        if fn.startswith("clear") or fn.startswith("refresh"):
            block = re.search(rf"export function {fn}\([\s\S]*?\n\}}\n", dst_text)
            if block and fn not in src_text:
                src_text = src_text.rstrip() + "\n\n" + block.group(0)
    dst.write_text(src_text, encoding="utf-8")


def merge_dict_types_and_api(cg_fe: Path) -> None:
    type_ts = cg_fe / "types" / "system" / "dictType.ts"
    item_ts = cg_fe / "types" / "system" / "dictItem.ts"
    if not type_ts.exists() or not item_ts.exists():
        return
    combined = (
        type_ts.read_text(encoding="utf-8").replace("dictType", "dict").replace("DictType", "DictType")
        + "\n"
        + item_ts.read_text(encoding="utf-8")
    )
    # rebuild dict.ts types cleanly from both files
    type_text = type_ts.read_text(encoding="utf-8")
    item_text = item_ts.read_text(encoding="utf-8")
    out = type_text + "\n" + item_text
    out = out.replace("dictType.ts", "dict.ts")
    (FE / "types" / "system" / "dict.ts").write_text(out, encoding="utf-8")

    type_api = (cg_fe / "api" / "system" / "dictType.ts").read_text(encoding="utf-8")
    item_api = (cg_fe / "api" / "system" / "dictItem.ts").read_text(encoding="utf-8")
    dict_api_dst = FE / "api" / "system" / "dict.ts"
    existing_perm = ""
    if dict_api_dst.exists():
        m = re.search(r"export const PERM_Dict = \{[^}]+\}", dict_api_dst.read_text(encoding="utf-8"), re.S)
        if m:
            existing_perm = m.group(0)
    merged_api = type_api + "\n\n" + item_api
    merged_api = re.sub(r"export const PERM_DictType = \{[^}]+\}", "", merged_api, flags=re.S)
    merged_api = re.sub(r"/\*\* @deprecated use PERM_DictType \*/\nexport const PERM_DICT_TYPE = PERM_DictType\n", "", merged_api)
    merged_api = re.sub(r"export const PERM_DictItem = \{[^}]+\}", "", merged_api, flags=re.S)
    merged_api = re.sub(r"/\*\* @deprecated use PERM_DictItem \*/\nexport const PERM_DICT_ITEM = PERM_DictItem\n", "", merged_api)
    if existing_perm:
        merged_api = merged_api.rstrip() + "\n\n" + existing_perm + "\n"
    # runtime dict data helpers
    if dict_api_dst.exists() and "getDictData" in dict_api_dst.read_text(encoding="utf-8"):
        tail = dict_api_dst.read_text(encoding="utf-8").split("export const PERM_Dict")[0]
        idx = tail.find("const DATA_BASE")
        if idx >= 0:
            merged_api = merged_api.rstrip() + "\n\n" + tail[idx:]
    dict_api_dst.write_text(merged_api, encoding="utf-8")


def publish_i18n(business: str, view: dict, zh: dict, en: dict) -> None:
    write_json(FE_GEN / business / "view.json", view)
    write_json(FE_GEN / business / "zh-CN.json", zh)
    write_json(FE_GEN / business / "en-US.json", en)
    if business in INFRA_MODULES:
        write_json(BE_INFRA_GEN / business / "view.json", view)
        write_json(BE_INFRA_GEN / business / "zh-CN.json", zh)
        write_json(BE_INFRA_GEN / business / "en-US.json", en)
    elif business in SYSTEM_MODULES or business == "dict":
        write_json(BE_SYSTEM_GEN / business / "view.json", view)
        write_json(BE_SYSTEM_GEN / business / "zh-CN.json", zh)
        write_json(BE_SYSTEM_GEN / business / "en-US.json", en)


def merge_i18n_gen() -> None:
    cg_gen = CODEGEN / "frontend" / "src" / "i18n" / "gen"

    # dict master-detail
    type_view = read_json(cg_gen / "dictType" / "view.json")
    item_view = read_json(cg_gen / "dictItem" / "view.json")
    existing_dict = read_json(FE_GEN / "dict" / "view.json") if (FE_GEN / "dict" / "view.json").exists() else None
    dict_view = build_dict_view(type_view, item_view, existing_dict)
    zh = read_json(cg_gen / "dictType" / "zh-CN.json")
    en = read_json(cg_gen / "dictType" / "en-US.json")
    item_zh = read_json(cg_gen / "dictItem" / "zh-CN.json")
    item_en = read_json(cg_gen / "dictItem" / "en-US.json")
    for loc, base, extra in (("fields", zh, item_zh), ("fields", en, item_en)):
        base.setdefault("fields", {}).update(extra.get("fields", {}))
    if existing_dict and (FE_GEN / "dict" / "zh-CN.json").exists():
        old_zh = read_json(FE_GEN / "dict" / "zh-CN.json")
        old_en = read_json(FE_GEN / "dict" / "en-US.json")
        for k, v in old_zh.get("page", {}).items():
            zh.setdefault("page", {})[k] = v
        for k, v in old_en.get("page", {}).items():
            en.setdefault("page", {})[k] = v
    publish_i18n("dict", dict_view, zh, en)

    for src_dir in sorted(cg_gen.iterdir()):
        if not src_dir.is_dir():
            continue
        codegen_name = src_dir.name
        if codegen_name in ("dictType", "dictItem"):
            continue
        if SKIP_RELATION_RE.search(codegen_name):
            continue
        business = FRONTEND_MODULE_MAP.get(codegen_name, codegen_name)
        view = read_json(src_dir / "view.json")
        existing_path = FE_GEN / business / "view.json"
        existing = read_json(existing_path) if existing_path.exists() else None
        view = preserve_layout(view, business, existing)
        zh = read_json(src_dir / "zh-CN.json")
        en = read_json(src_dir / "en-US.json")
        if existing:
            old_zh_path = FE_GEN / business / "zh-CN.json"
            old_en_path = FE_GEN / business / "en-US.json"
            if old_zh_path.exists():
                old_zh = read_json(old_zh_path)
                old_en = read_json(old_en_path)
                for k, v in old_zh.get("page", {}).items():
                    zh.setdefault("page", {})[k] = v
                for k, v in old_en.get("page", {}).items():
                    en.setdefault("page", {})[k] = v
            if existing.get("meta"):
                view["meta"] = deepcopy(existing["meta"])
        publish_i18n(business, view, zh, en)


def copy_infra_shell_views() -> None:
    """Standard GenCrudPage shells for infra modules not in bootstrap."""
    mapping = {
        "asyncTask": ("infra", "AsyncTask"),
        "asyncTaskLog": ("infra", "AsyncTaskLog"),
        "file": ("infra", "File"),
        "thirdpartyCallLog": ("infra", "ThirdPartyCallLog"),
        "apiAccessLog": ("infra", "ApiAccessLog"),
    }
    for cg_name, (api_mod, class_name) in mapping.items():
        business = FRONTEND_MODULE_MAP.get(cg_name, cg_name)
        src = CODEGEN / "frontend" / "src" / "views" / "infra" / cg_name / "index.vue"
        if cg_name == "thirdpartyCallLog":
            src = CODEGEN / "frontend" / "src" / "views" / "infra" / "thirdpartyCallLog" / "index.vue"
        if not src.exists() and cg_name == "apiAccessLog":
            src = CODEGEN / "frontend" / "src" / "views" / "infra" / "apiAccessLog" / "index.vue"
        dst_dir = FE / "views" / "infra" / business
        dst_dir.mkdir(parents=True, exist_ok=True)
        dst = dst_dir / "index.vue"
        if src.exists():
            text = src.read_text(encoding="utf-8")
            text = text.replace("thirdpartyCallLog", "thirdPartyCallLog")
            text = text.replace("ThirdpartyCallLog", "ThirdPartyCallLog")
            text = text.replace("/system/asyncTask", "/infra/asyncTask")
            text = text.replace("/system/asyncTaskLog", "/infra/asyncTaskLog")
            text = text.replace("/system/file", "/infra/file")
            text = text.replace("/system/thirdpartyCallLog", "/infra/thirdPartyCallLog")
            text = text.replace("/system/apiAccessLog", "/infra/apiAccessLog")
            text = text.replace("business-name=\"thirdpartyCallLog\"", "business-name=\"thirdPartyCallLog\"")
            dst.write_text(text, encoding="utf-8")


def main() -> int:
    if not CODEGEN.is_dir():
        print(f"ERROR: codegen path not found: {CODEGEN}")
        return 1

    java_files = copy_backend_java()
    xml_files = copy_backend_xml()
    print(f"Backend Java copied: {len(java_files)} files")
    print(f"Backend XML copied: {len(xml_files)} files")

    copy_frontend_types_and_api()
    print("Frontend api/types merged")

    merge_i18n_gen()
    print("i18n/gen published (frontend + backend mirror)")

    copy_infra_shell_views()
    print("Infra shell views updated")

    subprocess.run(
        [sys.executable, str(ROOT / "scripts" / "bootstrap_gen_crud_pages.py"), "--shells-only"],
        cwd=ROOT,
        check=True,
    )
    print("Bootstrap shells regenerated")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
