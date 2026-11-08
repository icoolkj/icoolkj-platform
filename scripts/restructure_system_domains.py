#!/usr/bin/env python3
"""Restructure icoolkj-admin-system from flat layer packages to domain-first packages.

Before:  com.icoolkj.platform.system.{domain|mapper|service|service.impl|dto|vo|convert|support|...}
After:   com.icoolkj.platform.system.{domain}/{domain|mapper|service|service/impl|dto|vo|convert|support|...}

The ``ai`` / ``datascope`` domains already follow the target style and stay in
place; classes that belong to them (e.g. AiSkill*, SystemDataPermission*)
are moved into those domains.

Also rewrites:
  - package declarations in every moved file
  - imports across the whole backend (old FQCN -> new FQCN)
  - Mapper XML namespaces / resultType / resultMap references
  - moves mapper XMLs into resources/mapper/system/{domain}/
  - moves icoolkj-admin-system tests to the matching domain package

Usage:
  python scripts/restructure_system_domains.py            # dry-run: print plan
  python scripts/restructure_system_domains.py --apply    # apply changes
"""
from __future__ import annotations

import argparse
import re
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SYSTEM_MAIN = (
    ROOT
    / "backend"
    / "icoolkj-admin-system"
    / "src"
    / "main"
    / "java"
    / "com"
    / "icoolkj"
    / "platform"
    / "system"
)
SYSTEM_TEST = (
    ROOT
    / "backend"
    / "icoolkj-admin-system"
    / "src"
    / "test"
    / "java"
    / "com"
    / "icoolkj"
    / "platform"
    / "system"
)
SYSTEM_MAPPER_RES = (
    ROOT / "backend" / "icoolkj-admin-system" / "src" / "main" / "resources" / "mapper" / "system"
)
BACKEND = ROOT / "backend"

# Ordered prefix -> domain. First match wins.
PREFIX_DOMAIN = [
    ("DatabaseBackupConfig", "backup"),
    ("YamlDatabaseBackup", "backup"),
    ("SystemBackupConfiguration", "backup"),
    ("SystemExcelImportLookup", "user"),
    ("SystemAsyncTaskPrincipalResolver", "auth"),
    ("SystemDataPermissionServiceImpl", "datascope"),
    ("SysConfigWorkflowNotifyTemplateSource", "config"),
    ("SysDictLabelResolver", "dict"),
    ("SysJob", "job"),
    ("Tenant", "tenant"),
    ("User", "user"),
    ("Role", "role"),
    ("Menu", "menu"),
    ("Dept", "dept"),
    ("Post", "post"),
    ("Dict", "dict"),
    ("Config", "config"),
    ("Notice", "notice"),
    ("Banner", "banner"),
    ("BrandPack", "brandpack"),
    ("Region", "region"),
    ("SiteMessage", "sitemessage"),
    ("IpAccessRule", "iprule"),
    ("IpRule", "iprule"),
    ("LoginLog", "loginlog"),
    ("OperationLog", "operationlog"),
    ("AuditChain", "audit"),
    ("JobRunner", "job"),
    ("Product", "product"),
    ("Workbench", "workbench"),
    ("I18n", "i18n"),
    ("PlatformI18n", "i18n"),
    ("DatabaseMessageSource", "i18n"),
    ("DatabaseI18n", "i18n"),
    ("PasswordPolicy", "user"),
    ("PasswordHistory", "user"),
    ("Mfa", "auth"),
    ("Captcha", "auth"),
    ("Auth", "auth"),
    ("LoginBrand", "auth"),
    ("LoginSecurity", "auth"),
    ("Permission", "auth"),
    ("Online", "auth"),
    ("ChangePassword", "auth"),
    ("AvatarSettingsProvider", "config"),
    ("FileStorageSettingsProvider", "config"),
    ("LayoutPreference", "user"),
    ("DataScope", "datascope"),
    ("OwnerDeptLookup", "datascope"),
    ("AiSkill", "ai"),
    ("SodRoleGuard", "role"),
    ("WorkflowLifecycleNotifyListener", "sitemessage"),
    ("Workflow", "workflow"),
    ("Route", "menu"),
]

# Old layer folder -> new layer folder (relative to the domain package).
LAYER_MAP = {
    "domain": "domain",
    "mapper": "mapper",
    "service": "service",
    "service.impl": "service/impl",
    "dto": "dto",
    "vo": "vo",
    "convert": "convert",
    "support": "support",
    "excel": "excel",
    "constant": "constant",
    "listener": "listener",
    "config": "config",
    "workflow": "workflow",
    "util": "support",
    "fileaccess": "support",
    "async": "support",
    "job": "support",
}


def domain_for(class_name: str) -> str | None:
    for prefix, domain in PREFIX_DOMAIN:
        if class_name.startswith(prefix):
            return domain
    return None


def compute_move(rel: str) -> tuple[str | None, Path | None]:
    """Return (new_package, new_relative_path) for a file, or (None, None) if it stays."""
    parts = Path(rel).parts
    class_name = parts[-1][:-5]
    old_layer = ".".join(parts[:-1])
    if parts[0] in ("ai", "datascope", "i18n"):
        # These domains already follow the target style; leave them in place.
        return None, None
    domain = domain_for(class_name)
    if domain is None:
        raise SystemExit(f"cannot classify {rel}")
    if old_layer == "":
        return None, None
    if domain == "workflow" and old_layer == "workflow":
        return None, None
    layer = LAYER_MAP.get(old_layer)
    if layer is None:
        raise SystemExit(f"unknown layer {old_layer!r} for {rel}")
    new_pkg = f"com.icoolkj.platform.system.{domain}"
    if layer:
        new_pkg += "." + layer.replace("/", ".")
    new_rel = Path(domain) / layer / parts[-1]
    return new_pkg, new_rel


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8", newline="")


def write_text(path: Path, text: str) -> None:
    path.write_text(text, encoding="utf-8", newline="")


def build_plan() -> list[tuple[Path, Path, str, str, str, str]]:
    """Return [(src, dest, old_pkg, new_pkg, old_fqcn, new_fqcn), ...]."""
    plan: list[tuple[Path, Path, str, str, str, str]] = []
    for root in (SYSTEM_MAIN, SYSTEM_TEST):
        if not root.exists():
            continue
        for f in sorted(root.rglob("*.java")):
            rel = str(f.relative_to(root)).replace("\\", "/")
            new_pkg, new_rel = compute_move(rel)
            if new_pkg is None:
                continue
            class_name = f.stem
            old_pkg = "com.icoolkj.platform.system." + ".".join(Path(rel).parent.parts)
            old_fqcn = f"{old_pkg}.{class_name}"
            new_fqcn = f"{new_pkg}.{class_name}"
            dest = root / new_rel
            plan.append((f, dest, old_pkg, new_pkg, old_fqcn, new_fqcn))
    return plan


def move_files(plan: list[tuple[Path, Path, str, str, str, str]]) -> None:
    for src, dest, old_pkg, new_pkg, _old_fqcn, _new_fqcn in plan:
        text = read_text(src)
        text = text.replace(f"package {old_pkg};", f"package {new_pkg};", 1)
        dest.parent.mkdir(parents=True, exist_ok=True)
        write_text(dest, text)
        if dest.resolve() != src.resolve():
            src.unlink()
        print(f"moved  {src.relative_to(ROOT)}  ->  {dest.relative_to(ROOT)}")


def rewrite_references(plan: list[tuple[Path, Path, str, str, str, str]]) -> None:
    mapping = {old: new for _s, _d, _p, _q, old, new in plan}
    rewritten = 0
    for path in BACKEND.rglob("*"):
        if path.is_dir() or "target" in path.parts:
            continue
        if path.suffix not in (".java", ".xml", ".vm", ".properties", ".yml", ".yaml"):
            continue
        text = read_text(path)
        new_text = text
        for old_fqcn, new_fqcn in mapping.items():
            new_text = new_text.replace(old_fqcn, new_fqcn)
        if new_text != text:
            write_text(path, new_text)
            rewritten += 1
    print(f"rewritten references in {rewritten} files")


def move_mapper_xmls(plan: list[tuple[Path, Path, str, str, str, str]]) -> None:
    moved = 0
    if not SYSTEM_MAPPER_RES.exists():
        return
    for xml in sorted(SYSTEM_MAPPER_RES.rglob("*.xml")):
        rel = xml.relative_to(SYSTEM_MAPPER_RES)
        if rel.parent.name == "ai":
            continue  # already in the domain folder
        mapper_class = xml.stem
        domain = domain_for(mapper_class)
        if domain is None:
            raise SystemExit(f"cannot classify mapper xml {xml}")
        dest = SYSTEM_MAPPER_RES / domain / xml.name
        dest.parent.mkdir(parents=True, exist_ok=True)
        if dest.resolve() != xml.resolve():
            xml.replace(dest)
            moved += 1
    print(f"moved {moved} mapper xml files into domain folders")


def cleanup_empty_dirs() -> None:
    removed = 0
    for root in (SYSTEM_MAIN, SYSTEM_TEST, SYSTEM_MAPPER_RES):
        if not root.exists():
            continue
        for dirpath in sorted((p for p in root.rglob("*") if p.is_dir()), reverse=True):
            try:
                dirpath.rmdir()
            except OSError:
                continue
            else:
                removed += 1
    print(f"removed {removed} empty directories")


def add_missing_imports() -> None:
    """Add imports for same-old-package references that split across domains.

    Before the restructure, classes living in one flat layer package (e.g.
    ``system.vo``) referenced each other without an import. After the move they
    live in different domain packages, so the missing imports must be added.

    Only code references count: comment text and string literals are stripped
    before scanning, so a javadoc mention does not produce a spurious import.
    Files are only rewritten when a missing import is actually needed, and new
    imports are inserted at the end of the existing import block so the diff
    stays minimal.
    """
    class_fqcns: dict[str, list[str]] = {}
    for root in (SYSTEM_MAIN, SYSTEM_TEST):
        for f in root.rglob("*.java"):
            rel = str(f.relative_to(root)).replace("\\", "/")
            pkg = "com.icoolkj.platform.system." + ".".join(Path(rel).parent.parts)
            class_fqcns.setdefault(f.stem, []).append(f"{pkg}.{f.stem}")

    def code_only(text: str) -> str:
        code = re.sub(r"/\*.*?\*/", "", text, flags=re.DOTALL)
        code = re.sub(r"//[^\n]*", "", code)
        code = re.sub(r'"(?:\\.|[^"\\])*"', "", code)
        return code

    added = 0
    for path in BACKEND.rglob("*.java"):
        if "target" in path.parts:
            continue
        text = read_text(path)
        lines = text.splitlines(keepends=True)
        own_types = set(
            re.findall(
                r"(?m)^\s*(?:public\s+|protected\s+|private\s+)?"
                r"(?:abstract\s+|final\s+|static\s+)*(?:class|interface|enum|record|@interface)\s+(\w+)",
                text,
            )
        )
        existing_imports = set(re.findall(r"(?m)^import\s+(?:static\s+)?([\w.]+)\s*;", text))
        imported_simple = {fq.split(".")[-1] for fq in existing_imports}
        pkg_match = re.search(r"(?m)^package\s+([\w.]+)\s*;", text)
        current_pkg = pkg_match.group(1) if pkg_match else ""

        code = code_only(text)
        needed: list[str] = []
        for name, fqcns in class_fqcns.items():
            if name in own_types or name in imported_simple:
                continue
            if any(fq in code for fq in fqcns):
                continue  # already referenced fully qualified
            if not re.search(rf"\b{re.escape(name)}\b", code):
                continue
            if any(fq.startswith(current_pkg + ".") for fq in fqcns):
                continue  # same package, no import required
            needed.append(fqcns[0])

        if not needed:
            continue
        # Insert the new imports at the end of the import block (or right after
        # the package declaration when the file has no imports yet).
        new_imports = [f"import {fq};\n" for fq in sorted(set(needed))]
        import_indices = [
            i for i, line in enumerate(lines) if re.match(r"^\s*import\s+", line)
        ]
        if import_indices:
            insert_at = import_indices[-1] + 1
        else:
            pkg_idx = next(
                (i for i, line in enumerate(lines) if line.startswith("package ")),
                -1,
            )
            insert_at = pkg_idx + 1
            if insert_at < len(lines) and lines[insert_at].strip() == "":
                insert_at += 1
        new_lines = lines[:insert_at] + new_imports + lines[insert_at:]
        write_text(path, "".join(new_lines))
        added += len(needed)
    print(f"added {added} missing imports")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--apply", action="store_true", help="apply the restructure")
    args = parser.parse_args()

    plan = build_plan()
    print(f"plan: {len(plan)} files to move")
    by_domain: dict[str, int] = {}
    for _s, _d, _p, new_pkg, _a, _b in plan:
        domain = new_pkg.split(".")[4]
        by_domain[domain] = by_domain.get(domain, 0) + 1
    for domain in sorted(by_domain):
        print(f"  {domain:<14} {by_domain[domain]:>3}")

    if not args.apply:
        print("dry-run: pass --apply to move files and rewrite references")
        return 0

    move_files(plan)
    rewrite_references(plan)
    move_mapper_xmls(plan)
    add_missing_imports()
    cleanup_empty_dirs()
    return 0


if __name__ == "__main__":
    sys.exit(main())
