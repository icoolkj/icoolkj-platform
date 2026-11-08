package com.icoolkj.platform.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.icoolkj.platform.generator.domain.GenViewConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface GenViewConfigMapper {

    GenViewConfig selectByTableId(@Param("tableId") String tableId);

    GenViewConfig selectByBusinessAndModule(@Param("businessName") String businessName,
                                              @Param("apiModule") String apiModule);

    int insert(GenViewConfig row);

    int update(GenViewConfig row);

    int deleteByTableId(@Param("tableId") String tableId);
}
