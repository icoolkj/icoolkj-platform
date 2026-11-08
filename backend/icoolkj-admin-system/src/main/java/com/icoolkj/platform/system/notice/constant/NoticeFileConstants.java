package com.icoolkj.platform.system.notice.constant;

/** 通知公告文件业务引用标识 */
public final class NoticeFileConstants {

    /** infra_file_ref.biz_type */
    public static final String BIZ_TYPE = "sys_notice";
    /** 封面上传 biz_type（upload 表单） */
    public static final String BIZ_TYPE_COVER = "notice_cover";
    /** 富文本内嵌图片 upload biz_type */
    public static final String BIZ_TYPE_RICHTEXT = "notice_richtext";
    /** 封面字段引用 */
    public static final String REF_COVER = "coverFileId";
    /** 富文本内嵌图片引用 */
    public static final String REF_CONTENT = "noticeContent";

    private NoticeFileConstants() {
    }
}
