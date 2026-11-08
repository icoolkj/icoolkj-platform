package com.icoolkj.platform.system.i18n.mapper;

import com.icoolkj.platform.system.i18n.domain.I18nMessageEntity;
import com.icoolkj.platform.system.i18n.dto.I18nMessagePageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface I18nMessageMapper {

    I18nMessageEntity selectById(@Param("id") String id);

    I18nMessageEntity selectByKeyAndLocale(@Param("messageKey") String messageKey, @Param("locale") String locale);

    List<I18nMessageEntity> selectByLocaleAndScopes(@Param("locale") String locale, @Param("scopes") List<String> scopes);

    List<I18nMessageEntity> selectList(I18nMessagePageQuery query);

    List<I18nMessageEntity> selectPage(I18nMessagePageQuery query);

    long countByQuery(I18nMessagePageQuery query);

    int insert(I18nMessageEntity entity);

    int updateById(I18nMessageEntity entity);

    int deleteById(@Param("id") String id);
}
