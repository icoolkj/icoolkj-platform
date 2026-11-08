package com.icoolkj.platform.oa.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OaFieldDefVO {

    private String id;
    private String fieldCode;
    private String fieldName;
    private String fieldAlias;
    private String fieldType;
    private String componentType;
    private String dictType;
    private String fieldRequired;
    private Integer sortNo;
    private String fieldGroup;
    private Integer colSpan;
    private String defaultValue;
    private Integer maxLength;
    private String platformLocked;
    private String usedInCondition;
    private String usedInList;
    private String promoteSlot;
    private String listSlotSync;
    private String originType;
    private String status;
    private String remark;
    private String sourcePlatformFieldId;

    /**
     * 运行态：当前任务节点字段权限 R/W/H（不入库快照；由 approve-detail 注入）。
     */
    private String fieldPerm;
}
