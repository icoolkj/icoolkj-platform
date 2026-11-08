    <select id="selectByTenantAndKey" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM sys_config
        WHERE tenant_id = #{tenantId} AND config_key = #{configKey} AND deleted = 0
        LIMIT 1
    </select>
