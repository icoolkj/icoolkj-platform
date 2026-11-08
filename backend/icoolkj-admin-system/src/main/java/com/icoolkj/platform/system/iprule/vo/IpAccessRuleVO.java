package com.icoolkj.platform.system.iprule.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** IP访问规则列表/详情 */
@Data
public class IpAccessRuleVO {
    private String id;
    private String ruleName;
    private String ipAddress;
    private String matchType;
    private String ruleType;
    private String applyScope;
    private Integer priorityNo;
    private String status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String remark;
}
