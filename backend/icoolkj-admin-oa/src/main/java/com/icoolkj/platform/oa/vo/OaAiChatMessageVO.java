package com.icoolkj.platform.oa.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OaAiChatMessageVO {

    private String id;
    private String sessionId;
    private String role;
    private String content;
    private List<String> usedSkills = new ArrayList<>();
    private LocalDateTime createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getUsedSkills() {
        return usedSkills;
    }

    public void setUsedSkills(List<String> usedSkills) {
        this.usedSkills = usedSkills;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
