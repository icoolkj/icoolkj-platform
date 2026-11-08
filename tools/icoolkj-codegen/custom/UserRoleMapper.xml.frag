    <select id="selectRoleIdsByTenantAndUser" resultType="java.lang.String">
        SELECT DISTINCT role_id FROM sys_user_role
        WHERE tenant_id = #{tenantId} AND user_id = #{userId}
    </select>
