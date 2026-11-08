package com.icoolkj.platform.infra.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.datascope.RowAccessSupport;
import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.infra.async.AsyncTaskStatus;
import com.icoolkj.platform.infra.domain.AsyncTaskEntity;
import com.icoolkj.platform.infra.dto.AsyncTaskPageQuery;
import com.icoolkj.platform.infra.excel.ExcelExportAsyncConstants;
import com.icoolkj.platform.infra.mapper.AsyncTaskMapper;
import com.icoolkj.platform.infra.service.InfraFileService;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AsyncTaskServiceImplOwnerFilterTest {

    @Mock
    private AsyncTaskMapper asyncTaskMapper;
    @Mock
    private RowAccessSupport rowAccessSupport;
    @Mock
    private InfraFileService infraFileService;

    private AsyncTaskServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AsyncTaskServiceImpl(asyncTaskMapper, rowAccessSupport, infraFileService);
        LoginUser login = new LoginUser();
        login.setUserId("u-self");
        login.setTenantId("t1");
        login.setUsername("self");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(login, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void page_forcesCurrentUserId() {
        AsyncTaskPageQuery query = new AsyncTaskPageQuery();
        query.setPageNo(1L);
        query.setPageSize(10L);
        query.setUserId("u-peer"); // client tamper ignored
        when(asyncTaskMapper.countByQuery(any())).thenReturn(0L);

        PageResult<?> result = service.page(query);
        assertEquals(0L, result.getTotal());

        ArgumentCaptor<AsyncTaskPageQuery> captor = ArgumentCaptor.forClass(AsyncTaskPageQuery.class);
        verify(asyncTaskMapper).countByQuery(captor.capture());
        assertEquals("u-self", captor.getValue().getUserId());
    }

    @Test
    void delete_rejectsPeerTaskAndDoesNotReleaseFiles() {
        AsyncTaskEntity peer = new AsyncTaskEntity();
        peer.setId("task-1");
        peer.setTenantId("t1");
        peer.setUserId("u-peer");
        peer.setTaskStatus(AsyncTaskStatus.SUCCESS);
        peer.setBizType(ExcelExportAsyncConstants.BIZ_TYPE);
        when(asyncTaskMapper.selectById("task-1")).thenReturn(peer);

        assertThrows(BusinessException.class, () -> service.deleteById("task-1"));
        verify(asyncTaskMapper, never()).updateById(any());
        verify(infraFileService, never()).releaseBizFiles(any(), any(), any());
    }

    @Test
    void delete_ownerReleasesExcelStaging() {
        AsyncTaskEntity own = new AsyncTaskEntity();
        own.setId("task-2");
        own.setTenantId("t1");
        own.setUserId("u-self");
        own.setTaskStatus(AsyncTaskStatus.SUCCESS);
        own.setBizType(ExcelExportAsyncConstants.BIZ_TYPE);
        when(asyncTaskMapper.selectById("task-2")).thenReturn(own);

        service.deleteById("task-2");

        verify(asyncTaskMapper).updateById(own);
        verify(infraFileService).releaseBizFiles("t1", ExcelExportAsyncConstants.BIZ_TYPE, "task-2");
    }
}
