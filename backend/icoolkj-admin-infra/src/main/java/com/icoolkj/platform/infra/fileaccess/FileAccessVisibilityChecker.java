package com.icoolkj.platform.infra.fileaccess;

import com.icoolkj.platform.infra.domain.InfraFileEntity;

/**
 * 业务可见性 SPI：{@link FileAccessVisibility#CUSTOM} 时由实现类判定。
 * <p>系统模块可注册公告等 checker；infra 不依赖业务模块。
 */
public interface FileAccessVisibilityChecker {

    /**
     * 是否处理该文件（通常看 biz_type 或 purpose）。
     */
    boolean supports(InfraFileEntity file, FileAccessCaller caller);

    /**
     * 调用方是否可见该文件内容。
     */
    boolean isVisible(InfraFileEntity file, FileAccessCaller caller);
}
