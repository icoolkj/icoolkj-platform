# -*- coding: utf-8 -*-
"""
Verify Flyway SQL single-source policy.

Canonical source: backend/database/migration/V*.sql
Runtime classpath: icoolkj-admin-api/target/classes/db/migration/ (Maven build)

Exit 0 = OK, 1 = duplicate or content drift.
"""
from __future__ import annotations

import hashlib
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CANONICAL_DIR = ROOT / "backend" / "database" / "migration"
LEGACY_DOCS_DIR = ROOT / "docs" / "database"
COMMITTED_COPY_DIR = (
    ROOT / "backend" / "icoolkj-admin-api" / "src" / "main" / "resources" / "db" / "migration"
)
BUILD_COPY_DIR = (
    ROOT / "backend" / "icoolkj-admin-api" / "target" / "classes" / "db" / "migration"
)


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def list_sql(directory: Path) -> list[Path]:
    if not directory.is_dir():
        return []
    return sorted(directory.glob("V*.sql"))


def main() -> int:
    errors: list[str] = []

    source_files = list_sql(CANONICAL_DIR)
    if not source_files:
        errors.append(f"missing Flyway SQL under {CANONICAL_DIR}")

    print(f"canonical source ({len(source_files)} file(s)):")
    for path in source_files:
        print(f"  - {path.relative_to(ROOT)}  sha256={sha256(path)[:16]}...")

    if LEGACY_DOCS_DIR.exists():
        errors.append(
            f"legacy docs/database/ still exists; SQL should live under backend/database/ only"
        )

    stale_committed = list_sql(COMMITTED_COPY_DIR)
    if stale_committed:
        errors.append(
            "stale SQL under backend/.../src/main/resources/db/migration/; "
            "use backend/database/migration/ only"
        )
        for path in stale_committed:
            print(f"  stale: {path.relative_to(ROOT)}")

    build_files = list_sql(BUILD_COPY_DIR)
    if build_files:
        print(f"build output ({len(build_files)} file(s)):")
        source_map = {p.name: p for p in source_files}
        for path in build_files:
            digest = sha256(path)
            print(f"  - {path.relative_to(ROOT)}  sha256={digest[:16]}...")
            source = source_map.get(path.name)
            if source is None:
                errors.append(f"build copy without canonical source: {path.name}")
            elif sha256(source) != digest:
                errors.append(f"content drift: {path.name}")

    if errors:
        print("\nFAILED:")
        for item in errors:
            print(f"  - {item}")
        return 1

    print("\nOK: Flyway SQL single-source policy satisfied.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
