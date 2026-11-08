package com.icoolkj.platform.oa.mapper;

import com.icoolkj.platform.oa.domain.OaInstanceEntity;
import com.icoolkj.platform.oa.dto.OaMyInstancePageQuery;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OaInstanceMapper {

    OaInstanceEntity selectById(@Param("id") String id);

    List<OaInstanceEntity> selectMyPage(OaMyInstancePageQuery query);

    long countMyByQuery(OaMyInstancePageQuery query);

    int insert(OaInstanceEntity entity);

    int updateById(OaInstanceEntity entity);

    /** Handler：仅更新审批字段包 */
    int updateApproveFields(OaInstanceEntity entity);

    int deleteById(@Param("id") String id);
}
