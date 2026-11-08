    <select id="selectActiveRules" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM sys_ip_access_rule
        WHERE deleted = 0 AND status = '0'
          AND tenant_id IN (#{platformTenantId}, #{tenantId})
          AND (start_at IS NULL OR start_at &lt;= #{now})
          AND (end_at IS NULL OR end_at &gt;= #{now})
        ORDER BY priority_no DESC
    </select>
