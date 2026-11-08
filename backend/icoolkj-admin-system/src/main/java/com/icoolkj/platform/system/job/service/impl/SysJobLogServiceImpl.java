package com.icoolkj.platform.system.job.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.system.job.convert.SysJobConvert;
import com.icoolkj.platform.system.job.domain.SysJobLogEntity;
import com.icoolkj.platform.system.job.dto.SysJobLogPageQuery;
import com.icoolkj.platform.system.job.mapper.SysJobLogMapper;
import com.icoolkj.platform.system.job.service.SysJobLogService;
import com.icoolkj.platform.system.job.vo.SysJobLogVO;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class SysJobLogServiceImpl implements SysJobLogService {

    private final SysJobLogMapper sysJobLogMapper;

    public SysJobLogServiceImpl(SysJobLogMapper sysJobLogMapper) {
        this.sysJobLogMapper = sysJobLogMapper;
    }

    @Override
    public PageResult<SysJobLogVO> page(SysJobLogPageQuery query) {
        long total = sysJobLogMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<SysJobLogEntity> list = sysJobLogMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        List<SysJobLogVO> rows = list.stream().map(SysJobConvert::toLogVo).toList();
        return PageResult.of(rows, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public SysJobLogVO getById(String id) {
        SysJobLogEntity entity = sysJobLogMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.job_log");
        }
        return SysJobConvert.toLogVo(entity);
    }
}
