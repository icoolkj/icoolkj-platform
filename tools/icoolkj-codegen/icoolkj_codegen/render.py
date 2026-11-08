"""Java / XML code renderers."""
from __future__ import annotations

from .model import BASE_SKIP, Column, TableContext, camel_to_snake, java_imports_for_type, snake_to_camel


def _field_lines(columns: list[Column], base: str, plain_with_id: bool) -> tuple[list[str], set[str]]:
    skip = BASE_SKIP.get(base, set())
    imports: set[str] = set()
    fields: list[str] = []
    for col in columns:
        if col.name in skip:
            continue
        prop = col.prop
        ann: list[str] = []
        if base == "plain" and col.name == "id" and plain_with_id:
            imports.add("com.baomidou.mybatisplus.annotation.IdType")
            imports.add("com.baomidou.mybatisplus.annotation.TableId")
            ann.append("    @TableId(type = IdType.ASSIGN_ID)")
        if col.name != prop:
            imports.add("com.baomidou.mybatisplus.annotation.TableField")
            ann.append(f'    @TableField("{col.name}")')
        imports.update(java_imports_for_type(col.java_type))
        fields.extend(ann + [f"    private {col.short_type} {prop};", ""])
    return fields, imports


def render_entity(ctx: TableContext) -> str:
    pkg = f"{ctx.pkg}.domain"
    imports: set[str] = {"com.baomidou.mybatisplus.annotation.TableName"}
    lombok: list[str]
    extends = ""

    if ctx.base in ("tenant", "base", "platform"):
        base_cls = "TenantBaseEntity" if ctx.base == "tenant" else "BaseEntity"
        imports.add(f"com.icoolkj.platform.framework.mybatis.{base_cls}")
        imports.update({"lombok.EqualsAndHashCode", "lombok.Getter", "lombok.Setter", "lombok.ToString"})
        lombok = [
            "@Getter", "@Setter",
            "@EqualsAndHashCode(callSuper = true)",
            "@ToString(callSuper = true)",
        ]
        extends = f" extends {base_cls}"
    else:
        imports.add("lombok.Data")
        lombok = ["@Data"]

    field_lines, field_imports = _field_lines(ctx.columns, ctx.base, ctx.has_id)
    imports.update(field_imports)

    return "\n".join([
        f"package {pkg};",
        "",
        *[f"import {i};" for i in sorted(imports)],
        "",
        f"/** {ctx.comment} — maps to `{ctx.table}`. */",
        f'@TableName("{ctx.table}")',
        *lombok,
        f"public class {ctx.entity}{extends} {{",
        "",
        *field_lines,
        "}",
        "",
    ])


def _result_map(ctx: TableContext) -> str:
    lines = []
    for col in ctx.columns:
        tag = "id" if col.name == "id" else "result"
        lines.append(f'        <{tag} column="{col.name}" property="{col.prop}"/>')
    return "\n".join(lines)


def _list_where(ctx: TableContext) -> str:
    parts = []
    if ctx.has_deleted:
        parts.append("            deleted = 0")
    for f in ctx.list_filters:
        parts.append(
            f'            <if test="{f} != null and {f} != \'\'">AND {camel_to_snake(f)} = #{{{f}}}</if>'
        )
    return "\n".join(parts)


def render_mapper_xml(ctx: TableContext, extra_xml: str = "") -> str:
    cols = [c.name for c in ctx.columns]
    col_list = ", ".join(cols)
    fq_entity = f"{ctx.pkg}.domain.{ctx.entity}"
    fq_mapper = f"{ctx.pkg}.mapper.{ctx.mapper}"
    fq_page = f"{ctx.pkg}.dto.{ctx.page_query_name}"

    blocks = [
        '<?xml version="1.0" encoding="UTF-8"?>',
        '<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">',
        f'<mapper namespace="{fq_mapper}">',
        "",
        f'    <resultMap id="BaseResultMap" type="{fq_entity}">',
        _result_map(ctx),
        "    </resultMap>",
        "",
        '    <sql id="Base_Column_List">',
        f"        {col_list}",
        "    </sql>",
        "",
        '    <sql id="Query_Where">',
        "        <where>",
        _list_where(ctx),
        "        </where>",
        "    </sql>",
        "",
    ]

    if ctx.has_id:
        where_del = " AND deleted = 0" if ctx.has_deleted else ""
        blocks += [
            '    <select id="selectById" resultMap="BaseResultMap">',
            '        SELECT <include refid="Base_Column_List"/>',
            f"        FROM {ctx.table}",
            f"        WHERE id = #{{id}}{where_del}",
            "    </select>",
            "",
        ]

    blocks += [
        '    <select id="selectList" resultMap="BaseResultMap">',
        '        SELECT <include refid="Base_Column_List"/>',
        f"        FROM {ctx.table}",
        '        <include refid="Query_Where"/>',
        f"        ORDER BY {ctx.order_by}",
        "    </select>",
        "",
    ]

    if ctx.crud != "none":
        blocks += [
            f'    <select id="selectPage" resultMap="BaseResultMap">',
            '        <bind name="offset" value="(pageNo - 1) * pageSize"/>',
            '        SELECT <include refid="Base_Column_List"/>',
            f"        FROM {ctx.table}",
            '        <include refid="Query_Where"/>',
            f"        ORDER BY {ctx.order_by}",
            "        LIMIT #{offset}, #{pageSize}",
            "    </select>",
            "",
            '    <select id="countByQuery" resultType="long">',
            f"        SELECT COUNT(1) FROM {ctx.table}",
            '        <include refid="Query_Where"/>',
            "    </select>",
            "",
        ]

    insert_vals = ", ".join(f"#{{{snake_to_camel(c)}}}" for c in cols)
    blocks += [
        '    <insert id="insert">',
        f"        INSERT INTO {ctx.table} ({col_list})",
        f"        VALUES ({insert_vals})",
        "    </insert>",
        "",
    ]

    if ctx.has_id:
        update_cols = [c for c in cols if c not in ("id", "created_at", "created_by")]
        set_clause = ",\n            ".join(f"{c} = #{{{snake_to_camel(c)}}}" for c in update_cols)
        where_del = " AND deleted = 0" if ctx.has_deleted else ""
        blocks += [
            '    <update id="updateById">',
            f"        UPDATE {ctx.table}",
            f"        SET {set_clause}",
            f"        WHERE id = #{{id}}{where_del}",
            "    </update>",
            "",
        ]
        if ctx.has_deleted:
            blocks += [
                '    <update id="deleteById">',
                f"        UPDATE {ctx.table} SET deleted = 1 WHERE id = #{{id}}",
                "    </update>",
                "",
            ]
        else:
            blocks += [
                '    <delete id="deleteById">',
                f"        DELETE FROM {ctx.table} WHERE id = #{{id}}",
                "    </delete>",
                "",
            ]
    else:
        blocks += [
            '    <delete id="delete">',
            f"        DELETE FROM {ctx.table}",
            "        <where>",
            _list_where(ctx),
            "        </where>",
            "    </delete>",
            "",
        ]

    if extra_xml.strip():
        blocks.append(extra_xml.rstrip())
        blocks.append("")
    blocks += ["</mapper>", ""]
    return "\n".join(blocks)


def render_mapper_java(ctx: TableContext, extra_java: list[str]) -> str:
    lines = [
        f"package {ctx.pkg}.mapper;",
        "",
        f"import {ctx.pkg}.domain.{ctx.entity};",
    ]
    if ctx.crud != "none":
        lines.append(f"import {ctx.pkg}.dto.{ctx.page_query_name};")
    lines += [
        "import org.apache.ibatis.annotations.Mapper;",
        "import org.apache.ibatis.annotations.Param;",
        "",
        "import java.util.List;",
        "",
        "@Mapper",
        f"public interface {ctx.mapper} {{",
        "",
    ]
    if ctx.has_id:
        lines += [f'    {ctx.entity} selectById(@Param("id") String id);', ""]
    lines += [
        f"    List<{ctx.entity}> selectList({ctx.entity} query);",
        "",
    ]
    if ctx.crud != "none":
        lines += [
            f"    List<{ctx.entity}> selectPage({ctx.page_query_name} query);",
            "",
            f"    long countByQuery({ctx.page_query_name} query);",
            "",
        ]
    lines += [
        f"    int insert({ctx.entity} entity);",
        "",
    ]
    if ctx.has_id:
        lines += [
            f"    int updateById({ctx.entity} entity);",
            "",
            '    int deleteById(@Param("id") String id);',
            "",
        ]
    else:
        lines += [f"    int delete({ctx.entity} entity);", ""]
    for line in extra_java:
        s = line.strip()
        if s:
            lines.append(f"    {s}" if not line.startswith("    ") else line)
    if extra_java:
        lines.append("")
    lines += ["}", ""]
    return "\n".join(lines)


def _dto_fields(columns: list[Column]) -> tuple[list[str], set[str]]:
    imports: set[str] = set()
    fields: list[str] = []
    for col in columns:
        imports.update(java_imports_for_type(col.java_type))
        ann = []
        if col.name != col.prop:
            ann.append(f'    @TableField("{col.name}")')  # wrong - DTOs don't use TableField
        fields.extend([f"    private {col.short_type} {col.prop};", ""])
    return fields, imports


def render_page_query(ctx: TableContext) -> str:
    imports = {"com.icoolkj.platform.common.model.page.PageQuery", "lombok.Data", "lombok.EqualsAndHashCode", "lombok.ToString"}
    for col in ctx.business_columns:
        if col.prop in ctx.list_filters:
            imports.update(java_imports_for_type(col.java_type))
    fields = []
    for f in ctx.list_filters:
        col = next((c for c in ctx.columns if c.prop == f), None)
        if col:
            fields.append(f"    private {col.short_type} {col.prop};")
    body = "\n".join([
        f"package {ctx.pkg}.dto;",
        "",
        *[f"import {i};" for i in sorted(imports)],
        "",
        f"/** {ctx.comment}分页查询 */",
        "@Data",
        "@EqualsAndHashCode(callSuper = true)",
        "@ToString(callSuper = true)",
        f"public class {ctx.page_query_name} extends PageQuery {{",
        "",
        *fields,
        "}",
        "",
    ])
    return body


def render_create_request(ctx: TableContext) -> str:
    imports: set[str] = {"lombok.Data"}
    for col in ctx.create_columns:
        imports.update(java_imports_for_type(col.java_type))
    fields = [f"    private {col.short_type} {col.prop};" for col in ctx.create_columns]
    return "\n".join([
        f"package {ctx.pkg}.dto;",
        "",
        *[f"import {i};" for i in sorted(imports)],
        "",
        f"/** 新增{ctx.comment} */",
        "@Data",
        f"public class {ctx.create_request_name} {{",
        "",
        *fields,
        "}",
        "",
    ])


def render_update_request(ctx: TableContext) -> str:
    imports: set[str] = {"jakarta.validation.constraints.NotBlank", "lombok.Data"}
    fields = ['    @NotBlank(message = "ID不能为空")', "    private String id;"]
    for col in ctx.update_columns:
        imports.update(java_imports_for_type(col.java_type))
        fields.append(f"    private {col.short_type} {col.prop};")
    return "\n".join([
        f"package {ctx.pkg}.dto;",
        "",
        *[f"import {i};" for i in sorted(imports)],
        "",
        f"/** 修改{ctx.comment} */",
        "@Data",
        f"public class {ctx.update_request_name} {{",
        "",
        *fields,
        "}",
        "",
    ])


def render_vo(ctx: TableContext) -> str:
    imports: set[str] = set()
    fields: list[str] = []
    for col in ctx.vo_columns:
        imports.update(java_imports_for_type(col.java_type))
        ann = []
        if col.name != col.prop:
            pass
        fields.extend(ann + [f"    private {col.short_type} {col.prop};"])
    getters = []
    for col in ctx.vo_columns:
        p = col.prop
        getters.append(f"    public {col.short_type} get{p[0].upper()}{p[1:]}() {{ return {p}; }}")
        getters.append(f"    public void set{p[0].upper()}{p[1:]}({col.short_type} {p}) {{ this.{p} = {p}; }}")
    return "\n".join([
        f"package {ctx.pkg}.vo;",
        "",
        *[f"import {i};" for i in sorted(imports)],
        "",
        f"/** {ctx.comment}列表/详情 */",
        f"public class {ctx.vo_name} {{",
        "",
        *fields,
        "",
        *getters,
        "}",
        "",
    ])


def render_convert(ctx: TableContext) -> str:
    entity = ctx.entity
    lines = [
        f"package {ctx.pkg}.convert;",
        "",
        f"import {ctx.pkg}.domain.{entity};",
        f"import {ctx.pkg}.vo.{ctx.vo_name};",
    ]
    if ctx.crud == "full":
        lines += [
            f"import {ctx.pkg}.dto.{ctx.create_request_name};",
            f"import {ctx.pkg}.dto.{ctx.update_request_name};",
        ]
    lines += [
        "",
        f"/** {ctx.comment}对象转换（生成器维护，可逐步替换为 MapStruct）。 */",
        f"public final class {ctx.convert_name} {{",
        "",
        f"    private {ctx.convert_name}() {{",
        "    }",
        "",
        f"    public static {ctx.vo_name} toVo({entity} entity) {{",
        "        if (entity == null) {",
        "            return null;",
        "        }",
        f"        {ctx.vo_name} vo = new {ctx.vo_name}();",
    ]
    for col in ctx.vo_columns:
        lines.append(f"        vo.set{col.prop[0].upper()}{col.prop[1:]}(entity.get{col.prop[0].upper()}{col.prop[1:]}());")
    lines += ["        return vo;", "    }", ""]
    if ctx.crud == "full":
        lines += [
            f"    public static {entity} toEntity({ctx.create_request_name} request) {{",
            f"        {entity} entity = new {entity}();",
        ]
        for col in ctx.create_columns:
            lines.append(f"        entity.set{col.prop[0].upper()}{col.prop[1:]}(request.get{col.prop[0].upper()}{col.prop[1:]}());")
        lines += [
            "        return entity;",
            "    }",
            "",
            f"    public static void copy({ctx.update_request_name} request, {entity} entity) {{",
        ]
        for col in ctx.update_columns:
            lines.append(f"        entity.set{col.prop[0].upper()}{col.prop[1:]}(request.get{col.prop[0].upper()}{col.prop[1:]}());")
        lines += ["    }", "}", ""]
    else:
        lines += ["}", ""]
    return "\n".join(lines)


def render_service(ctx: TableContext) -> str:
    pkg = ctx.pkg
    imports = [
        "import com.icoolkj.platform.common.model.page.PageResult;",
        f"import {pkg}.dto.{ctx.page_query_name};",
        f"import {pkg}.vo.{ctx.vo_name};",
    ]
    if ctx.crud == "full":
        imports += [
            f"import {pkg}.dto.{ctx.create_request_name};",
            f"import {pkg}.dto.{ctx.update_request_name};",
        ]
    lines = [
        f"package {pkg}.service;",
        "",
        *imports,
        "",
        f"/** {ctx.comment}服务 */",
        f"public interface {ctx.service_name} {{",
        "",
        f"    PageResult<{ctx.vo_name}> page({ctx.page_query_name} query);",
        "",
        f"    {ctx.vo_name} getById(String id);",
        "",
    ]
    if ctx.crud == "full":
        lines += [
            f"    String create({ctx.create_request_name} request);",
            "",
            f"    void update({ctx.update_request_name} request);",
            "",
            "    void deleteById(String id);",
            "",
        ]
    lines += ["}", ""]
    return "\n".join(lines)


def render_service_impl(ctx: TableContext) -> str:
    pkg = ctx.pkg
    mapper = ctx.mapper
    entity = ctx.entity
    mapper_var = mapper[0].lower() + mapper[1:]
    imports = [
        "import com.icoolkj.platform.common.exception.BusinessException;",
        "import com.icoolkj.platform.common.model.page.PageResult;",
        f"import {pkg}.convert.{ctx.convert_name};",
        f"import {pkg}.domain.{entity};",
        f"import {pkg}.dto.{ctx.page_query_name};",
        f"import {pkg}.mapper.{mapper};",
        f"import {pkg}.service.{ctx.service_name};",
        f"import {pkg}.vo.{ctx.vo_name};",
        "import org.springframework.stereotype.Service;",
        "import org.springframework.util.CollectionUtils;",
        "",
        "import java.util.Collections;",
        "import java.util.List;",
    ]
    if ctx.crud == "full":
        imports[2:2] = [
            "import com.icoolkj.platform.common.util.IdGeneratorUtils;",
            f"import {pkg}.dto.{ctx.create_request_name};",
            f"import {pkg}.dto.{ctx.update_request_name};",
        ]
    lines = [
        f"package {pkg}.service.impl;",
        "",
        *imports,
        "",
        f"/** {ctx.comment}服务实现 */",
        "@Service",
        f"public class {ctx.service_impl_name} implements {ctx.service_name} {{",
        "",
        f"    private final {mapper} {mapper_var};",
        "",
        f"    public {ctx.service_impl_name}({mapper} {mapper_var}) {{",
        f"        this.{mapper_var} = {mapper_var};",
        "    }",
        "",
        "    @Override",
        f"    public PageResult<{ctx.vo_name}> page({ctx.page_query_name} query) {{",
        f"        long total = {mapper_var}.countByQuery(query);",
        "        if (total == 0) {",
        f"            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());",
        "        }",
        f"        List<{entity}> list = {mapper_var}.selectPage(query);",
        "        if (CollectionUtils.isEmpty(list)) {",
        f"            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());",
        "        }",
        f"        List<{ctx.vo_name}> rows = list.stream().map({ctx.convert_name}::toVo).toList();",
        f"        return PageResult.of(rows, total, query.getPageNo(), query.getPageSize());",
        "    }",
        "",
        "    @Override",
        f"    public {ctx.vo_name} getById(String id) {{",
        f"        {entity} entity = {mapper_var}.selectById(id);",
        "        if (entity == null) {",
        f'            throw new BusinessException("{ctx.comment}不存在");',
        "        }",
        f"        return {ctx.convert_name}.toVo(entity);",
        "    }",
        "",
    ]
    if ctx.crud == "full":
        lines += [
            "    @Override",
            f"    public String create({ctx.create_request_name} request) {{",
            f"        {entity} entity = {ctx.convert_name}.toEntity(request);",
            "        entity.setId(IdGeneratorUtils.nextIdStr());",
            f"        {mapper_var}.insert(entity);",
            "        return entity.getId();",
            "    }",
            "",
            "    @Override",
            f"    public void update({ctx.update_request_name} request) {{",
            f"        {entity} entity = {mapper_var}.selectById(request.getId());",
            "        if (entity == null) {",
            f'            throw new BusinessException("{ctx.comment}不存在");',
            "        }",
            f"        {ctx.convert_name}.copy(request, entity);",
            f"        {mapper_var}.updateById(entity);",
            "    }",
            "",
            "    @Override",
            "    public void deleteById(String id) {",
            f"        {entity} entity = {mapper_var}.selectById(id);",
            "        if (entity == null) {",
            f'            throw new BusinessException("{ctx.comment}不存在");',
            "        }",
            f"        {mapper_var}.deleteById(id);",
            "    }",
            "",
        ]
    lines += ["}", ""]
    return "\n".join(lines)


def render_controller(ctx: TableContext) -> str:
    api_java_pkg = f"com.icoolkj.platform.api.controller.{ctx.api_module}"
    svc = ctx.service_name
    svc_var = svc[0].lower() + svc[1:]
    lines = [
        f"package {api_java_pkg};",
        "",
        "import com.icoolkj.platform.common.model.page.PageResult;",
        "import com.icoolkj.platform.common.model.response.ApiResponse;",
        f"import {ctx.pkg}.dto.{ctx.page_query_name};",
    ]
    if ctx.crud == "full":
        lines += [
            f"import {ctx.pkg}.dto.{ctx.create_request_name};",
            f"import {ctx.pkg}.dto.{ctx.update_request_name};",
        ]
    lines += [
        f"import {ctx.pkg}.service.{svc};",
        f"import {ctx.pkg}.vo.{ctx.vo_name};",
        "import jakarta.validation.Valid;",
        "import org.springframework.web.bind.annotation.DeleteMapping;",
        "import org.springframework.web.bind.annotation.GetMapping;",
        "import org.springframework.web.bind.annotation.PathVariable;",
        "import org.springframework.web.bind.annotation.PostMapping;",
        "import org.springframework.web.bind.annotation.PutMapping;",
        "import org.springframework.web.bind.annotation.RequestBody;",
        "import org.springframework.web.bind.annotation.RequestMapping;",
        "import org.springframework.web.bind.annotation.RestController;",
        "",
        f"/** {ctx.comment}管理 */",
        "@RestController",
        f'@RequestMapping("{ctx.route_path}")',
        f"public class {ctx.controller_name} {{",
        "",
        f"    private final {svc} {svc_var};",
        "",
        f"    public {ctx.controller_name}({svc} {svc_var}) {{",
        f"        this.{svc_var} = {svc_var};",
        "    }",
        "",
        '    @GetMapping("/page")',
        f"    public ApiResponse<PageResult<{ctx.vo_name}>> page({ctx.page_query_name} query) {{",
        f"        return ApiResponse.ok({svc_var}.page(query));",
        "    }",
        "",
        '    @GetMapping("/{id}")',
        f"    public ApiResponse<{ctx.vo_name}> getById(@PathVariable String id) {{",
        f"        return ApiResponse.ok({svc_var}.getById(id));",
        "    }",
        "",
    ]
    if ctx.crud == "full":
        lines += [
            '    @PostMapping',
            f"    public ApiResponse<String> create(@Valid @RequestBody {ctx.create_request_name} request) {{",
            f"        return ApiResponse.ok({svc_var}.create(request));",
            "    }",
            "",
            '    @PutMapping',
            f"    public ApiResponse<Void> update(@Valid @RequestBody {ctx.update_request_name} request) {{",
            f"        {svc_var}.update(request);",
            "        return ApiResponse.ok();",
            "    }",
            "",
            '    @DeleteMapping("/{id}")',
            "    public ApiResponse<Void> deleteById(@PathVariable String id) {",
            f"        {svc_var}.deleteById(id);",
            "        return ApiResponse.ok();",
            "    }",
            "",
        ]
    lines += ["}", ""]
    return "\n".join(lines)


def render_frontend_api(ctx: TableContext) -> str:
    return "\n".join([
        f"// AUTO-GENERATED — {ctx.comment} API ({ctx.table})",
        f"// Route: {ctx.route_path}",
        "import request from '@/utils/request'",
        "",
        f"export function page{ctx.biz_name}(params) {{",
        f"  return request.get('{ctx.route_path}/page', {{ params }})",
        "}",
        "",
        f"export function get{ctx.biz_name}(id) {{",
        f"  return request.get(`${{ '{ctx.route_path}' }}/${{id}}`)",
        "}",
        "",
        *(["export function create" + ctx.biz_name + "(data) {",
           f"  return request.post('{ctx.route_path}', data)",
           "}",
           "",
           "export function update" + ctx.biz_name + "(data) {",
           f"  return request.put('{ctx.route_path}', data)",
           "}",
           "",
           "export function delete" + ctx.biz_name + "(id) {",
           f"  return request.delete(`${{ '{ctx.route_path}' }}/${{id}}`)",
           "}",
           ""] if ctx.crud == "full" else []),
    ])
