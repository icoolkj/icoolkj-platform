"""Element Plus + TypeScript frontend renderers for Admin UI."""
from __future__ import annotations

from .column_meta import ColumnMeta
from .model import TableContext


def _route_module(ctx: TableContext) -> str:
    return ctx.api_module


def render_frontend_types(ctx: TableContext, metas: list[ColumnMeta]) -> str:
    vo_fields = []
    for col in ctx.vo_columns:
        ts = next((m.ts_type for m in metas if m.prop == col.prop), "string")
        vo_fields.append(f"  {col.prop}?: {ts}")
    query_fields = ["  pageNo?: number", "  pageSize?: number"]
    for m in metas:
        if m.is_query:
            query_fields.append(f"  {m.prop}?: {m.ts_type}")
    form_fields = []
    for m in metas:
        if m.is_form:
            form_fields.append(f"  {m.prop}?: {m.ts_type}")
    form_fields.insert(0, "  id?: string")

    return "\n".join([
        f"/** AUTO-GENERATED — {ctx.comment} */",
        "",
        f"export interface {ctx.biz_name}VO {{",
        *vo_fields,
        "}",
        "",
        f"export interface {ctx.biz_name}PageQuery {{",
        *query_fields,
        "}",
        "",
        f"export interface {ctx.biz_name}Form {{",
        *form_fields,
        "}",
        "",
        "export interface ApiResponse<T> {",
        "  code: number",
        "  message: string",
        "  data: T",
        "}",
        "",
        "export interface PageResult<T> {",
        "  list: T[]",
        "  total: number",
        "  pageNo: number",
        "  pageSize: number",
        "}",
        "",
    ])


def render_frontend_api_ts(ctx: TableContext) -> str:
    b = ctx.biz_name
    perm = ctx.permission_prefix
    return "\n".join([
        f"/** AUTO-GENERATED — {ctx.comment} API */",
        f"import request from '@/utils/request'",
        f"import type {{ ApiResponse, PageResult, {b}Form, {b}PageQuery, {b}VO }} from '@/types/{_route_module(ctx)}/{ctx.route_name}'",
        "",
        f"const BASE = '{ctx.route_path}'",
        "",
        f"export function page{b}(params: {b}PageQuery) {{",
        f"  return request.get<ApiResponse<PageResult<{b}VO>>>(`${{BASE}}/page`, {{ params }})",
        "}",
        "",
        f"export function get{b}(id: string) {{",
        f"  return request.get<ApiResponse<{b}VO>>(`${{BASE}}/${{id}}`)",
        "}",
        "",
        *( [
            f"export function create{b}(data: {b}Form) {{",
            f"  return request.post<ApiResponse<string>>(BASE, data)",
            "}",
            "",
            f"export function update{b}(data: {b}Form) {{",
            f"  return request.put<ApiResponse<void>>(BASE, data)",
            "}",
            "",
            f"export function delete{b}(id: string) {{",
            f"  return request.delete<ApiResponse<void>>(`${{BASE}}/${{id}}`)",
            "}",
            "",
            f"export const PERM_{b.upper()} = {{",
            f"  list: '{perm}:list',",
            f"  query: '{perm}:query',",
            f"  add: '{perm}:add',",
            f"  edit: '{perm}:edit',",
            f"  remove: '{perm}:remove',",
            "}",
            "",
        ] if ctx.crud == "full" else [
            f"export const PERM_{b.upper()} = {{ list: '{perm}:list', query: '{perm}:query' }}",
            "",
        ]),
    ])


def _query_field(m: ColumnMeta) -> str:
    if m.html_type == "datetime":
        return f"""      <el-form-item label="{m.label}" prop="{m.prop}">
        <el-date-picker v-model="queryParams.{m.prop}" type="date" value-format="YYYY-MM-DD" placeholder="请选择{m.label}" clearable />
      </el-form-item>"""
    if m.html_type in ("select", "radio"):
        return f"""      <el-form-item label="{m.label}" prop="{m.prop}">
        <el-select v-model="queryParams.{m.prop}" placeholder="请选择{m.label}" clearable>
          <el-option label="全部" value="" />
        </el-select>
      </el-form-item>"""
    return f"""      <el-form-item label="{m.label}" prop="{m.prop}">
        <el-input v-model="queryParams.{m.prop}" placeholder="请输入{m.label}" clearable @keyup.enter="handleQuery" />
      </el-form-item>"""


def _table_column(m: ColumnMeta) -> str:
    if not m.is_list:
        return ""
    return f'      <el-table-column label="{m.label}" prop="{m.prop}" align="center" show-overflow-tooltip />'


def _form_field(m: ColumnMeta) -> str:
    if not m.is_form:
        return ""
    if m.html_type == "textarea":
        return f"""        <el-form-item label="{m.label}" prop="{m.prop}">
          <el-input v-model="form.{m.prop}" type="textarea" placeholder="请输入{m.label}" />
        </el-form-item>"""
    if m.html_type == "datetime":
        return f"""        <el-form-item label="{m.label}" prop="{m.prop}">
          <el-date-picker v-model="form.{m.prop}" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择{m.label}" />
        </el-form-item>"""
    if m.html_type in ("select", "radio"):
        return f"""        <el-form-item label="{m.label}" prop="{m.prop}">
          <el-select v-model="form.{m.prop}" placeholder="请选择{m.label}">
            <el-option label="选项1" value="0" />
          </el-select>
        </el-form-item>"""
    return f"""        <el-form-item label="{m.label}" prop="{m.prop}">
          <el-input v-model="form.{m.prop}" placeholder="请输入{m.label}" />
        </el-form-item>"""


def render_frontend_view(ctx: TableContext, metas: list[ColumnMeta]) -> str:
    b = ctx.biz_name
    mod = _route_module(ctx)
    query_items = "\n".join(_query_field(m) for m in metas if m.is_query)
    table_cols = "\n".join(_table_column(m) for m in metas if m.is_list)
    form_items = "\n".join(_form_field(m) for m in metas if m.is_form)
    readonly = ctx.crud != "full"

    crud_buttons = "" if readonly else """
      <el-col :span="1.5">
        <el-button type="primary" plain :icon="Plus" @click="handleAdd" v-hasPermi="[PERM.add]">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain :icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="[PERM.edit]">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain :icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="[PERM.remove]">删除</el-button>
      </el-col>"""

    action_col = "" if readonly else """
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" :icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="[PERM.edit]">修改</el-button>
          <el-button link type="primary" :icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="[PERM.remove]">删除</el-button>
        </template>
      </el-table-column>"""

    dialog_block = "" if readonly else f"""
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" label-width="100px">
{form_items}
      </el-form>
      <template #footer>
        <el-button @click="open = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>"""

    script_crud = "" if readonly else f"""
async function handleAdd() {{
  resetForm()
  open.value = true
  title.value = '新增{ctx.comment}'
}}

async function handleUpdate(row?: {b}VO) {{
  resetForm()
  const id = row?.id ?? ids.value[0]
  if (!id) return
  const res = await get{b}(id)
  form.value = {{ ...res.data }}
  open.value = true
  title.value = '修改{ctx.comment}'
}}

async function submitForm() {{
  if (form.value.id) {{
    await update{b}(form.value)
    ElMessage.success('修改成功')
  }} else {{
    await create{b}(form.value)
    ElMessage.success('新增成功')
  }}
  open.value = false
  await loadPage()
}}

async function handleDelete(row?: {b}VO) {{
  const targetIds = row?.id ? [row.id] : ids.value
  if (!targetIds.length) return
  await ElMessageBox.confirm(`确认删除选中的 ${{targetIds.length}} 条数据？`, '提示')
  for (const id of targetIds) {{
    await delete{b}(id)
  }}
  ElMessage.success('删除成功')
  await loadPage()
}}

function resetForm() {{
  form.value = {{}}
}}"""

    imports_crud = "" if readonly else f", create{b}, update{b}, delete{b}"
    form_state = "" if readonly else f"""
const open = ref(false)
const title = ref('')
const form = ref<{b}Form>({{}})
const formRef = ref<FormInstance>()"""

    return f"""<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="90px">
{query_items}
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">{crud_buttons}
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
{table_cols}{action_col}
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="loadPage"
    />
{dialog_block}
  </div>
</template>

<script setup lang="ts">
import {{ ref, reactive, onMounted }} from 'vue'
import {{ ElMessage, ElMessageBox }} from 'element-plus'
import type {{ FormInstance }} from 'element-plus'
import {{ Search, Refresh, Plus, Edit, Delete }} from '@element-plus/icons-vue'
import {{ page{b}, get{b}{imports_crud}, PERM_{b.upper()} as PERM }} from '@/api/{mod}/{ctx.route_name}'
import type {{ {b}Form, {b}PageQuery, {b}VO }} from '@/types/{mod}/{ctx.route_name}'

const loading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const dataList = ref<{b}VO[]>([])
const ids = ref<string[]>([])
const single = ref(true)
const multiple = ref(true)
const queryRef = ref<FormInstance>()
{form_state}

const queryParams = reactive<{b}PageQuery>({{
  pageNo: 1,
  pageSize: 20,
}})

async function loadPage() {{
  loading.value = true
  try {{
    const res = await page{b}(queryParams)
    dataList.value = res.data.list
    total.value = res.data.total
  }} finally {{
    loading.value = false
  }}
}}

function handleQuery() {{
  queryParams.pageNo = 1
  loadPage()
}}

function resetQuery() {{
  queryRef.value?.resetFields()
  handleQuery()
}}

function handleSelectionChange(selection: {b}VO[]) {{
  ids.value = selection.map(item => item.id!).filter(Boolean)
  single.value = selection.length !== 1
  multiple.value = selection.length === 0
}}
{script_crud}

onMounted(() => {{
  loadPage()
}})
</script>
"""
