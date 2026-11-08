package com.icoolkj.platform.system.workbench.mapper;

import com.icoolkj.platform.system.workbench.vo.WorkbenchLoginLogItemVO;
import com.icoolkj.platform.system.workbench.vo.WorkbenchOperationLogItemVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WorkbenchMapper {

    long countTodayLogins(@Param("tenantId") String tenantId);

    List<WorkbenchLoginLogItemVO> selectRecentLoginLogs(@Param("tenantId") String tenantId, @Param("limit") int limit);

    List<WorkbenchOperationLogItemVO> selectRecentOperationLogs(@Param("tenantId") String tenantId, @Param("limit") int limit);
}
