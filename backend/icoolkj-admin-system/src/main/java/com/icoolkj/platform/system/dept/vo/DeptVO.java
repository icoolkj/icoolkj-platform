package com.icoolkj.platform.system.dept.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 部门表列表/详情 */
@Data
public class DeptVO {
    private String id;
    private String parentId;
    private String ancestors;
    private String deptName;
    private String deptCode;
    private String leaderName;
    private String phone;
    private String email;
    private Integer sortNo;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
}
