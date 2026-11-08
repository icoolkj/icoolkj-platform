package com.icoolkj.platform.oa.convert;

import com.icoolkj.platform.oa.domain.OaInstanceDataEntity;
import com.icoolkj.platform.oa.domain.OaInstanceEntity;
import com.icoolkj.platform.oa.support.OaFormContentTextSupport;
import com.icoolkj.platform.oa.support.OaInstanceFormSupport;
import com.icoolkj.platform.oa.vo.OaFieldDefVO;
import com.icoolkj.platform.oa.vo.OaInstanceVO;
import java.util.List;
import java.util.Map;

public final class OaInstanceConvert {

    private OaInstanceConvert() {
    }

    public static OaInstanceVO toVo(OaInstanceEntity entity, OaInstanceDataEntity data,
                                    OaInstanceFormSupport formSupport) {
        OaInstanceVO vo = new OaInstanceVO();
        vo.setId(entity.getId());
        vo.setCategoryCode(entity.getCategoryCode());
        vo.setInstanceTitle(entity.getInstanceTitle());
        vo.setApproveStatus(entity.getApproveStatus());
        vo.setProcessInstanceId(entity.getProcessInstanceId());
        vo.setProcessDefinitionKey(entity.getProcessDefinitionKey());
        vo.setStarterUserId(entity.getStarterUserId());
        vo.setSubmittedAt(entity.getSubmittedAt());
        vo.setFinishedAt(entity.getFinishedAt());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());

        Map<String, Object> form = formSupport.readFormJson(data == null ? null : data.getFormData());

        if (data != null) {
            List<OaFieldDefVO> fields = formSupport.resolveFields(
                    data.getFieldSnapshotJson(), entity.getTenantTplVerId());
            vo.setFields(fields);
            vo.setLayoutSnapshotJson(data.getLayoutSnapshotJson());
            // content_text → formData[richTextFieldCode]，对前端透明
            form = OaFormContentTextSupport.mergeInto(fields, data.getContentText(), form);
        }
        vo.setFormData(form);
        return vo;
    }
}
