"""Code generation engine."""
from __future__ import annotations

from pathlib import Path

import yaml

from .column_meta import build_column_meta, parse_sql_comments
from .model import TableContext, build_context, parse_tables
from .render import (
    render_controller,
    render_convert,
    render_create_request,
    render_entity,
    render_mapper_java,
    render_mapper_xml,
    render_page_query,
    render_service,
    render_service_impl,
    render_update_request,
    render_vo,
)
from .render_frontend import render_frontend_api_ts, render_frontend_types, render_frontend_view

CODEGEN_ROOT = Path(__file__).resolve().parents[1]
PROJECT_ROOT = CODEGEN_ROOT.parents[1]
CONFIG_FILE = CODEGEN_ROOT / "config" / "tables.yaml"
CUSTOM_DIR = CODEGEN_ROOT / "custom"
FRONTEND_ROOT = PROJECT_ROOT / "frontend" / "src"

ALL_ARTIFACTS = {
    "entity",
    "mapper",
    "xml",
    "dto",
    "vo",
    "convert",
    "service",
    "serviceImpl",
    "controller",
    "frontend",
    "frontend-api",
    "frontend-types",
    "frontend-view",
}


def load_config() -> dict:
    with CONFIG_FILE.open(encoding="utf-8") as f:
        return yaml.safe_load(f)


def load_custom(mapper_name: str, ext: str) -> str:
    path = CUSTOM_DIR / f"{mapper_name}.{ext}.frag"
    return path.read_text(encoding="utf-8") if path.exists() else ""


def should_gen_stack(table_cfg: dict) -> bool:
    if table_cfg.get("gen_stack") is False:
        return False
    return table_cfg.get("crud", resolve_crud_default(table_cfg)) != "none"


def resolve_crud_default(table_cfg: dict) -> str:
    if "crud" in table_cfg:
        return table_cfg["crud"]
    if not table_cfg.get("has_id", True):
        return "none"
    if table_cfg.get("base", "plain") == "plain":
        return "readonly"
    return "full"


def write_file(path: Path, content: str, dry_run: bool) -> None:
    if dry_run:
        print(f"  [dry-run] {path.relative_to(PROJECT_ROOT)}")
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")
    print(f"  -> {path.relative_to(PROJECT_ROOT)}")


def _frontend_artifacts(artifacts: set[str]) -> set[str]:
    if "frontend" in artifacts:
        return artifacts | {"frontend-api", "frontend-types", "frontend-view"}
    return artifacts


def generate_table(
    cfg: dict,
    table_cfg: dict,
    col_defs: list[tuple[str, str]],
    artifacts: set[str],
    dry_run: bool = False,
    sql_comments: dict[str, dict[str, str]] | None = None,
) -> None:
    artifacts = _frontend_artifacts(artifacts)
    ctx = build_context(cfg, table_cfg, col_defs)
    root = PROJECT_ROOT
    extra_java = [ln for ln in load_custom(ctx.mapper, "java").splitlines() if ln.strip()]
    extra_xml = load_custom(ctx.mapper, "xml")
    gen_stack = should_gen_stack(table_cfg)

    if "entity" in artifacts:
        write_file(root / ctx.java_dir / "domain" / f"{ctx.entity}.java", render_entity(ctx), dry_run)

    if "mapper" in artifacts:
        write_file(
            root / ctx.java_dir / "mapper" / f"{ctx.mapper}.java",
            render_mapper_java(ctx, extra_java),
            dry_run,
        )

    if "xml" in artifacts:
        write_file(
            root / ctx.xml_dir / f"{ctx.mapper}.xml",
            render_mapper_xml(ctx, extra_xml),
            dry_run,
        )

    if not gen_stack:
        return

    if "dto" in artifacts:
        write_file(root / ctx.java_dir / "dto" / f"{ctx.page_query_name}.java", render_page_query(ctx), dry_run)
        if ctx.crud == "full":
            write_file(
                root / ctx.java_dir / "dto" / f"{ctx.create_request_name}.java",
                render_create_request(ctx),
                dry_run,
            )
            write_file(
                root / ctx.java_dir / "dto" / f"{ctx.update_request_name}.java",
                render_update_request(ctx),
                dry_run,
            )

    if "vo" in artifacts:
        write_file(root / ctx.java_dir / "vo" / f"{ctx.vo_name}.java", render_vo(ctx), dry_run)

    if "convert" in artifacts and ctx.crud != "none":
        write_file(root / ctx.java_dir / "convert" / f"{ctx.convert_name}.java", render_convert(ctx), dry_run)

    if "service" in artifacts:
        write_file(root / ctx.java_dir / "service" / f"{ctx.service_name}.java", render_service(ctx), dry_run)

    if "serviceImpl" in artifacts:
        write_file(
            root / ctx.java_dir / "service" / "impl" / f"{ctx.service_impl_name}.java",
            render_service_impl(ctx),
            dry_run,
        )

    if "controller" in artifacts:
        write_file(
            root / ctx.api_dir / f"{ctx.controller_name}.java",
            render_controller(ctx),
            dry_run,
        )

    if "frontend-api" in artifacts or "frontend-types" in artifacts or "frontend-view" in artifacts:
        if not gen_stack:
            return
        comments = (sql_comments or {}).get(ctx.table, {})
        metas = build_column_meta(ctx, comments)
        mod = ctx.api_module
        if "frontend-types" in artifacts:
            write_file(
                FRONTEND_ROOT / "types" / mod / f"{ctx.route_name}.ts",
                render_frontend_types(ctx, metas),
                dry_run,
            )
        if "frontend-api" in artifacts:
            write_file(
                FRONTEND_ROOT / "api" / mod / f"{ctx.route_name}.ts",
                render_frontend_api_ts(ctx),
                dry_run,
            )
        if "frontend-view" in artifacts:
            write_file(
                FRONTEND_ROOT / "views" / mod / ctx.route_name / "index.vue",
                render_frontend_view(ctx, metas),
                dry_run,
            )


def run_generate(
    *,
    all_tables: bool = False,
    module: str | None = None,
    tables: list[str] | None = None,
    artifacts: set[str] | None = None,
    crud_only: bool = False,
    dry_run: bool = False,
) -> int:
    cfg = load_config()
    sql_path = PROJECT_ROOT / cfg["sql_file"]
    tables_sql = parse_tables(sql_path.read_text(encoding="utf-8"))
    selected = cfg["tables"]
    arts = artifacts or ALL_ARTIFACTS

    if not all_tables:
        if module:
            selected = [t for t in selected if t["module"] == module]
        if tables:
            names = set(tables)
            selected = [t for t in selected if t["table"] in names]
    if crud_only:
        selected = [t for t in selected if should_gen_stack(t)]

    print(f"生成 {len(selected)} 张表, artifacts={','.join(sorted(arts))}")
    for table_cfg in selected:
        table = table_cfg["table"]
        col_defs = tables_sql.get(table)
        if not col_defs:
            print(f"  SKIP {table}: SQL 中未找到")
            continue
        print(f"[{table}] {table_cfg.get('comment', '')}")
        generate_table(cfg, table_cfg, col_defs, arts, dry_run, sql_comments)
    print("完成。")
    return 0


def list_tables() -> None:
    cfg = load_config()
    sql_path = PROJECT_ROOT / cfg["sql_file"]
    tables_sql = parse_tables(sql_path.read_text(encoding="utf-8"))
    for t in cfg["tables"]:
        ok = "✓" if t["table"] in tables_sql else "✗"
        crud = t.get("crud", resolve_crud_default(t))
        stack = "stack" if should_gen_stack(t) else "mapper-only"
        print(f"{ok} [{t['module']}] {t['table']} -> {t['entity']} | crud={crud} | {stack}")
