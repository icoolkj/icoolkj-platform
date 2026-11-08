package com.icoolkj.platform.infra.vo;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InfraFileCatalogItemVO {

    private String profileCode;
    private String profileName;
    private String required;
    private Integer minNum;
    private Integer maxNum;
    private Integer sortNo;
    private String status;
    /** role → priority（越小越优先）；停用槽位解析时忽略 */
    private Map<String, Integer> chromeRoles;
    private InfraFileProfileVO resolvedProfile;
}
