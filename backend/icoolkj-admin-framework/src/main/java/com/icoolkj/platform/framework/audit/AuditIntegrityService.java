package com.icoolkj.platform.framework.audit;

import com.icoolkj.platform.common.constant.CacheKeyConstants;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.framework.cache.CacheTtlProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Per-tenant hash chain tips for audit log integrity.
 * After live-row archival/purge, callers must {@link #reanchor} remaining rows so verify starts at GENESIS again.
 */
@Service
public class AuditIntegrityService {

    public static final String GENESIS = "GENESIS";
    public static final String CHAIN_LOGIN = "login";
    public static final String CHAIN_OPERATION = "operation";
    public static final String CHAIN_API_ACCESS = "api_access";

    private final StringRedisTemplate stringRedisTemplate;
    private final CacheTtlProperties cacheTtlProperties;

    public AuditIntegrityService(StringRedisTemplate stringRedisTemplate,
                                 CacheTtlProperties cacheTtlProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.cacheTtlProperties = cacheTtlProperties;
    }

    public ChainHashes next(String chainName, String tenantId, String canonicalPayload,
                            Supplier<String> dbTipLoader) {
        String tipKey = tipKey(chainName, tenantId);
        String prev = stringRedisTemplate.opsForValue().get(tipKey);
        if (!StringUtils.hasText(prev) && dbTipLoader != null) {
            prev = dbTipLoader.get();
        }
        if (!StringUtils.hasText(prev)) {
            prev = GENESIS;
        }
        String rowHash = sha256Hex(prev + "|" + nullToEmpty(canonicalPayload));
        stringRedisTemplate.opsForValue().set(tipKey, rowHash, Duration.ofDays(400));
        return new ChainHashes(prev, rowHash);
    }

    /**
     * Rebuild live-chain hashes from GENESIS after archival removed prefix rows.
     *
     * @param rowsOrdered insertion order (prefer snowflake {@code id} ASC)
     * @param payloadFn   same payload contract as writers / verify
     * @param hashWriter  persist {@code prev_hash}/{@code row_hash} for each row
     */
    public <T> void reanchor(String chainName,
                             String tenantId,
                             List<T> rowsOrdered,
                             Function<T, String> payloadFn,
                             BiConsumer<T, ChainHashes> hashWriter) {
        String tipKey = tipKey(chainName, tenantId);
        if (CollectionUtils.isEmpty(rowsOrdered)) {
            stringRedisTemplate.delete(tipKey);
            return;
        }
        String prev = GENESIS;
        for (T row : rowsOrdered) {
            String payload = payloadFn.apply(row);
            String rowHash = sha256Hex(prev + "|" + nullToEmpty(payload));
            ChainHashes hashes = new ChainHashes(prev, rowHash);
            hashWriter.accept(row, hashes);
            prev = rowHash;
        }
        stringRedisTemplate.opsForValue().set(tipKey, prev, Duration.ofDays(400));
    }

    public void clearTip(String chainName, String tenantId) {
        stringRedisTemplate.delete(tipKey(chainName, tenantId));
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    public static String joinPayload(Object... parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append('|');
            }
            sb.append(nullToEmpty(parts[i] == null ? null : String.valueOf(parts[i])));
        }
        return sb.toString();
    }

    private String tipKey(String chainName, String tenantId) {
        String tid = StringUtils.hasText(tenantId) ? tenantId : TenantConstants.PLATFORM_TENANT_ID;
        return cacheTtlProperties.getKeyPrefix() + ":" + CacheKeyConstants.AUDIT_CHAIN_TIP
                + chainName + ":" + tid;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public record ChainHashes(String prevHash, String rowHash) {
    }
}
