package com.icoolkj.platform.oa.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.icoolkj.platform.framework.mybatis.TenantBaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** OA 实例表单数据 */
@TableName("oa_instance_data")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaInstanceDataEntity extends TenantBaseEntity {

    @TableField("instance_id")
    private String instanceId;

    @TableField("form_data")
    private String formData;

    @TableField("content_text")
    private String contentText;

    @TableField("field_snapshot_json")
    private String fieldSnapshotJson;

    /** 发起时布局快照 FormDocument JSON */
    @TableField("layout_snapshot_json")
    private String layoutSnapshotJson;
}
