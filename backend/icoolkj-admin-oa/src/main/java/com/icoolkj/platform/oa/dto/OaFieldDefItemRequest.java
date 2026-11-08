package com.icoolkj.platform.oa.dto;

import com.icoolkj.platform.common.constant.CommonConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OaFieldDefItemRequest {

    @NotBlank
    @Size(max = 64)
    private String fieldCode;

    @NotBlank
    @Size(max = 100)
    private String fieldName;

    @Size(max = 100)
    private String fieldAlias;

    @NotBlank
    @Size(max = 20)
    private String fieldType;

    @Size(max = 32)
    private String componentType;

    @Size(max = 64)
    private String dictType;

    @Size(max = 1)
    private String fieldRequired = CommonConstants.NO;

    private Integer sortNo = 0;

    @Size(max = 64)
    private String fieldGroup;

    @Min(1)
    @Max(2)
    private Integer colSpan = 1;

    @Size(max = 500)
    private String defaultValue;

    private Integer maxLength;

    @Size(max = 1)
    private String platformLocked = CommonConstants.NO;

    @Size(max = 1)
    private String usedInCondition = CommonConstants.NO;

    @Size(max = 1)
    private String usedInList = CommonConstants.NO;

    @Size(max = 32)
    private String promoteSlot;

    @Size(max = 1)
    private String listSlotSync = CommonConstants.NO;

    @Size(max = 1)
    private String status = CommonConstants.STATUS_NORMAL;

    @Size(max = 500)
    private String remark;
}
