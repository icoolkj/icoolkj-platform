package com.icoolkj.platform.system.job.convert;

import com.icoolkj.platform.system.job.domain.SysJobEntity;
import com.icoolkj.platform.system.job.domain.SysJobLogEntity;
import com.icoolkj.platform.system.job.vo.SysJobLogVO;
import com.icoolkj.platform.system.job.vo.SysJobVO;

public final class SysJobConvert {

    private SysJobConvert() {
    }

    public static SysJobVO toVo(SysJobEntity entity) {
        if (entity == null) {
            return null;
        }
        SysJobVO vo = new SysJobVO();
        vo.setId(entity.getId());
        vo.setJobCode(entity.getJobCode());
        vo.setJobName(entity.getJobName());
        vo.setJobGroup(entity.getJobGroup());
        vo.setCronExpression(entity.getCronExpression());
        vo.setMisfirePolicy(entity.getMisfirePolicy());
        vo.setAllowConcurrent(entity.getAllowConcurrent());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    public static SysJobLogVO toLogVo(SysJobLogEntity entity) {
        if (entity == null) {
            return null;
        }
        SysJobLogVO vo = new SysJobLogVO();
        vo.setId(entity.getId());
        vo.setJobId(entity.getJobId());
        vo.setJobCode(entity.getJobCode());
        vo.setTriggerType(entity.getTriggerType());
        vo.setTriggerUserId(entity.getTriggerUserId());
        vo.setStatus(entity.getStatus());
        vo.setJobMessage(entity.getJobMessage());
        vo.setExceptionInfo(entity.getExceptionInfo());
        vo.setRequestParam(entity.getRequestParam());
        vo.setResultSummary(entity.getResultSummary());
        vo.setStartedAt(entity.getStartedAt());
        vo.setFinishedAt(entity.getFinishedAt());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
