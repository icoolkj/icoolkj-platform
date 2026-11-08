package com.icoolkj.platform.system.banner.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.tenant.TenantQueryScope;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.infra.dto.InfraFileRefBinding;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.system.banner.constant.BannerFileConstants;
import com.icoolkj.platform.system.banner.convert.BannerConvert;
import com.icoolkj.platform.system.banner.domain.BannerEntity;
import com.icoolkj.platform.system.banner.dto.BannerBatchDeleteRequest;
import com.icoolkj.platform.system.banner.dto.BannerCreateRequest;
import com.icoolkj.platform.system.banner.dto.BannerPageQuery;
import com.icoolkj.platform.system.banner.dto.BannerPublishedQuery;
import com.icoolkj.platform.system.banner.dto.BannerStatusUpdateRequest;
import com.icoolkj.platform.system.banner.dto.BannerUpdateRequest;
import com.icoolkj.platform.system.banner.mapper.BannerMapper;
import com.icoolkj.platform.system.banner.service.BannerService;
import com.icoolkj.platform.system.banner.vo.BannerListVO;
import com.icoolkj.platform.system.banner.vo.BannerPublishedVO;
import com.icoolkj.platform.system.banner.vo.BannerVO;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 轮播图服务实现 */
@Service
public class BannerServiceImpl implements BannerService {

    private final BannerMapper bannerMapper;
    private final InfraFileService infraFileService;

    public BannerServiceImpl(BannerMapper bannerMapper, InfraFileService infraFileService) {
        this.bannerMapper = bannerMapper;
        this.infraFileService = infraFileService;
    }

    @Override
    public PageResult<BannerListVO> page(BannerPageQuery query) {
        TenantQueryScope.applyPageQuery(query);
        return TenantQueryScope.runForExplicitTenant(query, () -> {
            long total = bannerMapper.countByQuery(query);
            if (total == 0) {
                return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
            }
            List<BannerEntity> list = bannerMapper.selectPage(query);
            if (CollectionUtils.isEmpty(list)) {
                return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
            }
            return PageResult.of(list.stream().map(this::toListVo).toList(), total, query.getPageNo(), query.getPageSize());
        });
    }

    @Override
    public BannerVO getById(String id) {
        BannerEntity entity = requireReadableBanner(id);
        return toVo(entity);
    }

    @Override
    public List<BannerPublishedVO> listPublished(BannerPublishedQuery query) {
        String tenantId = TenantQueryScope.requiredOperatorTenantId();
        int limit = query.getLimit() == null ? 10 : Math.min(query.getLimit(), 50);
        return TenantScopeRunner.runIgnoringTenant(() -> bannerMapper.selectPublished(
                        tenantId, query.getDisplayPosition().trim(), limit).stream()
                .map(this::toPublishedVo)
                .toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(BannerCreateRequest request) {
        BannerEntity entity = BannerConvert.toEntity(request);
        entity.setId(IdGeneratorUtils.nextIdStr());
        bannerMapper.insert(entity);
        syncBannerFileRefs(entity);
        return String.valueOf(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(BannerUpdateRequest request) {
        BannerEntity entity = requireReadableBanner(request.getId());
        BannerConvert.copy(request, entity);
        bannerMapper.updateById(entity);
        syncBannerFileRefs(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(BannerStatusUpdateRequest request) {
        BannerEntity entity = requireReadableBanner(request.getId());
        entity.setStatus(request.getStatus());
        bannerMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        BannerEntity entity = requireReadableBanner(id);
        releaseBannerFileRefs(entity);
        AuditFieldHelper.markDeleted(entity);
        bannerMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(BannerBatchDeleteRequest request) {
        if (CollectionUtils.isEmpty(request.getIds())) {
            return;
        }
        for (String id : request.getIds()) {
            deleteById(id);
        }
    }

    private BannerEntity requireReadableBanner(String id) {
        BannerEntity entity = bannerMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.banner");
        }
        TenantQueryScope.assertReadable(entity.getTenantId());
        return entity;
    }

    private void syncBannerFileRefs(BannerEntity entity) {
        if (!StringUtils.hasText(entity.getTenantId()) || !StringUtils.hasText(entity.getId())) {
            return;
        }
        List<InfraFileRefBinding> bindings = List.of(
                new InfraFileRefBinding(entity.getImageFileId(), BannerFileConstants.REF_IMAGE));
        infraFileService.syncBizFileRefs(
                entity.getTenantId(), BannerFileConstants.BIZ_TYPE, entity.getId(), bindings);
    }

    private void releaseBannerFileRefs(BannerEntity entity) {
        if (!StringUtils.hasText(entity.getTenantId()) || !StringUtils.hasText(entity.getId())) {
            return;
        }
        infraFileService.releaseBizFiles(
                entity.getTenantId(), BannerFileConstants.BIZ_TYPE, entity.getId());
    }

    private BannerListVO toListVo(BannerEntity entity) {
        BannerListVO vo = BannerConvert.toListVo(entity);
        if (vo != null && StringUtils.hasText(entity.getImageFileId())) {
            vo.setImagePreviewUrl(infraFileService.resolveAccessibleUrl(
                    entity.getImageFileId(), entity.getImagePreviewUrl()));
        }
        return vo;
    }

    private BannerVO toVo(BannerEntity entity) {
        BannerVO vo = BannerConvert.toVo(entity);
        if (vo != null && StringUtils.hasText(entity.getImageFileId())) {
            vo.setImagePreviewUrl(infraFileService.resolveAccessibleUrl(
                    entity.getImageFileId(), entity.getImagePreviewUrl()));
        }
        return vo;
    }

    private BannerPublishedVO toPublishedVo(BannerEntity entity) {
        BannerPublishedVO vo = BannerConvert.toPublishedVo(entity);
        if (vo != null && StringUtils.hasText(entity.getImageFileId())) {
            vo.setImagePreviewUrl(infraFileService.resolveAccessibleUrl(
                    entity.getImageFileId(), entity.getImagePreviewUrl()));
        }
        return vo;
    }
}
