package com.icoolkj.platform.system.config.mapper;

import com.icoolkj.platform.system.config.domain.ConfigEntity;
import com.icoolkj.platform.system.config.dto.ConfigPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConfigMapper {

    ConfigEntity selectById(@Param("id") String id);

    List<ConfigEntity> selectList(ConfigEntity query);

    List<ConfigEntity> selectPage(ConfigPageQuery query);

    long countByQuery(ConfigPageQuery query);

    int insert(ConfigEntity entity);

    int updateById(ConfigEntity entity);

    /** 物理删除（租户覆盖恢复默认 / 非内置参数删除，避免 soft-delete 堆唯一索引垃圾行） */
    int deleteById(@Param("id") String id);

    /**
     * 物理清理某租户下某配置键的全部行（含历史 soft-delete / {@code #del#} 改名行）。
     */
    int purgeByTenantAndKey(@Param("tenantId") String tenantId, @Param("configKey") String configKey);

    ConfigEntity selectByTenantAndKey(@Param("tenantId") String tenantId, @Param("configKey") String configKey);

}
