package com.icoolkj.platform.oa.service;

import com.icoolkj.platform.oa.dto.OaInstanceFormSaveRequest;
import com.icoolkj.platform.oa.vo.OaInstanceApproveDetailVO;
import com.icoolkj.platform.oa.vo.OaInstanceFormSchemaVO;
import com.icoolkj.platform.oa.vo.OaInstanceVO;

public interface OaInstanceService {

    OaInstanceVO getById(String id);

    /** 新建表单 schema（当前租户该分类已发布模板） */
    OaInstanceFormSchemaVO getCreateFormSchema(String categoryCode);

    String create(String categoryCode, OaInstanceFormSaveRequest request);

    void update(OaInstanceFormSaveRequest request);

    void deleteById(String id);

    /** Close document with approval history (no delete). */
    void close(String id);

    String submitApprove(String id);

    OaInstanceApproveDetailVO getApproveDetail(String id);
}
