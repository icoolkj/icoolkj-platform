package com.icoolkj.platform.oa.vo;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/** 租户映射发布齐套检查（向导 Step4 / 门禁） */
@Data
public class OaTenantPublishReadinessVO {

    private boolean ready;
    private boolean fieldsReady;
    private boolean modelSynced;
    private boolean modelDeployed;
    private boolean approversReady;
    private String modelId;
    private String modelKey;
    private Integer modelVersionNo;
    private List<String> userTaskKeys = new ArrayList<>();
    private List<String> missingApproverTaskKeys = new ArrayList<>();
    private List<String> messages = new ArrayList<>();
}
