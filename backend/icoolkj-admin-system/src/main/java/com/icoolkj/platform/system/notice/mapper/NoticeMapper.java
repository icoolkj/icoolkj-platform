package com.icoolkj.platform.system.notice.mapper;

import com.icoolkj.platform.system.notice.domain.NoticeEntity;
import com.icoolkj.platform.system.notice.dto.NoticePageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeMapper {

    NoticeEntity selectById(@Param("id") String id);

    List<NoticeEntity> selectList(NoticeEntity query);

    List<NoticeEntity> selectPage(NoticePageQuery query);

    long countByQuery(NoticePageQuery query);

    List<NoticeEntity> selectPublished(@Param("tenantId") String tenantId,
                                       @Param("publishLocation") String publishLocation,
                                       @Param("limit") int limit);

    NoticeEntity selectPublishedById(@Param("tenantId") String tenantId, @Param("id") String id);

    long countByCoverFileId(@Param("coverFileId") String coverFileId);

    long countPublishedFileUsage(@Param("tenantId") String tenantId, @Param("fileId") String fileId);

    List<String> selectApprovingProcessInstanceIdsByFileId(@Param("fileId") String fileId);

    int insert(NoticeEntity entity);

    int updateById(NoticeEntity entity);

    /** Handler：仅更新审批字段包 */
    int updateApproveFields(NoticeEntity entity);

    int deleteById(@Param("id") String id);

    int deleteByIds(@Param("ids") List<String> ids);
}
