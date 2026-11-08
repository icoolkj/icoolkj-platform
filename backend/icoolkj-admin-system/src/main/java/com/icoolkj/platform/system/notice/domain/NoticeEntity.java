package com.icoolkj.platform.system.notice.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 通知公告表 — maps to `sys_notice`. */
@TableName("sys_notice")
@Getter
@Setter
public class NoticeEntity extends TenantBaseEntity {

    @TableField("notice_title")
    private String noticeTitle;

    @TableField("notice_type")
    private String noticeType;

    @TableField("cover_file_id")
    private String coverFileId;

    @TableField("notice_content")
    private String noticeContent;

    @TableField("notice_summary")
    private String noticeSummary;

    @TableField("sort_no")
    private Integer sortNo;

    @TableField("publish_locations")
    private String publishLocations;

    @TableField("approve_status")
    private String approveStatus;

    @TableField("process_instance_id")
    private String processInstanceId;

    @TableField("process_definition_key")
    private String processDefinitionKey;

    @TableField("starter_user_id")
    private String starterUserId;

    @TableField("submitted_at")
    private java.time.LocalDateTime submittedAt;

    @TableField("finished_at")
    private java.time.LocalDateTime finishedAt;

    /** 上架状态：unpublished / published */
    @TableField("release_status")
    private String releaseStatus;

    @TableField("published_at")
    private java.time.LocalDateTime publishedAt;

    private String status;

    private String remark;

    /** 列表联查封面预览地址，非表字段 */
    @TableField(exist = false)
    private String coverPreviewUrl;
}
