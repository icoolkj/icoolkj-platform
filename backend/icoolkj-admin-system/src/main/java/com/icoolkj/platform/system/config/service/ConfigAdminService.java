package com.icoolkj.platform.system.config.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.system.config.dto.ConfigCreateRequest;
import com.icoolkj.platform.system.config.dto.ConfigPageQuery;
import com.icoolkj.platform.system.config.dto.ConfigUpdateRequest;
import com.icoolkj.platform.system.config.vo.ConfigBootstrapVO;
import com.icoolkj.platform.system.config.vo.ConfigResolvedVO;
import com.icoolkj.platform.system.config.vo.ConfigVO;

/** 参数配置 CRUD（管理端） */
public interface ConfigAdminService {

    PageResult<ConfigVO> page(ConfigPageQuery query);

    ConfigVO getById(String id);

    ConfigVO getByKey(String configKey);

    /**
     * Authenticated read of tenant-resolved value for a whitelisted key
     * (no {@code system:config:list} required).
     */
    ConfigResolvedVO getResolved(String configKey);

    ConfigBootstrapVO getBootstrap();

    String create(ConfigCreateRequest request);

    void update(ConfigUpdateRequest request);

    void deleteById(String id);

    void refreshCache();
}
