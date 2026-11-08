package com.icoolkj.platform.infra.convert;

import com.icoolkj.platform.infra.domain.ThirdPartyCallLogEntity;
import com.icoolkj.platform.infra.vo.ThirdPartyCallLogVO;

/** 第三方调用日志对象转换（生成器维护，可逐步替换为 MapStruct）。 */
public final class ThirdPartyCallLogConvert {

    private ThirdPartyCallLogConvert() {
    }

    public static ThirdPartyCallLogVO toVo(ThirdPartyCallLogEntity entity) {
        if (entity == null) {
            return null;
        }
        ThirdPartyCallLogVO vo = new ThirdPartyCallLogVO();
        vo.setId(entity.getId());
        vo.setTenantId(entity.getTenantId());
        vo.setProviderName(entity.getProviderName());
        vo.setBizType(entity.getBizType());
        vo.setRequestUrl(entity.getRequestUrl());
        vo.setRequestMethod(entity.getRequestMethod());
        vo.setRequestData(entity.getRequestData());
        vo.setResponseData(entity.getResponseData());
        vo.setResponseStatus(entity.getResponseStatus());
        vo.setErrorMessage(entity.getErrorMessage());
        vo.setCostTime(entity.getCostTime());
        vo.setCalledAt(entity.getCalledAt());
        return vo;
    }

}
