package com.icoolkj.platform.system.auth.service;

import com.icoolkj.platform.system.auth.vo.AuthLoginBrandVO;

/**
 * 登录前按租户编码解析公开品牌（匿名）。
 * 素材定位：{@code catalogCode + profileCode}。
 */
public interface LoginBrandService {

    AuthLoginBrandVO resolveByTenantCode(String tenantCode);

    /**
     * 校验槽位属于该目录后，返回文件 id。
     */
    String resolveAssetFileId(String tenantCode, String catalogCode, String profileCode);
}
