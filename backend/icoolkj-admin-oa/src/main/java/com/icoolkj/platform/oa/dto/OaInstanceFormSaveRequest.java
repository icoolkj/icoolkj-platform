package com.icoolkj.platform.oa.dto;

import jakarta.validation.constraints.Size;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;

/** OA 动态表单保存 */
@Data
public class OaInstanceFormSaveRequest {

    /** 更新时必填 */
    private String id;

    /** 按模板字段编码提交的表单值 */
    private Map<String, Object> formData = new LinkedHashMap<>();

    @Size(max = 500)
    private String remark;
}
