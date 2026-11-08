    <select id="selectPermissionCodesByMenuIds" resultType="java.lang.String">
        SELECT DISTINCT permission_code FROM sys_menu
        WHERE deleted = 0 AND status = '0' AND permission_code IS NOT NULL AND permission_code != ''
          AND id IN <foreach collection="menuIds" item="menuId" open="(" separator="," close=")">#{menuId}</foreach>
        ORDER BY permission_code
    </select>
    <select id="selectAllActivePermissionCodes" resultType="java.lang.String">
        SELECT DISTINCT permission_code FROM sys_menu
        WHERE deleted = 0 AND status = '0' AND permission_code IS NOT NULL AND permission_code != ''
        ORDER BY permission_code
    </select>
