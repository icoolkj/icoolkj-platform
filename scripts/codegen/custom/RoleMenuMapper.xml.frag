    <select id="selectMenuIdsByTenantAndRoleIds" resultType="java.lang.String">
        SELECT DISTINCT menu_id FROM sys_role_menu
        WHERE tenant_id = #{tenantId}
          AND role_id IN <foreach collection="roleIds" item="roleId" open="(" separator="," close=")">#{roleId}</foreach>
    </select>
