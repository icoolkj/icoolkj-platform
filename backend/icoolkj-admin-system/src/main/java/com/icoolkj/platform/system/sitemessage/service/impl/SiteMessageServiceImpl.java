package com.icoolkj.platform.system.sitemessage.service.impl;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.constant.SiteMessageConstants;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.enums.YesNoEnum;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.datascope.DataScopeSpec;
import com.icoolkj.platform.framework.datascope.OwnershipColumns;
import com.icoolkj.platform.framework.datascope.RowAccessSupport;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.security.SecurityContextHelper;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.system.sitemessage.convert.SiteMessageConvert;
import com.icoolkj.platform.system.sitemessage.domain.SiteMessageEntity;
import com.icoolkj.platform.system.sitemessage.domain.SiteMessageRecipientEntity;
import com.icoolkj.platform.system.user.domain.UserEntity;
import com.icoolkj.platform.system.sitemessage.dto.SiteMessageBatchDeleteRequest;
import com.icoolkj.platform.system.sitemessage.dto.SiteMessageInboxPageQuery;
import com.icoolkj.platform.system.sitemessage.dto.SiteMessagePageQuery;
import com.icoolkj.platform.system.sitemessage.dto.SiteMessageSendRequest;
import com.icoolkj.platform.system.sitemessage.mapper.SiteMessageMapper;
import com.icoolkj.platform.system.sitemessage.mapper.SiteMessageRecipientMapper;
import com.icoolkj.platform.system.user.mapper.UserMapper;
import com.icoolkj.platform.system.user.mapper.UserRoleMapper;
import com.icoolkj.platform.system.sitemessage.service.SiteMessageService;
import com.icoolkj.platform.system.sitemessage.dto.SiteMessageRecipientPageQuery;
import com.icoolkj.platform.system.sitemessage.vo.SiteMessageInboxVO;
import com.icoolkj.platform.system.sitemessage.vo.SiteMessageRecipientStatusVO;
import com.icoolkj.platform.system.sitemessage.vo.SiteMessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/** 站内信服务实现 */
@Service
public class SiteMessageServiceImpl implements SiteMessageService {

    private static final Logger log = LoggerFactory.getLogger(SiteMessageServiceImpl.class);

    private final SiteMessageMapper siteMessageMapper;
    private final SiteMessageRecipientMapper siteMessageRecipientMapper;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RowAccessSupport rowAccessSupport;

    public SiteMessageServiceImpl(SiteMessageMapper siteMessageMapper,
                                  SiteMessageRecipientMapper siteMessageRecipientMapper,
                                  UserMapper userMapper,
                                  UserRoleMapper userRoleMapper,
                                  RowAccessSupport rowAccessSupport) {
        this.siteMessageMapper = siteMessageMapper;
        this.siteMessageRecipientMapper = siteMessageRecipientMapper;
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.rowAccessSupport = rowAccessSupport;
    }

    @Override
    public PageResult<SiteMessageVO> page(SiteMessagePageQuery query) {
        query.setTenantId(resolveAdminTenantScope(query.getTenantId()));
        if (isPlatformSuperAdmin()) {
            query.setDataScopeSql("");
        }
        return runAsPlatformAdmin(() -> doPage(query));
    }

    private PageResult<SiteMessageVO> doPage(SiteMessagePageQuery query) {
        long total = siteMessageMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<SiteMessageEntity> list = siteMessageMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        List<SiteMessageVO> rows = list.stream().map(entity -> {
            SiteMessageVO vo = SiteMessageConvert.toVo(entity);
            vo.setRecipientCount(siteMessageRecipientMapper.countByMessageId(entity.getId()));
            return vo;
        }).toList();
        return PageResult.of(rows, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public SiteMessageVO getById(String id) {
        SiteMessageEntity entity = requireMessage(id);
        assertAdminReadable(entity);
        SiteMessageVO vo = SiteMessageConvert.toVo(entity);
        vo.setRecipientCount(runAsPlatformAdmin(() -> siteMessageRecipientMapper.countByMessageId(entity.getId())));
        return vo;
    }

    @Override
    public PageResult<SiteMessageRecipientStatusVO> recipientPage(String messageId, SiteMessageRecipientPageQuery query) {
        SiteMessageEntity entity = requireMessage(messageId);
        assertAdminReadable(entity);
        query.setMessageId(messageId);
        return runAsPlatformAdmin(() -> doRecipientPage(query));
    }

    private PageResult<SiteMessageRecipientStatusVO> doRecipientPage(SiteMessageRecipientPageQuery query) {
        long total = siteMessageRecipientMapper.countRecipientStatusByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<SiteMessageRecipientStatusVO> rows = siteMessageRecipientMapper.selectRecipientStatusPage(query);
        return PageResult.of(rows, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String send(SiteMessageSendRequest request) {
        List<String> targetTenantIds = resolveSendTenantIds(request);
        String senderId = requireUserId();
        UserEntity sender = requireUser(senderId);
        String lastMessageId = null;
        for (String targetTenantId : targetTenantIds) {
            assertCategoryAllowed(request.getMessageCategory().trim(), sender, targetTenantId);
            Set<String> recipientUserIds = resolveRecipientUserIds(request, targetTenantId);
            if (recipientUserIds.isEmpty()) {
                continue;
            }
            lastMessageId = sendToTenant(request, sender, targetTenantId, recipientUserIds);
        }
        if (lastMessageId == null) {
            throw new BusinessException("error.siteMessage.recipient_required");
        }
        return lastMessageId;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void notifyUsers(List<String> userIds, String subject, String content) {
        List<String> ids = normalizeIds(userIds);
        if (ids.isEmpty() || !StringUtils.hasText(subject) || !StringUtils.hasText(content)) {
            return;
        }
        List<UserEntity> users = userMapper.selectBriefByIds(ids);
        if (CollectionUtils.isEmpty(users)) {
            log.warn("Workflow site notify skipped: no active users for ids={}", ids);
            return;
        }
        Map<String, Set<String>> byTenant = new LinkedHashMap<>();
        for (UserEntity user : users) {
            if (user == null || !StringUtils.hasText(user.getId()) || !StringUtils.hasText(user.getTenantId())) {
                continue;
            }
            if (!CommonConstants.STATUS_NORMAL.equals(user.getStatus())) {
                continue;
            }
            byTenant
                    .computeIfAbsent(user.getTenantId().trim(), k -> new LinkedHashSet<>())
                    .add(user.getId().trim());
        }
        if (byTenant.isEmpty()) {
            log.warn("Workflow site notify skipped: recipients filtered empty, ids={}", ids);
            return;
        }
        UserEntity sender = requireUser(requireUserId());
        SiteMessageSendRequest request = new SiteMessageSendRequest();
        request.setMessageSubject(subject.trim());
        request.setMessageContent(content.trim());
        request.setMessageCategory(SiteMessageConstants.CATEGORY_BUSINESS);
        for (Map.Entry<String, Set<String>> entry : byTenant.entrySet()) {
            sendToTenant(request, sender, entry.getKey(), entry.getValue());
        }
    }

    private String sendToTenant(SiteMessageSendRequest request, UserEntity sender, String targetTenantId,
                                Set<String> recipientUserIds) {
        LocalDateTime now = LocalDateTime.now();
        SiteMessageEntity message = new SiteMessageEntity();
        message.setId(IdGeneratorUtils.nextIdStr());
        message.setTenantId(targetTenantId);
        message.setMessageSubject(request.getMessageSubject().trim());
        message.setMessageCategory(request.getMessageCategory().trim());
        message.setMessageContent(request.getMessageContent().trim());
        message.setSenderId(sender.getId());
        OwnershipColumns.applyUser(message::setUserId, sender.getId());
        message.setSenderName(resolveDisplayName(sender));
        message.setSentAt(now);
        message.setCreatedBy(sender.getId());
        message.setCreatedAt(now);
        message.setUpdatedBy(sender.getId());
        message.setUpdatedAt(now);
        message.setDeleted(0);
        siteMessageMapper.insert(message);

        List<SiteMessageRecipientEntity> recipients = new ArrayList<>(recipientUserIds.size());
        for (String userId : recipientUserIds) {
            SiteMessageRecipientEntity recipient = new SiteMessageRecipientEntity();
            recipient.setId(IdGeneratorUtils.nextIdStr());
            recipient.setTenantId(targetTenantId);
            recipient.setMessageId(message.getId());
            recipient.setUserId(userId);
            recipient.setReadAt(null);
            recipient.setDeleted(0);
            recipient.setCreatedAt(now);
            recipients.add(recipient);
        }
        siteMessageRecipientMapper.batchInsert(recipients);
        return message.getId();
    }

    private List<String> resolveSendTenantIds(SiteMessageSendRequest request) {
        if (isPlatformSuperAdmin() && !CollectionUtils.isEmpty(request.getTargetTenantIds())) {
            List<String> tenantIds = normalizeIds(request.getTargetTenantIds());
            if (tenantIds.isEmpty()) {
                throw new BusinessException("error.siteMessage.target_tenant_required");
            }
            return tenantIds;
        }
        return List.of(resolveTargetTenantId(request.getTargetTenantId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        SiteMessageEntity entity = requireMessage(id);
        assertAdminReadable(entity);
        runAsPlatformAdmin(() -> {
            siteMessageRecipientMapper.deleteByMessageId(id);
            siteMessageMapper.deleteById(id);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(SiteMessageBatchDeleteRequest request) {
        if (CollectionUtils.isEmpty(request.getIds())) {
            return;
        }
        runAsPlatformAdmin(() -> {
            for (String id : request.getIds()) {
                SiteMessageEntity entity = siteMessageMapper.selectById(id);
                if (entity == null) {
                    continue;
                }
                assertAdminReadable(entity);
            }
            siteMessageRecipientMapper.deleteByMessageIds(request.getIds());
            siteMessageMapper.deleteByIds(request.getIds());
        });
    }

    @Override
    public PageResult<SiteMessageInboxVO> inboxPage(SiteMessageInboxPageQuery query) {
        query.setTenantId(requireTenantId());
        query.setUserId(requireUserId());
        long total = siteMessageRecipientMapper.countInboxByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<SiteMessageInboxVO> rows = siteMessageRecipientMapper.selectInboxPage(query);
        return PageResult.of(rows, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SiteMessageInboxVO inboxDetail(String recipientId, String tenantId, String userId) {
        SiteMessageInboxVO vo = siteMessageRecipientMapper.selectInboxDetail(recipientId, tenantId, userId);
        if (vo == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.siteMessage");
        }
        if (vo.getReadAt() == null) {
            siteMessageRecipientMapper.markRead(recipientId, tenantId, userId, LocalDateTime.now());
            vo = siteMessageRecipientMapper.selectInboxDetail(recipientId, tenantId, userId);
        }
        return vo;
    }

    @Override
    public long countUnread(String tenantId, String userId) {
        SiteMessageInboxPageQuery query = new SiteMessageInboxPageQuery();
        query.setTenantId(tenantId);
        query.setUserId(userId);
        query.setReadStatus(CommonConstants.NO);
        return siteMessageRecipientMapper.countInboxByQuery(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(String recipientId, String tenantId, String userId) {
        requireInboxOwned(recipientId, tenantId, userId);
        siteMessageRecipientMapper.markRead(recipientId, tenantId, userId, LocalDateTime.now());
    }

    private SiteMessageEntity requireMessage(String id) {
        SiteMessageEntity entity = runAsPlatformAdmin(() -> siteMessageMapper.selectById(id));
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.siteMessage");
        }
        return entity;
    }

    private <T> T runAsPlatformAdmin(Supplier<T> supplier) {
        if (isPlatformSuperAdmin()) {
            return TenantScopeRunner.runIgnoringTenant(supplier);
        }
        return supplier.get();
    }

    private void runAsPlatformAdmin(Runnable runnable) {
        if (isPlatformSuperAdmin()) {
            TenantScopeRunner.runIgnoringTenant(runnable);
            return;
        }
        runnable.run();
    }

    private void requireInboxOwned(String recipientId, String tenantId, String userId) {
        SiteMessageInboxVO vo = siteMessageRecipientMapper.selectInboxDetail(recipientId, tenantId, userId);
        if (vo == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.siteMessage");
        }
    }

    private UserEntity requireUser(String userId) {
        UserEntity entity = userMapper.selectById(userId);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.user");
        }
        return entity;
    }

    private String requireUserId() {
        String userId = SecurityContextHelper.getUserId();
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("error.auth.unauthorized");
        }
        return userId;
    }

    private String requireTenantId() {
        String tenantId = SecurityContextHelper.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
            throw new BusinessException("error.auth.unauthorized");
        }
        return tenantId;
    }

    private String resolveAdminTenantScope(String requestedTenantId) {
        if (isPlatformSuperAdmin()) {
            return StringUtils.hasText(requestedTenantId) ? requestedTenantId.trim() : null;
        }
        return requireTenantId();
    }

    private String resolveTargetTenantId(String requestedTenantId) {
        if (isPlatformSuperAdmin() && StringUtils.hasText(requestedTenantId)) {
            return requestedTenantId.trim();
        }
        return requireTenantId();
    }

    /** Platform super-admin L3 bypass (ops whitelist). */
    private void assertAdminReadable(SiteMessageEntity entity) {
        if (isPlatformSuperAdmin()) {
            return;
        }
        if (!StringUtils.hasText(entity.getUserId())) {
            throw new BusinessException("error.forbidden");
        }
        rowAccessSupport.assertReadable(
                entity.getTenantId(), DataScopeSpec.userOnly(), entity.getUserId(), null);
    }

    private void assertCategoryAllowed(String category, UserEntity sender, String targetTenantId) {
        if (SiteMessageConstants.CATEGORY_BUSINESS.equals(category)) {
            return;
        }
        if (SiteMessageConstants.CATEGORY_SYSTEM.equals(category)) {
            if (isPlatformSuperAdmin() || isTenantAdmin(sender, targetTenantId)) {
                return;
            }
            throw new BusinessException("error.siteMessage.category_forbidden");
        }
        throw new BusinessException("error.validation.invalid", "fields.messageCategory");
    }

    private boolean isPlatformSuperAdmin() {
        String tenantId = SecurityContextHelper.getTenantId();
        String userId = SecurityContextHelper.getUserId();
        if (!StringUtils.hasText(userId)) {
            return false;
        }
        if (TenantConstants.PLATFORM_TENANT_ID.equals(tenantId)) {
            UserEntity user = userMapper.selectById(userId);
            return user != null && YesNoEnum.isYes(user.getIsSuperAdmin());
        }
        return false;
    }

    private boolean isTenantAdmin(UserEntity sender, String tenantId) {
        if (sender == null) {
            return false;
        }
        if (YesNoEnum.isYes(sender.getIsSuperAdmin())) {
            return true;
        }
        return userRoleMapper.existsSuperRoleByTenantAndUser(tenantId, sender.getId());
    }

    private Set<String> resolveRecipientUserIds(SiteMessageSendRequest request, String tenantId) {
        return TenantScopeRunner.runIgnoringTenant(() -> doResolveRecipientUserIds(request, tenantId));
    }

    private Set<String> doResolveRecipientUserIds(SiteMessageSendRequest request, String tenantId) {
        String type = request.getRecipientType().trim().toLowerCase(Locale.ROOT);
        Set<String> userIds = new LinkedHashSet<>();
        switch (type) {
            case SiteMessageConstants.TARGET_ALL -> userIds.addAll(userMapper.selectActiveUserIdsByTenant(tenantId));
            case SiteMessageConstants.TARGET_USER -> {
                if (!CollectionUtils.isEmpty(request.getUserIds())) {
                    for (String userId : request.getUserIds()) {
                        if (StringUtils.hasText(userId)) {
                            userIds.add(userId.trim());
                        }
                    }
                }
                userIds.retainAll(new LinkedHashSet<>(userMapper.selectActiveUserIdsByTenant(tenantId)));
            }
            case SiteMessageConstants.TARGET_DEPT -> {
                if (!CollectionUtils.isEmpty(request.getDeptIds())) {
                    userIds.addAll(userMapper.selectActiveUserIdsByDeptIds(tenantId, normalizeIds(request.getDeptIds())));
                }
            }
            case SiteMessageConstants.TARGET_ROLE -> {
                if (!CollectionUtils.isEmpty(request.getRoleIds())) {
                    userIds.addAll(userMapper.selectActiveUserIdsByRoleIds(tenantId, normalizeIds(request.getRoleIds())));
                }
            }
            default -> throw new BusinessException("error.validation.invalid", "fields.recipientType");
        }
        return userIds;
    }

    private List<String> normalizeIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return ids.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private String resolveDisplayName(UserEntity user) {
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname().trim();
        }
        if (StringUtils.hasText(user.getRealName())) {
            return user.getRealName().trim();
        }
        return user.getUsername();
    }
}
