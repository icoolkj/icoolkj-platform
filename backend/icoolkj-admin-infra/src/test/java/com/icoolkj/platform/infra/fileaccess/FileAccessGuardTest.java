package com.icoolkj.platform.infra.fileaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.icoolkj.platform.common.constant.CommonConstants;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.infra.domain.InfraFileAccessPolicyEntity;
import com.icoolkj.platform.infra.domain.InfraFileEntity;
import com.icoolkj.platform.infra.mapper.InfraFileAccessPolicyMapper;
import com.icoolkj.platform.infra.storage.StorageProperties;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileAccessGuardTest {

    @Mock
    private InfraFileAccessPolicyMapper policyMapper;

    private FileAccessGuard guard;

    @BeforeEach
    void setUp() {
        // 业务策略来自表配置（测试内模拟 seed），代码内置仅 *
        when(policyMapper.selectEnabled()).thenReturn(sampleTablePolicies());
        FileAccessPolicyRegistry registry = new FileAccessPolicyRegistry(policyMapper);
        registry.refresh();
        guard = new FileAccessGuard(
                registry,
                List.of(new NoticePublishedChecker(), new ExcelStagingFileVisibilityChecker()),
                new StorageProperties());
    }

    @Test
    void matcher_prefersLongestPrefix() {
        List<FileAccessPolicy> policies = List.of(
                policy("brand_*", FileAccessAuthMode.SESSION_OR_ANON, FileAccessTenantScope.OWN_OR_PLATFORM,
                        FileAccessVisibility.ALWAYS, Set.of("LOGIN_BRAND"), 10),
                policy("notice_*", FileAccessAuthMode.SESSION, FileAccessTenantScope.OWN_OR_PLATFORM,
                        FileAccessVisibility.CUSTOM, Set.of(), 20),
                policy("banner_*", FileAccessAuthMode.SESSION, FileAccessTenantScope.OWN_OR_PLATFORM,
                        FileAccessVisibility.ALWAYS, Set.of(), 30),
                policy("user-avatar", FileAccessAuthMode.SESSION, FileAccessTenantScope.OWN,
                        FileAccessVisibility.ALWAYS, Set.of(), 40),
                policy("oa_instance", FileAccessAuthMode.SESSION, FileAccessTenantScope.OWN,
                        FileAccessVisibility.ALWAYS, Set.of(), 50),
                FileAccessPolicyBuiltin.DEFAULT);

        assertEquals("brand_*", FileAccessPolicyMatcher.resolve("brand_logo_sm", policies).bizTypePattern());
        assertEquals("notice_*", FileAccessPolicyMatcher.resolve("notice_richtext", policies).bizTypePattern());
        assertEquals("banner_*", FileAccessPolicyMatcher.resolve("banner_image", policies).bizTypePattern());
        assertEquals("user-avatar", FileAccessPolicyMatcher.resolve("user-avatar", policies).bizTypePattern());
        assertEquals("oa_instance", FileAccessPolicyMatcher.resolve("oa_instance", policies).bizTypePattern());
        assertSame(FileAccessPolicyBuiltin.DEFAULT, FileAccessPolicyMatcher.resolve("oa_leave", policies));
    }

    @Test
    void session_own_allowsSameTenant() {
        InfraFileEntity file = file("f1", "t1", "avatar");
        FileAccessCaller caller = new FileAccessCaller(true, "u1", "t1", Set.of(), FileAccessPurpose.DOWNLOAD);
        guard.assertAccessible(file, caller);
    }

    @Test
    void session_own_deniesOtherTenant() {
        InfraFileEntity file = file("f1", "t2", "avatar");
        FileAccessCaller caller = new FileAccessCaller(true, "u1", "t1", Set.of(), FileAccessPurpose.DOWNLOAD);
        assertThrows(BusinessException.class, () -> guard.assertAccessible(file, caller));
    }

    @Test
    void brand_allowsTenantReadPlatformFile() {
        InfraFileEntity file = file("f1", TenantConstants.PLATFORM_TENANT_ID, "brand_logo_md");
        FileAccessCaller caller = new FileAccessCaller(true, "u1", "t1", Set.of(), FileAccessPurpose.DOWNLOAD);
        guard.assertAccessible(file, caller);
    }

    @Test
    void brand_anon_loginBrand_allowed() {
        InfraFileEntity file = file("f1", "t1", "brand_favicon");
        FileAccessCaller caller = FileAccessCaller.anonymous(FileAccessPurpose.LOGIN_BRAND);
        guard.assertAccessible(file, caller);
    }

    @Test
    void brand_anon_download_denied() {
        InfraFileEntity file = file("f1", "t1", "brand_favicon");
        FileAccessCaller caller = FileAccessCaller.anonymous(FileAccessPurpose.DOWNLOAD);
        assertThrows(BusinessException.class, () -> guard.assertAccessible(file, caller));
    }

    @Test
    void platformOperator_deniedOtherTenantOnOwnScope() {
        InfraFileEntity file = file("f1", "t9", "avatar");
        FileAccessCaller caller = new FileAccessCaller(
                true, "ops", TenantConstants.PLATFORM_TENANT_ID, Set.of(), FileAccessPurpose.DOWNLOAD);
        assertThrows(BusinessException.class, () -> guard.assertAccessible(file, caller));
    }

    @Test
    void platformOperator_readsSameTenantFile() {
        InfraFileEntity file = file("f1", TenantConstants.PLATFORM_TENANT_ID, "avatar");
        FileAccessCaller caller = new FileAccessCaller(
                true, "ops", TenantConstants.PLATFORM_TENANT_ID, Set.of(), FileAccessPurpose.DOWNLOAD);
        guard.assertAccessible(file, caller);
    }

    @Test
    void notice_custom_visibility() {
        InfraFileEntity file = file("f1", "t1", "notice_richtext");
        FileAccessCaller ok = new FileAccessCaller(true, "u1", "t1", Set.of("notice:ok"), FileAccessPurpose.DOWNLOAD);
        guard.assertAccessible(file, ok);

        FileAccessCaller denied = new FileAccessCaller(true, "u1", "t1", Set.of(), FileAccessPurpose.DOWNLOAD);
        assertThrows(BusinessException.class, () -> guard.assertAccessible(file, denied));
    }

    @Test
    void excelExport_onlySubmitterCanDownload() {
        InfraFileEntity file = file("f1", "t1", "excel-export");
        file.setUserId("owner");
        FileAccessCaller owner = new FileAccessCaller(true, "owner", "t1", Set.of(), FileAccessPurpose.DOWNLOAD);
        guard.assertAccessible(file, owner);

        FileAccessCaller peer = new FileAccessCaller(true, "peer", "t1", Set.of(), FileAccessPurpose.DOWNLOAD);
        assertThrows(BusinessException.class, () -> guard.assertAccessible(file, peer));
    }

    @Test
    void excelImport_onlySubmitterCanDownload() {
        InfraFileEntity file = file("f1", "t1", "excel-import");
        file.setUserId("owner");
        FileAccessCaller owner = new FileAccessCaller(true, "owner", "t1", Set.of(), FileAccessPurpose.DOWNLOAD);
        guard.assertAccessible(file, owner);

        FileAccessCaller peer = new FileAccessCaller(true, "peer", "t1", Set.of(), FileAccessPurpose.DOWNLOAD);
        assertThrows(BusinessException.class, () -> guard.assertAccessible(file, peer));
    }

    @Test
    void customVisibility_firstClaimingCheckerDenyDoesNotFallThrough() {
        when(policyMapper.selectEnabled()).thenReturn(sampleTablePolicies());
        FileAccessPolicyRegistry registry = new FileAccessPolicyRegistry(policyMapper);
        registry.refresh();
        FileAccessGuard exclusiveGuard = new FileAccessGuard(
                registry,
                List.of(
                        new ExcelStagingFileVisibilityChecker(),
                        new AlwaysAllowExcelChecker()),
                new StorageProperties());

        InfraFileEntity file = file("f1", "t1", "excel-export");
        file.setUserId("owner");
        FileAccessCaller peer = new FileAccessCaller(true, "peer", "t1", Set.of(), FileAccessPurpose.DOWNLOAD);
        assertThrows(BusinessException.class, () -> exclusiveGuard.assertAccessible(file, peer));
    }

    @Test
    void excelStagingPolicyCannotBeWeakenedByTableRow() {
        when(policyMapper.selectEnabled()).thenReturn(List.of(
                row("excel-export", "SESSION", "OWN", "ALWAYS", null, 1),
                row("*", "SESSION", "OWN", "ALWAYS", null, 9999)));
        FileAccessPolicyRegistry registry = new FileAccessPolicyRegistry(policyMapper);
        registry.refresh();
        FileAccessPolicy policy = registry.resolve("excel-export");
        assertEquals(FileAccessVisibility.CUSTOM, policy.visibility());
        assertEquals(FileAccessPolicyBuiltin.EXCEL_EXPORT.bizTypePattern(), policy.bizTypePattern());
    }

    @Test
    void oaInstancePolicyCannotBeWeakenedByTableRow() {
        when(policyMapper.selectEnabled()).thenReturn(List.of(
                row("oa_instance", "SESSION", "OWN", "ALWAYS", null, 1),
                row("*", "SESSION", "OWN", "ALWAYS", null, 9999)));
        FileAccessPolicyRegistry registry = new FileAccessPolicyRegistry(policyMapper);
        registry.refresh();
        FileAccessPolicy policy = registry.resolve("oa_instance");
        assertEquals(FileAccessVisibility.CUSTOM, policy.visibility());
        assertEquals(FileAccessPolicyBuiltin.OA_INSTANCE.bizTypePattern(), policy.bizTypePattern());
    }

    private static List<InfraFileAccessPolicyEntity> sampleTablePolicies() {
        return List.of(
                row("brand_*", "SESSION_OR_ANON", "OWN_OR_PLATFORM", "ALWAYS", "LOGIN_BRAND", 10),
                row("notice_*", "SESSION", "OWN_OR_PLATFORM", "CUSTOM", null, 20),
                row("banner_*", "SESSION", "OWN_OR_PLATFORM", "ALWAYS", null, 30),
                row("user-avatar", "SESSION", "OWN", "ALWAYS", null, 40),
                row("oa_instance", "SESSION", "OWN", "ALWAYS", null, 50),
                row("*", "SESSION", "OWN", "ALWAYS", null, 9999));
    }

    private static InfraFileAccessPolicyEntity row(
            String pattern, String auth, String tenant, String visibility, String anon, int sortNo) {
        InfraFileAccessPolicyEntity e = new InfraFileAccessPolicyEntity();
        e.setBizTypePattern(pattern);
        e.setAuthMode(auth);
        e.setTenantScope(tenant);
        e.setVisibility(visibility);
        e.setAnonPurposes(anon);
        e.setSortNo(sortNo);
        e.setEnabled(CommonConstants.YES);
        return e;
    }

    private static FileAccessPolicy policy(
            String pattern,
            FileAccessAuthMode auth,
            FileAccessTenantScope tenant,
            FileAccessVisibility visibility,
            Set<String> anon,
            int sortNo) {
        return new FileAccessPolicy(pattern, auth, tenant, visibility, anon, Set.of(), sortNo, null);
    }

    private static InfraFileEntity file(String id, String tenantId, String bizType) {
        InfraFileEntity entity = new InfraFileEntity();
        entity.setId(id);
        entity.setTenantId(tenantId);
        entity.setBizType(bizType);
        entity.setDeleted(0);
        return entity;
    }

    private static final class NoticePublishedChecker implements FileAccessVisibilityChecker {
        @Override
        public boolean supports(InfraFileEntity file, FileAccessCaller caller) {
            return file.getBizType() != null && file.getBizType().startsWith("notice_");
        }

        @Override
        public boolean isVisible(InfraFileEntity file, FileAccessCaller caller) {
            return caller.permissions().contains("notice:ok");
        }
    }

    /** Malicious / accidental SPI that would previously allow peer excel download via OR fall-through. */
    private static final class AlwaysAllowExcelChecker implements FileAccessVisibilityChecker {
        @Override
        public boolean supports(InfraFileEntity file, FileAccessCaller caller) {
            return file.getBizType() != null && file.getBizType().startsWith("excel-");
        }

        @Override
        public boolean isVisible(InfraFileEntity file, FileAccessCaller caller) {
            return true;
        }
    }
}
