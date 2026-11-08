package com.icoolkj.platform.system.banner.mapper;

import com.icoolkj.platform.system.banner.domain.BannerEntity;
import com.icoolkj.platform.system.banner.dto.BannerPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BannerMapper {

    BannerEntity selectById(@Param("id") String id);

    List<BannerEntity> selectPage(BannerPageQuery query);

    long countByQuery(BannerPageQuery query);

    List<BannerEntity> selectPublished(@Param("tenantId") String tenantId,
                                       @Param("displayPosition") String displayPosition,
                                       @Param("limit") int limit);

    long countByImageFileId(@Param("imageFileId") String imageFileId);

    int insert(BannerEntity entity);

    int updateById(BannerEntity entity);
}
