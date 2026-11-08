package com.icoolkj.platform.oa.vo;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class OaInstanceVO {

    private String id;
    private String categoryCode;
    private String instanceTitle;
    private String approveStatus;
    private String processInstanceId;
    private String processDefinitionKey;
    private String starterUserId;
    private LocalDateTime submittedAt;
    private LocalDateTime finishedAt;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** 完整动态表单数据 */
    private Map<String, Object> formData = new LinkedHashMap<>();
    /** 单据锁定的字段定义快照（编辑/审批用） */
    private List<OaFieldDefVO> fields;
    /** 单据锁定的布局快照；空则回落纵向动态表单 */
    private String layoutSnapshotJson;
}
