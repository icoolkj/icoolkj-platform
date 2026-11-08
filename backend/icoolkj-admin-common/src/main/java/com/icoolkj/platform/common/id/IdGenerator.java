package com.icoolkj.platform.common.id;

/**
 * Distributed unique ID generator (snowflake).
 */
public interface IdGenerator {

    long nextId();

    default String nextIdStr() {
        return String.valueOf(nextId());
    }
}
