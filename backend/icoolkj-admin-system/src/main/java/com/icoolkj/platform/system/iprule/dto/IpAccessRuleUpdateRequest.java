package com.icoolkj.platform.system.iprule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

/** 修改IP访问规则 */
@Data
public class IpAccessRuleUpdateRequest {

    @NotBlank(message = "{validation.id.required}")
    private String id;
    @NotBlank(message = "{gen.ipAccessRule.ruleName.required}")
    @Size(max = 100, message = "{gen.ipAccessRule.ruleName.maxLength}")
    private String ruleName;
    @NotBlank(message = "{gen.ipAccessRule.ipAddress.required}")
    @Size(max = 255, message = "{gen.ipAccessRule.ipAddress.maxLength}")
    private String ipAddress;
    @NotBlank(message = "{gen.ipAccessRule.matchType.required}")
    @Size(max = 1, message = "{gen.ipAccessRule.matchType.maxLength}")
    private String matchType;
    @NotBlank(message = "{gen.ipAccessRule.ruleType.required}")
    @Size(max = 1, message = "{gen.ipAccessRule.ruleType.maxLength}")
    private String ruleType;
    @NotBlank(message = "{gen.ipAccessRule.applyScope.required}")
    @Size(max = 1, message = "{gen.ipAccessRule.applyScope.maxLength}")
    private String applyScope;
    @NotNull(message = "{gen.ipAccessRule.priorityNo.required}")
    private Integer priorityNo;
    @NotBlank(message = "{gen.ipAccessRule.status.required}")
    @Size(max = 1, message = "{gen.ipAccessRule.status.maxLength}")
    private String status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    @Size(max = 500, message = "{gen.ipAccessRule.remark.maxLength}")
    private String remark;
}
