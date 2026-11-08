package com.icoolkj.platform.api.ai.skill.dto;

import java.util.ArrayList;
import java.util.List;

public class OfficeAiChatResultVO {

    private String sessionId;
    private String reply;
    private List<String> usedSkills = new ArrayList<>();

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public List<String> getUsedSkills() {
        return usedSkills;
    }

    public void setUsedSkills(List<String> usedSkills) {
        this.usedSkills = usedSkills;
    }
}
