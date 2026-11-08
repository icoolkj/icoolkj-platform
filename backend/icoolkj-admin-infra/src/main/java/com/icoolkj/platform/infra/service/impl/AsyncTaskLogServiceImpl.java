package com.icoolkj.platform.infra.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.infra.convert.AsyncTaskLogConvert;
import com.icoolkj.platform.infra.domain.AsyncTaskLogEntity;
import com.icoolkj.platform.infra.dto.AsyncTaskLogPageQuery;
import com.icoolkj.platform.infra.mapper.AsyncTaskLogMapper;
import com.icoolkj.platform.infra.service.AsyncTaskLogService;
import com.icoolkj.platform.infra.vo.AsyncTaskLogVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/** 异步任务日志服务实现 */
@Service
public class AsyncTaskLogServiceImpl implements AsyncTaskLogService {

    private final AsyncTaskLogMapper asyncTaskLogMapper;

    public AsyncTaskLogServiceImpl(AsyncTaskLogMapper asyncTaskLogMapper) {
        this.asyncTaskLogMapper = asyncTaskLogMapper;
    }

    @Override
    public PageResult<AsyncTaskLogVO> page(AsyncTaskLogPageQuery query) {
        long total = asyncTaskLogMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<AsyncTaskLogEntity> list = asyncTaskLogMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        List<AsyncTaskLogVO> rows = list.stream().map(AsyncTaskLogConvert::toVo).toList();
        return PageResult.of(rows, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public AsyncTaskLogVO getById(String id) {
        AsyncTaskLogEntity entity = asyncTaskLogMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.async_task_log");
        }
        return AsyncTaskLogConvert.toVo(entity);
    }

}
