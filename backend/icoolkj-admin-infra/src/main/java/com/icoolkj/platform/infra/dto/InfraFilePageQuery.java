package com.icoolkj.platform.infra.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 文件分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InfraFilePageQuery extends PageQuery {

    private String tenantId;
    private String bizType;
    private String bizId;
    private String originalName;
    private String storageType;
    /**
     * When set, excel-import / excel-export staging rows are limited to this owner
     * (submitter-only list parity with download).
     */
    private String excelStagingOwnerUserId;
}
