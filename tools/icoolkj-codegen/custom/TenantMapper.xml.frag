    <select id="selectByTenantCode" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM sys_tenant
        WHERE tenant_code = #{tenantCode} AND deleted = 0
        LIMIT 1
    </select>
