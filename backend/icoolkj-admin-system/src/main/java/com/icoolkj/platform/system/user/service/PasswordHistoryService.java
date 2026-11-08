package com.icoolkj.platform.system.user.service;

/**
 * Prevents password reuse against recent password hashes.
 */
public interface PasswordHistoryService {

    /**
     * Rejects when {@code rawPassword} matches the current hash or any of the recent history entries.
     *
     * @param currentPasswordHash current {@code sys_user.password_hash} (may be null for brand-new users)
     */
    void assertNotReused(String tenantId, String userId, String currentPasswordHash, String rawPassword);

    /** Persists a password hash into history and trims to configured max count. */
    void record(String tenantId, String userId, String passwordHash);
}
