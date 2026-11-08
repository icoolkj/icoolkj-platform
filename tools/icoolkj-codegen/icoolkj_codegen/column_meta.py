"""Column UI metadata heuristics for Gen forms and tables."""
from __future__ import annotations

from dataclasses import dataclass

from .model import Column, TableContext, snake_to_camel

SKIP_LIST = {"id", "created_by", "updated_by", "deleted", "password_hash"}
SKIP_QUERY = SKIP_LIST | {"created_at", "updated_at", "remark"}
SKIP_FORM = {"id", "created_by", "created_at", "updated_by", "updated_at", "deleted", "tenant_id", "password_hash"}


@dataclass
class ColumnMeta:
    name: str
    prop: str
    label: str
    ts_type: str
    html_type: str
    is_query: bool
    is_list: bool
    is_form: bool
    query_like: bool


def _ts_type(java_short: str) -> str:
    mapping = {
        "String": "string",
        "Integer": "number",
        "Long": "number",
        "BigDecimal": "number",
        "LocalDateTime": "string",
        "LocalDate": "string",
    }
    return mapping.get(java_short, "string")


def _html_type(col: Column) -> str:
    name = col.name.lower()
    if name.endswith("status") or name.endswith("gender"):
        return "radio"
    if name.endswith("type") or name.endswith("_type"):
        return "select"
    if "time" in name or "date" in name or name.endswith("_at"):
        return "datetime"
    if name in ("remark", "content", "notice_content") or name.endswith("_content"):
        return "textarea"
    return "input"


def _label(col: Column) -> str:
    # tables.yaml 可扩展 comment；暂用字段名
    return col.prop


def build_column_meta(ctx: TableContext, comment_map: dict[str, str] | None = None) -> list[ColumnMeta]:
    comment_map = comment_map or {}
    query_set = set(ctx.list_filters)
    metas: list[ColumnMeta] = []

    def label_for(col: Column) -> str:
        return comment_map.get(col.name, _label(col))

    for col in ctx.business_columns:
        if col.name in SKIP_LIST and col.name != "status":
            continue
        metas.append(
            ColumnMeta(
                name=col.name,
                prop=col.prop,
                label=label_for(col),
                ts_type=_ts_type(col.short_type),
                html_type=_html_type(col),
                is_query=col.prop in query_set or (col.name not in SKIP_QUERY and col.name not in ("id",)),
                is_list=col.name not in SKIP_LIST,
                is_form=col.name not in SKIP_FORM,
                query_like=col.name.endswith("name") or col.name.endswith("title"),
            )
        )
    # id + audit for list
    for col in ctx.vo_columns:
        if col.name in ("id", "created_at", "updated_at") and not any(m.prop == col.prop for m in metas):
            metas.insert(0 if col.name == "id" else len(metas), ColumnMeta(
                name=col.name,
                prop=col.prop,
                label=label_for(col),
                ts_type=_ts_type(col.short_type),
                html_type="input",
                is_query=False,
                is_list=True,
                is_form=False,
                query_like=False,
            ))
    return metas


def parse_sql_comments(sql: str) -> dict[str, dict[str, str]]:
    """Parse column comments from CREATE TABLE for labels."""
    import re
    out: dict[str, dict[str, str]] = {}
    for m in re.finditer(r"create table if not exists (\w+)\s*\((.*?)\)\s*engine=", sql, re.I | re.S):
        table = m.group(1)
        cols: dict[str, str] = {}
        for line in m.group(2).splitlines():
            line = line.strip().rstrip(",")
            cm = re.match(r"(\w+)\s+\w+.*?comment\s+'([^']*)'", line, re.I)
            if cm:
                cols[cm.group(1)] = cm.group(2).split("（")[0].split("(")[0].strip()
        out[table] = cols
    return out
