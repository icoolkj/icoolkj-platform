package com.icoolkj.platform.workflow.support;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 解析 wf_model.process_config，提供发起 ACL 与通知开关 */
@Component
public class WorkflowProcessConfigSupport {

    private final ObjectMapper objectMapper;

    public WorkflowProcessConfigSupport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ProcessConfig parse(String json) {
        if (!StringUtils.hasText(json)) {
            return ProcessConfig.empty();
        }
        try {
            ProcessConfig config = objectMapper.readValue(json, ProcessConfig.class);
            return config == null ? ProcessConfig.empty() : config;
        } catch (Exception ignored) {
            return ProcessConfig.empty();
        }
    }

    /**
     * startAcl 活动维（user/dept/role/post）均为空则放行；否则任一命中即可。
     * {@code tenantIds} 为平台跨租户预留字段，本版不参与判定。
     */
    public boolean isStartAllowed(ProcessConfig config, String userId, String deptId,
                                  Collection<String> roleCodes, Collection<String> postIds) {
        StartAcl acl = config == null ? StartAcl.empty() : nullSafe(config.startAcl());
        boolean noUser = CollectionUtils.isEmpty(acl.userIds());
        boolean noDept = CollectionUtils.isEmpty(acl.deptIds());
        boolean noRole = CollectionUtils.isEmpty(acl.roleCodes());
        boolean noPost = CollectionUtils.isEmpty(acl.postIds());
        if (noUser && noDept && noRole && noPost) {
            return true;
        }
        if (!noUser && StringUtils.hasText(userId) && containsIgnoreBlank(acl.userIds(), userId)) {
            return true;
        }
        if (!noDept && StringUtils.hasText(deptId) && containsIgnoreBlank(acl.deptIds(), deptId)) {
            return true;
        }
        if (!noRole && !CollectionUtils.isEmpty(roleCodes)) {
            Set<String> allowed = Set.copyOf(acl.roleCodes().stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .toList());
            for (String code : roleCodes) {
                if (StringUtils.hasText(code) && allowed.contains(code.trim())) {
                    return true;
                }
            }
        }
        if (!noPost && !CollectionUtils.isEmpty(postIds)) {
            Set<String> allowed = Set.copyOf(acl.postIds().stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .toList());
            for (String postId : postIds) {
                if (StringUtils.hasText(postId) && allowed.contains(postId.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static StartAcl nullSafe(StartAcl acl) {
        return acl == null ? StartAcl.empty() : acl;
    }

    public static Notify nullSafe(Notify notify) {
        return notify == null ? Notify.defaults() : notify;
    }

    private static boolean containsIgnoreBlank(List<String> values, String target) {
        String needle = target.trim();
        for (String value : values) {
            if (StringUtils.hasText(value) && needle.equals(value.trim())) {
                return true;
            }
        }
        return false;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProcessConfig {
        private StartAcl startAcl;
        private Behavior behavior;
        private Notify notify;
        /** Design SoT：竖向设计节点权威快照（Map: schemaVersion + nodes） */
        private Object design;

        public static ProcessConfig empty() {
            ProcessConfig c = new ProcessConfig();
            c.startAcl = StartAcl.empty();
            c.behavior = Behavior.defaults();
            c.notify = Notify.defaults();
            return c;
        }

        public StartAcl getStartAcl() {
            return startAcl;
        }

        public void setStartAcl(StartAcl startAcl) {
            this.startAcl = startAcl;
        }

        public Behavior getBehavior() {
            return behavior;
        }

        public void setBehavior(Behavior behavior) {
            this.behavior = behavior;
        }

        public Notify getNotify() {
            return notify;
        }

        public void setNotify(Notify notify) {
            this.notify = notify;
        }

        public Object getDesign() {
            return design;
        }

        public void setDesign(Object design) {
            this.design = design;
        }

        public StartAcl startAcl() {
            return startAcl == null ? StartAcl.empty() : startAcl;
        }

        public Behavior behavior() {
            return behavior == null ? Behavior.defaults() : behavior;
        }

        public Notify notifyConfig() {
            return notify == null ? Notify.defaults() : notify;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StartAcl {
        private List<String> deptIds;
        private List<String> roleCodes;
        private List<String> userIds;
        private List<String> postIds;
        /** 平台跨租户可发起范围（预留，本版不参与 isStartAllowed） */
        private List<String> tenantIds;

        public static StartAcl empty() {
            StartAcl acl = new StartAcl();
            acl.deptIds = Collections.emptyList();
            acl.roleCodes = Collections.emptyList();
            acl.userIds = Collections.emptyList();
            acl.postIds = Collections.emptyList();
            acl.tenantIds = Collections.emptyList();
            return acl;
        }

        public List<String> getDeptIds() {
            return deptIds;
        }

        public void setDeptIds(List<String> deptIds) {
            this.deptIds = deptIds;
        }

        public List<String> getRoleCodes() {
            return roleCodes;
        }

        public void setRoleCodes(List<String> roleCodes) {
            this.roleCodes = roleCodes;
        }

        public List<String> getUserIds() {
            return userIds;
        }

        public void setUserIds(List<String> userIds) {
            this.userIds = userIds;
        }

        public List<String> getPostIds() {
            return postIds;
        }

        public void setPostIds(List<String> postIds) {
            this.postIds = postIds;
        }

        public List<String> getTenantIds() {
            return tenantIds;
        }

        public void setTenantIds(List<String> tenantIds) {
            this.tenantIds = tenantIds;
        }

        public List<String> deptIds() {
            return deptIds == null ? Collections.emptyList() : deptIds;
        }

        public List<String> roleCodes() {
            return roleCodes == null ? Collections.emptyList() : roleCodes;
        }

        public List<String> userIds() {
            return userIds == null ? Collections.emptyList() : userIds;
        }

        public List<String> postIds() {
            return postIds == null ? Collections.emptyList() : postIds;
        }

        public List<String> tenantIds() {
            return tenantIds == null ? Collections.emptyList() : tenantIds;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Behavior {
        private String titleTemplate;
        private String summaryTemplate;
        private Boolean allowWithdraw;
        private Boolean commentRequired;
        /** 同流程实例抄送去重：已抄送过的用户不再派发 */
        private Boolean autoDedup;

        public static Behavior defaults() {
            Behavior b = new Behavior();
            b.titleTemplate = "";
            b.summaryTemplate = "";
            b.allowWithdraw = true;
            b.commentRequired = false;
            b.autoDedup = false;
            return b;
        }

        public String getTitleTemplate() {
            return titleTemplate;
        }

        public void setTitleTemplate(String titleTemplate) {
            this.titleTemplate = titleTemplate;
        }

        public String getSummaryTemplate() {
            return summaryTemplate;
        }

        public void setSummaryTemplate(String summaryTemplate) {
            this.summaryTemplate = summaryTemplate;
        }

        public Boolean getAllowWithdraw() {
            return allowWithdraw;
        }

        public void setAllowWithdraw(Boolean allowWithdraw) {
            this.allowWithdraw = allowWithdraw;
        }

        public Boolean getCommentRequired() {
            return commentRequired;
        }

        public void setCommentRequired(Boolean commentRequired) {
            this.commentRequired = commentRequired;
        }

        public Boolean getAutoDedup() {
            return autoDedup;
        }

        public void setAutoDedup(Boolean autoDedup) {
            this.autoDedup = autoDedup;
        }

        public boolean isAutoDedup() {
            return Boolean.TRUE.equals(autoDedup);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Notify {
        private Boolean onReturn;
        private Boolean onReject;
        private Boolean onTodo;
        private Boolean onCc;
        private Boolean onComplete;
        private Boolean onTimeout;
        private Boolean onUrge;

        public static Notify defaults() {
            Notify n = new Notify();
            n.onReturn = false;
            n.onReject = true;
            n.onTodo = true;
            n.onCc = false;
            n.onComplete = true;
            n.onTimeout = false;
            n.onUrge = false;
            return n;
        }

        public boolean isOnReturn() {
            return Boolean.TRUE.equals(onReturn);
        }

        public boolean isOnReject() {
            return onReject == null || onReject;
        }

        public boolean isOnTodo() {
            return onTodo == null || onTodo;
        }

        public boolean isOnCc() {
            return Boolean.TRUE.equals(onCc);
        }

        public boolean isOnComplete() {
            return onComplete == null || onComplete;
        }

        public boolean isOnTimeout() {
            return Boolean.TRUE.equals(onTimeout);
        }

        public boolean isOnUrge() {
            return Boolean.TRUE.equals(onUrge);
        }

        public Boolean getOnReturn() {
            return onReturn;
        }

        public void setOnReturn(Boolean onReturn) {
            this.onReturn = onReturn;
        }

        public Boolean getOnReject() {
            return onReject;
        }

        public void setOnReject(Boolean onReject) {
            this.onReject = onReject;
        }

        public Boolean getOnTodo() {
            return onTodo;
        }

        public void setOnTodo(Boolean onTodo) {
            this.onTodo = onTodo;
        }

        public Boolean getOnCc() {
            return onCc;
        }

        public void setOnCc(Boolean onCc) {
            this.onCc = onCc;
        }

        public Boolean getOnComplete() {
            return onComplete;
        }

        public void setOnComplete(Boolean onComplete) {
            this.onComplete = onComplete;
        }

        public Boolean getOnTimeout() {
            return onTimeout;
        }

        public void setOnTimeout(Boolean onTimeout) {
            this.onTimeout = onTimeout;
        }

        public Boolean getOnUrge() {
            return onUrge;
        }

        public void setOnUrge(Boolean onUrge) {
            this.onUrge = onUrge;
        }
    }
}
