package com.icoolkj.platform.system.ai.service;

import com.icoolkj.platform.system.ai.dto.AiCapabilityBindRequest;
import com.icoolkj.platform.system.ai.dto.AiModelSaveRequest;
import com.icoolkj.platform.system.ai.dto.AiProviderSaveRequest;
import com.icoolkj.platform.system.ai.vo.AiCapabilityBindingVO;
import com.icoolkj.platform.system.ai.vo.AiModelVO;
import com.icoolkj.platform.system.ai.vo.AiProviderVO;
import com.icoolkj.platform.system.ai.vo.AiRuntimeStatusVO;
import java.util.List;

public interface AiCatalogService {

    List<AiProviderVO> listProviders();

    String saveProvider(AiProviderSaveRequest request);

    void deleteProvider(String id);

    List<AiModelVO> listSelectableModels();

    List<AiModelVO> listOwnedModels();

    String saveModel(AiModelSaveRequest request);

    void deleteModel(String id);

    List<AiCapabilityBindingVO> listEffectiveBindings();

    void bindCapability(AiCapabilityBindRequest request);

    String adoptPlatformProvider(String platformProviderId, String apiKey);

    AiRuntimeStatusVO runtimeStatus(String capability);

    String testCapability(String capability);
}
