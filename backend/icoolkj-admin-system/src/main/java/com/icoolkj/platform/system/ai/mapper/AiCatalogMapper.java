package com.icoolkj.platform.system.ai.mapper;

import com.icoolkj.platform.system.ai.domain.AiCapabilityBindingEntity;
import com.icoolkj.platform.system.ai.domain.AiModelEntity;
import com.icoolkj.platform.system.ai.domain.AiProviderEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiCatalogMapper {

    AiProviderEntity selectProviderById(@Param("id") String id);

    AiProviderEntity selectProviderByCode(@Param("tenantId") String tenantId, @Param("providerCode") String providerCode);

    List<AiProviderEntity> selectProviders(@Param("tenantId") String tenantId);

    int insertProvider(AiProviderEntity entity);

    int updateProvider(AiProviderEntity entity);

    int purgeProvider(@Param("id") String id);

    AiModelEntity selectModelById(@Param("id") String id);

    List<AiModelEntity> selectModelsByTenant(@Param("tenantId") String tenantId);

    List<AiModelEntity> selectModelsByProvider(@Param("providerId") String providerId);

    List<AiModelEntity> selectSelectableModels(@Param("tenantId") String tenantId);

    int insertModel(AiModelEntity entity);

    int updateModel(AiModelEntity entity);

    int purgeModel(@Param("id") String id);

    int purgeModelsByProvider(@Param("providerId") String providerId);

    AiCapabilityBindingEntity selectBinding(@Param("tenantId") String tenantId, @Param("capability") String capability);

    List<AiCapabilityBindingEntity> selectBindings(@Param("tenantId") String tenantId);

    int insertBinding(AiCapabilityBindingEntity entity);

    int updateBinding(AiCapabilityBindingEntity entity);

    int purgeBinding(@Param("id") String id);

    int purgeBindingsByModel(@Param("modelId") String modelId);

    int purgeBindingsByProvider(@Param("providerId") String providerId);
}
