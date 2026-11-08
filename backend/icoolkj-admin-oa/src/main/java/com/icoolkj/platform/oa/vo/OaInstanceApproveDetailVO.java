package com.icoolkj.platform.oa.vo;

import com.icoolkj.platform.workflow.vo.WorkflowInstanceDetailVO;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/** OA 统一审批页详情 */
@Data
public class OaInstanceApproveDetailVO {

    /** 业务单据 */
    private OaInstanceVO document;
    private String taskId;
    private String processInstanceId;
    private String approveStatus;
    /** 流程实例与轨迹 */
    private WorkflowInstanceDetailVO instance;
    /** 审批业务区动态字段 */
    private List<OaFieldDefVO> fields;
    private Map<String, Object> formData = new LinkedHashMap<>();
}
