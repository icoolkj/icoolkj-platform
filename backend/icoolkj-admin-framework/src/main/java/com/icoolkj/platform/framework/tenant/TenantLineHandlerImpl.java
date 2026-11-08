package com.icoolkj.platform.framework.tenant;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.icoolkj.platform.common.constant.TenantConstants;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Column;

import java.util.List;

/**
 * MyBatis-Plus tenant line handler backed by {@link TenantContextHolder}.
 */
public class TenantLineHandlerImpl implements TenantLineHandler {

    private final TenantProperties tenantProperties;

    public TenantLineHandlerImpl(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    @Override
    public Expression getTenantId() {
        String tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            return new StringValue(TenantConstants.MISSING_TENANT_ID);
        }
        return new StringValue(tenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }

    @Override
    public boolean ignoreTable(String tableName) {
        if (TenantContextHolder.isIgnoreTenant()) {
            return true;
        }
        if (tenantProperties.getIgnoreTables() == null || tenantProperties.getIgnoreTables().isEmpty()) {
            return false;
        }
        return tenantProperties.getIgnoreTables().stream()
                .anyMatch(table -> table.equalsIgnoreCase(tableName));
    }

    @Override
    public boolean ignoreInsert(List<Column> columns, String tenantIdColumn) {
        return columns.stream()
                .anyMatch(column -> column.getColumnName().equalsIgnoreCase(tenantIdColumn));
    }
}
