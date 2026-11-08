package com.icoolkj.platform.api.controller.oa;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.model.response.ApiResponse;
import com.icoolkj.platform.oa.dto.OaMyInstancePageQuery;
import com.icoolkj.platform.oa.service.OaInitiateService;
import com.icoolkj.platform.oa.vo.OaInitiateCatalogItemVO;
import com.icoolkj.platform.oa.vo.OaInstanceListVO;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 在线办公：发起申请 / 我的草稿（已发申请见工作流 submitted/page） */
@RestController
@RequestMapping("/oa/office")
public class OaOfficeController {

    private final OaInitiateService initiateService;

    public OaOfficeController(OaInitiateService initiateService) {
        this.initiateService = initiateService;
    }

    @PreAuthorize("@authz.hasPermission('oa:office:apply:list')")
    @GetMapping("/catalog")
    public ApiResponse<List<OaInitiateCatalogItemVO>> catalog() {
        return ApiResponse.ok(initiateService.listCatalog());
    }

    @PreAuthorize("@authz.hasPermission('oa:office:draft:list')")
    @GetMapping("/my/drafts")
    public ApiResponse<PageResult<OaInstanceListVO>> drafts(OaMyInstancePageQuery query) {
        if (query == null) {
            query = new OaMyInstancePageQuery();
        }
        query.setScope("draft");
        return ApiResponse.ok(initiateService.pageMyInstances(query));
    }
}
