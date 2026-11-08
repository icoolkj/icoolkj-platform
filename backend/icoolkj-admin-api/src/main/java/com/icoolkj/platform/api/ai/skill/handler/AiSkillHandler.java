package com.icoolkj.platform.api.ai.skill.handler;

/**
 * Executable binding for a {@code sys_ai_skill.skill_code}.
 * Metadata lives in DB; identity must come from security context, never from model args.
 */
public interface AiSkillHandler {

    /** Matches {@code sys_ai_skill.skill_code}. */
    String skillCode();

    /**
     * @param argumentsJson OpenAI tool arguments JSON (may be empty)
     * @return JSON string payload for the model
     */
    String execute(String argumentsJson);
}
