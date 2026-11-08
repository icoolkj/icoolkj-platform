package com.icoolkj.platform.system.job.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.system.job.dto.SysJobLogPageQuery;
import com.icoolkj.platform.system.job.vo.SysJobLogVO;

public interface SysJobLogService {

    PageResult<SysJobLogVO> page(SysJobLogPageQuery query);

    SysJobLogVO getById(String id);
}
