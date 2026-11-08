package com.icoolkj.platform.workflow.mapper;

import com.icoolkj.platform.workflow.domain.WfTaskActionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WfTaskActionMapper {

    int insert(WfTaskActionEntity entity);

    List<WfTaskActionEntity> selectByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    /** 当前用户作为操作人产生的加签交接/归还（用于已办列表） */
    List<WfTaskActionEntity> selectAddSignHandoffsByFromUserId(@Param("fromUserId") String fromUserId);
}
