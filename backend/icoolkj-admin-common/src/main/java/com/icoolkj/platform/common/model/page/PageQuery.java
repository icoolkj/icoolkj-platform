package com.icoolkj.platform.common.model.page;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Base pagination parameters for list/page queries.
 */
@Data
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final long DEFAULT_PAGE_NO = 1L;
    private static final long DEFAULT_PAGE_SIZE = 20L;
    private static final long MAX_PAGE_SIZE = 500L;

    private Long pageNo = DEFAULT_PAGE_NO;
    private Long pageSize = DEFAULT_PAGE_SIZE;
    /** Optional tenant filter; when null, tenant line plugin applies current tenant. */
    private String tenantId;
    /** Server-only L3 SQL fragment; never bind from client. */
    @JsonIgnore
    private String dataScopeSql;

    public Long getPageNo() {
        return pageNo == null || pageNo < 1 ? DEFAULT_PAGE_NO : pageNo;
    }

    public Long getPageSize() {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    public long getOffset() {
        return (getPageNo() - 1) * getPageSize();
    }
}
