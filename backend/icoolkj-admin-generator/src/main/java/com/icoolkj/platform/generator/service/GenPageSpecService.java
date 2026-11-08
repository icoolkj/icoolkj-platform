package com.icoolkj.platform.generator.service;

import com.icoolkj.platform.generator.dto.GenPageSpecCompileRequest;
import com.icoolkj.platform.generator.dto.GenPageSpecGenerateRequest;
import com.icoolkj.platform.generator.dto.GenPageSpecPublishRequest;
import com.icoolkj.platform.generator.dto.GenPageSpecValidateRequest;
import com.icoolkj.platform.generator.vo.GenPageSpecCompileResultVO;
import com.icoolkj.platform.generator.vo.GenPageSpecDraftVO;
import com.icoolkj.platform.generator.vo.GenPageSpecValidateResultVO;

public interface GenPageSpecService {

    String getPromptTemplate();

    String generate(GenPageSpecGenerateRequest request);

    GenPageSpecValidateResultVO validate(GenPageSpecValidateRequest request);

    GenPageSpecCompileResultVO compile(GenPageSpecCompileRequest request);

    /** 从 gen_table 推导初始方案（步骤 1） */
    GenPageSpecDraftVO draftFromTable(String tableId, String pageKey, String layout);

    /** AI 根据用户需求优化方案（步骤 2） */
    GenPageSpecDraftVO analyze(GenPageSpecGenerateRequest request);

    /** 发布：方案 → 同步 gen_table → 写 Git（步骤 4，用户无感） */
    GenPageSpecCompileResultVO publish(GenPageSpecPublishRequest request);
}
