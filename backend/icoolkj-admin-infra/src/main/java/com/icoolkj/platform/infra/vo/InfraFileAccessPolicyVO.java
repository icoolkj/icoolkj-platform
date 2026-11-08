package com.icoolkj.platform.infra.vo;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InfraFileAccessPolicyVO {

    private String id;
    private String bizTypePattern;
    private String authMode;
    private String tenantScope;
    private String visibility;
    private String anonPurposes;
    private String permAnyOf;
    private Integer sortNo;
    private String enabled;
    private String remark;
    private LocalDateTime updatedAt;
    /** Runtime-pinned pattern (excel / oa_instance); UI must not edit/delete. */
    private boolean pinned;
}
