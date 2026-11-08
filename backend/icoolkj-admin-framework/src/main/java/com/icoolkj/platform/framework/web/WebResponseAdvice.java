package com.icoolkj.platform.framework.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.model.response.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class WebResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    public WebResponseAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> parameterType = returnType.getParameterType();
        return !ApiResponse.class.isAssignableFrom(parameterType)
                && !ResponseEntity.class.isAssignableFrom(parameterType)
                && !Resource.class.isAssignableFrom(parameterType)
                && !byte[].class.isAssignableFrom(parameterType);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (shouldSkip(request) || body instanceof ApiResponse<?>) {
            return body;
        }
        ApiResponse<Object> wrapped = ApiResponse.ok(body);
        if (body instanceof String) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            try {
                return objectMapper.writeValueAsString(wrapped);
            } catch (JsonProcessingException ex) {
                throw new IllegalStateException("Failed to serialize unified response", ex);
            }
        }
        return wrapped;
    }

    private boolean shouldSkip(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return path.startsWith("/api/v3/api-docs")
                || path.startsWith("/api/swagger-ui")
                || path.startsWith("/api/actuator")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/actuator");
    }
}
