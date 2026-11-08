package com.icoolkj.platform.workflow.mapper;

import com.icoolkj.platform.workflow.domain.WfBizProcessEntity;
import com.icoolkj.platform.workflow.dto.WorkflowInstancePageQuery;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WfBizProcessMapper {

    WfBizProcessEntity selectByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    /** All process starts for one business document, oldest first. */
    List<WfBizProcessEntity> selectListByBiz(@Param("bizType") String bizType,
                                             @Param("bizId") String bizId);

    /**
     * 已发申请：每个业务单据仅最新一轮主流程（排除 callActivity 子流程、历史重提轮次）。
     */
    List<WfBizProcessEntity> selectSubmittedPageByStarter(@Param("starterUserId") String starterUserId,
                                                          WorkflowInstancePageQuery query);

    long countSubmittedByStarter(@Param("starterUserId") String starterUserId,
                                 WorkflowInstancePageQuery query);

    List<WfBizProcessEntity> selectPageByTenant(@Param("tenantId") String tenantId,
                                                WorkflowInstancePageQuery query);

    long countByTenant(@Param("tenantId") String tenantId, WorkflowInstancePageQuery query);

    /** 租户下全量（用于「无人办理」筛选，勿带大分页） */
    List<WfBizProcessEntity> selectListByTenant(@Param("tenantId") String tenantId,
                                                WorkflowInstancePageQuery query);

    int insert(WfBizProcessEntity entity);

    int updateStatusByProcessInstanceId(@Param("processInstanceId") String processInstanceId,
                                        @Param("status") String status,
                                        @Param("endTime") LocalDateTime endTime);

    int updateParentInstanceId(@Param("processInstanceId") String processInstanceId,
                               @Param("parentInstanceId") String parentInstanceId);
}
