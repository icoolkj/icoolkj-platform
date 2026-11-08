package com.icoolkj.platform.system.job.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.system.job.domain.SysJobEntity;
import com.icoolkj.platform.system.job.dto.SysJobPageQuery;
import com.icoolkj.platform.system.job.dto.SysJobUpdateRequest;
import com.icoolkj.platform.system.job.vo.SysJobVO;
import java.util.List;

public interface SysJobService {

    PageResult<SysJobVO> page(SysJobPageQuery query);

    List<SysJobVO> listWithRuntimeStatus();

    SysJobVO getById(String id);

    SysJobEntity requireByJobCode(String jobCode);

    void update(SysJobUpdateRequest request);

    void changeStatus(String id, String status);
}
