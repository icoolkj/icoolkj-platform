package com.icoolkj.platform.common.model.audit;

import lombok.Data;

/** Result of walking a per-tenant audit hash chain. */
@Data
public class AuditChainVerifyVO {

    private String chainName;
    private String tenantId;
    private boolean valid;
    private int checkedCount;
    private String breakId;
    private String message;
}
