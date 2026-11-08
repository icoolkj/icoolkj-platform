package com.icoolkj.platform.infra.vo;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InfraFileCatalogVO {

    private String catalogCode;
    private String catalogName;
    private Map<String, String> catalogNameI18n;
    private String status;
    private String remark;
    private boolean tenantOverride;
    private List<InfraFileCatalogItemVO> items;
}
