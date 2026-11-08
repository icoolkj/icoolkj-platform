package com.icoolkj.platform.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.icoolkj.platform.generator.domain.GenTableColumn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface GenTableColumnMapper {

    List<GenTableColumn> selectGenTableColumnListByTableId(@Param("tableId") String tableId);

    List<GenTableColumn> selectDbTableColumnsByName(@Param("tableName") String tableName);

    int insertGenTableColumn(GenTableColumn column);

    int updateGenTableColumn(GenTableColumn column);

    int deleteGenTableColumnByTableId(@Param("tableId") String tableId);
}
