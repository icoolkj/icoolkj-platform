package com.icoolkj.platform.system.job.service.impl;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.job.ManagedJobTriggerService;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.system.job.convert.SysJobConvert;
import com.icoolkj.platform.system.job.domain.SysJobEntity;
import com.icoolkj.platform.system.job.dto.SysJobPageQuery;
import com.icoolkj.platform.system.job.dto.SysJobUpdateRequest;
import com.icoolkj.platform.system.job.mapper.SysJobMapper;
import com.icoolkj.platform.system.job.service.SysJobService;
import com.icoolkj.platform.system.job.vo.SysJobVO;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class SysJobServiceImpl implements SysJobService {

    private final SysJobMapper sysJobMapper;
    private final ManagedJobTriggerService managedJobTriggerService;

    public SysJobServiceImpl(SysJobMapper sysJobMapper, ManagedJobTriggerService managedJobTriggerService) {
        this.sysJobMapper = sysJobMapper;
        this.managedJobTriggerService = managedJobTriggerService;
    }

    @Override
    public PageResult<SysJobVO> page(SysJobPageQuery query) {
        long total = sysJobMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<SysJobEntity> list = sysJobMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        List<SysJobVO> rows = list.stream().map(this::toVoWithRuntime).toList();
        return PageResult.of(rows, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public List<SysJobVO> listWithRuntimeStatus() {
        SysJobPageQuery query = new SysJobPageQuery();
        query.setPageNo(1L);
        query.setPageSize(200L);
        return page(query).getList();
    }

    @Override
    public SysJobVO getById(String id) {
        SysJobEntity entity = sysJobMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.job");
        }
        return toVoWithRuntime(entity);
    }

    @Override
    public SysJobEntity requireByJobCode(String jobCode) {
        SysJobEntity entity = sysJobMapper.selectByJobCode(jobCode);
        if (entity == null) {
            throw new BusinessException("error.job.unknown");
        }
        return entity;
    }

    @Override
    public void update(SysJobUpdateRequest request) {
        SysJobEntity entity = sysJobMapper.selectById(request.getId());
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.job");
        }
        String cron = request.getCronExpression().trim();
        validateCron(cron);
        if (!CommonConstants.STATUS_NORMAL.equals(request.getStatus().trim())
                && !CommonConstants.STATUS_DISABLE.equals(request.getStatus().trim())) {
            throw new BusinessException("error.job.status_invalid");
        }
        entity.setCronExpression(cron);
        entity.setStatus(request.getStatus().trim());
        if (StringUtils.hasText(request.getAllowConcurrent())) {
            entity.setAllowConcurrent(request.getAllowConcurrent().trim());
        }
        if (StringUtils.hasText(request.getMisfirePolicy())) {
            entity.setMisfirePolicy(request.getMisfirePolicy().trim());
        }
        entity.setRemark(request.getRemark());
        AuditFieldHelper.fillUpdate(entity);
        sysJobMapper.updateById(entity);
    }

    @Override
    public void changeStatus(String id, String status) {
        SysJobEntity entity = sysJobMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.job");
        }
        if (!CommonConstants.STATUS_NORMAL.equals(status) && !CommonConstants.STATUS_DISABLE.equals(status)) {
            throw new BusinessException("error.job.status_invalid");
        }
        entity.setStatus(status);
        AuditFieldHelper.fillUpdate(entity);
        sysJobMapper.updateById(entity);
    }

    private static void validateCron(String cron) {
        try {
            CronExpression.parse(cron);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("error.job.cron_invalid");
        }
    }

    private SysJobVO toVoWithRuntime(SysJobEntity entity) {
        SysJobVO vo = SysJobConvert.toVo(entity);
        Map<Object, Object> status = managedJobTriggerService.status(entity.getJobCode());
        if (status != null && !status.isEmpty()) {
            vo.setLastStatus(str(status.get("lastStatus")));
            vo.setLastMessage(str(status.get("lastMessage")));
            vo.setLastTriggerAt(str(status.get("lastTriggerAt")));
            vo.setLastStartedAt(str(status.get("lastStartedAt")));
            vo.setLastFinishedAt(str(status.get("lastFinishedAt")));
        }
        return vo;
    }

    private static String str(Object v) {
        return v == null ? null : String.valueOf(v);
    }
}
