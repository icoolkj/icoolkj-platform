package com.icoolkj.platform.infra.service;

import com.icoolkj.platform.infra.dto.InfraFileAccessPolicyUpsertRequest;
import com.icoolkj.platform.infra.vo.InfraFileAccessPolicyVO;
import java.util.List;

public interface InfraFileAccessPolicyAdminService {

    List<InfraFileAccessPolicyVO> list();

    /** 当前内存生效策略（内置 + 表覆盖合并结果） */
    List<InfraFileAccessPolicyVO> listEffective();

    InfraFileAccessPolicyVO getById(String id);

    void upsert(InfraFileAccessPolicyUpsertRequest request);

    void delete(String id);

    void refreshRegistry();
}
