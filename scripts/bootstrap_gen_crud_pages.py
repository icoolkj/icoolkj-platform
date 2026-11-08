#!/usr/bin/env python3
"""Bootstrap GenCrudPage shells + i18n/gen JSON for platform menu CRUD pages."""
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FE_GEN = ROOT / "frontend" / "src" / "i18n" / "gen"
BE_GEN = ROOT / "backend" / "icoolkj-admin-system" / "src" / "main" / "resources" / "i18n" / "gen"
FE_VIEWS = ROOT / "frontend" / "src" / "views"


def write_json(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def shell_vue(
    *,
    comment: str,
    business_name: str,
    api_module: str,
    class_name: str,
    extra_template: str = "",
    extra_script: str = "",
) -> str:
    return f"""<!-- AUTO-GENERATED {comment} — GenCrudPage + view.json -->
<template>
  <GenCrudPage
    :file-view="viewConfig"
    business-name="{business_name}"
    api-module="{api_module}"
    :i18n-files="GEN_I18N_FILES"
    :perm="PERM"
    :api="crudApi"
    {extra_template.strip()}
  >
{extra_template.split('props=')[0] if False else ''}  </GenCrudPage>
</template>

<script setup lang="ts">
import GenCrudPage from '@/components/gen/GenCrudPage.vue'
import type {{ GenCrudApi }} from '@/components/gen/GenCrudPage.vue'
import type {{ GenI18nMessages }} from '@/types/tool/genI18n'
import type {{ GenViewSchema }} from '@/types/tool/genView'
import viewConfigData from '@/i18n/gen/{business_name}/view.json'
import genI18nZhCN from '@/i18n/gen/{business_name}/zh-CN.json'
import genI18nEnUS from '@/i18n/gen/{business_name}/en-US.json'
import {{
  PERM_{class_name} as PERM,
  create{class_name},
  delete{class_name},
  get{class_name},
  page{class_name},
  update{class_name},
}} from '@/api/{api_module}/{business_name}'
{extra_script}

const viewConfig = viewConfigData as GenViewSchema
const GEN_I18N_FILES: Record<string, GenI18nMessages> = {{
  'zh-CN': genI18nZhCN as GenI18nMessages,
  'en-US': genI18nEnUS as GenI18nMessages,
}}

const crudApi: GenCrudApi = {{
  page: page{class_name},
  get: get{class_name},
  create: create{class_name},
  update: update{class_name},
  delete: delete{class_name},
}}
</script>
"""


def std_field(label_zh: str, label_en: str, **kw) -> dict:
    return {"labelZh": label_zh, "labelEn": label_en, **kw}


SELECT_LIKE_COMPONENTS = frozenset({"select", "radio", "tree-select", "date", "datetime"})


def collect_field_components(view: dict | None) -> dict[str, str]:
    """Map field name → component from query/form sections in view.json."""
    if not view:
        return {}
    result: dict[str, str] = {}

    def scan(fields: list | None) -> None:
        for item in fields or []:
            name = item.get("field")
            component = item.get("component")
            if name and component:
                result.setdefault(name, component)

    scan(view.get("query", {}).get("fields"))
    scan(view.get("form", {}).get("fields"))
    master_detail = view.get("masterDetail") or {}
    for panel in ("master", "detail"):
        section = master_detail.get(panel) or {}
        scan(section.get("query", {}).get("fields"))
        scan(section.get("form", {}).get("fields"))
    return result


def auto_placeholder(label: str, component: str, locale: str) -> str:
    if component in SELECT_LIKE_COMPONENTS:
        return f"Please select {label}" if locale == "en" else f"请选择{label}"
    return f"Please enter {label}" if locale == "en" else f"请输入{label}"


def validation_required(label: str, locale: str, component: str | None = None) -> str:
    select = component in SELECT_LIKE_COMPONENTS if component else False
    if locale == "en":
        return f"Please select {label}" if select else f"{label} is required"
    return f"请选择{label}" if select else f"请输入{label}"


def validation_max_length(label: str, max_len: int, locale: str) -> str:
    if locale == "en":
        return f"{label} must not exceed {max_len} characters"
    return f"{label}长度不能超过{max_len}"


def i18n_bundle(
    function_name_zh: str,
    function_name_en: str,
    business_name: str,
    api_module: str,
    fields: dict[str, dict],
    view: dict | None = None,
) -> tuple[dict, dict]:
    constraints = {}
    for name, spec in fields.items():
        c = {}
        if spec.get("required"):
            c["required"] = True
        if spec.get("maxLength"):
            c["maxLength"] = spec["maxLength"]
        if c:
            constraints[name] = c

    field_components = collect_field_components(view)

    def build_fields(locale: str) -> dict:
        out = {}
        for name, spec in fields.items():
            label = spec["labelEn"] if locale == "en" else spec["labelZh"]
            field = {"label": label}
            if spec.get("placeholderZh"):
                ph = spec["placeholderEn"] if locale == "en" else spec["placeholderZh"]
                field["placeholder"] = ph
            elif name in field_components:
                field["placeholder"] = auto_placeholder(label, field_components[name], locale)
            validation = {}
            comp = field_components.get(name)
            if spec.get("required"):
                msg = spec.get("requiredEn" if locale == "en" else "requiredZh")
                validation["required"] = msg or validation_required(label, locale, comp)
            if spec.get("maxLength"):
                msg = spec.get("maxLengthEn" if locale == "en" else "maxLengthZh")
                validation["maxLength"] = msg or validation_max_length(label, spec["maxLength"], locale)
            if validation:
                field["validation"] = validation
            out[name] = field
        return out

    zh = {
        "page": {
            "title": function_name_zh,
            "description": f"管理{function_name_zh}，支持新增、修改与删除",
            "addTitle": f"新增{function_name_zh}",
            "editTitle": f"修改{function_name_zh}",
            "deleteConfirm": "确认删除选中的 {count} 条数据？",
            "statusNormal": "正常",
            "statusDisabled": "停用",
        },
        "fields": build_fields("zh"),
        "meta": {
            "businessName": business_name,
            "apiModule": api_module,
            "functionName": function_name_zh,
            "constraints": constraints,
        },
    }
    en = {
        "page": {
            "title": f"{function_name_en} Management",
            "description": f"Manage {function_name_en.lower()} records",
            "addTitle": f"Add {function_name_en}",
            "editTitle": f"Edit {function_name_en}",
            "deleteConfirm": "Delete {count} selected record(s)?",
            "statusNormal": "Normal",
            "statusDisabled": "Disabled",
        },
        "fields": build_fields("en"),
        "meta": {
            "businessName": business_name,
            "apiModule": api_module,
            "functionName": function_name_en,
            "constraints": constraints,
        },
    }
    return zh, en


def publish(business_name: str, view: dict, zh: dict, en: dict) -> None:
    for base in (FE_GEN, BE_GEN):
        write_json(base / business_name / "view.json", view)
        write_json(base / business_name / "zh-CN.json", zh)
        write_json(base / business_name / "en-US.json", en)


MODULES: list[dict] = []


def module(**kw) -> None:
    MODULES.append(kw)


# --- post ---
_post_fields = {
    "postName": std_field("岗位名称", "Post Name", required=True, maxLength=50,
                          placeholderZh="请输入岗位名称", placeholderEn="Enter post name"),
    "postCode": std_field("岗位编码", "Post Code", required=True, maxLength=64,
                          placeholderZh="请输入岗位编码", placeholderEn="Enter post code"),
    "sortNo": std_field("排序", "Sort", placeholderZh="请输入排序", placeholderEn="Enter sort order"),
    "status": std_field("状态", "Status"),
    "remark": std_field("备注", "Remark", maxLength=500),
}
module(
    business_name="post", api_module="system", class_name="Post",
    function_zh="岗位", function_en="Post", permission_prefix="system:post",
    vue_path=FE_VIEWS / "system/post/index.vue",
    view={
        "meta": {"businessName": "post", "apiModule": "system", "backendModule": "system",
                 "permissionPrefix": "system:post", "className": "Post"},
        "page": {"layout": "standard-crud"},
        "query": {"maxVisible": 4, "collapsible": True, "fields": [
            {"field": "postName", "component": "input", "queryType": "LIKE", "width": 200},
            {"field": "postCode", "component": "input", "queryType": "LIKE", "width": 200},
            {"field": "status", "component": "select", "queryType": "EQ", "dictType": "sys_common_status", "width": 140},
        ]},
        "table": {"fields": [
            {"field": "postName", "component": "text", "minWidth": 140, "showOverflowTooltip": True},
            {"field": "postCode", "component": "text", "minWidth": 140, "showOverflowTooltip": True},
            {"field": "sortNo", "component": "text", "width": 80, "align": "center"},
            {"field": "status", "component": "dict-tag", "dictType": "sys_common_status", "width": 90, "align": "center"},
            {"field": "remark", "component": "text", "minWidth": 140, "showOverflowTooltip": True},
            {"field": "createdAt", "component": "text", "width": 170, "align": "center"},
        ], "actions": {"edit": True, "delete": True, "width": 160}},
        "form": {"mode": "dialog", "dialogWidth": 560, "labelWidth": 100, "fields": [
            {"field": "postName", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "postCode", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "sortNo", "component": "input-number", "insert": True, "edit": True, "span": 24, "min": 0},
            {"field": "status", "component": "radio", "insert": True, "edit": True, "span": 24, "dictType": "sys_common_status"},
            {"field": "remark", "component": "textarea", "insert": True, "edit": True, "span": 24, "rows": 2},
        ]},
    },
    fields=_post_fields,
)

# --- notice ---
_notice_fields = {
    "noticeTitle": std_field("公告标题", "Title", required=True, maxLength=200),
    "noticeType": std_field("公告类型", "Type", required=True),
    "noticeContent": std_field("公告内容", "Content", required=True),
    "status": std_field("状态", "Status"),
    "remark": std_field("备注", "Remark", maxLength=500),
}
module(
    business_name="notice", api_module="system", class_name="Notice",
    function_zh="通知公告", function_en="Notice", permission_prefix="system:notice",
    vue_path=FE_VIEWS / "system/notice/index.vue",
    view={
        "meta": {"businessName": "notice", "apiModule": "system", "backendModule": "system",
                 "permissionPrefix": "system:notice", "className": "Notice"},
        "page": {"layout": "standard-crud"},
        "query": {"fields": [
            {"field": "noticeTitle", "component": "input", "queryType": "LIKE", "width": 200},
            {"field": "noticeType", "component": "select", "queryType": "EQ", "dictType": "sys_notice_type", "width": 140},
            {"field": "status", "component": "select", "queryType": "EQ", "dictType": "sys_common_status", "width": 140},
        ]},
        "table": {"fields": [
            {"field": "noticeTitle", "component": "text", "minWidth": 180, "showOverflowTooltip": True},
            {"field": "noticeType", "component": "dict-tag", "dictType": "sys_notice_type", "width": 100, "align": "center"},
            {"field": "status", "component": "dict-tag", "dictType": "sys_common_status", "width": 90, "align": "center"},
            {"field": "remark", "component": "text", "minWidth": 140, "showOverflowTooltip": True},
            {"field": "createdAt", "component": "text", "width": 170, "align": "center"},
        ], "actions": {"edit": True, "delete": True, "width": 160}},
        "form": {"mode": "dialog", "dialogWidth": 640, "labelWidth": 100, "fields": [
            {"field": "noticeTitle", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "noticeType", "component": "select", "insert": True, "edit": True, "span": 24, "dictType": "sys_notice_type"},
            {"field": "noticeContent", "component": "textarea", "insert": True, "edit": True, "span": 24, "rows": 6},
            {"field": "status", "component": "radio", "insert": True, "edit": True, "span": 24, "dictType": "sys_common_status"},
            {"field": "remark", "component": "textarea", "insert": True, "edit": True, "span": 24, "rows": 2},
        ]},
    },
    fields=_notice_fields,
)

# --- config ---
_config_fields = {
    "configName": std_field("参数名称", "Config Name", required=True, maxLength=100),
    "configKey": std_field("参数键名", "Config Key", required=True, maxLength=100),
    "configValue": std_field("参数键值", "Config Value", required=True, maxLength=500),
    "configType": std_field("系统内置", "Built-in"),
    "remark": std_field("备注", "Remark", maxLength=500),
}
module(
    business_name="config", api_module="system", class_name="Config",
    function_zh="参数配置", function_en="Parameter", permission_prefix="system:config",
    vue_path=FE_VIEWS / "system/config/index.vue",
    vue_variant="config",
    view={
        "meta": {"businessName": "config", "apiModule": "system", "backendModule": "system",
                 "permissionPrefix": "system:config", "className": "Config"},
        "page": {"layout": "standard-crud"},
        "query": {"fields": [
            {"field": "configName", "component": "input", "queryType": "LIKE", "width": 200},
            {"field": "configKey", "component": "input", "queryType": "LIKE", "width": 200},
            {"field": "configType", "component": "select", "queryType": "EQ", "dictType": "sys_config_type", "width": 140},
        ]},
        "table": {"fields": [
            {"field": "configName", "component": "text", "minWidth": 180, "showOverflowTooltip": True},
            {"field": "configKey", "component": "text", "minWidth": 220, "showOverflowTooltip": True},
            {"field": "configValue", "component": "text", "minWidth": 160, "showOverflowTooltip": True},
            {"field": "configType", "component": "dict-tag", "dictType": "sys_config_type", "width": 100, "align": "center"},
            {"field": "remark", "component": "text", "minWidth": 160, "showOverflowTooltip": True},
            {"field": "createdAt", "component": "text", "width": 170, "align": "center"},
        ], "actions": {"edit": True, "delete": True, "width": 160}},
        "form": {"mode": "dialog", "dialogWidth": 640, "labelWidth": 120, "fields": [
            {"field": "configName", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "configKey", "component": "input", "insert": True, "edit": True, "span": 24, "disabledOnEdit": True},
            {"field": "configValue", "component": "textarea", "insert": True, "edit": True, "span": 24, "rows": 3},
            {"field": "configType", "component": "radio", "insert": True, "edit": True, "span": 24, "dictType": "sys_config_type"},
            {"field": "remark", "component": "textarea", "insert": True, "edit": True, "span": 24, "rows": 2},
        ]},
    },
    fields=_config_fields,
)

# --- i18nLocale ---
_i18n_locale_fields = {
    "localeCode": std_field("语言编码", "Locale Code", required=True, maxLength=20),
    "localeName": std_field("语言名称", "Locale Name", required=True, maxLength=50),
    "nativeName": std_field("本地名称", "Native Name", maxLength=50),
    "sortNo": std_field("排序", "Sort"),
    "isDefault": std_field("默认语言", "Default"),
    "status": std_field("状态", "Status"),
    "remark": std_field("备注", "Remark", maxLength=500),
}
module(
    business_name="i18nLocale", api_module="system", class_name="I18nLocale",
    function_zh="语言配置", function_en="Locale", permission_prefix="system:i18nLocale",
    vue_path=FE_VIEWS / "system/i18n/locale/index.vue",
    view={
        "meta": {"businessName": "i18nLocale", "apiModule": "system", "backendModule": "system",
                 "permissionPrefix": "system:i18nLocale", "className": "I18nLocale"},
        "page": {"layout": "standard-crud"},
        "query": {"fields": [
            {"field": "localeCode", "component": "input", "queryType": "LIKE", "width": 160},
            {"field": "localeName", "component": "input", "queryType": "LIKE", "width": 160},
            {"field": "status", "component": "select", "queryType": "EQ", "dictType": "sys_common_status", "width": 140},
        ]},
        "table": {"fields": [
            {"field": "localeCode", "component": "text", "width": 120},
            {"field": "localeName", "component": "text", "minWidth": 140, "showOverflowTooltip": True},
            {"field": "nativeName", "component": "text", "minWidth": 140, "showOverflowTooltip": True},
            {"field": "sortNo", "component": "text", "width": 80, "align": "center"},
            {"field": "isDefault", "component": "dict-tag", "dictType": "sys_yes_no", "width": 100, "align": "center"},
            {"field": "status", "component": "dict-tag", "dictType": "sys_common_status", "width": 90, "align": "center"},
            {"field": "remark", "component": "text", "minWidth": 160, "showOverflowTooltip": True},
        ], "actions": {"edit": True, "delete": True, "width": 160}},
        "form": {"mode": "dialog", "dialogWidth": 520, "labelWidth": 100, "fields": [
            {"field": "localeCode", "component": "input", "insert": True, "edit": True, "span": 24, "disabledOnEdit": True},
            {"field": "localeName", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "nativeName", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "sortNo", "component": "input-number", "insert": True, "edit": True, "span": 24, "min": 0},
            {"field": "isDefault", "component": "radio", "insert": True, "edit": True, "span": 24, "dictType": "sys_yes_no"},
            {"field": "status", "component": "radio", "insert": True, "edit": True, "span": 24, "dictType": "sys_common_status"},
            {"field": "remark", "component": "textarea", "insert": True, "edit": True, "span": 24, "rows": 2},
        ]},
    },
    fields=_i18n_locale_fields,
)

# --- i18nMessage ---
_i18n_msg_fields = {
    "messageKey": std_field("消息键", "Message Key", required=True, maxLength=200),
    "locale": std_field("语言", "Locale", required=True, maxLength=20),
    "messageValue": std_field("消息值", "Message Value", required=True),
    "module": std_field("模块", "Module", maxLength=50),
    "scope": std_field("范围", "Scope", maxLength=20),
    "remark": std_field("备注", "Remark", maxLength=500),
}
module(
    business_name="i18nMessage", api_module="system", class_name="I18nMessage",
    function_zh="国际化文案", function_en="Message", permission_prefix="system:i18nMessage",
    vue_path=FE_VIEWS / "system/i18n/message/index.vue",
    vue_variant="i18nMessage",
    view={
        "meta": {"businessName": "i18nMessage", "apiModule": "system", "backendModule": "system",
                 "permissionPrefix": "system:i18nMessage", "className": "I18nMessage"},
        "page": {"layout": "standard-crud"},
        "query": {"fields": [
            {"field": "messageKey", "component": "input", "queryType": "LIKE", "width": 200},
            {"field": "locale", "component": "input", "queryType": "EQ", "width": 140},
            {"field": "module", "component": "input", "queryType": "LIKE", "width": 140},
        ]},
        "table": {"fields": [
            {"field": "messageKey", "component": "text", "minWidth": 220, "showOverflowTooltip": True},
            {"field": "locale", "component": "text", "width": 100},
            {"field": "messageValue", "component": "text", "minWidth": 200, "showOverflowTooltip": True},
            {"field": "module", "component": "text", "width": 120},
            {"field": "scope", "component": "text", "width": 100},
            {"field": "remark", "component": "text", "minWidth": 160, "showOverflowTooltip": True},
        ], "actions": {"edit": True, "delete": True, "width": 160}},
        "form": {"mode": "dialog", "dialogWidth": 640, "labelWidth": 100, "fields": [
            {"field": "messageKey", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "locale", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "messageValue", "component": "textarea", "insert": True, "edit": True, "span": 24, "rows": 4},
            {"field": "module", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "scope", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "remark", "component": "textarea", "insert": True, "edit": True, "span": 24, "rows": 2},
        ]},
    },
    fields=_i18n_msg_fields,
)

# --- tenant ---
_tenant_fields = {
    "tenantCode": std_field("租户编码", "Tenant Code", required=True, maxLength=50),
    "tenantName": std_field("租户名称", "Tenant Name", required=True, maxLength=100),
    "contactName": std_field("联系人", "Contact", maxLength=50),
    "contactPhone": std_field("联系电话", "Phone", maxLength=20),
    "status": std_field("状态", "Status"),
    "remark": std_field("备注", "Remark", maxLength=500),
}
module(
    business_name="tenant", api_module="tenant", class_name="Tenant",
    function_zh="租户", function_en="Tenant", permission_prefix="tenant:tenant",
    vue_path=FE_VIEWS / "tenant/list/index.vue",
    vue_variant="tenant",
    view={
        "meta": {"businessName": "tenant", "apiModule": "tenant", "backendModule": "system",
                 "permissionPrefix": "tenant:tenant", "className": "Tenant"},
        "page": {"layout": "standard-crud"},
        "query": {"fields": [
            {"field": "tenantCode", "component": "input", "queryType": "LIKE", "width": 200},
            {"field": "tenantName", "component": "input", "queryType": "LIKE", "width": 200},
            {"field": "status", "component": "select", "queryType": "EQ", "dictType": "sys_common_status", "width": 140},
        ]},
        "table": {"fields": [
            {"field": "tenantCode", "component": "text", "minWidth": 140, "showOverflowTooltip": True},
            {"field": "tenantName", "component": "text", "minWidth": 160, "showOverflowTooltip": True},
            {"field": "contactName", "component": "text", "minWidth": 120, "showOverflowTooltip": True},
            {"field": "contactPhone", "component": "text", "width": 140},
            {"field": "status", "component": "dict-tag", "dictType": "sys_common_status", "width": 100, "align": "center"},
            {"field": "remark", "component": "text", "minWidth": 160, "showOverflowTooltip": True},
        ], "actions": {"edit": True, "delete": False, "width": 180}},
        "form": {"mode": "dialog", "dialogWidth": 560, "labelWidth": 100, "fields": [
            {"field": "tenantCode", "component": "input", "insert": True, "edit": True, "span": 24, "disabledOnEdit": True},
            {"field": "tenantName", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "contactName", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "contactPhone", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "remark", "component": "textarea", "insert": True, "edit": True, "span": 24, "rows": 2},
        ]},
    },
    fields=_tenant_fields,
)

# --- loginLog (read-only) ---
_login_fields = {
    "username": std_field("用户名", "Username"),
    "loginStatus": std_field("登录状态", "Login Status"),
    "ip": std_field("IP地址", "IP"),
    "message": std_field("消息", "Message"),
    "loginAt": std_field("登录时间", "Login Time"),
}
module(
    business_name="loginLog", api_module="system", class_name="LoginLog",
    function_zh="登录日志", function_en="Login Log", permission_prefix="monitor:loginLog",
    vue_path=FE_VIEWS / "system/loginLog/index.vue",
    vue_variant="auditLog",
    view={
        "meta": {"businessName": "loginLog", "apiModule": "system", "permissionPrefix": "monitor:loginLog", "className": "LoginLog"},
        "readOnly": True,
        "page": {"layout": "standard-crud"},
        "query": {"fields": [
            {"field": "username", "component": "input", "queryType": "LIKE", "width": 200},
            {"field": "loginStatus", "component": "select", "queryType": "EQ", "dictType": "sys_login_status", "width": 140},
        ]},
        "table": {"fields": [
            {"field": "username", "component": "text", "minWidth": 120, "showOverflowTooltip": True},
            {"field": "loginStatus", "component": "dict-tag", "dictType": "sys_login_status", "width": 100, "align": "center"},
            {"field": "ip", "component": "text", "width": 140, "showOverflowTooltip": True},
            {"field": "message", "component": "text", "minWidth": 200, "showOverflowTooltip": True},
            {"field": "loginAt", "component": "text", "width": 170, "align": "center"},
        ], "actions": {"edit": False, "delete": False}},
    },
    fields=_login_fields,
)

# --- operationLog ---
_op_fields = {
    "moduleName": std_field("模块", "Module"),
    "operatorName": std_field("操作人", "Operator"),
    "operationUrl": std_field("请求URL", "URL"),
    "operationIp": std_field("IP", "IP"),
    "operationStatus": std_field("状态", "Status"),
    "operationAt": std_field("操作时间", "Time"),
}
module(
    business_name="operationLog", api_module="system", class_name="OperationLog",
    function_zh="操作日志", function_en="Operation Log", permission_prefix="monitor:operationLog",
    vue_path=FE_VIEWS / "system/operationLog/index.vue",
    vue_variant="auditLog",
    view={
        "meta": {"businessName": "operationLog", "apiModule": "system", "permissionPrefix": "monitor:operationLog", "className": "OperationLog"},
        "readOnly": True,
        "page": {"layout": "standard-crud"},
        "query": {"fields": [
            {"field": "moduleName", "component": "input", "queryType": "LIKE", "width": 200},
            {"field": "operationStatus", "component": "select", "queryType": "EQ", "dictType": "sys_login_status", "width": 140},
        ]},
        "table": {"fields": [
            {"field": "moduleName", "component": "text", "minWidth": 120, "showOverflowTooltip": True},
            {"field": "operatorName", "component": "text", "width": 120, "showOverflowTooltip": True},
            {"field": "operationUrl", "component": "text", "minWidth": 220, "showOverflowTooltip": True},
            {"field": "operationIp", "component": "text", "width": 140},
            {"field": "operationStatus", "component": "dict-tag", "dictType": "sys_login_status", "width": 100, "align": "center"},
            {"field": "operationAt", "component": "text", "width": 170, "align": "center"},
        ], "actions": {"edit": False, "delete": False}},
    },
    fields=_op_fields,
)

# --- apiAccessLog ---
_api_fields = {
    "requestUri": std_field("请求URI", "Request URI"),
    "requestMethod": std_field("方法", "Method"),
    "username": std_field("用户", "User"),
    "clientIp": std_field("客户端IP", "Client IP"),
    "responseCode": std_field("响应码", "Code"),
    "costTime": std_field("耗时(ms)", "Cost"),
    "accessedAt": std_field("访问时间", "Accessed At"),
}
module(
    business_name="apiAccessLog", api_module="infra", class_name="ApiAccessLog",
    function_zh="接口日志", function_en="API Log", permission_prefix="monitor:apiLog",
    vue_path=FE_VIEWS / "infra/apiAccessLog/index.vue",
    vue_variant="auditLog",
    view={
        "meta": {"businessName": "apiAccessLog", "apiModule": "infra", "permissionPrefix": "monitor:apiLog", "className": "ApiAccessLog"},
        "readOnly": True,
        "page": {"layout": "standard-crud"},
        "query": {"fields": [
            {"field": "requestUri", "component": "input", "queryType": "LIKE", "width": 320},
        ]},
        "table": {"fields": [
            {"field": "requestUri", "component": "text", "minWidth": 240, "showOverflowTooltip": True},
            {"field": "requestMethod", "component": "text", "width": 100, "align": "center"},
            {"field": "username", "component": "text", "width": 120, "showOverflowTooltip": True},
            {"field": "clientIp", "component": "text", "width": 140},
            {"field": "responseCode", "component": "text", "width": 100, "align": "center"},
            {"field": "costTime", "component": "text", "width": 100, "align": "center"},
            {"field": "accessedAt", "component": "text", "width": 170, "align": "center"},
        ], "actions": {"edit": False, "delete": False}},
    },
    fields=_api_fields,
)

# --- menu (tree-crud) ---
_menu_fields = {
    "menuName": std_field("菜单名称", "Menu Name", required=True, maxLength=50),
    "menuNameEn": std_field("英文名称", "English Name", maxLength=100,
                             placeholderZh="路由/菜单英文标题", placeholderEn="English menu title"),
    "menuType": std_field("菜单类型", "Menu Type", required=True),
    "parentId": std_field("上级菜单", "Parent Menu"),
    "routePath": std_field("路由路径", "Route Path", maxLength=200),
    "routeName": std_field("路由名称", "Route Name", maxLength=100),
    "componentPath": std_field("组件路径", "Component Path", maxLength=200),
    "permissionCode": std_field("权限标识", "Permission Code", maxLength=100),
    "icon": std_field("图标", "Icon", maxLength=100),
    "sortNo": std_field("排序", "Sort"),
    "status": std_field("状态", "Status"),
    "remark": std_field("备注", "Remark", maxLength=500),
}
module(
    business_name="menu", api_module="system", class_name="Menu",
    function_zh="菜单", function_en="Menu", permission_prefix="system:menu",
    vue_path=FE_VIEWS / "system/menu/index.vue",
    vue_variant="treeMenu",
    view={
        "meta": {"businessName": "menu", "apiModule": "system", "backendModule": "system",
                 "permissionPrefix": "system:menu", "className": "Menu"},
        "page": {"layout": "tree-crud"},
        "query": {"fields": [
            {"field": "menuName", "component": "input", "queryType": "LIKE", "width": 160},
            {"field": "menuType", "component": "select", "queryType": "EQ", "dictType": "sys_menu_type", "width": 120},
            {"field": "status", "component": "select", "queryType": "EQ", "dictType": "sys_common_status", "width": 120},
        ]},
        "table": {"fields": [
            {"field": "menuName", "component": "text", "minWidth": 180, "showOverflowTooltip": True},
            {"field": "menuType", "component": "dict-tag", "dictType": "sys_menu_type", "width": 90, "align": "center"},
            {"field": "routePath", "component": "text", "minWidth": 160, "showOverflowTooltip": True},
            {"field": "componentPath", "component": "text", "minWidth": 180, "showOverflowTooltip": True},
            {"field": "permissionCode", "component": "text", "minWidth": 160, "showOverflowTooltip": True},
            {"field": "icon", "component": "text", "width": 100, "align": "center"},
            {"field": "sortNo", "component": "text", "width": 80, "align": "center"},
            {"field": "status", "component": "dict-tag", "dictType": "sys_common_status", "width": 90, "align": "center"},
        ]},
        "tree": {
            "parentField": "parentId",
            "rootValue": "0",
            "rowKey": "id",
            "labelField": "menuName",
            "pageSize": 10000,
            "actions": {
                "edit": True, "delete": True, "addChild": True,
                "addChildExclude": {"field": "menuType", "values": ["F"]},
                "expandCollapse": True, "width": 220,
            },
        },
        "form": {"mode": "dialog", "dialogWidth": 620, "labelWidth": 110, "fields": [
            {"field": "parentId", "component": "tree-select", "insert": True, "edit": True, "span": 24,
             "treeSource": "self", "treeLabelField": "menuName", "treeValueField": "id"},
            {"field": "menuType", "component": "radio", "insert": True, "edit": True, "span": 24, "dictType": "sys_menu_type"},
            {"field": "menuName", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "menuNameEn", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "routePath", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "componentPath", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "permissionCode", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "icon", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "sortNo", "component": "input-number", "insert": True, "edit": True, "span": 24, "min": 0},
            {"field": "status", "component": "radio", "insert": True, "edit": True, "span": 24, "dictType": "sys_common_status"},
            {"field": "remark", "component": "textarea", "insert": True, "edit": True, "span": 24, "rows": 2},
        ]},
    },
    fields=_menu_fields,
)

# --- dept (tree-crud) ---
_dept_fields = {
    "deptName": std_field("部门名称", "Dept Name", required=True, maxLength=50),
    "deptCode": std_field("部门编码", "Dept Code", maxLength=64),
    "parentId": std_field("上级部门", "Parent Dept"),
    "sortNo": std_field("排序", "Sort"),
    "status": std_field("状态", "Status"),
    "remark": std_field("备注", "Remark", maxLength=500),
}
module(
    business_name="dept", api_module="system", class_name="Dept",
    function_zh="部门", function_en="Department", permission_prefix="system:dept",
    vue_path=FE_VIEWS / "system/dept/index.vue",
    vue_variant="tree",
    view={
        "meta": {"businessName": "dept", "apiModule": "system", "backendModule": "system",
                 "permissionPrefix": "system:dept", "className": "Dept"},
        "page": {"layout": "tree-crud"},
        "query": {"fields": [
            {"field": "deptName", "component": "input", "queryType": "LIKE", "width": 160},
            {"field": "deptCode", "component": "input", "queryType": "LIKE", "width": 160},
            {"field": "status", "component": "select", "queryType": "EQ", "dictType": "sys_common_status", "width": 120},
        ]},
        "table": {"fields": [
            {"field": "deptName", "component": "text", "minWidth": 180, "showOverflowTooltip": True},
            {"field": "deptCode", "component": "text", "minWidth": 140, "showOverflowTooltip": True},
            {"field": "sortNo", "component": "text", "width": 80, "align": "center"},
            {"field": "status", "component": "dict-tag", "dictType": "sys_common_status", "width": 90, "align": "center"},
            {"field": "remark", "component": "text", "minWidth": 140, "showOverflowTooltip": True},
        ]},
        "tree": {
            "parentField": "parentId",
            "rootValue": "0",
            "rowKey": "id",
            "labelField": "deptName",
            "pageSize": 10000,
            "defaultExpandAll": True,
            "actions": {"edit": True, "delete": True, "width": 160},
        },
        "form": {"mode": "dialog", "dialogWidth": 560, "labelWidth": 100, "fields": [
            {"field": "parentId", "component": "tree-select", "insert": True, "edit": True, "span": 24,
             "treeSource": "self", "treeLabelField": "deptName", "treeValueField": "id"},
            {"field": "deptName", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "deptCode", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "sortNo", "component": "input-number", "insert": True, "edit": True, "span": 24, "min": 0},
            {"field": "status", "component": "radio", "insert": True, "edit": True, "span": 24, "dictType": "sys_common_status"},
            {"field": "remark", "component": "textarea", "insert": True, "edit": True, "span": 24, "rows": 2},
        ]},
    },
    fields=_dept_fields,
)

# --- dict (master-detail) ---
_dict_fields = {
    "dictName": std_field("字典名称", "Dict Name", required=True, maxLength=100),
    "dictType": std_field("字典类型", "Dict Type", required=True, maxLength=100),
    "dictLabel": std_field("字典标签", "Dict Label", required=True, maxLength=100),
    "dictValue": std_field("字典键值", "Dict Value", required=True, maxLength=100),
    "sortNo": std_field("排序", "Sort"),
    "status": std_field("状态", "Status"),
    "remark": std_field("备注", "Remark", maxLength=500),
}
module(
    business_name="dict", api_module="system", class_name="Dict",
    function_zh="字典", function_en="Dictionary", permission_prefix="system:dict",
    vue_path=FE_VIEWS / "system/dict/index.vue",
    vue_variant="masterDetail",
    view={
        "meta": {"businessName": "dict", "apiModule": "system", "backendModule": "system",
                 "permissionPrefix": "system:dict", "className": "Dict"},
        "page": {"layout": "master-detail"},
        "masterDetail": {
            "masterSpan": 10,
            "detailSpan": 14,
            "detailForeignKey": "dictTypeId",
            "master": {
                "query": {"fields": [
                    {"field": "dictName", "component": "input", "queryType": "LIKE", "width": 120},
                ]},
                "table": {"fields": [
                    {"field": "dictName", "component": "text", "minWidth": 120, "showOverflowTooltip": True},
                    {"field": "dictType", "component": "text", "minWidth": 120, "showOverflowTooltip": True},
                    {"field": "status", "component": "dict-tag", "dictType": "sys_common_status", "width": 80, "align": "center"},
                ], "actions": {"edit": True, "delete": True, "width": 120}},
                "form": {"mode": "dialog", "dialogWidth": 520, "labelWidth": 100, "fields": [
                    {"field": "dictName", "component": "input", "insert": True, "edit": True, "span": 24},
                    {"field": "dictType", "component": "input", "insert": True, "edit": True, "span": 24, "disabledOnEdit": True},
                    {"field": "status", "component": "radio", "insert": True, "edit": True, "span": 24, "dictType": "sys_common_status"},
                    {"field": "remark", "component": "textarea", "insert": True, "edit": True, "span": 24, "rows": 2},
                ]},
            },
            "detail": {
                "table": {"fields": [
                    {"field": "dictLabel", "component": "text", "minWidth": 120, "showOverflowTooltip": True},
                    {"field": "dictValue", "component": "text", "minWidth": 120, "showOverflowTooltip": True},
                    {"field": "sortNo", "component": "text", "width": 80, "align": "center"},
                    {"field": "status", "component": "dict-tag", "dictType": "sys_common_status", "width": 80, "align": "center"},
                    {"field": "remark", "component": "text", "minWidth": 120, "showOverflowTooltip": True},
                ], "actions": {"edit": True, "delete": True, "width": 120}},
                "form": {"mode": "dialog", "dialogWidth": 520, "labelWidth": 100, "fields": [
                    {"field": "dictLabel", "component": "input", "insert": True, "edit": True, "span": 24},
                    {"field": "dictValue", "component": "input", "insert": True, "edit": True, "span": 24},
                    {"field": "sortNo", "component": "input-number", "insert": True, "edit": True, "span": 24, "min": 0},
                    {"field": "status", "component": "radio", "insert": True, "edit": True, "span": 24, "dictType": "sys_common_status"},
                    {"field": "remark", "component": "textarea", "insert": True, "edit": True, "span": 24, "rows": 2},
                ]},
            },
        },
    },
    fields=_dict_fields,
)

# --- user (standard-crud + dept tree) ---
_user_fields = {
    "username": std_field("用户名", "Username", required=True, maxLength=50),
    "nickname": std_field("昵称", "Nickname", required=True, maxLength=50),
    "passwordHash": std_field("密码", "Password", required=True, maxLength=100),
    "phone": std_field("手机号", "Phone", maxLength=20),
    "email": std_field("邮箱", "Email", maxLength=100),
    "deptId": std_field("部门", "Department"),
    "status": std_field("状态", "Status"),
    "remark": std_field("备注", "Remark", maxLength=500),
}
module(
    business_name="user", api_module="system", class_name="User",
    function_zh="用户", function_en="User", permission_prefix="system:user",
    vue_path=FE_VIEWS / "system/user/index.vue",
    vue_variant="userExtended",
    view={
        "meta": {"businessName": "user", "apiModule": "system", "backendModule": "system",
                 "permissionPrefix": "system:user", "className": "User"},
        "page": {"layout": "standard-crud"},
        "query": {"fields": [
            {"field": "username", "component": "input", "queryType": "LIKE", "width": 160},
            {"field": "nickname", "component": "input", "queryType": "LIKE", "width": 160},
            {"field": "phone", "component": "input", "queryType": "LIKE", "width": 160},
            {"field": "status", "component": "select", "queryType": "EQ", "dictType": "sys_common_status", "width": 120},
        ]},
        "table": {"fields": [
            {"field": "username", "component": "text", "minWidth": 120, "showOverflowTooltip": True},
            {"field": "nickname", "component": "text", "minWidth": 120, "showOverflowTooltip": True},
            {"field": "phone", "component": "text", "width": 130, "align": "center"},
            {"field": "email", "component": "text", "minWidth": 160, "showOverflowTooltip": True},
            {"field": "deptId", "component": "text", "width": 120, "showOverflowTooltip": True},
            {"field": "status", "component": "dict-tag", "dictType": "sys_common_status", "width": 90, "align": "center"},
            {"field": "remark", "component": "text", "minWidth": 140, "showOverflowTooltip": True},
            {"field": "createdAt", "component": "text", "width": 170, "align": "center"},
        ], "actions": {"edit": True, "delete": True, "width": 240}},
        "form": {"mode": "dialog", "dialogWidth": 560, "labelWidth": 100, "fields": [
            {"field": "username", "component": "input", "insert": True, "edit": True, "span": 24, "disabledOnEdit": True},
            {"field": "nickname", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "passwordHash", "component": "password", "insert": True, "insertOnly": True, "span": 24},
            {"field": "phone", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "email", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "deptId", "component": "tree-select", "insert": True, "edit": True, "span": 24,
             "treeSource": "dept", "treeLabelField": "deptName", "treeValueField": "id"},
            {"field": "status", "component": "radio", "insert": True, "edit": True, "span": 24, "dictType": "sys_common_status"},
            {"field": "remark", "component": "textarea", "insert": True, "edit": True, "span": 24, "rows": 2},
        ]},
    },
    fields=_user_fields,
)

# --- role (standard-crud + menu auth) ---
_role_fields = {
    "roleName": std_field("角色名称", "Role Name", required=True, maxLength=50),
    "roleCode": std_field("角色编码", "Role Code", required=True, maxLength=64),
    "dataScope": std_field("数据范围", "Data Scope"),
    "sortNo": std_field("排序", "Sort"),
    "status": std_field("状态", "Status"),
    "remark": std_field("备注", "Remark", maxLength=500),
}
module(
    business_name="role", api_module="system", class_name="Role",
    function_zh="角色", function_en="Role", permission_prefix="system:role",
    vue_path=FE_VIEWS / "system/role/index.vue",
    vue_variant="roleExtended",
    view={
        "meta": {"businessName": "role", "apiModule": "system", "backendModule": "system",
                 "permissionPrefix": "system:role", "className": "Role"},
        "page": {"layout": "standard-crud"},
        "query": {"fields": [
            {"field": "roleName", "component": "input", "queryType": "LIKE", "width": 160},
            {"field": "roleCode", "component": "input", "queryType": "LIKE", "width": 160},
            {"field": "status", "component": "select", "queryType": "EQ", "dictType": "sys_common_status", "width": 120},
        ]},
        "table": {"fields": [
            {"field": "roleName", "component": "text", "minWidth": 140, "showOverflowTooltip": True},
            {"field": "roleCode", "component": "text", "minWidth": 140, "showOverflowTooltip": True},
            {"field": "dataScope", "component": "dict-tag", "dictType": "sys_data_scope", "width": 140, "align": "center"},
            {"field": "sortNo", "component": "text", "width": 80, "align": "center"},
            {"field": "status", "component": "dict-tag", "dictType": "sys_common_status", "width": 90, "align": "center"},
            {"field": "remark", "component": "text", "minWidth": 140, "showOverflowTooltip": True},
            {"field": "createdAt", "component": "text", "width": 170, "align": "center"},
        ], "actions": {"edit": True, "delete": True, "width": 240}},
        "form": {"mode": "dialog", "dialogWidth": 560, "labelWidth": 100, "fields": [
            {"field": "roleName", "component": "input", "insert": True, "edit": True, "span": 24},
            {"field": "roleCode", "component": "input", "insert": True, "edit": True, "span": 24, "disabledOnEdit": True},
            {"field": "dataScope", "component": "select", "insert": True, "edit": True, "span": 24, "dictType": "sys_data_scope"},
            {"field": "sortNo", "component": "input-number", "insert": True, "edit": True, "span": 24, "min": 0},
            {"field": "status", "component": "radio", "insert": True, "edit": True, "span": 24, "dictType": "sys_common_status"},
            {"field": "remark", "component": "textarea", "insert": True, "edit": True, "span": 24, "rows": 2},
        ]},
    },
    fields=_role_fields,
)


VUE_VARIANTS = {
    "config": '''    <template #toolbar-extra>
      <el-button type="warning" plain :icon="RefreshRight" v-hasPermi="[PERM.update]" @click="handleRefreshCache">
        {{ gp('refreshCache') }}
      </el-button>
    </template>
    <template #row-actions="{ row }">
      <el-button link type="primary" v-hasPermi="[PERM.update]" @click="openEdit(row)">{{ t('common.edit') }}</el-button>
      <el-button link type="danger" :disabled="row.configType === 'Y'" v-hasPermi="[PERM.delete]" @click="handleDeleteRow(row)">
        {{ t('common.delete') }}
      </el-button>
    </template>''',
    "i18nMessage": '''    <template #toolbar-extra>
      <el-button type="warning" plain :icon="RefreshRight" v-hasPermi="[PERM.update]" @click="handleRefreshCache">
        {{ gp('refreshCache') }}
      </el-button>
    </template>''',
    "tenant": '''    <template #row-actions="{ row }">
      <el-button link type="primary" v-hasPermi="[PERM.update]" @click="openEdit(row)">{{ t('common.edit') }}</el-button>
      <el-button v-if="row.status === '0'" link type="danger" v-hasPermi="[PERM.disable]" @click="handleDisable(row)">
        {{ gp('disable') }}
      </el-button>
      <el-button v-else link type="success" v-hasPermi="[PERM.update]" @click="handleEnable(row)">
        {{ gp('enable') }}
      </el-button>
    </template>''',
    "auditLog": '''    <template #toolbar-extra>
      <el-button type="danger" plain :icon="Delete" v-hasPermi="[PERM.clear]" @click="handleClear">
        {{ gp('clear') }}
      </el-button>
    </template>''',
    "userExtended": '''    <template #row-actions="{ row }">
      <el-button link type="primary" v-hasPermi="[PERM.update]" @click="openEdit(row)">{{ t('common.edit') }}</el-button>
      <el-button link type="primary" v-hasPermi="[PERM.update]" @click="openAssignRoles(row)">{{ gp('assignRoles') }}</el-button>
      <el-button link type="danger" v-hasPermi="[PERM.delete]" @click="handleDelete(row)">{{ t('common.delete') }}</el-button>
    </template>''',
    "roleExtended": '''    <template #row-actions="{ row }">
      <el-button link type="primary" v-hasPermi="[PERM.update]" @click="openEdit(row)">{{ t('common.edit') }}</el-button>
      <el-button link type="primary" v-hasPermi="[PERM.update]" @click="openAssignMenu(row)">{{ gp('assignMenu') }}</el-button>
      <el-button link type="danger" v-hasPermi="[PERM.delete]" @click="handleDelete(row)">{{ t('common.delete') }}</el-button>
    </template>''',
}

SCRIPT_VARIANTS = {
    "config": '''
import { RefreshRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { refreshConfigCache } from '@/api/system/config'
import { ref as vueRef } from 'vue'

const { t } = useI18n()
const pageRef = vueRef<InstanceType<typeof GenCrudPage>>()

async function handleRefreshCache() {
  await refreshConfigCache()
  ElMessage.success(gp('refreshCacheSuccess'))
}

function openEdit(row: Record<string, unknown>) {
  ;(pageRef.value as unknown as { openForm: (r?: Record<string, unknown>) => void })?.openForm?.(row)
}

async function handleDeleteRow(row: Record<string, unknown>) {
  if (!row.id) return
  await deleteConfig(String(row.id))
  ElMessage.success(t('common.success'))
}
''',
}


def render_vue(m: dict) -> str:
    variant = m.get("vue_variant")
    slots = VUE_VARIANTS.get(variant, "")
    extra_script = SCRIPT_VARIANTS.get(variant, "")

    if variant == "config":
        return f"""<!-- AUTO-GENERATED {m['function_zh']} — GenCrudPage + view.json -->
<template>
  <GenCrudPage
    ref="pageRef"
    :file-view="viewConfig"
    business-name="{m['business_name']}"
    api-module="{m['api_module']}"
    :i18n-files="GEN_I18N_FILES"
    :perm="PERM"
    :api="crudApi"
  >
{slots}
  </GenCrudPage>
</template>

<script setup lang="ts">
import {{ ref }} from 'vue'
import {{ RefreshRight }} from '@element-plus/icons-vue'
import {{ ElMessage }} from 'element-plus'
import {{ useI18n }} from 'vue-i18n'
import GenCrudPage from '@/components/gen/GenCrudPage.vue'
import type {{ GenCrudApi }} from '@/components/gen/GenCrudPage.vue'
import type {{ GenI18nMessages }} from '@/types/tool/genI18n'
import type {{ GenViewSchema }} from '@/types/tool/genView'
import viewConfigData from '@/i18n/gen/{m['business_name']}/view.json'
import genI18nZhCN from '@/i18n/gen/{m['business_name']}/zh-CN.json'
import genI18nEnUS from '@/i18n/gen/{m['business_name']}/en-US.json'
import {{
  PERM_{m['class_name']} as PERM,
  create{m['class_name']},
  delete{m['class_name']},
  get{m['class_name']},
  page{m['class_name']},
  update{m['class_name']},
  refreshConfigCache,
}} from '@/api/{m['api_module']}/{m['business_name']}'
import {{ useGenI18n }} from '@/composables/useGenI18n'

const {{ t }} = useI18n()
const pageRef = ref<InstanceType<typeof GenCrudPage>>()
const viewConfig = viewConfigData as GenViewSchema
const GEN_I18N_FILES: Record<string, GenI18nMessages> = {{
  'zh-CN': genI18nZhCN as GenI18nMessages,
  'en-US': genI18nEnUS as GenI18nMessages,
}}
const {{ gp }} = useGenI18n('{m['business_name']}', '{m['api_module']}', GEN_I18N_FILES)

const crudApi: GenCrudApi = {{
  page: page{m['class_name']},
  get: get{m['class_name']},
  create: create{m['class_name']},
  update: update{m['class_name']},
  delete: delete{m['class_name']},
}}

async function handleRefreshCache() {{
  await refreshConfigCache()
  ElMessage.success(gp('refreshCacheSuccess'))
}}

function openEdit(row: Record<string, unknown>) {{
  const inst = pageRef.value as unknown as {{ openForm?: (r?: Record<string, unknown>) => void }}
  inst?.openForm?.(row)
}}

async function handleDeleteRow(row: Record<string, unknown>) {{
  if (!row.id || row.configType === 'Y') return
  await delete{m['class_name']}(String(row.id))
  ElMessage.success(t('common.success'))
  await pageRef.value?.loadPage()
}}
</script>
"""

    if variant == "i18nMessage":
        return f"""<!-- AUTO-GENERATED {m['function_zh']} — GenCrudPage + view.json -->
<template>
  <GenCrudPage
    :file-view="viewConfig"
    business-name="{m['business_name']}"
    api-module="{m['api_module']}"
    :i18n-files="GEN_I18N_FILES"
    :perm="PERM"
    :api="crudApi"
  >
{slots}
  </GenCrudPage>
</template>

<script setup lang="ts">
import {{ RefreshRight }} from '@element-plus/icons-vue'
import {{ ElMessage, ElMessageBox }} from 'element-plus'
import {{ useI18n }} from 'vue-i18n'
import GenCrudPage from '@/components/gen/GenCrudPage.vue'
import type {{ GenCrudApi }} from '@/components/gen/GenCrudPage.vue'
import type {{ GenI18nMessages }} from '@/types/tool/genI18n'
import type {{ GenViewSchema }} from '@/types/tool/genView'
import viewConfigData from '@/i18n/gen/{m['business_name']}/view.json'
import genI18nZhCN from '@/i18n/gen/{m['business_name']}/zh-CN.json'
import genI18nEnUS from '@/i18n/gen/{m['business_name']}/en-US.json'
import {{
  PERM_{m['class_name']} as PERM,
  create{m['class_name']},
  delete{m['class_name']},
  get{m['class_name']},
  page{m['class_name']},
  update{m['class_name']},
  refreshI18nCache,
}} from '@/api/{m['api_module']}/{m['business_name']}'
import {{ useGenI18n }} from '@/composables/useGenI18n'

const {{ t }} = useI18n()
const viewConfig = viewConfigData as GenViewSchema
const GEN_I18N_FILES: Record<string, GenI18nMessages> = {{
  'zh-CN': genI18nZhCN as GenI18nMessages,
  'en-US': genI18nEnUS as GenI18nMessages,
}}
const {{ gp }} = useGenI18n('{m['business_name']}', '{m['api_module']}', GEN_I18N_FILES)

const crudApi: GenCrudApi = {{
  page: page{m['class_name']},
  get: (id) => get{m['class_name']}(Number(id)),
  create: create{m['class_name']},
  update: update{m['class_name']},
  delete: (id) => delete{m['class_name']}(Number(id)),
}}

async function handleRefreshCache() {{
  await ElMessageBox.confirm(gp('refreshCacheConfirm'), t('common.tip'))
  await refreshI18nCache()
  ElMessage.success(gp('refreshCacheSuccess'))
}}
</script>
"""

    if variant == "tenant":
        return f"""<!-- AUTO-GENERATED {m['function_zh']} — GenCrudPage + view.json -->
<template>
  <GenCrudPage
    ref="pageRef"
    :file-view="viewConfig"
    business-name="{m['business_name']}"
    api-module="{m['api_module']}"
    :i18n-files="GEN_I18N_FILES"
    :perm="PERM"
    :api="crudApi"
  >
{slots}
  </GenCrudPage>
</template>

<script setup lang="ts">
import {{ ref }} from 'vue'
import {{ ElMessage, ElMessageBox }} from 'element-plus'
import {{ useI18n }} from 'vue-i18n'
import GenCrudPage from '@/components/gen/GenCrudPage.vue'
import type {{ GenCrudApi, GenCrudPerm }} from '@/components/gen/GenCrudPage.vue'
import type {{ GenI18nMessages }} from '@/types/tool/genI18n'
import type {{ GenViewSchema }} from '@/types/tool/genView'
import viewConfigData from '@/i18n/gen/{m['business_name']}/view.json'
import genI18nZhCN from '@/i18n/gen/{m['business_name']}/zh-CN.json'
import genI18nEnUS from '@/i18n/gen/{m['business_name']}/en-US.json'
import {{
  PERM_{m['class_name']},
  create{m['class_name']},
  disable{m['class_name']},
  get{m['class_name']},
  page{m['class_name']},
  update{m['class_name']},
}} from '@/api/{m['api_module']}/{m['business_name']}'
import {{ useGenI18n }} from '@/composables/useGenI18n'

const {{ t }} = useI18n()
const pageRef = ref<InstanceType<typeof GenCrudPage>>()
const viewConfig = viewConfigData as GenViewSchema
const GEN_I18N_FILES: Record<string, GenI18nMessages> = {{
  'zh-CN': genI18nZhCN as GenI18nMessages,
  'en-US': genI18nEnUS as GenI18nMessages,
}}
const {{ gp }} = useGenI18n('{m['business_name']}', '{m['api_module']}', GEN_I18N_FILES)

const PERM: GenCrudPerm & {{ disable: string }} = PERM_{m['class_name']}

const crudApi: GenCrudApi = {{
  page: page{m['class_name']},
  get: get{m['class_name']},
  create: create{m['class_name']},
  update: update{m['class_name']},
  delete: async () => {{}},
}}

function openEdit(row: Record<string, unknown>) {{
  const inst = pageRef.value as unknown as {{ openForm?: (r?: Record<string, unknown>) => void }}
  inst?.openForm?.(row)
}}

async function handleDisable(row: Record<string, unknown>) {{
  if (!row.id) return
  await ElMessageBox.confirm(gp('disableConfirm'), t('common.tip'))
  await disable{m['class_name']}(String(row.id))
  ElMessage.success(t('common.success'))
  await pageRef.value?.loadPage()
}}

async function handleEnable(row: Record<string, unknown>) {{
  if (!row.id) return
  await update{m['class_name']}({{ id: String(row.id), status: '0' }} as never)
  ElMessage.success(t('common.success'))
  await pageRef.value?.loadPage()
}}
</script>
"""

    if variant == "auditLog":
        clear_fn = {
            "loginLog": "clearLoginLog",
            "operationLog": "clearOperationLog",
            "apiAccessLog": "clearApiAccessLog",
        }[m["business_name"]]
        perm_prefix = m["view"]["meta"]["permissionPrefix"]
        return f"""<!-- AUTO-GENERATED {m['function_zh']} — GenCrudPage + view.json -->
<template>
  <GenCrudPage
    :file-view="viewConfig"
    business-name="{m['business_name']}"
    api-module="{m['api_module']}"
    :i18n-files="GEN_I18N_FILES"
    :perm="PERM"
    :api="crudApi"
    read-only
  >
{slots}
  </GenCrudPage>
</template>

<script setup lang="ts">
import {{ Delete }} from '@element-plus/icons-vue'
import {{ ElMessage, ElMessageBox }} from 'element-plus'
import {{ useI18n }} from 'vue-i18n'
import GenCrudPage from '@/components/gen/GenCrudPage.vue'
import type {{ GenCrudApi, GenCrudPerm }} from '@/components/gen/GenCrudPage.vue'
import type {{ GenI18nMessages }} from '@/types/tool/genI18n'
import type {{ GenViewSchema }} from '@/types/tool/genView'
import viewConfigData from '@/i18n/gen/{m['business_name']}/view.json'
import genI18nZhCN from '@/i18n/gen/{m['business_name']}/zh-CN.json'
import genI18nEnUS from '@/i18n/gen/{m['business_name']}/en-US.json'
import {{ page{m['class_name']}, {clear_fn} }} from '@/api/{m['api_module']}/{m['business_name']}'
import {{ useGenI18n }} from '@/composables/useGenI18n'

const {{ t }} = useI18n()
const viewConfig = viewConfigData as GenViewSchema
const GEN_I18N_FILES: Record<string, GenI18nMessages> = {{
  'zh-CN': genI18nZhCN as GenI18nMessages,
  'en-US': genI18nEnUS as GenI18nMessages,
}}
const {{ gp }} = useGenI18n('{m['business_name']}', '{m['api_module']}', GEN_I18N_FILES)

const PERM: GenCrudPerm & {{ clear: string }} = {{
  add: '{perm_prefix}:list',
  update: '{perm_prefix}:list',
  delete: '{perm_prefix}:list',
  clear: '{perm_prefix}:clear',
}}

const crudApi: GenCrudApi = {{
  page: page{m['class_name']},
  get: async () => ({{}}),
  create: async () => ({{}}),
  update: async () => ({{}}),
  delete: async () => ({{}}),
}}

async function handleClear() {{
  await ElMessageBox.confirm(gp('clearConfirm'), t('common.tip'))
  await {clear_fn}()
  ElMessage.success(t('common.success'))
}}
</script>
"""

    if variant == "tree":
        return f"""<!-- AUTO-GENERATED {m['function_zh']} — GenTreeCrudPage + view.json -->
<template>
  <GenTreeCrudPage
    :file-view="viewConfig"
    business-name="{m['business_name']}"
    api-module="{m['api_module']}"
    :i18n-files="GEN_I18N_FILES"
    :perm="PERM"
    :api="crudApi"
    :create-defaults="createDefaults"
  />
</template>

<script setup lang="ts">
import GenTreeCrudPage from '@/components/gen/GenTreeCrudPage.vue'
import type {{ GenCrudApi }} from '@/components/gen/GenCrudPage.vue'
import type {{ GenI18nMessages }} from '@/types/tool/genI18n'
import type {{ GenViewSchema }} from '@/types/tool/genView'
import viewConfigData from '@/i18n/gen/{m['business_name']}/view.json'
import genI18nZhCN from '@/i18n/gen/{m['business_name']}/zh-CN.json'
import genI18nEnUS from '@/i18n/gen/{m['business_name']}/en-US.json'
import {{
  PERM_{m['class_name']} as PERM,
  create{m['class_name']},
  delete{m['class_name']},
  get{m['class_name']},
  page{m['class_name']},
  update{m['class_name']},
}} from '@/api/{m['api_module']}/{m['business_name']}'

const viewConfig = viewConfigData as GenViewSchema
const GEN_I18N_FILES: Record<string, GenI18nMessages> = {{
  'zh-CN': genI18nZhCN as GenI18nMessages,
  'en-US': genI18nEnUS as GenI18nMessages,
}}

const crudApi: GenCrudApi = {{
  page: page{m['class_name']},
  get: get{m['class_name']},
  create: create{m['class_name']},
  update: update{m['class_name']},
  delete: delete{m['class_name']},
}}

const createDefaults = {{ sortNo: 0, status: '0' }}
</script>
"""

    if variant == "treeMenu":
        return f"""<!-- AUTO-GENERATED {m['function_zh']} — GenTreeCrudPage + view.json -->
<template>
  <GenTreeCrudPage
    :file-view="viewConfig"
    business-name="{m['business_name']}"
    api-module="{m['api_module']}"
    :i18n-files="GEN_I18N_FILES"
    :perm="PERM"
    :api="crudApi"
    :get-add-child-defaults="getMenuAddChildDefaults"
    :create-defaults="menuCreateDefaults"
  />
</template>

<script setup lang="ts">
import GenTreeCrudPage from '@/components/gen/GenTreeCrudPage.vue'
import type {{ GenCrudApi }} from '@/components/gen/GenCrudPage.vue'
import type {{ GenI18nMessages }} from '@/types/tool/genI18n'
import type {{ GenViewSchema }} from '@/types/tool/genView'
import viewConfigData from '@/i18n/gen/{m['business_name']}/view.json'
import genI18nZhCN from '@/i18n/gen/{m['business_name']}/zh-CN.json'
import genI18nEnUS from '@/i18n/gen/{m['business_name']}/en-US.json'
import {{
  PERM_{m['class_name']} as PERM,
  create{m['class_name']},
  delete{m['class_name']},
  get{m['class_name']},
  page{m['class_name']},
  update{m['class_name']},
}} from '@/api/{m['api_module']}/{m['business_name']}'

const viewConfig = viewConfigData as GenViewSchema
const GEN_I18N_FILES: Record<string, GenI18nMessages> = {{
  'zh-CN': genI18nZhCN as GenI18nMessages,
  'en-US': genI18nEnUS as GenI18nMessages,
}}

const crudApi: GenCrudApi = {{
  page: page{m['class_name']},
  get: get{m['class_name']},
  create: create{m['class_name']},
  update: update{m['class_name']},
  delete: delete{m['class_name']},
}}

function getMenuAddChildDefaults(parent: Record<string, unknown>) {{
  if (!parent.id) return {{ menuType: 'M' }}
  if (parent.menuType === 'M') return {{ menuType: 'C' }}
  return {{ menuType: 'F' }}
}}

const menuCreateDefaults = {{ menuType: 'M', sortNo: 0, status: '0' }}
</script>
"""

    if variant == "masterDetail":
        return f"""<!-- AUTO-GENERATED {m['function_zh']} — GenMasterDetailPage + view.json -->
<template>
  <GenMasterDetailPage
    :file-view="viewConfig"
    business-name="{m['business_name']}"
    api-module="{m['api_module']}"
    :i18n-files="GEN_I18N_FILES"
    :perm="PERM"
    :api="masterDetailApi"
  />
</template>

<script setup lang="ts">
import GenMasterDetailPage from '@/components/gen/GenMasterDetailPage.vue'
import type {{ GenMasterDetailApi }} from '@/components/gen/GenMasterDetailPage.vue'
import type {{ GenCrudApi }} from '@/components/gen/GenCrudPage.vue'
import type {{ GenI18nMessages }} from '@/types/tool/genI18n'
import type {{ GenViewSchema }} from '@/types/tool/genView'
import viewConfigData from '@/i18n/gen/{m['business_name']}/view.json'
import genI18nZhCN from '@/i18n/gen/{m['business_name']}/zh-CN.json'
import genI18nEnUS from '@/i18n/gen/{m['business_name']}/en-US.json'
import {{
  PERM_{m['class_name']} as PERM,
  createDictItem,
  createDictType,
  deleteDictItem,
  deleteDictType,
  getDictItem,
  getDictType,
  pageDictItem,
  pageDictType,
  updateDictItem,
  updateDictType,
}} from '@/api/{m['api_module']}/{m['business_name']}'

const viewConfig = viewConfigData as GenViewSchema
const GEN_I18N_FILES: Record<string, GenI18nMessages> = {{
  'zh-CN': genI18nZhCN as GenI18nMessages,
  'en-US': genI18nEnUS as GenI18nMessages,
}}

const masterDetailApi: GenMasterDetailApi = {{
  master: {{
    page: pageDictType,
    get: getDictType,
    create: createDictType,
    update: updateDictType,
    delete: deleteDictType,
  }} as GenCrudApi,
  detail: {{
    page: pageDictItem,
    get: getDictItem,
    create: createDictItem,
    update: updateDictItem,
    delete: deleteDictItem,
  }} as GenCrudApi,
}}
</script>
"""

    if variant == "userExtended":
        slots = VUE_VARIANTS["userExtended"]
        return f"""<!-- AUTO-GENERATED {m['function_zh']} — GenCrudPage + view.json -->
<template>
  <div class="gen-page-shell">
    <GenCrudPage
      ref="pageRef"
      :file-view="viewConfig"
      business-name="{m['business_name']}"
      api-module="{m['api_module']}"
      :i18n-files="GEN_I18N_FILES"
      :perm="PERM"
      :api="crudApi"
      :tree-sources="treeSources"
    >
{slots}
    </GenCrudPage>

    <GenUserRoleDialog
      v-model="roleDialogVisible"
      :user-id="assignUserId"
      :title="gp('assignRolesTitle')"
      :cancel-text="t('common.cancel')"
      :confirm-text="t('common.confirm')"
      :success-text="t('common.success')"
      :empty-text="gp('noRolesAvailable')"
    />
  </div>
</template>

<script setup lang="ts">
import {{ computed, onMounted, ref }} from 'vue'
import {{ ElMessage, ElMessageBox }} from 'element-plus'
import {{ useI18n }} from 'vue-i18n'
import GenCrudPage from '@/components/gen/GenCrudPage.vue'
import GenUserRoleDialog from '@/components/gen/GenUserRoleDialog.vue'
import type {{ GenCrudApi }} from '@/components/gen/GenCrudPage.vue'
import type {{ GenI18nMessages }} from '@/types/tool/genI18n'
import type {{ GenViewSchema }} from '@/types/tool/genView'
import viewConfigData from '@/i18n/gen/{m['business_name']}/view.json'
import genI18nZhCN from '@/i18n/gen/{m['business_name']}/zh-CN.json'
import genI18nEnUS from '@/i18n/gen/{m['business_name']}/en-US.json'
import {{
  PERM_{m['class_name']} as PERM,
  create{m['class_name']},
  delete{m['class_name']},
  get{m['class_name']},
  page{m['class_name']},
  update{m['class_name']},
}} from '@/api/{m['api_module']}/{m['business_name']}'
import {{ pageDept }} from '@/api/system/dept'
import {{ buildGenTree }} from '@/utils/genTree'
import {{ useGenI18n }} from '@/composables/useGenI18n'

const {{ t }} = useI18n()
const pageRef = ref<InstanceType<typeof GenCrudPage>>()
const viewConfig = viewConfigData as GenViewSchema
const GEN_I18N_FILES: Record<string, GenI18nMessages> = {{
  'zh-CN': genI18nZhCN as GenI18nMessages,
  'en-US': genI18nEnUS as GenI18nMessages,
}}
const {{ gp }} = useGenI18n('{m['business_name']}', '{m['api_module']}', GEN_I18N_FILES)

const deptTree = ref<Record<string, unknown>[]>([])
const roleDialogVisible = ref(false)
const assignUserId = ref<string | null>(null)

const treeSources = computed(() => ({{
  dept: [
    {{
      id: '0',
      deptName: gp('treeRoot'),
      children: deptTree.value,
    }},
  ],
}}))

async function loadDeptTree() {{
  const res = await pageDept({{ pageNo: 1, pageSize: 10000 }})
  deptTree.value = buildGenTree(res.data.data.list as unknown as Record<string, unknown>[], 'parentId', '0')
}}

const crudApi: GenCrudApi = {{
  page: page{m['class_name']},
  get: get{m['class_name']},
  create: create{m['class_name']},
  update: update{m['class_name']},
  delete: delete{m['class_name']},
}}

function openEdit(row: Record<string, unknown>) {{
  const inst = pageRef.value as unknown as {{ openForm?: (r?: Record<string, unknown>) => void }}
  inst?.openForm?.(row)
}}

function openAssignRoles(row: Record<string, unknown>) {{
  if (!row.id) return
  assignUserId.value = String(row.id)
  roleDialogVisible.value = true
}}

async function handleDelete(row: Record<string, unknown>) {{
  if (!row.id) return
  await ElMessageBox.confirm(gp('deleteConfirm', {{ count: 1 }}), t('common.tip'))
  await delete{m['class_name']}(String(row.id))
  ElMessage.success(t('common.success'))
  await pageRef.value?.loadPage()
}}

onMounted(loadDeptTree)
</script>
"""

    if variant == "roleExtended":
        slots = VUE_VARIANTS["roleExtended"]
        return f"""<!-- AUTO-GENERATED {m['function_zh']} — GenCrudPage + view.json -->
<template>
  <div class="gen-page-shell">
    <GenCrudPage
      ref="pageRef"
      :file-view="viewConfig"
      business-name="{m['business_name']}"
      api-module="{m['api_module']}"
      :i18n-files="GEN_I18N_FILES"
      :perm="PERM"
      :api="crudApi"
    >
{slots}
    </GenCrudPage>

    <GenMenuAuthDialog
      v-model="menuDialogVisible"
      :role-id="assignRoleId"
      :title="gp('assignMenuTitle')"
      :cancel-text="t('common.cancel')"
      :confirm-text="t('common.confirm')"
      :success-text="t('common.success')"
    />
  </div>
</template>

<script setup lang="ts">
import {{ ref }} from 'vue'
import {{ ElMessage, ElMessageBox }} from 'element-plus'
import {{ useI18n }} from 'vue-i18n'
import GenCrudPage from '@/components/gen/GenCrudPage.vue'
import GenMenuAuthDialog from '@/components/gen/GenMenuAuthDialog.vue'
import type {{ GenCrudApi }} from '@/components/gen/GenCrudPage.vue'
import type {{ GenI18nMessages }} from '@/types/tool/genI18n'
import type {{ GenViewSchema }} from '@/types/tool/genView'
import viewConfigData from '@/i18n/gen/{m['business_name']}/view.json'
import genI18nZhCN from '@/i18n/gen/{m['business_name']}/zh-CN.json'
import genI18nEnUS from '@/i18n/gen/{m['business_name']}/en-US.json'
import {{
  PERM_{m['class_name']} as PERM,
  create{m['class_name']},
  delete{m['class_name']},
  get{m['class_name']},
  page{m['class_name']},
  update{m['class_name']},
}} from '@/api/{m['api_module']}/{m['business_name']}'
import {{ useGenI18n }} from '@/composables/useGenI18n'

const {{ t }} = useI18n()
const pageRef = ref<InstanceType<typeof GenCrudPage>>()
const viewConfig = viewConfigData as GenViewSchema
const GEN_I18N_FILES: Record<string, GenI18nMessages> = {{
  'zh-CN': genI18nZhCN as GenI18nMessages,
  'en-US': genI18nEnUS as GenI18nMessages,
}}
const {{ gp }} = useGenI18n('{m['business_name']}', '{m['api_module']}', GEN_I18N_FILES)

const menuDialogVisible = ref(false)
const assignRoleId = ref<string | null>(null)

const crudApi: GenCrudApi = {{
  page: page{m['class_name']},
  get: get{m['class_name']},
  create: create{m['class_name']},
  update: update{m['class_name']},
  delete: delete{m['class_name']},
}}

function openEdit(row: Record<string, unknown>) {{
  const inst = pageRef.value as unknown as {{ openForm?: (r?: Record<string, unknown>) => void }}
  inst?.openForm?.(row)
}}

function openAssignMenu(row: Record<string, unknown>) {{
  if (!row.id) return
  assignRoleId.value = String(row.id)
  menuDialogVisible.value = true
}}

async function handleDelete(row: Record<string, unknown>) {{
  if (!row.id) return
  await ElMessageBox.confirm(gp('deleteConfirm', {{ count: 1 }}), t('common.tip'))
  await delete{m['class_name']}(String(row.id))
  ElMessage.success(t('common.success'))
  await pageRef.value?.loadPage()
}}
</script>
"""

    if variant == "user":
        return f"""<!-- AUTO-GENERATED {m['function_zh']} — GenCrudPage + view.json -->
<template>
  <GenCrudPage
    :file-view="viewConfig"
    business-name="{m['business_name']}"
    api-module="{m['api_module']}"
    :i18n-files="GEN_I18N_FILES"
    :perm="PERM"
    :api="crudApi"
    :tree-sources="treeSources"
  />
</template>

<script setup lang="ts">
import {{ computed, onMounted, ref }} from 'vue'
import GenCrudPage from '@/components/gen/GenCrudPage.vue'
import type {{ GenCrudApi }} from '@/components/gen/GenCrudPage.vue'
import type {{ GenI18nMessages }} from '@/types/tool/genI18n'
import type {{ GenViewSchema }} from '@/types/tool/genView'
import viewConfigData from '@/i18n/gen/{m['business_name']}/view.json'
import genI18nZhCN from '@/i18n/gen/{m['business_name']}/zh-CN.json'
import genI18nEnUS from '@/i18n/gen/{m['business_name']}/en-US.json'
import {{
  PERM_{m['class_name']} as PERM,
  create{m['class_name']},
  delete{m['class_name']},
  get{m['class_name']},
  page{m['class_name']},
  update{m['class_name']},
}} from '@/api/{m['api_module']}/{m['business_name']}'
import {{ pageDept }} from '@/api/system/dept'
import {{ buildGenTree }} from '@/utils/genTree'
import {{ useGenI18n }} from '@/composables/useGenI18n'

const viewConfig = viewConfigData as GenViewSchema
const GEN_I18N_FILES: Record<string, GenI18nMessages> = {{
  'zh-CN': genI18nZhCN as GenI18nMessages,
  'en-US': genI18nEnUS as GenI18nMessages,
}}
const {{ gp }} = useGenI18n('{m['business_name']}', '{m['api_module']}', GEN_I18N_FILES)

const deptTree = ref<Record<string, unknown>[]>([])

const treeSources = computed(() => ({{
  dept: [
    {{
      id: '0',
      deptName: gp('treeRoot'),
      children: deptTree.value,
    }},
  ],
}}))

async function loadDeptTree() {{
  const res = await pageDept({{ pageNo: 1, pageSize: 10000 }})
  deptTree.value = buildGenTree(res.data.data.list as unknown as Record<string, unknown>[], 'parentId', '0')
}}

const crudApi: GenCrudApi = {{
  page: page{m['class_name']},
  get: get{m['class_name']},
  create: create{m['class_name']},
  update: update{m['class_name']},
  delete: delete{m['class_name']},
}}

onMounted(loadDeptTree)
</script>
"""

    return shell_vue(
        comment=m["function_zh"],
        business_name=m["business_name"],
        api_module=m["api_module"],
        class_name=m["class_name"],
    )


def main(*, shells_only: bool = False) -> None:
    for m in MODULES:
        if shells_only:
            m["vue_path"].parent.mkdir(parents=True, exist_ok=True)
            m["vue_path"].write_text(render_vue(m), encoding="utf-8")
            print(f"OK shell {m['business_name']} -> {m['vue_path'].relative_to(ROOT)}")
            continue

        zh, en = i18n_bundle(
            m["function_zh"], m["function_en"], m["business_name"], m["api_module"], m["fields"], m.get("view"),
        )
        if m.get("vue_variant") == "config":
            zh["page"]["refreshCache"] = "刷新缓存"
            zh["page"]["refreshCacheSuccess"] = "参数缓存已刷新"
            en["page"]["refreshCache"] = "Refresh cache"
            en["page"]["refreshCacheSuccess"] = "Parameter cache refreshed"
        if m.get("vue_variant") == "i18nMessage":
            zh["page"]["refreshCache"] = "刷新缓存"
            zh["page"]["refreshCacheSuccess"] = "文案缓存已刷新"
            zh["page"]["refreshCacheConfirm"] = "确认刷新全部国际化文案缓存？"
            en["page"]["refreshCache"] = "Refresh cache"
            en["page"]["refreshCacheSuccess"] = "Message cache refreshed"
            en["page"]["refreshCacheConfirm"] = "Refresh all i18n message caches?"
        if m.get("vue_variant") == "tenant":
            zh["page"]["disable"] = "停用"
            zh["page"]["enable"] = "启用"
            zh["page"]["disableConfirm"] = "确认停用该租户？"
            en["page"]["disable"] = "Disable"
            en["page"]["enable"] = "Enable"
            en["page"]["disableConfirm"] = "Disable this tenant?"
        if m.get("vue_variant") == "auditLog":
            zh["page"]["clear"] = "清空"
            zh["page"]["clearConfirm"] = "确认清空全部日志？此操作不可恢复。"
            en["page"]["clear"] = "Clear"
            en["page"]["clearConfirm"] = "Clear all logs? This cannot be undone."
        if m.get("vue_variant") in ("tree", "treeMenu", "user", "userExtended"):
            zh["page"]["treeRoot"] = "顶级" if m["business_name"] != "dept" else "顶级部门"
            zh["page"]["addChild"] = "新增子项" if m["business_name"] != "menu" else "新增子菜单"
            en["page"]["treeRoot"] = "Root"
            en["page"]["addChild"] = "Add Child"
        if m.get("vue_variant") == "userExtended":
            zh["page"]["assignRoles"] = "分配角色"
            zh["page"]["assignRolesTitle"] = "分配角色"
            zh["page"]["noRolesAvailable"] = "暂无可用角色"
            en["page"]["assignRoles"] = "Assign Roles"
            en["page"]["assignRolesTitle"] = "Assign Roles"
            en["page"]["noRolesAvailable"] = "No roles available"
        if m.get("vue_variant") == "roleExtended":
            zh["page"]["assignMenu"] = "菜单授权"
            zh["page"]["assignMenuTitle"] = "菜单授权"
            en["page"]["assignMenu"] = "Menu Auth"
            en["page"]["assignMenuTitle"] = "Assign Menus"
        if m.get("vue_variant") == "masterDetail":
            zh["page"]["masterPanel"] = "字典类型"
            zh["page"]["detailPanel"] = "字典数据"
            zh["page"]["selectMasterFirst"] = "请先选择字典类型"
            zh["page"]["masterAddTitle"] = "新增字典类型"
            zh["page"]["masterEditTitle"] = "修改字典类型"
            zh["page"]["detailAddTitle"] = "新增字典数据"
            zh["page"]["detailEditTitle"] = "修改字典数据"
            en["page"]["masterPanel"] = "Dict Types"
            en["page"]["detailPanel"] = "Dict Items"
            en["page"]["selectMasterFirst"] = "Select a dict type first"
            en["page"]["masterAddTitle"] = "Add Dict Type"
            en["page"]["masterEditTitle"] = "Edit Dict Type"
            en["page"]["detailAddTitle"] = "Add Dict Item"
            en["page"]["detailEditTitle"] = "Edit Dict Item"

        publish(m["business_name"], m["view"], zh, en)
        m["vue_path"].parent.mkdir(parents=True, exist_ok=True)
        m["vue_path"].write_text(render_vue(m), encoding="utf-8")
        print(f"OK {m['business_name']} -> {m['vue_path'].relative_to(ROOT)}")


if __name__ == "__main__":
    main(shells_only="--shells-only" in sys.argv)
