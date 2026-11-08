package com.icoolkj.platform.oa.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.oa.dto.OaFieldDefBatchSaveRequest;
import com.icoolkj.platform.oa.dto.OaPlatformTemplateCreateRequest;
import com.icoolkj.platform.oa.dto.OaPlatformTemplatePageQuery;
import com.icoolkj.platform.oa.dto.OaPlatformTemplateUpdateRequest;
import com.icoolkj.platform.oa.dto.OaVersionCreateRequest;
import com.icoolkj.platform.oa.dto.OaVersionPublishRequest;
import com.icoolkj.platform.oa.vo.OaPlatformTemplateDetailVO;
import com.icoolkj.platform.oa.vo.OaPlatformTemplateVO;
import com.icoolkj.platform.oa.vo.OaTemplateVersionVO;
import com.icoolkj.platform.workflow.vo.WorkflowModelOptionVO;
import java.util.List;

/** OA 平台模板服务（头表 CRUD + 版本扩展） */
public interface OaPlatformTemplateService {

    PageResult<OaPlatformTemplateVO> page(OaPlatformTemplatePageQuery query);

    OaPlatformTemplateDetailVO getById(String id);

    List<WorkflowModelOptionVO> listModelOptions();

    List<OaTemplateVersionVO> listVersions(String templateId);

    OaTemplateVersionVO getVersionDetail(String versionId);

    String create(OaPlatformTemplateCreateRequest request);

    void update(OaPlatformTemplateUpdateRequest request);

    String createDraftVersion(OaVersionCreateRequest request);

    void saveFieldDefs(OaFieldDefBatchSaveRequest request);

    void publishVersion(OaVersionPublishRequest request);
}
