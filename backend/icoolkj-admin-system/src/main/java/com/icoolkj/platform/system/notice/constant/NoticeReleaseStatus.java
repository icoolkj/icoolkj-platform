package com.icoolkj.platform.system.notice.constant;

/**
 * 公告上架状态（业务发布，与审批 {@code approve_status} 分离）。
 * <p>仅 {@code approve_status=approved} 时可发布/下架；门户只展示已上架。
 */
public final class NoticeReleaseStatus {

    public static final String UNPUBLISHED = "unpublished";
    public static final String PUBLISHED = "published";

    private NoticeReleaseStatus() {
    }
}
