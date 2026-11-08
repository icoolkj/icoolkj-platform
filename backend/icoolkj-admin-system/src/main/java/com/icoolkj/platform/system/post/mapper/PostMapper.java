package com.icoolkj.platform.system.post.mapper;

import com.icoolkj.platform.system.post.domain.PostEntity;
import com.icoolkj.platform.system.post.dto.PostPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {

    PostEntity selectById(@Param("id") String id);

    List<PostEntity> selectList(PostEntity query);

    List<PostEntity> selectPage(PostPageQuery query);

    long countByQuery(PostPageQuery query);

    int insert(PostEntity entity);

    int updateById(PostEntity entity);

    int deleteById(@Param("id") String id);

}
