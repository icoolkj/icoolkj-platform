package com.icoolkj.platform.framework.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantContextHolder;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Auto-fills audit and tenant fields for MyBatis-Plus built-in CRUD.
 * <p>
 * Custom XML mappers use {@link AuditFieldInterceptor} + {@link AuditFieldHelper} instead.
 */
@Component
public class MetaObjectHandlerImpl implements MetaObjectHandler {

    private static final Integer DEFAULT_DELETED = 0;

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String currentUserId = getCurrentUserId();

        strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
        strictInsertFill(metaObject, "createdBy", String.class, currentUserId);
        strictInsertFill(metaObject, "updatedBy", String.class, currentUserId);
        strictInsertFill(metaObject, "deleted", Integer.class, DEFAULT_DELETED);

        if (hasGetter(metaObject, "tenantId")
                && TenantContextHolder.hasTenant()
                && !TenantContextHolder.isIgnoreTenant()) {
            strictInsertFill(metaObject, "tenantId", String.class, TenantContextHolder.getTenantId());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        strictUpdateFill(metaObject, "updatedBy", String.class, getCurrentUserId());
    }

    private String getCurrentUserId() {
        String userId = SecurityContextHelper.getUserId();
        return userId == null ? CommonConstants.SYSTEM_USER_ID : userId;
    }

    private boolean hasGetter(MetaObject metaObject, String fieldName) {
        return metaObject.hasGetter(fieldName);
    }
}
