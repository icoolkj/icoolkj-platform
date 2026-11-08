package com.icoolkj.platform.generator.service.impl;

import com.icoolkj.platform.common.constant.AiInvokeBizType;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.logging.LogContext;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantQueryScope;
import com.icoolkj.platform.generator.domain.ToolAiDdlContent;
import com.icoolkj.platform.generator.domain.ToolAiDdlRecord;
import com.icoolkj.platform.generator.domain.ToolAiInvokeContent;
import com.icoolkj.platform.generator.domain.ToolAiInvokeLog;
import com.icoolkj.platform.generator.dto.ToolAiDdlRecordPageQuery;
import com.icoolkj.platform.generator.dto.ToolAiInvokeLogPageQuery;
import com.icoolkj.platform.generator.mapper.ToolAiLogMapper;
import com.icoolkj.platform.generator.vo.ToolAiDdlRecordVO;
import com.icoolkj.platform.generator.vo.ToolAiInvokeLogVO;
import com.icoolkj.platform.system.ai.dto.AiChatMessageItem;
import com.icoolkj.platform.system.ai.client.OpenAiCompatibleChatClient;
import com.icoolkj.platform.system.ai.protocol.openai.OpenAiCompatibleEndpoints;
import com.icoolkj.platform.system.ai.service.AiRuntimeConfig;
import com.icoolkj.platform.generator.vo.AiChatResultVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AiChatFacade {

    private static final Logger log = LoggerFactory.getLogger(AiChatFacade.class);
    private static final int PREVIEW_LEN = 500;

    private final OpenAiCompatibleChatClient chatClient;
    private final ToolAiLogMapper toolAiLogMapper;

    public AiChatFacade(OpenAiCompatibleChatClient chatClient, ToolAiLogMapper toolAiLogMapper) {
        this.chatClient = chatClient;
        this.toolAiLogMapper = toolAiLogMapper;
    }

    public AiChatResultVO chat(AiRuntimeConfig config, String systemPrompt, String userMessage, String bizType) {
        return chat(config, systemPrompt, userMessage, bizType, null);
    }

    public AiChatResultVO chat(AiRuntimeConfig config, String systemPrompt, String userMessage,
                               String bizType, String bizRefId) {
        String invokeId = IdGeneratorUtils.nextIdStr();
        String requestUrl = OpenAiCompatibleEndpoints.absoluteUrl(
                config.getBaseUrl(), OpenAiCompatibleEndpoints.CHAT_COMPLETIONS);
        long start = System.currentTimeMillis();
        try {
            String content = chatClient.chat(config, systemPrompt, userMessage);
            int cost = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - start);
            saveInvoke(invokeId, config, bizType, bizRefId, requestUrl, "0", null, cost,
                    systemPrompt, userMessage, content);
            AiChatResultVO result = new AiChatResultVO();
            result.setContent(content);
            result.setInvokeId(invokeId);
            return result;
        } catch (BusinessException ex) {
            int cost = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - start);
            saveInvoke(invokeId, config, bizType, bizRefId, requestUrl, "1", ex.getMessage(), cost,
                    systemPrompt, userMessage, null);
            throw ex;
        } catch (Exception ex) {
            int cost = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - start);
            saveInvoke(invokeId, config, bizType, bizRefId, requestUrl, "1", ex.getMessage(), cost,
                    systemPrompt, userMessage, null);
            throw new BusinessException("error.ai.request_failed", ex.getMessage());
        }
    }

    public String saveDdlRecord(String invokeId, String recordType, String apiModule, String targetTableName,
                                String userPrompt, String summary, String ddl) {
        String id = IdGeneratorUtils.nextIdStr();
        LocalDateTime now = LocalDateTime.now();
        ToolAiDdlRecord record = new ToolAiDdlRecord();
        record.setId(id);
        record.setTenantId(currentTenantId());
        record.setTraceId(LogContext.getTraceId());
        record.setInvokeId(invokeId);
        record.setRecordType(recordType);
        record.setApiModule(apiModule);
        record.setTargetTableName(targetTableName);
        record.setGeneratedSummary(truncate(summary, 500));
        record.setUserPromptPreview(truncate(userPrompt, PREVIEW_LEN));
        record.setStatus("GENERATED");
        record.setOperatorId(SecurityContextHelper.getUserId());
        record.setUserId(SecurityContextHelper.getUserId());
        record.setOperatorName(SecurityContextHelper.getUsername());
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        toolAiLogMapper.insertDdlRecord(record);

        ToolAiDdlContent content = new ToolAiDdlContent();
        content.setDdlRecordId(id);
        content.setUserPrompt(userPrompt);
        content.setGeneratedDdl(ddl);
        toolAiLogMapper.insertDdlContent(content);

        if (StringUtils.isNotBlank(invokeId)) {
            toolAiLogMapper.updateInvokeBizRefId(invokeId, id);
        }
        return id;
    }

    public AiChatResultVO chatConversation(AiRuntimeConfig config, String systemPrompt,
                                           List<AiChatMessageItem> history, String userMessage, String sessionId) {
        List<AiChatMessageItem> payload = new ArrayList<>();
        if (StringUtils.isNotBlank(systemPrompt)) {
            payload.add(new AiChatMessageItem("system", systemPrompt));
        }
        if (history != null) {
            payload.addAll(history);
        }
        payload.add(new AiChatMessageItem("user", userMessage));

        String invokeId = IdGeneratorUtils.nextIdStr();
        String requestUrl = OpenAiCompatibleEndpoints.absoluteUrl(
                config.getBaseUrl(), OpenAiCompatibleEndpoints.CHAT_COMPLETIONS);
        long start = System.currentTimeMillis();
        try {
            String content = chatClient.chatMessages(config, payload);
            int cost = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - start);
            saveInvoke(invokeId, config, AiInvokeBizType.CHAT, sessionId, requestUrl, "0", null, cost,
                    systemPrompt, userMessage, content);
            AiChatResultVO result = new AiChatResultVO();
            result.setContent(content);
            result.setInvokeId(invokeId);
            return result;
        } catch (BusinessException ex) {
            int cost = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - start);
            saveInvoke(invokeId, config, AiInvokeBizType.CHAT, sessionId, requestUrl, "1", ex.getMessage(), cost,
                    systemPrompt, userMessage, null);
            throw ex;
        } catch (Exception ex) {
            int cost = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - start);
            saveInvoke(invokeId, config, AiInvokeBizType.CHAT, sessionId, requestUrl, "1", ex.getMessage(), cost,
                    systemPrompt, userMessage, null);
            throw new BusinessException("error.ai.request_failed", ex.getMessage());
        }
    }

    public void markDdlCopied(String ddlRecordId, String finalDdl) {
        requireDdlRecord(ddlRecordId);
        toolAiLogMapper.updateDdlStatus(ddlRecordId, "COPIED");
        if (StringUtils.isNotBlank(finalDdl)) {
            toolAiLogMapper.updateDdlFinalContent(ddlRecordId, finalDdl);
        }
    }

    public void markDdlExecuted(String ddlRecordId, String finalDdl) {
        requireDdlRecord(ddlRecordId);
        toolAiLogMapper.updateDdlStatus(ddlRecordId, "EXECUTED");
        if (StringUtils.isNotBlank(finalDdl)) {
            toolAiLogMapper.updateDdlFinalContent(ddlRecordId, finalDdl);
        }
    }

    public PageResult<ToolAiInvokeLogVO> pageInvoke(ToolAiInvokeLogPageQuery query) {
        TenantQueryScope.bindQueryTenantId(query::setTenantId);
        long total = toolAiLogMapper.countInvokeByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, toLong(query.getPageNo()), toLong(query.getPageSize()));
        }
        List<ToolAiInvokeLogVO> rows = toolAiLogMapper.selectInvokePage(query).stream().map(this::toInvokeVo).toList();
        return PageResult.of(rows, total, toLong(query.getPageNo()), toLong(query.getPageSize()));
    }

    public ToolAiInvokeLogVO getInvokeDetail(String id) {
        ToolAiInvokeLog entity = toolAiLogMapper.selectInvokeById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.ai_invoke_log");
        }
        TenantQueryScope.assertReadable(entity.getTenantId());
        ToolAiInvokeLogVO vo = toInvokeVo(entity);
        ToolAiInvokeContent content = toolAiLogMapper.selectInvokeContentById(id);
        if (content != null) {
            vo.setSystemPrompt(content.getSystemPrompt());
            vo.setUserMessage(content.getUserMessage());
            vo.setResponseContent(content.getResponseContent());
        }
        return vo;
    }

    public PageResult<ToolAiDdlRecordVO> pageDdl(ToolAiDdlRecordPageQuery query) {
        TenantQueryScope.bindQueryTenantId(query::setTenantId);
        long total = toolAiLogMapper.countDdlByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, toLong(query.getPageNo()), toLong(query.getPageSize()));
        }
        List<ToolAiDdlRecordVO> rows = toolAiLogMapper.selectDdlPage(query).stream().map(this::toDdlVo).toList();
        return PageResult.of(rows, total, toLong(query.getPageNo()), toLong(query.getPageSize()));
    }

    public ToolAiDdlRecordVO getDdlDetail(String id) {
        ToolAiDdlRecord entity = toolAiLogMapper.selectDdlById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.ai_ddl_log");
        }
        TenantQueryScope.assertReadable(entity.getTenantId());
        ToolAiDdlRecordVO vo = toDdlVo(entity);
        ToolAiDdlContent content = toolAiLogMapper.selectDdlContentById(id);
        if (content != null) {
            vo.setUserPrompt(content.getUserPrompt());
            vo.setGeneratedDdl(content.getGeneratedDdl());
            vo.setFinalDdl(content.getFinalDdl());
        }
        return vo;
    }

    private void saveInvoke(String invokeId, AiRuntimeConfig config, String bizType, String bizRefId,
                            String requestUrl, String status, String errorMessage, int costTime,
                            String systemPrompt, String userMessage, String responseContent) {
        try {
            ToolAiInvokeLog row = new ToolAiInvokeLog();
            row.setId(invokeId);
            row.setTenantId(currentTenantId());
            row.setTraceId(LogContext.getTraceId());
            row.setProviderName(StringUtils.defaultIfBlank(config.getProviderName(), "AI"));
            row.setModel(config.getModel());
            row.setBizType(bizType);
            row.setBizRefId(bizRefId);
            row.setRequestUrl(requestUrl);
            row.setResponseStatus(status);
            row.setErrorMessage(truncate(errorMessage, 1000));
            row.setCostTime(costTime);
            row.setUserPromptPreview(truncate(userMessage, PREVIEW_LEN));
            row.setResponsePreview(truncate(responseContent, PREVIEW_LEN));
            row.setOperatorId(SecurityContextHelper.getUserId());
            row.setOperatorName(SecurityContextHelper.getUsername());
            row.setCreatedAt(LocalDateTime.now());
            toolAiLogMapper.insertInvokeLog(row);

            ToolAiInvokeContent content = new ToolAiInvokeContent();
            content.setInvokeId(invokeId);
            content.setSystemPrompt(systemPrompt);
            content.setUserMessage(userMessage);
            content.setResponseContent(responseContent);
            toolAiLogMapper.insertInvokeContent(content);
        } catch (Exception ex) {
            log.warn("Save AI invoke log failed, invokeId={}", invokeId, ex);
        }
    }

    private ToolAiInvokeLogVO toInvokeVo(ToolAiInvokeLog entity) {
        ToolAiInvokeLogVO vo = new ToolAiInvokeLogVO();
        vo.setId(entity.getId());
        vo.setTraceId(entity.getTraceId());
        vo.setProviderName(entity.getProviderName());
        vo.setModel(entity.getModel());
        vo.setBizType(entity.getBizType());
        vo.setBizRefId(entity.getBizRefId());
        vo.setRequestUrl(entity.getRequestUrl());
        vo.setResponseStatus(entity.getResponseStatus());
        vo.setErrorMessage(entity.getErrorMessage());
        vo.setCostTime(entity.getCostTime());
        vo.setUserPromptPreview(entity.getUserPromptPreview());
        vo.setResponsePreview(entity.getResponsePreview());
        vo.setOperatorName(entity.getOperatorName());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    private ToolAiDdlRecordVO toDdlVo(ToolAiDdlRecord entity) {
        ToolAiDdlRecordVO vo = new ToolAiDdlRecordVO();
        vo.setId(entity.getId());
        vo.setTraceId(entity.getTraceId());
        vo.setInvokeId(entity.getInvokeId());
        vo.setRecordType(entity.getRecordType());
        vo.setApiModule(entity.getApiModule());
        vo.setTargetTableName(entity.getTargetTableName());
        vo.setGeneratedSummary(entity.getGeneratedSummary());
        vo.setUserPromptPreview(entity.getUserPromptPreview());
        vo.setStatus(entity.getStatus());
        vo.setOperatorName(entity.getOperatorName());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private ToolAiDdlRecord requireDdlRecord(String ddlRecordId) {
        ToolAiDdlRecord entity = toolAiLogMapper.selectDdlById(ddlRecordId);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.ai_ddl_log");
        }
        TenantQueryScope.assertReadable(entity.getTenantId());
        return entity;
    }

    private static String currentTenantId() {
        String tenantId = SecurityContextHelper.getTenantId();
        return StringUtils.isNotBlank(tenantId) ? tenantId : TenantConstants.PLATFORM_TENANT_ID;
    }

    private static String truncate(String text, int max) {
        if (text == null || max <= 0) {
            return text;
        }
        if (text.length() <= max) {
            return text;
        }
        if (max == 1) {
            return "…";
        }
        return text.substring(0, max - 1) + "…";
    }

    private static Long toLong(Integer value) {
        return value == null ? 1L : value.longValue();
    }
}
