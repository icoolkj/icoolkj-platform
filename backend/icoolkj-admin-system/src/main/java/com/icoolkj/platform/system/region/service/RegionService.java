package com.icoolkj.platform.system.region.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.system.region.dto.RegionCreateRequest;
import com.icoolkj.platform.system.region.dto.RegionPageQuery;
import com.icoolkj.platform.system.region.dto.RegionUpdateRequest;
import com.icoolkj.platform.system.region.vo.RegionVO;

import java.util.List;

public interface RegionService {

    PageResult<RegionVO> page(RegionPageQuery query);

    List<RegionVO> listTree();

    List<RegionVO> listTenantTree();

    List<RegionVO> listSelectableRoots(String tenantType);

    RegionVO getById(String id);

    void assertRegionSelectableForTenantRoot(String regionId, String tenantType);

    void assertRegionWithinTenantScope(String regionId, String rootRegionId);

    String create(RegionCreateRequest request);

    void update(RegionUpdateRequest request);

    void deleteById(String id);
}
