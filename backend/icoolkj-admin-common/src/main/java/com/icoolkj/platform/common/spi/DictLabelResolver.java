package com.icoolkj.platform.common.spi;

/**
 * 字典 value → 展示标签（与字典管理 {@code dict_label} 同源）。
 * <p>OA / 工作流等模块勿直接依赖 system；缺省实现可回落为 value 本身。
 */
@FunctionalInterface
public interface DictLabelResolver {

    /**
     * @return 字典标签；未命中时返回 {@code null}（调用方再决定是否回落 value）
     */
    String resolveLabel(String dictType, String dictValue);
}
