package com.icoolkj.platform.system.iprule.service;

/**
 * Evaluates IP access rules for login and admin scopes.
 */
public interface IpRuleMatchService {

    void ensureLoginAllowed(String tenantId, String clientIp);

    void ensureAdminAccessAllowed(String tenantId, String clientIp);
}
