package com.icoolkj.platform.common.util;

import com.icoolkj.platform.common.id.IdGenerator;
import com.icoolkj.platform.common.id.SnowflakeIdGenerator;

/**
 * Unified entry for snowflake IDs across the platform.
 */
public final class IdGeneratorUtils {

    private static final IdGenerator FALLBACK = new SnowflakeIdGenerator(1L, 1L);

    private static volatile IdGenerator delegate;

    private IdGeneratorUtils() {
    }

    public static void bind(IdGenerator idGenerator) {
        delegate = idGenerator;
    }

    public static long nextId() {
        IdGenerator generator = delegate != null ? delegate : FALLBACK;
        return generator.nextId();
    }

    public static String nextIdStr() {
        return String.valueOf(nextId());
    }
}
