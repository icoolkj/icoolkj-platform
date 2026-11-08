package com.icoolkj.platform.oa.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.oa.dto.OaMyInstancePageQuery;
import com.icoolkj.platform.oa.vo.OaInitiateCatalogItemVO;
import com.icoolkj.platform.oa.vo.OaInstanceListVO;
import java.util.List;

/** 在线办公：发起目录 + 我的单据 */
public interface OaInitiateService {

    /** 当前租户可发起的模板目录（enabled + 已发布） */
    List<OaInitiateCatalogItemVO> listCatalog();

    /** 我的草稿 / 已发申请 */
    PageResult<OaInstanceListVO> pageMyInstances(OaMyInstancePageQuery query);
}
