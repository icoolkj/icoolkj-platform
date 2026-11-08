package com.icoolkj.platform.workflow.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.icoolkj.platform.workflow.domain.WfModelEntity;
import com.icoolkj.platform.workflow.dto.WorkflowModelPageQuery;
import com.icoolkj.platform.workflow.vo.WorkflowModelOptionVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WfModelMapper {

    /** 平台共享模型按 id 查询（含 tenant_id=0），运行时需自行校验权限 */
    @InterceptorIgnore(tenantLine = "true")
    WfModelEntity selectById(@Param("id") String id);

    @InterceptorIgnore(tenantLine = "true")
    WfModelEntity selectByTenantAndKey(@Param("tenantId") String tenantId, @Param("modelKey") String modelKey);

    /** 按 modelKey 解析表单路由/名称，含平台级模型 */
    @InterceptorIgnore(tenantLine = "true")
    WfModelEntity selectByModelKey(@Param("modelKey") String modelKey);

    /** 按 bizType 匹配租户模型或平台模型（tenant_id=0） */
    @InterceptorIgnore(tenantLine = "true")
    WfModelEntity selectActiveByBizType(@Param("tenantId") String tenantId, @Param("bizType") String bizType);

    /** 仅平台模板（platform_mandatory 策略） */
    @InterceptorIgnore(tenantLine = "true")
    WfModelEntity selectPlatformByBizType(@Param("bizType") String bizType);

    /** 仅指定业务租户模型（tenant_required，不回落平台） */
    @InterceptorIgnore(tenantLine = "true")
    WfModelEntity selectTenantByBizType(@Param("tenantId") String tenantId, @Param("bizType") String bizType);

    /**
     * 按 modelKey 解析，优先业务租户模型，再回落平台。
     */
    @InterceptorIgnore(tenantLine = "true")
    WfModelEntity selectByModelKeyPreferTenant(@Param("modelKey") String modelKey,
                                               @Param("preferTenantId") String preferTenantId);

    /** 平台目录模型（tenant_id=0，排除 platform_mandatory） */
    @InterceptorIgnore(tenantLine = "true")
    List<WfModelEntity> selectPlatformSyncCatalog();

    List<WfModelEntity> selectPage(WorkflowModelPageQuery query);

    long countByQuery(WorkflowModelPageQuery query);

    int insert(WfModelEntity entity);

    int updateById(WfModelEntity entity);

    @InterceptorIgnore(tenantLine = "true")
    List<WorkflowModelOptionVO> selectOptions(@Param("scopeTenantId") String scopeTenantId,
                                              @Param("includePlatform") boolean includePlatform);
}
