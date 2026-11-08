package com.icoolkj.platform.oa.fileaccess;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.framework.auth.DataPermissionService;
import com.icoolkj.platform.framework.datascope.DataScopeSpec;
import com.icoolkj.platform.infra.domain.InfraFileEntity;
import com.icoolkj.platform.infra.fileaccess.FileAccessCaller;
import com.icoolkj.platform.infra.fileaccess.FileAccessPurpose;
import com.icoolkj.platform.infra.mapper.InfraFileRefMapper;
import com.icoolkj.platform.oa.constant.OaInstanceFileConstants;
import com.icoolkj.platform.oa.domain.OaInstanceEntity;
import com.icoolkj.platform.oa.mapper.OaInstanceMapper;
import com.icoolkj.platform.workflow.domain.WfBizProcessEntity;
import com.icoolkj.platform.workflow.support.WorkflowAccessGuard;
import com.icoolkj.platform.workflow.support.WorkflowBizProcessLookup;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OaInstanceFileVisibilityCheckerTest {

    @Mock
    private OaInstanceMapper oaInstanceMapper;
    @Mock
    private InfraFileRefMapper infraFileRefMapper;
    @Mock
    private WorkflowBizProcessLookup workflowBizProcessLookup;
    @Mock
    private WorkflowAccessGuard workflowAccessGuard;
    @Mock
    private DataPermissionService dataPermissionService;

    private OaInstanceFileVisibilityChecker checker;

    @BeforeEach
    void setUp() {
        checker = new OaInstanceFileVisibilityChecker(
                oaInstanceMapper,
                infraFileRefMapper,
                workflowBizProcessLookup,
                workflowAccessGuard,
                dataPermissionService);
    }

    @Test
    void supports_onlyOaInstanceBizType() {
        InfraFileEntity file = file("f1", "oa_instance", "inst-1");
        assertTrue(checker.supports(file, caller("u1")));
        file.setBizType("notice_cover");
        assertFalse(checker.supports(file, caller("u1")));
    }

    @Test
    void isVisible_draftUploaderAllowedWithoutInstance() {
        InfraFileEntity file = file("f1", OaInstanceFileConstants.BIZ_TYPE, null);
        file.setUserId("uploader");
        when(infraFileRefMapper.selectActiveByFileId("t1", "f1")).thenReturn(List.of());

        assertTrue(checker.isVisible(file, caller("uploader")));
        assertFalse(checker.isVisible(file, caller("peer")));
        verify(oaInstanceMapper, never()).selectById(anyString());
    }

    @Test
    void isVisible_starterAllowed() {
        InfraFileEntity file = file("f1", OaInstanceFileConstants.BIZ_TYPE, "inst-1");
        OaInstanceEntity instance = instance("inst-1", "starter", null);
        when(oaInstanceMapper.selectById("inst-1")).thenReturn(instance);

        assertTrue(checker.isVisible(file, caller("starter")));
        verify(workflowAccessGuard, never()).assertCanViewInstance(anyString(), any());
    }

    @Test
    void isVisible_workflowParticipantAllowed() {
        InfraFileEntity file = file("f1", OaInstanceFileConstants.BIZ_TYPE, "inst-1");
        OaInstanceEntity instance = instance("inst-1", "starter", "pi-1");
        when(oaInstanceMapper.selectById("inst-1")).thenReturn(instance);
        WfBizProcessEntity biz = new WfBizProcessEntity();
        when(workflowBizProcessLookup.findByProcessInstanceId("pi-1")).thenReturn(biz);

        assertTrue(checker.isVisible(file, caller("assignee")));
        verify(workflowAccessGuard).assertCanViewInstance("assignee", biz);
    }

    @Test
    void isVisible_peerDeniedWithoutScopeOrProcess() {
        InfraFileEntity file = file("f1", OaInstanceFileConstants.BIZ_TYPE, "inst-1");
        OaInstanceEntity instance = instance("inst-1", "starter", null);
        when(oaInstanceMapper.selectById("inst-1")).thenReturn(instance);
        when(dataPermissionService.isReadable(any(DataScopeSpec.class), eq("owner"), eq("d1")))
                .thenReturn(false);

        assertFalse(checker.isVisible(file, caller("peer")));
    }

    @Test
    void isVisible_dataScopeReadableAllowed() {
        InfraFileEntity file = file("f1", OaInstanceFileConstants.BIZ_TYPE, "inst-1");
        OaInstanceEntity instance = instance("inst-1", "starter", null);
        when(oaInstanceMapper.selectById("inst-1")).thenReturn(instance);
        when(dataPermissionService.isReadable(any(DataScopeSpec.class), eq("owner"), eq("d1")))
                .thenReturn(true);

        assertTrue(checker.isVisible(file, caller("manager")));
    }

    @Test
    void isVisible_workflowDeniedFallsThroughToScope() {
        InfraFileEntity file = file("f1", OaInstanceFileConstants.BIZ_TYPE, "inst-1");
        OaInstanceEntity instance = instance("inst-1", "starter", "pi-1");
        when(oaInstanceMapper.selectById("inst-1")).thenReturn(instance);
        WfBizProcessEntity biz = new WfBizProcessEntity();
        when(workflowBizProcessLookup.findByProcessInstanceId("pi-1")).thenReturn(biz);
        doThrow(new BusinessException("error.workflow.instance_access_denied"))
                .when(workflowAccessGuard).assertCanViewInstance("peer", biz);
        when(dataPermissionService.isReadable(any(DataScopeSpec.class), eq("owner"), eq("d1")))
                .thenReturn(false);

        assertFalse(checker.isVisible(file, caller("peer")));
    }

    private static FileAccessCaller caller(String userId) {
        return new FileAccessCaller(true, userId, "t1", Set.of(), FileAccessPurpose.DOWNLOAD);
    }

    private static InfraFileEntity file(String id, String bizType, String bizId) {
        InfraFileEntity entity = new InfraFileEntity();
        entity.setId(id);
        entity.setTenantId("t1");
        entity.setBizType(bizType);
        entity.setBizId(bizId);
        return entity;
    }

    private static OaInstanceEntity instance(String id, String starter, String processInstanceId) {
        OaInstanceEntity entity = new OaInstanceEntity();
        entity.setId(id);
        entity.setTenantId("t1");
        entity.setStarterUserId(starter);
        entity.setUserId("owner");
        entity.setDeptId("d1");
        entity.setCategoryCode("oa_leave");
        entity.setProcessInstanceId(processInstanceId);
        return entity;
    }
}
