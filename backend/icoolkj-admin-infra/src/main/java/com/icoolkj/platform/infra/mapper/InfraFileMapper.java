package com.icoolkj.platform.infra.mapper;

import com.icoolkj.platform.infra.domain.InfraFileEntity;
import com.icoolkj.platform.infra.dto.InfraFilePageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface InfraFileMapper {

    InfraFileEntity selectById(@Param("id") String id);

    List<InfraFileEntity> selectList(InfraFileEntity query);

    List<InfraFileEntity> selectPage(InfraFilePageQuery query);

    long countByQuery(InfraFilePageQuery query);

    int insert(InfraFileEntity entity);

    int updateById(InfraFileEntity entity);

    int logicalDeleteById(@Param("id") String id);

    List<InfraFileEntity> selectSoftDeletedBefore(@Param("before") LocalDateTime before,
                                                  @Param("limit") int limit);

    /**
     * Active excel-import staging files older than {@code before} with no matching async task id.
     */
    List<InfraFileEntity> selectOrphanExcelImportStagingBefore(@Param("before") LocalDateTime before,
                                                              @Param("limit") int limit);

    int deleteById(@Param("id") String id);

}
