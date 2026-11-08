package com.icoolkj.platform.system.user.mapper;

import com.icoolkj.platform.system.user.domain.UserPreferenceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserPreferenceMapper {

    List<UserPreferenceEntity> selectByUserId(@Param("userId") String userId);

    UserPreferenceEntity selectByUserAndKey(@Param("userId") String userId, @Param("prefKey") String prefKey);

    int upsert(UserPreferenceEntity entity);
}
