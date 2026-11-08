package com.icoolkj.platform.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.icoolkj.platform.generator.domain.ToolAiDdlContent;
import com.icoolkj.platform.generator.domain.ToolAiDdlRecord;
import com.icoolkj.platform.generator.domain.ToolAiInvokeContent;
import com.icoolkj.platform.generator.domain.ToolAiInvokeLog;
import com.icoolkj.platform.generator.dto.ToolAiDdlRecordPageQuery;
import com.icoolkj.platform.generator.dto.ToolAiInvokeLogPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface ToolAiLogMapper {

    int insertInvokeLog(ToolAiInvokeLog log);

    int insertInvokeContent(ToolAiInvokeContent content);

    ToolAiInvokeLog selectInvokeById(@Param("id") String id);

    ToolAiInvokeContent selectInvokeContentById(@Param("invokeId") String invokeId);

    long countInvokeByQuery(ToolAiInvokeLogPageQuery query);

    List<ToolAiInvokeLog> selectInvokePage(ToolAiInvokeLogPageQuery query);

    int updateInvokeBizRefId(@Param("id") String id, @Param("bizRefId") String bizRefId);

    int insertDdlRecord(ToolAiDdlRecord record);

    int insertDdlContent(ToolAiDdlContent content);

    int updateDdlStatus(@Param("id") String id, @Param("status") String status);

    int updateDdlFinalContent(@Param("ddlRecordId") String ddlRecordId, @Param("finalDdl") String finalDdl);

    ToolAiDdlRecord selectDdlById(@Param("id") String id);

    ToolAiDdlContent selectDdlContentById(@Param("ddlRecordId") String ddlRecordId);

    long countDdlByQuery(ToolAiDdlRecordPageQuery query);

    List<ToolAiDdlRecord> selectDdlPage(ToolAiDdlRecordPageQuery query);
}
