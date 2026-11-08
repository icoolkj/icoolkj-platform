"""Table metadata and SQL parsing."""
from __future__ import annotations

import re
from dataclasses import dataclass, field

SQL_TYPE_MAP = {
    "varchar": "String",
    "char": "String",
    "text": "String",
    "longtext": "String",
    "mediumtext": "String",
    "tinyint": "Integer",
    "smallint": "Integer",
    "int": "Integer",
    "bigint": "Long",
    "decimal": "java.math.BigDecimal",
    "datetime": "java.time.LocalDateTime",
    "timestamp": "java.time.LocalDateTime",
    "date": "java.time.LocalDate",
    "json": "String",
}

BASE_SKIP = {
    "tenant": {"id", "tenant_id", "created_by", "created_at", "updated_by", "updated_at", "deleted"},
    "base": {"id", "created_by", "created_at", "updated_by", "updated_at", "deleted"},
    "platform": {"id", "created_by", "created_at", "updated_by", "updated_at", "deleted"},
    "plain": set(),
}

AUDIT_FIELDS = {"id", "created_by", "created_at", "updated_by", "updated_at", "deleted"}
VO_EXTRA = {"created_at", "updated_at", "tenant_id"}


def snake_to_camel(name: str) -> str:
    parts = name.split("_")
    return parts[0] + "".join(p.title() for p in parts[1:])


def camel_to_snake(name: str) -> str:
    return re.sub(r"(?<!^)(?=[A-Z])", "_", name).lower()


def parse_tables(sql: str) -> dict[str, list[tuple[str, str]]]:
    out: dict[str, list[tuple[str, str]]] = {}
    for m in re.finditer(r"create table if not exists (\w+)\s*\((.*?)\)\s*engine=", sql, re.I | re.S):
        table = m.group(1)
        cols: list[tuple[str, str]] = []
        for line in m.group(2).splitlines():
            line = line.strip().rstrip(",")
            if not line or line.lower().startswith(("primary key", "unique key", "key ")):
                continue
            cm = re.match(r"(\w+)\s+(\w+)", line, re.I)
            if not cm:
                continue
            col, sql_type = cm.group(1), cm.group(2).lower()
            cols.append((col, SQL_TYPE_MAP.get(sql_type, "String")))
        out[table] = cols
    return out


def short_type(java_type: str) -> str:
    if java_type.startswith("java."):
        return java_type.rsplit(".", 1)[-1]
    return java_type


def java_imports_for_type(java_type: str) -> set[str]:
    if java_type.startswith("java."):
        return {java_type}
    if java_type == "LocalDateTime":
        return {"java.time.LocalDateTime"}
    if java_type == "LocalDate":
        return {"java.time.LocalDate"}
    if java_type == "BigDecimal":
        return {"java.math.BigDecimal"}
    return set()


@dataclass
class Column:
    name: str
    java_type: str
    prop: str

    @property
    def short_type(self) -> str:
        return short_type(self.java_type)


@dataclass
class TableContext:
    table: str
    entity: str
    mapper: str
    module: str
    api_module: str
    base: str
    crud: str
    comment: str
    list_filters: list[str]
    has_id: bool
    has_deleted: bool
    order_by: str
    columns: list[Column]
    pkg: str
    java_dir: str
    xml_dir: str
    api_dir: str
    route_prefix: str
    route_name: str
    biz_name: str
    service_name: str
    service_impl_name: str
    controller_name: str
    page_query_name: str
    create_request_name: str
    update_request_name: str
    vo_name: str
    convert_name: str
    business_columns: list[Column] = field(default_factory=list)
    vo_columns: list[Column] = field(default_factory=list)
    create_columns: list[Column] = field(default_factory=list)
    update_columns: list[Column] = field(default_factory=list)

    @property
    def route_path(self) -> str:
        return f"{self.route_prefix}/{self.route_name}"

    @property
    def permission_prefix(self) -> str:
        seg = self.route_prefix.lstrip("/").replace("/", ":")
        return f"{seg}:{self.route_name}"


def resolve_has_deleted(table_cfg: dict, col_names: list[str]) -> bool:
    if "has_deleted" in table_cfg:
        return bool(table_cfg["has_deleted"])
    base = table_cfg.get("base", "plain")
    if base in ("tenant", "base", "platform"):
        return True
    return "deleted" in col_names


def resolve_crud(table_cfg: dict) -> str:
    if "crud" in table_cfg:
        return table_cfg["crud"]
    if not table_cfg.get("has_id", True):
        return "none"
    base = table_cfg.get("base", "plain")
    if base == "plain":
        return "readonly"
    return "full"


def build_context(cfg: dict, table_cfg: dict, col_defs: list[tuple[str, str]]) -> TableContext:
    module_key = table_cfg["module"]
    mod = cfg["modules"][module_key]
    api_module_key = table_cfg.get("api_module", module_key)
    api_mod = cfg["modules"].get(api_module_key, mod)

    entity = table_cfg["entity"]
    biz_name = entity.removesuffix("Entity")
    route_name = table_cfg.get("route_name", biz_name[0].lower() + biz_name[1:])
    controller_name = table_cfg.get("controller_name", f"{biz_name}Controller")

    columns = [Column(c, t, snake_to_camel(c)) for c, t in col_defs]
    col_names = [c.name for c in columns]
    base = table_cfg.get("base", "plain")
    skip = BASE_SKIP.get(base, set())

    business_columns = [c for c in columns if c.name not in skip]
    vo_cols = []
    for c in columns:
        if c.name in skip and c.name not in VO_EXTRA and c.name != "id":
            continue
        if c.name == "deleted":
            continue
        vo_cols.append(c)

    create_cols = [c for c in business_columns if c.name != "tenant_id" or base == "platform"]
    update_cols = [c for c in create_cols if c.name != "id"]

    order_default = "created_at DESC" if "created_at" in col_names else ("id DESC" if "id" in col_names else "1")

    return TableContext(
        table=table_cfg["table"],
        entity=entity,
        mapper=table_cfg["mapper"],
        module=module_key,
        api_module=api_module_key,
        base=base,
        crud=resolve_crud(table_cfg),
        comment=table_cfg.get("comment", biz_name),
        list_filters=table_cfg.get("list_filters", []),
        has_id=table_cfg.get("has_id", True),
        has_deleted=resolve_has_deleted(table_cfg, col_names),
        order_by=table_cfg.get("order_by") or order_default,
        columns=columns,
        pkg=mod["package"],
        java_dir=mod["java_dir"],
        xml_dir=mod["xml_dir"],
        api_dir=api_mod["api_dir"],
        route_prefix=api_mod["route_prefix"],
        route_name=route_name,
        biz_name=biz_name,
        service_name=table_cfg.get("service_name", f"{biz_name}Service"),
        service_impl_name=table_cfg.get("service_impl_name", f"{biz_name}ServiceImpl"),
        controller_name=controller_name,
        page_query_name=f"{biz_name}PageQuery",
        create_request_name=f"{biz_name}CreateRequest",
        update_request_name=f"{biz_name}UpdateRequest",
        vo_name=f"{biz_name}VO",
        convert_name=f"{biz_name}Convert",
        business_columns=business_columns,
        vo_columns=vo_cols,
        create_columns=create_cols,
        update_columns=update_cols,
    )
