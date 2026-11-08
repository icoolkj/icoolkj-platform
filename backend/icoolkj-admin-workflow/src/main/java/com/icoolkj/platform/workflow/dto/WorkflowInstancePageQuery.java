package com.icoolkj.platform.workflow.dto;

import com.icoolkj.platform.common.enums.YesNoEnum;
import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 流程实例分页查询（已发申请 / 租户监控） */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WorkflowInstancePageQuery extends PageQuery {

    private String bizTitle;
    private String bizType;
    private String status;
    /**
     * 仅未解析到办理人：Y/true/1；与字典 sys_yes_no 对齐用 Y。
     */
    private String unresolvedOnly;

    public boolean unresolvedOnlyEnabled() {
        return YesNoEnum.parseTruthy(unresolvedOnly);
    }
}
