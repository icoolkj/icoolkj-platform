package com.icoolkj.platform.system.workbench.vo;

import java.util.List;
import lombok.Data;

/** 工作台头部信息 */
@Data
public class WorkbenchHeaderVO {
    private String nickname;
    private String tenantId;
    private String tenantName;
    private String tenantCode;
    private String deptName;
    private String postLabel;
    private boolean platformOperator;
}
