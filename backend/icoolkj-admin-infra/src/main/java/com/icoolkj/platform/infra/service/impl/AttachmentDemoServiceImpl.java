package com.icoolkj.platform.infra.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.framework.tenant.TenantQueryScope;
import com.icoolkj.platform.infra.constant.AttachmentDemoFileConstants;
import com.icoolkj.platform.infra.convert.AttachmentDemoConvert;
import com.icoolkj.platform.infra.domain.AttachmentDemoEntity;
import com.icoolkj.platform.infra.dto.AttachmentDemoBatchDeleteRequest;
import com.icoolkj.platform.infra.dto.AttachmentDemoCreateRequest;
import com.icoolkj.platform.infra.dto.AttachmentDemoPageQuery;
import com.icoolkj.platform.infra.dto.AttachmentDemoStatusUpdateRequest;
import com.icoolkj.platform.infra.dto.AttachmentDemoUpdateRequest;
import com.icoolkj.platform.infra.dto.InfraFileRefBinding;
import com.icoolkj.platform.infra.mapper.AttachmentDemoMapper;
import com.icoolkj.platform.infra.service.AttachmentDemoService;
import com.icoolkj.platform.infra.service.InfraFileService;
import com.icoolkj.platform.infra.support.BizCatalogAssetsSupport;
import com.icoolkj.platform.infra.support.CatalogRequiredAssetsValidator;
import com.icoolkj.platform.infra.vo.AttachmentDemoVO;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 附件测试业务：保存 assets_json + syncBizFileRefs，删除 releaseBizFiles */
@Service
public class AttachmentDemoServiceImpl implements AttachmentDemoService {

    private final AttachmentDemoMapper attachmentDemoMapper;
    private final BizCatalogAssetsSupport assetsSupport;
    private final CatalogRequiredAssetsValidator requiredAssetsValidator;
    private final InfraFileService infraFileService;

    public AttachmentDemoServiceImpl(
            AttachmentDemoMapper attachmentDemoMapper,
            BizCatalogAssetsSupport assetsSupport,
            CatalogRequiredAssetsValidator requiredAssetsValidator,
            InfraFileService infraFileService) {
        this.attachmentDemoMapper = attachmentDemoMapper;
        this.assetsSupport = assetsSupport;
        this.requiredAssetsValidator = requiredAssetsValidator;
        this.infraFileService = infraFileService;
    }

    @Override
    public PageResult<AttachmentDemoVO> page(AttachmentDemoPageQuery query) {
        TenantQueryScope.applyPageQuery(query);
        return TenantQueryScope.runForExplicitTenant(query, () -> {
            long total = attachmentDemoMapper.countByQuery(query);
            if (total == 0) {
                return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
            }
            List<AttachmentDemoEntity> list = attachmentDemoMapper.selectPage(query);
            if (CollectionUtils.isEmpty(list)) {
                return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
            }
            return PageResult.of(
                    list.stream().map(e -> AttachmentDemoConvert.toVo(e, assetsSupport)).toList(),
                    total,
                    query.getPageNo(),
                    query.getPageSize());
        });
    }

    @Override
    public AttachmentDemoVO getById(String id) {
        return AttachmentDemoConvert.toVo(requireReadable(id), assetsSupport);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(AttachmentDemoCreateRequest request) {
        requiredAssetsValidator.validateRequired(
                request.getAssets(), AttachmentDemoFileConstants.CATALOG_CODE);
        AttachmentDemoEntity entity = AttachmentDemoConvert.toEntity(request, assetsSupport);
        entity.setId(IdGeneratorUtils.nextIdStr());
        attachmentDemoMapper.insert(entity);
        syncFileRefs(entity, request.getAssets());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AttachmentDemoUpdateRequest request) {
        requiredAssetsValidator.validateRequired(
                request.getAssets(), AttachmentDemoFileConstants.CATALOG_CODE);
        AttachmentDemoEntity entity = requireReadable(request.getId());
        AttachmentDemoConvert.copy(request, entity, assetsSupport);
        attachmentDemoMapper.updateById(entity);
        syncFileRefs(entity, request.getAssets());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(AttachmentDemoStatusUpdateRequest request) {
        AttachmentDemoEntity entity = requireReadable(request.getId());
        entity.setStatus(request.getStatus());
        attachmentDemoMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        AttachmentDemoEntity entity = requireReadable(id);
        releaseFileRefs(entity);
        AuditFieldHelper.markDeleted(entity);
        attachmentDemoMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(AttachmentDemoBatchDeleteRequest request) {
        if (CollectionUtils.isEmpty(request.getIds())) {
            return;
        }
        for (String id : request.getIds()) {
            deleteById(id);
        }
    }

    private AttachmentDemoEntity requireReadable(String id) {
        AttachmentDemoEntity entity = attachmentDemoMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.attachment_demo");
        }
        TenantQueryScope.assertReadable(entity.getTenantId());
        return entity;
    }

    private void syncFileRefs(AttachmentDemoEntity entity, Map<String, Object> assets) {
        if (!StringUtils.hasText(entity.getTenantId()) || !StringUtils.hasText(entity.getId())) {
            return;
        }
        List<InfraFileRefBinding> bindings = assetsSupport.toBindings(assets);
        infraFileService.syncBizFileRefs(
                entity.getTenantId(), AttachmentDemoFileConstants.BIZ_TYPE, entity.getId(), bindings);
    }

    private void releaseFileRefs(AttachmentDemoEntity entity) {
        if (!StringUtils.hasText(entity.getTenantId()) || !StringUtils.hasText(entity.getId())) {
            return;
        }
        infraFileService.releaseBizFiles(
                entity.getTenantId(), AttachmentDemoFileConstants.BIZ_TYPE, entity.getId());
    }
}
