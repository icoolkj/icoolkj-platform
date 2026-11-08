package com.icoolkj.platform.oa.vo;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/** OA 动态表单 schema */
@Data
public class OaInstanceFormSchemaVO {

    private String categoryCode;
    private String templateName;
    private String tenantTemplateId;
    private String tenantTplVerId;
    private Integer versionNo;
    private List<OaFieldDefVO> fields = new ArrayList<>();
    /** 模板未声明 remark 时，前端附加系统备注字段 */
    private Boolean appendRemark;
    /** 已发布布局快照；空则前端回落 OaDynamicForm */
    private String layoutSnapshotJson;
}
