    <select id="selectByTenantAndUsername" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM sys_user
        WHERE tenant_id = #{tenantId} AND username = #{username} AND deleted = 0
        LIMIT 1
    </select>
    <update id="resetLoginSuccess">
        UPDATE sys_user SET login_fail_count = 0, locked_until = NULL,
            last_login_ip = #{lastLoginIp}, last_login_at = #{lastLoginAt}, updated_at = NOW(3)
        WHERE id = #{id} AND deleted = 0
    </update>
    <update id="updateLoginFailCount">
        UPDATE sys_user SET login_fail_count = #{loginFailCount}, updated_at = NOW(3)
        WHERE id = #{id} AND deleted = 0
    </update>
    <update id="updateLockState">
        UPDATE sys_user SET login_fail_count = #{loginFailCount}, locked_until = #{lockedUntil}, updated_at = NOW(3)
        WHERE id = #{id} AND deleted = 0
    </update>
