package com.icoolkj.platform.workflow.design.gateway;

import com.icoolkj.platform.workflow.constant.DesignNodeTypes;
import java.util.Locale;
import org.springframework.util.StringUtils;

/** 网关类型判断（替代散落的三元 nodeType 字符串比较） */
public final class GatewayKindSupport {

    private GatewayKindSupport() {
    }

    public static String normalize(String nodeType) {
        if (!StringUtils.hasText(nodeType)) {
            return DesignNodeTypes.APPROVE;
        }
        return nodeType.trim().toLowerCase(Locale.ROOT);
    }

    public static GatewayKind resolve(String nodeType) {
        String type = normalize(nodeType);
        if (DesignNodeTypes.CONDITION.equals(type)) {
            return GatewayKind.CONDITION;
        }
        if (DesignNodeTypes.INCLUSIVE.equals(type)) {
            return GatewayKind.INCLUSIVE;
        }
        if (DesignNodeTypes.PARALLEL.equals(type)) {
            return GatewayKind.PARALLEL;
        }
        return null;
    }

    public static boolean isAnyGateway(String nodeType) {
        return resolve(nodeType) != null;
    }

    public static boolean isConditionLike(String nodeType) {
        GatewayKind kind = resolve(nodeType);
        return kind != null && kind.isConditionLike();
    }

    public static boolean isExclusive(String nodeType) {
        return GatewayKind.CONDITION.nodeType().equals(normalize(nodeType));
    }

    public static boolean isParallel(String nodeType) {
        return GatewayKind.PARALLEL.nodeType().equals(normalize(nodeType));
    }
}
