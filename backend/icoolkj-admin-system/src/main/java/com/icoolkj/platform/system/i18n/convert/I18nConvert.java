package com.icoolkj.platform.system.i18n.convert;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.i18n.LocaleResolver;
import com.icoolkj.platform.system.i18n.domain.I18nLocaleEntity;
import com.icoolkj.platform.system.i18n.domain.I18nMessageEntity;
import com.icoolkj.platform.system.i18n.dto.I18nLocaleCreateRequest;
import com.icoolkj.platform.system.i18n.dto.I18nLocaleUpdateRequest;
import com.icoolkj.platform.system.i18n.dto.I18nMessageCreateRequest;
import com.icoolkj.platform.system.i18n.dto.I18nMessageUpdateRequest;
import com.icoolkj.platform.system.i18n.vo.I18nLocaleVO;
import com.icoolkj.platform.system.i18n.vo.I18nMessageVO;
import org.springframework.util.StringUtils;

public final class I18nConvert {

    private I18nConvert() {
    }

    public static I18nLocaleVO toLocaleVo(I18nLocaleEntity entity) {
        if (entity == null) {
            return null;
        }
        I18nLocaleVO vo = new I18nLocaleVO();
        vo.setId(entity.getId());
        vo.setLocaleCode(entity.getLocaleCode());
        vo.setLocaleName(entity.getLocaleName());
        vo.setNativeName(entity.getNativeName());
        vo.setSortNo(entity.getSortNo());
        vo.setIsDefault(entity.getIsDefault());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    public static I18nMessageVO toMessageVo(I18nMessageEntity entity) {
        if (entity == null) {
            return null;
        }
        I18nMessageVO vo = new I18nMessageVO();
        vo.setId(entity.getId());
        vo.setMessageKey(entity.getMessageKey());
        vo.setLocale(entity.getLocale());
        vo.setMessageValue(entity.getMessageValue());
        vo.setModule(entity.getModule());
        vo.setScope(entity.getScope());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    public static I18nLocaleEntity toLocaleEntity(I18nLocaleCreateRequest request) {
        I18nLocaleEntity entity = new I18nLocaleEntity();
        entity.setLocaleCode(LocaleResolver.normalize(request.getLocaleCode()));
        entity.setLocaleName(request.getLocaleName());
        entity.setNativeName(request.getNativeName());
        entity.setSortNo(request.getSortNo() != null ? request.getSortNo() : 0);
        entity.setIsDefault(StringUtils.hasText(request.getIsDefault()) ? request.getIsDefault() : "N");
        entity.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : CommonConstants.STATUS_NORMAL);
        entity.setRemark(request.getRemark());
        return entity;
    }

    public static void copy(I18nLocaleUpdateRequest request, I18nLocaleEntity entity) {
        entity.setLocaleCode(LocaleResolver.normalize(request.getLocaleCode()));
        entity.setLocaleName(request.getLocaleName());
        entity.setNativeName(request.getNativeName());
        entity.setSortNo(request.getSortNo() != null ? request.getSortNo() : 0);
        entity.setIsDefault(StringUtils.hasText(request.getIsDefault()) ? request.getIsDefault() : "N");
        entity.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : CommonConstants.STATUS_NORMAL);
        entity.setRemark(request.getRemark());
    }

    public static I18nMessageEntity toMessageEntity(I18nMessageCreateRequest request) {
        I18nMessageEntity entity = new I18nMessageEntity();
        entity.setMessageKey(request.getMessageKey().trim());
        entity.setLocale(LocaleResolver.normalize(request.getLocale()));
        entity.setMessageValue(request.getMessageValue());
        entity.setModule(resolveModule(request.getModule(), request.getMessageKey()));
        entity.setScope(request.getScope());
        entity.setRemark(request.getRemark());
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }

    public static void copy(I18nMessageUpdateRequest request, I18nMessageEntity entity) {
        entity.setMessageKey(request.getMessageKey().trim());
        entity.setLocale(LocaleResolver.normalize(request.getLocale()));
        entity.setMessageValue(request.getMessageValue());
        entity.setModule(resolveModule(request.getModule(), request.getMessageKey()));
        entity.setScope(request.getScope());
        entity.setRemark(request.getRemark());
        entity.setUpdatedAt(java.time.LocalDateTime.now());
    }

    private static String resolveModule(String module, String messageKey) {
        if (StringUtils.hasText(module)) {
            return module.trim();
        }
        int dot = messageKey.indexOf('.');
        return dot > 0 ? messageKey.substring(0, dot) : "common";
    }
}
