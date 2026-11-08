package com.icoolkj.platform.infra.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.infra.dto.AttachmentDemoBatchDeleteRequest;
import com.icoolkj.platform.infra.dto.AttachmentDemoCreateRequest;
import com.icoolkj.platform.infra.dto.AttachmentDemoPageQuery;
import com.icoolkj.platform.infra.dto.AttachmentDemoStatusUpdateRequest;
import com.icoolkj.platform.infra.dto.AttachmentDemoUpdateRequest;
import com.icoolkj.platform.infra.vo.AttachmentDemoVO;

/** 附件测试业务服务 */
public interface AttachmentDemoService {

    PageResult<AttachmentDemoVO> page(AttachmentDemoPageQuery query);

    AttachmentDemoVO getById(String id);

    String create(AttachmentDemoCreateRequest request);

    void update(AttachmentDemoUpdateRequest request);

    void updateStatus(AttachmentDemoStatusUpdateRequest request);

    void deleteById(String id);

    void batchDelete(AttachmentDemoBatchDeleteRequest request);
}
