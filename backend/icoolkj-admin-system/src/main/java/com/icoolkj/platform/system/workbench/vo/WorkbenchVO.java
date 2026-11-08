package com.icoolkj.platform.system.workbench.vo;

import java.util.List;
import lombok.Data;

/** 租户工作台聚合数据 */
@Data
public class WorkbenchVO {
    private WorkbenchHeaderVO header;
    private List<WorkbenchStatVO> stats;
    private Long unreadSiteMessageCount;
    private List<WorkbenchLoginLogItemVO> recentLoginLogs;
    private List<WorkbenchOperationLogItemVO> recentOperationLogs;
}
