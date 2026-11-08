package com.icoolkj.platform.system.i18n.mapper;

import com.icoolkj.platform.system.i18n.domain.I18nLocaleEntity;
import com.icoolkj.platform.system.i18n.dto.I18nLocalePageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface I18nLocaleMapper {

    I18nLocaleEntity selectById(@Param("id") String id);

    I18nLocaleEntity selectByCode(@Param("localeCode") String localeCode);

    List<I18nLocaleEntity> selectList(I18nLocalePageQuery query);

    List<I18nLocaleEntity> selectEnabled();

    List<I18nLocaleEntity> selectPage(I18nLocalePageQuery query);

    long countByQuery(I18nLocalePageQuery query);

    int insert(I18nLocaleEntity entity);

    int updateById(I18nLocaleEntity entity);

    int deleteById(@Param("id") String id);

    int clearDefaultFlag();

    I18nLocaleEntity selectDefault();
}
