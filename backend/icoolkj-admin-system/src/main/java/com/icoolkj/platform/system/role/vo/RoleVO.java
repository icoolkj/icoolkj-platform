package com.icoolkj.platform.system.role.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 角色表列表/详情 */
@Data
public class RoleVO {
    private String id;
    private String roleName;
    private String roleCode;
    private String dataScope;
    private Integer sortNo;
    private String status;
    private String isSuperRole;
    private String remark;
    private LocalDateTime createdAt;
}
