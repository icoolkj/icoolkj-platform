#!/usr/bin/env python3
"""
iCoolKJ 代码生成器 — 单表 CRUD 生成。

用法:
  python tools/icoolkj-codegen/codegen.py --list
  python tools/icoolkj-codegen/codegen.py --all
  python tools/icoolkj-codegen/codegen.py --crud-only
  python tools/icoolkj-codegen/codegen.py --tables sys_post --artifacts service,controller
  python tools/icoolkj-codegen/codegen.py --tables sys_post --dry-run
"""
from __future__ import annotations

import argparse
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(Path(__file__).resolve().parent))

from icoolkj_codegen.engine import ALL_ARTIFACTS, list_tables, run_generate  # noqa: E402


def main() -> None:
    parser = argparse.ArgumentParser(description="iCoolKJ Platform code generator")
    parser.add_argument("--all", action="store_true", help="全部表")
    parser.add_argument("--crud-only", action="store_true", help="仅 crud 可生成完整栈的表")
    parser.add_argument("--module", choices=["system", "infra"], help="模块过滤")
    parser.add_argument("--tables", help="逗号分隔表名")
    parser.add_argument(
        "--artifacts",
        default="entity,mapper,xml,dto,vo,convert,service,serviceImpl,controller",
        help="生成产物，逗号分隔；加 frontend 可生成 API 占位",
    )
    parser.add_argument("--list", action="store_true", help="列出表")
    parser.add_argument("--dry-run", action="store_true", help="仅打印路径")
    args = parser.parse_args()

    if args.list:
        list_tables()
        return

    if not args.all and not args.module and not args.tables and not args.crud_only:
        parser.error("请指定 --all、--crud-only、--module 或 --tables")

    artifacts = {a.strip() for a in args.artifacts.split(",") if a.strip()}
    unknown = artifacts - ALL_ARTIFACTS
    if unknown:
        parser.error(f"未知 artifacts: {unknown}")

    tables = [t.strip() for t in args.tables.split(",")] if args.tables else None
    run_generate(
        all_tables=args.all,
        module=args.module,
        tables=tables,
        artifacts=artifacts,
        crud_only=args.crud_only,
        dry_run=args.dry_run,
    )


if __name__ == "__main__":
    main()
