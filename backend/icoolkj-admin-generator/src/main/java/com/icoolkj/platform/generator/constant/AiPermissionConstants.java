package com.icoolkj.platform.generator.constant;

/** AI 助手权限码 — 与 sys_menu.permission_code 对齐 */
public final class AiPermissionConstants {

    private AiPermissionConstants() {
    }

    /** 查询：审计日志、会话列表、运行时就绪探测 */
    public static final String QUERY = "tool:ai:query";
    /** 调用 AI：建表、代码生成、对话发送 */
    public static final String GENERATE = "tool:ai:generate";
    /** 智能建表菜单/导航（API 仍校验 GENERATE） */
    public static final String TABLE = "tool:ai:table";
    /** 代码生成助手菜单/导航（API 仍校验 GENERATE） */
    public static final String CODEGEN = "tool:ai:codegen";
    /** 文生图 */
    public static final String IMAGE = "tool:ai:image";
}
