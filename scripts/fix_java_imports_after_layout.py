#!/usr/bin/env python3
"""Add missing imports after platform package restructure."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA_ROOTS = [
    ROOT / "backend" / "icoolkj-admin-system" / "src" / "main" / "java",
    ROOT / "backend" / "icoolkj-admin-infra" / "src" / "main" / "java",
    ROOT / "backend" / "icoolkj-admin-api" / "src" / "main" / "java",
    ROOT / "backend" / "icoolkj-admin-framework" / "src" / "main" / "java",
]


def index_types() -> dict[str, str]:
    index: dict[str, str] = {}
    for root in JAVA_ROOTS:
        if not root.exists():
            continue
        for path in root.rglob("*.java"):
            text = path.read_text(encoding="utf-8")
            pkg_m = re.search(r"^package\s+([\w.]+);", text, re.M)
            cls_m = re.search(r"^public\s+(?:class|interface|enum)\s+(\w+)", text, re.M)
            if pkg_m and cls_m:
                index[cls_m.group(1)] = f"{pkg_m.group(1)}.{cls_m.group(1)}"
    return index


def existing_imports(text: str) -> set[str]:
    return set(re.findall(r"^import\s+([\w.]+);", text, re.M))


def needs_import(text: str, simple: str, fqcn: str, package: str) -> bool:
    if simple not in text:
        return False
    if fqcn in existing_imports(text):
        return False
    if f"import {fqcn};" in text:
        return False
    type_pkg = fqcn.rsplit(".", 1)[0]
    if type_pkg == package:
        return False
    # skip if fully qualified usage already present
    if fqcn in text:
        return False
    return True


def fix_file(path: Path, index: dict[str, str]) -> bool:
    text = path.read_text(encoding="utf-8")
    pkg_m = re.search(r"^package\s+([\w.]+);", text, re.M)
    if not pkg_m:
        return False
    package = pkg_m.group(1)
    cls_m = re.search(r"^public\s+(?:class|interface|enum)\s+(\w+)", text, re.M)
    current_class = cls_m.group(1) if cls_m else None

    to_add: list[str] = []
    for simple, fqcn in sorted(index.items(), key=lambda x: x[1]):
        if current_class and simple == current_class:
            continue
        if needs_import(text, simple, fqcn, package):
            to_add.append(fqcn)

    if not to_add:
        return False

    # insert after package declaration
    imports_block = "\n".join(f"import {fqcn};" for fqcn in sorted(set(to_add)))
    if re.search(r"^import\s+", text, re.M):
        # after last import
        new_text = re.sub(
            r"(^import\s+[\w.]+;\n)(?!import )",
            rf"\1{imports_block}\n",
            text,
            count=1,
            flags=re.M,
        )
        if new_text == text:
            new_text = text.replace(
                f"package {package};",
                f"package {package};\n\n{imports_block}",
                1,
            )
    else:
        new_text = text.replace(
            f"package {package};",
            f"package {package};\n\n{imports_block}",
            1,
        )

    path.write_text(new_text, encoding="utf-8")
    return True


def main() -> None:
    index = index_types()
    changed = 0
    for root in JAVA_ROOTS:
        for path in root.rglob("*.java"):
            if fix_file(path, index):
                changed += 1
    # second pass for nested dependencies
    for _ in range(3):
        index = index_types()
        for root in JAVA_ROOTS:
            for path in root.rglob("*.java"):
                if fix_file(path, index):
                    changed += 1
    print(f"Fixed imports in {changed} files")


if __name__ == "__main__":
    main()
