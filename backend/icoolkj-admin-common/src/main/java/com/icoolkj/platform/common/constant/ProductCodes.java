package com.icoolkj.platform.common.constant;

/**
 * Console Shell 内置产品宇宙编码。
 * <p>业务宇宙使用自定义 code（如 {@code geo}），{@code sort_no} 应小于内置能力段，保证顶栏业务靠前。
 */
public final class ProductCodes {

    public static final String PLATFORM = "platform";
    public static final String OA = "oa";
    public static final String FILES = "files";
    public static final String AI = "ai";

    /** 业务宇宙建议占用 1–49 */
    public static final int SORT_BIZ_MAX = 49;
    /** 内置能力段起点（oa/files/ai） */
    public static final int SORT_BUILTIN_MIN = 50;
    /** 平台底座建议值 */
    public static final int SORT_PLATFORM = 90;

    private ProductCodes() {
    }
}
