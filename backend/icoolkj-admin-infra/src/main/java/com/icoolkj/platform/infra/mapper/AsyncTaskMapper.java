package com.icoolkj.platform.infra.mapper;

import com.icoolkj.platform.infra.domain.AsyncTaskEntity;
import com.icoolkj.platform.infra.dto.AsyncTaskPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AsyncTaskMapper {

    AsyncTaskEntity selectById(@Param("id") String id);

    List<AsyncTaskEntity> selectList(AsyncTaskEntity query);

    List<AsyncTaskEntity> selectPage(AsyncTaskPageQuery query);

    long countByQuery(AsyncTaskPageQuery query);

    List<AsyncTaskEntity> selectStaleByStatus(@Param("taskStatus") String taskStatus,
                                             @Param("updatedBefore") java.time.LocalDateTime updatedBefore,
                                             @Param("limit") int limit);

    /** Terminal excel-import tasks whose staging files may be released after retention. */
    List<AsyncTaskEntity> selectTerminalExcelImportFinishedBefore(
            @Param("finishedBefore") java.time.LocalDateTime finishedBefore,
            @Param("limit") int limit);

    int insert(AsyncTaskEntity entity);

    int updateById(AsyncTaskEntity entity);

    int deleteById(@Param("id") String id);

}
