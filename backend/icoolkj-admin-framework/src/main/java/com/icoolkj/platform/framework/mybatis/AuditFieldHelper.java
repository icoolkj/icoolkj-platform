package com.icoolkj.platform.framework.mybatis;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * Fills audit columns for hand-written MyBatis XML mappers.
 * <p>
 * MyBatis-Plus {@link MetaObjectHandlerImpl} does not run on custom {@code insert}/{@code updateById} statements.
 */
public final class AuditFieldHelper {

    private static final Integer NOT_DELETED = 0;
    private static final Integer DELETED = 1;

    private AuditFieldHelper() {
    }

    public static void fillInsert(BaseEntity entity) {
        if (entity == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        String operator = operatorId();
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        if (entity.getUpdatedAt() == null) {
            entity.setUpdatedAt(now);
        }
        if (!StringUtils.hasText(entity.getCreatedBy())) {
            entity.setCreatedBy(operator);
        }
        if (!StringUtils.hasText(entity.getUpdatedBy())) {
            entity.setUpdatedBy(operator);
        }
        if (entity.getDeleted() == null) {
            entity.setDeleted(NOT_DELETED);
        }
    }

    public static void fillUpdate(BaseEntity entity) {
        if (entity == null) {
            return;
        }
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(operatorId());
    }

    public static void markDeleted(BaseEntity entity) {
        if (entity == null) {
            return;
        }
        entity.setDeleted(DELETED);
        fillUpdate(entity);
    }

    public static String operatorId() {
        String userId = SecurityContextHelper.getUserId();
        return StringUtils.hasText(userId) ? userId : CommonConstants.SYSTEM_USER_ID;
    }
}
