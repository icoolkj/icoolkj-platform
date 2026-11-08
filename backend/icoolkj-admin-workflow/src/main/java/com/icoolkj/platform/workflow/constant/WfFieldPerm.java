package com.icoolkj.platform.workflow.constant;

import org.springframework.util.StringUtils;

/** 节点 × 字段权限：R 只读 / W 可写 / H 隐藏（存 design_json，不另建表） */
public final class WfFieldPerm {

    public static final String R = "R";
    public static final String W = "W";
    public static final String H = "H";

    private WfFieldPerm() {
    }

    public static boolean isValid(String perm) {
        if (!StringUtils.hasText(perm)) {
            return false;
        }
        String p = perm.trim().toUpperCase();
        return R.equals(p) || W.equals(p) || H.equals(p);
    }

    /** 非法或缺省 → R（与当前审批页只读默认一致） */
    public static String normalize(String perm) {
        if (!StringUtils.hasText(perm)) {
            return R;
        }
        String p = perm.trim().toUpperCase();
        return isValid(p) ? p : R;
    }

    public static boolean isWritable(String perm) {
        return W.equals(normalize(perm));
    }

    public static boolean isHidden(String perm) {
        return H.equals(normalize(perm));
    }
}
