package com.icoolkj.platform.infra.convert;

import com.icoolkj.platform.infra.domain.InfraFileEntity;
import com.icoolkj.platform.infra.vo.InfraFileVO;

/** 文件对象转换（生成器维护，可逐步替换为 MapStruct）。 */
public final class InfraFileConvert {

    private InfraFileConvert() {
    }

    public static InfraFileVO toVo(InfraFileEntity entity) {
        if (entity == null) {
            return null;
        }
        InfraFileVO vo = new InfraFileVO();
        vo.setId(entity.getId());
        vo.setTenantId(entity.getTenantId());
        vo.setBizType(entity.getBizType());
        vo.setBizId(entity.getBizId());
        vo.setOriginalName(entity.getOriginalName());
        vo.setFileName(entity.getFileName());
        vo.setFileExt(entity.getFileExt());
        vo.setContentType(entity.getContentType());
        vo.setFileSize(entity.getFileSize());
        vo.setStorageType(entity.getStorageType());
        vo.setBucketName(entity.getBucketName());
        vo.setObjectKey(entity.getObjectKey());
        vo.setFileUrl(entity.getFileUrl());
        vo.setMd5(entity.getMd5());
        vo.setScanStatus(entity.getScanStatus());
        vo.setScanEngine(entity.getScanEngine());
        vo.setScanAt(entity.getScanAt());
        vo.setEncrypted(entity.getEncrypted());
        vo.setSensitivityLevel(entity.getSensitivityLevel());
        vo.setUserId(entity.getUserId());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setRemark(entity.getRemark());
        return vo;
    }

}
