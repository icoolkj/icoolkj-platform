package com.icoolkj.platform.system.dept.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 部门表分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeptPageQuery extends PageQuery {
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
}
