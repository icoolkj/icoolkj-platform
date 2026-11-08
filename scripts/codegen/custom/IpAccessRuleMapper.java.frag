    List<IpAccessRuleEntity> selectActiveRules(@Param("platformTenantId") String platformTenantId,
            @Param("tenantId") String tenantId, @Param("now") java.time.LocalDateTime now);
