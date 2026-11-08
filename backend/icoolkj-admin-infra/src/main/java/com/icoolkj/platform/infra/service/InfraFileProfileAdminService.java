package com.icoolkj.platform.infra.service;

import com.icoolkj.platform.infra.dto.InfraFileCatalogUpsertRequest;
import com.icoolkj.platform.infra.dto.InfraFileProfileBatchDeleteRequest;
import com.icoolkj.platform.infra.dto.InfraFileProfileUpsertRequest;
import com.icoolkj.platform.infra.vo.InfraFileCatalogVO;
import com.icoolkj.platform.infra.vo.InfraFileProfileVO;
import java.util.List;

public interface InfraFileProfileAdminService {

    List<InfraFileProfileVO> list(String categoryTag, String keyword, String targetTenantId);

    InfraFileProfileVO resolve(String profileCode, String targetTenantId);

    void upsert(InfraFileProfileUpsertRequest request);

    /** 删除租户规则覆盖，恢复平台规则 */
    void resetRuleOverride(String profileCode, String targetTenantId);

    /** 平台删除槽位（含规则）；租户禁止 */
    void deleteProfile(String profileCode, String targetTenantId);

    void batchDeleteProfiles(InfraFileProfileBatchDeleteRequest request);

    List<InfraFileCatalogVO> listCatalogs(String targetTenantId);

    InfraFileCatalogVO getCatalog(String catalogCode, String targetTenantId);

    void upsertCatalog(InfraFileCatalogUpsertRequest request);

    void deleteCatalog(String catalogCode, String targetTenantId);
}
