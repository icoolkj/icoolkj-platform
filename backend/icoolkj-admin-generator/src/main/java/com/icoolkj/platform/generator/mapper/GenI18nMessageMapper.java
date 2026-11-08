package com.icoolkj.platform.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.icoolkj.platform.generator.domain.GenI18nMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface GenI18nMessageMapper {

    List<GenI18nMessage> selectByTableId(@Param("tableId") String tableId);

    GenI18nMessage selectByTableIdAndLocale(@Param("tableId") String tableId, @Param("locale") String locale);

    GenI18nMessage selectByBusinessAndLocale(@Param("businessName") String businessName,
                                               @Param("apiModule") String apiModule,
                                               @Param("locale") String locale);

    List<GenI18nMessage> selectAllByLocale(@Param("locale") String locale);

    int insert(GenI18nMessage row);

    int update(GenI18nMessage row);

    int deleteByTableId(@Param("tableId") String tableId);
}
