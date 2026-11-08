package com.icoolkj.platform.system.banner.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.system.banner.dto.BannerBatchDeleteRequest;
import com.icoolkj.platform.system.banner.dto.BannerCreateRequest;
import com.icoolkj.platform.system.banner.dto.BannerPageQuery;
import com.icoolkj.platform.system.banner.dto.BannerPublishedQuery;
import com.icoolkj.platform.system.banner.dto.BannerStatusUpdateRequest;
import com.icoolkj.platform.system.banner.dto.BannerUpdateRequest;
import com.icoolkj.platform.system.banner.vo.BannerListVO;
import com.icoolkj.platform.system.banner.vo.BannerPublishedVO;
import com.icoolkj.platform.system.banner.vo.BannerVO;
import java.util.List;

/** 轮播图服务 */
public interface BannerService {

    PageResult<BannerListVO> page(BannerPageQuery query);

    BannerVO getById(String id);

    List<BannerPublishedVO> listPublished(BannerPublishedQuery query);

    String create(BannerCreateRequest request);

    void update(BannerUpdateRequest request);

    void updateStatus(BannerStatusUpdateRequest request);

    void deleteById(String id);

    void batchDelete(BannerBatchDeleteRequest request);
}
