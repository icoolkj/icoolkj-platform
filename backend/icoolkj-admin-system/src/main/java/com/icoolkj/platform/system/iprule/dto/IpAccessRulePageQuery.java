package com.icoolkj.platform.system.iprule.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** IP访问规则分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IpAccessRulePageQuery extends PageQuery {
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
