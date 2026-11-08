package com.icoolkj.platform.oa.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.oa.dto.OaFieldDefBatchSaveRequest;
import com.icoolkj.platform.oa.dto.OaTenantEnableRequest;
import com.icoolkj.platform.oa.dto.OaTenantTemplatePageQuery;
import com.icoolkj.platform.oa.dto.OaTenantTemplateUpdateRequest;
import com.icoolkj.platform.oa.dto.OaVersionCreateRequest;
import com.icoolkj.platform.oa.dto.OaVersionPublishRequest;
import com.icoolkj.platform.oa.vo.OaPlatformTemplateOptionVO;
import com.icoolkj.platform.oa.vo.OaTenantPublishReadinessVO;
import com.icoolkj.platform.oa.vo.OaTenantSyncPreviewVO;
import com.icoolkj.platform.oa.vo.OaTenantTemplateDetailVO;
import com.icoolkj.platform.oa.vo.OaTenantTemplateVO;
import com.icoolkj.platform.oa.vo.OaTemplateVersionVO;
import java.util.List;

/** OA 租户模板服务（头表 CRUD + 启用/版本扩展） */
public interface OaTenantTemplateService {

    PageResult<OaTenantTemplateVO> page(OaTenantTemplatePageQuery query);

    List<OaPlatformTemplateOptionVO> listAvailablePlatformTemplates();

    OaTenantTemplateDetailVO getById(String id);

    List<OaTemplateVersionVO> listVersions(String templateId);

    OaTemplateVersionVO getVersionDetail(String versionId);

    String enable(OaTenantEnableRequest request);

    void update(OaTenantTemplateUpdateRequest request);

    String createDraftVersion(OaVersionCreateRequest request);

    /** 同步前比对预览（有无变动、字段差异） */
    OaTenantSyncPreviewVO previewSyncFromPlatform(String templateId);

    /** 同步平台最新已发布字段到租户草稿，返回草稿版本 ID */
    String syncFromPlatform(String templateId);

    void saveFieldDefs(OaFieldDefBatchSaveRequest request);

    /** 发布齐套检查（表单字段 / 流程同步部署 / 审批人覆盖） */
    OaTenantPublishReadinessVO getPublishReadiness(String versionId);

    void publishVersion(OaVersionPublishRequest request);
}
