package com.icoolkj.platform.system.region.mapper;

import com.icoolkj.platform.system.region.domain.RegionEntity;
import com.icoolkj.platform.system.region.dto.RegionPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RegionMapper {

    RegionEntity selectById(@Param("id") String id);

    List<RegionEntity> selectAllActive();

    List<RegionEntity> selectSubtree(@Param("rootRegionId") String rootRegionId);

    List<RegionEntity> selectSelectableRoots(@Param("levels") List<String> levels);

    List<RegionEntity> selectPage(RegionPageQuery query);

    long countByQuery(RegionPageQuery query);

    RegionEntity selectByRegionCode(@Param("regionCode") String regionCode);

    List<RegionEntity> selectChildren(@Param("parentId") String parentId);

    int insert(RegionEntity entity);

    int updateById(RegionEntity entity);

    long countTenantsByRootInSubtree(@Param("regionId") String regionId);
}
