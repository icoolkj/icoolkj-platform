package com.icoolkj.platform.system.workbench.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 工作台最近操作 */
@Data
public class WorkbenchOperationLogItemVO {
    private String moduleName;
    private String businessType;
    private String operatorName;
    private String operationStatus;
    private LocalDateTime operationAt;
}
