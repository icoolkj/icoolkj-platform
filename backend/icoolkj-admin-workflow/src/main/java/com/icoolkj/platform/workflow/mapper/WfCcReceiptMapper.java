package com.icoolkj.platform.workflow.mapper;

import com.icoolkj.platform.workflow.domain.WfCcReceiptEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WfCcReceiptMapper {

    int insert(WfCcReceiptEntity entity);

    int updateReadAt(@Param("taskId") String taskId,
                     @Param("userId") String userId,
                     @Param("readAt") java.time.LocalDateTime readAt);

    WfCcReceiptEntity selectByTaskIdAndUserId(@Param("taskId") String taskId,
                                              @Param("userId") String userId);

    List<WfCcReceiptEntity> selectByTaskIdsAndUserId(@Param("taskIds") List<String> taskIds,
                                                     @Param("userId") String userId);
}
