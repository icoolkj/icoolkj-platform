package com.icoolkj.platform.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.icoolkj.platform.generator.domain.GenTable;
import com.icoolkj.platform.generator.dto.GenTablePageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface GenTableMapper {

    List<GenTable> selectGenTablePage(GenTablePageQuery query);

    long countGenTableByQuery(GenTablePageQuery query);

    List<GenTable> selectGenTableList(GenTable query);

    GenTable selectGenTableById(@Param("id") String id);

    GenTable selectGenTableByName(@Param("tableName") String tableName);

    List<GenTable> selectDbTableList(GenTable query);

    List<GenTable> selectDbTablePage(GenTablePageQuery query);

    long countDbTableByQuery(GenTablePageQuery query);

    List<GenTable> selectDbTableListByNames(@Param("tableNames") String[] tableNames);

    List<GenTable> selectWizardDbTableList(GenTable query);

    List<GenTable> selectWizardDbTablePage(GenTablePageQuery query);

    long countWizardDbTableByQuery(GenTablePageQuery query);

    long countTableInDb(@Param("tableName") String tableName);

    void executeDdl(@Param("ddl") String ddl);

    int insertGenTable(GenTable table);

    int updateGenTable(GenTable table);

    int deleteGenTableByIds(@Param("ids") String[] ids);
}
