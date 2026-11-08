package com.icoolkj.platform.system.user.mapper;

import com.icoolkj.platform.system.user.domain.UserPasswordHistoryEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserPasswordHistoryMapper {

    int insert(UserPasswordHistoryEntity entity);

    List<UserPasswordHistoryEntity> selectRecent(@Param("tenantId") String tenantId,
                                                 @Param("userId") String userId,
                                                 @Param("limit") int limit);

    int deleteById(@Param("id") String id);
}
