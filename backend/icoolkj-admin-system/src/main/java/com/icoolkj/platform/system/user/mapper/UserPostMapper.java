package com.icoolkj.platform.system.user.mapper;

import com.icoolkj.platform.system.user.domain.UserPostEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserPostMapper {

    List<UserPostEntity> selectList(UserPostEntity query);

    int insert(UserPostEntity entity);

    int delete(UserPostEntity entity);

}
