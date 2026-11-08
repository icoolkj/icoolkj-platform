package com.icoolkj.platform.system.notice.convert;

import com.icoolkj.platform.system.notice.constant.NoticeReleaseStatus;
import com.icoolkj.platform.framework.tenant.TenantFieldHelper;
import com.icoolkj.platform.system.notice.domain.NoticeEntity;
import com.icoolkj.platform.system.notice.dto.NoticeCreateRequest;
import com.icoolkj.platform.system.notice.dto.NoticeUpdateRequest;
import com.icoolkj.platform.system.notice.vo.NoticeListVO;
import com.icoolkj.platform.system.notice.vo.NoticePublishedVO;
import com.icoolkj.platform.system.notice.vo.NoticeVO;
import com.icoolkj.platform.workflow.constant.WorkflowBizApproveStatus;

import org.springframework.util.StringUtils;

/** 通知公告表对象转换 */
public final class NoticeConvert {

    private NoticeConvert() {
    }

    public static NoticeVO toVo(NoticeEntity entity) {
        if (entity == null) {
            return null;
        }
        NoticeVO vo = new NoticeVO();
        vo.setId(entity.getId());
        vo.setNoticeTitle(entity.getNoticeTitle());
        vo.setNoticeType(entity.getNoticeType());
        vo.setCoverFileId(entity.getCoverFileId());
        vo.setCoverPreviewUrl(entity.getCoverPreviewUrl());
        vo.setNoticeContent(entity.getNoticeContent());
        vo.setNoticeSummary(entity.getNoticeSummary());
        vo.setSortNo(entity.getSortNo());
        vo.setPublishLocations(entity.getPublishLocations());
        vo.setStatus(entity.getStatus());
        vo.setApproveStatus(entity.getApproveStatus());
        vo.setReleaseStatus(entity.getReleaseStatus());
        vo.setProcessInstanceId(entity.getProcessInstanceId());
        vo.setStarterUserId(entity.getStarterUserId());
        vo.setSubmittedAt(entity.getSubmittedAt());
        vo.setFinishedAt(entity.getFinishedAt());
        vo.setPublishedAt(entity.getPublishedAt());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    public static NoticeListVO toListVo(NoticeEntity entity) {
        if (entity == null) {
            return null;
        }
        NoticeListVO vo = new NoticeListVO();
        vo.setId(entity.getId());
        vo.setNoticeTitle(entity.getNoticeTitle());
        vo.setNoticeType(entity.getNoticeType());
        vo.setCoverFileId(entity.getCoverFileId());
        vo.setCoverPreviewUrl(entity.getCoverPreviewUrl());
        vo.setNoticeSummary(entity.getNoticeSummary());
        vo.setSortNo(entity.getSortNo());
        vo.setPublishLocations(entity.getPublishLocations());
        vo.setStatus(entity.getStatus());
        vo.setApproveStatus(entity.getApproveStatus());
        vo.setReleaseStatus(entity.getReleaseStatus());
        vo.setProcessInstanceId(entity.getProcessInstanceId());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    public static NoticePublishedVO toPublishedVo(NoticeEntity entity) {
        if (entity == null) {
            return null;
        }
        NoticePublishedVO vo = new NoticePublishedVO();
        vo.setId(entity.getId());
        vo.setNoticeTitle(entity.getNoticeTitle());
        vo.setNoticeType(entity.getNoticeType());
        vo.setCoverFileId(entity.getCoverFileId());
        vo.setCoverPreviewUrl(entity.getCoverPreviewUrl());
        vo.setNoticeSummary(entity.getNoticeSummary());
        vo.setNoticeContent(entity.getNoticeContent());
        vo.setSortNo(entity.getSortNo());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    public static NoticeEntity toEntity(NoticeCreateRequest request) {
        NoticeEntity entity = new NoticeEntity();
        entity.setTenantId(TenantFieldHelper.resolveTenantId(null));
        entity.setNoticeTitle(request.getNoticeTitle());
        entity.setNoticeType(request.getNoticeType());
        entity.setCoverFileId(trimToNull(request.getCoverFileId()));
        entity.setNoticeContent(request.getNoticeContent());
        entity.setNoticeSummary(request.getNoticeSummary());
        entity.setSortNo(request.getSortNo() != null ? request.getSortNo() : 99);
        entity.setPublishLocations(request.getPublishLocations());
        entity.setStatus(request.getStatus());
        entity.setApproveStatus(WorkflowBizApproveStatus.DRAFT);
        entity.setReleaseStatus(NoticeReleaseStatus.UNPUBLISHED);
        entity.setRemark(request.getRemark());
        return entity;
    }

    public static void copy(NoticeUpdateRequest request, NoticeEntity entity) {
        entity.setNoticeTitle(request.getNoticeTitle());
        entity.setNoticeType(request.getNoticeType());
        entity.setCoverFileId(trimToNull(request.getCoverFileId()));
        entity.setNoticeContent(request.getNoticeContent());
        entity.setNoticeSummary(request.getNoticeSummary());
        entity.setSortNo(request.getSortNo());
        entity.setPublishLocations(request.getPublishLocations());
        entity.setStatus(request.getStatus());
        entity.setRemark(request.getRemark());
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
