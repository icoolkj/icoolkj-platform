package com.icoolkj.platform.workflow.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 修复历史已办 ASSIGNEE_ 为空：create 监听里 {@code DelegateTask#setAssignee} 不写 HI，
 * 导致「我已处理」按办理人查不到（含条件分支内节点，不限于加签）。
 */
@Component
public class WorkflowHistoricAssigneeBackfill {

    private static final Logger log = LoggerFactory.getLogger(WorkflowHistoricAssigneeBackfill.class);

    private final JdbcTemplate jdbcTemplate;

    public WorkflowHistoricAssigneeBackfill(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void backfillNullHistoricAssignees() {
        try {
            int byKeyUserId = jdbcTemplate.update("""
                    UPDATE ACT_HI_TASKINST t
                    INNER JOIN ACT_HI_VARINST v
                      ON v.PROC_INST_ID_ = t.PROC_INST_ID_
                     AND v.NAME_ = CONCAT(t.TASK_DEF_KEY_, 'UserId')
                     AND v.TEXT_ IS NOT NULL
                     AND v.TEXT_ <> ''
                    SET t.ASSIGNEE_ = v.TEXT_
                    WHERE t.END_TIME_ IS NOT NULL
                      AND (t.ASSIGNEE_ IS NULL OR t.ASSIGNEE_ = '')
                    """);
            // deptLeaderApprove + variableName=deptLeaderUserId 等：去掉尾缀 Approve 再拼 UserId
            int byStrippedApprove = jdbcTemplate.update("""
                    UPDATE ACT_HI_TASKINST t
                    INNER JOIN ACT_HI_VARINST v
                      ON v.PROC_INST_ID_ = t.PROC_INST_ID_
                     AND t.TASK_DEF_KEY_ LIKE '%Approve'
                     AND CHAR_LENGTH(t.TASK_DEF_KEY_) > 7
                     AND v.NAME_ = CONCAT(SUBSTRING(t.TASK_DEF_KEY_, 1, CHAR_LENGTH(t.TASK_DEF_KEY_) - 7), 'UserId')
                     AND v.TEXT_ IS NOT NULL
                     AND v.TEXT_ <> ''
                    SET t.ASSIGNEE_ = v.TEXT_
                    WHERE t.END_TIME_ IS NOT NULL
                      AND (t.ASSIGNEE_ IS NULL OR t.ASSIGNEE_ = '')
                    """);
            // 设计器自定义 variableName：按 wf_approver_rule 对齐
            int byRuleVar = jdbcTemplate.update("""
                    UPDATE ACT_HI_TASKINST t
                    INNER JOIN ACT_HI_VARINST mid
                      ON mid.PROC_INST_ID_ = t.PROC_INST_ID_ AND mid.NAME_ = 'modelId' AND mid.TEXT_ IS NOT NULL
                    INNER JOIN wf_approver_rule r
                      ON r.model_id = mid.TEXT_
                     AND r.task_definition_key = t.TASK_DEF_KEY_
                     AND r.deleted = 0
                     AND r.variable_name IS NOT NULL
                     AND r.variable_name <> ''
                    INNER JOIN ACT_HI_VARINST v
                      ON v.PROC_INST_ID_ = t.PROC_INST_ID_
                     AND v.NAME_ = r.variable_name
                     AND v.TEXT_ IS NOT NULL
                     AND v.TEXT_ <> ''
                    SET t.ASSIGNEE_ = v.TEXT_
                    WHERE t.END_TIME_ IS NOT NULL
                      AND (t.ASSIGNEE_ IS NULL OR t.ASSIGNEE_ = '')
                    """);
            if (byKeyUserId + byStrippedApprove + byRuleVar > 0) {
                log.info("Backfilled historic task assignees: keyUserId={}, strippedApprove={}, ruleVar={}",
                        byKeyUserId, byStrippedApprove, byRuleVar);
            }
        } catch (Exception ex) {
            log.warn("Historic assignee backfill skipped: {}", ex.getMessage());
        }
    }
}
