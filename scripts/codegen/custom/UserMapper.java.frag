    UserEntity selectByTenantAndUsername(@Param("tenantId") String tenantId, @Param("username") String username);
    int resetLoginSuccess(@Param("id") String id, @Param("lastLoginIp") String lastLoginIp, @Param("lastLoginAt") java.time.LocalDateTime lastLoginAt);
    int updateLoginFailCount(@Param("id") String id, @Param("loginFailCount") int loginFailCount);
    int updateLockState(@Param("id") String id, @Param("loginFailCount") int loginFailCount, @Param("lockedUntil") java.time.LocalDateTime lockedUntil);
