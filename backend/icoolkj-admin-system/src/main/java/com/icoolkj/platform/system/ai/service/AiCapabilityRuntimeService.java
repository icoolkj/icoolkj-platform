package com.icoolkj.platform.system.ai.service;

import com.icoolkj.platform.system.ai.support.AiResolvedRuntime;

public interface AiCapabilityRuntimeService {

    AiResolvedRuntime resolve(String capability);

    AiResolvedRuntime resolve(String tenantId, String capability);
}
