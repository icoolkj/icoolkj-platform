package com.icoolkj.platform.workflow.support;

import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.workflow.domain.WfCcReceiptEntity;
import com.icoolkj.platform.workflow.mapper.WfCcReceiptMapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 抄送已读回执：创建与标记 */
@Component
public class WorkflowCcReceiptSupport {

    private final WfCcReceiptMapper wfCcReceiptMapper;

    public WorkflowCcReceiptSupport(WfCcReceiptMapper wfCcReceiptMapper) {
        this.wfCcReceiptMapper = wfCcReceiptMapper;
    }

    public void ensureReceipt(String tenantId, String processInstanceId, String taskId, String userId) {
        if (!StringUtils.hasText(taskId) || !StringUtils.hasText(userId)) {
            return;
        }
        WfCcReceiptEntity existing = wfCcReceiptMapper.selectByTaskIdAndUserId(taskId, userId);
        if (existing != null) {
            return;
        }
        WfCcReceiptEntity row = new WfCcReceiptEntity();
        row.setId(IdGeneratorUtils.nextIdStr());
        row.setTenantId(StringUtils.hasText(tenantId) ? tenantId : TenantConstants.PLATFORM_TENANT_ID);
        row.setProcessInstanceId(processInstanceId);
        row.setTaskId(taskId);
        row.setUserId(userId);
        row.setReadAt(null);
        AuditFieldHelper.fillInsert(row);
        wfCcReceiptMapper.insert(row);
    }

    public void markRead(String taskId, String userId) {
        if (!StringUtils.hasText(taskId) || !StringUtils.hasText(userId)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        int updated = wfCcReceiptMapper.updateReadAt(taskId, userId, now);
        if (updated > 0) {
            return;
        }
        WfCcReceiptEntity row = new WfCcReceiptEntity();
        row.setId(IdGeneratorUtils.nextIdStr());
        row.setTenantId(TenantConstants.PLATFORM_TENANT_ID);
        row.setProcessInstanceId(null);
        row.setTaskId(taskId);
        row.setUserId(userId);
        row.setReadAt(now);
        AuditFieldHelper.fillInsert(row);
        wfCcReceiptMapper.insert(row);
    }

    public boolean isRead(String taskId, String userId) {
        if (!StringUtils.hasText(taskId) || !StringUtils.hasText(userId)) {
            return false;
        }
        WfCcReceiptEntity row = wfCcReceiptMapper.selectByTaskIdAndUserId(taskId, userId);
        return row != null && row.getReadAt() != null;
    }

    public Map<String, Boolean> readStatusByTaskIds(List<String> taskIds, String userId) {
        if (CollectionUtils.isEmpty(taskIds) || !StringUtils.hasText(userId)) {
            return Map.of();
        }
        List<WfCcReceiptEntity> rows = wfCcReceiptMapper.selectByTaskIdsAndUserId(taskIds, userId);
        Map<String, Boolean> out = new HashMap<>();
        for (WfCcReceiptEntity row : rows) {
            if (row != null && StringUtils.hasText(row.getTaskId())) {
                out.put(row.getTaskId(), row.getReadAt() != null);
            }
        }
        return out;
    }
}
