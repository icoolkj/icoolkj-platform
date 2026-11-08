package com.icoolkj.platform.workflow.vo;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 流程模型详情 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WorkflowModelDetailVO extends WorkflowModelVO {

    private String bpmnXml;
    /** 已发布版本 design 快照（优先 version.design_json） */
    private String designJson;
    private List<WorkflowModelVersionVO> versions;
}
