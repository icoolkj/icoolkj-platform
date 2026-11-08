package com.icoolkj.platform.infra.async;

/** Outcome snapshot after execution (for logging / future Redis ACK). */
public record AsyncTaskResult(String taskId, String status, String resultData, String errorMessage) {
}
